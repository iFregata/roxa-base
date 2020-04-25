/**
 * The MIT License
 * 
 * Copyright (c) 2019-2022 Shell Technologies PTY LTD
 *
 * You may obtain a copy of the License at
 * 
 *       http://mit-license.org/
 *       
 */
package io.roxa.vertx.rx.cassandra;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Row;

import io.reactivex.Single;
import io.roxa.vertx.rx.EventActionDispatcher;
import io.roxa.vertx.rx.JsonAsync;
import io.vertx.cassandra.CassandraClientOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.cassandra.CassandraClient;

/**
 * @author Steven Chen
 *
 */
public class CassandraResource extends EventActionDispatcher {

	private CassandraClient client;

	private Map<String, PreparedStatement> pstmtCache = new ConcurrentHashMap<>();

	private String resourceName;

	private int port;

	private List<String> endpoints;

	public CassandraResource(String resourceName, List<String> endpoints, int port) {
		super(String.format("roxa.resource.cassandra::%s", resourceName));
		this.endpoints = endpoints;
		this.resourceName = resourceName;
		this.port = port;

	}

	protected void didSetupDispatch() {
		CassandraClientOptions options = new CassandraClientOptions();
		options.setContactPoints(endpoints).setPort(port);
		client = CassandraClient.createShared(vertx, resourceName, options);
	}

	public Single<Integer> batchUpdate(JsonObject msg) {
		String cql = msg.getString("cql");
		List<JsonArray> batchParams = msg.getJsonArray("params").stream().map(i -> (JsonArray) i)
				.collect(Collectors.toList());
		return getPreparedStatement(cql).flatMap(pstmt -> {
			BatchStatement batchStatement = new BatchStatement();
			batchParams.stream().forEach(params -> {
				BoundStatement bstmt = bindParamenters(pstmt, params);
				batchStatement.add(bstmt);
			});
			return client.rxExecute(batchStatement).map(affect -> 0);
		});
	}

	public Single<Integer> update(JsonObject msg) {
		String cql = msg.getString("cql");
		JsonArray params = msg.getJsonArray("params");
		return getPreparedStatement(cql).flatMap(pstmt -> {
			BoundStatement bstmt = bindParamenters(pstmt, params);
			return client.rxExecute(bstmt).map(affect -> 0);
		});
	}

	public Single<JsonObject> queryFirstRow(JsonObject msg) {
		return queryRows(msg).map(rows -> {
			if (rows == null || rows.isEmpty())
				return JsonAsync.EMPTY_JSON;
			return rows.getJsonObject(0);
		});
	}

	public Single<JsonArray> queryRows(JsonObject msg) {
		String cql = msg.getString("cql");
		JsonArray params = msg.getJsonArray("params");
		return getPreparedStatement(cql).flatMap(pstmt -> {
			BoundStatement bstmt = bindParamenters(pstmt, params);
			return client.rxExecuteWithFullFetch(bstmt).map(list -> {
				return list.stream().map(row -> toJson(row)).collect(JsonArray::new, JsonArray::add, JsonArray::addAll);
			});
		});
	}

	private Single<PreparedStatement> getPreparedStatement(String cql) {
		if (pstmtCache.containsKey(cql))
			return Single.just(pstmtCache.get(cql));
		return client.rxPrepare(cql).map(pstmt -> {
			pstmtCache.put(cql, pstmt);
			return pstmt;
		});
	}

	private static final BoundStatement bindParamenters(PreparedStatement pstmt, JsonArray params) {
		Object[] paramsAsArray = params.stream().toArray();
		return (paramsAsArray == null || paramsAsArray.length == 0) ? pstmt.bind() : pstmt.bind(paramsAsArray);
	}

	private static final JsonObject toJson(Row row) {
		ColumnDefinitions columnDefinitions = row.getColumnDefinitions();
		JsonObject result = new JsonObject();
		columnDefinitions.asList().stream().forEach(colDef -> {
			DataType.Name dtname = colDef.getType().getName();
			String colName = colDef.getName();
			switch (dtname) {
			case SET:
				Set<String> set = row.getSet(colName, String.class);
				result.put(colName, new JsonArray(Arrays.asList(set)));
				break;
			case LIST:
				List<String> list = row.getList(colName, String.class);
				result.put(colName, new JsonArray(list));
				break;
			case MAP:
				Map<String, Object> map = row.getMap(colName, String.class, Object.class);
				result.put(colName, new JsonObject(map));
				break;
			case TIME:
				long tiem = row.getTime(colName);
				result.put(colName, tiem);
				break;
			case TIMESTAMP:
				Date date = row.getTimestamp(colName);
				result.put(colName, date.getTime());
				break;
			default:
				Object val = row.getObject(colName);
				result.put(colName, val);
				break;
			}
		});
		return result;
	}
}

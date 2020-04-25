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

import java.util.List;

import io.reactivex.Single;
import io.roxa.vertx.rx.EventActionEndpoint;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;

/**
 * @author Steven Chen
 *
 */
public class CassandraAgent {

	private final Vertx vertx;
	private final String urn;

	public static CassandraAgent create(Vertx vertx, String sourceName) {
		String urn = String.format("roxa.resource.cassandra::%s", sourceName);
		return new CassandraAgent(vertx, urn);
	}

	private CassandraAgent(Vertx vertx, String urn) {
		this.vertx = vertx;
		this.urn = urn;
	}

	public Single<JsonObject> queryFirstRow(String cql) {
		JsonObject msg = new JsonObject().put("cql", cql);
		return EventActionEndpoint.create(vertx).urn(urn).action("queryFirstRow").<JsonObject>request(msg);
	}

	public Single<JsonObject> queryFirstRow(String cql, JsonArray params) {
		JsonObject msg = new JsonObject().put("cql", cql).put("params", params.copy());
		return EventActionEndpoint.create(vertx).urn(urn).action("queryFirstRow").<JsonObject>request(msg);
	}

	public Single<JsonArray> queryRows(String cql) {
		JsonObject msg = new JsonObject().put("cql", cql);
		return EventActionEndpoint.create(vertx).urn(urn).action("queryRows").<JsonArray>request(msg);
	}

	public Single<JsonArray> queryRows(String cql, JsonArray params) {
		JsonObject msg = new JsonObject().put("cql", cql).put("params", params.copy());
		return EventActionEndpoint.create(vertx).urn(urn).action("queryRows").<JsonArray>request(msg);
	}

	public Single<Integer> update(String cql) {
		JsonObject msg = new JsonObject().put("cql", cql);
		return EventActionEndpoint.create(vertx).urn(urn).action("update").<Integer>request(msg);
	}

	public Single<Integer> update(String cql, JsonArray params) {
		JsonObject msg = new JsonObject().put("cql", cql).put("params", params.copy());
		return EventActionEndpoint.create(vertx).urn(urn).action("update").<Integer>request(msg);
	}

	public Single<Integer> batchUpdate(String cql) {
		JsonObject msg = new JsonObject().put("cql", cql);
		return EventActionEndpoint.create(vertx).urn(urn).action("batchUpdate").<Integer>request(msg);
	}

	public Single<Integer> batchUpdate(String cql, List<JsonArray> batchParams) {
		JsonObject msg = new JsonObject().put("cql", cql).put("params", new JsonArray(batchParams));
		return EventActionEndpoint.create(vertx).urn(urn).action("batchUpdate").<Integer>request(msg);
	}

}

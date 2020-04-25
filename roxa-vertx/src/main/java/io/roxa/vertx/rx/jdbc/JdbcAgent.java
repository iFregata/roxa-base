/**
 * The MIT License
 * 
 * Copyright (c) 2018-2020 Shell Technologies PTY LTD
 *
 * You may obtain a copy of the License at
 * 
 *       http://mit-license.org/
 *       
 */
package io.roxa.vertx.rx.jdbc;

import java.util.List;
import java.util.function.Function;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zaxxer.hikari.HikariDataSource;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.roxa.vertx.rx.JsonAsync;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLOptions;
import io.vertx.ext.sql.UpdateResult;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.ext.jdbc.JDBCClient;
import io.vertx.reactivex.ext.sql.SQLClientHelper;
import io.vertx.reactivex.ext.sql.SQLConnection;

/**
 * The Rxified JdbcAgent
 * 
 * @author Steven Chen
 *
 */
public class JdbcAgent extends AbstractVerticle {

	private static final Logger logger = LoggerFactory.getLogger(JdbcAgent.class);

	private JDBCClient jdbcClient;

	private HikariDataSource hikariDataSource;

	public JdbcAgent(HikariDataSource hikariDataSource) {
		this.hikariDataSource = hikariDataSource;
	}

	@Override
	public void start() throws Exception {
		jdbcClient = new JDBCClient(io.vertx.ext.jdbc.JDBCClient.create(vertx.getDelegate(), hikariDataSource));
	}

	/**
	 * @return the dataSource
	 */
	public DataSource getDataSource() {
		return this.hikariDataSource;
	}

	@Override
	public void stop() throws Exception {
		if (hikariDataSource != null)
			hikariDataSource.close();
	}

	/**
	 * Execute a one shot SQL query statement
	 * 
	 * @param sql    - the statement to execute
	 * @param params - the statement parameters
	 * @return the list of rows where each row represents as JsonArray
	 */
	public Single<List<JsonArray>> query(String sql, JsonArray params) {
		if (params == null || params.isEmpty())
			return jdbcClient.rxQuery(sql).map(ResultSet::getResults);
		else
			return jdbcClient.rxQueryWithParams(sql, params).map(ResultSet::getResults);
	}

	/**
	 * 
	 * Execute a SQL query on specified connection
	 * 
	 * @param conn   - the SQL connection
	 * @param sql    - the statement to execute
	 * @param params - the statement parameters
	 * @return
	 */
	public Single<List<JsonArray>> query(SQLConnection conn, String sql, JsonArray params) {
		if (params == null || params.isEmpty())
			return conn.rxQuery(sql).map(ResultSet::getResults);
		else
			return conn.rxQueryWithParams(sql, params).map(ResultSet::getResults);
	}

	/**
	 * Execute a one shot SQL query statement with single row result
	 * 
	 * @param sql    - the statement to execute
	 * @param params - the statement parameters
	 * @return the first item of rows that represents as JsonObject, if no data
	 *         which be empty JsonObject
	 */
	public Single<JsonObject> queryFirstRow(String sql, JsonArray params) {
		return queryRows(sql, params).map(list -> {
			if (list == null || list.isEmpty())
				return JsonAsync.EMPTY_JSON;
			return list.get(0);
		});
	}

	/**
	 * Execute a one shot SQL query statement with single row result on specified
	 * connection
	 * 
	 * @param conn   - the SQL connection
	 * @param sql    - the statement to execute
	 * @param params - the statement parameters
	 * @return the first item of rows that represents as JsonObject, if no data
	 *         which be empty JsonObject
	 */
	public Single<JsonObject> queryFirstRow(SQLConnection conn, String sql, JsonArray params) {
		return queryRows(conn, sql, params).map(list -> {
			if (list == null || list.isEmpty())
				return JsonAsync.EMPTY_JSON;
			return list.get(0);
		});
	}

	/**
	 * Execute a one shot SQL query statement
	 * 
	 * @param sql    - the statement to execute
	 * @param params - the statement parameters
	 * @return the list of rows where each row represents as JsonObject
	 */
	public Single<List<JsonObject>> queryRows(String sql, JsonArray params) {
		if (params == null || params.isEmpty())
			return jdbcClient.rxQuery(sql).map(rs -> rs.getRows(true));
		return jdbcClient.rxQueryWithParams(sql, params).map(rs -> rs.getRows(true));
	}

	/**
	 * Execute a SQL query on specified connection
	 * 
	 * @param conn   - the SQL connection
	 * @param sql    - the statement to execute
	 * @param params - the statement parameters
	 * @return
	 */
	public Single<List<JsonObject>> queryRows(SQLConnection conn, String sql, JsonArray params) {
		if (params == null || params.isEmpty())
			return conn.rxQuery(sql).map(rs -> rs.getRows(true));
		else
			return conn.rxQueryWithParams(sql, params).map(rs -> rs.getRows(true));
	}

	/**
	 * Execute a one shot SQL query statement that return a single SQL row.
	 * 
	 * @param sql    - the statement to execute
	 * @param params - the statement parameters
	 * @return the single row which represents as async JsonArray or null if there
	 *         is not data
	 */
	public Maybe<JsonArray> queryOneMaybe(String sql, JsonArray params) {
		if (params == null || params.isEmpty())
			return jdbcClient.rxQuerySingle(sql);
		return jdbcClient.rxQuerySingleWithParams(sql, params);
	}

	/**
	 * Execute a SQL query on specified connection
	 * 
	 * @param conn   - the SQL connection
	 * @param params - the statement parameters
	 * @return the single row which represents as async JsonArray or null if there
	 *         is not data
	 */
	public Maybe<JsonArray> queryOneMaybe(SQLConnection conn, String sql, JsonArray params) {
		if (params == null || params.isEmpty())
			return conn.rxQuerySingle(sql);
		return conn.rxQuerySingleWithParams(sql, params);
	}

	/**
	 * Execute a SQL update (INSERT, UPDATE, DELETE) statement on specified
	 * connection
	 * 
	 * @param conn   - the SQL connection
	 * @param sql    - the statement to execute
	 * @param params - the statement parameters
	 * @return
	 */
	public Single<JsonArray> update(SQLConnection conn, String sql, JsonArray params) {
		return update(conn, sql, params, null);
	}

	/**
	 * Execute a SQL update (INSERT, UPDATE, DELETE) statement on specified
	 * connection, and captures the generated key from database
	 * 
	 * @param conn        - the SQL connection
	 * @param sql         - the statement to execute
	 * @param params      - the statement parameters
	 * @param autoKeyName - the column name of generated key
	 * @return
	 */
	public Single<JsonArray> update(SQLConnection conn, String sql, JsonArray params, String autoKeyName) {
		if (autoKeyName != null) {
			conn.setOptions(new SQLOptions().setAutoGeneratedKeys(true)
					.setAutoGeneratedKeysIndexes(new JsonArray().add(autoKeyName)));
			logger.debug("Jdbc auto generated key: {}", autoKeyName);
		}
		Single<UpdateResult> single;
		if (params == null || params.isEmpty())
			single = conn.rxUpdate(sql);
		else
			single = conn.rxUpdateWithParams(sql, params);
		return single.map(rs -> {
			if (autoKeyName != null)
				return rs.getKeys();
			return new JsonArray().add(rs.getUpdated());
		});
	}

	/**
	 * Execute a SQL update (INSERT, UPDATE, DELETE) statement
	 * 
	 * @param sql    - the statement to execute
	 * @param params - the statement parameters, it allows null
	 * @return
	 */
	public Completable updateCompletable(String sql, JsonArray params) {
		return update(sql, params).ignoreElement();
	}

	/**
	 * Execute a SQL update (INSERT, UPDATE, DELETE) statement, return the
	 * updateResult
	 * 
	 * @param sql    - the statement to execute
	 * @param params - the statement parameters, it allows null
	 * @return
	 */
	public Single<UpdateResult> update(String sql, JsonArray params) {
		if (params == null || params.isEmpty())
			return jdbcClient.rxUpdate(sql);
		return jdbcClient.rxUpdateWithParams(sql, params);
	}

	/**
	 * Execute batch SQL on specified connection
	 * 
	 * @param conn    - the SQL connection
	 * @param sqlList - a group of SQL to execute
	 * @return
	 */
	public Single<List<Integer>> batch(SQLConnection conn, List<String> sqlList) {
		return conn.rxBatch(sqlList);
	}

	/**
	 * Execute batch SQL on specified connection
	 * 
	 * @param conn        - the SQL connection
	 * @param sql         - the SQL to execute
	 * @param batchParams - a group of parameters
	 * @return
	 */
	public Single<List<Integer>> batch(SQLConnection conn, String sql, List<JsonArray> batchParams) {
		return conn.rxBatchWithParams(sql, batchParams);
	}

	/**
	 * Calls the given SQL PROCEDURE on specified connection
	 * 
	 * @param conn          - the SQL connection
	 * @param funcStatement - standard JDBC format { call func_proc_name() }
	 * @returna list of JsonArray to represent the row
	 */
	public Single<List<JsonArray>> call(SQLConnection conn, String funcStatement) {
		return conn.rxCall(funcStatement).map(ResultSet::getResults);
	}

	/**
	 * Calls the given SQL PROCEDURE on specified connection. A JsonArray containing
	 * the parameter values and finally a JsonArray containing the output types
	 * e.g.: [null, 'VARCHAR'].
	 * <p>
	 * <code>
	 * String proc = "{ call customer_lastname(?, ?) }";
	 * executor.call(proc,new JsonArray().add("ABC"), new JsonArray().addNull().add("VARCHAR"),res -> {
	 * if (res.succeeded()) {
	 *  ResultSet result = res.result();
	 * } else {
	 *   
	 * }
	 * });
	 * </code>
	 * </p>
	 * 
	 * @param conn          - the SQL connection
	 * @param procStatement - standard JDBC format { call func_proc_name() }
	 * @param in            - the IN parameters
	 * @param out           - the OUT parameters
	 * @return a list of JsonArray to represent the row
	 */
	public Single<List<JsonArray>> call(SQLConnection conn, String procStatement, JsonArray in, JsonArray out) {
		return conn.rxCallWithParams(procStatement, in, out).map(ResultSet::getResults);
	}

	/**
	 * Calls the given SQL PROCEDURE on specified connection. A JsonArray containing
	 * the parameter values and finally a JsonArray containing the output types
	 * e.g.: [null, 'VARCHAR'].
	 * <p>
	 * <code>
	 * String proc = "{ call customer_lastname(?, ?) }";
	 * executor.call(proc,new JsonArray().add("ABC"), new JsonArray().addNull().add("VARCHAR"),res -> {
	 * if (res.succeeded()) {
	 *  ResultSet result = res.result();
	 * } else {
	 *   
	 * }
	 * });
	 * </code>
	 * </p>
	 * 
	 * @param conn          - the SQL connection
	 * @param procStatement - standard JDBC format { call func_proc_name() }
	 * @param in            - the IN parameters
	 * @param out           - the OUT parameters
	 * @return a list of JsonObject to represent the row
	 */
	public Single<List<JsonObject>> callRows(SQLConnection conn, String procStatement, JsonArray in, JsonArray out) {
		return conn.rxCallWithParams(procStatement, in, out).map(rs -> rs.getRows(true));
	}

	/**
	 * Calls the given SQL PROCEDURE on specified connection
	 * 
	 * @param conn          - the SQL connection
	 * @param procStatement - standard JDBC format { call func_proc_name() }
	 * @return a list of JsonObject to represent the row
	 */
	public Single<List<JsonObject>> callRows(SQLConnection conn, String procStatement) {
		return conn.rxCall(procStatement).map(rs -> rs.getRows(true));
	}

	/**
	 * 
	 * @param procStatement - standard JDBC format { call func_proc_name() }
	 * @param in            - the IN parameters
	 * @param out           - the OUT parameters
	 * @returna list of JsonArray to represent the row with Single
	 */
	public Single<List<JsonArray>> callSingle(String procStatement, JsonArray in, JsonArray out) {
		return jdbcClient.rxCallWithParams(procStatement, in, out).map(ResultSet::getResults);
	}

	/**
	 * 
	 * @param conn          - the SQL connection
	 * @param funcStatement - standard JDBC format { call func_proc_name() }
	 * @returna list of JsonArray to represent the row with Single
	 */
	public Single<List<JsonArray>> callSingle(String funcStatement) {
		return jdbcClient.rxCall(funcStatement).map(ResultSet::getResults);
	}

	/**
	 * 
	 * @param funcStatement - standard JDBC format { call func_proc_name() }
	 * @returna Completable
	 */
	public Completable callCompletable(String funcStatement) {
		return jdbcClient.rxCall(funcStatement).ignoreElement();
	}

	/**
	 * Calls the given SQL PROCEDURE. A JsonArray containing the parameter values
	 * and finally a JsonArray containing the output types e.g.: [null, 'VARCHAR'].
	 * <p>
	 * <code>
	 * String proc = "{ call customer_lastname(?, ?) }";
	 * executor.call(proc,new JsonArray().add("ABC"), new JsonArray().addNull().add("VARCHAR"),res -> {
	 * if (res.succeeded()) {
	 *  ResultSet result = res.result();
	 * } else {
	 *   
	 * }
	 * });
	 * </code>
	 * </p>
	 * 
	 * @param conn          - the SQL connection
	 * @param procStatement - standard JDBC format { call func_proc_name() }
	 * @param in            - the IN parameters
	 * @param out           - the OUT parameters
	 * @return a list of JsonObject to represent the row
	 */
	public Single<List<JsonObject>> callRowsSingle(String procStatement, JsonArray in, JsonArray out) {
		return jdbcClient.rxCallWithParams(procStatement, in, out).map(rs -> rs.getRows(true));
	}

	/**
	 * Calls the given SQL PROCEDURE
	 * 
	 * @param conn          - the SQL connection
	 * @param procStatement - standard JDBC format { call func_proc_name() }
	 * @return a list of JsonObject to represent the row
	 */
	public Single<List<JsonObject>> callRowsSingle(String procStatement) {
		return jdbcClient.rxCall(procStatement).map(rs -> rs.getRows(true));
	}

	/**
	 * Execute the DDL statement
	 * 
	 * @param conn         - the SQL connection
	 * @param ddlStatement - the DDL statement
	 * @return
	 */
	public Single<Integer> ddl(SQLConnection conn, String ddlStatement) {
		return conn.rxExecute(ddlStatement).andThen(Single.just(0));
	}

	/**
	 * Execute a group of JdbcExecutor operations within same connection
	 * 
	 * @param handler - the JdbcExecutor operations handler
	 * @return
	 */
	public <T> Single<T> with(Function<SQLConnection, Single<T>> handler) {
		return SQLClientHelper.usingConnectionSingle(jdbcClient, handler);
	}

	/**
	 * Execute a group of JdbcExecutor operations within same connection
	 * 
	 * @param handler - the JdbcExecutor operations handler
	 * @return
	 */
	public Completable withCompletable(Function<SQLConnection, Completable> handler) {
		return SQLClientHelper.usingConnectionCompletable(jdbcClient, handler);
	}

	/**
	 * Execute a group of JdbcExecutor operations within same connection
	 * 
	 * @param handler - the JdbcExecutor operations handler
	 * @return
	 */
	public <T> Flowable<T> withFlowable(Function<SQLConnection, Flowable<T>> handler) {
		return SQLClientHelper.usingConnectionFlowable(jdbcClient, handler);
	}

	/**
	 * Execute a group of JdbcExecutor operations within same connection
	 * 
	 * @param handler - the JdbcExecutor operations handler
	 * @return
	 */
	public <T> Maybe<T> withMaybe(Function<SQLConnection, Maybe<T>> handler) {
		return SQLClientHelper.usingConnectionMaybe(jdbcClient, handler);
	}

	/**
	 * Execute a group of JdbcExecutor operations within same connection
	 * 
	 * @param handler - the JdbcExecutor operations handler
	 * @return
	 */
	public <T> Observable<T> withObservable(Function<SQLConnection, Observable<T>> handler) {
		return SQLClientHelper.usingConnectionObservable(jdbcClient, handler);
	}

	/**
	 * Execute a group of JdbcExecutor operations within same connection and
	 * transaction
	 * 
	 * @param handler - the JdbcExecutor operations handler
	 * @return
	 */
	public <T> Single<T> tx(Function<SQLConnection, Single<T>> handler) {
		return SQLClientHelper.inTransactionSingle(jdbcClient, handler);
	}

	/**
	 * Execute a group of JdbcExecutor operations within same connection and
	 * transaction
	 * 
	 * @param handler - the JdbcExecutor operations handler
	 * @return
	 */
	public Completable txCompletable(Function<SQLConnection, Completable> handler) {
		return SQLClientHelper.inTransactionCompletable(jdbcClient, handler);
	}

	/**
	 * Execute a group of JdbcExecutor operations within same connection and
	 * transaction
	 * 
	 * @param handler - the JdbcExecutor operations handler
	 * @return
	 */
	public <T> Flowable<T> txFlowable(Function<SQLConnection, Flowable<T>> handler) {
		return SQLClientHelper.inTransactionFlowable(jdbcClient, handler);
	}

	/**
	 * Execute a group of JdbcExecutor operations within same connection and
	 * transaction
	 * 
	 * @param handler - the JdbcExecutor operations handler
	 * @return
	 */
	public <T> Maybe<T> txMaybe(Function<SQLConnection, Maybe<T>> handler) {
		return SQLClientHelper.inTransactionMaybe(jdbcClient, handler);
	}

	/**
	 * Execute a group of JdbcExecutor operations within same connection and
	 * transaction
	 * 
	 * @param handler - the JdbcExecutor operations handler
	 * @return
	 */
	public <T> Observable<T> txObservable(Function<SQLConnection, Observable<T>> handler) {
		return SQLClientHelper.inTransactionObservable(jdbcClient, handler);
	}
}

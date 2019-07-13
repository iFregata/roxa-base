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
package io.roxa.vertx.jdbc;

import java.util.List;
import java.util.function.Function;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLOptions;
import io.vertx.ext.sql.UpdateResult;
import io.vertx.reactivex.MaybeHelper;
import io.vertx.reactivex.SingleHelper;
import io.vertx.reactivex.ext.jdbc.JDBCClient;
import io.vertx.reactivex.ext.sql.SQLClientHelper;
import io.vertx.reactivex.ext.sql.SQLConnection;

/**
 * The Rxified JdbcExecutor
 * 
 * @author Steven Chen
 *
 */
public class JdbcExecutor {

	private static final Logger logger = LoggerFactory.getLogger(JdbcExecutor.class);

	private static final JsonObject EMPTY_JSON_OBJECT = new JsonObject();

	private JDBCClient jdbcClient;

	/**
	 * Create a instance of JdbcExecutor
	 * 
	 * @param vertx
	 * @param dataSource
	 * @return
	 */
	public static JdbcExecutor create(Vertx vertx, DataSource dataSource) {
		JdbcExecutor inst = new JdbcExecutor(io.vertx.ext.jdbc.JDBCClient.create(vertx, dataSource));
		return inst;
	}

	private JdbcExecutor(io.vertx.ext.jdbc.JDBCClient client) {
		jdbcClient = new JDBCClient(client);
	}

	/**
	 * Execute a one shot SQL query statement
	 * 
	 * @param sql    - the statement to execute
	 * @param params - the statement parameters
	 * @return the list of rows where each row represents as JsonArray
	 */
	public Future<List<JsonArray>> query(String sql, JsonArray params) {
		Future<List<JsonArray>> future = Future.future();
		if (params == null || params.isEmpty())
			jdbcClient.rxQuery(sql).map(ResultSet::getResults).subscribe(SingleHelper.toObserver(future));
		else
			jdbcClient.rxQueryWithParams(sql, params).map(ResultSet::getResults)
					.subscribe(SingleHelper.toObserver(future));
		return future;
	}

	/**
	 * Execute a one shot SQL query statement
	 * 
	 * @param sql    - the statement to execute
	 * @param params - the statement parameters
	 * @return the list of rows where each row represents as JsonArray
	 */
	public Single<List<JsonArray>> querySingle(String sql, JsonArray params) {
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
	public Future<JsonObject> queryFirstRow(String sql, JsonArray params) {
		return queryRows(sql, params).map(list -> {
			if (list == null || list.isEmpty())
				return EMPTY_JSON_OBJECT;
			return list.get(0);
		});
	}

	/**
	 * Execute a one shot SQL query statement with single row result
	 * 
	 * @param sql    - the statement to execute
	 * @param params - the statement parameters
	 * @return the first item of rows that represents as JsonObject, if no data
	 *         which be empty JsonObject
	 */
	public Single<JsonObject> queryFirstRowSingle(String sql, JsonArray params) {
		return queryRowsSingle(sql, params).map(list -> {
			if (list == null || list.isEmpty())
				return EMPTY_JSON_OBJECT;
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
				return EMPTY_JSON_OBJECT;
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
	public Future<List<JsonObject>> queryRows(String sql, JsonArray params) {
		Future<List<JsonObject>> future = Future.future();
		if (params == null || params.isEmpty())
			jdbcClient.rxQuery(sql).map(rs -> rs.getRows(true)).subscribe(SingleHelper.toObserver(future));
		else
			jdbcClient.rxQueryWithParams(sql, params).map(rs -> rs.getRows(true))
					.subscribe(SingleHelper.toObserver(future));
		return future;
	}

	/**
	 * Execute a one shot SQL query statement
	 * 
	 * @param sql    - the statement to execute
	 * @param params - the statement parameters
	 * @return the list of rows where each row represents as JsonObject
	 */
	public Single<List<JsonObject>> queryRowsSingle(String sql, JsonArray params) {
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
	public Future<JsonArray> queryOne(String sql, JsonArray params) {
		Future<JsonArray> future = Future.future();
		if (params == null || params.isEmpty())
			jdbcClient.rxQuerySingle(sql).subscribe(MaybeHelper.toObserver(future));
		else
			jdbcClient.rxQuerySingleWithParams(sql, params).subscribe(MaybeHelper.toObserver(future));
		return future;
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
	public Future<JsonArray> queryOne(SQLConnection conn, String sql, JsonArray params) {
		Future<JsonArray> future = Future.future();
		if (params == null || params.isEmpty())
			conn.rxQuerySingle(sql).subscribe(MaybeHelper.toObserver(future));
		else
			conn.rxQuerySingleWithParams(sql, params).subscribe(MaybeHelper.toObserver(future));
		return future;
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
	 * Execute a one shot SQL update (INSERT, UPDATE, DELETE) statement
	 * 
	 * @param sql    - the statement to execute
	 * @param params - the statement parameters
	 * @return
	 */
	public Future<JsonArray> update(String sql, JsonArray params) {
		return with(conn -> update(conn, sql, params));
	}

	/**
	 * Execute a one shot SQL update (INSERT, UPDATE, DELETE) statement, capture the
	 * generated key from database
	 * 
	 * @param sql         - the statement to execute
	 * @param params      - the statement parameters
	 * @param autoKeyName - the column name of generated key
	 * @return
	 */
	public Future<JsonArray> update(String sql, JsonArray params, String autoKeyName) {
		return with(conn -> update(conn, sql, params, autoKeyName));
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
		return updateSingle(sql, params).ignoreElement();
	}

	/**
	 * Execute a SQL update (INSERT, UPDATE, DELETE) statement, return the
	 * updateResult
	 * 
	 * @param sql    - the statement to execute
	 * @param params - the statement parameters, it allows null
	 * @return
	 */
	public Single<UpdateResult> updateSingle(String sql, JsonArray params) {
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
	 * Execute batch SQL
	 * 
	 * @param sqlList - a group of SQL to execute
	 * @return
	 */
	public Future<List<Integer>> batch(List<String> sqlList) {
		return with(conn -> batch(conn, sqlList));
	}

	/**
	 * Execute batch SQL
	 * 
	 * @param sql         - the SQL to execute
	 * @param batchParams - a group of parameters
	 * @return
	 */
	public Future<List<Integer>> batch(String sql, List<JsonArray> batchParams) {
		return with(conn -> batch(conn, sql, batchParams));
	}

	/**
	 * Calls the given SQL PROCEDURE
	 * 
	 * @param procStatement - standard JDBC format { call func_proc_name() }
	 * @return - a list of JsonArray to represent the row
	 */
	public Future<List<JsonArray>> call(String procStatement) {
		return with(conn -> call(conn, procStatement));
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
	 * @param procStatement - standard JDBC format { call func_proc_name() }
	 * @param in            - the IN parameters
	 * @param out           - the OUT parameters
	 * @returna list of JsonArray to represent the row
	 */
	public Future<List<JsonArray>> call(String procStatement, JsonArray in, JsonArray out) {
		return with(conn -> call(conn, procStatement, in, out));
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
	 * Calls the given SQL PROCEDURE
	 * 
	 * @param procStatement - standard JDBC format { call func_proc_name() }
	 * @return - a list of JsonObject to represent the row
	 */
	public Future<List<JsonObject>> callRows(String procStatement) {
		return with(conn -> callRows(conn, procStatement));
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
	 * @param procStatement - standard JDBC format { call func_proc_name() }
	 * @param in            - the IN parameters
	 * @param out           - the OUT parameters
	 * @returna list of JsonObject to represent the row
	 */
	public Future<List<JsonObject>> callRows(String procStatement, JsonArray in, JsonArray out) {
		return with(conn -> callRows(conn, procStatement, in, out));
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
	 * @param ddlStatement - the DDL statement
	 * @return
	 */
	public Future<Integer> ddl(String ddlStatement) {
		return with(conn -> ddl(conn, ddlStatement));
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
	public <T> Future<T> with(Function<SQLConnection, Single<T>> handler) {
		Future<T> future = Future.future();
		withSingle(handler).subscribe(SingleHelper.toObserver(future));
		return future;
	}

	/**
	 * Execute a group of JdbcExecutor operations within same connection
	 * 
	 * @param handler - the JdbcExecutor operations handler
	 * @return
	 */
	public <T> Single<T> withSingle(Function<SQLConnection, Single<T>> handler) {
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
	public <T> Future<T> tx(Function<SQLConnection, Single<T>> handler) {
		Future<T> future = Future.future();
		txSingle(handler).subscribe(SingleHelper.toObserver(future));
		return future;
	}

	/**
	 * Execute a group of JdbcExecutor operations within same connection and
	 * transaction
	 * 
	 * @param handler - the JdbcExecutor operations handler
	 * @return
	 */
	public <T> Single<T> txSingle(Function<SQLConnection, Single<T>> handler) {
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

	@Deprecated
	<T> Future<T> withInternal(boolean autoCommit, Function<SQLConnection, Single<T>> handler) {
		Future<T> future = Future.future();
		jdbcClient.rxGetConnection().flatMap(sqlConn -> {
			logger.debug("Jdbc connection acquired that autoCommit is: {}", autoCommit);
			return sqlConn.rxSetAutoCommit(autoCommit).andThen(handler.apply(sqlConn)).flatMap(result -> {
				if (!autoCommit) {
					logger.debug("Jdbc prepare to commit");
					return sqlConn.rxCommit().andThen(Single.just(result));
				}
				logger.debug("Jdbc just return single");
				return Single.just(result);
			}).onErrorResumeNext(throwable -> {
				if (!autoCommit) {
					logger.debug("Jdbc prepare to rollback on error resume next");
					return sqlConn.rxRollback().onErrorComplete()
							.andThen(sqlConn.rxSetAutoCommit(true).onErrorComplete()).andThen(Single.error(throwable));
				}
				logger.debug("Jdbc on error resume next");
				return sqlConn.rxSetAutoCommit(true).onErrorComplete().andThen(Single.error(throwable));
			}).flatMap(result -> sqlConn.rxSetAutoCommit(true).andThen(Single.just(result))).doFinally(() -> {
				sqlConn.close();
				logger.debug("Jdbc connection closed");
			});
		}).subscribe(SingleHelper.toObserver(future));
		return future;
	}
}

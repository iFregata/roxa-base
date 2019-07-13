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

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import io.vertx.core.json.JsonObject;

/**
 * @author Steven Chen
 *
 */
public class DataSourceBuilderMySQL extends DataSourceBuilder {

	/**
	 * @param config
	 */
	DataSourceBuilderMySQL(JsonObject config) {
		super(config);
	}

	public HikariDataSource build() {
		HikariConfig cfg = new HikariConfig();
		String jdbcUrl = "jdbc:mysql://" + getHost() + ":" + getPort() + "/" + getDatabase();
		cfg.setDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource");
		cfg.setConnectionTestQuery("select 1");
		cfg.setMaximumPoolSize(getPoolSize());
		cfg.setInitializationFailTimeout(0);
		cfg.addDataSourceProperty("user", getUser());
		cfg.addDataSourceProperty("password", getPass());
		cfg.addDataSourceProperty("url", jdbcUrl);
		cfg.addDataSourceProperty("cachePrepStmts", true);
		cfg.addDataSourceProperty("prepStmtCacheSize", 250);
		cfg.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
		cfg.addDataSourceProperty("useServerPrepStmts", true);
		cfg.addDataSourceProperty("useLocalSessionState", true);
		cfg.addDataSourceProperty("rewriteBatchedStatements", true);
		cfg.addDataSourceProperty("cacheResultSetMetadata", true);
		cfg.addDataSourceProperty("cacheServerConfiguration", true);
		cfg.addDataSourceProperty("elideSetAutoCommits", true);
		cfg.addDataSourceProperty("maintainTimeStats", false);
		cfg.addDataSourceProperty("verifyServerCertificate", false);
		cfg.addDataSourceProperty("useSSL", false);
		setupLeakTraceInterval(cfg);
		return new HikariDataSource(cfg);
	}

}

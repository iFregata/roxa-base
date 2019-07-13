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
public class DataSourceBuilderMSSQL extends DataSourceBuilder {

	/**
	 * @param config
	 */
	DataSourceBuilderMSSQL(JsonObject config) {
		super(config);
	}

	public HikariDataSource build() {
		HikariConfig cfg = new HikariConfig();
		cfg.setDataSourceClassName("net.sourceforge.jtds.jdbcx.JtdsDataSource");
		cfg.setConnectionTestQuery("select 1");
		cfg.setMaximumPoolSize(getPoolSize());
		cfg.setInitializationFailTimeout(0);
		cfg.addDataSourceProperty("user", getUser());
		cfg.addDataSourceProperty("password", getPass());
		cfg.addDataSourceProperty("serverName", getHost());
		cfg.addDataSourceProperty("portNumber", getPort());
		cfg.addDataSourceProperty("databaseName", getDatabase());
		cfg.addDataSourceProperty("serverType", 1);
		cfg.addDataSourceProperty("charset", getCharset());
		setupLeakTraceInterval(cfg);
		return new HikariDataSource(cfg);
	}

}

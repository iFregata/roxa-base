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
public class DataSourceBuilderSybase extends DataSourceBuilder {

	/**
	 * @param config
	 */
	DataSourceBuilderSybase(JsonObject config) {
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
		cfg.addDataSourceProperty("serverType", 2);
		String charset = getCharset();
		cfg.addDataSourceProperty("charset", charset == null ? "gb2312" : charset);
		setupLeakTraceInterval(cfg);
		return new HikariDataSource(cfg);
	}

}

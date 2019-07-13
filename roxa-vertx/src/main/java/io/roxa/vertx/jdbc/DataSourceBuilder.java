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
public abstract class DataSourceBuilder {

	protected JsonObject config;

	static DataSourceBuilder create(JsonObject config) {
		String vendor = config.getString("vendor");
		switch (vendor) {
		case "mysql":
			return new DataSourceBuilderMySQL(config);
		case "mssql":
			return new DataSourceBuilderMSSQL(config);
		case "sybase":
			return new DataSourceBuilderSybase(config);
		default:
			throw new IllegalStateException("Unsupported the data source vendor: " + vendor);
		}
	}

	DataSourceBuilder(JsonObject config) {
		this.config = config;

	}

	abstract public HikariDataSource build();

	protected String getUser() {
		return config.getString("user");
	}

	protected String getPass() {
		return config.getString("pass");
	}

	protected int getPort() {
		return config.getInteger("port");
	}

	protected String getHost() {
		return config.getString("host");
	}

	protected String getDatabase() {
		return config.getString("database");
	}

	protected String getCharset() {
		return config.getString("charset");
	}

	protected int getPoolSize() {
		return config.getInteger("pool_size");
	}

	protected int getLeakTraceInterval() {
		return config.getInteger("leak_trace_interval");
	}

	protected void setupLeakTraceInterval(HikariConfig cfg) {
		int leakTraceInterval = getLeakTraceInterval();
		if (leakTraceInterval > 5000)
			cfg.setLeakDetectionThreshold(leakTraceInterval);
	}
}

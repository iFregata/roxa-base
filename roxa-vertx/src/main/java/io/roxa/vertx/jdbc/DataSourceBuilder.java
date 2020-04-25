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

import io.roxa.util.DESedeCipher;
import io.vertx.core.json.JsonObject;

/**
 * @author Steven Chen
 *
 */
public abstract class DataSourceBuilder {

	private static final String desedeKey = "qdGEYSg9ZGSbWKZaVj7e8w59boVKGmWG";
	private static final String desedeIv = "jaZoxSs2";

	protected JsonObject config;

	public static DataSourceBuilder create(JsonObject config) {
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
		String pass = config.getString("pass");
		boolean encrypted = config.getBoolean("encrypted", false);
		if (encrypted) {
			return decryptPass(pass);
		}
		return pass;
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
		return config.getString("charset", "gb2312");
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

	private static String decryptPass(String encryptedPass) {
		String[] rs = new DESedeCipher(false).cbc().pkcs5().plainIV(desedeIv).key(desedeKey)
				.content(encryptedPass, true).doFinal();
		return rs[0];
	}
}

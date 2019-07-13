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

import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.sql.DataSource;

import io.vertx.core.Vertx;

/**
 * @author Steven Chen
 *
 */
public interface DataSourceLocator {

	void locate(Supplier<String> dsNameSupplier, Consumer<DataSource> dsConsumer);

	static DataSourceLocator create(Vertx vertx, String configLocation) {
		return new DataSourceLocatorImpl(vertx, configLocation);
	}

}

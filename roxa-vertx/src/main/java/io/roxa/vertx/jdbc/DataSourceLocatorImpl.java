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

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zaxxer.hikari.HikariDataSource;

import io.roxa.vertx.cfg.ConfigRegistry;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * @author Steven Chen
 *
 */
public class DataSourceLocatorImpl implements DataSourceLocator {

	private static final Logger logger = LoggerFactory.getLogger(DataSourceLocatorImpl.class);

	private Map<String, HikariDataSource> multiDataSource = new ConcurrentHashMap<>();
	private Map<String, Consumer<DataSource>> dsConsumers = new ConcurrentHashMap<>();
	private ConfigRegistry configRegistry;
//	private Supplier<String> dsNameSupplier;
//	private Consumer<DataSource> dsConsumer;
	private Vertx vertx;

	DataSourceLocatorImpl(Vertx vertx, String configLocation) {
		this.vertx = vertx;
		configRegistry = ConfigRegistry.create(vertx, configLocation);
		vertx.eventBus().consumer(configRegistry.getEventAddress(), this::dataSourceUpdateHandler);
	}

	public void destroy() {
		if (multiDataSource != null)
			multiDataSource.clear();
		if (dsConsumers != null)
			dsConsumers.clear();
	}

	public void locate(Supplier<String> dsNameSupplier, Consumer<DataSource> dsConsumer) {
		// this.dsNameSupplier = dsNameSupplier;
		// this.dsConsumer = dsConsumer;
		register(dsNameSupplier.get(), dsConsumer);
	}

	public void register(String dsName, Consumer<DataSource> dsConsumer) {
		dsConsumers.put(dsName, dsConsumer);
	}

	private void dataSourceUpdateHandler(Message<JsonObject> msg) {
		JsonObject body = msg.body();
		Future<Void> future = Future.future();
		vertx.executeBlocking(execFuture -> {
			try {
				releaseHikariDataSources();
				JsonArray configArray = body.getJsonArray("data_sources");
				dsConsumers.entrySet().stream().forEach(e -> {
					String dsName = e.getKey();
					configArray.stream().map(item -> (JsonObject) item)
							.filter(cfg -> dsName.equals(cfg.getString("data_source_name"))).findFirst()
							.ifPresent(config -> {
								DataSourceBuilder builder = DataSourceBuilder.create(config);
								HikariDataSource hikariDataSource = builder.build();
								multiDataSource.put(config.getString("data_source_name"), hikariDataSource);
								logger.info("Register the data source service with config: {}", config.encode());
							});
				});
				execFuture.complete();
			} catch (Throwable e) {
				execFuture.fail(e);
			}

		}, future);
		future.setHandler(ar -> {
			if (ar.succeeded()) {
				dsConsumers.entrySet().stream().forEach(e -> {
					String dsName = e.getKey();
					Consumer<DataSource> c = e.getValue();
					c.accept(multiDataSource.get(dsName));
				});
			} else {
				logger.error("Data source locator failed", ar.cause());
			}
		});
	}

	private void releaseHikariDataSources() {
		if (multiDataSource == null || multiDataSource.isEmpty())
			return;
		multiDataSource.values().stream().map(sr -> Optional.ofNullable(sr)).forEach(opt -> {
			opt.ifPresent(target -> target.close());
		});
		multiDataSource.clear();
		logger.info("Released data source pool");
	}
}

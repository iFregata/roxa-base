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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zaxxer.hikari.HikariDataSource;

import io.reactivex.Completable;
import io.roxa.vertx.jdbc.DataSourceBuilder;
import io.roxa.vertx.rx.BaseVerticle;
import io.vertx.config.ConfigChange;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * @author Steven Chen
 *
 */
public class JdbcManager extends BaseVerticle {

	private static final Logger logger = LoggerFactory.getLogger(JdbcManager.class);

	private Map<String, HikariDataSource> multiDataSource = new ConcurrentHashMap<>();

	private Map<String, JdbcExecutor> jdbcExecutors = new ConcurrentHashMap<>();

	private Map<String, List<Consumer<JdbcExecutor>>> consumers = new ConcurrentHashMap<>();

	private JsonObject dataSourceConfs;

	static class JdbcManagerInitilizer {
		static final JdbcManager instance;
		static {
			instance = new JdbcManager();
		}
	}

	public static JdbcManager instance() {
		return JdbcManagerInitilizer.instance;
	}

	public static void register(String dsName, Consumer<JdbcExecutor> consumer) {
		instance().registerInternal(dsName, consumer);
	}

	private JdbcManager() {
	}

	@Override
	public void start(Promise<Void> startPromise) throws Exception {
		configuration("conf/data_sources.json").subscribe(config -> {
			dataSourceConfs = new JsonObject();
			JsonArray configArray = config.getJsonArray("data_sources");
			configArray.stream().map(item -> (JsonObject) item).forEach(i -> {
				dataSourceConfs.put(i.getString("data_source_name"), i);
			});
			startPromise.complete();
		}, e -> {
			startPromise.fail(e);
		});
	}

	@Override
	public void stop() throws Exception {
		releaseDataSources();
		if (jdbcExecutors != null)
			jdbcExecutors.clear();
		if (consumers != null)
			consumers.clear();
	}

	protected void configurationChanged(ConfigChange change) {
		JsonObject cfgNew = change.getNewConfiguration();
		JsonObject cfgOld = change.getPreviousConfiguration();
		logger.debug("Configuration changed, the new one is {}, prev is {}", cfgNew.encodePrettily(),
				cfgOld.encodePrettily());
		if (!cfgNew.equals(cfgOld)) {
			logger.debug("Configuration change detected, the new one is {}", cfgNew.encodePrettily());
			updateDataSources(cfgNew).subscribe(() -> logger.info("Apply new configuration to deploy succeeded"),
					e -> logger.error("Apply new configuration to deploy failed", e));
		}
	}

	private void registerInternal(String dsName, Consumer<JdbcExecutor> consumer) {
		JdbcExecutor item = jdbcExecutors.get(dsName);
		if (item != null) {
			consumer.accept(item);
			addConsumer(dsName, consumer);
			return;
		}
		vertx.rxExecuteBlocking(promise -> {
			try {
				multiDataSource.computeIfAbsent(dsName,
						k -> DataSourceBuilder.create(dataSourceConfs.getJsonObject(dsName)).build());
				jdbcExecutors.computeIfAbsent(dsName, k -> JdbcExecutor.create(vertx, multiDataSource.get(dsName)));
				consumer.accept(jdbcExecutors.get(dsName));
				promise.complete();
			} catch (Throwable e) {
				promise.fail(e);
			}
		}).ignoreElement().subscribe(() -> {
			logger.info("Register JdbcManager consumer succeeded");
		}, e -> {
			logger.error("Register JdbcManager consumer error", e);
		});
		addConsumer(dsName, consumer);
	}

	/**
	 * @param dsName
	 * @param consumer
	 */
	private void addConsumer(String dsName, Consumer<JdbcExecutor> consumer) {
		List<Consumer<JdbcExecutor>> list = consumers.get(dsName);
		if (list == null) {
			list = new ArrayList<>();
			consumers.put(dsName, list);
		}
		list.add(consumer);
	}

	private Completable updateDataSources(JsonObject conf) {
		return vertx.rxExecuteBlocking(execPromise -> {
			try {
				dataSourceConfs.clear();
				releaseDataSources();
				JsonArray configArray = conf.getJsonArray("data_sources");
				configArray.stream().map(item -> (JsonObject) item).forEach(config -> {
					DataSourceBuilder builder = DataSourceBuilder.create(config);
					HikariDataSource hikariDataSource = builder.build();
					String dsName = config.getString("data_source_name");
					JdbcExecutor jdbcExecutor = JdbcExecutor.create(vertx, hikariDataSource);
					dataSourceConfs.put(dsName, config);
					multiDataSource.put(dsName, hikariDataSource);
					jdbcExecutors.put(dsName, jdbcExecutor);
					consumers.get(dsName).stream().forEach(c -> c.accept(jdbcExecutor));
					logger.info("Register the JdbcManager service with config: {}", config.encode());
				});
				execPromise.complete();
			} catch (Throwable e) {
				execPromise.fail(e);
			}

		}).ignoreElement();
	}

	private void releaseDataSources() {
		if (multiDataSource == null || multiDataSource.isEmpty())
			return;
		multiDataSource.values().stream().map(sr -> Optional.ofNullable(sr)).forEach(opt -> {
			opt.ifPresent(target -> target.close());
		});
		multiDataSource.clear();
		logger.info("Released all data source");
	}
}

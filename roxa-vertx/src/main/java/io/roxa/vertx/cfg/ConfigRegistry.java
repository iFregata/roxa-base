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
package io.roxa.vertx.cfg;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.roxa.vertx.cache.LocalCache;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;

/**
 * @author Steven Chen
 *
 */
public class ConfigRegistry {

	private static final Logger logger = LoggerFactory.getLogger(ConfigRegistry.class);
	private static final JsonObject EMPTY_JSON_OBJECT = new JsonObject();
	private EventBus eventBus;
	private ConfigRetriever configRetriever;
	private String registryLocation;
	private String eventAddress;
	private String cacheAddress;
	private LocalCache localCache;
	private MessageConsumer<JsonObject> consumer;
	private boolean cached;

	public static ConfigRegistry create(Vertx vertx, String registryLocation, boolean cached) {
		ConfigRegistry inst = new ConfigRegistry();
		inst.init(vertx, registryLocation, cached);
		return inst;
	}

	public static ConfigRegistry create(Vertx vertx, String registryLocation) {
		ConfigRegistry inst = new ConfigRegistry();
		inst.init(vertx, registryLocation, false);
		return inst;
	}

	public Future<JsonObject> getCached() {
		return localCache.get(cacheAddress).recover(e -> Future.succeededFuture(EMPTY_JSON_OBJECT));
	}

	public String getRegistryLocation() {
		return this.registryLocation;
	}

	public String getEventAddress() {
		return this.eventAddress;
	}

	public String getCacheAddress() {
		return this.cacheAddress;
	}

	public void destroy() {
		if (consumer != null)
			consumer.unregister();
		if (configRetriever != null)
			configRetriever.close();
		if (cached && localCache != null)
			localCache.remove(cacheAddress);
		logger.info("Config registry destroy for location {}", registryLocation);
	}

	private ConfigRegistry() {

	}

	private void init(Vertx vertx, String registryLocationParam, boolean cached) {
		localCache = LocalCache.create(vertx);
		this.cached = cached;
		eventBus = vertx.eventBus();
		registryLocation = registryLocationParam;
		eventAddress = String.format("roxa.vertx::cfg.events[%s]", registryLocation);
		cacheAddress = String.format("roxa.vertx::cfg.cache[%s]", registryLocation);
		consumer = eventBus.consumer(eventAddress);
		consumer.handler(this::registryChangedHandler);
		ConfigStoreOptions fileStore = new ConfigStoreOptions().setType("file").setOptional(true)
				.setConfig(new JsonObject().put("path", registryLocation));
		ConfigRetrieverOptions options = new ConfigRetrieverOptions().addStore(fileStore);
		configRetriever = ConfigRetriever.create(vertx, options);
		configRetriever.getConfig(ar -> {
			if (ar.succeeded()) {
				JsonObject config = ar.result();
				if (config != null && !config.isEmpty()) {
					logger.info("Loading registry configuration from location: {}, cache status: {}", registryLocation,
							cached);
					eventBus.publish(eventAddress, config.copy());
				} else {
					logger.warn("No initial registry config found!");
				}
			} else {
				logger.error("Load the registry config failed", ar.cause());
			}

		});
		configRetriever.listen(change -> {
			JsonObject config = change.getNewConfiguration();
			if (!config.isEmpty()) {
				logger.info("Reloading registry config from location: {}, cache status: {}", registryLocation, cached);
				eventBus.publish(eventAddress, config.copy());
			} else {
				logger.warn("No registry config found!");
			}
		});

	}

	private void registryChangedHandler(Message<JsonObject> msg) {
		JsonObject body = msg.body();
		if (cached)
			localCache.put(cacheAddress, body);
	}
}

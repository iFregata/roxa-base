/**
 * The MIT License
 * 
 * Copyright (c) 2016-2018 Shell Technologies PTY LTD
 *
 * You may obtain a copy of the License at
 * 
 *       http://mit-license.org/
 *       
 */
package io.roxa.vertx;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.config.ConfigChange;
import io.vertx.config.ConfigRetriever;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Verticle;
import io.vertx.core.json.JsonObject;

/**
 * @author Steven Chen
 *
 */
public abstract class AbstractBootVerticle extends AbstractVerticle {

	private static final Logger logger = LoggerFactory.getLogger(AbstractBootVerticle.class);

	private final Map<String, String> deploymentIds = new ConcurrentHashMap<>();

	/**
	 * 
	 */
	public AbstractBootVerticle() {
	}

	public void stop(Promise<Void> stopPromise) throws Exception {
		/preStop().compose(v -> undeployAll()).setHandler(stopPromise.future());
	}

	protected Future<Void> preStop() {
		return Future.succeededFuture();
	}

	protected Future<JsonObject> configuration() {
		Promise<JsonObject> promise = Promise.promise();
		ConfigRetriever cfgr = ConfigRetriever.create(vertx);
		cfgr.getConfig(ar -> {
			if (ar.succeeded()) {
				JsonObject cfgJson = ar.result();
				logger.debug("ConfigRetriever conf/config.json: {}", cfgJson.encodePrettily());
				promise.complete(cfgJson);
			} else
				promise.fail(ar.cause());

		});
		cfgr.listen(this::configurationChanged);
		return promise.future();
	}

	protected void configurationChanged(ConfigChange change) {
		JsonObject cfgNew = change.getNewConfiguration();
		JsonObject cfgOld = change.getPreviousConfiguration();
		logger.debug("Configuration changed, the new one is {}, prev is {}", cfgNew.encodePrettily(),
				cfgOld.encodePrettily());
	}

	protected Future<String> deploy(Supplier<Verticle> supplier, Class<? extends Verticle> verticleClass,
			DeploymentOptions deploymentOptions) {
		if (supplier == null || verticleClass == null)
			return Future.failedFuture("Verticle supplier is null or not found!");
		Promise<String> promise = Promise.promise();
		String name = verticleClass.getName();
		vertx.deployVerticle(supplier, deploymentOptions, ar -> {
			if (ar.succeeded()) {
				String id = ar.result();
				logger.info("Deployed verticle with supplier, name: {}, Id: {}, deployment options: {}", name, id,
						deploymentOptions.toJson().encode());
				deploymentIds.put(id, name);
				promise.complete(id);
			} else {
				logger.error("Cannot deploy verticle with supplier: " + name, ar.cause());
				promise.fail(ar.cause());
			}
		});
		return promise.future();
	}

	protected Future<String> deploy(Verticle verticle, DeploymentOptions deploymentOptions) {
		if (verticle == null)
			return Future.failedFuture("The verticle instance must not be null!");
		Promise<String> promise = Promise.promise();
		String name = verticle.getClass().getName();
		vertx.deployVerticle(verticle, deploymentOptions, ar -> {
			if (ar.succeeded()) {
				String id = ar.result();
				logger.info("Deployed verticle instance: {}, Id: {}", name, id);
				deploymentIds.put(id, name);
				promise.complete(id);
			} else {
				logger.error("Cannot deploy verticle instance: " + name, ar.cause());
				promise.fail(ar.cause());
			}
		});
		return promise.future();
	}

	protected Future<String> redeploy(Verticle verticle) {
		String name = verticle.getClass().getName();
		@SuppressWarnings("rawtypes")
		List<Future> list = new ArrayList<Future>();
		deploymentIds.entrySet().stream().forEach(e -> {
			if (name.equals(e.getValue())) {
				list.add(undeploy(e.getKey()));
			}
		});
		return CompositeFuture.all(list).compose(e -> deploy(verticle));
	}

	protected Future<String> deploy(Verticle verticle) {
		if (verticle == null)
			return Future.failedFuture("The verticle instance must not be null!");
		Promise<String> promise = Promise.promise();
		String name = verticle.getClass().getName();
		vertx.deployVerticle(verticle, ar -> {
			if (ar.succeeded()) {
				String id = ar.result();
				logger.info("Deployed verticle instance: {}, Id: {}", name, id);
				deploymentIds.put(id, name);
				promise.complete(id);
			} else {
				logger.error("Cannot deploy verticle instance: " + name, ar.cause());
				promise.fail(ar.cause());
			}
		});
		return promise.future();
	}

	protected Future<Void> undeploy(String verticleId) {
		if (verticleId == null)
			return Future.succeededFuture();
		Promise<Void> promise = Promise.promise();
		String name = deploymentIds.remove(verticleId);
		if (name == null)
			return Future.succeededFuture();

		vertx.undeploy(verticleId, ar -> {
			if (ar.succeeded()) {
				logger.info("Undeploy verticle succeeded with id: {}, name: {}", verticleId, name);
				promise.complete();
			} else {
				logger.error("Undeploy verticle failed with id: {}, name: {}", verticleId, name);
				promise.fail(ar.cause());
			}
		});
		return promise.future();
	}

	private Future<Void> undeployAll() {
		Promise<Void> promise = Promise.promise();
		if (deploymentIds == null || deploymentIds.isEmpty()) {
			promise.complete();
		} else {
			CompositeFuture.all(deploymentIds.keySet().stream().filter(id -> vertx.deploymentIDs().contains(id))
					.map(this::undeploy).collect(Collectors.toList())).setHandler(ar -> {
						if (ar.succeeded()) {
							promise.complete();
						} else {
							promise.fail(ar.cause());
						}
					});
		}
		return promise.future();
	}

}

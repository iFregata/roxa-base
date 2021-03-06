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
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.roxa.util.Strings;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.impl.ConcurrentHashSet;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.types.HttpEndpoint;

/**
 * @author Steven Chen
 *
 */
public abstract class BaseVerticle extends AbstractVerticle {

	private static final Logger logger = LoggerFactory.getLogger(BaseVerticle.class);

	protected ServiceDiscovery discovery;
	protected Set<Record> registeredRecords = new ConcurrentHashSet<>();

	public void stop(Promise<Void> stopPromise) throws Exception {
		tearDownServiceDiscovery().compose(v -> closeResources()).onComplete(stopPromise.future());
	}

	protected Future<Void> closeResources() {
		return Future.succeededFuture();
	}

	protected Future<ServiceDiscovery> setupServiceDiscovery() {
		if (discovery == null)
			discovery = ServiceDiscovery.create(vertx);
		return Future.succeededFuture(discovery);
	}

	protected Future<ServiceDiscovery> setupServiceDiscovery(Void v) {
		if (discovery == null)
			discovery = ServiceDiscovery.create(vertx);
		return Future.succeededFuture(discovery);
	}

	@SuppressWarnings("rawtypes")
	protected Future<Void> tearDownServiceDiscovery() {
		logger.info("Prepare to tear down the service discovery");
		if (discovery == null)
			return Future.succeededFuture();
		Promise<Void> promise = Promise.promise();
		List<Future> futures = new ArrayList<>();
		registeredRecords.forEach(record -> {
			Promise<Void> cleanupPromise = Promise.promise();
			Future<Void> f = cleanupPromise.future();
			futures.add(f);
			String reg = record.getRegistration();
			logger.info("Unpublish service discovery registration: {}", reg);
			discovery.unpublish(reg, f);
		});
		if (futures.isEmpty()) {
			closeServiceDiscovery();
			promise.complete();
		} else {
			CompositeFuture.all(futures).onComplete(ar -> {
				closeServiceDiscovery();
				if (ar.failed()) {
					promise.fail(ar.cause());
				} else {
					promise.complete();
				}
			});
		}
		return promise.future();
	}

	protected Future<Void> publishHttpEndpoint(String name, String apiName, String host, int port, String contextPath) {
		Record record = HttpEndpoint.createRecord(name, host, port, contextPath,
				new JsonObject().put("api.name", apiName));
		logger.info("Preparing to pushlish HTTP Endpoint service {} on {}:{}, context path:{}, api name:{}", name, host,
				port, contextPath, apiName);
		return publish(record);
	}

	protected Future<Void> publishHttpEndpoint(String name, String apiName, String host, int port) {
		return publishHttpEndpoint(name, apiName, host, port, "/");
	}

	protected Future<Void> publishHttpEndpoint(String name, String apiName, int port) {
		return publishHttpEndpoint(name, apiName, port, "/");
	}

	protected Future<Void> publishHttpEndpoint(String name, String apiName, int port, String contextPath) {
		return publishHttpEndpoint(name, apiName, inferServiceHost(), port, contextPath);
	}

	private static String inferServiceHost() {
		Map<String, String> envMap = System.getenv();
		String host = Strings.emptyAsNull(envMap.get("HTTP_ENDPOINT_HOST"));
		if (host == null)
			host = Strings.emptyAsNull(envMap.get("HOSTNAME"));
		if (host == null)
			host = "localhost";
		return host;
	}

	protected Future<Void> publish(Record record) {
		Promise<Void> promise = Promise.promise();
		discovery.publish(record, ar -> {
			if (ar.succeeded()) {
				registeredRecords.add(record);
				logger.info("ServiceDiscovery publish,  registration: {}, name: {}", record.getRegistration(),
						ar.result().getName());
				promise.complete();
			} else {
				promise.fail(ar.cause());
			}
		});

		return promise.future();
	}

	private void closeServiceDiscovery() {
		if (discovery != null) {
			discovery.close();
			logger.info("ServiceDiscovery closed.");
		}
		if (registeredRecords != null) {
			registeredRecords.clear();
		}
	}
}

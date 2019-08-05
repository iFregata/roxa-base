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
package io.roxa.vertx.rx;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.roxa.util.Strings;
import io.vertx.core.Promise;
import io.vertx.core.impl.ConcurrentHashSet;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.CompletableHelper;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.http.HttpServer;
import io.vertx.reactivex.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.types.HttpEndpoint;

/**
 * @author Steven Chen
 *
 */
public abstract class BaseVerticle extends AbstractVerticle {

	private static final Logger logger = LoggerFactory.getLogger(BaseVerticle.class);

	protected ServiceDiscovery discovery;
	protected Set<Record> registeredRecords = new ConcurrentHashSet<>();

	@Override
	public void stop(Promise<Void> stopPromise) throws Exception {
		tearDownServiceDiscovery().andThen(closeResources())
				.subscribe(CompletableHelper.toObserver(stopPromise.future()));
	}

	protected Completable closeResources() {
		return Completable.complete();
	}

	protected Single<ServiceDiscovery> setupServiceDiscovery() {
		if (discovery == null)
			discovery = ServiceDiscovery.create(vertx);
		return Single.just(discovery);
	}

	protected Single<ServiceDiscovery> setupServiceDiscovery(HttpServer httpServer) {
		if (discovery == null)
			discovery = ServiceDiscovery.create(vertx);
		return Single.just(discovery);
	}

	protected Completable tearDownServiceDiscovery() {
		logger.info("Prepare to tear down the service discovery");
		if (discovery == null)
			return Completable.complete();
		List<Completable> completables = new ArrayList<>();
		registeredRecords.forEach(record -> {
			String reg = record.getRegistration();
			logger.info("Unpublish service discovery registration: {}", reg);
			completables.add(discovery.rxUnpublish(reg));
		});
		if (completables.isEmpty()) {
			closeServiceDiscovery();
			return Completable.complete();
		}
		return Completable.mergeDelayError(completables).doFinally(() -> {
			closeServiceDiscovery();
		});
	}

	protected Completable publishHttpEndpoint(String name, String apiName, String host, int port, String contextPath) {
		Record record = HttpEndpoint.createRecord(name, host, port, contextPath,
				new JsonObject().put("api.name", apiName));
		logger.info("Preparing to pushlish HTTP Endpoint service {} on {}:{}, context path:{}, api name:{}", name, host,
				port, contextPath, apiName);
		return publish(record);
	}

	protected Completable publishHttpEndpoint(String name, String apiName, String host, int port) {
		return publishHttpEndpoint(name, apiName, host, port, "/");
	}

	protected Completable publishHttpEndpoint(String name, String apiName, int port) {
		return publishHttpEndpoint(name, apiName, port, "/");
	}

	protected Completable publishHttpEndpoint(String name, String apiName, int port, String contextPath) {
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

	protected Completable publish(Record record) {
		return discovery.rxPublish(record).doOnSuccess(r -> {
			registeredRecords.add(record);
			logger.info("ServiceDiscovery publish,  registration: {}, name: {}", record.getRegistration(), r.getName());
		}).ignoreElement();
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

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
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;
import io.roxa.GeneralFailureException;
import io.vertx.config.ConfigChange;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.CompletableHelper;
import io.vertx.reactivex.config.ConfigRetriever;
import io.vertx.reactivex.core.AbstractVerticle;

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

	public void stop(Future<Void> stopFuture) throws Exception {
		preStop().andThen(undeployAll()).subscribe(CompletableHelper.toObserver(stopFuture));

	}

	protected Completable preStop() {
		return Completable.complete();
	}

	protected Single<JsonObject> configuration() {
		ConfigRetriever cfgr = ConfigRetriever.create(vertx);
		Single<JsonObject> single = cfgr.rxGetConfig();
		cfgr.listen(this::configurationChanged);
		return single.doOnSuccess(cfg -> logger.debug("Configuration info: {}", cfg.encodePrettily()));
	}

	protected void configurationChanged(ConfigChange change) {
		JsonObject cfgNew = change.getNewConfiguration();
		JsonObject cfgOld = change.getPreviousConfiguration();
		logger.debug("Configuration changed, the new one is {}, prev is {}", cfgNew.encodePrettily(),
				cfgOld.encodePrettily());
	}

	protected Completable redeploy(String verticleId, Verticle verticle, Consumer<? super String> doOnSuccess) {
		return redeploy(verticleId, verticle, null, doOnSuccess);
	}

	protected Completable redeploy(String verticleId, String className, Consumer<? super String> doOnSuccess) {
		return redeploy(verticleId, className, null, doOnSuccess);
	}

	protected Completable redeploy(String verticleId, Verticle verticle, DeploymentOptions deploymentOptions,
			Consumer<? super String> doOnSuccess) {
		final Supplier<Single<String>> supplier = () -> {
			return deploymentOptions != null ? deploy(verticle, deploymentOptions) : deploy(verticle);
		};
		return undeploy(verticleId).andThen(supplier.get()).doOnSuccess(doOnSuccess).ignoreElement();
	}

	protected Completable redeploy(String verticleId, String className, DeploymentOptions deploymentOptions,
			Consumer<? super String> doOnSuccess) {
		final Supplier<Single<String>> supplier = () -> {
			return deploymentOptions != null ? deploy(className, deploymentOptions) : deploy(className);
		};
		return undeploy(verticleId).andThen(supplier.get()).doOnSuccess(doOnSuccess).ignoreElement();
	}

	protected Completable redeploy(String className, DeploymentOptions deploymentOptions) {
		List<Completable> list = new ArrayList<>();
		deploymentIds.entrySet().stream().forEach(e -> {
			if (className.equals(e.getValue())) {
				list.add(undeploy(e.getKey()));
			}
		});
		return Completable.merge(list).andThen(deploy(className, deploymentOptions)).ignoreElement();
	}

	protected Completable redeploy(Verticle verticle) {
		List<Completable> list = new ArrayList<>();
		String name = verticle.getClass().getName();
		deploymentIds.entrySet().stream().forEach(e -> {
			if (name.equals(e.getValue())) {
				list.add(undeploy(e.getKey()));
			}
		});
		return Completable.merge(list).andThen(deploy(verticle)).ignoreElement();
	}

	protected Single<String> deploy(Supplier<Verticle> supplier, Class<? extends Verticle> verticleClass,
			DeploymentOptions deploymentOptions) {
		if (supplier == null || verticleClass == null)
			return Single.error(new GeneralFailureException("Verticle supplier is null or not found!"));
		String name = verticleClass.getName();
		return vertx.rxDeployVerticle(supplier, deploymentOptions).doOnSuccess(id -> {
			logger.info("Deployed verticle with supplier, name: {}, Id: {}, deployment options: {}", name, id,
					deploymentOptions.toJson().encode());
			deploymentIds.put(id, name);
		}).doOnError(e -> logger.error("Cannot deploy verticle with supplier: " + name, e));
	}

	protected Single<String> deploy(Verticle verticle, DeploymentOptions deploymentOptions) {
		if (verticle == null)
			return Single.error(new GeneralFailureException("The verticle instance must not be null!"));
		String name = verticle.getClass().getName();
		return vertx.rxDeployVerticle(verticle, deploymentOptions).doOnSuccess(id -> {
			logger.info("Deployed verticle instance: {}, Id: {}", name, id);
			deploymentIds.put(id, name);
		}).doOnError(e -> logger.error("Cannot deploy verticle instance: " + name, e));

	}

	protected Single<String> deploy(String className, DeploymentOptions deploymentOptions) {
		if (className == null)
			return Single.error(new GeneralFailureException("The verticle instance must not be null!"));
		return vertx.rxDeployVerticle(className, deploymentOptions).doOnSuccess(id -> {
			logger.info("Deployed verticle instance: {}, Id: {}", className, id);
			deploymentIds.put(id, className);
		}).doOnError(e -> logger.error("Cannot deploy verticle instance: " + className, e));

	}

	protected Single<String> deploy(String className) {
		if (className == null)
			return Single.error(new GeneralFailureException("The verticle instance must not be null!"));
		return vertx.rxDeployVerticle(className).doOnSuccess(id -> {
			logger.info("Deployed verticle instance: {}, Id: {}", className, id);
			deploymentIds.put(id, className);
		}).doOnError(e -> logger.error("Cannot deploy verticle instance: " + className, e));

	}

	protected Single<String> deploy(Verticle verticle) {
		if (verticle == null)
			return Single.error(new GeneralFailureException("The verticle instance must not be null!"));
		String name = verticle.getClass().getName();
		return vertx.rxDeployVerticle(verticle).doOnSuccess(id -> {
			logger.info("Deployed verticle instance: {}, Id: {}", name, id);
			deploymentIds.put(id, name);
		}).doOnError(e -> logger.error("Cannot deploy verticle instance: " + name, e));

	}

	protected Completable undeploy(String verticleId) {
		if (verticleId == null)
			return Completable.complete();
		String name = deploymentIds.remove(verticleId);
		return vertx.rxUndeploy(verticleId)
				.doOnComplete(() -> logger.info("Undeploy verticle succeeded with id: {}, name: {}", verticleId, name))
				.doOnError(e -> logger.warn("Undeploy verticle failed with id: {}, name: {}", verticleId, name))
				.onErrorResumeNext(e -> Completable.complete());

	}

	private Completable undeployAll() {
		if (deploymentIds == null || deploymentIds.isEmpty())
			return Completable.complete();
		List<Completable> list = deploymentIds.keySet().stream().filter(id -> vertx.deploymentIDs().contains(id))
				.map(this::undeploy).collect(Collectors.toList());
		return Completable.concat(list);

	}

}

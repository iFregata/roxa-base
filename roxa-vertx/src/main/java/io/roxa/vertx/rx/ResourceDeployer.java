/**
 * The MIT License
 * 
 * Copyright (c) 2019-2022 Shell Technologies PTY LTD
 *
 * You may obtain a copy of the License at
 * 
 *       http://mit-license.org/
 *       
 */
package io.roxa.vertx.rx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.roxa.GeneralFailureException;
import io.vertx.config.ConfigChange;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.Promise;
import io.vertx.core.Verticle;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.config.ConfigRetriever;
import io.vertx.reactivex.core.AbstractVerticle;

/**
 * @author Steven Chen
 *
 */
public abstract class ResourceDeployer extends AbstractVerticle {

	private static final Logger logger = LoggerFactory.getLogger(ResourceDeployer.class);

	protected final String resourceName;

	protected final String resourceCatalog;

	protected String deploymentId;

	protected boolean awaredConfigStore;

	public ResourceDeployer(String resourceCatalog, String resourceName) {
		this(resourceCatalog, resourceName, true);
	}

	public ResourceDeployer(String resourceCatalog, String resourceName, boolean awaredConfigStore) {
		this.resourceCatalog = resourceCatalog;
		this.resourceName = resourceName;
		this.awaredConfigStore = awaredConfigStore;
	}

	@Override
	public void start(Promise<Void> startPromise) throws Exception {
		if (awaredConfigStore) {
			ConfigStoreOptions fileStore = new ConfigStoreOptions().setType("file").setOptional(true).setConfig(
					new JsonObject().put("path", String.format("conf/%s_%s.json", resourceCatalog, resourceName)));
			ConfigRetrieverOptions options = new ConfigRetrieverOptions().addStore(fileStore);
			ConfigRetriever cfgr = ConfigRetriever.create(vertx, options);
			cfgr.listen(this::configurationChanged);
			cfgr.rxGetConfig().flatMapCompletable(this::configure).subscribe(() -> {
				startPromise.complete();
			}, e -> {
				startPromise.fail(e);
			});
		} else {
			configure(JsonAsync.EMPTY_JSON).subscribe(() -> {
				startPromise.complete();
			}, e -> {
				startPromise.fail(e);
			});
		}
	}

	abstract protected Single<Verticle> getResourceAgent(JsonObject cfg);

	private void configurationChanged(ConfigChange change) {
		JsonObject cfgNew = change.getNewConfiguration();
		JsonObject cfgOld = change.getPreviousConfiguration();
		logger.debug("Resource configuration changed, the new one is {}, prev is {}", cfgNew.encodePrettily(),
				cfgOld.encodePrettily());
		if (!cfgNew.equals(cfgOld)) {
			logger.debug("Resource configuration change detected, the new one is {}", cfgNew.encodePrettily());
			configure(cfgNew).subscribe(() -> logger.info("Apply new Resource configuration to deploy succeeded"),
					e -> logger.error("Apply new Resource configuration to deploy failed", e));
		}
	}

	private Completable configure(JsonObject cfg) {
		return undeploy(deploymentId).andThen(getResourceAgent(cfg).flatMapCompletable(this::deploy));
	}

	private Completable deploy(Verticle verticle) {
		if (verticle == null)
			return Completable.error(new GeneralFailureException("The ResourceAgent instance must not be null!"));
		return vertx.rxDeployVerticle(verticle).doOnSuccess(id -> {
			logger.info("Deployed ResourceAgent instance: {}, Id: {}", resourceName, id);
			deploymentId = id;
		}).doOnError(e -> logger.error("Cannot deploy ResourceAgent instance: " + resourceName, e)).ignoreElement();

	}

	private Completable undeploy(String verticleId) {
		if (verticleId == null)
			return Completable.complete();
		return vertx.rxUndeploy(verticleId)
				.doOnComplete(() -> logger.info("Undeploy ResourceAgent succeeded with id: {}, resourceName: {}",
						verticleId, resourceName))
				.doOnError(e -> logger.warn("Undeploy ResourceAgent failed with id: {}, resourceName: {}", verticleId,
						resourceName))
				.onErrorResumeNext(e -> Completable.complete());

	}
}

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
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;
import io.roxa.GeneralFailureException;
import io.roxa.vertx.rx.cron.CronSchedulerVerticle;
import io.roxa.vertx.rx.jdbc.JdbcDeployer;
import io.roxa.vertx.rx.jdbc.JdbcManager;
import io.vertx.config.ConfigChange;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;
import io.vertx.core.Verticle;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.CompletableHelper;

/**
 * @author Steven Chen
 *
 */
public abstract class AbstractBootVerticle extends BaseVerticle {

	private static final Logger logger = LoggerFactory.getLogger(AbstractBootVerticle.class);

	private final Map<String, String> deploymentIds = new ConcurrentHashMap<>();
	private final Map<String, JdbcDeployer> jdbcDeployers = new ConcurrentHashMap<>();

	/**
	 * 
	 */
	public AbstractBootVerticle() {
	}

	@Override
	public void start(Promise<Void> startPromise) throws Exception {
		start();
		configuration().flatMapCompletable(this::configure)
				.subscribe(CompletableHelper.toObserver(startPromise.future()));
	}

	@Override
	public void stop(Promise<Void> stopPromise) throws Exception {
		jdbcDeployers.clear();
		stop();
		preStop().andThen(undeployAll()).subscribe(CompletableHelper.toObserver(stopPromise.future()));
	}

	public JdbcDeployer getJdbcDeployer(String jdbcResourceName) {
		return jdbcDeployers.get(jdbcResourceName);
	}

	@Deprecated
	protected void setupJdbcManager() {
		deploy(JdbcManager.instance()).subscribe(id -> {
			logger.info("Deployed JdbcManager with id: {}", id);
		}, e -> {
			logger.info("Deployed JdbcManager error", e);
		});
	}

	protected void setupJdbcDeployer(String jdbcResourceName) {
		JdbcDeployer jdbcDeployer = new JdbcDeployer(jdbcResourceName);
		jdbcDeployers.put(jdbcResourceName, jdbcDeployer);
		deploy(jdbcDeployer).subscribe(id -> {
			logger.info("Deployed JdbcDeployer with id: {}", id);
		}, e -> {
			logger.info("Deployed JdbcDeployer error", e);
		});
	}

	protected void setupCronScheduler() {
		deploy(CronSchedulerVerticle.instance()).subscribe(id -> {
			logger.info("Deployed CronScheduler with id: {}", id);
		}, e -> {
			logger.info("Deployed CronScheduler error", e);
		});
	}

	protected Completable configure(JsonObject conf) {
		return Completable.complete();
	}

	protected Completable preStop() {
		return Completable.complete();
	}

	protected void configurationChanged(ConfigChange change) {
		JsonObject cfgNew = change.getNewConfiguration();
		JsonObject cfgOld = change.getPreviousConfiguration();
		logger.debug("Configuration changed, the new one is {}, prev is {}", cfgNew.encodePrettily(),
				cfgOld.encodePrettily());
		if (!cfgNew.equals(cfgOld)) {
			logger.debug("Configuration change detected, the new one is {}", cfgNew.encodePrettily());
			configure(cfgNew).subscribe(() -> logger.info("Apply new configuration to deploy succeeded"),
					e -> logger.error("Apply new configuration to deploy failed", e));
		}
	}

	@Deprecated
	protected Completable redeploy(String verticleId, Verticle verticle, Consumer<? super String> doOnSuccess) {
		return redeploy(verticleId, verticle, null, doOnSuccess);
	}

	protected Completable awareDeploy(String verticleId, Verticle verticle, Consumer<? super String> doOnSuccess) {
		return awareDeploy(verticleId, verticle, null, doOnSuccess);
	}

	@Deprecated
	protected Completable redeploy(String verticleId, String className, Consumer<? super String> doOnSuccess) {
		return redeploy(verticleId, className, null, doOnSuccess);
	}

	protected Completable awareDeploy(String verticleId, String className, Consumer<? super String> doOnSuccess) {
		return awareDeploy(verticleId, className, null, doOnSuccess);
	}

	@Deprecated
	protected Completable redeploy(String verticleId, Verticle verticle, DeploymentOptions deploymentOptions,
			Consumer<? super String> doOnSuccess) {
		final Supplier<Single<String>> supplier = () -> {
			return deploymentOptions != null ? deploy(verticle, deploymentOptions) : deploy(verticle);
		};
		return undeploy(verticleId).andThen(supplier.get()).doOnSuccess(doOnSuccess).ignoreElement();
	}

	protected Completable awareDeploy(String verticleId, Verticle verticle, DeploymentOptions deploymentOptions,
			Consumer<? super String> doOnSuccess) {
		final Supplier<Single<String>> supplier = () -> {
			return deploymentOptions != null ? deploy(verticle, deploymentOptions) : deploy(verticle);
		};
		return undeploy(verticleId).andThen(supplier.get()).doOnSuccess(doOnSuccess).ignoreElement();
	}

	@Deprecated
	protected Completable redeploy(String verticleId, String className, DeploymentOptions deploymentOptions,
			Consumer<? super String> doOnSuccess) {
		final Supplier<Single<String>> supplier = () -> {
			return deploymentOptions != null ? deploy(className, deploymentOptions) : deploy(className);
		};
		return undeploy(verticleId).andThen(supplier.get()).doOnSuccess(doOnSuccess).ignoreElement();
	}

	protected Completable awareDeploy(String verticleId, String className, DeploymentOptions deploymentOptions,
			Consumer<? super String> doOnSuccess) {
		final Supplier<Single<String>> supplier = () -> {
			return deploymentOptions != null ? deploy(className, deploymentOptions) : deploy(className);
		};
		return undeploy(verticleId).andThen(supplier.get()).doOnSuccess(doOnSuccess).ignoreElement();
	}

	@Deprecated
	protected Completable redeploy(String className, DeploymentOptions deploymentOptions) {
		List<Completable> list = new ArrayList<>();
		deploymentIds.entrySet().stream().forEach(e -> {
			if (className.equals(e.getValue())) {
				list.add(undeploy(e.getKey()));
			}
		});
		return Completable.merge(list).andThen(deploy(className, deploymentOptions)).ignoreElement();
	}

	protected Completable awareDeploy(String className, DeploymentOptions deploymentOptions) {
		List<Completable> list = new ArrayList<>();
		deploymentIds.entrySet().stream().forEach(e -> {
			if (className.equals(e.getValue())) {
				list.add(undeploy(e.getKey()));
			}
		});
		return Completable.merge(list).andThen(deploy(className, deploymentOptions)).ignoreElement();
	}

	protected Completable awareDeploy(Verticle verticle) {
		List<Completable> list = new ArrayList<>();
		String name = verticle.getClass().getName();
		deploymentIds.entrySet().stream().forEach(e -> {
			if (name.equals(e.getValue())) {
				list.add(undeploy(e.getKey()));
			}
		});
		return Completable.merge(list).andThen(deploy(verticle)).ignoreElement();
	}

	@Deprecated
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

	protected Completable awareDeploy(Verticle... verticles) {
		if (verticles == null || verticles.length == 0)
			return Completable.complete();
		List<Completable> list = new ArrayList<>();
		String name = verticles[0].getClass().getName();
		deploymentIds.entrySet().stream().forEach(e -> {
			if (name.equals(e.getValue())) {
				list.add(undeploy(e.getKey()));
			}
		});
		return Completable.merge(list).andThen(Completable.defer(() -> {
			List<Completable> dlist = Stream.of(verticles).map(v -> deploy(v).ignoreElement())
					.collect(Collectors.toList());
			return Completable.merge(dlist);
		}));
	}

	@Deprecated
	protected Completable redeploy(Verticle... verticles) {
		if (verticles == null || verticles.length == 0)
			return Completable.complete();
		List<Completable> list = new ArrayList<>();
		String name = verticles[0].getClass().getName();
		deploymentIds.entrySet().stream().forEach(e -> {
			if (name.equals(e.getValue())) {
				list.add(undeploy(e.getKey()));
			}
		});
		return Completable.merge(list).andThen(Completable.defer(() -> {
			List<Completable> dlist = Stream.of(verticles).map(v -> deploy(v).ignoreElement())
					.collect(Collectors.toList());
			return Completable.merge(dlist);
		}));
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

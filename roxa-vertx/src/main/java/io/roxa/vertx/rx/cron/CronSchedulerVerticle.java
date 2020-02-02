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
package io.roxa.vertx.rx.cron;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.eventbus.EventBus;
import io.vertx.reactivex.core.eventbus.Message;
import io.vertx.reactivex.core.shareddata.LocalMap;
import io.vertx.reactivex.core.shareddata.SharedData;

/**
 * @author Steven Chen
 *
 */
public class CronSchedulerVerticle extends AbstractVerticle {

	private static final Logger logger = LoggerFactory.getLogger(CronSchedulerVerticle.class);

	public static final String CRON_ADDR_SCHEDULE = "io.roxa.vertx.rx.cron.schedule";
	public static final String CRON_ADDR_CANCEL = "io.roxa.vertx.rx.cron.cancel";
	public static final String CRON_TOPIC_PREFIX = "io.roxa.vertx.rx.cron.jobs.";
	private static final String cronCache = "io.roxa.vertx.rx.cron.cache";
	private EventBus ebs;

	private CronSchedulerVerticle() {
	}

	static class CronSchedulerVerticleInitilizer {
		static final CronSchedulerVerticle instance;
		static {
			instance = new CronSchedulerVerticle();
		}
	}

	public static CronSchedulerVerticle instance() {
		return CronSchedulerVerticleInitilizer.instance;
	}

	public void start(Promise<Void> startPromise) throws Exception {
		ebs = vertx.eventBus();
		ebs.consumer(CRON_ADDR_CANCEL, msg -> {
			String id = (String) msg.body();
			vertx.sharedData().getLocalMap(cronCache).remove(id);
		});

		ebs.consumer(CRON_ADDR_SCHEDULE, this::handleSchedule);
		logger.info(
				"Prepare to start CronSchedulerVerticle, schedule event bus address: {}, cancel event bus address: {}",
				CRON_ADDR_SCHEDULE, CRON_ADDR_CANCEL);
		super.start(startPromise);
	}

	private void handleSchedule(Message<Object> msg) {
		Object body = msg.body();
		JsonObject cronJson = null;
		try {
			if (!(body instanceof JsonObject))
				throw new IllegalArgumentException("Cron JSON must be a JSON object");
			cronJson = (JsonObject) body;

			if (!cronJson.containsKey("id"))
				throw new IllegalArgumentException("Cron JSON must contain the cron_id");

			if (!cronJson.containsKey("expr"))
				throw new IllegalArgumentException("Cron JSON must contain the cron_expr");

			if (!cronJson.containsKey("topic"))
				throw new IllegalArgumentException("Cron JSON must contain the cron_topic");
		} catch (Exception e) {
			msg.fail(510, e.getMessage());
			return;
		}

		String cronExprLiteral = cronJson.getString("expr");
		String cronTopic = cronJson.getString("topic");
		String cronId = cronJson.getString("id");
		JsonObject cronPayload = cronJson.getJsonObject("payload", null);

		SharedData sd = vertx.sharedData();

		LocalMap<String, JsonObject> map = sd.getLocalMap(cronCache);

		if (map.putIfAbsent(cronId, cronJson) != null) {
			msg.fail(511, "cron_id alredy exists: " + cronId);
			return;
		}

		logger.info("Schedule event, id: {}, expr: {}, topic: {}, payload: {}", cronId, cronExprLiteral, cronTopic,
				cronPayload);

		CronScheduler.createCronObservable(vertx, cronExprLiteral).takeWhile(timestamped -> {
			return map.get(cronId) != null;
		}).subscribe(timestamped -> {
			if (cronPayload != null)
				ebs.publish(cronTopic, cronPayload);
			else
				ebs.publish(cronTopic, new JsonObject());
			msg.reply(cronId);
		}, e -> {
			final String errorMsg = "Unable to process cron " + cronExprLiteral + " for topic " + cronTopic;
			logger.error(errorMsg, e);
			msg.fail(512, errorMsg);
		});
	}

}

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

import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.quartz.CronExpression;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Timed;
import io.reactivex.subjects.PublishSubject;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.RxHelper;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.eventbus.EventBus;

/**
 * @author Steven Chen
 *
 */
public class CronScheduler {

	/**
	 * <p>
	 * http://www.quartz-scheduler.org/api/2.2.0/org/quartz/CronExpression.html
	 * http://www.cronmaker.com
	 * 
	 * @param vertx    - The instance of Vertx
	 * @param id       - The id of Job
	 * @param cronExpr - The Cron expression literal, to refer cronmaker
	 * @return
	 */
	public static Observable<JsonObject> schedule(Vertx vertx, String id, String cronExpr) {
		return schedule(vertx, id, cronExpr, null);
	}

	/**
	 * 
	 * @param vertx    - The instance of Vertx
	 * @param id       - The id of Job
	 * @param cronExpr - The Cron expression literal, to refer cronmaker
	 * @param payload  - The parameters of event
	 */
	public static Observable<JsonObject> schedule(Vertx vertx, String id, String cronExpr, JsonObject payload) {
		EventBus ebs = vertx.eventBus();
		String topic = CronSchedulerVerticle.CRON_TOPIC_PREFIX + id;
		JsonObject schedulePlan = new JsonObject().put("id", id).put("topic", topic).put("expr", cronExpr);
		if (payload != null)
			schedulePlan.put("payload", payload);
		ebs.send(CronSchedulerVerticle.CRON_ADDR_SCHEDULE, schedulePlan);
		PublishSubject<JsonObject> subject = PublishSubject.create();
		ebs.consumer(topic, msg -> {
			JsonObject payloadBody = (JsonObject) msg.body();
			subject.onNext(payloadBody);
		});
		return subject;
	}

	public static void cancel(Vertx vertx, String id) {
		EventBus ebs = vertx.eventBus();
		ebs.send(CronSchedulerVerticle.CRON_ADDR_CANCEL, id);
	}

	static Observable<Timed<Long>> createCronObservable(Vertx vertx, String cronExprLiteral) {
		return createCronObservable(vertx, cronExprLiteral, null);

	}

	static Observable<Timed<Long>> createCronObservable(Vertx vertx, String cronExprLiteral, String timeZoneName) {
		if (timeZoneName != null) {
			Boolean noneMatch = Arrays.stream(TimeZone.getAvailableIDs())
					.noneMatch(available -> available.equals(timeZoneName));
			if (noneMatch)
				throw new IllegalArgumentException("timeZoneName " + timeZoneName + " is invalid");
		}
		Scheduler scheduler = RxHelper.scheduler(vertx);

		return Observable.just(cronExprLiteral).map(v -> toCronExpression(v, timeZoneName))
				.map(CronScheduler::calculateDelayMillis).flatMap(delay -> {
					return Observable.timer(delay, TimeUnit.MILLISECONDS, scheduler);
				}).timestamp().repeat();

	}

	private static long calculateDelayMillis(CronExpression expr) {
		long nowMillisOffset = System.currentTimeMillis() + 500;
		Date nextRunDate = expr.getNextValidTimeAfter(new Date(nowMillisOffset));
		long nowMillis = System.currentTimeMillis();
		return nextRunDate.getTime() - nowMillis;
	}

	private static CronExpression toCronExpression(String cronExprLiteral, String timeZoneName) {
		CronExpression cron;
		try {
			cron = new CronExpression(cronExprLiteral);
			if (timeZoneName != null) {
				cron.setTimeZone(TimeZone.getTimeZone(timeZoneName));
			}
			return cron;
		} catch (ParseException e) {
			throw new IllegalArgumentException("Invalid cron expr literal " + cronExprLiteral, e);
		}

	}
}

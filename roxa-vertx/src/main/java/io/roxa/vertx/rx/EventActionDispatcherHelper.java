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

import io.reactivex.Single;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.eventbus.Message;

/**
 * @author Steven Chen
 *
 */
public abstract class EventActionDispatcherHelper {

	public static final JsonObject EMPTY_JSON = new JsonObject();

	public static final JsonArray EMPTY_JSON_ARRAY = new JsonArray();

	public static <T> Single<Message<T>> request(Vertx vertx, String actionSignature) {
		return request(vertx, actionSignature, null);
	}

	public static <T> Single<Message<T>> request(Vertx vertx, String actionSignature, JsonObject params) {
		String[] pair = resolveActionSignature(actionSignature);
		return vertx.eventBus().rxRequest(pair[0], params,
				new DeliveryOptions()
						.addHeader(EventActionDispatcher.EVENT_HEADER_STYLE, EventActionDispatcher.EVENT_STYLE_REQUEST)
						.addHeader(EventActionDispatcher.EVENT_HEADER_ACTION, pair[1]));
	}

	public static void send(Vertx vertx, String actionSignature) {
		send(vertx, actionSignature, null);
	}

	public static void send(Vertx vertx, String actionSignature, JsonObject params) {
		String[] pair = resolveActionSignature(actionSignature);
		vertx.eventBus().send(pair[0], params,
				new DeliveryOptions()
						.addHeader(EventActionDispatcher.EVENT_HEADER_STYLE, EventActionDispatcher.EVENT_STYLE_SEND)
						.addHeader(EventActionDispatcher.EVENT_HEADER_ACTION, pair[1]));
	}

	public static void publish(Vertx vertx, String actionSignature) {
		publish(vertx, actionSignature, null);
	}

	public static void publish(Vertx vertx, String actionSignature, JsonObject params) {
		String[] pair = resolveActionSignature(actionSignature);
		vertx.eventBus().publish(pair[0], params,
				new DeliveryOptions()
						.addHeader(EventActionDispatcher.EVENT_HEADER_STYLE, EventActionDispatcher.EVENT_STYLE_PUBLISH)
						.addHeader(EventActionDispatcher.EVENT_HEADER_ACTION, pair[1]));
	}

	private static String[] resolveActionSignature(String actionSignature) {
		return actionSignature.split("#");
	}

}

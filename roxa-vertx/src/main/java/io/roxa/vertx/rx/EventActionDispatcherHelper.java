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

	public static <T> Single<Message<T>> request(Vertx vertx, String address, String action) {
		return request(vertx, address, action, null);
	}

	public static <T> Single<Message<T>> request(Vertx vertx, String address, String action, JsonObject params) {
		return vertx.eventBus().<T>rxRequest(address, params,
				new DeliveryOptions()
						.addHeader(EventActionDispatcher.EVENT_HEADER_STYLE, EventActionDispatcher.EVENT_STYLE_REQUEST)
						.addHeader(EventActionDispatcher.EVENT_HEADER_ACTION, action));
	}

	public static void send(Vertx vertx, String address, String action) {
		send(vertx, address, action, null);
	}

	public static void send(Vertx vertx, String address, String action, JsonObject params) {
		vertx.eventBus().send(address, params,
				new DeliveryOptions()
						.addHeader(EventActionDispatcher.EVENT_HEADER_STYLE, EventActionDispatcher.EVENT_STYLE_SEND)
						.addHeader(EventActionDispatcher.EVENT_HEADER_ACTION, action));
	}

	public static void publish(Vertx vertx, String address, String action) {
		publish(vertx, address, action, null);
	}

	public static void publish(Vertx vertx, String address, String action, JsonObject params) {
		vertx.eventBus().publish(address, params,
				new DeliveryOptions()
						.addHeader(EventActionDispatcher.EVENT_HEADER_STYLE, EventActionDispatcher.EVENT_STYLE_PUBLISH)
						.addHeader(EventActionDispatcher.EVENT_HEADER_ACTION, action));
	}

}

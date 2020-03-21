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
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;

/**
 * @author Steven Chen
 *
 */
public class EventActionEndpoint {

	private final Vertx vertx;

	private String action;

	private String urn;

	private EventActionEndpoint(Vertx vertx) {
		this.vertx = vertx;
	}

	public static EventActionEndpoint create(Vertx vertx) {
		return new EventActionEndpoint(vertx);
	}

	public EventActionEndpoint urn(String urn) {
		this.urn = urn;
		return this;
	}

	public EventActionEndpoint action(String action) {
		this.action = action;
		return this;
	}

	public <T> Single<T> request() {
		return EventActionDispatcherHelper.<T>request(vertx, urn, action).map(reply -> reply.body());
	}

	public <T> Single<T> request(JsonObject params) {
		return EventActionDispatcherHelper.<T>request(vertx, urn, action, params).map(reply -> reply.body());
	}

}

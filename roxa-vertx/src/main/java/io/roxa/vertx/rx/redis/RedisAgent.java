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
package io.roxa.vertx.rx.redis;

import java.util.List;

import io.reactivex.Single;
import io.roxa.vertx.rx.EventActionEndpoint;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;

/**
 * @author Steven Chen
 *
 */
public class RedisAgent {

	private final Vertx vertx;
	private final String urn;

	public static RedisAgent create(Vertx vertx, String sourceName) {
		String urn = String.format("roxa.resource.redis::%s", sourceName);
		return new RedisAgent(vertx, urn);
	}

	private RedisAgent(Vertx vertx, String urn) {
		this.vertx = vertx;
		this.urn = urn;
	}

	public Single<String> del(List<String> keys) {
		JsonObject msg = new JsonObject().put("keys", new JsonArray(keys));
		return EventActionEndpoint.create(vertx).urn(urn).action("del").<String>request(msg);
	}

	/**
	 * 
	 * @param key
	 * @return
	 */
	public Single<String> incr(String key) {
		JsonObject msg = new JsonObject().put("key", key);
		return EventActionEndpoint.create(vertx).urn(urn).action("incr").<String>request(msg);
	}

	/**
	 * 
	 * @param key
	 * @return
	 */
	public Single<String> get(String key) {
		JsonObject msg = new JsonObject().put("key", key);
		return EventActionEndpoint.create(vertx).urn(urn).action("get").<String>request(msg);
	}

	/**
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public Single<String> set(String key, String value) {
		JsonObject msg = new JsonObject().put("key", key).put("value", value);
		return EventActionEndpoint.create(vertx).urn(urn).action("set").<String>request(msg);
	}

	/**
	 * @param key
	 * @param field
	 * @param value
	 */
	public Single<String> hset(String key, String field, String value) {
		JsonObject msg = new JsonObject().put("key", key).put("field", field).put("value", value);
		return EventActionEndpoint.create(vertx).urn(urn).action("hset").<String>request(msg);
	}

	/**
	 * 
	 * @param key
	 * @param field
	 * @param value
	 * @return
	 */
	public Single<String> hincrby(String key, String field, String value) {
		JsonObject msg = new JsonObject().put("key", key).put("field", field).put("value", value);
		return EventActionEndpoint.create(vertx).urn(urn).action("hincrby").<String>request(msg);
	}

	/**
	 * 
	 * @param key
	 * @return
	 */
	public Single<JsonArray> hkeys(String key) {
		JsonObject msg = new JsonObject().put("key", key);
		return EventActionEndpoint.create(vertx).urn(urn).action("hkeys").<JsonArray>request(msg);
	}

	/**
	 * 
	 * @param key
	 * @param field
	 * @return
	 */
	public Single<String> hget(String key, String field) {
		JsonObject msg = new JsonObject().put("key", key).put("field", field);
		return EventActionEndpoint.create(vertx).urn(urn).action("hget").<String>request(msg);
	}

	/**
	 * put JSON value with key
	 * 
	 * @param key
	 * @param body
	 */
	public Single<JsonObject> hmset(String key, JsonObject data) {
		JsonObject msg = new JsonObject().put("key", key).put("data", data);
		return EventActionEndpoint.create(vertx).urn(urn).action("hmset").<JsonObject>request(msg);
	}

	/**
	 * Get JSON value with key
	 * 
	 * @param key
	 * @return
	 */
	public Single<JsonObject> hmget(String key) {
		JsonObject msg = new JsonObject().put("key", key);
		return EventActionEndpoint.create(vertx).urn(urn).action("hmget").<JsonObject>request(msg);

	}
}

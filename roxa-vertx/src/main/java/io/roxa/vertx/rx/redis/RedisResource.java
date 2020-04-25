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
import java.util.stream.Collectors;

import io.reactivex.Single;
import io.roxa.vertx.rx.EventActionDispatcher;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * @author Steven Chen
 *
 */
public class RedisResource extends EventActionDispatcher {

	private RedisExecutor redisExecutor;

	public RedisResource(RedisExecutor redisExecutor, String resourceName) {
		super(String.format("roxa.resource.redis::%s", resourceName));
		this.redisExecutor = redisExecutor;
	}

	public Single<String> del(JsonObject msg) {
		List<String> keys = msg.getJsonArray("keys").stream().map(e -> (String) e).collect(Collectors.toList());
		if (keys == null || keys.isEmpty())
			return Single.just("0");
		return redisExecutor.del(keys);
	}

	public Single<String> incr(JsonObject msg) {
		String key = msg.getString("key");
		return redisExecutor.incr(key);
	}

	public Single<String> get(JsonObject msg) {
		String key = msg.getString("key");
		return redisExecutor.get(key);
	}

	public Single<String> set(JsonObject msg) {
		String key = msg.getString("key");
		String value = msg.getString("value");
		return redisExecutor.set(key, value);
	}

	public Single<String> hset(JsonObject msg) {
		String key = msg.getString("key");
		String field = msg.getString("field");
		String value = msg.getString("value");
		return redisExecutor.hset(key, field, value);

	}

	public Single<String> hincrby(JsonObject msg) {
		String key = msg.getString("key");
		String field = msg.getString("field");
		String value = msg.getString("value");
		return redisExecutor.hincrby(key, field, value);
	}

	public Single<JsonArray> hkeys(JsonObject msg) {
		String key = msg.getString("key");
		return redisExecutor.hkeys(key);
	}

	public Single<String> hget(JsonObject msg) {
		String key = msg.getString("key");
		String field = msg.getString("field");
		return redisExecutor.hget(key, field);
	}

	public Single<JsonObject> hmset(JsonObject msg) {
		String key = msg.getString("key");
		JsonObject data = msg.getJsonObject("data");
		return redisExecutor.hmset(key, data);
	}

	public Single<JsonObject> hmget(JsonObject msg) {
		String key = msg.getString("key");
		return redisExecutor.hmget(key);

	}
}

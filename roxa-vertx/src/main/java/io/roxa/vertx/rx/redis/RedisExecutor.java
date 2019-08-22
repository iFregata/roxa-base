/**
 * The MIT License
 * 
 * Copyright (c) 2018-2020 Shell Technologies PTY LTD
 *
 * You may obtain a copy of the License at
 * 
 *       http://mit-license.org/
 *       
 */
package io.roxa.vertx.rx.redis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.SocketAddress;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.redis.client.Redis;
import io.vertx.reactivex.redis.client.RedisAPI;
import io.vertx.reactivex.redis.client.Response;
import io.vertx.redis.client.RedisClientType;
import io.vertx.redis.client.RedisOptions;
import io.vertx.redis.client.RedisRole;
import io.vertx.redis.client.impl.types.SimpleStringType;

/**
 * @author Steven Chen
 *
 */
public class RedisExecutor {

	private static final Logger logger = LoggerFactory.getLogger(RedisExecutor.class);

	private static final JsonObject EMPTY_JsonObject = new JsonObject();
	private static final JsonArray EMPTY_JsonArray = new JsonArray();
	private static final Response NIL_RESP = Response.newInstance(SimpleStringType.create("NIL"));
	private static final Response ZERO_RESP = Response.newInstance(SimpleStringType.create("0"));

	private RedisAPI redisAPI;

	RedisExecutor() {
	}

	/**
	 * 
	 * @param vertx
	 * @param conf
	 * @return
	 */
	public static RedisExecutor create(Vertx vertx, JsonObject conf) {
		RedisExecutor redisExecutor = new RedisExecutor();
		createRedisClient(vertx, conf).subscribe(redis -> {
			redisExecutor.redisAPI = RedisAPI.api(redis);
		}, e -> {
			logger.error("Create redis client error", e);
		});
		return redisExecutor;
	}

	/**
	 * 
	 * @param vertx
	 * @param conf
	 * @return
	 */
	public static RedisExecutor createSync(Vertx vertx, JsonObject conf) {
		RedisExecutor redisExecutor = new RedisExecutor();
		redisExecutor.redisAPI = RedisAPI.api(createRedisClient(vertx, conf).blockingGet());
		return redisExecutor;
	}

	public Single<String> del(List<String> keys) {
		if (keys == null || keys.isEmpty())
			return Single.just("0");
		return redisAPI.rxDel(keys).map(resp -> keys.stream().collect(Collectors.joining(","))).toSingle();
	}

	/**
	 * 
	 * @param key
	 * @return
	 */
	public Single<String> incr(String key) {
		return redisAPI.rxIncr(key).toSingle(ZERO_RESP).map(resp -> {
			return resp.toString();
		});
	}

	/**
	 * 
	 * @param key
	 * @return
	 */
	public Single<String> get(String key) {
		return redisAPI.rxGet(key).toSingle(NIL_RESP).map(resp -> {
			return resp.toString();
		});
	}

	/**
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public Single<String> set(String key, String value) {
		return redisAPI.rxSet(Arrays.asList(key, value)).map(r -> value).toSingle();
	}

	/**
	 * @param key
	 * @param field
	 * @param value
	 */
	public Single<String> hset(String key, String field, String value) {
		return redisAPI.rxHset(Arrays.asList(key, field, value)).map(r -> value).toSingle();

	}

	/**
	 * 
	 * @param key
	 * @param field
	 * @param value
	 * @return
	 */
	public Single<String> hincrby(String key, String field, String value) {
		return redisAPI.rxHincrby(key, field, value).toSingle(ZERO_RESP).map(resp -> {
			return resp.toString();
		});
	}

	/**
	 * 
	 * @param key
	 * @return
	 */
	public Single<JsonArray> hkeys(String key) {
		return redisAPI.rxHkeys(key).map(resp -> {
			if (resp.size() == 0) {
				return EMPTY_JsonArray;
			}
			JsonArray list = new JsonArray();
			resp.forEach(e -> {
				list.add(e.toString());
			});
			return list;
		}).toSingle();
	}

	/**
	 * 
	 * @param key
	 * @param field
	 * @return
	 */
	public Single<String> hget(String key, String field) {
		return redisAPI.rxHget(key, field).toSingle(NIL_RESP).map(resp -> {
			return resp.toString();
		});
	}

	/**
	 * put JSON value with key
	 * 
	 * @param key
	 * @param body
	 */
	public Single<JsonObject> hmset(String key, JsonObject data) {
		return redisAPI.rxHmset(hmsetLeteral(key, data)).map(r -> data).toSingle();
	}

	/**
	 * Get JSON value with key
	 * 
	 * @param key
	 * @return
	 */
	public Single<JsonObject> hmget(String key) {
		return redisAPI.rxHgetall(key).map(resp -> {
			if (resp.size() == 0)
				return EMPTY_JsonObject;
			JsonObject json = new JsonObject();
			for (int i = 0; i < resp.size(); i += 2) {
				json.put(resp.get(i).toString(), resp.get(i + 1).toString());
			}
			return json;
		}).toSingle();

	}

	private static Single<Redis> createRedisClient(Vertx vertx, JsonObject conf) {
		RedisOptions redisOptions = new RedisOptions();
		JsonArray redisGroup = conf.getJsonArray("redis_conf");
		if (redisGroup == null || redisGroup.isEmpty())
			throw new IllegalStateException("No redis host, port configuration found!");
		if (redisGroup.size() == 1) {
			JsonObject cfg = (JsonObject) redisGroup.getJsonObject(0);
			logger.debug("Prepare to connect redis standalone server, {}", cfg.encode());
			return Redis.createClient(vertx, io.vertx.reactivex.core.net.SocketAddress
					.inetSocketAddress(cfg.getInteger("port"), cfg.getString("host"))).rxConnect();
		}
		redisGroup.stream().forEach(e -> {
			JsonObject cfg = (JsonObject) e;
			redisOptions.addEndpoint(SocketAddress.inetSocketAddress(cfg.getInteger("port"), cfg.getString("host")));
			redisOptions.setType(RedisClientType.CLUSTER);
			redisOptions.setRole(RedisRole.MASTER);
		});
		logger.debug("Prepare to connect redis cluster servers, {}", redisGroup.encode());
		return Redis.createClient(vertx, redisOptions).rxConnect();
	}

	private static List<String> hmsetLeteral(String key, JsonObject data) {
		List<String> list = new ArrayList<>();
		list.add(key);
		data.stream().forEach(e -> {
			list.add(e.getKey());
			list.add(String.valueOf(e.getValue()));
		});
		return list;
	}

}

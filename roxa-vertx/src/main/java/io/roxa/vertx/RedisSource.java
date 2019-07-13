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
package io.roxa.vertx;

import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.RedisClient;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.types.RedisDataSource;

/**
 * @author Steven Chen
 *
 */
@Deprecated
public abstract class RedisSource {

	private static final Logger logger = LoggerFactory.getLogger(RedisSource.class);

	public static <T> Future<T> exec(ServiceDiscovery discovery, Function<RedisClient, Future<T>> callback) {
		Future<T> future = Future.future();
		RedisDataSource.getRedisClient(discovery, new JsonObject().put("name", "roxa.vertx::service.default-redis"),
				ar -> {
					if (ar.succeeded()) {
						RedisClient redisClient = ar.result();
						callback.apply(redisClient).setHandler(applied -> {
							ServiceDiscovery.releaseServiceObject(discovery, redisClient);
							logger.info("RedisDataSource client released.");
							if (applied.failed()) {
								future.fail(applied.cause());
							} else {
								future.complete(applied.result());
							}
						});
					} else {
						logger.error("RedisDataSource could not discovery!", ar.cause());
						future.fail(ar.cause());
					}
				});
		return future;
	}
}

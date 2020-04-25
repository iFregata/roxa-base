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

import io.reactivex.Single;
import io.roxa.vertx.rx.ResourceDeployer;
import io.vertx.core.Verticle;
import io.vertx.core.json.JsonObject;

/**
 * @author Steven Chen
 *
 */
public class RedisDeployer extends ResourceDeployer {

	public RedisDeployer(String resourceName) {
		super("redis", resourceName);
	}

	@Override
	protected Single<Verticle> getResourceAgent(JsonObject cfg) {
		RedisExecutor redisExecutor = RedisExecutor.createSync(vertx, cfg);
		return Single.just(new RedisResource(redisExecutor, resourceName));
	}
}

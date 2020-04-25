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
package io.roxa.vertx.rx.cassandra;

import java.util.List;
import java.util.stream.Collectors;

import io.reactivex.Single;
import io.roxa.vertx.rx.ResourceDeployer;
import io.vertx.core.Verticle;
import io.vertx.core.json.JsonObject;

/**
 * @author Steven Chen
 *
 */
public class CassandraDeployer extends ResourceDeployer {

	public CassandraDeployer(String resourceName) {
		super("cassandra", resourceName);
	}

	@Override
	protected Single<Verticle> getResourceAgent(JsonObject cfg) {
		List<String> endpoints = cfg.getJsonArray("endpoints").stream().map(i -> (String) i)
				.collect(Collectors.toList());
		int port = cfg.getInteger("port");
		return Single.just(new CassandraResource(resourceName, endpoints, port));
	}
}

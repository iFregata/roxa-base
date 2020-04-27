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
package io.roxa.vertx.rx.nitrite;

import io.reactivex.Single;
import io.roxa.vertx.rx.ResourceDeployer;
import io.vertx.core.Verticle;
import io.vertx.core.json.JsonObject;

/**
 * @author Steven Chen
 *
 */
public class NitriteDeployer extends ResourceDeployer {

	public NitriteDeployer(String resourceName) {
		super("nitrite", resourceName, false);
	}

	@Override
	protected Single<Verticle> getResourceAgent(JsonObject cfg) {
		return Single.just(new NitriteResource(resourceName));
	}
}

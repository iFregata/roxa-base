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
package io.roxa.vertx.rx.cfg;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.SingleHelper;
import io.vertx.reactivex.core.Vertx;

/**
 * @author Steven Chen
 *
 */
public class ConfigRegistry {

	private io.roxa.vertx.cfg.ConfigRegistry delegate;

	public static ConfigRegistry create(Vertx vertx, String registryLocation, boolean cached) {
		io.roxa.vertx.cfg.ConfigRegistry delegate = io.roxa.vertx.cfg.ConfigRegistry.create(vertx.getDelegate(),
				registryLocation, cached);
		ConfigRegistry inst = new ConfigRegistry(delegate);
		return inst;
	}

	public static ConfigRegistry create(Vertx vertx, String registryLocation) {
		io.roxa.vertx.cfg.ConfigRegistry delegate = io.roxa.vertx.cfg.ConfigRegistry.create(vertx.getDelegate(),
				registryLocation);
		ConfigRegistry inst = new ConfigRegistry(delegate);
		return inst;
	}

	public Single<JsonObject> getCached() {
		return SingleHelper.toSingle(handler -> delegate.getCached().setHandler(handler));
	}

	public String getRegistryLocation() {
		return delegate.getRegistryLocation();
	}

	public String getEventAddress() {
		return delegate.getEventAddress();
	}

	public String getCacheAddress() {
		return delegate.getCacheAddress();
	}

	public void destroy() {
		delegate.destroy();
	}

	private ConfigRegistry(io.roxa.vertx.cfg.ConfigRegistry delegate) {
		this.delegate = delegate;
	}
}

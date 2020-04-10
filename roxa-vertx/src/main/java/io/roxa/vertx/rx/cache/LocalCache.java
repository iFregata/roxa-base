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
package io.roxa.vertx.rx.cache;

import java.util.List;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.SingleHelper;
import io.vertx.reactivex.core.Vertx;

/**
 * @author Steven Chen
 *
 */
public class LocalCache {

	private io.roxa.vertx.cache.LocalCache delegate;

	public static LocalCache create(Vertx vertx) {
		io.vertx.core.Vertx vertxDelegate = vertx.getDelegate();
		io.roxa.vertx.cache.LocalCache delegate = io.roxa.vertx.cache.LocalCache.create(vertxDelegate);
		LocalCache inst = new LocalCache(delegate);
		return inst;
	}

	public LocalCache(io.roxa.vertx.cache.LocalCache delegate) {
		this.delegate = delegate;
	}

	public Single<JsonObject> remove(String key) {
		return SingleHelper.<JsonObject>toSingle(handler -> {
			delegate.remove(key).onComplete(handler);
		});

	}

	public Single<JsonObject> put(String key, Integer period, JsonObject item) {
		return SingleHelper.<JsonObject>toSingle(handler -> {
			delegate.put(key, period, item).onComplete(handler);
		});
	}

	public Single<JsonObject> put(String key, JsonObject item) {
		return SingleHelper.<JsonObject>toSingle(handler -> {
			delegate.put(key, item).onComplete(handler);
		});
	}

	public void clear() {
		delegate.clear();
	}

	public List<String> list() {
		return delegate.list();
	}

	public Single<JsonObject> get(String key) {
		return SingleHelper.<JsonObject>toSingle(handler -> {
			delegate.get(key).onComplete(handler);
		});
	}
}

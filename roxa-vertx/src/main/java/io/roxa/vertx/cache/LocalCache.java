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
package io.roxa.vertx.cache;

import java.util.List;
import java.util.stream.Collectors;

import io.roxa.GeneralFailureException;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.shareddata.SharedData;

/**
 * @author Steven Chen
 *
 */
public class LocalCache {

	private LocalMap<String, JsonObject> sharedMap;

	public static LocalCache create(Vertx vertx) {
		SharedData sd = vertx.sharedData();
		LocalMap<String, JsonObject> sharedMap = sd.getLocalMap("roxa.vertx::localShard:gophe-cache");
		return new LocalCache(sharedMap);
	}

	private LocalCache(LocalMap<String, JsonObject> sharedMap) {
		this.sharedMap = sharedMap;
	}

	public Future<JsonObject> remove(String key) {
		JsonObject item = sharedMap.remove(key);
		return Future.succeededFuture(item);
	}

	public Future<JsonObject> put(String key, Integer period, JsonObject item) {
		if (item == null || item.isEmpty())
			return Future.failedFuture(new GeneralFailureException(900, "Item is null or empty!"));
		PeriodItem periodItem = new PeriodItem(period, item);
		sharedMap.put(key, periodItem.asJson());
		return Future.succeededFuture(item);
	}

	public Future<JsonObject> put(String key, JsonObject item) {
		if (item == null || item.isEmpty())
			return Future.failedFuture(new GeneralFailureException(900, "Item is null or empty!"));
		PeriodItem periodItem = new PeriodItem(item);
		sharedMap.put(key, periodItem.asJson());
		return Future.succeededFuture(item);
	}

	public void clear() {
		sharedMap.clear();
	}

	public List<String> list() {
		return sharedMap.entrySet().stream()
				.map(e -> String.format("%s=%s", e.getKey(), e.getValue() == null ? "" : e.getValue().encode()))
				.collect(Collectors.toList());
	}

	public Future<JsonObject> get(String key) {
		JsonObject data = sharedMap.get(key);
		if (data == null)
			return Future.failedFuture("No value with key: " + key);
		PeriodItem periodItem = PeriodItem.of(data);
		if (periodItem.isNotPeriod())
			return Future.succeededFuture(periodItem.getItem());
		if (periodItem.isExpired()) {
			sharedMap.remove(key);
			return Future.failedFuture(new GeneralFailureException(900, "Cached item has been expired"));
		}
		return Future.succeededFuture(periodItem.getItem());
	}
}

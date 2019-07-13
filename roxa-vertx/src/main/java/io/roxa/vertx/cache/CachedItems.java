/**
 * The MIT License
 * 
 * Copyright (c) 2016-2018 Shell Technologies PTY LTD
 *
 * You may obtain a copy of the License at
 * 
 *       http://mit-license.org/
 *       
 */
package io.roxa.vertx.cache;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.roxa.GeneralFailureException;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.shareddata.SharedData;

/**
 * @author Steven Chen
 *
 */
public class CachedItems {

	private static final Logger logger = LoggerFactory.getLogger(CachedItems.class);

	private LocalMap<String, CachedItem<?>> cachedObjects;

	/**
	 * 
	 */
	private CachedItems(LocalMap<String, CachedItem<?>> shardMap) {
		cachedObjects = shardMap;
	}

	public static CachedItems create(Vertx vertx) {
		SharedData sd = vertx.sharedData();
		LocalMap<String, CachedItem<?>> shardMap = sd.getLocalMap("vertx-shard:gophe-CachedItems");
		return new CachedItems(shardMap);
	}

	public <T> Future<List<T>> putAll(List<CachedItem<T>> cachedItemList) {
		Objects.requireNonNull(cachedItemList);
		List<T> list = cachedItemList.stream().map(this::putItem).collect(Collectors.toList());
		return Future.succeededFuture(list);
	}

	public <T> Future<T> put(CachedItem<T> cachedItem) {
		return Future.succeededFuture(putItem(cachedItem));
	}

	public <T> T putNow(CachedItem<T> cachedItem) {
		return putItem(cachedItem);
	}

	public <R> Future<R> get(String cacheId, Class<R> clazz) {
		Objects.requireNonNull(cacheId);
		String typeName = clazz.getName();
		String cacheKey = String.format("%s#%s", typeName, cacheId);
		R val = this.<R>find(cacheKey);
		if (val == null)
			return Future.failedFuture(
					new GeneralFailureException(String.format("No cached value with key: %s found!", cacheKey)));
		logger.info("Hit cache item with key: {}", cacheKey);
		return Future.succeededFuture(val);

	}

	public <R> Future<R> get(CachedItem<?> example, Class<R> clazz) {
		Objects.requireNonNull(example);
		String typeName = clazz.getName();
		String cacheKey = String.format("%s#%s", typeName, example.getCacheId());
		R val = this.<R>find(cacheKey);
		if (val == null)
			return Future.failedFuture(
					new GeneralFailureException(String.format("No cached value with key: %s found!", cacheKey)));
		logger.info("Hit cache item with key: {}", cacheKey);
		return Future.succeededFuture(val);

	}

	@SuppressWarnings("unchecked")
	protected <R> R find(String cacheKey) {
		return (R) cachedObjects.get(cacheKey);
	}

	private <T> T putItem(CachedItem<T> cachedItem) {
		Objects.requireNonNull(cachedItem);
		String cacheId = cachedItem.getCacheId();
		T val = cachedItem.item();
		Objects.requireNonNull(cacheId);
		Objects.requireNonNull(val);
		String typeName = val.getClass().getName();
		String cacheKey = String.format("%s#%s", typeName, cacheId);
		logger.info("Cache item key: {}", cacheKey);
		cachedObjects.put(cacheKey, cachedItem);
		return val;
	}
}

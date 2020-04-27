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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.dizitart.no2.Cursor;
import org.dizitart.no2.Document;
import org.dizitart.no2.Filter;
import org.dizitart.no2.IndexOptions;
import org.dizitart.no2.IndexType;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteCollection;
import org.dizitart.no2.UpdateOptions;
import org.dizitart.no2.WriteResult;
import org.dizitart.no2.filters.Filters;
import org.dizitart.no2.mapper.JacksonFacade;
import org.dizitart.no2.mapper.MapperFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.roxa.vertx.rx.EventActionDispatcher;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * @author Steven Chen
 *
 */
public class NitriteResource extends EventActionDispatcher {

	private static final Logger logger = LoggerFactory.getLogger(NitriteResource.class);

	private Nitrite nitrite;

	private static MapperFacade mapperFacade = new JacksonFacade();

	private String resourceName;

	public NitriteResource(String resourceName) {
		super(String.format("roxa.resource.nitrite::%s", resourceName));
		this.resourceName = resourceName;
	}

	public Single<Integer> delete(JsonObject msg) {
		String collection = msg.getString("collection");
		JsonObject filterValues = msg.getJsonObject("filter");
		NitriteCollection nc = nitrite.getCollection(collection);
		return delete(nc, filterValues).map(wr -> wr.getAffectedCount());
	}

	public Single<JsonArray> select(JsonObject msg) {
		String collection = msg.getString("collection");
		JsonObject filterValues = msg.getJsonObject("filter");
		NitriteCollection nc = nitrite.getCollection(collection);
		return select(nc, filterValues).map(docs -> {
			return new JsonArray(docs.stream().map(doc -> toJsonObject(doc)).collect(Collectors.toList()));
		});
	}

	public Single<Integer> upsert(JsonObject msg) {
		String collection = msg.getString("collection");
		JsonArray values = msg.getJsonArray("values");
		JsonObject filterValues = msg.getJsonObject("filter");
		NitriteCollection nc = nitrite.getCollection(collection);
		if (filterValues != null && !filterValues.isEmpty()) {
			indexUnique(nc, filterValues);
		}
		List<Single<WriteResult>> upserts = values.stream().map(e -> (JsonObject) e)
				.map(doc -> upsert(nc, filterValues, doc)).collect(Collectors.toList());
		return Single.concat(upserts).toList()
				.map(list -> list.stream().collect(Collectors.summingInt(wr -> wr.getAffectedCount())));
	}

	private Single<WriteResult> delete(NitriteCollection nc, JsonObject filterValues) {
		return vertx.<WriteResult>rxExecuteBlocking(promise -> {
			Filter filter = and(filterValues);
			WriteResult wr;
			if (filter != null) {
				wr = nc.remove(Filters.ALL);
			} else {
				wr = nc.remove(filter);
			}
			promise.complete(wr);
		}).toSingle();
	}

	private Single<List<Document>> select(NitriteCollection nc, JsonObject filterValues) {
		return vertx.<List<Document>>rxExecuteBlocking(promise -> {
			Filter filter = and(filterValues);
			Cursor cur;
			if (filter == null) {
				cur = nc.find();
			} else {
				cur = nc.find(filter);
			}
			List<Document> list = new ArrayList<>();
			cur.forEach(e -> list.add(e));
			promise.complete(list);
		}).toSingle();
	}

	private Single<WriteResult> upsert(NitriteCollection nc, JsonObject filterValues, JsonObject value) {
		return vertx.<WriteResult>rxExecuteBlocking(promise -> {
			Filter filter = and(filterValues);
			Document doc = toDocument(value);
			WriteResult wr;
			if (filter != null) {
				UpdateOptions uop = new UpdateOptions();
				uop.setUpsert(true);
				wr = nc.update(filter, doc, uop);
			} else
				wr = nc.update(doc, true);
			promise.complete(wr);
		}).toSingle();
	}

	private void indexUnique(NitriteCollection nc, JsonObject filter) {
		vertx.<JsonArray>rxExecuteBlocking(promise -> {
			List<String> keys = filter.stream().map(e -> e.getKey()).collect(Collectors.toList());
			JsonArray uniqueIndex = new JsonArray();
			keys.stream().forEach(key -> {
				if (!nc.hasIndex(key)) {
					nc.createIndex(key, IndexOptions.indexOptions(IndexType.Unique, true));
					uniqueIndex.add(key);
				}
			});
			promise.complete(uniqueIndex);
		}).subscribe(keys -> {
			logger.debug("Unique index for collection {}, keys: {}", nc.getName(), keys.encode());
		});

	}

	@Override
	protected void didSetupDispatch() {
		final String userDir = System.getProperty("user.dir");
		vertx.fileSystem().rxMkdir(String.format("%s/data", userDir)).onErrorResumeNext(e -> Completable.complete())
				.andThen(vertx.rxExecuteBlocking(promise -> {
					String filePath = String.format("%s/data/nitrite_%s.db", userDir, resourceName);
					nitrite = Nitrite.builder().filePath(filePath).openOrCreate("aves", "seva");
				})).subscribe(o -> {
					logger.debug("Nitrite data directory prepared");
				}, e -> {
					logger.error("Nitrite data directory preparing failed", e);
				});
	}

	@Override
	protected void dispose() {
		if (nitrite != null) {
			nitrite.close();
		}
	}

	private static Document toDocument(JsonObject jsonObject) {
		return mapperFacade.parse(removeMetaData(jsonObject).encode());
	}

	private static JsonObject toJsonObject(Document doc) {
		return removeMetaData(new JsonObject(doc));
	}

	private static JsonObject removeMetaData(JsonObject source) {
		source.remove("_id");
		source.remove("_modified");
		source.remove("_revision");
		return source;
	}

	private static Filter and(JsonObject filterValues) {
		if (filterValues == null || filterValues.isEmpty())
			return null;
		List<Filter> filters = filterValues.stream().map(e -> Filters.eq(e.getKey(), e.getValue()))
				.collect(Collectors.toList());
		return Filters.and(filters.toArray(new Filter[0]));
	}
}

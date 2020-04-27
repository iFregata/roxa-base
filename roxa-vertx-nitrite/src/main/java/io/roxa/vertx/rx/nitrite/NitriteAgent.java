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
import io.roxa.vertx.rx.EventActionEndpoint;
import io.roxa.vertx.rx.JsonAsync;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;

/**
 * @author Steven Chen
 *
 */
public class NitriteAgent {

	private final Vertx vertx;
	private final String urn;

	public static NitriteAgent create(Vertx vertx, String sourceName) {
		String urn = String.format("roxa.resource.nitrite::%s", sourceName);
		return new NitriteAgent(vertx, urn);
	}

	private NitriteAgent(Vertx vertx, String urn) {
		this.vertx = vertx;
		this.urn = urn;
	}

	public Single<Integer> upsert(String collection, JsonObject json) {
		JsonObject msg = new JsonObject().put("collection", collection).put("values", new JsonArray().add(json));
		return EventActionEndpoint.create(vertx).urn(urn).action("upsert").<Integer>request(msg);
	}

	/**
	 * 
	 * @param collection
	 * @param filter     - MUST include the primary key
	 * @param json
	 * @return
	 */
	public Single<Integer> upsert(String collection, JsonObject filter, JsonObject json) {
		JsonObject msg = new JsonObject().put("collection", collection).put("values", new JsonArray().add(json))
				.put("filter", filter);
		return EventActionEndpoint.create(vertx).urn(urn).action("upsert").<Integer>request(msg);
	}

	public Single<JsonArray> select(String collection) {
		JsonObject msg = new JsonObject().put("collection", collection);
		return EventActionEndpoint.create(vertx).urn(urn).action("select").<JsonArray>request(msg);
	}

	/**
	 * 
	 * @param collection
	 * @param filter-    MUST include the primary key
	 * @return
	 */
	public Single<JsonObject> selectFirst(String collection, JsonObject filter) {
		JsonObject msg = new JsonObject().put("collection", collection).put("filter", filter);
		return EventActionEndpoint.create(vertx).urn(urn).action("select").<JsonArray>request(msg).map(array -> {
			if (array == null || array.isEmpty())
				return JsonAsync.EMPTY_JSON;
			return array.getJsonObject(0);
		});
	}

	/**
	 * 
	 * @param collection
	 * @param filter-    MUST include the primary key
	 * @return
	 */
	public Single<JsonArray> select(String collection, JsonObject filter) {
		JsonObject msg = new JsonObject().put("collection", collection).put("filter", filter);
		return EventActionEndpoint.create(vertx).urn(urn).action("select").<JsonArray>request(msg);
	}

	public Single<JsonObject> delete(String collection) {
		JsonObject msg = new JsonObject().put("collection", collection);
		return EventActionEndpoint.create(vertx).urn(urn).action("delete").<JsonObject>request(msg);
	}

	/**
	 * 
	 * @param collection
	 * @param filter-    MUST include the primary key
	 * @return
	 */
	public Single<JsonObject> delete(String collection, JsonObject filter) {
		JsonObject msg = new JsonObject().put("collection", collection).put("filter", filter);
		return EventActionEndpoint.create(vertx).urn(urn).action("delete").<JsonObject>request(msg);
	}

}

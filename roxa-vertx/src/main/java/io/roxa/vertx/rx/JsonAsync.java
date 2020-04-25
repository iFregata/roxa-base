/**
 * The MIT License
 * 
 * Copyright (c) 2016 Shell Technologies PTY LTD
 *
 * You may obtain a copy of the License at
 * 
 *       http://mit-license.org/
 *       
 */
package io.roxa.vertx.rx;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.JavaType;

import io.reactivex.Single;
import io.roxa.GeneralFailureException;
import io.roxa.util.Jsons;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * @author steven
 *
 */
public class JsonAsync extends Jsons {

	public static final JsonObject EMPTY_JSON = new JsonObject();
	public static final JsonArray EMPTY_ARRAY = new JsonArray();

	public static Single<String> jsonAsyncOpt(Optional<?> opt) {
		try {
			if (opt.isPresent())
				return Single.just(JsonAsync.getMapper().writeValueAsString(opt.get()));
			else
				return Single.error(new GeneralFailureException("No value can be json"));
		} catch (Throwable e) {
			return Single.error(e);
		}
	}

	public static Single<String> jsonAsync(Object value) {
		try {
			return Single.just(JsonAsync.getMapper().writeValueAsString(value));
		} catch (Throwable e) {
			return Single.error(e);
		}
	}

	public static <T> Single<List<T>> objectsAsync(String json, Class<T> clazz) {
		try {
			if (json == null)
				return Single.error(new GeneralFailureException("The json string is null!"));
			String _json = json.trim();
			if ("".equals(_json))
				return Single.error(new GeneralFailureException("The json string is empty!"));
			if (_json.startsWith("{") || "[]".equals(_json))
				return Single.error(new GeneralFailureException("The json string is empty: " + _json));
			JavaType type = JsonAsync.getMapper().getTypeFactory().constructCollectionType(List.class, clazz);
			return Single.just(JsonAsync.getMapper().readValue(json, type));
		} catch (Throwable e) {
			return Single.error(e);
		}
	}

	public static <T> Single<T> objectAsync(String json, Class<T> clazz) {
		try {
			if (json == null)
				return Single.error(new GeneralFailureException("The json string is null!"));
			String _json = json.trim();
			if ("".equals(_json))
				return Single.error(new GeneralFailureException("The json string is empty!"));
			if ("{}".equals(_json) || "[]".equals(_json))
				return Single.error(new GeneralFailureException("The json string is empty: " + _json));
			return Single.just(JsonAsync.getMapper().readValue(json, clazz));
		} catch (Throwable e) {
			return Single.error(e);
		}
	}
}

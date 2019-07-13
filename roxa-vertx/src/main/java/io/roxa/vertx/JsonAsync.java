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
package io.roxa.vertx;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.JavaType;

import io.roxa.GeneralFailureException;
import io.roxa.util.Jsons;
import io.vertx.core.Future;

/**
 * @author steven
 *
 */
public class JsonAsync extends Jsons {

	public static Future<String> jsonAsyncOpt(Optional<?> opt) {
		try {
			if (opt.isPresent())
				return Future.succeededFuture(JsonAsync.getMapper().writeValueAsString(opt.get()));
			else
				return Future.failedFuture(new GeneralFailureException("No value can be json"));
		} catch (Throwable e) {
			return Future.failedFuture(e);
		}
	}

	public static Future<String> jsonAsync(Object value) {
		try {
			return Future.succeededFuture(JsonAsync.getMapper().writeValueAsString(value));
		} catch (Throwable e) {
			return Future.failedFuture(e);
		}
	}

	public static <T> Future<List<T>> objectsAsync(String json, Class<T> clazz) {
		try {
			if (json == null)
				return Future.failedFuture(new GeneralFailureException("The json string is null!"));
			String _json = json.trim();
			if ("".equals(_json))
				return Future.failedFuture(new GeneralFailureException("The json string is empty!"));
			if (_json.startsWith("{") || "[]".equals(_json))
				return Future.failedFuture(new GeneralFailureException("The json string is empty: " + _json));
			JavaType type = JsonAsync.getMapper().getTypeFactory().constructCollectionType(List.class, clazz);
			return Future.succeededFuture(JsonAsync.getMapper().readValue(json, type));
		} catch (Throwable e) {
			return Future.failedFuture(e);
		}
	}

	public static <T> Future<T> objectAsync(String json, Class<T> clazz) {
		try {
			if (json == null)
				return Future.failedFuture(new GeneralFailureException("The json string is null!"));
			String _json = json.trim();
			if ("".equals(_json))
				return Future.failedFuture(new GeneralFailureException("The json string is empty!"));
			if ("{}".equals(_json) || "[]".equals(_json))
				return Future.failedFuture(new GeneralFailureException("The json string is empty: " + _json));
			return Future.succeededFuture(JsonAsync.getMapper().readValue(json, clazz));
		} catch (Throwable e) {
			return Future.failedFuture(e);
		}
	}
}

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
package io.roxa.util;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

import io.roxa.GeneralSeriousException;

/**
 * @author steven
 *
 */
public class Jsons {

	private static class ObjectMapperInitializer {
		private static final ObjectMapper instance;
		static {
			instance = new ObjectMapper();
			instance.setSerializationInclusion(Include.NON_NULL);
			instance.setSerializationInclusion(Include.NON_EMPTY);
			instance.setVisibility(instance.getVisibilityChecker().with(JsonAutoDetect.Visibility.NONE));
			instance.setVisibility(instance.getVisibilityChecker().withFieldVisibility(JsonAutoDetect.Visibility.ANY));
			instance.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
			instance.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
			instance.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
		}
	}

	public static ObjectMapper getMapper() {
		return ObjectMapperInitializer.instance;
	}

	public static String json(Object value) {
		try {
			return Jsons.getMapper().writeValueAsString(value);
		} catch (Throwable e) {
			throw new GeneralSeriousException(e);
		}
	}

	public static <T> List<T> objects(String json, Class<T> clazz) {
		try {
			if (json == null)
				throw new GeneralSeriousException("The json string is null!");
			String _json = json.trim();
			if ("".equals(_json))
				throw new GeneralSeriousException("The json string is null!");
			if (_json.startsWith("{") || "[]".equals(_json))
				throw new GeneralSeriousException("The json string is empty: " + _json);
			JavaType type = Jsons.getMapper().getTypeFactory().constructCollectionType(List.class, clazz);
			return Jsons.getMapper().readValue(json, type);
		} catch (Throwable e) {
			throw new GeneralSeriousException(e);
		}
	}

	public static <T> T object(String json, Class<T> clazz) {
		try {
			if (json == null)
				throw new GeneralSeriousException("The json string is null!");
			String _json = json.trim();
			if ("".equals(_json))
				throw new GeneralSeriousException("The json string is null!");
			if ("{}".equals(_json) || "[]".equals(_json))
				throw new GeneralSeriousException("The json string is empty: " + _json);
			return Jsons.getMapper().readValue(json, clazz);
		} catch (Throwable e) {
			throw new GeneralSeriousException(e);
		}
	}

}

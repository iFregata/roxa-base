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

import com.ctc.wstx.stax.WstxInputFactory;
import com.ctc.wstx.stax.WstxOutputFactory;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import io.vertx.core.Future;

/**
 * @author steven
 *
 */
public abstract class Xmls {

	private static class ObjectMapperInitializer {
		private static final ObjectMapper instance;
		static {
			XmlFactory factory = new XmlFactory(new WstxInputFactory(), new WstxOutputFactory());
			instance = new XmlMapper(factory);
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

	public static Future<String> xmlAsync(Object object) {
		try {
			return Future.succeededFuture(getMapper().writeValueAsString(object));
		} catch (Exception e) {
			return Future.failedFuture(e);
		}
	}

	public static String xml(Object object) {
		try {
			return getMapper().writeValueAsString(object);
		} catch (JsonProcessingException e) {
			return null;
		}
	}

	public static <T> Future<T> object(String xmlContent, Class<T> clazz) {
		try {
			return Future.succeededFuture(getMapper().readValue(xmlContent, clazz));
		} catch (Exception e) {
			return Future.failedFuture(e);
		}
	}

	public static <T> T objectAsync(String xmlContent, Class<T> clazz) {
		try {
			return getMapper().readValue(xmlContent, clazz);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
}

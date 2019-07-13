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
package io.roxa.vertx.http;

import java.io.File;
import java.net.URI;
import java.util.List;

import io.roxa.http.HttpAgentBuilder;
import io.roxa.http.HttpForm;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * <p>
 * The HTTP client makes easy to do HTTP request/response interactions with a
 * web server.
 * </p>
 * <p>
 * Basic usage:
 * </p>
 * <code>
 * AsyncHttpAgent.newBuilder(vertx).scheme("https").host("www.somehost.com")
 * 	.path("api/path")
 *  .queryParam("paramName", paramValue)
 *  .build().get()
 * </code>
 * 
 * @author steven
 *
 */
public interface AsyncHttpAgent {

	/**
	 * 
	 * @return
	 */
	URI getURI();

	/**
	 * Send a simple HTTP GET request
	 * 
	 * @return
	 */
	Future<JsonObject> get();

	/**
	 * Send a HTTP POST request with payload, the payload could be HTTPForm, File,
	 * HttpXmlBody, JsonObject and POJO. If the payload is a POJO, which will be
	 * mapped to JSON by using Jackson Json mapper
	 * 
	 * @param payload
	 * @return
	 */
	Future<JsonObject> post(Object payload);

	/**
	 * Send a HTTP PUT request with payload, the payload could be HTTPForm, File,
	 * HttpXmlBody, JsonObject and POJO. If the payload is a POJO, which will be
	 * mapped to JSON by using Jackson Json mapper
	 * 
	 * @param payload
	 * @return
	 */
	Future<JsonObject> put(Object payload);

	/**
	 * Send a HTTP PATCH request with payload, the payload could be HTTPForm, File,
	 * HttpXmlBody, JsonObject and POJO. If the payload is a POJO, which will be
	 * mapped to JSON by using Jackson Json mapper
	 * 
	 * @param payload
	 * @return
	 */
	Future<JsonObject> patch(Object payload);

	/**
	 * Send a HTTP POST request with Multipart Form
	 * 
	 * @param file    - the file will be uploaded
	 * @param httForm - a form will be sent
	 * @return
	 */
	Future<JsonObject> postMultipart(File file, HttpForm httForm);

	/**
	 * Send a HTTP GET request with a JSON body response. The JSON body response
	 * will be mapped to POJO class by using Jackson object mapper
	 * 
	 * @param resultClass
	 * @return
	 */
	<T> Future<T> get(Class<T> resultClass);

	/**
	 * Send a HTTP GET request with a JSON body response. The JSON body response
	 * will be mapped to POJO class by using Jackson object mapper
	 * 
	 * @param resultClass
	 * @return
	 */
	<T> Future<List<T>> get2(Class<T> resultClass);

	/**
	 * Send a HTTP POST request with payload, the payload could be HTTPForm, File,
	 * HttpXmlBody, JsonObject and POJO. If the payload is a POJO, which will be
	 * mapped to JSON by using Jackson Json mapper. The JSON body response also be
	 * handled by Jackson object mapper
	 * 
	 * @param payload
	 * @param resultClass
	 * @return
	 */
	<T> Future<T> post(Object payload, Class<T> resultClass);

	/**
	 * Send a HTTP PUT request with payload, the payload could be HTTPForm, File,
	 * HttpXmlBody, JsonObject and POJO. If the payload is a POJO, which will be
	 * mapped to JSON by using Jackson Json mapper. The JSON body response also be
	 * handled by Jackson object mapper
	 * 
	 * @param payload
	 * @param resultClass
	 * @return
	 */
	<T> Future<T> put(Object payload, Class<T> resultClass);

	/**
	 * Send a HTTP PATCH request with payload, the payload could be HTTPForm, File,
	 * HttpXmlBody, JsonObject and POJO. If the payload is a POJO, which will be
	 * mapped to JSON by using Jackson Json mapper. The JSON body response also be
	 * handled by Jackson object mapper
	 * 
	 * @param payload
	 * @param resultClass
	 * @return
	 */
	<T> Future<T> patch(Object payload, Class<T> resultClass);

	/**
	 * Send a HTTP POST request with payload, the payload could be HTTPForm, File,
	 * HttpXmlBody, JsonObject and POJO. If the payload is a POJO, which will be
	 * mapped to JSON by using Jackson Json mapper. The JSON body response also be
	 * handled by Jackson object mapper. The result is POJO collection.
	 * 
	 * @param payload
	 * @param resultClass
	 * @return
	 */
	<T> Future<List<T>> post2(Object payload, Class<T> itemClass);

	/**
	 * Send a HTTP POST request with Multipart Form. The JSON body response also be
	 * handled by Jackson object mapper.
	 * 
	 * @param file
	 * @param httForm
	 * @param resultClass
	 * @return
	 */
	<T> Future<T> postMultipart(File file, HttpForm httForm, Class<T> resultClass);

	/**
	 * The factory method for HttpAgentBuilder
	 * 
	 * @param vertx
	 * @return
	 */
	static HttpAgentBuilder<AsyncHttpAgent> newBuilder(Vertx vertx) {
		return new AsyncHttpAgentOkHttp.HttpAgentOkHttpAsyncBuilder(vertx);
	}

}

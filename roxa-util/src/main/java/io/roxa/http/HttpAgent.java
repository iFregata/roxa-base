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
package io.roxa.http;

import java.io.File;
import java.net.URI;
import java.util.List;

import javax.json.JsonObject;

import io.roxa.Result;

/**
 * @author steven
 *
 */
public interface HttpAgent {

	URI getURI();

	Result<JsonObject> get();

	Result<JsonObject> post(Object payload);

	Result<JsonObject> put(Object payload);

	Result<JsonObject> patch(Object payload);

	Result<JsonObject> postMultipart(File file, HttpForm httForm);

	<T> Result<T> get(Class<T> resultClass);

	<T> Result<List<T>> get2(Class<T> resultClass);

	<T> Result<T> post(Object payload, Class<T> resultClass);

	<T> Result<T> put(Object payload, Class<T> resultClass);

	<T> Result<T> patch(Object payload, Class<T> resultClass);

	<T> Result<List<T>> post2(Object payload, Class<T> itemClass);

	<T> Result<T> postMultipart(File file, HttpForm httForm, Class<T> resultClass);

	static HttpAgentBuilder<HttpAgent> newBuilder() {
		return new HttpAgentOkHttp.HttpAgentOkHttpBuilder();
	}

}

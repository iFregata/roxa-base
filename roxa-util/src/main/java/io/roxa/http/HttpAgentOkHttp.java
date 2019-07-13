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
package io.roxa.http;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import javax.json.JsonObject;

import io.roxa.Result;
import okhttp3.HttpUrl;

/**
 * @author Steven Chen
 *
 */
public class HttpAgentOkHttp extends HttpAgentAbstract implements HttpAgent {

	public static class HttpAgentOkHttpBuilder extends HttpAgentAbstract.HttpAgentBuilderAbstract<HttpAgent> {

		@Override
		public HttpAgent build() {
			HttpAgentOkHttp agent = new HttpAgentOkHttp();
			agent.connector = OkHttpClients.sharedClient();
			if (uri != null)
				agent.url = HttpUrl.get(uri).url();
			else
				agent.url = urlBuilder.build().url();
			agent.headerParams = new HashMap<>(this.headers);
			return agent;
		}

	}

	private HttpAgentOkHttp() {
	}

	@Override
	public Result<JsonObject> get() {
		return get(after(JsonObject.class));

	}

	@Override
	public <T> Result<T> get(Class<T> resultClass) {
		return get(after(resultClass));

	}

	@Override
	public <T> Result<List<T>> get2(Class<T> resultClass) {
		return get(afterList(resultClass));

	}

	@Override
	public <T> Result<T> post(Object payload, Class<T> resultClass) {
		return post(payload, null, after(resultClass));
	}

	@Override
	public <T> Result<List<T>> post2(Object payload, Class<T> resultClass) {
		return post(payload, null, afterList(resultClass));
	}

	@Override
	public Result<JsonObject> post(Object payload) {
		return post(payload, null, after(JsonObject.class));
	}

	@Override
	public <T> Result<T> put(Object payload, Class<T> resultClass) {
		return put(payload, null, after(resultClass));
	}

	@Override
	public <T> Result<T> patch(Object payload, Class<T> resultClass) {
		return patch(payload, null, after(resultClass));
	}

	@Override
	public Result<JsonObject> put(Object payload) {
		return put(payload, null, after(JsonObject.class));
	}

	@Override
	public Result<JsonObject> patch(Object payload) {
		return patch(payload, null, after(JsonObject.class));
	}

	@Override
	public <T> Result<T> postMultipart(File file, HttpForm httForm, Class<T> resultClass) {
		return post(file, httForm, after(resultClass));
	}

	@Override
	public Result<JsonObject> postMultipart(File file, HttpForm httForm) {
		return post(file, httForm, after(JsonObject.class));
	}

}

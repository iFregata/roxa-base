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
package io.roxa.vertx.http;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.roxa.Result;
import io.roxa.http.HttpAgentAbstract;
import io.roxa.http.HttpForm;
import io.roxa.http.OkHttpClients;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import okhttp3.HttpUrl;

/**
 * @author Steven Chen
 *
 */
public class AsyncHttpAgentOkHttp extends AsyncHttpAgentAbstract implements AsyncHttpAgent {

	private static final Logger logger = LoggerFactory.getLogger(AsyncHttpAgentOkHttp.class);

	public static class HttpAgentOkHttpAsyncBuilder extends HttpAgentAbstract.HttpAgentBuilderAbstract<AsyncHttpAgent> {

		private Vertx vertx;

		public HttpAgentOkHttpAsyncBuilder(Vertx vertx) {
			this.vertx = vertx;
		}

		@Override
		public AsyncHttpAgent build() {
			AsyncHttpAgentOkHttp agent = new AsyncHttpAgentOkHttp();
			agent.connector = OkHttpClients.sharedClient();
			if (uri != null)
				agent.url = HttpUrl.get(uri).url();
			else
				agent.url = urlBuilder.build().url();
			agent.headerParams = new HashMap<>(this.headers);
			agent.vertx = vertx;
			return agent;
		}

	}

	private Vertx vertx;

	/**
	 * 
	 */
	private AsyncHttpAgentOkHttp() {
	}

	@Override
	public Future<JsonObject> get() {
		return executeBlocking(() -> get(after(JsonObject.class)));
	}

	@Override
	public <T> Future<T> get(Class<T> resultClass) {
		return executeBlocking(() -> get(after(resultClass)));

	}

	@Override
	public <T> Future<List<T>> get2(Class<T> resultClass) {
		return executeBlocking(() -> get(afterList(resultClass)));

	}

	@Override
	public <T> Future<T> post(Object payload, Class<T> resultClass) {
		return executeBlocking(() -> post(payload, null, after(resultClass)));
	}

	@Override
	public <T> Future<T> put(Object payload, Class<T> resultClass) {
		return executeBlocking(() -> put(payload, null, after(resultClass)));
	}

	@Override
	public <T> Future<T> patch(Object payload, Class<T> resultClass) {
		return executeBlocking(() -> put(payload, null, after(resultClass)));
	}

	@Override
	public <T> Future<List<T>> post2(Object payload, Class<T> resultClass) {
		return executeBlocking(() -> post(payload, null, afterList(resultClass)));
	}

	@Override
	public Future<JsonObject> post(Object payload) {
		return executeBlocking(() -> post(payload, null, after(JsonObject.class)));
	}

	@Override
	public Future<JsonObject> put(Object payload) {
		return executeBlocking(() -> put(payload, null, after(JsonObject.class)));
	}

	@Override
	public Future<JsonObject> patch(Object payload) {
		return executeBlocking(() -> patch(payload, null, after(JsonObject.class)));
	}

	@Override
	public <T> Future<T> postMultipart(File file, HttpForm httForm, Class<T> resultClass) {
		return executeBlocking(() -> post(file, httForm, after(resultClass)));
	}

	@Override
	public Future<JsonObject> postMultipart(File file, HttpForm httForm) {
		return executeBlocking(() -> post(file, httForm, after(JsonObject.class)));
	}

	protected <R> Future<R> executeBlocking(Supplier<Result<R>> fn) {
		Objects.requireNonNull(vertx);
		Promise<R> pendingPromise = Promise.promise();
		Future<R> f = pendingPromise.future();
		vertx.executeBlocking(promise -> {
			logger.info("Execute blocking to send the Http request...");
			Result<R> syncResult = fn.get();
			if (syncResult.succeeded())
				promise.complete(syncResult.result());
			else
				promise.fail(syncResult.cause());

		}, f);
		return f;
	}
}

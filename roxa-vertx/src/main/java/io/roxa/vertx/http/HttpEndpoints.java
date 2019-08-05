/**
 * The MIT License
 * 
 * Copyright (c) 2018-2020 Shell Technologies PTY LTD
 *
 * You may obtain a copy of the License at
 * 
 *       http://mit-license.org/
 *       
 */
package io.roxa.vertx.http;

import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.roxa.GeneralFailureException;
import io.roxa.fn.Tuple2;
import io.roxa.util.Strings;
import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.types.HttpEndpoint;

/**
 * @author Steven Chen
 *
 */
public final class HttpEndpoints {

	private static final Logger logger = LoggerFactory.getLogger(HttpEndpoints.class);

	private static final JsonArray EMPTY_JSON_ARRAY = new JsonArray();

	private static final JsonObject EMPTY_JSON_OBJECT = new JsonObject();

	private Vertx vertx;
	private ServiceDiscovery discovery;
	private CircuitBreaker circuitBreaker;

	private JsonObject options;

	private WebClient webClient;

	public static HttpEndpoints create(Vertx vertx) {
		return new HttpEndpoints(vertx);
	}

	private HttpEndpoints(Vertx vertx) {
		this.vertx = vertx;
		this.options = new JsonObject();
	}

	public HttpEndpoints discovery(String endpointName) {
		options.put("endpointName", endpointName);
		options.remove("endpoint_info");
		return this;
	}

	public HttpEndpoints discovery(String host, int port) {
		return discovery(host, port, false);
	}

	public HttpEndpoints discovery(String host, int port, boolean tls) {
		JsonObject endpointInfo = new JsonObject().put("host", host).put("port", port).put("tls", tls);
		options.put("endpoint_info", endpointInfo);
		options.remove("endpointName");
		return this;
	}

	public HttpEndpoints uri(String uri) {
		options.put("uri", uri);
		return this;
	}

	public HttpEndpoints pathParams(JsonObject pathParams) {
		options.put("pathParams", pathParams);
		return this;
	}

	public HttpEndpoints queryParams(JsonObject queryParams) {
		options.put("queryParams", queryParams);
		return this;
	}

	public HttpEndpoints headers(JsonObject headers) {
		options.put("headers", headers);
		return this;
	}

	public HttpEndpoints circuit() {
		return circuit(2000, 5000, 3, 1);
	}

	public HttpEndpoints circuit(long timeout, long resetTimeout, int maxFailures, int maxRetries) {
		Objects.requireNonNull(vertx);
		logger.info("Setup the roxa.vertx::HttpEndpoint.CircuitBreaker");
		if (circuitBreaker == null)
			circuitBreaker = CircuitBreaker.create("roxa.vertx::HttpEndpoint.CircuitBreaker", vertx,
					new CircuitBreakerOptions().setMaxFailures(maxFailures).setMaxRetries(maxRetries)
							.setTimeout(timeout).setResetTimeout(resetTimeout))
					.openHandler(v -> {
						logger.warn("roxa.vertx::HttpEndpoint.CircuitBreaker on open state!");
					}).fallback(v -> {
						return new JsonObject().put("sc", "500").put("st", "ciruit opened");
					});
		return this;
	}

	public Future<JsonObject> get() {
		return get(null);
	}

	public Future<JsonObject> get(JsonObject payload) {
		return request("get", payload);
	}

	public Future<JsonObject> post() {
		return post(null);
	}

	public Future<JsonObject> post(JsonObject payload) {
		return request("post", payload);
	}

	public Future<JsonObject> patch() {
		return post(null);
	}

	public Future<JsonObject> patch(JsonObject payload) {
		return request("patch", payload);
	}

	public Future<JsonObject> put() {
		return put(null);
	}

	public Future<JsonObject> put(JsonObject payload) {
		return request("put", payload);
	}

	public Future<JsonObject> delete() {
		return delete(null);
	}

	public Future<JsonObject> delete(JsonObject payload) {
		return request("delete", payload);
	}

	public Future<JsonObject> unwrapSafeObject(JsonObject ar) {
		Promise<JsonObject> promise = Promise.promise();
		Integer sc = ar.getInteger("sc", -1);
		if (sc == 200) {
			JsonObject payload = ar.getJsonObject("payload", null);
			if (payload != null) {
				promise.complete(payload);
			} else {
				promise.complete(EMPTY_JSON_OBJECT);
			}
		} else {
			promise.complete(EMPTY_JSON_OBJECT);
		}
		return promise.future();
	}

	public Future<JsonArray> unwrapSafeArray(JsonObject ar) {
		Promise<JsonArray> promise = Promise.promise();
		Integer sc = ar.getInteger("sc", -1);
		if (sc == 200) {
			JsonArray payload = ar.getJsonArray("payload", null);
			if (payload != null) {
				promise.complete(payload);
			} else {
				promise.complete(EMPTY_JSON_ARRAY);
			}
		} else {
			promise.complete(EMPTY_JSON_ARRAY);
		}
		return promise.future();
	}

	public Future<JsonObject> unwrapObject(JsonObject ar) {
		Promise<JsonObject> promise = Promise.promise();
		Integer sc = ar.getInteger("sc", -1);
		if (sc == 200) {
			JsonObject payload = ar.getJsonObject("payload", null);
			if (payload != null) {
				promise.complete(payload);
			} else {
				promise.complete(EMPTY_JSON_OBJECT);
			}
		} else {
			promise.fail(new GeneralFailureException(sc, ar.getString("st")));
		}
		return promise.future();
	}

	public Future<JsonArray> unwrapArray(JsonObject ar) {
		Promise<JsonArray> promise = Promise.promise();
		Integer sc = ar.getInteger("sc", -1);
		if (sc == 200) {
			JsonArray payload = ar.getJsonArray("payload", null);
			if (payload != null) {
				promise.complete(payload);
			} else {
				promise.complete(EMPTY_JSON_ARRAY);
			}
		} else {
			promise.fail(new GeneralFailureException(sc, ar.getString("st")));
		}
		return promise.future();
	}

	private Future<JsonObject> request(String httpMethod, JsonObject payload) {
		String uri = composeUriWithPathParams();
		JsonObject queryParams = getQueryParams();
		JsonObject headers = getHeaders();
		String endpointName = getEndpointName();
		JsonObject endpointInfo = getEndpointInfo();
		logger.debug("Prepare to {} HTTP Endpoint[{}] with url: {}, queryParams: {}, headers: {}, payload: {}",
				httpMethod.toUpperCase(), endpointName == null ? endpointInfo.encode() : endpointName, uri,
				queryParams == null ? "NIL" : queryParams.encode(), headers == null ? "NIL" : headers.encode(),
				payload == null ? "NIL" : payload.encode());
		if (circuitBreaker != null)
			return circuitBreaker.execute(promise -> {
				logger.debug("{} running with CiruitBreaker", httpMethod.toUpperCase());
				getEndpoint().compose(client -> {
					Promise<JsonObject> promiseInternal = Promise.promise();
					HttpRequest<Buffer> request = switchHttpMethod(client, httpMethod, uri);
					bindQueryParam(queryParams, request);
					bindHeader(headers, request);
					if (payload != null && !payload.isEmpty())
						request.as(BodyCodec.jsonObject()).sendJsonObject(payload, responseHandler(promiseInternal));
					else
						request.as(BodyCodec.jsonObject()).send(responseHandler(promiseInternal));
					return promiseInternal.future().map(completeHandler(client));
				}).setHandler(promise.future());
			});
		return getEndpoint().compose(client -> {
			Promise<JsonObject> promise = Promise.promise();
			HttpRequest<Buffer> request = switchHttpMethod(client, httpMethod, uri);
			bindQueryParam(queryParams, request);
			bindHeader(headers, request);
			if (payload != null && !payload.isEmpty())
				request.as(BodyCodec.jsonObject()).sendJsonObject(payload, responseHandler(promise));
			else
				request.as(BodyCodec.jsonObject()).send(responseHandler(promise));
			return promise.future().map(completeHandler(client));
		});
	}

	private JsonObject getHeaders() {
		return options.getJsonObject("headers");
	}

	private String composeUriWithPathParams() {
		String uriPattern = options.getString("uri");
		JsonObject pathParams = options.getJsonObject("pathParams");
		if (pathParams == null || pathParams.isEmpty())
			return uriPattern;
		String[] pathPart = uriPattern.split("/");
		return Stream.of(pathPart).map(p -> {
			if (!p.startsWith(":"))
				return p;
			String key = p.substring(1, p.length());
			return pathParams.getString(key, "null");
		}).collect(Collectors.joining("/"));
	}

	private String getEndpointName() {
		return options.getString("endpointName");
	}

	private JsonObject getEndpointInfo() {
		return options.getJsonObject("endpoint_info");
	}

	private JsonObject getQueryParams() {
		return options.getJsonObject("queryParams");
	}

	private Future<WebClient> getEndpoint() {
		String endpointName = getEndpointName();
		if (endpointName != null)
			return getServiceDiscovery().compose(d -> {
				Promise<WebClient> promise = Promise.promise();
				logger.debug("Discovery the HTTPEndpoint client name: {}", endpointName);
				HttpEndpoint.getWebClient(discovery, new JsonObject().put("name", endpointName), promise.future());
				return promise.future();
			});
		JsonObject endpointInfo = getEndpointInfo();
		if (endpointInfo == null || endpointInfo.isEmpty())
			return Future.failedFuture(new GeneralFailureException("No http endpoint info found!"));
		if (webClient == null) {
			webClient = WebClient.create(vertx, new WebClientOptions().setDefaultHost(endpointInfo.getString("host"))
					.setDefaultPort(endpointInfo.getInteger("port")).setSsl(endpointInfo.getBoolean("tls")));
		}
		return Future.succeededFuture(webClient);

	}

	private Function<JsonObject, JsonObject> completeHandler(WebClient client) {
		return r -> {
			ServiceDiscovery.releaseServiceObject(discovery, client);
			logger.debug("Release the HTTPEndpoint service object");
			clear();
			return r;
		};
	}

	/**
	 * 
	 */
	private void clear() {
		options.remove("pathParams");
		options.remove("queryParams");
		options.remove("headers");
	}

	private Future<ServiceDiscovery> getServiceDiscovery() {
		if (discovery == null)
			discovery = ServiceDiscovery.create(vertx);
		return Future.succeededFuture(discovery);
	}

	private static void bindQueryParam(JsonObject queryParams, HttpRequest<Buffer> request) {
		if (queryParams != null && !queryParams.isEmpty())
			queryParams.stream().map(e -> {
				String v = String.valueOf(e.getValue());
				v = Strings.emptyAsNull(v);
				if (v != null && !"null".equals(v))
					return new Tuple2<String, String>(e.getKey(), v);
				return null;
			}).filter(i -> i != null).forEach(t -> request.addQueryParam(t.$0(), t.$1()));
	}

	private static void bindHeader(JsonObject headers, HttpRequest<Buffer> request) {
		if (headers != null && !headers.isEmpty())
			headers.stream().map(e -> {
				String v = String.valueOf(e.getValue());
				v = Strings.emptyAsNull(v);
				if (v != null && !"null".equals(v))
					return new Tuple2<String, String>(e.getKey(), v);
				return null;
			}).filter(i -> i != null).forEach(t -> request.putHeader(t.$0(), t.$1()));
	}

	private static HttpRequest<Buffer> switchHttpMethod(WebClient client, String httpMethod, String uri) {
		switch (httpMethod) {
		case "get":
			return client.get(uri);
		case "post":
			return client.post(uri);
		case "put":
			return client.put(uri);
		case "delete":
			return client.delete(uri);
		case "patch":
			return client.patch(uri);
		default:
			throw new IllegalStateException("Illegal http method: " + httpMethod);
		}
	}

	private static Handler<AsyncResult<HttpResponse<JsonObject>>> responseHandler(Promise<JsonObject> promise) {
		return ar -> {
			if (ar.succeeded()) {
				HttpResponse<JsonObject> response = ar.result();
				int sc = response.statusCode();
				logger.debug("Response code: {}", sc);
				if (sc == 200) {
					JsonObject body = response.body();
					logger.debug("Response body: {}", body == null ? "" : body.encode());
					promise.complete(body);
				} else {
					promise.fail(response.statusMessage());
				}
			} else {
				logger.error("Could not complete HTTP request!", ar.cause());
				promise.fail(ar.cause());
			}
		};
	}
}

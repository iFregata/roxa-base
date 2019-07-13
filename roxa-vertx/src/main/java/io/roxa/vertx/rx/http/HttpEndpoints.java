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
package io.roxa.vertx.rx.http;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reactivex.Single;
import io.roxa.GeneralFailureException;
import io.roxa.fn.Tuple2;
import io.roxa.util.Strings;
import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.reactivex.SingleHelper;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.ext.web.client.HttpRequest;
import io.vertx.reactivex.ext.web.client.HttpResponse;
import io.vertx.reactivex.ext.web.client.WebClient;
import io.vertx.reactivex.ext.web.client.predicate.ResponsePredicate;
import io.vertx.reactivex.ext.web.codec.BodyCodec;
import io.vertx.reactivex.servicediscovery.ServiceDiscovery;
import io.vertx.reactivex.servicediscovery.types.HttpEndpoint;

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

	public static HttpEndpoints create(io.vertx.core.Vertx vertx) {
		return new HttpEndpoints(vertx);
	}

	private HttpEndpoints(io.vertx.core.Vertx vertx) {
		this.vertx = Vertx.newInstance(vertx);
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
			circuitBreaker = CircuitBreaker.create("roxa.vertx::HttpEndpoint.CircuitBreaker", vertx.getDelegate(),
					new CircuitBreakerOptions().setMaxFailures(maxFailures).setMaxRetries(maxRetries)
							.setTimeout(timeout).setResetTimeout(resetTimeout))
					.openHandler(v -> {
						logger.warn("roxa.vertx::HttpEndpoint.CircuitBreaker on open state!");
					}).fallback(v -> {
						return new JsonObject().put("sc", "500").put("st", "ciruit opened");
					});
		return this;
	}

	public Single<JsonObject> get() {
		return get(null);
	}

	public Single<JsonObject> get(JsonObject payload) {
		return request("get", payload);
	}

	public Single<JsonObject> post() {
		return post(null);
	}

	public Single<JsonObject> post(JsonObject payload) {
		return request("post", payload);
	}

	public Single<JsonObject> patch() {
		return post(null);
	}

	public Single<JsonObject> patch(JsonObject payload) {
		return request("patch", payload);
	}

	public Single<JsonObject> put() {
		return put(null);
	}

	public Single<JsonObject> put(JsonObject payload) {
		return request("put", payload);
	}

	public Single<JsonObject> delete() {
		return delete(null);
	}

	public Single<JsonObject> delete(JsonObject payload) {
		return request("delete", payload);
	}

	public Single<JsonObject> unwrapSafeObject(JsonObject ar) {
		Integer sc = ar.getInteger("sc", -1);
		if (sc == 200) {
			JsonObject payload = ar.getJsonObject("payload", null);
			if (payload != null)
				return Single.just(payload);

			return Single.just(EMPTY_JSON_OBJECT);
		}
		return Single.just(EMPTY_JSON_OBJECT);
	}

	public Single<JsonArray> unwrapSafeArray(JsonObject ar) {
		Integer sc = ar.getInteger("sc", -1);
		if (sc == 200) {
			JsonArray payload = ar.getJsonArray("payload", null);
			if (payload != null)
				return Single.just(payload);

			return Single.just(EMPTY_JSON_ARRAY);
		}
		return Single.just(EMPTY_JSON_ARRAY);
	}

	public Future<JsonObject> unwrapObject(JsonObject ar) {
		Future<JsonObject> future = Future.future();
		Integer sc = ar.getInteger("sc", -1);
		if (sc == 200) {
			JsonObject payload = ar.getJsonObject("payload", null);
			if (payload != null) {
				future.complete(payload);
			} else {
				future.complete(EMPTY_JSON_OBJECT);
			}
		} else {
			future.fail(new GeneralFailureException(sc, ar.getString("st")));
		}
		return future;
	}

	public Future<JsonArray> unwrapArray(JsonObject ar) {
		Future<JsonArray> future = Future.future();
		Integer sc = ar.getInteger("sc", -1);
		if (sc == 200) {
			JsonArray payload = ar.getJsonArray("payload", null);
			if (payload != null) {
				future.complete(payload);
			} else {
				future.complete(EMPTY_JSON_ARRAY);
			}
		} else {
			future.fail(new GeneralFailureException(sc, ar.getString("st")));
		}
		return future;
	}

	private Single<JsonObject> request(String httpMethod, JsonObject payload) {
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
			return SingleHelper.toSingle(handler -> {
				circuitBreaker.executeCommand(future -> {
					logger.info("{} running with CiruitBreaker", httpMethod.toUpperCase());
					request(queryParams, headers, uri, httpMethod, payload).subscribe(SingleHelper.toObserver(future));
				}, handler);
			});
		return request(queryParams, headers, uri, httpMethod, payload);
	}

	private Single<JsonObject> request(JsonObject queryParams, JsonObject headers, String uri, String httpMethod,
			JsonObject payload) {
		return getEndpoint().flatMap(client -> {
			HttpRequest<Buffer> request = switchHttpMethod(client, httpMethod, uri);
			bindQueryParam(queryParams, request);
			bindHeader(headers, request);
			request.expect(ResponsePredicate.SC_SUCCESS).expect(ResponsePredicate.JSON);
			Single<HttpResponse<JsonObject>> requestObr = null;
			if (payload != null && !payload.isEmpty())
				requestObr = request.as(BodyCodec.jsonObject()).rxSendJsonObject(payload);
			else
				requestObr = request.as(BodyCodec.jsonObject()).rxSend();
			return requestObr.map(response -> response.body()).doOnError(e -> {
				logger.error("Could not complete HTTPEndpoint request!", e);
			}).doFinally(() -> {
				if (discovery != null && client != null) {
					ServiceDiscovery.releaseServiceObject(discovery, client);
				}
				logger.debug("Release the HTTPEndpoint service object");
				clear();
			});
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

	private Single<WebClient> getEndpoint() {
		String endpointName = getEndpointName();
		if (endpointName != null)
			return getServiceDiscovery().flatMap(d -> {
				logger.debug("Discovery the HTTPEndpoint client name: {}", endpointName);
				return HttpEndpoint.rxGetWebClient(discovery, new JsonObject().put("name", endpointName));
			});
		JsonObject endpointInfo = getEndpointInfo();
		if (endpointInfo == null || endpointInfo.isEmpty())
			return Single.error(new GeneralFailureException("No http endpoint info found!"));
		if (webClient == null) {
			webClient = WebClient.create(vertx, new WebClientOptions().setDefaultHost(endpointInfo.getString("host"))
					.setDefaultPort(endpointInfo.getInteger("port")).setSsl(endpointInfo.getBoolean("tls")));
		}
		return Single.just(webClient);

	}

	/**
	 * 
	 */
	private void clear() {
		if (options != null) {
			options.remove("pathParams");
			options.remove("queryParams");
			options.remove("headers");
		}
	}

	private Single<ServiceDiscovery> getServiceDiscovery() {
		if (discovery == null)
			discovery = ServiceDiscovery.create(vertx);
		return Single.just(discovery);
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
}

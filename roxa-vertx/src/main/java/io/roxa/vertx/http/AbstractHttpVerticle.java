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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.roxa.AuthorizeRestrictionException;
import io.roxa.GeneralFailureException;
import io.roxa.GeneralSeriousException;
import io.roxa.IllegalParametersException;
import io.roxa.fn.Tuple2;
import io.roxa.http.BadRequestException;
import io.roxa.http.ClientSideException;
import io.roxa.http.HttpStatusException;
import io.roxa.http.InternalServerErrorException;
import io.roxa.http.ServerSideException;
import io.roxa.http.ServiceUnavailableException;
import io.roxa.http.UnauthorizedException;
import io.roxa.util.Codecs;
import io.roxa.util.Strings;
import io.roxa.util.SysInfo;
import io.roxa.vertx.BaseVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.healthchecks.Status;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.ResponseContentTypeHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.sstore.ClusteredSessionStore;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.servicediscovery.ServiceDiscovery;

/**
 * <p>
 * The super class for HTTP server. That contains many convenience methods to
 * build HTTP server easily
 * </p>
 * The default context path is /, and port is 8080, that can override by method
 * setContextPath, setPort
 * 
 * @author Steven Chen
 *
 */
public abstract class AbstractHttpVerticle extends BaseVerticle {

	private static final Logger logger = LoggerFactory.getLogger(AbstractHttpVerticle.class);
	public static final String FILE_UPLOADS_LOCATION = "/var/roxa/file-uploads";
	public static final String STATIC_SERVING_LOCATION = "/assets";
	public static final String MEDIA_TYPE_APPLICATION_JSON_UTF8 = "application/json; charset=utf-8";
	public static final String MEDIA_TYPE_APPLICATION_HTML_UTF8 = "text/html; charset=utf-8";
	public static final String MEDIA_TYPE_APPLICATION_JSON = "application/json";
	protected int port = 8080;
	protected String contextPath = "/";
	protected String fileUploadsLocation = FILE_UPLOADS_LOCATION;
	protected String staticServingLocation = STATIC_SERVING_LOCATION;
	private HttpServer httpServer;
	private HealthCheckHandler hcHandler;
	private List<String> procedures;

	public AbstractHttpVerticle() {
		super();
	}

	/**
	 * 
	 * @param contextPath - The path of context
	 */
	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}

	/**
	 * 
	 * @param port - The port of host
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * @param fileUploadsLocation the fileUploadsLocation to set
	 */
	public void setFileUploadsLocation(String fileUploadsLocation) {
		this.fileUploadsLocation = fileUploadsLocation;
	}

	/**
	 * @param staticServingLocation the staticServingLocation to set
	 */
	public void setStaticServingLocation(String staticServingLocation) {
		this.staticServingLocation = staticServingLocation;
	}

	public void start(Future<Void> fut) throws Exception {
		String hostname = System.getenv("HOSTNAME");
		String serverName = getServerName();
		String serverNameOn = hostname == null ? serverName : String.format("%s on %s", serverName, hostname);
		logger.info("{} prepare to start Http service.", serverNameOn);
		setupResources().compose(v -> {
			Router router = Router.router(vertx);
			router.route().handler(BodyHandler.create().setUploadsDirectory(fileUploadsLocation));
			router.route(pathOf("/*")).handler(ResponseContentTypeHandler.create());
			procedures = new ArrayList<>();
			hcHandler = HealthCheckHandler.create(vertx);
			router.get(pathOf("/health*")).handler(hcHandler);
			router.get(pathOf("/sysinfo")).produces(MEDIA_TYPE_APPLICATION_JSON).handler(this::sysInfoHandler);
			hcHandler.register("http", hcFut -> {
				logger.debug("{} health checking", serverNameOn);
				hcFut.complete(Status.OK(new JsonObject().put("name", serverNameOn)));
			});
			StaticHandler staticHandler = StaticHandler.create("assets");
			router.route(pathOf(staticServingLocation + "/*")).handler(staticHandler);
			return Future.succeededFuture(router);
		}).compose(this::setupRouter).compose(r -> {
			Future<Void> httpServerFuture = Future.future();
			httpServer = vertx.createHttpServer();
			httpServer.requestHandler(r).listen(port, ar -> {
				if (ar.succeeded()) {
					logger.info("{} Http service started. Listen on: {}, context path: {}", serverNameOn, port,
							contextPath);
					httpServerFuture.complete();
				} else {
					logger.error("Could not start Http service", ar.cause());
					httpServerFuture.fail(ar.cause());
				}
			});
			return httpServerFuture;
		}).compose(this::setupServiceDiscovery).compose(this::setupHttpEndpoint).setHandler(fut);

	}

	/**
	 * Close the resources
	 */
	protected Future<Void> closeResources() {
		if (hcHandler != null) {
			hcHandler.unregister("http");
			procedures.stream().forEach(hcHandler::unregister);
		}
		if (httpServer != null) {
			httpServer.close();
			logger.info("{} Http service closed. port: {}, context path: {}", getServerName(), port, contextPath);
		}
		return super.closeResources();
	}

	/**
	 * Register the health check procedure
	 * 
	 * @param name
	 * @param procedure
	 */
	protected void hcProcedure(String name, Handler<Future<Status>> procedure) {
		hcHandler.register(name, procedure);
		procedures.add(name);
	}

	/**
	 * Setup the local session
	 * 
	 * @param router
	 * @param sessionMapName
	 */
	protected void setupLocalSession(Router router, String sessionMapName) {
		router.route().handler(CookieHandler.create());
		router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx, sessionMapName)));
		logger.debug("Local session enabled: {}", sessionMapName);
	}

	/**
	 * Setup the clustered session
	 * 
	 * @param router
	 * @param sessionMapName
	 */
	protected void setupClusteredSession(Router router, String sessionMapName) {
		router.route().handler(CookieHandler.create());
		router.route().handler(SessionHandler.create(ClusteredSessionStore.create(vertx, sessionMapName)));
		logger.debug("Clustered session enabled: {}", sessionMapName);
	}

	protected void setupCORS(Router router) {
		Set<HttpMethod> allowMethods = new HashSet<>();
		allowMethods.add(HttpMethod.GET);
		allowMethods.add(HttpMethod.PUT);
		allowMethods.add(HttpMethod.OPTIONS);
		allowMethods.add(HttpMethod.POST);
		allowMethods.add(HttpMethod.DELETE);
		allowMethods.add(HttpMethod.PATCH);
		Set<String> allowHeaders = new HashSet<>();
		allowHeaders.add("x-requested-with");
		allowHeaders.add("Access-Control-Allow-Origin");
		allowHeaders.add("origin");
		allowHeaders.add("Content-Type");
		allowHeaders.add("accept");
		router.route().handler(CorsHandler.create("*").allowedHeaders(allowHeaders).allowedMethods(allowMethods));
		logger.debug("Cross Origin Resource Sharing enabled");
	}

	/**
	 * Setup the HTTP endpoint by using the Service Discovery
	 * 
	 * @param discovery
	 * @return
	 */
	protected Future<Void> setupHttpEndpoint(ServiceDiscovery discovery) {
		return Future.succeededFuture();
	}

	/**
	 * Override this method to initial resource that will be using
	 * 
	 * @return
	 */
	protected Future<Void> setupResources() {
		return Future.succeededFuture();
	}

	/**
	 * Override this method to custom the route path
	 * 
	 * @param router
	 * @return
	 */
	protected Future<Router> setupRouter(Router router) {
		return Future.succeededFuture(router);

	}

	/**
	 * Prints system information
	 * 
	 * @param rc
	 */
	protected void sysInfoHandler(RoutingContext rc) {
		SysInfo sysInfo = new SysInfo().collect();
		JsonObject info = new JsonObject();
		JsonObject jvmItems = new JsonObject();
		info.put("Description", getServerName());
		sysInfo.getJvmInfo().entrySet().forEach(e -> jvmItems.put(e.getKey(), e.getValue()));
		info.put("JVM", jvmItems);
		JsonObject threadsItems = new JsonObject();
		sysInfo.getThreadsInfo().entrySet().forEach(e -> threadsItems.put(e.getKey(), e.getValue()));
		info.put("Threads", threadsItems);
		JsonObject memoryItems = new JsonObject();
		sysInfo.getMemoryInfo().entrySet().forEach(e -> memoryItems.put(e.getKey(), e.getValue()));
		info.put("Memory", memoryItems);
		JsonObject classesItems = new JsonObject();
		sysInfo.getClassesInfo().entrySet().forEach(e -> classesItems.put(e.getKey(), e.getValue()));
		info.put("Classes", classesItems);
		JsonObject osItems = new JsonObject();
		sysInfo.getOsInfo().entrySet().forEach(e -> osItems.put(e.getKey(), e.getValue()));
		info.put("OS", osItems);
		succeeded(rc, info);
	}

	/**
	 * Specified a server name to health check
	 * 
	 * @return
	 */
	abstract protected String getServerName();

	/**
	 * Retrieve the parameters from request context
	 * 
	 * @param rc
	 * @param name
	 * @return
	 */
	protected String requestParam(RoutingContext rc, String name) {
		return rc.request().getParam(name);
	}

	protected <T> Handler<AsyncResult<T>> responseJson(RoutingContext rc) {
		return ar -> {
			if (ar.succeeded()) {
				succeeded(rc, ar.result());
			} else {
				failed(rc, ar.cause());
			}
		};
	}

	protected <T, R> Handler<AsyncResult<T>> responseJson(RoutingContext rc, Function<T, R> converter) {
		return ar -> {
			if (ar.succeeded()) {
				succeeded(rc, converter.apply(ar.result()));
			} else {
				failed(rc, ar.cause());
			}
		};
	}

	protected <T> Handler<AsyncResult<T>> response(RoutingContext rc) {
		return ar -> {
			if (ar.succeeded()) {
				succeeded(rc);
			} else {
				failed(rc, ar.cause());
			}
		};
	}

	protected void succeeded(RoutingContext rc) {
		rc.response().end(buildResponse(200, "OK").encode());
	}

	protected <T> void succeeded(RoutingContext rc, T payload) {
		if (payload instanceof JsonArray || payload instanceof JsonObject)
			rc.response().end(buildResponse(200, "OK", payload).encode());
		else if (payload instanceof String)
			rc.response().end((String) payload);
		else
			rc.response().end(payload.toString());
	}

	protected void failed(RoutingContext rc, Throwable t) {
		logFailure(rc).andThen(replyFailure()).apply(t);
	}

	protected String pathOf(String path) {
		if (contextPath == null || "/".equals(contextPath)) {
			return path;
		}
		return contextPath + path;
	}

	protected void healthCheck(RoutingContext rc) {
		succeeded(rc, new JsonObject().put("server_name", getServerName()).put("status", "up"));
	}

	protected Function<RoutingContext, Void> errorPage(String baseUrl, String message) {
		return (rc) -> {
			seeOther(rc, urlOfErrorPage(baseUrl, message));
			return (Void) null;
		};
	}

	protected Function<RoutingContext, Void> warnPage(String baseUrl, String message) {
		return (rc) -> {
			seeOther(rc, urlOfWarnPage(baseUrl, message));
			return (Void) null;
		};
	}

	protected Function<RoutingContext, Void> replyFailure() {
		return (rc) -> {
			Tuple2<String, Integer> tupl2 = rc.get("failure.intent");
			rc.response().setStatusCode(200).end(buildResponse(tupl2.$1(), tupl2.$0()).encode());
			return (Void) null;
		};
	}

	protected Function<Throwable, RoutingContext> logFailure(RoutingContext rc) {
		return (e) -> {
			HttpStatusException he = null;
			if (e instanceof HttpStatusException) {
				he = (HttpStatusException) e;
			} else if (e instanceof IllegalParametersException) {
				he = new BadRequestException(e);
			} else if (e instanceof AuthorizeRestrictionException) {
				he = new UnauthorizedException(e);
			} else if (e instanceof GeneralFailureException) {
				GeneralFailureException gfe = (GeneralFailureException) e;
				he = new ClientSideException(gfe.getStatusCode() == 0 ? 400 : gfe.getStatusCode(), gfe.getMessage());
			} else if (e instanceof GeneralSeriousException) {
				he = new ServiceUnavailableException(e);
			} else if (e instanceof ReplyException) {
				ReplyException re = (ReplyException) e;
				he = new HttpStatusException(re.failureCode(), re.getMessage());
			} else {
				he = new InternalServerErrorException(e);
			}
			return logHttpStatusException(rc).apply(he);
		};
	}

	protected void seeOther(RoutingContext rc, String url) {
		logger.debug("[303] See other: {}", url);
		rc.response().setStatusCode(303).putHeader("Location", url).end();
	}

	protected void found(RoutingContext rc, String url) {
		logger.debug("[302] Found other: {}", url);
		rc.response().setStatusCode(302).putHeader("Location", url).end();
	}

	protected void movedPermanently(RoutingContext rc, String url) {
		logger.debug("[301] Moved permanently: {}", url);
		rc.response().setStatusCode(301).putHeader("Location", url).end();
	}

	protected void staticServing(RoutingContext rc, String htmlLocation) {
		logger.debug("Static serving: {}", htmlLocation);
		rc.reroute("/assets" + htmlLocation);
	}

	protected void logErrorAndStaticServing(RoutingContext rc, Throwable e, String htmlLocation) {
		if (e != null)
			logger.error(e.getMessage(), e);
		else
			logger.error("Unknown!!!");
		staticServing(rc, htmlLocation);
	}

	private Function<HttpStatusException, RoutingContext> logHttpStatusException(RoutingContext rc) {
		return (e) -> {
			Objects.requireNonNull(e);
			String st = nonNullMessage(e.getMessage());
			int sc = e.getStatusCode();
			String serverName = getServerName();
			String emsg = String.format("%s [%s] %s!", serverName == null ? "Server" : serverName, sc, st);
			Throwable cause = e.getCause();
			if (cause != null && cause instanceof RuntimeException) {
				logger.error(emsg, e);
			} else if (e instanceof ServerSideException) {
				logger.error(emsg, e);
			} else {
				logger.warn(emsg);
			}
			rc.put("failure.intent", new Tuple2<String, Integer>(st, sc));
			return rc;
		};
	}

	protected static <T> JsonObject buildResponse(int statusCode, String statusText, T payload) {
		JsonObject resp = new JsonObject().put("sc", statusCode).put("st", statusText);
		if (payload != null)
			resp.put("payload", payload);
		return resp;
	}

	protected static JsonObject buildResponse(int statusCode, String statusText) {
		JsonObject resp = new JsonObject().put("sc", statusCode).put("st", statusText);
		return resp;
	}

	protected static String nonNullMessage(String message) {
		if (message == null)
			return "Unknown";
		String _message = message.trim();
		if (_message.length() == 0)
			return "Unknown";
		return _message;
	}

	protected Future<JsonObject> checkParameters(JsonObject params) {
		if (params == null || params.isEmpty())
			return Future.failedFuture(new IllegalParametersException("Missing all parameter!"));
		return CompositeFuture.all(params.fieldNames().stream().map(k -> {
			Object objValue = params.getValue(k, null);
			if (objValue == null)
				return Future.failedFuture(new IllegalParametersException("Missing " + k + " parameter!"));
			if (objValue instanceof String) {
				String _v = Strings.emptyAsNull((String) objValue);
				if (_v == null)
					return Future.failedFuture(new IllegalParametersException("Missing " + k + " parameter!"));
				return Future.succeededFuture(new Tuple2<String, Object>(k, _v));
			}
			return Future.succeededFuture(new Tuple2<String, Object>(k, objValue));
		}).collect(Collectors.toList())).map(fs -> {
			List<Tuple2<String, Object>> list = fs.list();
			return new JsonObject(list.stream().collect(Collectors.toMap(t -> t.$0(), t -> t.$1())));
		});

	}

	protected static String resolveBaseUrl(RoutingContext rc) {
		String host = rc.request().getHeader("Host");
		String httpSchema = rc.request().getHeader("X-Http-Schema");
		Objects.requireNonNull(host);
		String _httpSchema = "on".equalsIgnoreCase(httpSchema) ? "https" : "http";
		String[] hostAndPort = host.split(":");
		if (hostAndPort.length > 1) {
			String port = hostAndPort[1];
			if (!"80".equals(port) && !"443".equals(port))
				return String.format("%s://%s", _httpSchema, host);
		}
		return String.format("%s://%s", _httpSchema, hostAndPort[0]);

	}

	protected static String urlOfWarnPage(String baseUrl, String causeText) {
		return urlOfFailurePage(baseUrl, "warn", causeText);
	}

	protected static String urlOfErrorPage(String baseUrl, String causeText) {
		return urlOfFailurePage(baseUrl, "error", causeText);
	}

	protected static String urlOfFailurePage(String baseUrl, String level, String causeText) {
		return String.format("%s/%s?cause=%s", baseUrl, "/error.html",
				Codecs.urlEncode(new JsonObject().put("level", level).put("text", causeText).encode()));
	}
}
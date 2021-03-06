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
package io.roxa.vertx.rx.http;

import java.io.File;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reactivex.Completable;
import io.reactivex.Single;
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
import io.roxa.util.Digests;
import io.roxa.util.Strings;
import io.roxa.util.SysInfo;
import io.roxa.vertx.rx.BaseVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.healthchecks.Status;
import io.vertx.reactivex.CompletableHelper;
import io.vertx.reactivex.core.http.HttpServer;
import io.vertx.reactivex.ext.healthchecks.HealthCheckHandler;
import io.vertx.reactivex.ext.web.FileUpload;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import io.vertx.reactivex.ext.web.handler.CorsHandler;
import io.vertx.reactivex.ext.web.handler.ResponseContentTypeHandler;
import io.vertx.reactivex.ext.web.handler.SessionHandler;
import io.vertx.reactivex.ext.web.handler.StaticHandler;
import io.vertx.reactivex.ext.web.sstore.ClusteredSessionStore;
import io.vertx.reactivex.ext.web.sstore.LocalSessionStore;
import io.vertx.reactivex.servicediscovery.ServiceDiscovery;

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
	private JsonObject serverConfiguration;

	public AbstractHttpVerticle() {
		super();
	}

	public AbstractHttpVerticle(JsonObject serverConfiguration) {
		this.serverConfiguration = serverConfiguration;
		setPort(serverConfiguration.getInteger("port", port));
		setContextPath(serverConfiguration.getString("context_path", contextPath));
	}

	/**
	 * @return the serverConfiguration
	 */
	public JsonObject getServerConfiguration() {
		return this.serverConfiguration;
	}

	/**
	 * @param serverConfiguration the serverConfiguration to set
	 */
	public void setServerConfiguration(JsonObject serverConfiguration) {
		this.serverConfiguration = serverConfiguration;
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

	@Override
	public void start(Promise<Void> startPromise) throws Exception {
		String hostname = System.getenv("HOSTNAME");
		String serverName = getServerName();
		String serverNameOn = hostname == null ? serverName : String.format("%s on %s", serverName, hostname);
		logger.info("{} prepare to start Http service.", serverNameOn);
		Supplier<Single<Router>> preRouter = () -> {
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
			return Single.just(router);
		};
		setupResources().andThen(preRouter.get()).flatMap(this::setupRouter).flatMap(r -> {
			return vertx.createHttpServer().requestHandler(r).rxListen(port).doOnSuccess(s -> {
				logger.info("{} Http service started. Listen on: {}, context path: {}", serverNameOn, port,
						contextPath);
			}).doOnError(e -> {
				logger.error("Could not start Http service", e);
			});
		}).flatMap(this::setupServiceDiscovery).flatMapCompletable(this::setupHttpEndpoint)
				.subscribe(CompletableHelper.toObserver(startPromise.future()));
	}

	protected Single<JsonObject> issueClientToken(RoutingContext rc, JsonObject tokenPolicy) {
		String clientId = requestParam(rc, "client_id");
		String clientKey = requestParam(rc, "client_key");
		if (clientId == null || clientKey == null)
			return Single.error(new BadRequestException("Missing parameters!"));
		return getClientRegister(clientId).flatMap(clientInfo -> {
			return WebAPIs.generateClientToken(clientInfo, clientId, clientKey, tokenPolicy);
		});
	}

	protected Single<JsonObject> issueClientToken(String clientId, String clientKey, JsonObject tokenPolicy) {
		if (clientId == null || clientKey == null)
			return Single.error(new BadRequestException("Missing parameters!"));
		return getClientRegister(clientId).flatMap(clientInfo -> {
			return WebAPIs.generateClientToken(clientInfo, clientId, clientKey, tokenPolicy);
		});
	}

	protected Single<JsonObject> verifyClientToken(RoutingContext rc) {
		String authHeader = rc.request().getHeader("Authorization");
		return WebAPIs.badClientToken(authHeader).flatMap(asJson -> {
			JsonObject signContent = asJson.getJsonObject("content");
			String signBase64 = asJson.getString("signature");
			return getClientRegister(signContent.getString("client_id")).flatMap(clientRegister -> {
				if (clientRegister == null || clientRegister.isEmpty())
					return Single.error(new UnauthorizedException("JWT client illegal!"));
				String clientKey = clientRegister.getString("client_key", null);
				if (clientKey == null)
					return Single.error(new UnauthorizedException("JWT client key illegal!"));
				if (!Digests.digestVerifyBase64PlainKeyUrlSafe(signBase64, clientKey, signContent.encode()))
					return Single.error(new UnauthorizedException("JWT signature illegal!"));
				return Single.just(clientRegister);
			});
		});
	}

	protected Single<JsonObject> authorize(RoutingContext rc, JsonObject authPolicy) {
		String method = rc.request().method().name();
		String absoluteURI = rc.request().absoluteURI();
		String uri = rc.request().uri();
		String path = rc.request().path();
		JsonObject requestInfo = new JsonObject().put("http_verb", method).put("abs_uri", absoluteURI).put("uri", uri)
				.put("path", path);
		logger.debug("Authorize, requestInfo: {}, auth policy: {}", requestInfo.encode(),
				authPolicy == null ? "NIL" : authPolicy.encode());
		if (authPolicy == null || authPolicy.isEmpty())
			return Single.just(new JsonObject());
		String mode = authPolicy.getString("mode", "base");
		if ("bypass".equals(mode)) {
			logger.debug("Authorizing bypass");
			return Single.just(new JsonObject());
		}
		String authHeader = rc.request().getHeader("Authorization");
		if ("base".equals(mode)) {
			String baseSecretCode = String.format("Base %s", getAuthBaseSecretCode());
			logger.debug("Authorizing base: {}", authHeader);
			if (baseSecretCode == null)
				return Single.just(new JsonObject());
			if (baseSecretCode.equals(authHeader))
				return Single.just(new JsonObject());
			return Single.error(new UnauthorizedException("Authorization base secret code illegal!"));
		}
		logger.debug("Authorizing bearer: {}", authHeader);
		if ("bearer".equals(mode))
			return WebAPIs.badBearerAuthorization(authHeader, requestInfo).flatMap(asJson -> {
				JsonObject signContent = asJson.getJsonObject("content");
				String clientId = signContent.getString("client_id");
				String signBase64 = asJson.getString("signature");
				return getClientRegister(clientId).flatMap(clientRegister -> {
					if (clientRegister == null || clientRegister.isEmpty())
						return Single.error(new UnauthorizedException("Authorization client illegal!"));
					String clientKey = clientRegister.getString("client_key", null);
					if (clientKey == null)
						return Single.error(new UnauthorizedException("Authorization client key illegal!"));
					if (!Digests.digestVerifyBase64PlainKeyUrlSafe(signBase64, clientKey, signContent.encode()))
						return Single.error(new UnauthorizedException("Authorization signature illegal!"));
					return Single.just(asJson.copy().put("client_register", clientRegister));
				});
			});
		return Single.error(new ServiceUnavailableException("Authorization mode illegal!"));
	}

	/**
	 * 
	 * @param clientId
	 * @return
	 */
	protected Single<JsonObject> getClientRegister(String clientId) {
		return Single.just(new JsonObject());
	}

	/**
	 * 
	 * @return
	 */
	protected String getAuthBaseSecretCode() {
		return null;
	}

	/**
	 * Close the resources
	 */
	protected Completable closeResources() {
		logger.debug("Prepare to close resources of HttpServer.");
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
	protected void hcProcedure(String name, Handler<io.vertx.reactivex.core.Promise<Status>> procedure) {
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
	protected Completable setupHttpEndpoint(ServiceDiscovery discovery) {
		logger.debug("Prepare to setup http endpoint of HttpServer.");
		return Completable.complete();
	}

	/**
	 * Override this method to initial resource that will be using
	 * 
	 * @return
	 */
	protected Completable setupResources() {
		logger.debug("Prepare to setup resource of HttpServer.");
		return Completable.complete();
	}

	/**
	 * Override this method to custom the route path
	 * 
	 * @param router
	 * @return
	 */
	protected Single<Router> setupRouter(Router router) {
		logger.debug("Prepare to setup router of HttpServer.");
		return Single.just(router);

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

	protected static String guessContentTypeFromName(String filename) {
		File file = new File(filename);
		String mimeType = URLConnection.guessContentTypeFromName(file.getName());
		if (mimeType == null)
			mimeType = "application/octet-stream";
		return mimeType;
	}

	protected static JsonObject resolveFileInfo(RoutingContext rc) {
		FileUpload lastestOne = null;
		for (FileUpload f : rc.fileUploads())
			lastestOne = f;
		if (lastestOne == null)
			return null;
		JsonObject uploadFileInfo = new JsonObject();
		String value = lastestOne.fileName();
		if (value != null)
			uploadFileInfo.put("file_name", value);
		value = lastestOne.name();
		if (value != null)
			uploadFileInfo.put("name", value);
		value = lastestOne.contentType();
		if (value != null)
			uploadFileInfo.put("content_type", value);
		value = lastestOne.uploadedFileName();
		if (value != null)
			uploadFileInfo.put("uploaded_file_name", value);
		value = lastestOne.charSet();
		if (value != null)
			uploadFileInfo.put("charset", value);
		value = lastestOne.contentTransferEncoding();
		if (value != null)
			uploadFileInfo.put("content_transfer_encoding", value);
		long size = lastestOne.size();
		uploadFileInfo.put("size", size);
		return uploadFileInfo;
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
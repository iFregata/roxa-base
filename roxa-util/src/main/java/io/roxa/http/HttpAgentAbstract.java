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
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import javax.json.Json;
import javax.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;

import io.roxa.Result;
import io.roxa.util.Jsons;
import io.roxa.util.Strings;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * @author Steven Chen
 *
 */
public abstract class HttpAgentAbstract {

	private static final Logger logger = LoggerFactory.getLogger(HttpAgentAbstract.class);

	public abstract static class HttpAgentBuilderAbstract<T> implements HttpAgentBuilder<T> {

		protected HttpUrl.Builder urlBuilder = new HttpUrl.Builder();
		protected Map<String, String> headers = new HashMap<>();
		protected URI uri;

		public HttpAgentBuilder<T> host(String host) {
			urlBuilder.host(host);
			return this;
		}

		public HttpAgentBuilder<T> scheme(String scheme) {
			urlBuilder.scheme(scheme);
			return this;
		}

		public HttpAgentBuilder<T> uri(URI uri) {
			this.uri = uri;
			return this;
		}

		public HttpAgentBuilder<T> port(Integer port) {
			urlBuilder.port(port);
			return this;
		}

		public HttpAgentBuilder<T> path(String path) {
			urlBuilder.addPathSegments(path);
			return this;
		}

		public HttpAgentBuilder<T> header(String name, String value) {
			headers.put(name, value);
			return this;
		}

		public HttpAgentBuilder<T> pathParam(String pathParam) {
			urlBuilder.addPathSegments(pathParam);
			return this;
		}

		public HttpAgentBuilder<T> queryParam(String name, String value) {
			urlBuilder.addQueryParameter(name, value);
			return this;
		}

	}

	protected static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
	protected static final MediaType MEDIA_TYPE_XML = MediaType.parse("text/xml");

	protected OkHttpClient connector;
	protected URL url;
	protected Map<String, String> headerParams;
	protected static Map<String, MediaType> mimeTypeMapping = new HashMap<>();
	static {
		mimeTypeMapping.put("png", MediaType.parse("image/png"));
		mimeTypeMapping.put("jpg", MediaType.parse("image/jpeg"));
		mimeTypeMapping.put("jpeg", MediaType.parse("image/jpeg"));
		mimeTypeMapping.put("gif", MediaType.parse("image/gif"));
	}

	public URI getURI() {
		try {
			return url.toURI();
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	protected <R> Result<R> get(Function<Result<Response>, Result<R>> after) {
		final Function<URL, Request> before = (url) -> {
			Request.Builder b = new Request.Builder().url(url);
			for (String name : headerParams.keySet()) {
				b.addHeader(name, headerParams.get(name));
			}
			return b.build();
		};
		return exec(before, after);
	}

	protected <R> Result<R> post(Object payload, HttpForm form, Function<Result<Response>, Result<R>> after) {
		final Function<URL, Request> before = (url) -> {
			Request.Builder b = new Request.Builder().url(url);
			if (payload != null) {
				RequestBody rb = requestBody(payload, form);
				b.post(rb);
			}
			for (String name : headerParams.keySet()) {
				b.addHeader(name, headerParams.get(name));
			}
			return b.build();
		};
		return exec(before, after);
	}

	protected <R> Result<R> put(Object payload, HttpForm form, Function<Result<Response>, Result<R>> after) {
		final Function<URL, Request> before = (url) -> {
			Request.Builder b = new Request.Builder().url(url);
			if (payload != null) {
				RequestBody rb = requestBody(payload, form);
				b.put(rb);
			}
			for (String name : headerParams.keySet()) {
				b.addHeader(name, headerParams.get(name));
			}
			return b.build();
		};
		return exec(before, after);
	}

	protected <R> Result<R> patch(Object payload, HttpForm form, Function<Result<Response>, Result<R>> after) {
		final Function<URL, Request> before = (url) -> {
			Request.Builder b = new Request.Builder().url(url);
			if (payload != null) {
				RequestBody rb = requestBody(payload, form);
				b.patch(rb);
			}
			for (String name : headerParams.keySet()) {
				b.addHeader(name, headerParams.get(name));
			}
			return b.build();
		};
		return exec(before, after);
	}

	protected <R> Result<R> exec(Function<URL, Request> before, Function<Result<Response>, Result<R>> after) {
		final Function<Request, Result<Response>> apply = (req) -> {
			Response resp = null;
			try {
				resp = connector.newCall(req).execute();
				checkResponse(resp);
				return HttpResult.succeeful(resp);
			} catch (Exception e) {
				if (resp != null)
					resp.close();// always close?
				return HttpResult.failure(e);
			}
		};
		return apply.compose(before).andThen(after).apply(url);
	}

	@SuppressWarnings("unchecked")
	protected <R> Function<Result<Response>, Result<R>> after(Class<R> resultClass) {
		return rs -> {
			if (rs.succeeded()) {
				try (Response resp = rs.result()) {
					ResponseBody body = resp.body();
					if (body == null) {
						logger.warn("The response body is null!");
						Result<R> trs = (Result<R>) HttpResult.succeeful((R) null);
						return trs;
					}
					if (resultClass == JsonObject.class) {
						Reader reader = body.charStream();
						if (reader == null) {
							logger.warn("The response body char stream is null!");
							Result<R> trs = (Result<R>) HttpResult.succeeful((R) null);
							return trs;
						}
						JsonObject json = Json.createReader(reader).readObject();
						Result<R> trs = (Result<R>) HttpResult.succeeful(json);
						return trs;
					} else if (resultClass == Void.class) {
						Result<R> trs = (Result<R>) HttpResult.succeeful("succee");
						return trs;
					} else if (resultClass == Reader.class) {
						String stringBody = body.string();
						stringBody = Strings.emptyAsNull(stringBody);
						if (stringBody == null) {
							logger.warn("The response string body is null!");
							Result<R> trs = (Result<R>) HttpResult.succeeful((R) null);
							return trs;
						}
						Result<R> trs = (Result<R>) HttpResult.succeeful(new StringReader(stringBody));
						return trs;
					} else {
						Reader reader = body.charStream();
						if (reader == null) {
							logger.warn("The response body char stream is null before json mapper reading value!");
							Result<R> trs = (Result<R>) HttpResult.succeeful((R) null);
							return trs;
						}
						return HttpResult.succeeful(Jsons.getMapper().readValue(reader, resultClass));
					}
				} catch (Exception e) {
					return HttpResult.failure(e);
				}
			} else {
				return HttpResult.failure(rs.cause());
			}
		};
	}

	protected boolean isApplicationJsonMimeType(MediaType mediaType) {
		if (mediaType == null)
			return false;
		String type = mediaType.type();
		if (type == null)
			return false;
		String _type = type.trim();
		if (_type == null)
			return false;
		return (_type.contains("application/json"));
	}

	protected <R> Function<Result<Response>, Result<List<R>>> afterList(Class<R> resultClass) {
		return rs -> {
			if (rs.succeeded()) {
				try (Response resp = rs.result()) {

					JavaType type = Jsons.getMapper().getTypeFactory().constructCollectionType(List.class, resultClass);
					List<R> list = Jsons.getMapper().readValue(rs.result().body().charStream(), type);
					return HttpResult.succeeful(list);
				} catch (Exception e) {
					return HttpResult.failure(e);
				}
			} else {
				return HttpResult.failure(rs.cause());
			}
		};
	}

	protected RequestBody composeMultipartBody(File payload, HttpForm form) {
		MultipartBody.Builder b = new MultipartBody.Builder().setType(MultipartBody.FORM);
		form.stream().forEach(entry -> {
			b.addFormDataPart(entry.getKey(), entry.getValue());
		});
		String[] filePartName = form.filePartName();
		if (filePartName != null)
			b.addFormDataPart(filePartName[0], filePartName[1], composeFileBody(payload, filePartName[1]));
		else
			b.addPart(composeFileBody(payload, null));
		return b.build();
	}

	protected RequestBody composeFileBody(File payload, String altFileName) {
		MediaType mimeType = guessMimeType(payload, altFileName);
		return RequestBody.create(mimeType, payload);
	}

	protected RequestBody composeFormBody(HttpForm payload) {
		FormBody.Builder b = new FormBody.Builder();
		payload.stream().forEach(entry -> {
			b.add(entry.getKey(), entry.getValue());
		});
		return b.build();

	}

	protected void checkResponse(Response resp) throws HttpStatusException {
		int sc = resp.code();
		String st = resp.message();
		if (!resp.isSuccessful()) {
			switch (sc) {
			case 500:
				throw new InternalServerErrorException(st);
			case 503:
				throw new ServiceUnavailableException(st);
			case 400:
				throw new BadRequestException(st);
			case 404:
				throw new NotFoundException(st);
			case 401:
				throw new UnauthorizedException(st);
			case 403:
				throw new ForbiddenException(st);
			case 405:
				throw new MethodNotAllowedException(st);
			}
			if (sc > 500)
				throw new ServerSideException(sc, st);
			if (sc > 400)
				throw new ClientSideException(sc, st);
		}
	}

	protected static MediaType guessMimeType(File payload, String altFileName) {
		String filename = payload.getName();
		String extName = getFilenameExtension(filename);
		if (extName == null)
			extName = getFilenameExtension(altFileName);
		if (extName == null)
			extName = "NONE";
		MediaType mimeType = mimeTypeMapping.get(extName);
		if (Objects.isNull(mimeType))
			return MediaType.parse("application/octet-stream");
		return mimeType;
	}

	protected static String getFilenameExtension(String filename) {
		if (filename == null)
			return null;
		String _filename = filename.trim();
		if (_filename.length() == 0)
			return null;
		String extName = _filename.substring(_filename.lastIndexOf(".") + 1, _filename.length());
		if (extName == null || extName.equals(_filename) || extName.length() == 0)
			return null;
		return extName;
	}

	protected RequestBody requestBody(Object payload, HttpForm form) {
		RequestBody rb = null;
		if (payload instanceof JsonObject) {
			rb = RequestBody.create(MEDIA_TYPE_JSON, ((JsonObject) payload).toString());
		} else if (payload instanceof HttpForm) {
			rb = composeFormBody((HttpForm) payload);
		} else if (payload instanceof File) {
			if (form == null)
				rb = composeFileBody((File) payload, null);
			else
				rb = composeMultipartBody((File) payload, form);
		} else if (payload instanceof HttpXmlBody) {
			rb = RequestBody.create(MEDIA_TYPE_XML, ((HttpXmlBody) payload).xml);
		} else {
			try {
				String jsonContent = Jsons.getMapper().writeValueAsString(payload);
				rb = RequestBody.create(MEDIA_TYPE_JSON, jsonContent);
			} catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}
		}
		return rb;
	}
}

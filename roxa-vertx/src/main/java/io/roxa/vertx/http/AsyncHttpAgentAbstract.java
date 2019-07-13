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

import java.io.File;
import java.io.Reader;
import java.io.StringReader;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.roxa.Result;
import io.roxa.http.HttpAgentAbstract;
import io.roxa.http.HttpForm;
import io.roxa.http.HttpResult;
import io.roxa.http.HttpXmlBody;
import io.roxa.util.Jsons;
import io.roxa.util.Strings;
import io.vertx.core.json.JsonObject;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * @author Steven Chen
 *
 */
public class AsyncHttpAgentAbstract extends HttpAgentAbstract {

	private static final Logger logger = LoggerFactory.getLogger(AsyncHttpAgentAbstract.class);

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
						String stringBody = body.string();
						stringBody = Strings.emptyAsNull(stringBody);
						if (stringBody == null) {
							logger.warn("The response string body is null!");
							Result<R> trs = (Result<R>) HttpResult.succeeful((R) null);
							return trs;
						}
						JsonObject json = new JsonObject(stringBody);
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
						return HttpResult.succeeful(Jsons.getMapper().readValue(body.charStream(), resultClass));
					}
				} catch (Exception e) {
					return HttpResult.failure(e);
				}
			} else {
				return HttpResult.failure(rs.cause());
			}
		};
	}

	protected RequestBody requestBody(Object payload, HttpForm form) {
		RequestBody rb = null;
		if (payload instanceof JsonObject) {
			rb = RequestBody.create(MEDIA_TYPE_JSON, ((JsonObject) payload).encode());
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

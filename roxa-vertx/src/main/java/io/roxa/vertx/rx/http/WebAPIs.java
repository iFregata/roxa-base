/**
 * The MIT License
 * 
 * Copyright (c) 2019-2022 Shell Technologies PTY LTD
 *
 * You may obtain a copy of the License at
 * 
 *       http://mit-license.org/
 *       
 */
package io.roxa.vertx.rx.http;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reactivex.Single;
import io.roxa.http.BadRequestException;
import io.roxa.util.Codecs;
import io.roxa.util.Digests;
import io.roxa.util.Moments;
import io.roxa.util.Randoms;
import io.roxa.util.Strings;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * @author Steven Chen
 *
 */
public abstract class WebAPIs {

	private static final Logger logger = LoggerFactory.getLogger(WebAPIs.class);

	public static final String authHeaderBearer = "Bearer";
	public static final long authTimeMillisOffset = 5 * 60 * 1000;

	public static void main(String[] args) {
		System.out.println(bearerAuthorization("abc", "abc", "GET"));
	}

	public static JsonObject generateClientRegister(String clientTitle) {
		return generateClientRegister(clientTitle, "UNCATALOG", null);
	}

	public static JsonObject generateClientRegister(String clientTitle, String clientCatalog) {
		return generateClientRegister(clientTitle, clientCatalog, null);
	}

	public static JsonObject generateClientRegister(String clientTitle, String clientCatalog, String[] roles) {
		String clientId = Digests.digestMD5(String.format("%s::%s::roxa-vertx-webapi", clientTitle, clientCatalog));
		String clientKey = Randoms.randomString(16);
		JsonObject registerInfo = new JsonObject().put("client_id", clientId).put("client_key", clientKey)
				.put("client_title", clientTitle).put("client_catalog", clientCatalog);
		if (roles != null && roles.length > 0) {
			registerInfo.put("roles", new JsonArray(Arrays.asList(roles)));
		}
		return registerInfo;
	}

	public static String bearerAuthorization(String clientId, String clientKey, String httpVerb) {
		JsonObject bearerJson = new JsonObject().put("client_id", clientId)
				.put("timestamp", Moments.currentTimeMillis()).put("nonce", Randoms.randomString(16))
				.put("verb", httpVerb);
		String signContent = bearerJson.encode();
		String signature = Digests.digestAsBase64PlainKeyUrlSafe(clientKey, signContent);
		String base64Content = Codecs.asBase64URLSafeString(signContent);
		return String.format("Bearer %s.%s", base64Content, signature);
	}

	public static Single<JsonObject> badBearerAuthorization(String authorization, JsonObject requestInfo) {
		try {
			String authHeader = Strings.emptyAsNull(authorization);
			if (authHeader == null)
				throw new BadRequestException("No authorization found");
			if (!authHeader.startsWith(authHeaderBearer))
				throw new BadRequestException("Illegal authorization");
			final String _bearerContent = authHeader.substring(authHeaderBearer.length() + 1, authHeader.length());
			final String bearerContent = Strings.emptyAsNull(_bearerContent);
			if (bearerContent == null)
				throw new BadRequestException("Illegal authorization Bearer");
			final String[] pair = bearerContent.split("\\.");
			if (pair == null || pair.length < 2)
				throw new BadRequestException("Illegal authorization Bearer pair");
			String signPart = Strings.emptyAsNull(pair[1]);
			if (signPart == null)
				throw new BadRequestException("Illegal authorization Bearer part signature");
			String jsonPart = Codecs.base64URLSafeAsString(pair[0]);
			logger.debug("Authorization json part: {},signature part: {} ", jsonPart, signPart);
			JsonObject jsonContent = new JsonObject(jsonPart);
			String clientId = Strings.emptyAsNull(jsonContent.getString("client_id", null));
			String nonce = Strings.emptyAsNull(jsonContent.getString("nonce", null));
			Long timestamp = jsonContent.getLong("timestamp", null);
			String verb = Strings.emptyAsNull(jsonContent.getString("verb", null));
			if (clientId == null || nonce == null || timestamp == null || verb == null)
				throw new BadRequestException("Illegal authorization Bearer part missing field");
			if (!verb.equalsIgnoreCase(requestInfo.getString("http_verb")))
				throw new BadRequestException("Illegal authorization http verb, expected: "
						+ requestInfo.getString("http_verb") + ", actual: " + verb);
			Long nowMillis = System.currentTimeMillis();
			Long before = nowMillis - authTimeMillisOffset;
			Long after = nowMillis + authTimeMillisOffset;
			if (timestamp > after || timestamp < before) {
				logger.warn("Request timemillis offset, server: {}, client: {}", nowMillis, timestamp);
				throw new BadRequestException("Illegal authorization timestamp");
			}
			return Single.just(new JsonObject().put("content", jsonContent).put("signature", signPart));
		} catch (Throwable e) {
			logger.warn("Authorization failed, {}", e.getMessage());
			return Single.error(e);
		}
	}

}

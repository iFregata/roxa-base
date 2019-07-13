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

import java.io.IOException;
import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author Steven Chen
 *
 */
public class OkHttpClients {

	private static final long TIMEOUT_CONNECTION = 10;
	private static final long TIMEOUT_READ = 60;

	public static void main(String[] args) {
		OkHttpClients.sharedClient();
	}

	static class LoggingInterceptor implements Interceptor {
		private static final Logger logger = LoggerFactory.getLogger(OkHttpClients.LoggingInterceptor.class);

		@Override
		public Response intercept(Chain chain) throws IOException {
			Request request = chain.request();
			logger.debug(String.format("Sending request %s%n%s", request.url(), request.headers()));
			Response response = chain.proceed(request);
			logger.debug(String.format("Received response for %s%n%s", response.request().url(), response.headers()));
			return response;
		}

	}

	private static class OkHttpClientInitializer {
		private static final OkHttpClient instance;
		static {
			try {
				final TrustAllX509ExtendedTrustManager trustManager = new TrustAllX509ExtendedTrustManager();
				final SSLContext sslContext = SSLContext.getInstance("TLSv1");
				sslContext.init(null, new TrustManager[] { trustManager }, new SecureRandom());
				SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
				HostnameVerifier hostnameVerifier = new DefaultHostnameVerifier();

				instance = new OkHttpClient.Builder().addInterceptor(new LoggingInterceptor())
						.sslSocketFactory(sslSocketFactory, trustManager).hostnameVerifier(hostnameVerifier)
						.connectTimeout(TIMEOUT_CONNECTION, TimeUnit.SECONDS)
						.readTimeout(TIMEOUT_READ, TimeUnit.SECONDS).build();
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
	}

	public static OkHttpClient sharedClient() {
		return OkHttpClientInitializer.instance;
	}

	public static OkHttpClient perCallClient(long connTimeoutInSecond, long readTimeoutInSecond) {
		return OkHttpClientInitializer.instance.newBuilder().connectTimeout(connTimeoutInSecond, TimeUnit.SECONDS)
				.readTimeout(readTimeoutInSecond, TimeUnit.SECONDS).build();
	}

}

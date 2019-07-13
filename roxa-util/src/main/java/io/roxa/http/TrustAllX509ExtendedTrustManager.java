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

import java.net.Socket;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedTrustManager;

/**
 * @author Steven Chen
 *
 */
public class TrustAllX509ExtendedTrustManager extends X509ExtendedTrustManager {

	private static final X509Certificate[] acceptedIssuers = new X509Certificate[0];

	/**
	 * 
	 */
	public TrustAllX509ExtendedTrustManager() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.net.ssl.X509TrustManager#checkClientTrusted(java.security.cert.
	 * X509Certificate[], java.lang.String)
	 */
	@Override
	public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.net.ssl.X509TrustManager#checkServerTrusted(java.security.cert.
	 * X509Certificate[], java.lang.String)
	 */
	@Override
	public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.net.ssl.X509TrustManager#getAcceptedIssuers()
	 */
	@Override
	public X509Certificate[] getAcceptedIssuers() {
		return acceptedIssuers;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.net.ssl.X509ExtendedTrustManager#checkClientTrusted(java.security.cert.
	 * X509Certificate[], java.lang.String, java.net.Socket)
	 */
	@Override
	public void checkClientTrusted(X509Certificate[] arg0, String arg1, Socket arg2) throws CertificateException {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.net.ssl.X509ExtendedTrustManager#checkClientTrusted(java.security.cert.
	 * X509Certificate[], java.lang.String, javax.net.ssl.SSLEngine)
	 */
	@Override
	public void checkClientTrusted(X509Certificate[] arg0, String arg1, SSLEngine arg2) throws CertificateException {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.net.ssl.X509ExtendedTrustManager#checkServerTrusted(java.security.cert.
	 * X509Certificate[], java.lang.String, java.net.Socket)
	 */
	@Override
	public void checkServerTrusted(X509Certificate[] arg0, String arg1, Socket arg2) throws CertificateException {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.net.ssl.X509ExtendedTrustManager#checkServerTrusted(java.security.cert.
	 * X509Certificate[], java.lang.String, javax.net.ssl.SSLEngine)
	 */
	@Override
	public void checkServerTrusted(X509Certificate[] arg0, String arg1, SSLEngine arg2) throws CertificateException {

	}

}

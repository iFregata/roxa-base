/**
 * The MIT License
 * 
 * Copyright (c) 2016 Shell Technologies PTY LTD
 *
 * You may obtain a copy of the License at
 * 
 *       http://mit-license.org/
 *       
 */
package io.roxa.http;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/**
 * @author Steven Chen
 *
 */
public class DefaultHostnameVerifier implements HostnameVerifier {

	/**
	 * 
	 */
	public DefaultHostnameVerifier() {
	}

	public boolean verify(String arg0, SSLSession arg1) {
		return true;
	}

}

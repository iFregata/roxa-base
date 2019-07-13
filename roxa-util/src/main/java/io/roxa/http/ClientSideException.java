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

/**
 * @author Steven Chen
 *
 */
public class ClientSideException extends HttpStatusException {

	private static final long serialVersionUID = -2281984817308131008L;

	public ClientSideException(int statusCode, String statusText, Throwable cause) {
		super(statusCode, statusText, cause);
	}

	public ClientSideException(int statusCode, String statusText) {
		super(statusCode, statusText);
	}

	public ClientSideException(int statusCode, Throwable cause) {
		super(statusCode, cause);
	}

}

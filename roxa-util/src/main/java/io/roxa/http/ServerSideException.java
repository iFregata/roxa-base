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
public class ServerSideException extends HttpStatusException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7530876686203816747L;

	public ServerSideException(int statusCode, String statusText, Throwable cause) {
		super(statusCode, statusText, cause);
	}

	public ServerSideException(int statusCode, String statusText) {
		super(statusCode, statusText);
	}

	public ServerSideException(int statusCode, Throwable cause) {
		super(statusCode, cause);
	}

}

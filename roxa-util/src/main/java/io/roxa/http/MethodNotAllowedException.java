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
public class MethodNotAllowedException extends ClientSideException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6879896284228990497L;

	/**
	 * 
	 */
	public MethodNotAllowedException() {
		super(405, "Method Not Allowed");
	}

	/**
	 * @param message
	 */
	public MethodNotAllowedException(String message) {
		super(405, message);
	}

	/**
	 * @param cause
	 */
	public MethodNotAllowedException(Throwable cause) {
		super(405, cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public MethodNotAllowedException(String message, Throwable cause) {
		super(405, message, cause);
	}

}

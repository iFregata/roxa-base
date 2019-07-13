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
public class BadRequestException extends ClientSideException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5830595830816132339L;

	/**
	 * 
	 */
	public BadRequestException() {
		super(400, "Bad Request");
	}

	/**
	 * @param message
	 */
	public BadRequestException(String message) {
		super(400, message);
	}

	/**
	 * @param cause
	 */
	public BadRequestException(Throwable cause) {
		super(400, cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public BadRequestException(String message, Throwable cause) {
		super(400, message, cause);
	}

}

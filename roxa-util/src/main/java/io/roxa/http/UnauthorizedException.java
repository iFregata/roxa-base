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
public class UnauthorizedException extends ClientSideException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7544788578998911795L;

	/**
	 * 
	 */
	public UnauthorizedException() {
		super(401, "Unauthorized");
	}

	/**
	 * @param message
	 */
	public UnauthorizedException(String message) {
		super(401, message);
	}

	/**
	 * @param cause
	 */
	public UnauthorizedException(Throwable cause) {
		super(401, cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public UnauthorizedException(String message, Throwable cause) {
		super(401, message, cause);
	}

}

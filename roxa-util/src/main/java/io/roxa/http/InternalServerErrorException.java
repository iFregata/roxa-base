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
public class InternalServerErrorException extends ServerSideException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8832905082926227453L;

	/**
	 * 
	 */
	public InternalServerErrorException() {
		super(500, "Internal Server Error");
	}

	/**
	 * @param statusCode
	 * @param statusText
	 */
	public InternalServerErrorException(String statusText) {
		super(500, statusText);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public InternalServerErrorException(String message, Throwable cause) {
		super(500, message, cause);
	}

	/**
	 * @param cause
	 */
	public InternalServerErrorException(Throwable cause) {
		super(500, cause);
	}

}

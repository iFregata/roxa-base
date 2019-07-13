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
public class ForbiddenException extends ClientSideException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5573273158957940205L;

	/**
	 * 
	 */
	public ForbiddenException() {
		super(403, "Forbidden");
	}

	/**
	 * @param message
	 */
	public ForbiddenException(String message) {
		super(403, message);
	}

	/**
	 * @param cause
	 */
	public ForbiddenException(Throwable cause) {
		super(403, cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ForbiddenException(String message, Throwable cause) {
		super(403, message, cause);
	}

}

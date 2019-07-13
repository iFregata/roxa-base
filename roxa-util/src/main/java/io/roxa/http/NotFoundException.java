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
public class NotFoundException extends ClientSideException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7384440222723301014L;

	/**
	 * 
	 */
	public NotFoundException() {
		super(404, "Not Found");
	}

	/**
	 * @param message
	 */
	public NotFoundException(String message) {
		super(404, message);
	}

	/**
	 * @param cause
	 */
	public NotFoundException(Throwable cause) {
		super(404, cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public NotFoundException(String message, Throwable cause) {
		super(404, message, cause);
	}

}

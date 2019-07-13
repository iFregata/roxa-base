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
package io.roxa;

/**
 * @author Steven Chen
 *
 */
public class IllegalParametersException extends GeneralFailureException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8123075513192079043L;

	public IllegalParametersException() {
		super();
	}

	public IllegalParametersException(int statusCode, String message) {
		super(statusCode, message);
	}

	public IllegalParametersException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public IllegalParametersException(String message, Throwable cause) {
		super(message, cause);
	}

	public IllegalParametersException(String message) {
		super(message);
	}

	public IllegalParametersException(Throwable cause) {
		super(cause);
	}

}

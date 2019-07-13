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
public class AuthorizeRestrictionException extends GeneralFailureException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8123075513192079043L;

	public AuthorizeRestrictionException() {
		super();
	}

	public AuthorizeRestrictionException(int statusCode, String message) {
		super(statusCode, message);
	}

	public AuthorizeRestrictionException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public AuthorizeRestrictionException(String message, Throwable cause) {
		super(message, cause);
	}

	public AuthorizeRestrictionException(String message) {
		super(message);
	}

	public AuthorizeRestrictionException(Throwable cause) {
		super(cause);
	}

}

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
public class GeneralFailureException extends Exception implements StatusCodifiedException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1351385219701671395L;
	protected int statusCode;

	/**
	 * 
	 */
	public GeneralFailureException() {
	}

	/**
	 * @param message
	 */
	public GeneralFailureException(String message) {
		super(message);
	}

	/**
	 * 
	 * @param statusCode
	 * @param message
	 */
	public GeneralFailureException(int statusCode, String message) {
		super(message);
		setStatusCode(statusCode);
	}

	/**
	 * @param cause
	 */
	public GeneralFailureException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public GeneralFailureException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public GeneralFailureException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	/**
	 * @return the statusCode
	 */
	public int getStatusCode() {
		return this.statusCode;
	}

	/**
	 * @param statusCode the statusCode to set
	 */
	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

}

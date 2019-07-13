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
public class GeneralSeriousException extends RuntimeException implements StatusCodifiedException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1020726891492554558L;
	protected int statusCode;

	/**
	 * 
	 */
	public GeneralSeriousException() {
	}

	/**
	 * @param message
	 */
	public GeneralSeriousException(String message) {
		super(message);
	}

	/**
	 * 
	 * @param statusCode
	 * @param message
	 */
	public GeneralSeriousException(int statusCode, String message) {
		super(message);
		this.statusCode = statusCode;
	}

	/**
	 * @param cause
	 */
	public GeneralSeriousException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public GeneralSeriousException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public GeneralSeriousException(String message, Throwable cause, boolean enableSuppression,
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

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

import io.roxa.GeneralFailureException;
import io.roxa.GeneralSeriousException;
import io.roxa.StatusCodifiedException;

/**
 * @author Steven Chen
 *
 */
public class HttpStatusException extends Exception implements StatusCodifiedException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5185160742681603464L;
	protected int statusCode;

	public HttpStatusException(int statusCode, String statusText) {
		super(statusText);
		setStatusCode(statusCode);
	}

	public HttpStatusException(int statusCode, Throwable cause) {
		super(cause);
		setStatusCode(statusCode);
	}

	public HttpStatusException(int statusCode, String statusText, Throwable cause) {
		super(statusText, cause);
		setStatusCode(statusCode);
	}

	public String getMessage() {
		Throwable cause = getCause();
		if (cause != null && cause != this) {
			return cause.getMessage();
		}
		return super.getMessage();
	}

	public int getStatusCode() {
		Throwable cause = getCause();
		if (cause != null) {
			if (cause instanceof GeneralFailureException) {
				Integer sc = ((GeneralFailureException) cause).getStatusCode();
				if (sc != null)
					setStatusCode(statusCode);
			} else if (cause instanceof GeneralSeriousException) {
				Integer sc = ((GeneralSeriousException) cause).getStatusCode();
				if (sc != null)
					setStatusCode(statusCode);
			}
		}
		return this.statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

}

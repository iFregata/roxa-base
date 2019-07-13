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
public class ServiceUnavailableException extends ServerSideException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6750577333504094433L;

	/**
	 * 
	 */
	public ServiceUnavailableException() {
		super(503, "Service unavailable");
	}

	/**
	 * @param message
	 */
	public ServiceUnavailableException(String message) {
		super(503, message);
	}

	/**
	 * @param cause
	 */
	public ServiceUnavailableException(Throwable cause) {
		super(503, cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ServiceUnavailableException(String message, Throwable cause) {
		super(503, message, cause);
	}

}

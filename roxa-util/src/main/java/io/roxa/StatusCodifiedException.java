/**
 * The MIT License
 * 
 * Copyright (c) 2018-2020 Shell Technologies PTY LTD
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
public interface StatusCodifiedException {

	/**
	 * @return the statusCode
	 */
	int getStatusCode();

	/**
	 * @param statusCode the statusCode to set
	 */
	void setStatusCode(int statusCode);

	String getMessage();

}

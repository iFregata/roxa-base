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

import java.net.URI;

/**
 * @author Steven Chen
 *
 */
public interface HttpAgentBuilder<T> {

	HttpAgentBuilder<T> uri(URI uri);

	HttpAgentBuilder<T> port(Integer port);

	HttpAgentBuilder<T> path(String path);

	HttpAgentBuilder<T> header(String name, String value);

	HttpAgentBuilder<T> pathParam(String pathParam);

	HttpAgentBuilder<T> queryParam(String name, String value);

	T build();

	HttpAgentBuilder<T> scheme(String string);

	HttpAgentBuilder<T> host(String host);

}

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
package io.roxa.vertx.cache;

import io.vertx.core.shareddata.Shareable;

/**
 * @author Steven Chen
 *
 */
public interface CachedItem<T> extends Shareable {

	String getCacheId();

	T item();
}

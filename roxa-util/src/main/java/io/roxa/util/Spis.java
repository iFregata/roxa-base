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
package io.roxa.util;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * @author Steven Chen
 *
 */
public abstract class Spis {

	public static <T> T load(Class<T> clazz) {
		ServiceLoader<T> loader = ServiceLoader.load(clazz);
		Iterator<T> itr = loader.iterator();
		if (itr.hasNext())
			return itr.next();
		throw new IllegalStateException("No SPI provider found!");
	}

}

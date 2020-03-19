/**
 * The MIT License
 * 
 * Copyright (c) 2019-2022 Shell Technologies PTY LTD
 *
 * You may obtain a copy of the License at
 * 
 *       http://mit-license.org/
 *       
 */
package io.roxa.util;

import java.util.function.Consumer;

/**
 * @author Steven Chen
 *
 */
public class GenericBuilder<T> {

	private T instance;

	private boolean ifCond = true;

	private GenericBuilder(Class<T> clazz) {
		try {
			instance = clazz.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public GenericBuilder<T> with(Consumer<T> setter) {
		if (ifCond) {
			setter.accept(instance);
		}
		return this;
	}

	public T build() {
		return instance;
	}

	public static <T> GenericBuilder<T> create(Class<T> clazz) {
		return new GenericBuilder<>(clazz);
	}
}

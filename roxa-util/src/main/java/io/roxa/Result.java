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

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Steven Chen
 *
 */
public interface Result<T> {

	boolean failed();

	Result<T> fail(String message);

	Result<T> fail(Throwable cause);

	Result<T> fail(String message, Throwable cause);

	boolean succeeded();

	Result<T> succeed(T result);

	Throwable cause();

	T result();

	<R> R map(Function<Result<T>, R> mapper);

	void handle(Consumer<Result<T>> consumer);

	static <T> Result<T> defaultResult() {
		return new DefaultResult<T>();
	}

}

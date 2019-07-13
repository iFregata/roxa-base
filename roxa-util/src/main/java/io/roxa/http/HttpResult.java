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

import java.io.Reader;
import java.util.function.Consumer;
import java.util.function.Function;

import io.roxa.GeneralFailureException;
import io.roxa.Result;

/**
 * @author Steven Chen
 *
 */
public class HttpResult<T> implements Result<T> {
	private T result;
	private Throwable cause;
	private boolean succeeded;
	private boolean failed;

	public static <T> Result<T> failure(Throwable e) {
		return new HttpResult<T>().fail(e);
	}

	public static <T> Result<T> failure(String message) {
		return new HttpResult<T>().fail(message);
	}

	public static <T> Result<T> failure(String message, Throwable e) {
		return new HttpResult<T>().fail(message, e);
	}

	public static <T> Result<T> succeeful(T rs) {
		return new HttpResult<T>().succeed(rs);
	}

	public static Result<String> succeeful(String message) {
		return new HttpResult<String>().succeed(message);
	}

	public static Result<Reader> succeeful(Reader reader) {
		return new HttpResult<Reader>().succeed(reader);
	}

	/**
	 * 
	 */
	private HttpResult() {
	}

	@Override
	public boolean failed() {
		return failed;
	}

	@Override
	public Result<T> fail(String message) {
		this.cause = new GeneralFailureException(message, cause);
		this.failed = true;
		this.succeeded = false;
		return this;
	}

	@Override
	public Result<T> fail(Throwable cause) {
		this.cause = cause;
		this.failed = true;
		this.succeeded = false;
		return this;
	}

	@Override
	public Result<T> fail(String message, Throwable cause) {
		this.cause = new GeneralFailureException(message, cause);
		this.failed = true;
		this.succeeded = false;
		return this;
	}

	@Override
	public boolean succeeded() {
		return succeeded;
	}

	@Override
	public Result<T> succeed(T result) {
		this.succeeded = true;
		this.failed = false;
		this.result = result;
		return this;
	}

	@Override
	public Throwable cause() {
		return cause;
	}

	@Override
	public T result() {
		return result;
	}

	@Override
	public <R> R map(Function<Result<T>, R> mapper) {
		return mapper.apply(this);
	}

	@Override
	public void handle(Consumer<Result<T>> consumer) {
		consumer.accept(this);
	}

}

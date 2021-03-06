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
package io.roxa.vertx.rx;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.roxa.vertx.rx.jdbc.JdbcAgent;
import io.roxa.vertx.rx.jdbc.JdbcDeployer;
import io.roxa.vertx.rx.jdbc.JdbcExecutor;
import io.roxa.vertx.rx.jdbc.JdbcManager;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.eventbus.Message;

/**
 * @author Steven Chen
 *
 */
public abstract class EventActionDispatcher extends AbstractVerticle {

	private static final Logger logger = LoggerFactory.getLogger(EventActionDispatcher.class);

	public static final String EVENT_HEADER_ACTION = "action";

	public static final String EVENT_HEADER_STYLE = "style";

	public static final String EVENT_STYLE_REQUEST = "request";

	public static final String EVENT_STYLE_SEND = "send";

	public static final String EVENT_STYLE_PUBLISH = "publish";

	protected Map<String, Consumer<Message<Object>>> handlers;

	@Deprecated
	protected JdbcExecutor jdbc;

	protected String eventBusURN;
	@Deprecated
	protected String jdbcSourceName;

	private Disposable jdbcAgentDisposable;

	/**
	 * 
	 * @param eventBusURN
	 */
	public EventActionDispatcher(String eventBusURN) {
		this(eventBusURN, (String) null);
	}

	/**
	 * @param eventBusURN
	 * @param jdbcSourceName
	 */
	@Deprecated
	public EventActionDispatcher(String eventBusURN, String jdbcSourceName) {
		this.eventBusURN = eventBusURN;
		this.jdbcSourceName = jdbcSourceName;
	}

	public EventActionDispatcher(String eventBusURN, JdbcDeployer jdbcDeployer) {
		this.eventBusURN = eventBusURN;
		jdbcAgentDisposable = jdbcDeployer.subscribe(jdbcAgent -> {
			logger.debug("Prepare to setup JdbcAgent[{}] on EventActionDispatcher[{}]", jdbcAgent, this);
			setJdbcAgent(jdbcAgent);
		});
	}

	@Override
	public void start(Promise<Void> startPromise) throws Exception {
		if (eventBusURN != null)
			vertx.eventBus().consumer(eventBusURN, this::dispatch);
		if (jdbcSourceName != null)
			JdbcManager.register(jdbcSourceName, this::setJdbc);
		didSetupDispatch();
		super.start(startPromise);
	}

	@Override
	public void stop(Promise<Void> stopPromise) throws Exception {
		if (jdbcAgentDisposable != null)
			jdbcAgentDisposable.dispose();
		dispose();
		super.stop(stopPromise);
	}

	protected void didSetupDispatch() {

	}

	protected void dispose() {

	}

	protected void registerHandler(String action, Consumer<Message<Object>> handler) {
		if (handlers == null) {
			handlers = new HashMap<>();
		}
		handlers.put(action, handler);
	}

	@Deprecated
	protected void setJdbc(JdbcExecutor jdbc) {
		this.jdbc = jdbc;
	}

	protected void setJdbcAgent(JdbcAgent jdbcAgent) {
	}

	protected void dispatch(Message<Object> msg) {
		String action = msg.headers().get(EVENT_HEADER_ACTION);
		if (handlers != null && handlers.containsKey(action)) {
			handlers.get(action).accept(msg);
		} else {
			dispatchByReflect(msg);
		}

	}

	protected void dispatchByReflect(Message<Object> msg) {
		String action = msg.headers().get(EVENT_HEADER_ACTION);
		String style = msg.headers().get(EVENT_HEADER_STYLE);
		boolean needsToReply = EVENT_STYLE_REQUEST.equals(style);
		String actionSignature = String.format("%s#%s", this.eventBusURN, action);
		Class<? extends EventActionDispatcher> clazz = this.getClass();
		Object params = msg.body();
		try {
			if (params == null) {
				Method m = clazz.getMethod(action);
				if (m == null)
					throw new IllegalStateException(String.format("%s not found", actionSignature));
				// publishDispatchBefore(actionSignature, null);
				Single<?> single = (Single<?>) m.invoke(this);
				single.subscribe(result -> {
					// publishDispatchAfter(actionSignature, null, result);
					if (needsToReply)
						msg.reply(result);
				}, e -> {
					String errorMsg = String.format("Invoke %s failed", actionSignature);
					logger.error(errorMsg, e);
					// publishDispatchException(actionSignature, errorMsg);
					if (needsToReply)
						msg.fail(500, errorMsg);
				});

			} else if (params != null && (params instanceof JsonObject)) {
				Method m = clazz.getMethod(action, JsonObject.class);
				if (m == null)
					throw new IllegalStateException(String.format("%s not found", actionSignature));
				// publishDispatchBefore(actionSignature, msg.body());
				Single<?> single = (Single<?>) m.invoke(this, msg.body());
				single.subscribe(result -> {
					// publishDispatchAfter(actionSignature, null, msg.body());
					if (needsToReply)
						msg.reply(result);
				}, e -> {
					String errorMsg = String.format("Invoke %s failed", actionSignature);
					logger.error(errorMsg, e);
					// publishDispatchException(actionSignature, errorMsg);
					if (needsToReply)
						msg.fail(500, errorMsg);
				});
			} else {
				throw new IllegalStateException("The argument must be JsonObject only!");
			}
		} catch (Throwable e) {
			String errorMsg = String.format("Invoke Exception: %s, cause by: %s", actionSignature, e.getMessage());
			logger.error(errorMsg, e);
			// publishDispatchException(actionSignature, errorMsg);
			if (needsToReply)
				msg.fail(500, errorMsg);
		}
	}

}

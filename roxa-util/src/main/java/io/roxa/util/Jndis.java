/**
 * The MIT License
 * 
 * Copyright (c) 2016 Shell Technologies PTY LTD. http://au.shellpays.com
 *
 * You may obtain a copy of the License at
 * 
 *       http://mit-license.org/
 *       
 */
package io.roxa.util;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author steven
 *
 */
@SuppressWarnings("unchecked")
public abstract class Jndis {

	private static final Logger logger = LoggerFactory.getLogger(Jndis.class);

	static final String REGULAR_JNDI_SERVICE_PREFIX = "jndi:service";

	public static void registry(String name, Object entity) {
		registry(name, null, entity);
	}

	public static void registry(String name, String filterExp, Object entity) {
		try {
			StringBuilder sb = new StringBuilder(REGULAR_JNDI_SERVICE_PREFIX).append("/").append(name);
			String _filterExp = resolveExp(filterExp);
			if (_filterExp != null) {
				sb.append("/(&").append(_filterExp).append(")");
			}
			String lookupName = sb.toString();
			logger.info("Jndi registry: {}", lookupName);
			InitialContext ctx = new InitialContext();
			Context root = createSubcontext(ctx);
			root.bind(lookupName, entity);
		} catch (Exception e) {
			logger.error("Jndi registry failed!", e);
		}
	}

	public static void unregistry(String name) {
		unregistry(name, null);
	}

	public static void unregistry(String name, String filterExp) {
		try {
			StringBuilder sb = new StringBuilder(REGULAR_JNDI_SERVICE_PREFIX).append("/").append(name);
			String _filterExp = resolveExp(filterExp);
			if (_filterExp != null) {
				sb.append("/(&").append(_filterExp).append(")");
			}
			InitialContext ctx = new InitialContext();
			String lookupName = sb.toString();
			logger.info("Jndi unregistry: {}", lookupName);
			ctx.unbind(lookupName);
		} catch (Exception e) {
		}
	}

	public static <T> T service(Class<T> clazz) {
		return service(clazz, null);
	}

	public static <T> T service(Class<T> clazz, String filterExp) {
		try {
			StringBuilder sb = new StringBuilder(REGULAR_JNDI_SERVICE_PREFIX).append("/");
			sb.append(clazz.getName());
			String _filterExp = resolveExp(filterExp);
			if (_filterExp != null) {
				sb.append("/(&").append(_filterExp).append(")");
			}
			String lookupName = sb.toString();
			logger.info("Jndi lookup: {}", lookupName);
			InitialContext ctx = new InitialContext();
			return (T) ctx.lookup(lookupName);
		} catch (Exception e) {
			logger.error("Jndi lookup failed!", e);
			return null;
		}
	}

	private static Context createSubcontext(Context ctx) throws Exception {
		try {
			ctx.lookup(REGULAR_JNDI_SERVICE_PREFIX);
		} catch (Exception e) {
			ctx.createSubcontext(REGULAR_JNDI_SERVICE_PREFIX);
		}
		return ctx;
	}

	private static String resolveExp(String filterExp) {
		if (filterExp == null)
			return null;
		String _exp = filterExp.trim();
		if (_exp == null)
			return null;
		String[] group = _exp.split(";");
		if (group == null || group.length == 0)
			return null;
		StringBuilder sb = new StringBuilder("/(&");
		for (String item : group) {
			sb.append(String.format("(%s)", item));
		}
		sb.append(")");
		return sb.toString();
	}
}

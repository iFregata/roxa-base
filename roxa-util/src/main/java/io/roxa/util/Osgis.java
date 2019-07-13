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

import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * @author steven
 *
 */
@SuppressWarnings("unchecked")
public abstract class Osgis {

	static final String OSGi_SERVICE_PREFIX = "osgi:service/";

	/**
	 * 
	 * @param clazzv    - The service interface
	 * @param filterExp - The filter expression formed with
	 *                  <code>foo=bar;bba=ffp</code>
	 * @return
	 */
	public static <T> T service(Class<T> clazz, String filterExp) {
		try {
			StringBuilder osgiURL = new StringBuilder(OSGi_SERVICE_PREFIX);
			osgiURL.append(clazz.getName());
			String _filterExp = resolveExp(filterExp);
			if (_filterExp != null) {
				osgiURL.append("/(&").append(_filterExp).append(")");
			}
			InitialContext ctx = new InitialContext();
			return (T) ctx.lookup(osgiURL.toString());
		} catch (NamingException e) {
			return null;
		}
	}

	/**
	 * When service register provides the property <em>osgi.jndi.service.name</em>,
	 * then we look up service with the name of JDNI service.
	 * 
	 * @param jndiServiceName - The name of JDNI service
	 * @return T as expected service interface
	 */
	public static <T> T service(String jndiServiceName) {
		try {
			StringBuilder osgiURL = new StringBuilder(OSGi_SERVICE_PREFIX);
			osgiURL.append(jndiServiceName);
			InitialContext ctx = new InitialContext();
			return (T) ctx.lookup(osgiURL.toString());
		} catch (NamingException e) {
			return null;
		}
	}

	public static <T> T service(Class<T> clazz) {
		return service(clazz, null);
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

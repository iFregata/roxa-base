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
package io.roxa.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Steven Chen
 *
 */
public abstract class Strings {

	/**
	 * @param expression
	 * @return
	 */
	public static Map<String, Object> asMap(String expression, String groupSplitChar, String entrySplitChar) {
		Map<String, Object> queryParams = new HashMap<>();
		String _value = emptyAsNull(expression);
		if (_value == null)
			return null;
		String[] entries = asArray(_value, groupSplitChar);
		if (entries == null || entries.length == 0)
			return null;
		for (String entryExp : entries) {
			String[] entry = asArray(entryExp, entrySplitChar);
			if (entry == null || entry.length < 2)
				continue;
			String key = emptyAsNull(entry[0]);
			String value = emptyAsNull(entry[1]);
			if (key == null || value == null)
				continue;
			queryParams.put(key, value);
		}
		return queryParams;
	}

	/**
	 * @param expression
	 * @return
	 */
	public static String[] asArray(String expression, String splitChar) {
		List<String> result = new ArrayList<String>();
		String _value = emptyAsNull(expression);
		if (_value == null)
			return null;
		String[] entries = _value.split(splitChar);
		if (entries == null || entries.length == 0)
			return null;
		for (String entryExp : entries) {
			String _entryExp = emptyAsNull(entryExp);
			if (_entryExp != null)
				result.add(_entryExp);
		}
		if (result.isEmpty())
			return null;
		return result.toArray(new String[0]);
	}

	public static String emptyAsNull(String source) {
		if (source == null)
			return null;
		String value = source.trim();
		if ("".equals(value))
			return null;
		return value;
	}

	public static String emptyWith(String source, String replacement) {
		String _v = emptyAsNull(source);
		return _v == null ? replacement : _v;
	}

	public static Integer toInteger(String v) {
		if (v == null)
			return null;
		try {
			return new Integer(v);
		} catch (Exception e) {
			return null;
		}
	}

	public static Long toLong(String v) {
		if (v == null)
			return null;
		try {
			return new Long(v);
		} catch (Exception e) {
			return null;
		}
	}

	public static BigDecimal toBigDecimal(String v) {
		if (v == null)
			return null;
		try {
			return new BigDecimal(v);
		} catch (Exception e) {
			return null;
		}
	}
}

/**
 * The MIT License
 * 
 * Copyright (c) 2016-2016 Shell Technologies PTY LTD
 *
 * You may obtain a copy of the License at
 * 
 *       http://mit-license.org/
 *       
 */
package io.roxa.util;

import java.math.BigDecimal;

/**
 * @author steven
 *
 */
public class Money {
	private static final BigDecimal CURRENCY_FACTOR_100 = new BigDecimal("100");
	private static int DECIMALS = 2;
	private static int ROUNDING_MODE = BigDecimal.ROUND_HALF_UP;

	public static Double currencyHalfUp(Double d) {
		if (d == null) {
			return null;
		}
		BigDecimal bd = new BigDecimal(d);
		return bd.setScale(DECIMALS, ROUNDING_MODE).doubleValue();
	}

	public static Double currencyHalfDown(Double d) {
		if (d == null) {
			return null;
		}
		BigDecimal bd = new BigDecimal(d);
		return bd.setScale(DECIMALS, BigDecimal.ROUND_HALF_DOWN).doubleValue();
	}

	public static Double currencyHalfEven(Double d) {
		if (d == null) {
			return null;
		}
		BigDecimal bd = new BigDecimal(d);
		return bd.setScale(DECIMALS, BigDecimal.ROUND_HALF_EVEN).doubleValue();
	}

	public static BigDecimal asCurrency(String currencyStr) {
		String _cur = Strings.emptyAsNull(currencyStr);
		if (_cur == null)
			return null;
		try {
			return rounded(new BigDecimal(_cur));
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public static BigDecimal asCurrencyFactor100(String currencyStr) {
		String _cur = Strings.emptyAsNull(currencyStr);
		if (_cur == null)
			return null;
		try {
			BigDecimal _temp = rounded(new BigDecimal(_cur));
			if (_temp.abs().compareTo(CURRENCY_FACTOR_100) < 0)
				return null;
			return _temp.divide(CURRENCY_FACTOR_100, ROUNDING_MODE);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public static BigDecimal asYuanAmount(Integer amount) {
		if (amount == null)
			return null;
		BigDecimal b = new BigDecimal(amount);
		return b.divide(CURRENCY_FACTOR_100, 2, BigDecimal.ROUND_HALF_UP);
	}

	public static BigDecimal asYuanAmount(String amount) {
		if (amount == null)
			return null;
		BigDecimal b = new BigDecimal(amount);
		return b.divide(CURRENCY_FACTOR_100, 2, BigDecimal.ROUND_HALF_UP);
	}

	public static Integer asFenAmount(Double amount) {
		if (amount == null)
			return null;
		BigDecimal b = new BigDecimal(amount);
		return rounded(b.multiply(CURRENCY_FACTOR_100)).intValue();
	}

	public static Integer asFenAmount(String amount) {
		if (amount == null)
			return null;
		BigDecimal b = new BigDecimal(amount);
		return rounded(b.multiply(CURRENCY_FACTOR_100)).intValue();
	}

	private static BigDecimal rounded(BigDecimal aNumber) {
		if (aNumber == null)
			return null;
		return aNumber.setScale(DECIMALS, ROUNDING_MODE);
	}
}

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

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * @author Steven Chen
 *         <p>
 *         15位身份证号码：第7、8位为出生年份(两位数)，第9、10位为出生月份，第11、12位代表出生日期，第15位代表性别，奇数为男，偶数为女。
 *         </p>
 *         <p>
 *         18位身份证号码：第7、8、9、10位为出生年份(四位数)，第11、第12位为出生月份，第13、14位代表出生日期，第17位代表性别，奇数为男，偶数为女。
 *         </p>
 */
public abstract class Validators {
	protected static String[][] idCardCityPairs = { { "11", "北京" }, { "12", "天津" }, { "13", "河北" }, { "14", "山西" },
			{ "15", "内蒙古" }, { "21", "辽宁" }, { "22", "吉林" }, { "23", "黑龙江" }, { "31", "上海" }, { "32", "江苏" },
			{ "33", "浙江" }, { "34", "安徽" }, { "35", "福建" }, { "36", "江西" }, { "37", "山东" }, { "41", "河南" },
			{ "42", "湖北" }, { "43", "湖南" }, { "44", "广东" }, { "45", "广西" }, { "46", "海南" }, { "50", "重庆" },
			{ "51", "四川" }, { "52", "贵州" }, { "53", "云南" }, { "54", "西藏" }, { "61", "陕西" }, { "62", "甘肃" },
			{ "63", "青海" }, { "64", "宁夏" }, { "65", "新疆" }, { "71", "台湾" }, { "81", "香港" }, { "82", "澳门" },
			{ "91", "国外" } };

	protected static String[] idCardCityCode = { "11", "12", "13", "14", "15", "21", "22", "23", "31", "32", "33", "34",
			"35", "36", "37", "41", "42", "43", "44", "45", "46", "50", "51", "52", "53", "54", "61", "62", "63", "64",
			"65", "71", "81", "82", "91" };
	// 每位加权因子
	private static int idCardFactor[] = { 7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2 };
	// 第18位校检码
	private static String idCardCC[] = { "1", "0", "X", "9", "8", "7", "6", "5", "4", "3", "2" };

	private static SimpleDateFormat shortDf = new SimpleDateFormat("yyMMdd");

	public static boolean isDob(String yyyyMMdd) {
		try {
			LocalDate ld = LocalDate.parse(yyyyMMdd, DateTimeFormatter.ISO_DATE);
			if (ld.isBefore(LocalDate.of(1900, 1, 1)))
				return false;
			if (ld.isAfter(LocalDate.now()))
				return false;
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean isPhoneNumber(String phoneNumber) {
		String _phoneNumber = Strings.emptyAsNull(phoneNumber);
		if (_phoneNumber == null)
			return false;
		return _phoneNumber.matches("^\\d{8,13}$");
	}

	public static boolean isIdCardNumber(String idCardNumber) {
		String _idCardNumber = Strings.emptyAsNull(idCardNumber);
		if (_idCardNumber == null)
			return false;
		if (_idCardNumber.length() == 15)
			_idCardNumber = toIdCardNumber18(_idCardNumber);
		return isIdCardNumber18(_idCardNumber);
	}

	public static String toIdCardNumber18(String idCardNumber15) {
		String _oldIdCardNumber = Strings.emptyAsNull(idCardNumber15);
		if (_oldIdCardNumber == null)
			return null;
		if (_oldIdCardNumber.length() != 15)
			return null;
		String _dob = _oldIdCardNumber.substring(6, 12);
		String dob = fullymd(_dob);
		String newIdCardNumbers = _oldIdCardNumber.substring(0, 6) + dob + _oldIdCardNumber.substring(12);
		int[][] digits = toDigits(newIdCardNumbers);
		if (digits == null)
			return null;
		int factorSum = Arrays.stream(digits).collect(Collectors.summingInt(e -> e[0] * e[1]));
		int mod = factorSum % 11;
		String _cc = idCardCC[mod];
		return newIdCardNumbers + _cc;
	}

	/**
	 * <p>
	 * 判断18位身份证的合法性
	 * </p>
	 * <p>
	 * 根据〖中华人民共和国国家标准GB11643-1999〗中有关公民身份号码的规定，公民身份号码是特征组合码，由十七位数字本体码和一位数字校验码组成。
	 * 排列顺序从左至右依次为：
	 * </p>
	 * <ul>
	 * <li>六位数字地址码</li>
	 * <li>八位数字出生日期码</li>
	 * <li>三位数字顺序码和一位数字校验码</li>
	 * </ul>
	 * <p>
	 * 顺序码
	 * </p>
	 * <p>
	 * 表示在同一地址码所标识的区域范围内，对同年、同月、同 日出生的人编定的顺序号，顺序码的奇数分配给男性，偶数分配 给女性。
	 * </p>
	 * <ol>
	 * <li>前1、2位数字表示：所在省份的代码</li>
	 * <li>第3、4位数字表示：所在城市的代码</li>
	 * <li>第5、6位数字表示：所在区县的代码</li>
	 * <li>第7~14位数字表示：出生年、月、日</li>
	 * <li>第15、16位数字表示：所在地的派出所的代码</li>
	 * <li>第17位数字表示性别：奇数表示男性，偶数表示女性</li>
	 * <li>第18位数字是校检码：也有的说是个人信息码，一般是随计算机的随机产生，用来检验身份证的正确性。校检码可以是0~9的数字，有时也用x表示</li>
	 * </ol>
	 * <p>
	 * 第十八位数字(校验码)的计算方法为
	 * </p>
	 * <ol>
	 * <li>将前面的身份证号码17位数分别乘以不同的系数。从第一位到第十七位的系数分别为：7 9 10 5 8 4 2 1 6 3 7 9 10 5 8 4
	 * 2</li>
	 * <li>将这17位数字和系数相乘的结果相加</li>
	 * <li>用加出来和除以11，看余数是多少</li>
	 * <li>余数只可能有0 1 2 3 4 5 6 7 8 9 10这11个数字。其分别对应的最后一位身份证的号码为1 0 X 9 8 7 6 5 4 3
	 * 2</li>
	 * <li>通过上面得知如果余数是2，就会在身份证的第18位数字上出现罗马数字的Ⅹ。如果余数是10，身份证的最后一位号码就是2</li>
	 * 
	 * @param idCardNumber
	 * @return
	 */
	private static boolean isIdCardNumber18(String idCardNumber) {
		String _idCardNumber = Strings.emptyAsNull(idCardNumber);
		if (_idCardNumber == null)
			return false;
		if (_idCardNumber.length() != 18)
			return false;
		String numbers = _idCardNumber.substring(0, 17);
		String cc = _idCardNumber.substring(17, 18);
		int[][] digits = toDigits(numbers);
		if (digits == null)
			return false;
		int factorSum = Arrays.stream(digits).collect(Collectors.summingInt(e -> e[0] * e[1]));
		int mod = factorSum % 11;
		String _cc = idCardCC[mod];
		return _cc.equals(cc);
	}

	private static String fullymd(String simpleymd) {
		try {
			Date date = shortDf.parse(simpleymd);
			return String.format("%1$tY%1$tm%1$td", date);
		} catch (Exception e) {
			return null;
		}
	}

	private static int[][] toDigits(String idCardNumber) {
		char[] ca = idCardNumber.toCharArray();
		int[][] rs = new int[ca.length][2];
		for (int i = 0; i < ca.length; i++) {
			if (!Character.isDigit(ca[i]))
				return null;
			rs[i][0] = ca[i] ^ '0';
			rs[i][1] = idCardFactor[i];
		}
		return rs;
	}
}

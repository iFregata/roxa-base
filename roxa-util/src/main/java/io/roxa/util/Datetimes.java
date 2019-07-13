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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;

/**
 * @author Steven Chen
 *
 */
public class Datetimes {

	private final static String FORMAT_ISO8601 = "yyyy-MM-dd'T'HH:mm:ss'Z'";
	private final static String TIME_ZONE = "GMT";
	private final static ZoneId ZONE_ID_UTC8 = ZoneId.of("UTC+8");
	private final static ZoneOffset ZONE_OFFSET_8 = ZoneOffset.of("+8");

	public static void main(String[] args) {
		int inSecond = 1489120637;
		System.out.printf("%d%n", inSecond);
		System.out.printf("%s%n", asISOLocalDateTime(inSecond));
		int s = (int) (new Date().getTime() / 1000);
		System.out.printf("%s%n", asISOLocalDateTime(s));
	}

	public static String asISO8601GMTString() {
		return asISO8601GMTString(new Date());
	}

	public static String asISO8601GMTString(Date date) {
		Date nowDate = date;
		if (null == date) {
			nowDate = new Date();
		}
		SimpleDateFormat df = new SimpleDateFormat(FORMAT_ISO8601);
		df.setTimeZone(new SimpleTimeZone(0, TIME_ZONE));

		return df.format(nowDate);
	}

	public static String asISOLocalDateTime(Integer inSecond) {
		return LocalDateTime.ofEpochSecond(inSecond, 0, ZONE_OFFSET_8).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
	}

	public static Integer asSecond(String isoLocaDateTime) {
		return (int) LocalDateTime.parse(isoLocaDateTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME).atZone(ZONE_ID_UTC8)
				.toInstant().getEpochSecond();
	}

	public static Date asDate(String dateStr, String pattern) {

		String _dateStr = Strings.emptyAsNull(dateStr);
		if (_dateStr == null)
			return null;
		try {
			LocalDateTime ldt = LocalDateTime.parse(_dateStr, DateTimeFormatter.ofPattern(pattern));
			return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
		} catch (Exception e) {
			return null;
		}
	}

	public static Long asDateMillis(Date date) {
		if (date == null)
			return null;
		Calendar c = new GregorianCalendar();
		c.setTime(date);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c.getTimeInMillis();
	}

	public static Date asDateFromSecond(String second) {
		String _second = Strings.emptyAsNull(second);
		if (_second == null)
			return new Date();
		try {
			int secInt = Integer.parseInt(_second);
			return asDateFromSecond(secInt);
		} catch (NumberFormatException e) {
			return new Date();
		}
	}

	public static Date asDateFromSecond(int second) {
		if (second < 0 || second > Integer.MAX_VALUE)
			return new Date();
		long secondsBased = second * 1000L;
		return new Date(secondsBased);
	}

	public static Integer asSecondFromDate(Date date) {
		return (int) (date.getTime() / 1000);
	}

	public static Long currentTimeInMillis() {
		return LocalDateTime.now().atZone(ZONE_ID_UTC8).toInstant().toEpochMilli();
	}

	public static Integer currentTimeInSecond() {
		return (int) LocalDateTime.now().atZone(ZONE_ID_UTC8).toInstant().getEpochSecond();
	}

	public static Integer afterDaysInSecond(Integer fromSecond, Integer days) {
		return (int) LocalDateTime.ofEpochSecond(fromSecond, 0, ZONE_OFFSET_8).plusDays(days)
				.toEpochSecond(ZONE_OFFSET_8);
	}

}

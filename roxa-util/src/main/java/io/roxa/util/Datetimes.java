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
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;
import java.util.regex.Pattern;

/**
 * <p>
 * GMT - Greenwich Mean Time
 * <p>
 * UTC - Coordinated Universal Time
 * 
 * @author Steven Chen
 *
 */
public class Datetimes {

	private final static String FORMAT_ISO8601 = "yyyy-MM-dd'T'HH:mm:ss'Z'";
	private final static String TIME_ZONE = "GMT";
	private final static ZoneId ZONE_ID_UTC8 = ZoneId.of("UTC+8");
	public final static ZoneId ZONE_ID_GMT = ZoneId.of("Z");// GMT, UTC
	private final static ZoneOffset ZONE_OFFSET_8 = ZoneOffset.of("+8");
	public final static ZoneOffset ZONE_OFFSET_GMT = ZoneOffset.of("Z");
	private static final Pattern DATETIME = Pattern
			.compile("^\\d{4}-(?:0[0-9]|1[0-2])-[0-9]{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d{1,9})?Z$");
	private static final Pattern DATETIME_LOCAL = Pattern
			.compile("^\\d{4}-(?:0[0-9]|1[0-2])-[0-9]{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d{1,9})?$");

	public static void main(String[] args) {
		System.out.println(currentLocalDate(System.currentTimeMillis()));
	}

	/**
	 * 
	 * @param zValue - UTC/GMT time string e.g. 2011-12-03T10:15:30Z
	 * @return
	 */
	public static Date asDate(String zValue) {
		if (DATETIME.matcher(zValue).matches()) {
			Instant instant = Instant.from(DateTimeFormatter.ISO_INSTANT.parse(zValue));
			return Date.from(instant);
		}
		throw new IllegalStateException("Malformed date time string");
	}

	/**
	 * 
	 * @param localValue - Local time string e.g. 2011-12-03T10:15:30
	 * @return
	 */
	public static Date asDateLocal(String localValue) {
		if (DATETIME_LOCAL.matcher(localValue).matches()) {
			LocalDateTime ldt = LocalDateTime.parse(localValue, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
			return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
		}
		throw new IllegalStateException("Malformed date time string");
	}

	/**
	 * 
	 * @return - Local date string e.g. 20111203
	 */
	public static String currentLocalDate() {
		return LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
	}

	/**
	 * 
	 * @return - Local date string e.g. 20111203
	 */
	public static String currentLocalDate(long timeMillis) {
		return LocalDateTime.ofInstant(Instant.ofEpochMilli(timeMillis), ZoneId.systemDefault())
				.format(DateTimeFormatter.BASIC_ISO_DATE);
	}

	/**
	 * 
	 * @return - Local time string e.g. 2011-12-03T10:15:30
	 */
	public static String asStringLocal() {
		return LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
	}

	/**
	 * 
	 * @param date
	 * @return - Local time string e.g. 2011-12-03T10:15:30
	 */
	public static String asStringLocal(Date date) {
		return String.format("%1$tFT%1$tT", date);
	}

	/**
	 * 
	 * @return - UTC/GMT time string e.g. 2011-12-03T10:15:30Z
	 */
	public static String asZString() {
		return DateTimeFormatter.ISO_INSTANT.format(Instant.now().truncatedTo(ChronoUnit.SECONDS));
	}

	/**
	 * 
	 * @param date
	 * @return - UTC/GMT time string e.g. 2011-12-03T10:15:30Z
	 */
	public static String asZString(Date date) {
		return DateTimeFormatter.ISO_INSTANT.format(date.toInstant().truncatedTo(ChronoUnit.SECONDS));
	}

	/**
	 * 
	 * @return - The local time-millis at start of day
	 */
	public static Long startOfDayInMillis() {
		return LocalDate.now().atStartOfDay(ZONE_ID_UTC8).toInstant().toEpochMilli();
	}

	/**
	 * ISO8601 GMT date time string 2019-08-13T12:01:07Z
	 * 
	 * @return
	 */
	public static String asISO8601GMTString() {
		return asISO8601GMTString(new Date());
	}

	/**
	 * 
	 * @param date
	 * @return
	 */
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

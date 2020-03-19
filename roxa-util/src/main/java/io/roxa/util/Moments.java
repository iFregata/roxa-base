/**
 * The MIT License
 * 
 * Copyright (c) 2018-2020 Shell Technologies PTY LTD
 *
 * You may obtain a copy of the License at
 * 
 *       http://mit-license.org/
 *       
 */
package io.roxa.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * @author Steven Chen
 *
 */
public class Moments {
	private static final Pattern DATETIME_Z = Pattern
			.compile("^\\d{4}-(?:0[0-9]|1[0-2])-[0-9]{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d{1,9})?Z$");
	private static final Pattern DATETIME_LOCAL = Pattern
			.compile("^\\d{4}-(?:0[0-9]|1[0-2])-[0-9]{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d{1,9})?$");
	private static final Pattern DATETIME_WITHOUT_T_FLAG = Pattern
			.compile("^\\d{4}-(?:0[0-9]|1[0-2])-[0-9]{2} \\d{2}:\\d{2}:\\d{2}(\\.\\d{1,9})?$");

	private static final DateTimeFormatter compact_ISO_LOCAL_DATE_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
	private static final DateTimeFormatter withoudTFlag_ISO_LOCAL_DATE_TIME = DateTimeFormatter
			.ofPattern("yyyy-MM-dd HH:mm:ss");

	private final static ZoneId ZONE_ID_GMT = ZoneId.of("Z");// GMT, UTC
	public final static ZoneOffset ZONE_OFFSET_GMT = ZoneOffset.of("Z");

	public final static Long ONE_DAY_MILLIS = 24 * 60 * 60 * 1000L;
	public final static Long ONE_DAY_SECONDS = 24 * 60 * 60L;

	public static void main(String[] args) {
		System.out.println(zstrDateTimeSec(currentTimeSeconds()));
		System.out.println(secondStartOfDay());
		System.out.println(currentTimeSeconds());
	}

	public static Long currentTimeMillis() {
		return System.currentTimeMillis();
	}

	public static Long currentTimeSeconds() {
		return LocalDateTime.now().atZone(ZoneId.systemDefault()).toEpochSecond();
	}

	/**
	 * 
	 * @return - The local time-millis at start of day
	 */
	public static Long millisStartOfDay() {
		return LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
	}

	/**
	 * 
	 * @return - The local time-millis at start of day
	 */
	public static Long millisStartOfDay(Long timeMillis) {
		return LocalDateTime.ofInstant(Instant.ofEpochMilli(timeMillis), ZoneId.systemDefault()).toLocalDate()
				.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
	}

	/**
	 * 
	 * @return - The local time-second at start of day
	 */
	public static Long secondStartOfDay() {
		return LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
	}

	/**
	 * 
	 * @return - The local time-second at start of day
	 */
	public static Long secondStartOfDay(Long timeMillis) {
		return LocalDateTime
				.ofInstant(Instant.ofEpochMilli(timeMillis).truncatedTo(ChronoUnit.SECONDS), ZoneId.systemDefault())
				.toLocalDate().atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
	}

	/**
	 * 
	 * @param localValue - Local time string e.g. 2011-12-03T10:15:30
	 * @return
	 */
	public static Date asDate(String localValue) {
		if (DATETIME_LOCAL.matcher(localValue).matches()) {
			LocalDateTime ldt = LocalDateTime.parse(localValue, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
			return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
		}
		throw new IllegalStateException("Malformed date time string");
	}

	/**
	 * 
	 * @param localValue - Local time string e.g. 2011-12-03 10:15:30
	 * @return
	 */
	public static Date asDateWithoutTFlag(String localValue) {
		if (DATETIME_WITHOUT_T_FLAG.matcher(localValue).matches()) {
			LocalDateTime ldt = LocalDateTime.parse(localValue, withoudTFlag_ISO_LOCAL_DATE_TIME);
			return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
		}
		throw new IllegalStateException("Malformed date time string");
	}

	/**
	 * 
	 * @param strCompactDateTimeSec - 20111203101530
	 * @return
	 */
	public static Long asEpochSecondsFromCompactStr(String strCompactDateTimeSec) {
		return LocalDateTime.parse(strCompactDateTimeSec, compact_ISO_LOCAL_DATE_TIME).atZone(ZoneId.systemDefault())
				.toEpochSecond();
	}

	/**
	 * 
	 * @param strCompactDateTimeSec - 20111203101530
	 * @return
	 */
	public static Long asEpochMillisFromCompactStr(String strCompactDateTimeSec) {
		return asEpochSecondsFromCompactStr(strCompactDateTimeSec) * 1000;
	}

	/**
	 * 
	 * @param localValue - Local time string e.g. 2011-12-03T10:15:30
	 * @return epochSeconds
	 */
	public static Long asEpochSeconds(String localValue) {
		if (DATETIME_LOCAL.matcher(localValue).matches()) {
			LocalDateTime ldt = LocalDateTime.parse(localValue, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
			return ldt.atZone(ZoneId.systemDefault()).toEpochSecond();
		}
		throw new IllegalStateException("Malformed date time string");
	}

	/**
	 * 
	 * @param localValue - Local time string e.g. 2011-12-03T10:15:30
	 * @return epochSeconds
	 */
	public static Long asEpochMillis(String localValue) {
		return asEpochMillis(localValue) * 100;
	}

	/**
	 * 
	 * @param localValue - Local time string e.g. 2011-12-03 10:15:30
	 * @return epochSeconds
	 */
	public static Long asEpochSecondsWithoutTFlag(String localValue) {
		if (DATETIME_WITHOUT_T_FLAG.matcher(localValue).matches()) {
			LocalDateTime ldt = LocalDateTime.parse(localValue, withoudTFlag_ISO_LOCAL_DATE_TIME);
			return ldt.atZone(ZoneId.systemDefault()).toEpochSecond();
		}
		throw new IllegalStateException("Malformed date time string");
	}

	/**
	 * 
	 * @return - Local date string e.g. 20111203
	 */
	public static String currentDate() {
		return LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
	}

	/**
	 * 
	 * @return - Local date time string e.g. 2019-09-05T15:27:23
	 */
	public static String currentDateTime() {
		return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
	}

	/**
	 * 
	 * @return - Local date time compacted string e.g. 20190905152723
	 */
	public static String currentCompactDateTime() {
		return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
	}

	/**
	 * @param timeMillis - date time in millisecond
	 * @return - Local date string e.g. 20111203
	 */
	public static String strDate(Long timeMillis) {
		return DateTimeFormatter.BASIC_ISO_DATE
				.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(timeMillis), ZoneId.systemDefault()));
	}

	/**
	 * @param timeMillis - date time in second
	 * @return - Local date string e.g. 20111203
	 */
	public static String strDateSec(Long second) {
		return DateTimeFormatter.BASIC_ISO_DATE
				.format(LocalDateTime.ofInstant(Instant.ofEpochSecond(second), ZoneId.systemDefault()));
	}

	/**
	 * 
	 * @param timeMillis - date time in millisecond
	 * @return
	 */
	public static String strDateTime(Long timeMillis) {
		return DateTimeFormatter.ISO_LOCAL_DATE_TIME
				.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(timeMillis), ZoneId.systemDefault()));
	}

	/**
	 * 
	 * @param timeMillis - date time in second
	 * @return
	 */
	public static String strDateTimeSec(Long second) {
		return DateTimeFormatter.ISO_LOCAL_DATE_TIME
				.format(LocalDateTime.ofInstant(Instant.ofEpochSecond(second), ZoneId.systemDefault()));
	}

	/**
	 * 
	 * @param timeMillis - date time in millisecond
	 * @return - Local date time compacted string e.g. 20190905152723
	 */
	public static String strCompactDateTime(Long timeMillis) {
		return compact_ISO_LOCAL_DATE_TIME.format(LocalDateTime
				.ofInstant(Instant.ofEpochMilli(timeMillis), ZoneId.systemDefault()).truncatedTo(ChronoUnit.SECONDS));
	}

	/**
	 * 
	 * @param timeMillis - date time in second
	 * @return - Local date time compacted string e.g. 20190905152723
	 */
	public static String strCompactDateTimeSec(Long second) {
		return compact_ISO_LOCAL_DATE_TIME
				.format(LocalDateTime.ofInstant(Instant.ofEpochSecond(second), ZoneId.systemDefault()));
	}

	/**
	 * 
	 * @param zValue - UTC/GMT time string e.g. 2011-12-03T10:15:30Z
	 * @return
	 */
	public static Date zasDate(String zValue) {
		if (DATETIME_Z.matcher(zValue).matches()) {
			Instant instant = Instant.from(DateTimeFormatter.ISO_INSTANT.parse(zValue));
			return Date.from(instant);
		}
		throw new IllegalStateException("Malformed date time string");
	}

	/**
	 * 
	 * @return - UTC/GMT date string e.g. 20111203
	 */
	public static String zcurrentDate() {
		return LocalDate.now(ZONE_ID_GMT).format(DateTimeFormatter.BASIC_ISO_DATE);
	}

	/**
	 * 
	 * @return - UTC/GMT date time string e.g. 2019-09-05T07:48:10Z
	 */
	public static String zcurrentDateTime() {
		return DateTimeFormatter.ISO_INSTANT.format(Instant.now().truncatedTo(ChronoUnit.SECONDS));
	}

	/**
	 * 
	 * @return - UTC/GMT date time string e.g. 20190905074810Z
	 */
	public static String zcurrentCompactDateTime() {
		return zcurrentDateTime().replaceAll("-", "").replaceAll("T", "").replaceAll(":", "");
	}

	/**
	 * @param timeMillis - date time in millisecond
	 * @return - UTC/GMT date string e.g. 20111203
	 */
	public static String zstrDate(Long timeMillis) {
		return LocalDateTime.ofInstant(Instant.ofEpochMilli(timeMillis), ZONE_ID_GMT)
				.format(DateTimeFormatter.BASIC_ISO_DATE);
	}

	/**
	 * 
	 * @param timeMillis - date time in millisecond
	 * @return - UTC/GMT date time string e.g. 2019-09-05T07:48:10Z
	 */
	public static String zstrDateTime(Long timeMillis) {
		return DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochMilli(timeMillis).truncatedTo(ChronoUnit.SECONDS));
	}

	/**
	 * 
	 * @param timeMillis - date time in second
	 * @return - UTC/GMT date time string e.g. 2019-09-05T07:48:10Z
	 */
	public static String zstrDateTimeSec(Long second) {
		return DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochSecond(second));
	}

	/**
	 * 
	 * @param timeMillis - date time in millisecond
	 * @return - UTC/GMT date time compacted string e.g. 20190905152723Z
	 */
	public static String zstrCompactDateTime(Long timeMillis) {
		return zstrCompactDateTime(timeMillis).replaceAll("-", "").replaceAll("T", "").replaceAll(":", "");
	}

	/**
	 * 
	 * @param timeMillis - date time in second
	 * @return - UTC/GMT date time compacted string e.g. 20190905152723Z
	 */
	public static String zstrCompactDateTimeSec(Long second) {
		return zstrDateTimeSec(second).replaceAll("-", "").replaceAll("T", "").replaceAll(":", "");
	}

}

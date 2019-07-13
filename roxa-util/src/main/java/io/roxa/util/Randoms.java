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

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.UUID;

import org.bouncycastle.util.encoders.Hex;

import com.fasterxml.uuid.EthernetAddress;
import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.RandomBasedGenerator;
import com.fasterxml.uuid.impl.TimeBasedGenerator;

/**
 * @author Steven Chen
 *
 */
public abstract class Randoms {

	private static final String randomStrBase = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
	private static final String randomNumBase = "0123456789";
	private static EthernetAddress nic = EthernetAddress.fromInterface();
	private static TimeBasedGenerator uuidGenerator = Generators.timeBasedGenerator(nic);

	public static void main(String[] args) {
		System.out.printf("%s%n", randomHexString(16));
	}

	public static String randomNumber(int len) {
		Random random = new Random();
		char[] buf = new char[len];
		for (int i = 0; i < len; i++) {
			int n = random.nextInt(randomNumBase.length());
			buf[i] = randomNumBase.charAt(n);
		}
		return new String(buf);
	}

	public static String randomString(int len) {
		Random random = new Random();
		char[] buf = new char[len];
		for (int i = 0; i < len; i++) {
			int n = random.nextInt(randomStrBase.length());
			buf[i] = randomStrBase.charAt(n);
		}
		return new String(buf);
	}

	public static String randomHexString(int len) {
		SecureRandom random = new SecureRandom();
		byte[] bytes = new byte[len];
		random.nextBytes(bytes);
		return Hex.toHexString(bytes);
	}

	public static String generateSequenceNumber(int randomNumberLen, String... prefix) {
		String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssnnnnnnnnn"));
		if (randomNumberLen > 4)
			randomNumberLen = 4;
		if (randomNumberLen < 0)
			randomNumberLen = 1;
		String s1 = timestamp.substring(0, timestamp.length() - randomNumberLen);
		String s2 = randomNumber(randomNumberLen);
		String result = String.format("%s%s%s", prefix == null ? "" : String.join("", prefix), s1, s2);
		return result + checksum(result);
	}

	public static String generateUUID() {
		UUID uuid = uuidGenerator.generate();
		return uuid.toString().replaceAll("-", "");
	}

	public static String randomBasedUUID() {
		RandomBasedGenerator uuidGenerator = Generators.randomBasedGenerator();
		UUID uuid = uuidGenerator.generate();
		return uuid.toString();
	}

	public static String checksum(String numberStr) {
		int len = numberStr.length();
		int result = 0;
		for (int i = 0; i < len; i += 2) {
			result += (numberStr.charAt(i) ^ '0');
		}
		for (int i = 1; i < len; i += 2) {
			result += (numberStr.charAt(i) ^ '0') * 3;
		}
		return String.valueOf((10 - (result % 10)) % 10);
	}
}

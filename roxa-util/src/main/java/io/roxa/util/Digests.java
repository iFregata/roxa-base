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

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author Steven Chen
 *
 */
public abstract class Digests {
	private static final char[] DIGITS_LOWER = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd',
			'e', 'f' };
	private static final String HMAC_SHA1 = "HmacSHA1";
	private static final String HMAC_SHA256 = "HmacSHA256";
	private static final String CHARSET_UTF8 = "UTF-8";

	/**
	 * HmacSHA1 内容签名, Base64
	 * 
	 * @param appSecretKey - 签名Key
	 * @param content      - 需签名的内容
	 * @return - Base64 encoded 签名
	 */
	public static String digestAsBase64(String appSecretKey, String content) {
		return Base64.getUrlEncoder().encodeToString(digest(appSecretKey, content));
	}

	/**
	 * HmacSHA1 内容签名, Hex
	 * 
	 * @param appSecretKey - 签名Key
	 * @param content      - 需签名的内容
	 * @return - Hex encoded 签名
	 */
	public static String digestAsHex(String appSecretKey, String content) {
		return asHex(digest(appSecretKey, content));
	}

	/**
	 * HmacSHA1 内容签名验证, Base64
	 * 
	 * @param appSecret    - Base64 encoded 签名
	 * @param appSecretKey - 签名Key
	 * @param content      - 签名的内容
	 * @return
	 */
	public static boolean digestVerifyBase64(String appSecret, String appSecretKey, String content) {
		byte[] expectedBytes = digest(appSecretKey, content);
		byte[] actualBytpes = Base64.getUrlDecoder().decode(appSecret);
		return MessageDigest.isEqual(expectedBytes, actualBytpes);

	}

	/**
	 * HmacSHA1 内容签名验证, Hex
	 * 
	 * @param appSecret    - Hex encoded 签名
	 * @param appSecretKey - 签名Key
	 * @param content      - 签名的内容
	 * @return
	 */
	public static boolean digestVerifyHex(String appSecret, String appSecretKey, String content) {
		byte[] expectedBytes = digest(appSecretKey, content);
		byte[] actualBytpes = asBytes((appSecret));
		return MessageDigest.isEqual(expectedBytes, actualBytpes);

	}

	/**
	 * MD5 digest
	 * 
	 * @param content
	 * @return - Hex encoded
	 */
	public static String digestMD5(String content) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			return asHex(md.digest(content.getBytes(CHARSET_UTF8)));
		} catch (GeneralSecurityException | UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * HmacSHA256 with plain key
	 * 
	 * @param appSecretKey
	 * @param content
	 * @return
	 */
	public static byte[] digestHmacSHA256PlainKey(String appSecretKey, String content) {
		try {
			Mac mac = Mac.getInstance(HMAC_SHA256);
			byte[] keyBytes = appSecretKey.getBytes();
			SecretKeySpec keySpec = new SecretKeySpec(keyBytes, HMAC_SHA256);
			mac.init(keySpec);
			return mac.doFinal(content.getBytes(CHARSET_UTF8));
		} catch (GeneralSecurityException | UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * HmacSHA256 with Hex key
	 * 
	 * @param appSecretKey
	 * @param content
	 * @return
	 */
	public static byte[] digestHmacSHA256(String appSecretKey, String content) {
		try {
			Mac mac = Mac.getInstance(HMAC_SHA256);
			byte[] keyBytes = asBytes(appSecretKey);
			SecretKeySpec keySpec = new SecretKeySpec(keyBytes, HMAC_SHA256);
			mac.init(keySpec);
			return mac.doFinal(content.getBytes(CHARSET_UTF8));
		} catch (GeneralSecurityException | UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * HmacSHA256 with bytes key
	 * 
	 * @param appSecretKey
	 * @param content
	 * @return
	 */
	public static byte[] digestHmacSHA256(byte[] appSecretKey, String content) {
		try {
			Mac mac = Mac.getInstance(HMAC_SHA256);
			SecretKeySpec keySpec = new SecretKeySpec(appSecretKey, HMAC_SHA256);
			mac.init(keySpec);
			return mac.doFinal(content.getBytes(CHARSET_UTF8));
		} catch (GeneralSecurityException | UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * HmacSHA1 with plain key
	 * 
	 * @param appSecretKey
	 * @param content
	 * @return
	 */
	public static byte[] digestPlainKey(String appSecretKey, String content) {
		try {
			Mac mac = Mac.getInstance(HMAC_SHA1);
			byte[] keyBytes = appSecretKey.getBytes();
			SecretKeySpec keySpec = new SecretKeySpec(keyBytes, HMAC_SHA1);
			mac.init(keySpec);
			return mac.doFinal(content.getBytes(CHARSET_UTF8));
		} catch (GeneralSecurityException | UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * HmacSHA1 with Hex key
	 * 
	 * @param appSecretKey
	 * @param content
	 * @return
	 */
	public static byte[] digest(String appSecretKey, String content) {
		try {
			Mac mac = Mac.getInstance(HMAC_SHA1);
			byte[] keyBytes = asBytes(appSecretKey);
			SecretKeySpec keySpec = new SecretKeySpec(keyBytes, HMAC_SHA1);
			mac.init(keySpec);
			return mac.doFinal(content.getBytes(CHARSET_UTF8));
		} catch (GeneralSecurityException | UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public static byte[] digest(byte[] appSecretKey, String content) {
		try {
			Mac mac = Mac.getInstance(HMAC_SHA1);
			SecretKeySpec keySpec = new SecretKeySpec(appSecretKey, HMAC_SHA1);
			mac.init(keySpec);
			return mac.doFinal(content.getBytes(CHARSET_UTF8));
		} catch (GeneralSecurityException | UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public static String asHex(final byte[] data) {
		final int l = data.length;
		final char[] out = new char[l << 1];
		for (int i = 0, j = 0; i < l; i++) {
			out[j++] = DIGITS_LOWER[(0xF0 & data[i]) >>> 4];
			out[j++] = DIGITS_LOWER[0x0F & data[i]];
		}
		return new String(out);
	}

	protected static byte[] asBytes(String hexStr) {
		char[] data = hexStr.toCharArray();
		final int len = data.length;
		if ((len & 0x01) != 0)
			throw new RuntimeException("Odd number of characters.");
		final byte[] out = new byte[len >> 1];
		for (int i = 0, j = 0; j < len; i++) {
			int f = toDigit(data[j], j) << 4;
			j++;
			f = f | toDigit(data[j], j);
			j++;
			out[i] = (byte) (f & 0xFF);
		}
		return out;
	}

	protected static int toDigit(final char ch, final int index) {
		final int digit = Character.digit(ch, 16);
		if (digit == -1) {
			throw new RuntimeException("Illegal hexadecimal character " + ch + " at index " + index);
		}
		return digit;
	}
}

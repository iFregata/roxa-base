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
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Base64;

import org.apache.commons.codec.digest.DigestUtils;
import org.bouncycastle.util.encoders.Hex;

/**
 * @author Steven Chen
 *
 */
public abstract class Codecs {

	/**
	 * 
	 * @param hexStr
	 * @return
	 */
	public static byte[] hexBytes(String hexStr) {
		return Hex.decode(hexStr);
	}

	public static String asBase64URLSafeString(byte[] original) {

		return Base64.getUrlEncoder().encodeToString(original);

	}

	public static String asBase64URLSafeString(String original) {
		return asBase64URLSafeString(original.getBytes());
	}

	public static String base64URLSafeAsString(String original) {
		return new String(base64URLSafeAsBytes(original));
	}

	public static byte[] base64URLSafeAsBytes(String original) {
		return Base64.getUrlDecoder().decode(original);
	}

	public static String base64AsString(String original) {
		return new String(base64AsBytes(original));
	}

	public static byte[] base64AsBytes(String original) {
		return Base64.getDecoder().decode(original);
	}

	public static String asBase64(String original) {
		return asBase64(original.getBytes());
	}

	public static String asBase64(byte[] bytes) {
		return Base64.getEncoder().encodeToString(bytes);

	}

	public static String asMD5String(String str) {
		return DigestUtils.md5Hex(str);
	}

	// 160bit
	public static String asSHA1String(String original) {
		return DigestUtils.sha1Hex(original);
	}

	public static byte[] sha1(byte[] content) {
		return DigestUtils.sha1(content);
	}

	public static String asSHA1String(byte[] bytes) {
		return DigestUtils.sha1Hex(bytes);
	}

	public static String asSHA256String(String original) {
		return DigestUtils.sha256Hex(original);
	}

	public static byte[] sha256(byte[] content) {
		return DigestUtils.sha256(content);
	}

	public static String asSHA256String(byte[] bytes) {
		return DigestUtils.sha256Hex(bytes);
	}

	public static String asSHA512String(String original) {
		return DigestUtils.sha512Hex(original);
	}

	public static byte[] sha512(byte[] content) {
		return DigestUtils.sha512(content);
	}

	public static String asSHA512String(byte[] bytes) {
		return DigestUtils.sha512Hex(bytes);
	}

	public static String urlEncode(String str) {
		try {
			String _value = Strings.emptyAsNull(str);
			if (_value == null)
				return "";
			_value = URLEncoder.encode(str, "UTF-8");
			return _value;
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public static String urlDecode(String str) {
		try {
			String _value = Strings.emptyAsNull(str);
			if (_value == null)
				return "";
			_value = URLDecoder.decode(str, "UTF-8");
			return _value;
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

}

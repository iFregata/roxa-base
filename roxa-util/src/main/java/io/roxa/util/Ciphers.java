/**
 * The MIT License
 * 
 * Copyright (c) 2016 Shell Technologies PTY LTD
 *
 * You may obtain a copy of the License at
 * 
 *       http://mit-license.org/
 *       
 */
package io.roxa.util;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.util.Base64;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

/**
 * @author steven
 *
 */
public abstract class Ciphers {

	private static final String randomStrBase = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
	private final String algorithm;
	private String mode = "CBC";
	private String padding = "PKCS5Padding";
	private Key secretKey;
	private IvParameterSpec ivSpec;
	private byte[] content;
	private Cipher cipher;
	private boolean encrypt = true;
	private final Base64.Decoder base64Decoder;
	private final Base64.Encoder base64Encoder;
	private String ivSpecString;

	public static Ciphers createDESede() {
		return new DESedeCipher();
	}

	public static Ciphers createAES() {
		return new AESCipler();
	}

	public static Ciphers createDESedeURLSafe() {
		return new DESedeCipher(true);
	}

	public static Ciphers createAESURLSafe() {
		return new AESCipler(true);
	}

	public Ciphers(boolean urlSafe, String algorithm) {
		this.algorithm = algorithm;
		if (urlSafe) {
			base64Decoder = Base64.getUrlDecoder();
			base64Encoder = Base64.getUrlEncoder();
		} else {
			base64Decoder = Base64.getDecoder();
			base64Encoder = Base64.getEncoder();
		}
	}

	public Ciphers cbc() {
		return mode("CBC");
	}

	public Ciphers pkcs5() {
		return padding("PKCS5Padding");
	}

	public Ciphers pkcs7() {
		return padding("PKCS7Padding");
	}

	public Ciphers mode(String feedback) {
		this.mode = feedback;
		return this;
	}

	public Ciphers padding(String padding) {
		this.padding = padding;
		return this;
	}

	abstract protected Key getSecretKey(byte[] keyBytes) throws GeneralSecurityException;

	public Ciphers base64Key(String base64Key) {
		try {
			byte[] keyBytes = base64Decoder.decode(base64Key);
			secretKey = getSecretKey(keyBytes);
			return this;
		} catch (GeneralSecurityException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 
	 * @param key - DESede Key size: 3 * 8bytes, AES key size: 16bytes
	 * @return
	 */
	public Ciphers key(String key) {
		try {
			byte[] keyBytes = key.getBytes();
			secretKey = getSecretKey(keyBytes);
			return this;
		} catch (GeneralSecurityException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 
	 * @param ivString - DESede Iv size: 8bytes, AES Iv size: 16bytes
	 * @return
	 */
	public Ciphers plainIV(String ivString) {
		this.ivSpecString = ivString;
		byte[] ivBytes = ivSpecString.getBytes();
		ivSpec = new IvParameterSpec(ivBytes);
		return this;
	}

	public String ivString() {
		if (ivSpec == null) {
			ivSpecString = randomString(8);
			byte[] ivBytes = generateIVBytes(ivSpecString);
			ivSpec = new IvParameterSpec(ivBytes);
		}
		return ivSpecString;
	}

	public Ciphers base64IV(String base64IV) {
		byte[] ivBytes = base64Decoder.decode(base64IV);
		ivSpec = new IvParameterSpec(ivBytes);
		return this;
	}

	public Ciphers content(String content, boolean encrypted, String charset) {
		encrypt = !encrypted;
		try {
			if (encrypt)
				this.content = content.getBytes(charset);
			else
				this.content = base64Decoder.decode(content.getBytes(charset));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		return this;
	}

	public Ciphers content(String content, boolean encrypted) {
		return content(content, encrypted, "UTF-8");
	}

	public String[] doFinal() {
		try {
			Cipher cipher = getCipher();
			String ivStr = ivString();
			cipher.init(encrypt ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE, secretKey, ivSpec);
			byte[] result = cipher.doFinal(content);
			String resultStr = encrypt ? base64Encoder.encodeToString(result) : new String(result);
			return new String[] { resultStr, ivStr };
		} catch (GeneralSecurityException e) {
			throw new RuntimeException(e);
		}
	}

	protected Cipher getCipher() throws GeneralSecurityException {
		if (cipher == null)
			cipher = Cipher.getInstance(String.join("/", algorithm, mode, padding));
		return cipher;
	}

	public static String generateKeyBase64URLSafe(String algorithm) throws GeneralSecurityException {
		return Base64.getUrlEncoder().encodeToString(generateKey(algorithm));
	}

	public static String randomIVBytesBase64URLSafe(int len) {
		return Base64.getUrlEncoder().encodeToString(randomIVBytes(len));
	}

	public static String generateKeyBase64(String algorithm) throws GeneralSecurityException {
		return Base64.getEncoder().encodeToString(generateKey(algorithm));
	}

	public static String randomIVBytesBase64(int len) {
		return Base64.getEncoder().encodeToString(randomIVBytes(len));
	}

	public static byte[] generateKey(String algorithm) throws GeneralSecurityException {
		KeyGenerator keyGenerator = KeyGenerator.getInstance(algorithm);
		keyGenerator.init(168);
		SecretKey skey = keyGenerator.generateKey();
		return skey.getEncoded();
	}

	public static byte[] randomIVBytes(int len) {
		byte[] ivBytes = new byte[len];
		Random r = new Random();
		r.nextBytes(ivBytes);
		return ivBytes;
	}

	public static byte[] generateIVBytes(String code) {
		byte[] result = new byte[code.length()];
		for (int i = 0; i < result.length; i++) {
			result[i] = (byte) (code.charAt(i) - 48);
		}
		return result;
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

	protected static void sample() throws Exception {
		String key = "RaUtxM2ecl1AW86ByEi9v1nG3eQdieb9";
		String aesKey = "RaUtxM2ecl1AW86B";
		String iv16 = "VLeZE6bjfLS9qmsb";
		String iv8 = "nOmdJDsw";
		String plainText = "00000000";
		String[] rs = Ciphers.createDESede().key(key).plainIV(iv8).content(plainText, false).doFinal();
		System.out.println("DESede Encrypted: " + rs[0]);
		rs = Ciphers.createDESede().key(key).plainIV(iv8).content(rs[0], true).doFinal();
		System.out.println("DESede Decrypted: " + rs[0]);

		rs = Ciphers.createAES().key(aesKey).plainIV(iv16).content(plainText, false).doFinal();
		System.out.println("AES Encrypted: " + rs[0]);
		rs = Ciphers.createAES().key(aesKey).plainIV(iv16).content(rs[0], true).doFinal();
		System.out.println("AES Decrypted: " + rs[0]);

		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		int blockSize = cipher.getBlockSize();
		System.out.println(blockSize);
	}
}

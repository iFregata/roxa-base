/**
 * Copyright (c) 2011-2014 SC Abacus, Inc
 * The MIT License (MIT)
 */
package io.roxa.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Enumeration;

import javax.crypto.Cipher;

/**
 * @author Steven Chen
 *
 */
public abstract class RSAs {

	public static final String ALGORITHM_RSA = "RSA";

	public static final String SIGN_ALGORITHMS_RSA_SHA1 = "SHA1withRSA";

	public static final String SIGN_ALGORITHMS_RSA_SHA2 = "SHA256withRSA";

	static {
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
	}

	/**
	 * Algorithm SHA1WithRSA
	 * 
	 * @param publicKeyAsBase64
	 *            - X509 encoded public key as base64 URL safe
	 * @param signatureAsBase64URLSafe
	 *            - signature string as base64 URL safe
	 * @param signedContent
	 * @param charset
	 * @return true - if verified
	 * @throws GeneralSecurityException
	 */
	public static boolean verifySignSHA1Base64(String publicKeyAsBase64, String signatureAsBase64, String signedContent,
			String charset) throws GeneralSecurityException {
		PublicKey publicKey = publicKeyFromBase64(publicKeyAsBase64);
		return verifySignSHA1Base64(publicKey, signatureAsBase64, signedContent, charset);
	}

	public static boolean verifySignSHA2Base64(String publicKeyAsBase64, String signatureAsBase64, String signedContent,
			String charset) throws GeneralSecurityException {
		PublicKey publicKey = publicKeyFromBase64(publicKeyAsBase64);
		return verifySignSHA2Base64(publicKey, signatureAsBase64, signedContent, charset);
	}

	/**
	 * Algorithm SHA1WithRSA
	 * 
	 * @param publicKey
	 *            - X509 encoded public key
	 * @param signatureAsBase64URLSafe
	 *            - signature string as base64 URL safe
	 * @param signedContent
	 * @param charset
	 * @return
	 * @throws GeneralSecurityException
	 */
	public static boolean verifySignSHA1Base64(PublicKey publicKey, String signatureAsBase64, String signedContent,
			String charset) throws GeneralSecurityException {
		return verifySignSHA1(publicKey, Codecs.base64AsBytes(signatureAsBase64), signedContent, charset);
	}

	public static boolean verifySignSHA2Base64(PublicKey publicKey, String signatureAsBase64, String signedContent,
			String charset) throws GeneralSecurityException {
		return verifySignSHA2(publicKey, Codecs.base64AsBytes(signatureAsBase64), signedContent, charset);
	}

	/**
	 * Algorithm SHA1WithRSA
	 * 
	 * @param publicKey-
	 *            X509 encoded public key
	 * @param signatureMark
	 * @param signedContent
	 * @param charset
	 * @return
	 * @throws GeneralSecurityException
	 */
	public static boolean verifySignSHA1(PublicKey publicKey, byte[] signatureMark, String signedContent,
			String charset) throws GeneralSecurityException {
		try {
			Signature signature = Signature.getInstance(SIGN_ALGORITHMS_RSA_SHA1);
			signature.initVerify(publicKey);
			if (Strings.emptyAsNull(charset) == null) {
				signature.update(signedContent.getBytes());
			} else {
				signature.update(signedContent.getBytes(charset));
			}
			return signature.verify(signatureMark);
		} catch (NoSuchAlgorithmException | IOException | InvalidKeyException | SignatureException e) {
			throw new GeneralSecurityException(e);
		}
	}

	public static boolean verifySignSHA2(PublicKey publicKey, byte[] signatureMark, String signedContent,
			String charset) throws GeneralSecurityException {
		try {
			Signature signature = Signature.getInstance(SIGN_ALGORITHMS_RSA_SHA2);
			signature.initVerify(publicKey);
			if (Strings.emptyAsNull(charset) == null) {
				signature.update(signedContent.getBytes());
			} else {
				signature.update(signedContent.getBytes(charset));
			}
			return signature.verify(signatureMark);
		} catch (NoSuchAlgorithmException | IOException | InvalidKeyException | SignatureException e) {
			throw new GeneralSecurityException(e);
		}
	}

	public static String signSHA1Base64(String privateKeyAsBase64, String signedContent, String charset)
			throws GeneralSecurityException {
		PrivateKey privateKey = privateKeyFromBase64(privateKeyAsBase64);
		return signSHA1Base64(privateKey, signedContent, charset);
	}

	public static String signSHA2Base64(String privateKeyAsBase64, String signedContent, String charset)
			throws GeneralSecurityException {
		PrivateKey privateKey = privateKeyFromBase64(privateKeyAsBase64);
		return signSHA2Base64(privateKey, signedContent, charset);
	}

	public static String signSHA1Base64(PrivateKey privateKey, String signedContent, String charset)
			throws GeneralSecurityException {
		byte[] signed = signSHA1(privateKey, signedContent, charset);
		return Codecs.asBase64(signed);
	}

	public static String signSHA2Base64(PrivateKey privateKey, String signedContent, String charset)
			throws GeneralSecurityException {
		byte[] signed = signSHA2(privateKey, signedContent, charset);
		return Codecs.asBase64(signed);
	}

	/**
	 * 
	 * @param privateKeyAsBase64
	 *            - PKCS8 Encoded Key private key as base 64
	 * @param signedContent
	 * @param charset
	 * @return - signature as base 64 URL safe
	 * @throws GeneralSecurityException
	 */
	public static String signSHA1Base64URLSafe(String privateKeyAsBase64, String signedContent, String charset)
			throws GeneralSecurityException {
		PrivateKey privateKey = privateKeyFromBase64(privateKeyAsBase64);
		return signSHA1Base64URLSafe(privateKey, signedContent, charset);
	}

	public static String signSHA2Base64URLSafe(String privateKeyAsBase64, String signedContent, String charset)
			throws GeneralSecurityException {
		PrivateKey privateKey = privateKeyFromBase64(privateKeyAsBase64);
		return signSHA2Base64URLSafe(privateKey, signedContent, charset);
	}

	/**
	 * 
	 * @param privateKey
	 *            - PKCS8 Encoded Key private key
	 * @param signedContent
	 * @param charset
	 * @return
	 * @throws GeneralSecurityException
	 */
	public static String signSHA1Base64URLSafe(PrivateKey privateKey, String signedContent, String charset)
			throws GeneralSecurityException {
		byte[] signed = signSHA1(privateKey, signedContent, charset);
		return Codecs.asBase64URLSafeString(signed);
	}

	public static String signSHA2Base64URLSafe(PrivateKey privateKey, String signedContent, String charset)
			throws GeneralSecurityException {
		byte[] signed = signSHA2(privateKey, signedContent, charset);
		return Codecs.asBase64URLSafeString(signed);
	}

	/**
	 * 
	 * @param privateKey
	 *            - PKCS8 Encoded Key private key
	 * @param signedContent
	 * @param charset
	 * @return
	 * @throws GeneralSecurityException
	 */
	public static byte[] signSHA1(PrivateKey privateKey, String signedContent, String charset)
			throws GeneralSecurityException {
		try {
			Signature signature = java.security.Signature.getInstance(SIGN_ALGORITHMS_RSA_SHA1);
			signature.initSign(privateKey);
			if (Strings.emptyAsNull(charset) == null) {
				signature.update(signedContent.getBytes());
			} else {
				signature.update(signedContent.getBytes(charset));
			}
			return signature.sign();
		} catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException | IOException e) {
			throw new GeneralSecurityException(e);
		}
	}

	public static byte[] signSHA2(PrivateKey privateKey, String signedContent, String charset)
			throws GeneralSecurityException {
		try {
			Signature signature = java.security.Signature.getInstance(SIGN_ALGORITHMS_RSA_SHA2);
			signature.initSign(privateKey);
			if (Strings.emptyAsNull(charset) == null) {
				signature.update(signedContent.getBytes());
			} else {
				signature.update(signedContent.getBytes(charset));
			}
			return signature.sign();
		} catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException | IOException e) {
			throw new GeneralSecurityException(e);
		}
	}

	public static String[] keyPairAsBase64Pair(KeyPair keyPair) {
		PublicKey pubKey = keyPair.getPublic();
		PrivateKey priKey = keyPair.getPrivate();
		return new String[] { Codecs.asBase64(pubKey.getEncoded()), Codecs.asBase64(priKey.getEncoded()) };
	}

	public static String[] keyPairAsBase64URLSafePair(KeyPair keyPair) {
		PublicKey pubKey = keyPair.getPublic();
		PrivateKey priKey = keyPair.getPrivate();
		return new String[] { Codecs.asBase64URLSafeString(pubKey.getEncoded()),
				Codecs.asBase64URLSafeString(priKey.getEncoded()) };
	}

	public static String keyAsBase64(Key key) {
		return Codecs.asBase64(key.getEncoded());
	}

	public static String keyAsBase64URLSafe(Key key) {
		return Codecs.asBase64URLSafeString(key.getEncoded());
	}

	public static String encryptAsBase64(String keyAsBase64, String content) throws GeneralSecurityException {
		return encryptAsBase64(privateKeyFromBase64(keyAsBase64), content);
	}

	public static String encryptAsBase64URLSafe(String keyAsBase64, String content) throws GeneralSecurityException {
		return encryptAsBase64URLSafe(privateKeyFromBase64(keyAsBase64), content);
	}

	public static String encryptAsBase64(Key key, String content) throws GeneralSecurityException {
		byte[] encrypted = encrypt(key, content);
		return Codecs.asBase64(encrypted);
	}

	public static String encryptAsBase64URLSafe(Key key, String content) throws GeneralSecurityException {
		byte[] encrypted = encrypt(key, content);
		return Codecs.asBase64URLSafeString(encrypted);
	}

	public static byte[] encrypt(Key key, String content) throws GeneralSecurityException {
		Cipher c = Cipher.getInstance(ALGORITHM_RSA);
		c.init(Cipher.ENCRYPT_MODE, key);// aa
		return c.doFinal(content.getBytes());
	}

	public static String decrytFromBase64(Key key, String content) throws GeneralSecurityException {
		byte[] encryptedContent = Codecs.base64AsBytes(content);
		return decryt(key, encryptedContent);
	}

	public static String decryt(Key key, byte[] encryptedContent) throws GeneralSecurityException {
		Cipher c = Cipher.getInstance(ALGORITHM_RSA);
		c.init(Cipher.DECRYPT_MODE, key);
		byte[] decrypted = c.doFinal(encryptedContent);
		return new String(decrypted);
	}

	public static PrivateKey privateKeyFromBase64(String keyAsBase64) throws GeneralSecurityException {
		KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM_RSA);
		byte[] keyByte = Codecs.base64AsBytes(keyAsBase64);
		return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(keyByte));
	}

	public static PublicKey publicKeyFromBase64(String keyAsBase64) throws GeneralSecurityException {
		KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM_RSA);
		byte[] keyByte = Codecs.base64AsBytes(keyAsBase64);
		return keyFactory.generatePublic(new X509EncodedKeySpec(keyByte));
	}

	public static KeyPair loadKeyPairFromPFXFile(String pfxFileLocation, String password, String alias)
			throws GeneralSecurityException, IOException {
		KeyStore keyStore = KeyStore.getInstance("PKCS12", "BC");
		try (InputStream in = Files.newInputStream(Paths.get(pfxFileLocation))) {
			keyStore.load(in, password.toCharArray());
		}
		String _alias = Strings.emptyAsNull(alias);
		if (_alias == null) {
			Enumeration<String> aliases = keyStore.aliases();
			if (aliases.hasMoreElements()) {
				_alias = aliases.nextElement();
			}
		}
		if (_alias == null)
			throw new GeneralSecurityException("No alias found!");
		Certificate cert = keyStore.getCertificate(_alias);
		PublicKey pubKey = cert.getPublicKey();
		PrivateKey priKey = (PrivateKey) keyStore.getKey(_alias, password.toCharArray());
		return new KeyPair(pubKey, priKey);
	}
}

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

import java.security.GeneralSecurityException;
import java.security.Key;

import javax.crypto.spec.SecretKeySpec;

/**
 * @author Steven Chen
 * 
 *         IV: 128bit 16bytes
 *
 */
public class AESCipler extends Ciphers {

	public AESCipler() {
		super(false, "AES");
	}

	public AESCipler(boolean urlSafe) {
		super(urlSafe, "AES");
	}

	@Override
	protected Key getSecretKey(byte[] keyBytes) throws GeneralSecurityException {
		return new SecretKeySpec(keyBytes, "AES");
	}

}

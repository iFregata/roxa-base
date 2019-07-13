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

import java.security.GeneralSecurityException;
import java.security.Key;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;

/**
 * @author steven
 * 
 *         Key size: 24bytes
 *
 */
public class DESedeCipher extends Ciphers {

	public DESedeCipher() {
		super(false, "DESede");
	}

	public DESedeCipher(boolean urlSafe) {
		super(urlSafe, "DESede");
	}

	@Override
	protected Key getSecretKey(byte[] keyBytes) throws GeneralSecurityException {
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DESede");
		return keyFactory.generateSecret(new DESedeKeySpec(keyBytes));
	}

}

package com.infa.data.crypt;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class AESencrp {

	private static final String ALGO = "AES";

	public static String encrypt(String key, String Data) throws Exception {
		Key gKey = generateKey(key.getBytes());
		Cipher c = Cipher.getInstance(ALGO);
		c.init(Cipher.ENCRYPT_MODE, gKey);
		byte[] encVal = c.doFinal(Data.getBytes());
		String encryptedValue = new BASE64Encoder().encode(encVal);
		return encryptedValue;
	}

	public static String decrypt(String key, String encryptedData)
			throws Exception {
		Key gKey = generateKey(key.getBytes());
		Cipher c = Cipher.getInstance(ALGO);
		c.init(Cipher.DECRYPT_MODE, gKey);
		byte[] decordedValue = new BASE64Decoder().decodeBuffer(encryptedData);
		byte[] decValue = c.doFinal(decordedValue);
		String decryptedValue = new String(decValue,"UTF-8");
		return decryptedValue;
	}

	private static Key generateKey(byte[] keyValue) throws Exception {
		Key key = new SecretKeySpec(keyValue, ALGO);
		return key;
	}

	public static String base64Key(String key) throws Exception {
		return new BASE64Encoder().encode(key.getBytes());
	}

}
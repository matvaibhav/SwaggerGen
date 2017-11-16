package com.infa.data.crypt;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class KeyTest {

	public static void main(String[] args) throws Exception {
//		RndReplace Char;
		//		"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";
		int i = 0;
		String t = String.format("%05d", 1);
		Checksum cs = new CRC32();
		cs.update(t.getBytes(), 0, t.length());
		t = String.format("%16d", cs.getValue());
		t = t.replace(' ', '9');
		System.out.println(cs.getValue());
		System.out.println(t);
		System.out.println(AESencrp.base64Key(t));
		System.out.println(AESencrp.encrypt(t, "s"));
		System.out.println(AESencrp.decrypt(t, "fq5xrqcKJ3UQtu3JZ+7V2g=="));
	}

}

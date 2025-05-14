package com.zywl.app.base.util;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;



public class HexUtil {
	
	public static String str2Hex(String str){
		/*StringBuilder code = new StringBuilder();
		for(int i = 0 ; i < str.length();i++){
			code.append(Integer.toHexString((int)str.charAt(i)));
		}
		return code.toString();*/
		return Hex.encodeHexString(str.getBytes());
	}
	
	public static String hex2Str(String code){
		/*String str = "0123456789ABCDEF";
		char[] hexs = code.toCharArray();
	    byte[] bytes = new byte[code.length() / 2];
	    int n;
	    for (int i = 0; i < bytes.length; i++) {
	        n = str.indexOf(hexs[2 * i]) * 16;
	        n += str.indexOf(hexs[2 * i + 1]);
	        bytes[i] = (byte) (n & 0xff);
	    }
	    return new String(bytes);*/
		try {
			return new String(Hex.decodeHex(code));
		} catch (DecoderException e) {
			return null;
		}
	}
	
	public static void main(String[] args) {
		String code = str2Hex("elder away ocean catalog student gallery sound ticket useful genius wife monitor".replaceAll(" ", ""));
		System.out.println(code);
		System.out.println(hex2Str(code));
	}
}

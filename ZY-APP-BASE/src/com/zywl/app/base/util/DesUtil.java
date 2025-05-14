package com.zywl.app.base.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.io.IOException;
import java.net.URLDecoder;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 对称算法加密解密工具类
 * 
 * @author FXBTG Doe.
 * 
 */
public class DesUtil {

	protected final static String DES = "DES";

	protected final static String CIPHER_INSTANCE = "DES/ECB/PKCS5Padding";
	
	protected final static String ENCODE = "UTF-8";

	/**
	 * 加密
	 * 
	 * @author Doe.
	 * @param data 需要加密的数据
	 * @param key 加密需要的key
	 * @return 返回加密后的数据
	 * @throws Exception
	 */
	public static String encrypt(String data, String key) throws Exception {
		return Base64.getEncoder().encodeToString(encrypt(data.getBytes(ENCODE), key.getBytes(ENCODE)));
	}

	/**
	 * 解密
	 * 
	 * @author Doe.
	 * @param data 需要解密的数据
	 * @param key 解密需要的Key
	 * @return 返回解密后的数据
	 * @throws IOException
	 * @throws Exception
	 */
	public static String decrypt(String data, String key) throws IOException, Exception {
		if (data == null) {
			return null;
		}
		byte[] buf = org.apache.commons.codec.binary.Base64.decodeBase64(data);
//		byte[] buf = Base64.getMimeDecoder().decode(data);
		byte[] bt = decrypt(buf, key.getBytes(ENCODE));
		return new String(bt, ENCODE);
	}

	/**
	 * 加密
	 * 
	 * @author Doe.
	 * @param data 加密的byte数据
	 * @param 加密需要的byte类型的key
	 * @return 返回加密后的byte数据
	 * @throws Exception
	 */
	protected static byte[] encrypt(byte[] data, byte[] key) throws Exception {
		SecureRandom sr = new SecureRandom();
		sr.setSeed(key);

		DESKeySpec dks = new DESKeySpec(key);
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
		SecretKey securekey = keyFactory.generateSecret(dks);
		// Cipher cipher = Cipher.getInstance(DES);
		Cipher cipher = Cipher.getInstance(CIPHER_INSTANCE);
		cipher.init(Cipher.ENCRYPT_MODE, securekey, sr);
		return cipher.doFinal(data);
	}

	/**
	 * 解密
	 * 
	 * @author Doe.
	 * @param data 需要解密的byte数据
	 * @param key	解密需要的byte类型的Key
	 * @return  返回解密后的byte数据
	 * @throws Exception
	 */
	protected static byte[] decrypt(byte[] data, byte[] key) throws Exception {
		SecureRandom sr = new SecureRandom();
		sr.setSeed(key);
		DESKeySpec dks = new DESKeySpec(key);
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
		SecretKey securekey = keyFactory.generateSecret(dks);
		// Cipher cipher = Cipher.getInstance(DES);
		Cipher cipher = Cipher.getInstance(CIPHER_INSTANCE);
		cipher.init(Cipher.DECRYPT_MODE, securekey, sr);
		return cipher.doFinal(data);
	}
	

	public static void main(String[] args) throws IOException, Exception {
		//System.out.println(DesUtil.encrypt("{\"test\":\"111\"}", "sadasdsada"));
		System.out.println(DesUtil.encrypt("{\"wsPrivateKey\":\"rY4m5G7hux696f9DEN2ysJrzRJtnWRus\"}","rY4m5G7hux696f9DEN2ysJrzRJtnWRus"));
		String a="rY4m5G7hux696f9DEN2ysJrzRJtnWRus";
		String b=URLDecoder.decode(a, "UTF-8");
		//System.out.println(DesUtil.encrypt(b, "aAy3kFEsNSy6wQcmcppG7dlgePSGtCMg"));
		
		JSONObject o = new JSONObject();
		o.put("wsPrivateKey", "DJTlYbzwfocpaHkjRbIYqc0SQxSUwlvS");
		String js = JSON.toJSONString(o);
		System.out.println(DesUtil.decrypt("YoW1lcunzyBIIeSCReKojrenTPCsEHGJhxRHokahd5KWYZEd2JTYgSKPbfWH+Puk5Pk+ISo8RAU=", "rY4m5G7hux696f9DEN2ysJrzRJtnWRus"));
		
				
				
	}
}

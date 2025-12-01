package com.live.app.ws.util;

import com.zywl.app.base.util.HTTPUtil;
import com.zywl.app.base.util.UID;

public class KeyUtil {

	public static int server = 1;
	public static String KEY_URL = "http://47.96.189.119:8080/ZY-KEYFACTORY/getKey";
	public static String KEY_URL2 = "http://127.0.0.1:8080/ZY-KEYFACTORY/getKey";
	public static String publicKey() {
		return UID.create();
	}

	public static String privateKey(String publicKey, String method) {
		String addr;
		if (server==2){
			addr = KEY_URL + "?method=" + method + "&key=" + publicKey;
		}else{
			addr = KEY_URL2 + "?method=" + method + "&key=" + publicKey;
		}
		return HTTPUtil.get(addr);
	}
	
	public static void main(String[] args) {
		System.out.println(privateKey(publicKey(), "create"));
	}
}

package com.zywl.app.base.util;

import org.apache.commons.codec.digest.DigestUtils;

public class MD5Util {
	
	public static String md5(String str) {
		return DigestUtils.md5Hex(str);
	}
}

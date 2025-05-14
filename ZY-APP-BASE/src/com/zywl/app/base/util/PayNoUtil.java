package com.zywl.app.base.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class PayNoUtil {

	public static synchronized String generatePayNo(String proxyName) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		String newDate = sdf.format(new Date());
		String result = "";
		Random random = new Random();
		for (int i = 0; i < 3; i++) {
			result += random.nextInt(10);
		}
		return (proxyName == null ? "" : (proxyName + "-")) + newDate + result;
	}
	
}

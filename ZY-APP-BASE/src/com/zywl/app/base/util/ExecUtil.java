package com.zywl.app.base.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ExecUtil {
	private static final Log logger = LogFactory.getLog(ExecUtil.class);
	
	public static String exec(String command){
		try {
			logger.info("执行系统命令：" + command);
			String line = null;
			StringBuilder sb = new StringBuilder();
			Process process = Runtime.getRuntime().exec(command);
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(process.getInputStream(), System.getProperty("sun.jnu.encoding")));
			while ((line = bufferedReader.readLine()) != null) {
				sb.append(line + "\n");
			}
			return sb.toString();
		} catch (IOException e) {
			logger.error("执行系统命令失败：" + command, e);
			return null;
		}
	}
}

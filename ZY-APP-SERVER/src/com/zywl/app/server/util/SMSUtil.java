package com.zywl.app.server.util;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.fastjson2.JSON;
import com.zywl.app.base.util.HTTPUtil;

public class SMSUtil {

	private static final Log logger = LogFactory.getLog(SMSUtil.class);

	private static final String ENCODING = "UTF-8";

	/** APP KEY 用户唯一标识 */
	private static final String SpareLineFour_API_KEY = "19611cb53a10040c4605a730432399d1";

	/** 服务地址--通知类型模版 */
	private static final String SpareLineFour_SERVER_URL = "https://sms.yunpian.com/v2/sms/tpl_single_send.json";

	/**
	 * 发送验证码
	 * 
	 * @param mobile
	 * @param code
	 * @return
	 */
	public static boolean sendCode(String mobile, String code) {
		try {
			String tpl_value = URLEncoder.encode("#code#", ENCODING) + "=" + URLEncoder.encode(code, ENCODING);
//			send(mobile, tpl_value, "3218602");
			return true;
		} catch (Exception e) {
			logger.error("短信发送异常：" + e, e);
			return false;
		}
	}

	/**
	 * 发送
	 */
	public static void send(String mobile, String tplVal, String tplId) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("apikey", SpareLineFour_API_KEY);
		params.put("mobile", mobile);
		params.put("tpl_id", tplId);
		params.put("tpl_value", tplVal);

		logger.debug("发送验证码：" + JSON.toJSONString(params));

		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type", "application/x-www-form-urlencoded");

		String responseBody = HTTPUtil.post(SpareLineFour_SERVER_URL, params, headers);
		logger.debug("短信发送返回->" + responseBody);
	}

	public static void main(String[] args) {
		sendCode("17744602860", "1234");
	}
}

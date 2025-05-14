package com.zywl.app.server.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.Base;
import com.zywl.app.base.util.HTTPUtil;

public class VaptchaUtil extends Base {
	
	private static final Log logger = LogFactory.getLog(VaptchaUtil.class);
	
	public static void check(String token, String scene, String ip) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("id", "5d8eb524fc650ed3247dcb4f");
		params.put("secretkey", "f15e62a8a39b4869844a71421527364d");
		params.put("scene", scene);
		params.put("token", token);
		params.put("ip", ip);
		String post = HTTPUtil.post("http://api.vaptcha.com/v2/validate", params, null);
		logger.debug("行为验证结果：" + post);
		if(post != null) {
			JSONObject validateData = JSON.parseObject(post);
			if(!"1".equals(validateData.getString("success"))) {
				throwExp(validateData.getString("msg"));
			}
		}else {
			throwExp("验证失败，请重试");
		}
	}

	@Override
	protected Log logger() {
		return logger;
	}
}

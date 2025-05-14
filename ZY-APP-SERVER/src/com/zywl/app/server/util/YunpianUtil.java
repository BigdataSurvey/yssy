package com.zywl.app.server.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.Base;
import com.zywl.app.base.util.HTTPUtil;
import com.zywl.app.base.util.MD5Util;

public class YunpianUtil extends Base {
	
	private static final Log logger = LogFactory.getLog(YunpianUtil.class);
	
	public static void check(String token, String authenticate, String phone) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("captchaId", "ecea8c05998441819065529ca5a27c14");
		params.put("token", token);
		params.put("authenticate", authenticate);
		params.put("secretId", "beb3cea5dfc44e6aa7d26220b004ced9");
		params.put("version", "1.0");
		params.put("user", MD5Util.md5(phone));
		params.put("timestamp", "" + System.currentTimeMillis());
		params.put("nonce", String.valueOf(new Random().nextInt(99999)));
		String signature = genSignature("d8f6bcbe90804cff854c629074801f42", params);
		params.put("signature", signature);
		
		String post = HTTPUtil.post("https://captcha.yunpian.com/v1/api/authenticate", params, null);
		logger.debug("行为验证结果：" + post);
		if(post != null) {
			JSONObject validateData = JSON.parseObject(post);
			if(!"0".equals(validateData.getString("code"))) {
				throwExp(validateData.getString("msg"));
			}
		}else {
			throwExp("验证失败，请重试");
		}
	}
	
	private static String genSignature(String secretKey, Map<String, String> params) {
	    String[] keys = params.keySet().toArray(new String[0]);
	    Arrays.sort(keys);
	    StringBuilder sb = new StringBuilder();
	    for (String key : keys) {
	      sb.append(key).append(params.get(key));
	    }
	    sb.append(secretKey);
	    return DigestUtils.md5Hex(sb.toString());
	  }

	@Override
	protected Log logger() {
		return logger;
	}
}

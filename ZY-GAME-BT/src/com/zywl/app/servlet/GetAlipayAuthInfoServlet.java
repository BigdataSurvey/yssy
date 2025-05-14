package com.zywl.app.servlet;

import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.User;
import com.zywl.app.base.servlet.BaseServlet;
import com.zywl.app.base.util.JSONUtil;
import com.zywl.app.base.util.OrderUtil;
import com.zywl.app.base.util.RSA2Util;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.util.SpringUtil;
import com.zywl.app.service.AliPayCashService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;

@SuppressWarnings("serial")
@WebServlet(name = "GetAlipayAuthInfoServlet", urlPatterns = "/getAlipayAuthInfo")
public class GetAlipayAuthInfoServlet extends BaseServlet {

	private static final Log logger = LogFactory.getLog(GetAlipayAuthInfoServlet.class);

	private UserCacheService userCacheService;
	public GetAlipayAuthInfoServlet() {
		userCacheService = SpringUtil.getService(UserCacheService.class);
	}



	public Object doProcess(HttpServletRequest request, HttpServletResponse response, String ip) throws Exception {
		request.getSession().invalidate();
		StringBuffer buffer = new StringBuffer();
		buffer.append("apiname=com.alipay.account.auth&method=alipay.open.auth.sdk.code.get&app_id=2021005111696226" +
				"&app_name=mc&biz_type=openservice&pid=2088941989029863&product_id=APP_FAST_LOGIN&scope=kuaijie&" +
				"target_id=");
		buffer.append(OrderUtil.getOrder5Number());
		buffer.append("&auth_type=AUTHACCOUNT&sign_type=RSA2&sign=");
		String sign = RSA2Util.sign256(buffer.toString(), AliPayCashService.privateKey);
		URLEncoder.encode(sign,"UTF-8");
		buffer.append(sign);
		String gameToken = request.getParameter("gameToken");
		User userInfoByGameToken = userCacheService.getUserInfoByGameToken(gameToken);
		JSONObject result = JSONUtil.getReturnDate(1, buffer, "");
		if (userInfoByGameToken!=null && userInfoByGameToken.getTokenTime().getTime()>System.currentTimeMillis()){
			result.put("event","login");
		}else {
			result.put("event","auth");
		}
		return result;
	}

	@Override
	protected Log logger() {
		return logger;
	}
}

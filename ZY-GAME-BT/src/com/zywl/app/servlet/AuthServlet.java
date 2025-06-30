package com.zywl.app.servlet;

import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.Config;
import com.zywl.app.base.constant.RedisKeyConstant;
import com.zywl.app.base.servlet.BaseServlet;
import com.zywl.app.base.util.JSONUtil;
import com.zywl.app.defaultx.cache.AppConfigCacheService;
import com.zywl.app.defaultx.util.SpringUtil;
import com.zywl.app.service.AuthService;
import com.zywl.app.service.LoginConfigService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
@WebServlet(name = "AuthServlet", urlPatterns = "/afdagfwae")
public class AuthServlet extends BaseServlet {

	private static final Log logger = LogFactory.getLog(AuthServlet.class);

	private AuthService authService;

	private LoginConfigService managerConfigService;

	private AppConfigCacheService appConfigCacheService;
	public AuthServlet() {
		authService = SpringUtil.getService(AuthService.class);
		managerConfigService= SpringUtil.getService(LoginConfigService.class);
		appConfigCacheService = SpringUtil.getService(AppConfigCacheService.class);
	}

	public Object doProcess(HttpServletRequest request, HttpServletResponse response, String ip) throws Exception {
		request.getSession().invalidate();
		String baiIp = appConfigCacheService.getConfigByKey(RedisKeyConstant.APP_CONFIG_BAI_IP,Config.BAI_IP);
		if(managerConfigService.getInteger(Config.SERVICE_STATUS) == 0) {
			if (!ip.equals(baiIp)){
				JSONObject result = JSONUtil.getReturnDate(0, null, "系统维护中");
				return result;
			}
		}
		//String aaa = request.getParameter("aaa");
		logger.debug(ip+" 获取版本信息");
		JSONObject authData = authService.checkVersionAndCreateWsid();
		logger.debug("获取版本信息 及登录地址完成：" + authData.toJSONString());
		JSONObject result = JSONUtil.getReturnDate(1, authData, "");
		return result;
	}

	@Override
	protected Log logger() {
		return logger;
	}
}

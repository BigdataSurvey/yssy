package com.zywl.app.manager.servlet;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections4.map.HashedMap;
import org.omg.PortableInterceptor.USER_EXCEPTION;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.User;
import com.zywl.app.base.bean.UserCapital;
import com.zywl.app.base.bean.UserConfig;
import com.zywl.app.base.constant.RedisKeyConstant;
import com.zywl.app.base.exp.AppException;
import com.zywl.app.base.servlet.BaseServlet;
import com.zywl.app.base.util.HTTPUtil;
import com.zywl.app.base.util.JSONUtil;
import com.zywl.app.base.util.UID;
import com.zywl.app.base.bean.WsidBean;
import com.zywl.app.defaultx.enmus.UserCapitalTypeEnum;
import com.zywl.app.defaultx.service.UserService;
import com.zywl.app.defaultx.service.UserCapitalService;
import com.zywl.app.defaultx.service.UserConfigService;
import com.zywl.app.defaultx.util.SpringUtil;
import com.zywl.app.manager.context.UserConfigContext;
import com.zywl.app.manager.service.AuthService;
import com.zywl.app.manager.service.LoginService;

@SuppressWarnings("serial")
@WebServlet(name = "AdvertisementServlet", urlPatterns = "/advertisementt")
public class AdvertisementServlet extends BaseServlet{

	
	
	
	public AdvertisementServlet() {
	}
	
	
	@Override
	@Transactional
	public Object doProcess(HttpServletRequest request, HttpServletResponse response, String clientIp)
			throws AppException, Exception {
		System.out.println(3);
		return  null;
	}
	
	
	
	
	
	
	
	
	
	

}

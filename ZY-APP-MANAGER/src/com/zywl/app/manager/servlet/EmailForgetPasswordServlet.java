package com.zywl.app.manager.servlet;

import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.exp.AppException;
import com.zywl.app.base.servlet.BaseServlet;
import com.zywl.app.base.util.JSONUtil;
import com.zywl.app.base.util.MatcherUtil;
import com.zywl.app.defaultx.cache.CodeCacheService;
import com.zywl.app.defaultx.util.SpringUtil;
import com.zywl.app.manager.service.LoginService;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
@WebServlet(name = "MailForgetPasswordServlet", urlPatterns = "/mailForgetPassword")
public class EmailForgetPasswordServlet extends BaseServlet{

	
	private LoginService loginService;
	
	private CodeCacheService codeCacheService;
	
	public EmailForgetPasswordServlet() {
		loginService = SpringUtil.getService(LoginService.class);
		codeCacheService = SpringUtil.getService(CodeCacheService.class);
	}
	
	
	@Override
	@Transactional
	public Object doProcess(HttpServletRequest request, HttpServletResponse response, String clientIp)
			throws AppException, Exception {
		request.getSession().invalidate();
		String code = request.getParameter("code");
		String email = request.getParameter("email");
		 if (!MatcherUtil.isEmail(email)) {
	           JSONUtil.getReturnDate(0, null, "Please fill in the correct email address");
	        }
		String newPassword = request.getParameter("newPassword");
		String cacheCode = codeCacheService.getCode(email);
		if (cacheCode!=null && cacheCode.equals(code)) {
			codeCacheService.removeCode(email);
			loginService.emailUpdatePassword(email, newPassword);
			return	JSONUtil.getReturnDate(1, new JSONObject(), "update success!");
		}
		return JSONUtil.getReturnDate(0, new JSONObject(), "Please check the verification code");
		
	}
	

}

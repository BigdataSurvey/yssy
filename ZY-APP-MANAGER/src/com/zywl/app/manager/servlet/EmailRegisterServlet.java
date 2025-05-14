package com.zywl.app.manager.servlet;

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
@WebServlet(name = "MailRegisterServlet", urlPatterns = "/mailRegister")
public class EmailRegisterServlet extends BaseServlet{

	
	private LoginService loginService;
	
	private CodeCacheService codeCacheService;
	
	
	public EmailRegisterServlet() {
		loginService = SpringUtil.getService(LoginService.class);
		codeCacheService = SpringUtil.getService(CodeCacheService.class);
	}
	
	
	@Override
	@Transactional
	public Object doProcess(HttpServletRequest request, HttpServletResponse response, String clientIp)
			throws AppException, Exception {
		request.getSession().invalidate();
		String email = request.getParameter("email");
		 if (!MatcherUtil.isEmail(email)) {
	           JSONUtil.getReturnDate(0, null, "Please fill in the correct email address");
	        }
		if (!loginService.isHaveEmail(email)) {
			 JSONUtil.getReturnDate(0, null, "The email address is registered");
		}
		String code = request.getParameter("code");
		String password = request.getParameter("password");
		String versionId = request.getParameter("versionId");
		String inviteCode = request.getParameter("inviteCode");
		String cacheCode = codeCacheService.getCode(email);
		if (cacheCode!=null && cacheCode.equals(code)) {
			codeCacheService.removeCode(email);
			return loginService.emailRegister(email, password, clientIp, inviteCode);
		}else {
			return JSONUtil.getReturnDate(0, null, "Please check the verification code");
		}
		
		
	}
	
	
	
	
	
	
	
	

}

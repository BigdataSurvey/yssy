package com.zywl.app.manager.servlet;

import com.zywl.app.base.exp.AppException;
import com.zywl.app.base.servlet.BaseServlet;
import com.zywl.app.base.util.JSONUtil;
import com.zywl.app.base.util.MatcherUtil;
import com.zywl.app.defaultx.util.SpringUtil;
import com.zywl.app.manager.service.LoginService;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
@WebServlet(name = "MailLoginServlet", urlPatterns = "/mailLogin")
public class EmailLoginServlet extends BaseServlet{

	
	private LoginService loginService;
	
	
	
	public EmailLoginServlet() {
		loginService = SpringUtil.getService(LoginService.class);
		
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
		String password = request.getParameter("password");
		String versionId = request.getParameter("versionId");
		return loginService.emailLogin(email, password, versionId, clientIp);
	}

}

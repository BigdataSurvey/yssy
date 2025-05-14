package com.zywl.app.manager.servlet;

import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.exp.AppException;
import com.zywl.app.base.servlet.BaseServlet;
import com.zywl.app.base.util.*;
import com.zywl.app.defaultx.cache.CodeCacheService;
import com.zywl.app.defaultx.util.SpringUtil;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.Executors;

@SuppressWarnings("serial")
@WebServlet(name = "MailGetCodeServlet", urlPatterns = "/mailGetCode")
public class EmailGetCodeServlet extends BaseServlet{

	
	
	private CodeCacheService codeCacheService;
	
	
	 
	public EmailGetCodeServlet() {
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
		String cacheCode = codeCacheService.getCode(email);
		if (cacheCode!=null) {
			return JSONUtil.getReturnDate(0, null, "You have obtained the verification code");
		}
		String code = OrderUtil.get6NumberCode();
		Executors.newSingleThreadExecutor(new AppDefaultThreadFactory("emailRegist-" + email)).execute(new Runnable() {
			public void run() {
				try {
					MailUtil.sendEmail(email, code);
				} catch (Exception e) {
					logger().error("发送邮件异常：" + e, e);
				}
			}
		});
	
		codeCacheService.saveCode(email, code);
		return JSONUtil.getReturnDate(1, new JSONObject(), "Go to the mailbox to confirm the verification code, if not, please check the trash");
	}
	
	public static void main(String[] args) {
		System.out.println(MatcherUtil.isEmail("111"));
	}
	
	
	
	
	
	
	

}

package com.zywl.app.manager.servlet;

import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.service.RechargeOrderService;
import com.zywl.app.defaultx.util.SpringUtil;
import com.zywl.app.manager.service.PlayGameService;
import com.zywl.app.manager.service.manager.ManagerSocketService;
import com.zywl.app.manager.service.manager.ManagerUserService;
import com.zywl.app.manager.service.pay.WechatPayService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("serial")
@WebServlet(name = "IOSPayServlet", urlPatterns = "/iosPayNotify")
public class IOSPayServlet extends HttpServlet {

	private static final Log logger = LogFactory.getLog(IOSPayServlet.class);

	private WechatPayService wechatPayService;

	private RechargeOrderService rechargeOrderService;

	private ManagerUserService managerUserService;


	private ManagerSocketService managerSocketService;

	private UserCacheService userCacheService;

	private static  int i;

	private PlayGameService gameService;
	public IOSPayServlet() {
		wechatPayService = SpringUtil.getService(WechatPayService.class);
		rechargeOrderService = SpringUtil.getService(RechargeOrderService.class);
		managerUserService = SpringUtil.getService(ManagerUserService.class);
		userCacheService = SpringUtil.getService(UserCacheService.class);
		gameService = SpringUtil.getService(PlayGameService.class);
		managerSocketService = SpringUtil.getService(ManagerSocketService.class);
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doProcess(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doProcess(request, response);
	}

	public void doProcess(HttpServletRequest request, HttpServletResponse response) {
		Map<String, String> map = new HashMap<>(12);
		System.out.println("===================="+i);
		i++;
		System.out.println(request.getParameterMap());
	}
}

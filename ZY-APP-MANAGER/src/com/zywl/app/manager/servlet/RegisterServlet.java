package com.zywl.app.manager.servlet;

import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.Config;
import com.zywl.app.base.bean.User;
import com.zywl.app.base.bean.WsidBean;
import com.zywl.app.base.constant.RedisKeyConstant;
import com.zywl.app.base.servlet.BaseServlet;
import com.zywl.app.base.util.JSONUtil;
import com.zywl.app.defaultx.cache.AppConfigCacheService;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.service.UserService;
import com.zywl.app.defaultx.util.SpringUtil;
import com.zywl.app.manager.service.AuthService;
import com.zywl.app.manager.service.LoginService;
import com.zywl.app.manager.service.manager.ManagerUserService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@SuppressWarnings("serial")
@WebServlet(name = "RegisterServlet", urlPatterns = "/register")
public class RegisterServlet extends BaseServlet {

	private static final Log logger = LogFactory.getLog(RegisterServlet.class);


	private AppConfigCacheService appConfigCacheService;

	private LoginService loginService;

	private UserService userService;

	private AuthService authService;

	private UserCacheService userCacheService;

	private ManagerUserService managerUserService;

	public RegisterServlet() {
		appConfigCacheService = SpringUtil.getService(AppConfigCacheService.class);
		userService = SpringUtil.getService(UserService.class);
		loginService = SpringUtil.getService(LoginService.class);
		authService = SpringUtil.getService(AuthService.class);
		userCacheService = SpringUtil.getService(UserCacheService.class);
		managerUserService = SpringUtil.getService(ManagerUserService.class);
	}

	@Override
	public Object doProcess(HttpServletRequest request, HttpServletResponse response, String ip) throws Exception {
		String account = request.getParameter("account");
		String password = request.getParameter("password");
		if (account==null){
			return JSONUtil.getReturnDate(0,null,"请输入账号");
		}
		User byOpenId = userService.findByOpenId(account);
		if (byOpenId!=null){
			return JSONUtil.getReturnDate(0,null,"该账号已存在");
		}
		String configByKey = appConfigCacheService.getConfigByKey(RedisKeyConstant.APP_CONFIG_REGISTER_NUMBER, Config.REGISTER_NUM);
		long number = Long.parseLong(configByKey);
		String	inviteCode = null;
		String gameToken = LoginService.generateToken();
		JSONObject wxInfo = new JSONObject();
		wxInfo.put("unionid",password);
		User newPlayer = userService.insertUserInfo(null, account, inviteCode, loginService.getNo(), wxInfo, "", "", gameToken, "");

		loginService.initUserInfo(newPlayer.getId());
		// 创建ws 返回用户用以创建握手连接
		WsidBean wsid = authService.createWsid(newPlayer.getId(), null, null);
		JSONObject result = new JSONObject();
		result.put("userInfo", newPlayer);
		result.put("wsInfo", wsid);
		JSONObject result2 = JSONUtil.getReturnDate(1, result, "");
		userCacheService.addTodayRegister();
		managerUserService.pushAddUser();
		logger.info("账号注册返回：" + result2);
		return JSONUtil.getReturnDate(1,result2,"注册成功");
	}
}

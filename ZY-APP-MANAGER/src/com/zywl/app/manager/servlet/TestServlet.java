package com.zywl.app.manager.servlet;

import com.zywl.app.base.bean.UserStatistic;
import com.zywl.app.base.servlet.BaseServlet;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.service.UserService;
import com.zywl.app.defaultx.service.UserStatisticService;
import com.zywl.app.defaultx.util.SpringUtil;
import com.zywl.app.manager.service.TaskService;
import org.apache.commons.logging.Log;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
@WebServlet(name = "TestServlet", urlPatterns = "/test")
public class TestServlet extends BaseServlet {



	private TaskService taskService;

	private UserService userService;

	private UserCacheService userCacheService;

	private UserStatisticService userStatisticService;

	public TestServlet(){
		taskService = SpringUtil.getService(TaskService.class);
		userService = SpringUtil.getService(UserService.class);
		userCacheService = SpringUtil.getService(UserCacheService.class);
		userStatisticService = SpringUtil.getService(UserStatisticService.class);
	}

	public Object doProcess(HttpServletRequest request, HttpServletResponse response, String ip) throws Exception {
		request.getSession().invalidate();
		Long userId = Long.valueOf(request.getParameter("userId"));
		List<Long> list = new ArrayList<>();
		list.add(userId);
		List<Long> ids = new ArrayList<>();
		StringBuffer stringBuffer = new StringBuffer();
		for (int i = 0; i < 30; i++) {
			if (list.size()==0){
				break;
			}
			List<Long> idByParentId = userService.findIdByParentId(list);
			list.clear();
			stringBuffer.append("第"+(i+1)+"代人数："+idByParentId.size());
			ids.addAll(idByParentId);
			list.addAll(idByParentId);
		}
		stringBuffer.append("团队人数："+ids.size());
		BigDecimal all = BigDecimal.ZERO;
		List<UserStatistic> byIds = userStatisticService.findByIds(ids);
		for (UserStatistic byId : byIds) {
			all = all.add(byId.getCreateAnima());
		}
		stringBuffer.append("团队总创建友情值"+all);
		return stringBuffer;
	}

	@Override
	protected Log logger() {
		return logger;
	}




}

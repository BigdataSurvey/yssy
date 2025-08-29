package com.zywl.app.manager.servlet;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.User;
import com.zywl.app.base.bean.UserRole;
import com.zywl.app.base.bean.UserStatistic;
import com.zywl.app.base.servlet.BaseServlet;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.service.UserRoleService;
import com.zywl.app.defaultx.service.UserService;
import com.zywl.app.defaultx.service.UserStatisticService;
import com.zywl.app.defaultx.util.SpringUtil;
import com.zywl.app.manager.service.PlayGameService;
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

	private UserRoleService userRoleService;

	public TestServlet(){
		taskService = SpringUtil.getService(TaskService.class);
		userService = SpringUtil.getService(UserService.class);
		userCacheService = SpringUtil.getService(UserCacheService.class);
		userStatisticService = SpringUtil.getService(UserStatisticService.class);
	}
	public Object doProcess(HttpServletRequest request, HttpServletResponse response, String ip) throws Exception {
		List<UserRole> byUserRole = userRoleService.findByUserRole(1L);
		for (UserRole userRole : byUserRole) {
			for (Object o : userRole.getUnReceive()) {
				JSONObject reward = (JSONObject) o;
				Integer itemId = reward.getIntValue("id");
				if(itemId == 34){
					reward.put("id",50);
				}else if(itemId == 35){
					reward.put("id",51);
				}
				else if(itemId == 36){
					reward.put("id",52);
				}
			}
		}
		userRoleService.batchUpdateUserRole(byUserRole);
		return null;
	}

	/*public Object doProcess(HttpServletRequest request, HttpServletResponse response, String ip) throws Exception {
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
			int k = 0;
			List<Long> idByParentId = userService.findIdByParentId(list);
			for (int j = 0; j < idByParentId.size(); j++) {
				User byId = userService.findById(idByParentId.get(j));
				if (byId!=null && byId.getVip2()==1){
					k++;
				}
			}
			list.clear();
			stringBuffer.append("第"+(i+1)+"代人数："+idByParentId.size()+"，开卡人数："+k);
			ids.addAll(idByParentId);
			list.addAll(idByParentId);
		}
		stringBuffer.append("团队人数："+ids.size());
		BigDecimal all = BigDecimal.ZERO;
		List<UserStatistic> byIds = userStatisticService.findByIds(ids);
		for (UserStatistic byId : byIds) {
			all = all.add(byId.getCreateAnima());
		}
		return stringBuffer;
	}*/

	@Override
	protected Log logger() {
		return logger;
	}




}

package com.zywl.app.server.service;

import cn.hutool.core.collection.ListUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.bean.Command;
import com.live.app.ws.enums.TargetSocketType;
import com.live.app.ws.interfacex.Listener;
import com.live.app.ws.socket.BaseClientSocket;
import com.live.app.ws.util.CommandBuilder;
import com.live.app.ws.util.Executer;
import com.zywl.app.base.bean.Mail;
import com.zywl.app.base.bean.User;
import com.zywl.app.base.bean.UserMail;
import com.zywl.app.base.bean.UserTimeTask;
import com.zywl.app.base.bean.card.DicTimeTask;
import com.zywl.app.base.bean.card.DicTimeTaskGroup;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.Async;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.service.MailService;
import com.zywl.app.defaultx.service.UserMailService;
import com.zywl.app.defaultx.service.card.DicTimeTaskGroupService;
import com.zywl.app.defaultx.service.card.DicTimeTaskService;
import com.zywl.app.defaultx.service.card.UserTimeTaskService;
import com.zywl.app.server.context.MessageCodeContext;
import com.zywl.app.server.socket.AppSocket;
import com.zywl.app.server.util.RequestManagerListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@ServiceClass(code = MessageCodeContext.TIME_TASK)
public class ServerTimeTaskService extends BaseService{
	

	@Autowired
	private UserTimeTaskService userTimeTaskService;

	@Autowired
	private DicTimeTaskGroupService dicTimeTaskGroupService;

	@Autowired
	private DicTimeTaskService dicTimeTaskService;


	
	
	
/*
	@ServiceMethod(code = "001", description = "查看限时活动列表")
	public Object getMailInfo(final AppSocket appSocket, Command appCommand, JSONObject params) {
		checkNull(params);
		long userId = appSocket.getWsidBean().getUserId();
		DicTimeTaskGroup dicTimeTaskGroup = dicTimeTaskGroupService.findGroup();
		if (dicTimeTaskGroup==null){
			throwExp("限时活动异常");
		}
		int group = dicTimeTaskGroup.getGroup();
		dicTimeTaskGroupService.findAllTimeTaskGroup();
		List<UserTimeTask> byGroupAndUserId = userTimeTaskService.findByGroupAndUserId(group, userId);
		if (null==byGroupAndUserId ||byGroupAndUserId.size()==0){
			List<DicTimeTask> byGroup = dicTimeTaskService.findByGroup(group);
			for (DicTimeTask dicTimeTask : byGroup) {
				userTimeTaskService.addTimeTask(userId,dicTimeTask);
			}
		}
		byGroupAndUserId = userTimeTaskService.findByGroupAndUserId(group, userId);
		JSONObject result = new JSONObject();
		result.put("tasks",byGroupAndUserId);
		result.put("endTime",dicTimeTaskGroup.getEndTime().getTime());
		return result;
	}


	@ServiceMethod(code = "002", description = "领取限时活动奖励")
	public Object receiveReward(final AppSocket appSocket, Command appCommand, JSONObject params) {
		checkNull(params);
		long userId = appSocket.getWsidBean().getUserId();
		params.put("userId",userId);
		Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("9007002", params).build(), new RequestManagerListener(appCommand));
		return async();
	}
*/



}

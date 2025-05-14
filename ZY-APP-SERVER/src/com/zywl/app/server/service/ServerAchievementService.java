package com.zywl.app.server.service;

import java.util.Iterator;
import java.util.List;

import com.live.app.ws.enums.TargetSocketType;
import com.live.app.ws.util.CommandBuilder;
import com.live.app.ws.util.Executer;
import com.zywl.app.defaultx.enmus.AchievementTypeEnum;
import com.zywl.app.server.util.RequestManagerListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.bean.Command;
import com.zywl.app.base.bean.User;
import com.zywl.app.base.bean.vo.UserAchievementVo;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.Async;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.server.context.MessageCodeContext;
import com.zywl.app.server.socket.AppSocket;

@Service
@ServiceClass(code = MessageCodeContext.ACHIEVEMENT_SETVER)
public class ServerAchievementService extends BaseService{
	
	@Autowired
	private UserCacheService userCacheService;
	

	@ServiceMethod(code = "001", description = "获取成就列表")
	public Object getAchievementList(final AppSocket appSocket, Command appCommand, JSONObject params) {
		long userId =  appSocket.getWsidBean().getUserId();
		User user = userCacheService.getUserInfoById(userId);
		params.put("userId",userId);
		if (user==null) {
			throwExp("查询用户信息失败，请稍后重试");
		}
		Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("600002", params).build(), new RequestManagerListener(appCommand));
		return async();
	}

	public void groupAchievementVo(List<UserAchievementVo> vos){
		Iterator iterator = vos.iterator();
		while (iterator.hasNext()){
			UserAchievementVo vo = (UserAchievementVo) iterator.next();
			if (vo.getStatus()==2){
				iterator.remove();
			}
		}

	}
	
	@ServiceMethod(code = "002", description = "领取成就奖励")
	public Async receiveReward(final AppSocket appSocket, Command appCommand, JSONObject params) {
		long userId =  appSocket.getWsidBean().getUserId();
		checkNull(params.get("achievementId"));
		params.put("userId",userId);
		//判断此成就是否是已达到条件
		if (userCacheService.canReceiveMail(String.valueOf(userId))) {
			throwExp("请求频繁");
		}
		userCacheService.userReceiveMailTime(String.valueOf(userId));
		User user = userCacheService.getUserInfoById(userId);
		if (user==null) {
			throwExp("查询用户信息失败，请稍后重试");
		}

		Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("600001", params).build(), new RequestManagerListener(appCommand));
		return async();
		
	}
	
	
	

}

package com.zywl.app.server.service;

import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.bean.Command;
import com.live.app.ws.util.CommandBuilder;
import com.live.app.ws.util.Executer;
import com.zywl.app.base.bean.User;
import com.zywl.app.base.bean.UserGift;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.service.UserGiftService;
import com.zywl.app.server.context.MessageCodeContext;
import com.zywl.app.server.socket.AppSocket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@ServiceClass(code = MessageCodeContext.USER_ROLE)
public class ServerUserRoleService extends BaseService{

	@Autowired
	private UserGiftService userGiftService;

	@Autowired
	private UserCacheService userCacheService;
	
	@ServiceMethod(code="001", description="获取角色礼包信息")
	public JSONObject getUserConfig(final AppSocket appSocket, Command appCommand, JSONObject params){
		checkNull(params);
		checkNull(params.get("type"));
		Long userId = appSocket.getWsidBean().getUserId();
		int type = params.getIntValue("type");
		if (type!=1 && type!=2){
			throwExp("非法请求");
		}
		UserGift userGift = userGiftService.findUserGift(userId, type);
		params.put("number",0);
		if (userGift!=null){
			params.put("number",userGift);
		}
		return params;
	}

	@ServiceMethod(code="002", description="激活角色礼包")
	public JSONObject useGift(final AppSocket appSocket, Command appCommand, JSONObject params){
		checkNull(params);
		checkNull(params.get("userNo"),params.get("type"));
		String userNo = params.getString("userNo");
		int type = params.getIntValue("type");
		if (type!=1 && type!=2){
			throwExp("非法请求");
		}
		User user = userCacheService.getUserInfoByUserNo(userNo);
		if (user==null){
			throwExp("玩家不存在");
		}
		UserGift userGift = userGiftService.findUserGift(user.getId(), type);
		params.put("number",0);
		if (userGift!=null){
			params.put("number",userGift);
		}
		return params;
	}

	
	

}

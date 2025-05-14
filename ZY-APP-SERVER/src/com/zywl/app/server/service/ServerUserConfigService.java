package com.zywl.app.server.service;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.bean.Command;
import com.live.app.ws.util.CommandBuilder;
import com.live.app.ws.util.Executer;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.server.context.MessageCodeContext;
import com.zywl.app.server.socket.AppSocket;

@Service
@ServiceClass(code = MessageCodeContext.USER_CONFIG_SERVER)
public class ServerUserConfigService extends BaseService{
	
	@ServiceMethod(code="001", description="获取用户配置信息")
	public void getUserConfig(final AppSocket appSocket, Command appCommand, JSONObject params){
		checkNull(params);
		checkNull(params.get("account"), params.get("type"));
		Executer.response(CommandBuilder.builder(appCommand).error("errorMessage", "").build());
	}

	
	@ServiceMethod(code="002", description="更新用户配置信息")
	public void updateUserConfig(final AppSocket appSocket, Command appCommand, JSONObject params){
		checkNull(params);
		checkNull(params.get("account"), params.get("type"));
		Executer.response(CommandBuilder.builder(appCommand).error("errorMessage", "").build());
	}

	

	@ServiceMethod(code="003", description="用户背包扩容")
	public void addUserBackpackVol(final AppSocket appSocket, Command appCommand, JSONObject params){
		checkNull(params);
		checkNull(params.get("account"), params.get("type"));
		Executer.response(CommandBuilder.builder(appCommand).error("errorMessage", "").build());
	}
	
	

}

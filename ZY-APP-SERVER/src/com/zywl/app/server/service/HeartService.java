package com.zywl.app.server.service;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.bean.Command;
import com.live.app.ws.util.CommandBuilder;
import com.live.app.ws.util.Executer;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.server.socket.AppSocket;

@Service
@ServiceClass(code = "999")
public class HeartService extends BaseService{
	
	private final static JSONObject heart = new JSONObject();
	
	@ServiceMethod(code="999", description="心跳协议")
	public Object ping(final AppSocket appSocket, Command appCommand, JSONObject params){
		return heart;
	}

}

package com.zywl.app.manager.service;

import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.enums.TargetSocketType;
import com.zywl.app.base.util.PropertiesUtil;
import com.zywl.app.manager.socket.LogSocket;
import org.springframework.stereotype.Service;

@SuppressWarnings("ALL")
@Service
public class SocketLogService {


	private LogSocket managerSocket;
	

	
	public void connectLog(){

		if(managerSocket == null){
			PropertiesUtil managerProperties = new PropertiesUtil("logserver.properties");
			PropertiesUtil serverProperties = new PropertiesUtil("config.properties");
			JSONObject shakeHandsDatas = new JSONObject();
			shakeHandsDatas.put("name", serverProperties.get("server.name"));
			shakeHandsDatas.put("address", serverProperties.get("template.server"));
			shakeHandsDatas.put("host", serverProperties.get("template.server"));
			shakeHandsDatas.put("weight", serverProperties.get("server.weight"));
			managerSocket = new LogSocket(TargetSocketType.logServer, -1, managerProperties.get("log.ws.address"), shakeHandsDatas);
			managerSocket.connect();
		}
	}
	

}

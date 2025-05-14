package com.zywl.app.service;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.enums.TargetSocketType;
import com.zywl.app.base.util.PropertiesUtil;
import com.zywl.app.socket.ManagerSocket;

@Service
public class ServerManagerService {

	private ManagerSocket managerSocket;
	
	
	public void connectManager(){
		if(managerSocket == null){
			PropertiesUtil managerProperties = new PropertiesUtil("manager.properties");
			PropertiesUtil serverProperties = new PropertiesUtil("config.properties");
			JSONObject shakeHandsDatas = new JSONObject();
			shakeHandsDatas.put("name", serverProperties.get("server.name"));
			shakeHandsDatas.put("address", serverProperties.get("server.address"));
			shakeHandsDatas.put("host", serverProperties.get("server.host"));
			shakeHandsDatas.put("weight", serverProperties.get("server.weight"));
			shakeHandsDatas.put("gameId", 1);
			managerSocket = new ManagerSocket(TargetSocketType.battleRoyale, -1, managerProperties.get("manager.ws.address"), shakeHandsDatas);
			managerSocket.connect();
		}
	}
	
}

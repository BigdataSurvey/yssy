package com.zywl.app.service;

import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.enums.TargetSocketType;
import com.zywl.app.base.util.PropertiesUtil;
import com.zywl.app.socket.LogSocket;
import com.zywl.app.socket.ManagerSocket;
import org.springframework.stereotype.Service;

@Service
public class LoginManagerService {

	private ManagerSocket managerSocket;


	private LogSocket logSocket;
	
	public void connectManager(){
		if(managerSocket == null){
			PropertiesUtil managerProperties = new PropertiesUtil("manager.properties");
			PropertiesUtil serverProperties = new PropertiesUtil("config.properties");
			JSONObject shakeHandsDatas = new JSONObject();
			shakeHandsDatas.put("name", serverProperties.get("server.name"));
			shakeHandsDatas.put("address", serverProperties.get("server.address"));
			shakeHandsDatas.put("host", serverProperties.get("server.host"));
			shakeHandsDatas.put("weight", serverProperties.get("server.weight"));
			shakeHandsDatas.put("gameId", 9);
			managerSocket = new ManagerSocket(TargetSocketType.manager, -1, managerProperties.get("manager.ws.address"), shakeHandsDatas);
			managerSocket.connect();
		}
	}

	public void connectLog(){
		if(logSocket == null){
			PropertiesUtil managerProperties = new PropertiesUtil("manager.properties");
			PropertiesUtil serverProperties = new PropertiesUtil("config.properties");
			JSONObject shakeHandsDatas = new JSONObject();
			shakeHandsDatas.put("name", serverProperties.get("server.name"));
			shakeHandsDatas.put("address", serverProperties.get("server.address"));
			shakeHandsDatas.put("host", serverProperties.get("server.host"));
			shakeHandsDatas.put("weight", serverProperties.get("server.weight"));
			shakeHandsDatas.put("gameId", 9);
			logSocket = new LogSocket(TargetSocketType.logServer, -1, managerProperties.get("log.ws.address"), shakeHandsDatas);
			logSocket.connect();
		}
	}
	
}

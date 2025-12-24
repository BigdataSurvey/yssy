package com.zywl.app.service;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.enums.TargetSocketType;
import com.zywl.app.base.util.PropertiesUtil;
import com.zywl.app.socket.ManagerSocket2;

@Service
public class ServerManagerService2 {

	private ManagerSocket2 managerSocket2;
	
	
	public void connectManager(){
		if(managerSocket2 == null){
			PropertiesUtil managerProperties = new PropertiesUtil("manager.properties");
			PropertiesUtil serverProperties = new PropertiesUtil("config.properties");
			JSONObject shakeHandsDatas = new JSONObject();
			shakeHandsDatas.put("name", serverProperties.get("server.name"));
			shakeHandsDatas.put("address", serverProperties.get("server.address"));
			shakeHandsDatas.put("host", serverProperties.get("server.host"));
			shakeHandsDatas.put("weight", serverProperties.get("server.weight"));
			shakeHandsDatas.put("gameId", 1);
			managerSocket2 = new ManagerSocket2(TargetSocketType.dts2, -1, managerProperties.get("manager.ws.address"), shakeHandsDatas);
			managerSocket2.connect();
		}
	}
	public void close() {
		if (managerSocket2 != null) {
			try {
				managerSocket2.disconnect();
				System.out.println("ZY-APP-DTS2: ManagerSocket2 closed.");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}

package com.zywl.app.server.service;

import com.zywl.app.server.socket.*;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.enums.TargetSocketType;
import com.zywl.app.base.util.PropertiesUtil;

@SuppressWarnings("ALL")
@Service
public class ServerManagerService {

	private ManagerSocket managerSocket;
	

	private LogSocket logSocket;

	private SGSocket sgSocket;

	private BattleRoyaleSocket battleRoyaleSocket;

	private BattleRoyale2Socket battleRoyale2Socket;




	public void connectManager(){
		if(managerSocket == null){
			PropertiesUtil managerProperties = new PropertiesUtil("manager.properties");
			PropertiesUtil serverProperties = new PropertiesUtil("config.properties");
			JSONObject shakeHandsDatas = new JSONObject();
			shakeHandsDatas.put("name", serverProperties.get("server.name"));
			shakeHandsDatas.put("address", serverProperties.get("template.server"));
			shakeHandsDatas.put("host", serverProperties.get("template.server"));
			shakeHandsDatas.put("weight", serverProperties.get("server.weight"));
			managerSocket = new ManagerSocket(TargetSocketType.manager, -1, managerProperties.get("manager.ws.address"), shakeHandsDatas);
			managerSocket.connect();
		}
	}
	

	

	public void connectLogServer(){
		if(logSocket == null){
			PropertiesUtil managerProperties = new PropertiesUtil("manager.properties");
			PropertiesUtil serverProperties = new PropertiesUtil("config.properties");
			JSONObject shakeHandsDatas = new JSONObject();
			shakeHandsDatas.put("name", serverProperties.get("server.name"));
			shakeHandsDatas.put("address", serverProperties.get("template.server"));
			shakeHandsDatas.put("host", serverProperties.get("template.server"));
			shakeHandsDatas.put("weight", serverProperties.get("server.weight"));
			logSocket = new LogSocket(TargetSocketType.logServer, -1, managerProperties.get("log.ws.address"), shakeHandsDatas);
			logSocket.connect();
		}
	}

	public void connectSgServer(){
		if(sgSocket == null){
			PropertiesUtil managerProperties = new PropertiesUtil("manager.properties");
			PropertiesUtil serverProperties = new PropertiesUtil("config.properties");
			JSONObject shakeHandsDatas = new JSONObject();
			shakeHandsDatas.put("name", serverProperties.get("server.name"));
			shakeHandsDatas.put("address", serverProperties.get("template.server"));
			shakeHandsDatas.put("host", serverProperties.get("template.server"));
			shakeHandsDatas.put("weight", serverProperties.get("server.weight"));
			sgSocket = new SGSocket(TargetSocketType.sg, -1, managerProperties.get("sgServer.ws.address"), shakeHandsDatas);
			sgSocket.connect();
		}
	}

	public void connectBattleRoyaleServer(){
		if(battleRoyaleSocket == null){
			PropertiesUtil managerProperties = new PropertiesUtil("manager.properties");
			PropertiesUtil serverProperties = new PropertiesUtil("config.properties");
			JSONObject shakeHandsDatas = new JSONObject();
			shakeHandsDatas.put("name", serverProperties.get("server.name"));
			shakeHandsDatas.put("address", serverProperties.get("template.server"));
			shakeHandsDatas.put("host", serverProperties.get("template.server"));
			shakeHandsDatas.put("weight", serverProperties.get("server.weight"));
			battleRoyaleSocket = new BattleRoyaleSocket(TargetSocketType.battleRoyale, -1, managerProperties.get("battleRoyale.ws.address"), shakeHandsDatas);
			battleRoyaleSocket.connect();
		}
	}

	public void connectDts2Server(){
		if(battleRoyale2Socket == null){
			PropertiesUtil managerProperties = new PropertiesUtil("manager.properties");
			PropertiesUtil serverProperties = new PropertiesUtil("config.properties");
			JSONObject shakeHandsDatas = new JSONObject();
			shakeHandsDatas.put("name", serverProperties.get("server.name"));
			shakeHandsDatas.put("address", serverProperties.get("template.server"));
			shakeHandsDatas.put("host", serverProperties.get("template.server"));
			shakeHandsDatas.put("weight", serverProperties.get("server.weight"));
			battleRoyale2Socket = new BattleRoyale2Socket(TargetSocketType.dts2, -1, managerProperties.get("dts2Server.ws.address"), shakeHandsDatas);
			battleRoyale2Socket.connect();
		}
	}
}

package com.zywl.app.manager.socket;


import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.bean.Command;
import com.live.app.ws.bean.ConnectedData;
import com.live.app.ws.bean.PushBean;
import com.live.app.ws.config.HttpSessionConfigurator;
import com.live.app.ws.constant.SocketConstants;
import com.live.app.ws.enums.PushCode;
import com.live.app.ws.enums.TargetSocketType;
import com.live.app.ws.interfacex.PushListener;
import com.live.app.ws.socket.BaseServerSocket;
import com.live.app.ws.socket.BaseSocket;
import com.live.app.ws.util.Push;
import com.zywl.app.base.util.PropertiesUtil;
import com.zywl.app.defaultx.cache.UserCapitalCacheService;
import com.zywl.app.defaultx.cache.WsidCaCheService;
import com.zywl.app.defaultx.service.UserService;
import com.zywl.app.defaultx.util.SpringUtil;
import com.zywl.app.manager.service.PlayGameService;
import com.zywl.app.manager.service.manager.ManagerCapitalService;
import com.zywl.app.manager.service.manager.ManagerPromoteService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.websocket.server.ServerEndpoint;
import java.util.Random;
import java.util.Set;

@ServerEndpoint(value = "/ManagerDgsServer" + SocketConstants.SOCKET_CONNECT_SHAKE_HANDS, configurator = HttpSessionConfigurator.class)
public class ManagerDgsSocketServer extends BaseServerSocket {
private static final Log logger = LogFactory.getLog(ManagerDgsSocketServer.class);

	private String address;

	private String host;

	private String name;

	private double weight = 1; //权重


	private PropertiesUtil staticProperties;

	private PropertiesUtil globalProperties;

	private PlayGameService gameService;

	private WsidCaCheService wsidCaCheService;


	private UserCapitalCacheService userCapitalCacheService;

	private UserService userService;

	private ManagerPromoteService managerPromoteService;

	private ManagerCapitalService managerCapitalService;

	public ManagerDgsSocketServer() {
		super(TargetSocketType.dgs, false, true);
		staticProperties = new PropertiesUtil("static.properties");
		globalProperties = new PropertiesUtil("global.properties");
		gameService =  SpringUtil.getService(PlayGameService.class);
		wsidCaCheService = SpringUtil.getService(WsidCaCheService.class);
		userCapitalCacheService= SpringUtil.getService(UserCapitalCacheService.class);
		userService = SpringUtil.getService(UserService.class);
		managerPromoteService = SpringUtil.getService(ManagerPromoteService.class);
		managerCapitalService = SpringUtil.getService(ManagerCapitalService.class);
	}


/*	public static void main(String[] args) {
		int number = 66; // 可以替换为任意数字
		int lowerBound = (number / 10) * 10;
		int upperBound = lowerBound + 9;

		System.out.println("数字 " + number + " 位于十位数区间: [" + lowerBound + ", " + upperBound + "]");
	}*/


	public ConnectedData onConnect(JSONObject shakeHandsData) {
		this.address = shakeHandsData.getString("address");
		this.name = shakeHandsData.getString("name");
		this.host = shakeHandsData.getString("host"); 
		this.weight = shakeHandsData.getDoubleValue("weight");
		if (shakeHandsData.containsKey("gameId")) {
			this.socketType = TargetSocketType.getServerEnum(shakeHandsData.getIntValue("gameId"));
		}
		initPush();
		JSONObject responseShakeHandsData = new JSONObject();
		responseShakeHandsData.put("staticWebUrl", staticProperties.get("base.webPath"));
		responseShakeHandsData.put("managerWebUrl", "http://"+globalProperties.get("host"));
		return new ConnectedData(address, responseShakeHandsData);
	}

	@Override
	protected void onDisconnect() {
	}

	protected String getPrivateKey(String pk) {
		return pk;
	}

	@Override
	public boolean isEncrypt(Command command) {
		return false;
	}
	
	@Override
	protected void filterCommand(Command command) {
	}
	
	protected Set<String> getWhiteList() {
		return null;
	}
	
	private void initPush(){
		Push.registPush(new PushBean(PushCode.cancelBet), new PushListener() {
			public void onRegist(BaseSocket baseSocket, Object data) {
			}

			public void onReceive(BaseSocket baseSocket, Object data) {
				JSONObject object = (JSONObject) data;
				managerCapitalService.cancelBet(object);
			}
		}, this);
	}

	public String getName() {
		return name;
	}

	public String getHost() {
		return host;
	}

	public String getAddress() {
		return address;
	}
	
	public double getWeight() {
		return weight;
	}
	
	public void setWeight(double weight) {
		this.weight = weight;
	}

	@Override
	protected Log logger() {
		return logger;
	}


	
}

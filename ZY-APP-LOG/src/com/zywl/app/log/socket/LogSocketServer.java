package com.zywl.app.log.socket;


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
import com.zywl.app.defaultx.util.SpringUtil;
import com.zywl.app.log.service.IncomeStatementService;
import com.zywl.app.log.service.LogService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.websocket.server.ServerEndpoint;
import java.util.Set;

@ServerEndpoint(value = "/LogServer" + SocketConstants.SOCKET_CONNECT_SHAKE_HANDS, configurator = HttpSessionConfigurator.class)
public class LogSocketServer extends BaseServerSocket {
private static final Log logger = LogFactory.getLog(LogSocketServer.class);

	private String address;

	private String host;

	private String name;

	private double weight = 1; //权重


	private LogService logService;

	private IncomeStatementService incomeStatementService;


	public LogSocketServer() {
		super(TargetSocketType.logServer, false, true);
		logService = SpringUtil.getService(LogService.class);
		incomeStatementService = SpringUtil.getService(IncomeStatementService.class);
	}

	public ConnectedData onConnect(JSONObject shakeHandsData) {
		this.address = shakeHandsData.getString("address");
		if (shakeHandsData.containsKey("gameId")) {
			this.socketType = TargetSocketType.getServerEnum(shakeHandsData.getIntValue("gameId"));
		}
		
		initPush();
		JSONObject responseShakeHandsData = new JSONObject();
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
		//注册服务器实时任务数量
		//注册服务器可用状态
		Push.registPush(new PushBean(PushCode.insertLog), new PushListener() {
			public void onRegist(BaseSocket baseSocket, Object data) {
			}

			public void onReceive(BaseSocket baseSocket, Object data) {
				JSONObject object = (JSONObject) data;
				logService.insertLog(object);
			}
		}, this);

		Push.registPush(new PushBean(PushCode.addStatement), new PushListener() {
			public void onRegist(BaseSocket baseSocket, Object data) {
			}

			public void onReceive(BaseSocket baseSocket, Object data) {
				JSONObject object = (JSONObject) data;
				incomeStatementService.addUserIncome(object.getLong("parentId"),object.getLong("grandfaId"),object.getBigDecimal("parentIncome"),object.getBigDecimal("grandfaIncome"));

			}
		}, this);


		//注册服务器可用状态
		Push.registPush(new PushBean(PushCode.syncIsService), new PushListener() {
			public void onRegist(BaseSocket baseSocket, Object data) {
				if(data != null){
					baseSocket.setService(Boolean.parseBoolean(data.toString()));
				}
			}
			
			public void onReceive(BaseSocket baseSocket, Object data) {
				if(data != null){
					baseSocket.setService(Boolean.parseBoolean(data.toString())); 
				}
			}
		}, this);
	}

	public String getName() {
		return name;
	}


	@Override
	protected Log logger() {
		return logger;
	}


	
}

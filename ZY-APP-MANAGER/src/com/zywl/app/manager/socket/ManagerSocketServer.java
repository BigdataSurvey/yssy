package com.zywl.app.manager.socket;


import com.alibaba.fastjson2.JSONArray;
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
import com.zywl.app.base.bean.WsidBean;
import com.zywl.app.base.util.PropertiesUtil;
import com.zywl.app.defaultx.cache.UserCapitalCacheService;
import com.zywl.app.defaultx.cache.WsidCaCheService;
import com.zywl.app.defaultx.service.UserService;
import com.zywl.app.defaultx.util.SpringUtil;
import com.zywl.app.manager.service.PlayGameService;
import com.zywl.app.manager.service.manager.ManagerGameBaseService;
import com.zywl.app.manager.service.manager.ManagerPromoteService;
import com.zywl.app.manager.service.manager.ManagerSocketService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.websocket.server.ServerEndpoint;
import java.util.Set;

@ServerEndpoint(value = "/ManagerServer" + SocketConstants.SOCKET_CONNECT_SHAKE_HANDS, configurator = HttpSessionConfigurator.class)
public class ManagerSocketServer extends BaseServerSocket {
private static final Log logger = LogFactory.getLog(ManagerSocketServer.class);
	
	private String address;
	
	private String host;
	
	private String name;
	
	private double weight = 1; //权重
	
	private ManagerSocketService managerSocketService;


	private PropertiesUtil staticProperties;
	
	private PropertiesUtil globalProperties;
	
	private PlayGameService gameService;
	
	private WsidCaCheService wsidCaCheService;
	

	private UserCapitalCacheService userCapitalCacheService;
	
	private UserService userService;

	private ManagerPromoteService managerPromoteService;
	


	public ManagerSocketServer() {
		super(TargetSocketType.server, false, true);
		this.managerSocketService = SpringUtil.getService(ManagerSocketService.class);
		staticProperties = new PropertiesUtil("static.properties");
		globalProperties = new PropertiesUtil("global.properties");
		gameService =  SpringUtil.getService(PlayGameService.class);
		wsidCaCheService = SpringUtil.getService(WsidCaCheService.class);
		userCapitalCacheService= SpringUtil.getService(UserCapitalCacheService.class);
		userService = SpringUtil.getService(UserService.class);
		managerPromoteService = SpringUtil.getService(ManagerPromoteService.class);
	}

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
		managerSocketService.clear(this);
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
		Push.registPush(new PushBean(PushCode.syncTaskNum), new PushListener() {
			public void onRegist(BaseSocket baseSocket, Object data) {
				if(data != null){
					ManagerSocketServer managerSocketServer = ((ManagerSocketServer)baseSocket);
					String[] split = data.toString().split(",");
					managerSocketService.updateServerTask(managerSocketServer, Integer.valueOf(split[0]), Integer.valueOf(split[1]));
				}
			}
			public void onReceive(BaseSocket baseSocket, Object data) {
				if(data != null){
					ManagerSocketServer managerSocketServer = ((ManagerSocketServer)baseSocket);
					String[] split = data.toString().split(",");
					managerSocketService.updateServerTask(managerSocketServer, Integer.valueOf(split[0]), Integer.valueOf(split[1]));
				}
			}
		}, this);
		
		//注册APP上线推送
		Push.registPush(new PushBean(PushCode.syncAppOnline), new PushListener() {
			public void onRegist(BaseSocket baseSocket, Object data) {
				if(data != null){
					ManagerSocketServer managerSocketServer = ((ManagerSocketServer)baseSocket);
					JSONArray pushArray = (JSONArray)data;
					for (Object object : pushArray) {
						JSONObject pushData = (JSONObject) object;
						String sessionId = pushData.getString("sessionId");
						String wsid = pushData.getString("wsid");
						managerSocketService.online(managerSocketServer, wsid, sessionId, pushData);
						
					}
				}
			}
			public void onReceive(BaseSocket baseSocket, Object data) {
				if(data != null){
					ManagerSocketServer managerSocketServer = ((ManagerSocketServer)baseSocket);
					JSONObject pushData = (JSONObject)data;
					String wsid = pushData.getString("wsid");
					String sessionId = pushData.getString("sessionId");
					WsidBean wsidBean =  wsidCaCheService.getWsid(wsid);
					if (wsidBean!=null) {
						long userId = wsidBean.getUserId();
						managerPromoteService.initUserInviteRecord(userId);
					}
					logger.debug("WSID为[" + wsid + "]APP在" + managerSocketServer.getName() + "["+sessionId+"]上线：" + pushData.toJSONString());
					managerSocketService.online(managerSocketServer, wsid, sessionId, pushData);

				}
			}
		}, this);
		
		//注册APP离线推送
		Push.registPush(new PushBean(PushCode.syncAppOffline), new PushListener() {
			public void onRegist(BaseSocket baseSocket, Object data) {}
			public void onReceive(BaseSocket baseSocket, Object data) {
				if(data != null){
					ManagerSocketServer managerSocketServer = ((ManagerSocketServer)baseSocket);
					JSONObject pushData = (JSONObject)data;
					String wsid = pushData.getString("wsid");
					WsidBean ws = wsidCaCheService.getWsid(wsid);
					String sessionId = pushData.getString("sessionId");
					logger.info("WSID为[" + wsid + "]APP在" + managerSocketServer.getName() + "["+sessionId+"]下线");
					managerSocketService.offline(managerSocketServer, sessionId);
				}
			}
		}, this);
		
		//注册APP信息变更
		Push.registPush(new PushBean(PushCode.syncAppChange), new PushListener() {
			public void onRegist(BaseSocket baseSocket, Object data) {}
			public void onReceive(BaseSocket baseSocket, Object data) {
				if(data != null){
					ManagerSocketServer managerSocketServer = ((ManagerSocketServer)baseSocket);
					JSONObject pushData = (JSONObject)data;
					String wsid = pushData.getString("wsid");
					String sessionId = pushData.getString("sessionId");
					logger.debug("WSID为[" + wsid + "]APP在" + managerSocketServer.getName() + "["+sessionId+"]更新状态：" + pushData.toJSONString());
					managerSocketService.appChange(managerSocketServer, pushData.getString("wsid"), sessionId, pushData);
				}
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

package com.zywl.app.manager.socket;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpSession;
import javax.websocket.server.ServerEndpoint;

import com.zywl.app.defaultx.service.UserService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.bean.Command;
import com.live.app.ws.config.HttpSessionConfigurator;
import com.live.app.ws.constant.CommandConstants;
import com.live.app.ws.enums.TargetSocketType;
import com.live.app.ws.socket.BaseServerSocket;
import com.live.app.ws.socket.BaseWebSocketServer;
import com.live.app.ws.socket.manager.SocketManager;
import com.live.app.ws.util.CommandBuilder;
import com.live.app.ws.util.Executer;
import com.zywl.app.base.bean.Admin;
import com.zywl.app.base.bean.Config;
import com.zywl.app.defaultx.util.SpringUtil;
import com.zywl.app.manager.service.AdminSocketService;
import com.zywl.app.manager.service.manager.ManagerConfigService;
import com.zywl.app.manager.service.manager.ManagerSocketService;

@ServerEndpoint(value = "/admin", configurator = HttpSessionConfigurator.class)
public class AdminSocketServer extends BaseWebSocketServer{
	private static final Log logger = LogFactory.getLog(AdminSocketServer.class);
	
	private static Set<String> whiteList = new HashSet<String>();

	static{
		whiteList.add("002001"); //登录
	}
	
	private Admin admin;
	
	private ManagerSocketService managerSocketService;
	
	private ManagerConfigService managerConfigService;
	
	private AdminSocketService adminSocketService;


	public AdminSocketServer(){
		super(TargetSocketType.adminWeb);
		managerSocketService = SpringUtil.getService(ManagerSocketService.class);
		managerConfigService = SpringUtil.getService(ManagerConfigService.class);
		adminSocketService = SpringUtil.getService(AdminSocketService.class);
	}



	@Override
	public void onOpen(HttpSession httpSession, Map<String, String> shakeHandsMap) {
		this.httpSession = httpSession;
		setAdmin((Admin) getHttpSession().getAttribute("Admin"));
		
		id = httpSession.getId();
		ip = httpSession.getAttribute("ip").toString();
		logger().info("新的Web连接 -> " + ip);
		
		//检查连接id唯一性
		Map<String, BaseServerSocket> servers = SocketManager.getClients(getSocketType());
		if(servers != null && servers.containsKey(id)){
			System.out.println("----------------");
			servers.get(id).close();
		}
		
		SocketManager.addClient(getSocketType(), id, this);
		
		Command connectSuccess = new Command();
		connectSuccess.setCode(CommandConstants.CMD_CONNECTED);
		connectSuccess.setPush(true);
		JSONObject data = new JSONObject();
		data.put("login", isLogin());
		if(isLogin()){
			data.putAll(getLoginData());
		}
		connectSuccess.setData(data);
		sendCommand(connectSuccess);
	}
	
	@Override
	public void onMessage(String message) {
		logger.debug("收到控制台指令：" + message);
		Command command = JSON.parseObject(message, Command.class);
		if(getWhiteList() != null){
			if(getWhiteList().contains(command.getCode()) || isLogin()){
				Executer.disposeRequest(this, command);
			}else{
				JSONObject data = new JSONObject();
				data.put("timeout", true);
				sendCommand(CommandBuilder.builder(command).error("登录超时", data).build());
			}
		}else{
			Executer.disposeRequest(this, command);
		}
	}
	@Override
	public boolean isEncrypt(Command command) {
		return false;
	}
	public JSONObject getLoginData(){
		JSONObject loginShakeHands = new JSONObject();
		
		JSONObject serverOnline = new JSONObject();
		JSONObject serverTask = new JSONObject();
		JSONObject serverWeight = new JSONObject();
		JSONObject serverOnlineNum = new JSONObject();
		int allCount = 0;
		Map<String, ManagerSocketServer> servers = SocketManager.getClients(TargetSocketType.server);
		for(ManagerSocketServer managerSocketServer : servers.values()){
			Map<String, JSONObject> onlineAppData = managerSocketService.getOnlineAppData(managerSocketServer);
			onlineAppData = onlineAppData == null ? new HashMap<String, JSONObject>() : onlineAppData;
			serverOnline.put(managerSocketServer.getName(), onlineAppData);
			serverTask.put(managerSocketServer.getName(), managerSocketService.getNowServerTask(managerSocketServer));
			serverWeight.put(managerSocketServer.getName(), managerSocketServer.getWeight());
			serverOnlineNum.put(managerSocketServer.getName(), onlineAppData.size());
			allCount+=onlineAppData.size();
		}
//		loginShakeHands.put("serverOnline", serverOnline);
		loginShakeHands.put("serverOnlineNum", serverOnlineNum);
		loginShakeHands.put("serverTask", serverTask);
		loginShakeHands.put("serverWeight", serverWeight);
		if(getAdmin() != null) {
			JSONObject adminObj = (JSONObject)JSON.toJSON(getAdmin());
			adminObj.remove("password");
			loginShakeHands.put("admin", adminObj);
		}
		loginShakeHands.put("resourceUrl", managerConfigService.getString(Config.APP_RESOURCE_ONLINE_URL));
		loginShakeHands.put("monitorData", adminSocketService.getMonitorData());
		loginShakeHands.getJSONObject("monitorData").put("onlineCount", allCount);
		return loginShakeHands;
	}
	
	@Override
	protected Set<String> getWhiteList() {
		return isLogin() ? null : whiteList;
	}

	public boolean isLogin(){
		try{
			return this.getHttpSession() != null && isNotNull(this.getHttpSession().getAttribute("Admin"));
		}catch (Exception e) {
			return false;
		}
	}
	
	public Admin getAdmin() {
		return admin;
	}

	public void setAdmin(Admin admin) {
		if(admin != null){
			Map<String, AdminSocketServer> servers = SocketManager.getClients(TargetSocketType.adminWeb);
			if(servers != null){
				for (AdminSocketServer adminSocket : servers.values()) {
					if(adminSocket.getAdmin() != null && adminSocket.getAdmin().getId().equals(admin.getId())){
						adminSocket.sendCommand(CommandBuilder.builder().push(CommandConstants.CMD_FC_LOGINOUT, getIp()).build());
						adminSocket.setAdmin(null);
						adminSocket.getHttpSession().removeAttribute("Admin");
					}
				}
			}
		}
		this.admin = admin;
	}

	@Override
	protected Log logger() {
		return logger;
	}

}

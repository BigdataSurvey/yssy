package com.zywl.app.manager.service.manager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import com.zywl.app.base.bean.Config;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.service.ConfigService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson2.JSONObject;
import com.google.common.collect.Sets;
import com.live.app.ws.defaultx.ServiceRunable;
import com.live.app.ws.enums.PushCode;
import com.live.app.ws.enums.TargetSocketType;
import com.live.app.ws.socket.manager.SocketManager;
import com.live.app.ws.util.CommandBuilder;
import com.live.app.ws.util.DefaultPushHandler;
import com.live.app.ws.util.Executer;
import com.live.app.ws.util.Push;
import com.zywl.app.base.bean.Role;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.manager.bean.ServerTaskBean;
import com.zywl.app.manager.context.MessageCodeContext;
import com.zywl.app.manager.service.AdminSocketService;
import com.zywl.app.manager.service.IPService;
import com.zywl.app.manager.socket.AdminSocketServer;
import com.zywl.app.manager.socket.ManagerSocketServer;

@Service
@ServiceClass(code = MessageCodeContext.SOCKET_SERVER)
public class ManagerSocketService extends BaseService {

    private static final Log logger = LogFactory.getLog(ManagerSocketService.class);

    private static Map<ManagerSocketServer, List<ServerTaskBean>> serverTaskData = new ConcurrentHashMap<ManagerSocketServer, List<ServerTaskBean>>();



    private static Map<ManagerSocketServer, Map<String, JSONObject>> serverOnlineAppData = new ConcurrentHashMap<ManagerSocketServer, Map<String, JSONObject>>();

    private static Map<String, Map<String, UserOnlineInfo>> userOnlineCache = new ConcurrentHashMap<String, Map<String, UserOnlineInfo>>();

    @Autowired
    private ManagerUserService managerUserService;

    @Autowired
    private ConfigService configService;

    @Autowired
    private UserCacheService userCacheService;


    @Autowired
    private ManagerConfigService managerConfigService;


    @Autowired
    private IPService ipService;

    @PostConstruct
    public void _construct() {
        /**
         * 强制app退出登录推送
         */
        Push.addPushSuport(PushCode.fcAppLoginOut, new DefaultPushHandler());

    }

    @ServiceMethod(code = "001", description = "修改服务器权重")
    public void updateServerWeight(AdminSocketServer adminSocketServer, JSONObject data) {
        checkNull(data);
        checkNull(data.get("name"), data.get("weight"));
        String serverName = data.getString("name");
        Double weight = data.getDouble("weight");
        Map<String, ManagerSocketServer> clients = SocketManager.getClients(TargetSocketType.server);
        if (clients != null && !clients.isEmpty()) {
            for (ManagerSocketServer socketServer : clients.values()) {
                if (eq(socketServer.getName(), serverName)) {
                    socketServer.setWeight(weight);
                }
            }
        }
    }

    public int getServerNum() {
        int num = 0;
        Map<String, ManagerSocketServer> clients = SocketManager.getClients(TargetSocketType.server);
        if (clients != null && !clients.isEmpty()) {
            for (ManagerSocketServer nowSocketServer : clients.values()) {
                if (!nowSocketServer.isService())
                    continue;
                num++;
            }
        }
        return num;
    }

    /**
     * 获取最佳socket连接
     *
     * @param filterAddress
     * @return
     */
    public ManagerSocketServer getGoodServer(String filterAddress) {
        ManagerSocketServer goodServer = null;
        Map<String, ManagerSocketServer> clients = SocketManager.getClients(TargetSocketType.server);
        if (clients != null && !clients.isEmpty()) {

            for (ManagerSocketServer nowSocketServer : clients.values()) {
                if (!nowSocketServer.isService() || nowSocketServer.getAddress().equals(filterAddress) && clients.values().size() > 1) {
                    continue;
                }
                Map<String, JSONObject> onlineAppData = getOnlineAppData(nowSocketServer);
                if (onlineAppData != null && onlineAppData.size() > managerConfigService.getInteger(Config.SERVER_MAX_CONNECT)) {
                    continue;
                }
                if (goodServer == null) {
                    goodServer = nowSocketServer;
                    continue;
                } else {
                    double nowWeight = nowSocketServer.getWeight();
                    double goodWeight = goodServer.getWeight();
                    Map<String, JSONObject> nowServerOnline = getOnlineAppData(nowSocketServer);
                    Map<String, JSONObject> goodServerOnline = getOnlineAppData(goodServer);
                    ServerTaskBean nowServerTask = getNowServerTask(nowSocketServer);
                    ServerTaskBean goodServerTask = getNowServerTask(goodServer);
                    if (nowServerOnline == null) {
                        goodServer = nowSocketServer;
                    } else if (goodServerOnline == null) {
                        continue;
                    } else if (nowServerOnline.size() / nowWeight < goodServerOnline.size() / goodWeight) {
                        goodServer = nowSocketServer;
                    } else if (nowServerTask == null) {
                        goodServer = nowSocketServer;
                    } else if (goodServerTask == null) {
                        continue;
                    } else if (nowServerTask.getTask() / nowWeight < goodServerTask.getTask() / goodWeight) {
                        goodServer = nowSocketServer;
                    }
                }
            }
        }
        return goodServer;
    }

    public void online(ManagerSocketServer server, String wsid, String sessionId, JSONObject data) {
        Map<String, JSONObject> appData = serverOnlineAppData.get(server);
        if (appData == null) {
            serverOnlineAppData.put(server, appData = new ConcurrentHashMap<String, JSONObject>());
        }
        appData.put(sessionId, data);
        if (data.containsKey("ip")) {
            data.put("ipInfo", ipService.buildRegionCityTxt(data.getString("ip")));
        }
        if (data.containsKey("userId")) {
            setUserCache(data.getString("userId"), wsid, sessionId, data.getString("ip"), data.getString("deviceId"), server);
        }

        JSONObject pushData = new JSONObject();
        pushData.put("server", server.getName());
        pushData.put("id", sessionId);
        pushData.put("data", data);
        pushData.put("onlineNum", appData.size());
        AdminSocketService.massWeb(CommandBuilder.builder().push(PushCode.syncAppOnline, pushData).build());
    }


    public void offline(ManagerSocketServer server, String sessionId) {
        Map<String, JSONObject> onlineAppData = serverOnlineAppData.get(server);
        if (onlineAppData != null) {
            JSONObject appData = onlineAppData.remove(sessionId);
            if (appData != null) {
                if (appData.containsKey("userId")) {
                    removeUserCache(appData.getString("userId"), sessionId);
                }

                JSONObject pushData = new JSONObject();
                pushData.put("server", server.getName());
                pushData.put("id", sessionId);
                pushData.put("data", appData);
                pushData.put("onlineNum", onlineAppData.size());

                AdminSocketService.massWeb(CommandBuilder.builder().push(PushCode.syncAppOffline, pushData).build());
            }
        }
    }

    public void appChange(ManagerSocketServer server, String wsid, String sessionId, JSONObject data) {
        Map<String, JSONObject> onlineAppData = serverOnlineAppData.get(server);
        if (onlineAppData != null && onlineAppData.containsKey(sessionId)) {

            //edit 2019年8月11日
            String newUserId = data.getString("userId");
            String oldUserId = null;

            JSONObject oldAppData = onlineAppData.put(sessionId, data);
            if (data.containsKey("ip")) {
                data.put("ipInfo", ipService.buildRegionCityTxt(data.getString("ip")));
            }
            if (oldAppData != null) {
                oldUserId = oldAppData.getString("userId");
                if (oldUserId != null && !eq(newUserId, oldUserId)) {
                    removeUserCache(oldUserId, sessionId);
                }
            }
            if (newUserId != null && !eq(newUserId, oldUserId)) {
                setUserCache(newUserId, wsid, sessionId, data.getString("ip"), data.getString("deviceId"), server);
            }
            //edit end

            //old btg code
//			JSONObject oldAppData = onlineAppData.put(sessionId, data);
//			if(oldAppData != null && oldAppData.containsKey("userId")){
//				removeUserCache(oldAppData.getString("userId"), sessionId);
//			}
//			if(data.containsKey("userId")){
//				setUserCache(data.getString("userId"), wsid, sessionId, data.getString("ip"), server);
//			}
            //old btg code end

            JSONObject pushData = new JSONObject();
            pushData.put("server", server.getName());
            pushData.put("id", sessionId);
            pushData.put("data", data);
            AdminSocketService.massWeb(CommandBuilder.builder().push(PushCode.syncAppChange, pushData).build());
        }
    }

    public void removeUserCache(String userId, String sessionId) {
        Map<String, UserOnlineInfo> userSessions = userOnlineCache.get(userId);
        if (userSessions != null) {
            UserOnlineInfo onlineInfo = userSessions.remove(sessionId);
            if (onlineInfo != null) {
                userCacheService.removeUserServerId(userId);
                Executer.executeService(new ServiceRunable(logger) {
                    public void service() {
                        long onlineTime = System.currentTimeMillis() - onlineInfo.getOnlineTime().getTime();
                        managerUserService.addUserOnlineTime(userId, onlineTime / 1000);
                    }
                });
            }
        }
    }


    public String getSessionId(Long userId){
        return getSessionId(userId.toString());
    }

    public String getSessionId(String userId){
        String oldSessionId=null;
        Map<String, UserOnlineInfo> userSessions = userOnlineCache.get(userId);
        if (!userSessions.isEmpty()) {
            oldSessionId = userSessions.keySet().iterator().next();
        }
        return oldSessionId;
    }


    public void setUserCache(String userId, String wsid, String sessionId, String newLoginIp, String newDeviceId, ManagerSocketServer managerSocketServer) {
        Map<String, UserOnlineInfo> userSessions = userOnlineCache.get(userId);
        if (userSessions == null) {
            userOnlineCache.put(userId, userSessions = new ConcurrentHashMap<String, UserOnlineInfo>());
        }
        if (!userSessions.isEmpty()) {    //检测用户在线数量，如果不为空则需要给上一个登录该用户的AppSocket发送fcAppLoginOut指令
            String oldSessionId = userSessions.keySet().iterator().next();
            UserOnlineInfo userOnlineServer = userSessions.get(oldSessionId);
            JSONObject data = new JSONObject();
            data.put("newLoginIp", newLoginIp);
            data.put("newDeviceId", newDeviceId);
            data.put("sessionId", oldSessionId);
            data.put("type", 1);

            Push.push(userOnlineServer.getOnlineServer(), CommandBuilder.builder().push(PushCode.fcAppLoginOut, data).build());
        }

        userSessions.put(sessionId, new UserOnlineInfo(sessionId, wsid, newLoginIp, managerSocketServer, new Date()));
        UserOnlineInfo userOnlineServer = userSessions.get(sessionId);
        userCacheService.setUserServerId(userId,userOnlineServer.getOnlineServer().getId());

    }

	public void kickPlayer(String userId, String reason) {
		Map<String, UserOnlineInfo> userSessions = userOnlineCache.get(userId);
		if(userSessions == null || userSessions.isEmpty()){
			return;
		}

		String sessionId = userSessions.keySet().iterator().next();
		UserOnlineInfo userOnlineServer = userSessions.get(sessionId);
		JSONObject data = new JSONObject();
		data.put("sessionId", sessionId);
		data.put("type",3);
		String reason1 = reason;
		if(reason == null || reason.isEmpty()) {
			reason1 = "您已被强制下线!";
		}
		data.put("reason",reason1);
		Push.push(userOnlineServer.getOnlineServer(), CommandBuilder.builder().push(PushCode.fcAppLoginOut, data).build());
	}

    public void noPassAuthUser(Long userId){
        Map<String, UserOnlineInfo> userSessions = userOnlineCache.get(userId.toString());
        if(userSessions == null || userSessions.isEmpty()){
            return;
        }

        String sessionId = userSessions.keySet().iterator().next();
        UserOnlineInfo userOnlineServer = userSessions.get(sessionId);
        JSONObject data = new JSONObject();
        data.put("sessionId", sessionId);
        data.put("type",3);
        String reason="未通过实名认证，部分游戏功能将受到影响，请前往【设置-实名信息】修改与登录时使用微信一致的实名信息。";
        data.put("reason",reason);
        Push.push(userOnlineServer.getOnlineServer(), CommandBuilder.builder().push(PushCode.fcAppLoginOut, data).build());
    }


	/**
	 * 通过userId获取用户登录的数据
	 * @author DOE
	 * @param userId
	 * @return
	 */
	public UserOnlineInfo getUserOnlineInfo(String userId){
		Map<String, UserOnlineInfo> userSessions = userOnlineCache.get(userId);
		if(userSessions != null && !userSessions.isEmpty()){
			return userSessions.values().iterator().next();
		}
		return null;
	}
	
	/**
	 * 通过userId获取用户所在的节点
	 * @author DOE
	 * @param userId
	 * @return
	 */
	public ManagerSocketServer getManagerSocketServerByAppUserId(String userId){
		UserOnlineInfo userOnlineInfo = getUserOnlineInfo(userId);
		if(userOnlineInfo != null){
			return userOnlineInfo.getOnlineServer();
		}
		return null;
	}

    public String getServerIdByUserId(Long userId){
        ManagerSocketServer managerSocketServerByAppUserId = getManagerSocketServerByAppUserId(userId.toString());
        if (managerSocketServerByAppUserId!=null){
           return managerSocketServerByAppUserId.getId();
        }else{
            return null;
        }
    }
    public String getServerIdByUserId(String userId){
        ManagerSocketServer managerSocketServerByAppUserId = getManagerSocketServerByAppUserId(userId.toString());
        if (managerSocketServerByAppUserId!=null){
            return managerSocketServerByAppUserId.getId();
        }else{
            return null;
        }
    }
	/**
	 * 通过userId获取用户所使用的wsid
	 * @author DOE
	 * @param userId
	 * @return
	 */
	public String getWsidByAppUserId(String userId){
		UserOnlineInfo userOnlineInfo = getUserOnlineInfo(userId);
		if(userOnlineInfo != null){
			return userOnlineInfo.getWsid();
		}
		return null;
	}

    /**
     * 通过userId获取用户所使用的sessionId
     *
     * @param userId
     * @return
     * @author DOE
     */
    public String getSessionIdByAppUserId(String userId) {
        UserOnlineInfo userOnlineInfo = getUserOnlineInfo(userId);
        if (userOnlineInfo != null) {
            return userOnlineInfo.getSessionId();
        }
        return null;
    }

    public Map<String, JSONObject> getOnlineAppData(ManagerSocketServer server) {
        return serverOnlineAppData.get(server);
    }

    public List<ServerTaskBean> getServerTaskItems(ManagerSocketServer server) {
        return serverTaskData.get(server);
    }

    public List<ServerTaskBean> getServerTask(ManagerSocketServer server) {
        return serverTaskData.get(server);
    }

    public ServerTaskBean getNowServerTask(ManagerSocketServer server) {
        List<ServerTaskBean> taskItems = serverTaskData.get(server);
        if (taskItems != null) {
            return taskItems.get(taskItems.size() - 1);
        }
        return null;
    }


    public static int getOnlineCount() {
        return userOnlineCache.size();
    }

    public void updateServerTask(ManagerSocketServer server, int task, int qps) {
        List<ServerTaskBean> taskItems = serverTaskData.get(server);
        if (taskItems == null) {
            taskItems = Collections.synchronizedList(new LinkedList<ServerTaskBean>());
            serverTaskData.put(server, taskItems);
        }
        if (taskItems.size() >= 1000) {
            taskItems.remove(0);
        }

        taskItems.add(new ServerTaskBean(task, qps, DateUtil.format(new Date())));
        JSONObject pushData = new JSONObject();
        pushData.put("key", server.getName());
        pushData.put("value", getNowServerTask(server));
        AdminSocketService.massRole(CommandBuilder.builder().push(PushCode.syncTaskNum, pushData).build(), Sets.newHashSet(Role.ADMIN));
    }



    public void clear(ManagerSocketServer server) {
        serverTaskData.remove(server);
        Map<String, JSONObject> appData = serverOnlineAppData.remove(server);
        if (appData != null) {
            for (JSONObject data : appData.values()) {
                if (data.containsKey("userId")) {
                    removeUserCache(data.getString("userId"), data.getString("sessionId"));

                    //节点离线
					/*String userId = data.getString("userId");
					Live liveCache = managerLiveService.getLiveCacheByUserId(userId);
					if(liveCache != null) {
						managerLiveService.addAndPushPlayerDisconnectCache(liveCache.getId());
					}
					managerLiveService.addViewerDisconnectCache(userId);*/
                }
            }
        }
    }

    @Override
    protected Log logger() {
        return logger;
    }
}

class UserOnlineInfo {
    private String wsid;

    private String sessionId;

    private String ip;

    private ManagerSocketServer onlineServer;

    private Date onlineTime;

    public UserOnlineInfo(String sessionId, String wsid, String ip, ManagerSocketServer onlineServer, Date onlineTime) {
        super();
        this.sessionId = sessionId;
        this.wsid = wsid;
        this.ip = ip;
        this.onlineServer = onlineServer;
        this.onlineTime = onlineTime;
    }

    public String getWsid() {
        return wsid;
    }

    public void setWsid(String wsid) {
        this.wsid = wsid;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public ManagerSocketServer getOnlineServer() {
        return onlineServer;
    }

    public void setOnlineServer(ManagerSocketServer onlineServer) {
        this.onlineServer = onlineServer;
    }

    public Date getOnlineTime() {
        return onlineTime;
    }

    public void setOnlineTime(Date onlineTime) {
        this.onlineTime = onlineTime;
    }


}

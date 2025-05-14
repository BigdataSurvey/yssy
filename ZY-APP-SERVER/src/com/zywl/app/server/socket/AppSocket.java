package com.zywl.app.server.socket;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.google.common.util.concurrent.RateLimiter;
import com.live.app.ws.bean.Command;
import com.live.app.ws.bean.ConnectedData;
import com.live.app.ws.config.HttpSessionConfigurator;
import com.live.app.ws.constant.SocketConstants;
import com.live.app.ws.enums.PushCode;
import com.live.app.ws.enums.TargetSocketType;
import com.live.app.ws.socket.BaseServerSocket;
import com.live.app.ws.util.CommandBuilder;
import com.live.app.ws.util.Push;
import com.zywl.app.base.bean.*;
import com.zywl.app.base.constant.RedisKeyConstant;
import com.zywl.app.base.exp.AppException;
import com.zywl.app.defaultx.cache.AppConfigCacheService;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.cache.WsidCaCheService;
import com.zywl.app.defaultx.enmus.Top;
import com.zywl.app.defaultx.util.SpringUtil;
import com.zywl.app.server.service.GameBaseService;
import com.zywl.app.server.service.ServerConfigService;
import com.zywl.app.server.service.ServerStateService;
import com.zywl.app.server.service.TemplateLoadService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.websocket.server.ServerEndpoint;
import java.util.HashSet;
import java.util.Set;

@ServerEndpoint(value = "/APPServer" + SocketConstants.SOCKET_CONNECT_SHAKE_HANDS, configurator = HttpSessionConfigurator.class)
public class AppSocket extends BaseServerSocket {

    private static final Log logger = LogFactory.getLog(AppSocket.class);

    static Set<String> whiteList = new HashSet<String>();

    static {
        whiteList.add("001001"); //登录
        whiteList.add("001002"); //注册
        whiteList.add("001004"); //注册验证码
        whiteList.add("001005"); //登录验证码
        whiteList.add("001006"); //修改密码验证码
        whiteList.add("001007"); //修改密码
        whiteList.add("002004"); //切换语言
        whiteList.add("002005"); //更新坐标
        whiteList.add("012001"); //修改设备信息
        whiteList.add("022001"); //查询未读消息数量
        whiteList.add("022002"); //查询消息列表
        whiteList.add("012002"); //采集app崩溃日志
        whiteList.add("029003"); //修改分享关联
    }

    private WsidBean wsidBean;

    private Device device;

    private Location location;

    private AppConfigCacheService appConfigCacheService;

    private GameBaseService gameBaseService;

    private ServerStateService serverStateService;

    private TemplateLoadService templateLoadService;

    private ServerConfigService serverConfigService;


    private UserCacheService userCacheService;

    private User user;

    private WsidCaCheService wsidService;

    private RateLimiter rateLimiter;

    private int requestNum;


    public AppSocket() {
        super(TargetSocketType.app, true, false);
        gameBaseService = SpringUtil.getService(GameBaseService.class);
        serverStateService = SpringUtil.getService(ServerStateService.class);
        templateLoadService = SpringUtil.getService(TemplateLoadService.class);
        serverConfigService = SpringUtil.getService(ServerConfigService.class);
        wsidService = SpringUtil.getService(WsidCaCheService.class);
        userCacheService = SpringUtil.getService(UserCacheService.class);
        appConfigCacheService = SpringUtil.getService(AppConfigCacheService.class);
    }

    @Override
    public ConnectedData onConnect(JSONObject shakeHandsData) throws AppException {
        if (!ServerStateService.isService())
            throwExp("系统繁忙，请稍后");

        if (serverConfigService.getInteger(Config.SERVICE_STATUS) == 0) {
            String baiIp = appConfigCacheService.getConfigByKey(RedisKeyConstant.APP_CONFIG_BAI_IP, Config.BAI_IP);
            if (!ip.equals(baiIp)) {
                throwExp("系统维护中");
            }

        }
        if (wsidBean.getWsPrivateKey().equals(shakeHandsData.get("wsPrivateKey"))) {
            ConnectedData connectedData = new ConnectedData(getSessionId());
            User user = null;
            JSONObject responseShakeHandsData = new JSONObject();
            if (shakeHandsData.containsKey("device")) {
                device = shakeHandsData.getJSONObject("device").toJavaObject(Device.class);
            }
            if (shakeHandsData.containsKey("location")) {
                location = shakeHandsData.getJSONObject("location").toJavaObject(Location.class);
            }
            if (wsidBean.getUserId() != null) {
                user = userCacheService.getUserInfoById(wsidBean.getUserId());
                responseShakeHandsData.put("login", true);
                updateSocketInfo(user, false);

            } else {
                responseShakeHandsData.put("login", false);
            }

			
			/*File file = new File(Thread.currentThread().getContextClassLoader().getResource("").getPath() + "version.lock");
			if(file.exists()){
				try{
					String version = FileUtils.readFileToString(file, "UTF-8");
					responseShakeHandsData.put("disableVersion", version);
				}catch (Exception e) {
					logger.error("读取版本控制文件异常：" + e, e);
				}
			}*/
            JSONArray topList = new JSONArray();
            for (Top top : Top.values()) {
                JSONObject obj = new JSONObject();
                obj.put("topType", top.name());
                obj.put("name", top.getName());
                obj.put("hour", top.getHour());
                topList.add(obj);
            }
            responseShakeHandsData.put("top", topList);
            responseShakeHandsData.put("timestamp", System.currentTimeMillis());
            responseShakeHandsData.put("indexTab", JSON.parseArray(templateLoadService.loadTemplate("index_tab")));
//			responseShakeHandsData.put("indexBanner", JSON.parseArray(templateLoadService.loadTemplate("index_banner")));

            responseShakeHandsData.putAll(gameBaseService.getLoginData(this, user));
            JSONObject responseShakeHandsData2 = new JSONObject();
            responseShakeHandsData2.put("time", System.currentTimeMillis());
            connectedData.setResponseShakeHandsData(responseShakeHandsData2);
            rateLimiter = RateLimiter.create(5);
            syncAppOnline();
            return connectedData;
        } else {
            throw new AppException("握手数据不合法");
        }
    }

    protected void onDisconnect() {
        syncAppOffline();
        //移除大逃杀房间用户
        try {
            if (wsidBean.getUserId()!=null){
                gameBaseService.syncOffline(wsidBean.getUserId().toString());
            }
        } catch (Exception e) {
            logger.error("离线时清理数据异常，" + e);
        }

        //if(user != null){
        //当连接关闭的时候，开始超时
        //serverLiveService.playerDisconnect(this);
        //serverLiveService.viewerDisconnect(this);
        //}
    }

    private void syncAppOnline() {
        JSONObject pushData = getAppData();
        logger.debug("推送APP上线信息：" + pushData.toJSONString());
        Push.push(PushCode.syncAppOnline, null, pushData);
    }


    private void syncAppOffline() {
        JSONObject pushData = new JSONObject();
        pushData.put("wsid", wsidBean.getWsid());
        pushData.put("userId", wsidBean.getUserId());
        pushData.put("sessionId", getSessionId());
        logger.info("推送APP离线信息：" + pushData.toJSONString());
        Push.push(PushCode.syncAppOffline, null, pushData);
    }

    private void syncAppChange() {
        JSONObject pushData = getAppData();
        logger.debug("推送APP信息变更：" + pushData.toJSONString());
        Push.push(PushCode.syncAppChange, null, pushData);
    }

    public JSONObject getAppData() {
        JSONObject data = new JSONObject();
        data.put("wsid", wsidBean.getWsid());
        data.put("sessionId", getSessionId());
        data.put("ip", getIp());
        data.put("lastConnectTime", getConnectTime().getTime());
        if (device != null) {
            data.put("deviceId", device.getId());
            data.put("deviceBrand", device.getBrand());
            data.put("deviceType", device.getDeviceType());
        }
        if (user != null) {
            data.put("userId", user.getId());
            data.put("phone", user.getPhone());
            data.put("lastLoginTime", user.getLastLoginTime() == null ? null : user.getLastLoginTime().getTime());
        }
        return data;
    }

    @Override
    protected String getPrivateKey(String pk) {
        wsidBean = wsidService.getWsid(pk);
        return wsidBean == null ? null : wsidBean.getWsPrivateKey();
    }

    public void addAttr(String key, Object value) {
    }

    @SuppressWarnings("unchecked")
    public <T> T getAttr(String key) {
        return (T) wsidBean.getAttr().get(key);
    }

    public void removeAttr(String key) {
        if (wsidBean.getAttr().remove(key) != null) {
        }
    }

    public void updateSocketInfo(User user, boolean syncAppChange, boolean syncWsidState, boolean clearUserAction) {

        if (user == null) {
            gameBaseService.unregistLoginPush(this);
            gameBaseService.syncOffline(this.user.getId().toString());
        }
        this.user = user;
        this.wsidBean.setUserId(user == null ? null : user.getId());
        if (syncWsidState)
            wsidService.setWsid(this.wsidBean);
        if (syncAppChange)
            syncAppChange();
    }

    public void updateSocketInfo(User user, boolean syncAppChange) {
        updateSocketInfo(user, syncAppChange, true, true);
    }

    @Override
    protected void filterCommand(Command command) {
        if (command.isPush()) {
            command.setRequestTime(null);
            command.setResponseTime(null);
            command.setId(null);
            command.setLocale(null);
        }
    }

    public boolean isEncrypt(Command command) {
        if (PushCode.leaveLive.toString().equals(command.getCode())) {
            return false;
        }
        if (PushCode.joinLive.toString().equals(command.getCode())) {
            return false;
        }
        return true;
    }


    @Override
    protected Set<String> getWhiteList() {
        if (!ServerStateService.isService()) {
            throwExp("服务器拒绝服务");
        }
        if (getUser() != null) {
            return null;
        }
        return whiteList;
    }

    @Override
    public void disposeRequest(Command command) {
        if (requestNum > 5 && rateLimiter != null && !rateLimiter.tryAcquire() && !command.getCode().equals("007002")) {
            //可以在这里做计数，看触发了多少次，次数多了可以封号
            sendCommand(CommandBuilder.builder(command).error("").build());
        } else {
            requestNum++;
            super.disposeRequest(command);
        }

    }

	/*public long getLastPingTime() {
		return getLastActiveTime() > super.getLastPingTime() ? getLastActiveTime() : super.getLastPingTime();
	}*/

    public WsidBean getWsidBean() {
        return wsidBean;
    }

    public void setWsidBean(WsidBean wsidBean) {
        this.wsidBean = wsidBean;
    }

    /**
     * 用户如果session失效，则无法拿到当前会话中的user对象
     *
     * @return
     * @author DOE
     */
    public User getUser() {
        if (user != null) {
            long now = System.currentTimeMillis();
            if (now - getLastActiveTime() < WsidBean.TIMEOUT) {
                return user;
            } else {
                updateSocketInfo(null, true);
            }
        }
        return user;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }


    @Override
    protected Log logger() {
        return logger;
    }

}

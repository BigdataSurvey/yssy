package com.zywl.app.server.socket;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.bean.Command;
import com.live.app.ws.bean.PushBean;
import com.live.app.ws.enums.PushCode;
import com.live.app.ws.enums.TargetSocketType;
import com.live.app.ws.interfacex.PushListener;
import com.live.app.ws.socket.BaseClientSocket;
import com.live.app.ws.socket.BaseSocket;
import com.live.app.ws.util.DefaultPushHandler;
import com.live.app.ws.util.Push;
import com.zywl.app.defaultx.cache.UserCapitalCacheService;
import com.zywl.app.defaultx.enmus.LotteryGameStatusEnum;
import com.zywl.app.defaultx.service.IncomeRecordService;
import com.zywl.app.defaultx.service.UserCapitalService;
import com.zywl.app.defaultx.service.VersionService;
import com.zywl.app.defaultx.util.SpringUtil;
import com.zywl.app.server.service.ServerConfigService;
import com.zywl.app.server.service.ServerStateService;
import com.zywl.app.server.service.TemplateLoadService;
import com.zywl.app.server.service.UpdateAppService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.websocket.ClientEndpoint;
import java.util.Map;

@ClientEndpoint
public class DgsSocket extends BaseClientSocket {
    private static final Log logger = LogFactory.getLog(DgsSocket.class);


    private VersionService versionService;

    private UpdateAppService updateAppService;


    private IncomeRecordService incomeRecordService;

    private ServerConfigService serverConfigService;

    private UserCapitalService userCapitalService;

    private UserCapitalCacheService userCapitalCacheService;


    public DgsSocket(TargetSocketType socketType, int reconnect, String server, JSONObject shakeHandsDatas) {
        super(socketType, false, reconnect, server, shakeHandsDatas);
        versionService = SpringUtil.getService(VersionService.class);
        updateAppService = SpringUtil.getService(UpdateAppService.class);
        serverConfigService = SpringUtil.getService(ServerConfigService.class);
        incomeRecordService = SpringUtil.getService(IncomeRecordService.class);
        userCapitalService = SpringUtil.getService(UserCapitalService.class);
        userCapitalCacheService = SpringUtil.getService(UserCapitalCacheService.class);

        Push.addPushSuport(PushCode.syncIsService, new DefaultPushHandler() {
            public void onRegist(BaseSocket baseSocket, PushBean pushBean) {
                pushBean.setShakeHands(ServerStateService.isService());
            }
        });

        Push.addPushSuport(PushCode.updateDgsInfo, new DefaultPushHandler());
        Push.addPushSuport(PushCode.updateDgsStatus, new DefaultPushHandler());
        Push.addPushSuport(PushCode.updateGameStatus, new DefaultPushHandler());
    }

    @Override
    public void onConnect(Object data) {

        Push.registPush(new PushBean(PushCode.updateDgsInfo), new PushListener() {
            public void onRegist(BaseSocket baseSocket, Object data) {

            }

            public void onReceive(BaseSocket baseSocket, Object data) {
                logger.info("收到打怪兽信息变更" + data);
                    JSONObject obj = (JSONObject) data;
                    String gameId = obj.getString("gameId");
                    if ("10".equals(gameId)) {
                        Push.push(PushCode.updateDgsInfo, gameId, obj);
                    }

            }
        }, this);
        Push.registPush(new PushBean(PushCode.updateDgsStatus), new PushListener() {
            public void onRegist(BaseSocket baseSocket, Object data) {

            }

            public void onReceive(BaseSocket baseSocket, Object data) {
                logger.info("收到打怪兽状态变更" + data);
                JSONObject obj = JSONObject.from(data);
                String gameId = obj.getString("gameId");
                JSONArray ids = obj.getJSONArray("userIds");
                if ("10".equals(gameId)) {
                    for (Object id : ids) {
                        JSONObject result = new JSONObject();
                        String userId = (String) id;
                        result.put("userId",userId);
                        result.put("gameStatus", obj.get("status"));
                        result.put("userSettleInfo",obj.get("userSettleInfo"));
                        result.put("userId",userId);
                        Push.push(PushCode.updateDgsStatus, userId, result);
                    }

                }
            }
        }, this);


        JSONObject connectedData = ((JSONObject) data).getJSONObject("responseShakeHandsData");
        if (connectedData != null) {
            TemplateLoadService.staticWebUrl = connectedData.getString("staticWebUrl");
            TemplateLoadService.managerWebUrl = connectedData.getString("managerWebUrl");
        }
        new Thread("同步握手数据监测") {
            public void run() {
                try {
                    long t1 = System.currentTimeMillis();
                    logger.info("握手数据初始化完毕[" + (System.currentTimeMillis() - t1) + "ms]");
                    ServerStateService.startService();
                } catch (Exception e) {
                    logger.error("同步握手数据异常：" + e, e);
                }
            }

            ;
        }.start();
    }

    @Override
    public boolean isEncrypt(Command command) {
        return false;
    }

    @Override
    public void onDisconnect(int surplusReconnectNum) {
        logger.debug("剩余重连次数：" + surplusReconnectNum);
        //ServerStateService.stopService();
        //ServerNoticeService.setOpenNotice(false);
    }

    @Override
    protected Log logger() {
        return logger;
    }

}

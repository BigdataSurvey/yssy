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

@ClientEndpoint
public class PbxSocket extends BaseClientSocket {
    private static final Log logger = LogFactory.getLog(PbxSocket.class);


    private VersionService versionService;

    private UpdateAppService updateAppService;


    private IncomeRecordService incomeRecordService;

    private ServerConfigService serverConfigService;

    private UserCapitalService userCapitalService;

    private UserCapitalCacheService userCapitalCacheService;


    public PbxSocket(TargetSocketType socketType, int reconnect, String server, JSONObject shakeHandsDatas) {
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

        Push.addPushSuport(PushCode.updatePbxInfo, new DefaultPushHandler());
        Push.addPushSuport(PushCode.updatePbxStatus, new DefaultPushHandler());
        Push.addPushSuport(PushCode.updateGameStatus, new DefaultPushHandler());
    }

    @Override
    public void onConnect(Object data) {

        // 1) 透传 updatePbxInfo（全服广播信息：倒计时/奖池/近16/近100）
        Push.registPush(new PushBean(PushCode.updatePbxInfo), new PushListener() {
            @Override
            public void onRegist(BaseSocket baseSocket, Object data) {}

            @Override
            public void onReceive(BaseSocket baseSocket, Object data) {
                if (data == null) return;

                JSONObject obj;
                try {
                    obj = JSONObject.from(data);
                } catch (Exception e) {
                    logger.error("PBX updatePbxInfo payload parse error: " + data, e);
                    return;
                }

                // ★全服广播给 App 端：保持 code 不变
                Push.push(PushCode.updatePbxInfo, null, obj);
            }
        }, this);

        // 2) 桥接 updatePbxStatus -> updateGameStatus（单播给对应 userId）
        Push.registPush(new PushBean(PushCode.updatePbxStatus), new PushListener() {
            @Override
            public void onRegist(BaseSocket baseSocket, Object data) {}

            @Override
            public void onReceive(BaseSocket baseSocket, Object data) {
                if (data == null) return;

                JSONObject obj;
                try {
                    obj = JSONObject.from(data);
                } catch (Exception e) {
                    logger.error("PBX updatePbxStatus payload parse error: " + data, e);
                    return;
                }

                String gameId = obj.getString("gameId");
                JSONArray ids = obj.getJSONArray("userIds");
                if (ids == null || ids.isEmpty()) return;

                for (Object id : ids) {
                    String userId = String.valueOf(id);
                    JSONObject result = new JSONObject();

                    int status = obj.getIntValue("status");
                    result.put("status", status);
                    result.put("gameId", gameId);
                    result.put("userId", userId);

                    // 透传结算信息
                    if (obj.containsKey("userSettleInfo")) {
                        result.put("userSettleInfo", obj.get("userSettleInfo"));
                    }

                    // 合并统一汇总（近16/近100/总投入/总获得/时间）
                    mergeUnifiedSummary(result, obj, userId);

                    // ★关键：只单播 updateGameStatus 给客户端
                    Push.push(PushCode.updateGameStatus, userId, result);
                }
            }
        }, this);

        // 原握手逻辑保留
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

    /**
     * 合并统一摘要字段到推送结果：recent16Summary/recent100Periods/totalInvest/totalGain/serverTime
     */
    private static void mergeUnifiedSummary(JSONObject result, JSONObject obj, String userId) {
        if (result == null || obj == null) {
            return;
        }

        JSONObject summary = null;

        try {
            Object m = obj.get("userRecordSummaryMap");
            if (m instanceof JSONObject) {
                JSONObject map = (JSONObject) m;
                Object s = map.get(userId);
                if (s instanceof JSONObject) {
                    summary = (JSONObject) s;
                } else if (s != null) {
                    summary = JSONObject.from(s);
                }
            } else if (m instanceof java.util.Map) {
                JSONObject map = JSONObject.from(m);
                Object s = map.get(userId);
                if (s instanceof JSONObject) {
                    summary = (JSONObject) s;
                } else if (s != null) {
                    summary = JSONObject.from(s);
                }
            }
        } catch (Exception ignore) {
        }

        if (summary == null) {
            boolean hasAny = obj.containsKey("recent16Summary")
                    || obj.containsKey("recent100Periods")
                    || obj.containsKey("totalInvest")
                    || obj.containsKey("totalGain")
                    || obj.containsKey("serverTime");
            if (hasAny) {
                summary = obj;
            }
        }

        if (summary == null) {
            return;
        }

        if (summary.containsKey("recent16Summary")) {
            result.put("recent16Summary", summary.get("recent16Summary"));
        }
        if (summary.containsKey("recent100Periods")) {
            result.put("recent100Periods", summary.get("recent100Periods"));
        }
        if (summary.containsKey("totalInvest")) {
            result.put("totalInvest", summary.get("totalInvest"));
        }
        if (summary.containsKey("totalGain")) {
            result.put("totalGain", summary.get("totalGain"));
        }
        if (summary.containsKey("serverTime")) {
            result.put("serverTime", summary.get("serverTime"));
        }
    }
}

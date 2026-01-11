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
import com.live.app.ws.util.Executer;
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
import java.util.concurrent.CountDownLatch;

@ClientEndpoint
public class BattleRoyale2Socket extends BaseClientSocket {
    private static final Log logger = LogFactory.getLog(BattleRoyale2Socket.class);

    private VersionService versionService;
    private UpdateAppService updateAppService;
    private IncomeRecordService incomeRecordService;
    private ServerConfigService serverConfigService;
    private UserCapitalService userCapitalService;
    private UserCapitalCacheService userCapitalCacheService;

    public BattleRoyale2Socket(TargetSocketType socketType, int reconnect, String server, JSONObject shakeHandsDatas) {
        super(socketType, false, reconnect, server, shakeHandsDatas);
        versionService = SpringUtil.getService(VersionService.class);
        updateAppService = SpringUtil.getService(UpdateAppService.class);
        serverConfigService = SpringUtil.getService(ServerConfigService.class);
        incomeRecordService = SpringUtil.getService(IncomeRecordService.class);
        userCapitalService = SpringUtil.getService(UserCapitalService.class);
        userCapitalCacheService = SpringUtil.getService(UserCapitalCacheService.class);

        Push.addPushSuport(PushCode.syncTaskNum, new DefaultPushHandler() {
            public void onRegist(BaseSocket baseSocket, PushBean pushBean) {
                pushBean.setShakeHands(Executer.size() + "," + Executer.QPS());
            }
        });
        Push.addPushSuport(PushCode.syncIsService, new DefaultPushHandler() {
            public void onRegist(BaseSocket baseSocket, PushBean pushBean) {
                pushBean.setShakeHands(ServerStateService.isService());
            }
        });
    }

    @Override
    public void onConnect(Object data) {
        CountDownLatch downLatch = new CountDownLatch(2);

        // 先注册 PBX 推送
        Push.registPush(new PushBean(PushCode.updatePbxInfo), new PushListener() {
            @Override
            public void onRegist(BaseSocket baseSocket, Object data) {
                downLatch.countDown();
            }

            @Override
            public void onReceive(BaseSocket baseSocket, Object data) {
                logger.info("收到推箱子信息变更" + data);
                JSONObject obj = JSONObject.from(data);
                String gameId = obj.getString("gameId");
                if ("12".equals(gameId)) {
                    Push.push(PushCode.updatePbxInfo, gameId, obj);
                }
            }
        }, this);

        Push.registPush(new PushBean(PushCode.updatePbxStatus), new PushListener() {
            @Override
            public void onRegist(BaseSocket baseSocket, Object data) {
                downLatch.countDown();
            }

            @Override
            public void onReceive(BaseSocket baseSocket, Object data) {
                logger.info("收到推箱子状态变更" + data);
                JSONObject obj = JSONObject.from(data);
                String gameId = obj.getString("gameId");
                JSONArray ids = obj.getJSONArray("userIds");
                if ("12".equals(gameId) && ids != null) {
                    for (Object id : ids) {
                        String userId = (String) id;
                        JSONObject result = new JSONObject();
                        result.put("userId", userId);
                        result.put("gameStatus", obj.get("status"));
                        result.put("userSettleInfo", obj.get("userSettleInfo"));
                        mergeUnifiedSummary(result, obj, userId);
                        Push.push(PushCode.updatePbxStatus, userId, result);
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
                    downLatch.await();
                    logger.info("握手数据初始化完毕[" + (System.currentTimeMillis() - t1) + "ms]");
                    ServerStateService.startService();
                } catch (Exception e) {
                    logger.error("同步握手数据异常：" + e, e);
                }
            }
        }.start();
    }

    static void dtsPublic(JSONObject obj, JSONObject result) {
        if (LotteryGameStatusEnum.gaming.getValue() == obj.getIntValue("status")) {
            result.put("endTime", obj.get("endTime"));
        } else if (LotteryGameStatusEnum.ready.getValue() == obj.getIntValue("status")) {
            result.put("lookList", obj.get("lookList"));
            result.put("roomList", obj.get("roomList"));
            result.put("lastResult", obj.get("lastResult"));
            result.put("periodsNum", obj.get("periodsNum"));
        }
    }

    @Override
    public void onDisconnect(int surplusReconnectNum) {
        logger.debug("剩余重连次数：" + surplusReconnectNum);
    }

    @Override
    public boolean isEncrypt(Command command) {
        return false;
    }

    @Override
    protected Log logger() {
        return logger;
    }

    /**
     * 合并统一摘要字段到推送结果：
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

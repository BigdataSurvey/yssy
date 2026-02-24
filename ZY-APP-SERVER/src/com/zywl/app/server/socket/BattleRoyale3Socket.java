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
public class BattleRoyale3Socket extends BaseClientSocket {
    private static final Log logger = LogFactory.getLog(BattleRoyale3Socket.class);

    private VersionService versionService;
    private UpdateAppService updateAppService;
    private IncomeRecordService incomeRecordService;
    private ServerConfigService serverConfigService;
    private UserCapitalService userCapitalService;
    private UserCapitalCacheService userCapitalCacheService;

    public BattleRoyale3Socket(TargetSocketType socketType, int reconnect, String server, JSONObject shakeHandsDatas) {
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

        // DTS3 房间信息
        Push.registPush(new PushBean(PushCode.updateDts3Info), new PushListener() {
            @Override
            public void onRegist(BaseSocket baseSocket, Object data) {
                downLatch.countDown();
            }

            @Override
            public void onReceive(BaseSocket baseSocket, Object data) {
                if (data == null) return;

                JSONArray array = new JSONArray();
                if (data instanceof JSONObject) {
                    array.add((JSONObject) data);
                } else if (data instanceof JSONArray) {
                    array = (JSONArray) data;
                } else {
                    try {
                        array = JSONArray.from(data);
                    } catch (Exception e) {
                        try {
                            array.add(JSONObject.from(data));
                        } catch (Exception ex) {
                            logger.error("DTS3推送格式错误，无法解析: " + data, ex);
                            return;
                        }
                    }
                }

                for (Object o : array) {
                    JSONObject obj = JSONObject.from(o);
                    String gameId = obj.getString("gameId");
                    if ("1".equals(gameId)) {
                        Push.push(PushCode.updateRoomDate, gameId, obj);
                    }
                }
            }
        }, this);

        // DTS3 游戏状态
        Push.registPush(new PushBean(PushCode.updateDts3Status), new PushListener() {
            @Override
            public void onRegist(BaseSocket baseSocket, Object data) {
                downLatch.countDown();
            }

            @Override
            @SuppressWarnings("unchecked")
            public void onReceive(BaseSocket baseSocket, Object data) {
                if (data == null) return;

                JSONObject obj;
                try {
                    obj = (data instanceof JSONObject) ? (JSONObject) data : JSONObject.from(data);
                } catch (Exception e) {
                    logger.error("DTS3 updateDts3Status 推送数据无法解析: " + data, e);
                    return;
                }

                String gameId = obj.getString("gameId");
                JSONArray ids = obj.getJSONArray("userIds");

                if (!"1".equals(gameId) || ids == null) {
                    return;
                }

                // userSettleInfo 可能是 JSONObject / Map / 甚至 null，这里统一转成 Map
                Map<String, Object> userSettleMap = null;
                try {
                    Object usi = obj.get("userSettleInfo");
                    if (usi instanceof Map) {
                        userSettleMap = (Map<String, Object>) usi;
                    } else if (usi != null) {
                        userSettleMap = JSONObject.from(usi);
                    }
                } catch (Exception ignore) {
                    userSettleMap = null;
                }

                // 顶层兼容映射（你在 DTS3 推送里可能会塞 getAmountMap/amountMap 等）
                JSONObject getAmountMap = null;
                JSONObject amountMap = null;
                JSONObject awardMap = null;
                JSONObject gainMap = null;
                try {
                    getAmountMap = obj.getJSONObject("getAmountMap");
                    amountMap = obj.getJSONObject("amountMap");
                    awardMap = obj.getJSONObject("awardMap");
                    gainMap = obj.getJSONObject("gainMap");
                } catch (Exception ignore) {}

                for (Object id : ids) {
                    String userId = String.valueOf(id);

                    JSONObject result = new JSONObject();

                    if (LotteryGameStatusEnum.settle.getValue() == obj.getIntValue("status")) {

                        Map<String, Object> one = null;
                        if (userSettleMap != null) {
                            Object oneObj = userSettleMap.get(userId);
                            if (oneObj instanceof Map) {
                                one = (Map<String, Object>) oneObj;
                            } else if (oneObj != null) {
                                try {
                                    one = JSONObject.from(oneObj);
                                } catch (Exception ignore) {
                                    one = null;
                                }
                            }
                        }

                        // 默认：未知/未找到该用户结算信息 => roomResult=2（你原来的逻辑就是 2）
                        if (one == null) {
                            result.put("roomResult", 2);
                            result.put("isBot", "0");
                            result.put("winAmount", "0");
                            result.put("betAmount", "0");
                        } else {
                            String isBot = one.get("isBot") == null ? "0" : String.valueOf(one.get("isBot"));
                            String winAmount = one.get("winAmount") == null ? null : String.valueOf(one.get("winAmount"));
                            String betAmount = one.get("betAmount") == null ? null : String.valueOf(one.get("betAmount"));
                            String isWin = one.get("isWin") == null ? null : String.valueOf(one.get("isWin"));

                            // ✅关键：安全解析，杜绝 NumberFormatException
                            int roomResult = safeParseInt(isWin, 2);

                            result.put("isBot", isBot);
                            result.put("winAmount", winAmount == null ? "0" : winAmount);
                            result.put("betAmount", betAmount == null ? "0" : betAmount);
                            result.put("roomResult", roomResult);

                            // 顶层兼容：给前端可能直接取 totalGain/totalInvest 的情况兜底
                            result.put("totalInvest", result.getString("betAmount"));
                            result.put("totalGain", result.getString("winAmount"));

                            // 兼容字段兜底（如果 settle 推送没带齐）
                            String gain = result.getString("winAmount");
                            result.put("award", gain);
                            result.put("gain", gain);
                            result.put("amount", gain);
                            result.put("getAmount", gain);
                        }

                        // 如果你 DTS3 顶层补了 map（getAmountMap/amountMap/...），这里也可以兜底覆盖
                        try {
                            if (getAmountMap != null && getAmountMap.containsKey(userId)) {
                                String v = String.valueOf(getAmountMap.get(userId));
                                result.put("getAmount", v);
                                result.put("totalGain", v);
                            }
                            if (amountMap != null && amountMap.containsKey(userId)) {
                                result.put("amount", String.valueOf(amountMap.get(userId)));
                            }
                            if (awardMap != null && awardMap.containsKey(userId)) {
                                result.put("award", String.valueOf(awardMap.get(userId)));
                            }
                            if (gainMap != null && gainMap.containsKey(userId)) {
                                result.put("gain", String.valueOf(gainMap.get(userId)));
                            }
                        } catch (Exception ignore) {}

                    } else {
                        dtsPublic(obj, result);
                    }

                    result.put("allLoseAmount", obj.get("allLoseAmount"));
                    result.put("roomIds", obj.get("roomIds"));
                    result.put("status", obj.get("status"));
                    result.put("userId", userId);

                    mergeUnifiedSummary(result, obj, userId);

                    Push.push(PushCode.updateGameStatus, userId, result);
                }
            }
        }, this);

        // DTS3 用户离开房间推送
        Push.registPush(new PushBean(PushCode.updateDts3UserLeave), new PushListener() {
            @Override
            public void onRegist(BaseSocket baseSocket, Object data) {}

            @Override
            public void onReceive(BaseSocket baseSocket, Object data) {
                if (data == null) return;
                JSONObject obj = JSONObject.from(data);
                String gameId = obj.getString("gameId");
                Push.push(PushCode.updateDts3UserLeave, gameId, obj);
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
    private int safeParseInt(String v, int def) {
        if (v == null) return def;
        v = v.trim();
        if (v.length() == 0) return def;
        try { return Integer.parseInt(v); } catch (Exception e) { return def; }
    }
}

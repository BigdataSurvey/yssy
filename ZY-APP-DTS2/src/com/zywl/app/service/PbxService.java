package com.zywl.app.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.bean.Command;
import com.live.app.ws.enums.PushCode;
import com.live.app.ws.interfacex.Listener;
import com.live.app.ws.socket.BaseClientSocket;
import com.live.app.ws.util.CommandBuilder;
import com.live.app.ws.util.DefaultPushHandler;
import com.live.app.ws.util.Executer;
import com.live.app.ws.util.Push;
import com.zywl.app.base.bean.Game;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.OrderUtil;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.service.GameService;
import com.zywl.app.socket.BattleRoyaleSocketServer2;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 重要：PBX 下注采用 ACK 模式。
 * - 102103 立即回 ACK（仅表示“已受理”），不等待 Manager 扣款返回。
 * - 扣款/余额/奖池结果只通过 push(updatePbxStatus/updatePbxInfo) 下发。
 * 目的：避免 DTS2 -> Manager 扣款链路波动导致 102103 request 超时（你当前日志已出现“任务已超时”）。
 */

/**
 * 推箱子（PBX）- Step2-1：下注闭环（DTS2 -> Manager 扣款 + 返回余额/奖池，再 push）
 *
 * 约定：
 * - PBX gameId=12
 * - 指令码：102101(join) / 102103(op|bet) / 102104(leave)
 * - Push：updatePbxInfo(condition=gameId) / updatePbxStatus(condition=userId)
 * - 配置：来自 l_game(id=12).game_setting
 */
@Service
@ServiceClass(code = "102")
public class PbxService extends BaseService {

    private static final Log logger = LogFactory.getLog(PbxService.class);

    /** PBX 固定 gameId */
    public static final long PBX_GAME_ID = 12L;

    /** 内存 join 状态（重启丢失属于预期） */
    private final Map<String, JSONObject> onlineUserState = new ConcurrentHashMap<>();

    /** 配置缓存 */
    private volatile JSONObject PBX_GAME_SETTING = new JSONObject();

    /** 解析后的配置字段（避免魔法值） */
    private volatile int TIME_SEC = 60;
    private volatile int CAPITAL_TYPE = 1002;
    private volatile JSONArray CHIPS = new JSONArray();
    private volatile BigDecimal FEE_RATE = new BigDecimal("0.05");

    @Autowired
    private GameService gameService;

    @Autowired
    private BattleRoyaleRequsetMangerService2 requsetMangerService2;

    @PostConstruct
    public void init() {
        // 声明 push 支持（必须）
        Push.addPushSuport(PushCode.updatePbxInfo, new DefaultPushHandler());
        Push.addPushSuport(PushCode.updatePbxStatus, new DefaultPushHandler());

        initGameSetting();

        logger.info("[PBX] init ok. gameId=" + PBX_GAME_ID
                + ", time=" + TIME_SEC
                + ", capitalType=" + CAPITAL_TYPE
                + ", chips=" + CHIPS
                + ", feeRate=" + FEE_RATE);
    }

    /** 参考 BattleRoyaleService2：从 l_game(id=12).game_setting 初始化 */
    public void initGameSetting() {
        try {
            Game game = gameService.findGameById(PBX_GAME_ID);
            if (game == null || game.getGameSetting() == null || game.getGameSetting().trim().isEmpty()) {
                logger.warn("[PBX] l_game(game_setting) empty, fallback default");
                return;
            }
            PBX_GAME_SETTING = JSON.parseObject(game.getGameSetting());
            TIME_SEC = PBX_GAME_SETTING.getIntValue("time", 60);
            CAPITAL_TYPE = PBX_GAME_SETTING.getIntValue("capitalType", 1002);
            CHIPS = PBX_GAME_SETTING.getJSONArray("chips");
            if (CHIPS == null) CHIPS = new JSONArray();

            String feeRateStr = PBX_GAME_SETTING.getString("feeRate");
            if (feeRateStr != null && !feeRateStr.trim().isEmpty()) {
                FEE_RATE = new BigDecimal(feeRateStr.trim());
            }
            logger.info("[PBX] initGameSetting success: " + PBX_GAME_SETTING.toJSONString());
        } catch (Exception e) {
            logger.error("[PBX] initGameSetting failed, fallback default", e);
        }
    }

    /** 102101：加入房间 */
    @ServiceMethod(code = "101", description = "推箱子-加入房间")
    public Object joinRoom(BattleRoyaleSocketServer2 adminSocketServer, Command lotteryCommand, JSONObject data) {
        checkNull(data);
        checkNull(data.get("userId"));

        String userId = data.getString("userId");
        String userNo = data.getString("userNo");
        if (userNo == null || userNo.isEmpty()) userNo = "U" + userId;

        JSONObject state = new JSONObject();
        state.put("gameId", String.valueOf(PBX_GAME_ID));
        state.put("userId", userId);
        state.put("userNo", userNo);
        state.put("status", 1);
        state.put("ts", System.currentTimeMillis());
        onlineUserState.put(userId, state);

        // 广播 pbxInfo（condition=gameId）
        Push.push(PushCode.updatePbxInfo, String.valueOf(PBX_GAME_ID), buildPbxInfo(null));

        JSONObject resp = new JSONObject();
        resp.put("success", true);
        resp.put("gameId", String.valueOf(PBX_GAME_ID));
        resp.put("userId", userId);
        resp.put("userNo", userNo);
        resp.put("status", 1);
        resp.put("onlineCount", onlineUserState.size());
        resp.put("gameSetting", PBX_GAME_SETTING);
        return resp;
    }

    /**
     * 102103：操作/下注
     *
     * Step2-1 约定：debug servlet 先支持 betAmount（元，2位），后续 Step2-2 再升级到 chip+elementId
     */
    @ServiceMethod(code = "103", description = "推箱子-操作/下注")
    public Object operate(BattleRoyaleSocketServer2 adminSocketServer, Command lotteryCommand, JSONObject data) {
        checkNull(data);
        checkNull(data.get("userId"));

        String userId = data.getString("userId");
        if (!onlineUserState.containsKey(userId)) {
            JSONObject err = new JSONObject();
            err.put("success", false);
            err.put("message", "PBX not joined");
            err.put("gameId", String.valueOf(PBX_GAME_ID));
            return err;
        }

        // 兼容旧的 status=2 调试；真正下注用 betAmount
        int status = data.getIntValue("status", 2);

        BigDecimal betAmount = data.getBigDecimal("betAmount");
        if (betAmount == null) {
            // 没下注金额：仍按旧逻辑立即回包（只做状态联调）
            JSONObject resp = new JSONObject();
            resp.put("success", true);
            resp.put("gameId", String.valueOf(PBX_GAME_ID));
            resp.put("userId", userId);
            resp.put("status", status);
            resp.put("gameSetting", PBX_GAME_SETTING);

            // push 用户状态（condition=userId）
            JSONObject pushData = buildPbxStatusPush(userId, status, true, null, null, null, null, null, null, null);
            Push.push(PushCode.updatePbxStatus, userId, pushData);

            return resp;
        }

        // 有下注金额：走 Manager 扣款（200720）
        betAmount = betAmount.setScale(2, RoundingMode.HALF_UP);
        if (betAmount.compareTo(BigDecimal.ZERO) <= 0) {
            JSONObject err = new JSONObject();
            err.put("success", false);
            err.put("message", "betAmount 非法");
            err.put("gameId", String.valueOf(PBX_GAME_ID));
            return err;
        }

        String orderNo = data.getString("orderNo");
        if (orderNo == null || orderNo.trim().isEmpty()) {
            orderNo = OrderUtil.getOrder5Number();
        }

        JSONObject betReq = new JSONObject();
        betReq.put("gameId", String.valueOf(PBX_GAME_ID));
        betReq.put("userId", userId);
        betReq.put("capitalType", CAPITAL_TYPE);
        betReq.put("betAmount", betAmount);
        betReq.put("feeRate", FEE_RATE);
        betReq.put("orderNo", orderNo);

        // ACK：立即回包（只代表受理成功），真实扣款结果走 push
        JSONObject ack = new JSONObject();
        ack.put("success", true);
        ack.put("ack", true);
        ack.put("gameId", String.valueOf(PBX_GAME_ID));
        ack.put("userId", userId);
        ack.put("status", status);
        ack.put("betAmount", betAmount);
        ack.put("orderNo", orderNo);
        ack.put("gameSetting", PBX_GAME_SETTING);

        BigDecimal finalBetAmount = betAmount;
        String finalOrderNo = orderNo;
        requsetMangerService2.requestPbxBet(betReq, new Listener() {
            @Override
            public void handle(BaseClientSocket clientSocket, Command command) {
                if (command.isSuccess()) {
                    JSONObject mgr = command.getData() == null ? new JSONObject() : (JSONObject) command.getData();
                    BigDecimal balance = mgr.getBigDecimal("balance");
                    BigDecimal poolBalance = mgr.getBigDecimal("poolBalance");

                    // 兼容 Manager 返回 fee/feeRate
                    BigDecimal fee = mgr.getBigDecimal("fee");
                    BigDecimal feeRate = mgr.getBigDecimal("feeRate");

                    // push：用户维度
                    JSONObject pushStatus = buildPbxStatusPush(userId, status, true, null, finalOrderNo, finalBetAmount, balance, poolBalance, fee, feeRate);
                    Push.push(PushCode.updatePbxStatus, userId, pushStatus);

                    // push：全局维度（带 poolBalance）
                    Push.push(PushCode.updatePbxInfo, String.valueOf(PBX_GAME_ID), buildPbxInfo(poolBalance));
                } else {
                    // Manager 扣款失败：只 push 错误（ACK 已回）
                    JSONObject pushStatus = buildPbxStatusPush(userId, status, false, command.getMessage(), finalOrderNo, finalBetAmount, null, null, null, null);
                    if (command.getData() instanceof JSONObject) {
                        pushStatus.put("mgrData", command.getData());
                    }
                    Push.push(PushCode.updatePbxStatus, userId, pushStatus);
                }
            }
        });

        // 重要：ACK 直接返回；后续结果只走 push
        return ack;
    }

    /** 102104：离开房间 */
    @ServiceMethod(code = "104", description = "推箱子-离开房间")
    public Object leaveRoom(BattleRoyaleSocketServer2 adminSocketServer, Command lotteryCommand, JSONObject data) {
        checkNull(data);
        checkNull(data.get("userId"));

        String userId = data.getString("userId");
        onlineUserState.remove(userId);

        Push.push(PushCode.updatePbxInfo, String.valueOf(PBX_GAME_ID), buildPbxInfo(null));

        JSONObject resp = new JSONObject();
        resp.put("success", true);
        resp.put("gameId", String.valueOf(PBX_GAME_ID));
        resp.put("userId", userId);
        resp.put("status", 0);
        return resp;
    }

    private JSONObject buildPbxInfo(BigDecimal poolBalance) {
        JSONObject info = new JSONObject();
        info.put("gameId", String.valueOf(PBX_GAME_ID));
        info.put("onlineCount", onlineUserState.size());
        info.put("gameSetting", PBX_GAME_SETTING);
        if (poolBalance != null) info.put("poolBalance", poolBalance);
        info.put("ts", System.currentTimeMillis());
        return info;
    }

    private JSONObject buildPbxStatusPush(String userId,
                                          int status,
                                          boolean success,
                                          String message,
                                          String orderNo,
                                          BigDecimal betAmount,
                                          BigDecimal balance,
                                          BigDecimal poolBalance,
                                          BigDecimal fee,
                                          BigDecimal feeRate) {
        JSONObject pushData = new JSONObject();
        pushData.put("gameId", String.valueOf(PBX_GAME_ID));

        JSONArray userIds = new JSONArray();
        userIds.add(userId);
        pushData.put("userIds", userIds);

        pushData.put("status", status);
        pushData.put("success", success);
        if (message != null) {
            pushData.put("message", message);
        }
        if (orderNo != null) {
            pushData.put("orderNo", orderNo);
        }

        JSONObject userSettleInfo = new JSONObject();
        userSettleInfo.put("userId", userId);
        userSettleInfo.put("ts", System.currentTimeMillis());
        if (betAmount != null) userSettleInfo.put("betAmount", betAmount);
        if (balance != null) userSettleInfo.put("balance", balance);
        if (poolBalance != null) userSettleInfo.put("poolBalance", poolBalance);
        if (fee != null) userSettleInfo.put("fee", fee);
        if (feeRate != null) userSettleInfo.put("feeRate", feeRate);
        pushData.put("userSettleInfo", userSettleInfo);

        return pushData;
    }
}

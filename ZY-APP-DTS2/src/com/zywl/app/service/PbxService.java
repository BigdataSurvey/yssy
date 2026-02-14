package com.zywl.app.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.bean.Command;
import com.live.app.ws.enums.PushCode;
import com.live.app.ws.interfacex.Listener;
import com.live.app.ws.socket.BaseClientSocket;
import com.live.app.ws.util.Push;
import com.zywl.app.base.bean.Config;
import com.zywl.app.base.bean.Game;
import com.zywl.app.base.bean.User;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.StringUtils;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.enmus.UserCapitalTypeEnum;
import com.zywl.app.defaultx.service.BattleRoyaleRecord2Service;
import com.zywl.app.defaultx.service.ConfigService;
import com.zywl.app.defaultx.service.GameService;
import com.zywl.app.defaultx.service.UserService;
import com.zywl.app.socket.BattleRoyaleSocketServer2;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @Author: lzx
 * @Create: 2025/12/20
 * @Version: V1.0
 * @Description: 推箱子 (PBX) / 翻牌牌 核心业务服务
 */

@Service
@ServiceClass(code = "102")
public class PbxService extends BaseService {

    private static final Log log = LogFactory.getLog(PbxService.class);

    /** 时间格式化工具 (ThreadLocal，线程安全) */
    private static final ThreadLocal<SimpleDateFormat> PBX_SDF = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"));
    private static final ThreadLocal<SimpleDateFormat> PBX_ORDER_SDF = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyyMMddHHmmssSSS"));

    /** 游戏 ID (对应数据库 l_game.id) */
    private static final int PBX_GAME_ID = 12;

    /** 机器人用户池缓存 (UserId -> UserObj) */
    private static final Map<String, User> BOT_USER = new ConcurrentHashMap<>();

    /** 游戏完整配置快照 */
    private volatile JSONObject PBX_GAME_SETTING = new JSONObject();

    /** 单期游戏时长 (秒) */
    private volatile int TIME_SEC = 20;

    /** 游戏元素数量 (ElementId 范围: 1 .. ELEMENT_COUNT) */
    private volatile int ELEMENT_COUNT = 6;

    /** 下注扣款的资产类型 ID (1002: 游戏消耗货币) */
    private volatile int CAPITAL_TYPE = UserCapitalTypeEnum.xxxhhb.getValue();

    /** 平台手续费率 (例如 0.05 代表 5%) */
    private volatile BigDecimal FEE_RATE = new BigDecimal("0.05");

    /** 可选筹码档位列表 (格式: ["1", "10", "100"]) */
    private volatile JSONArray CHIPS = new JSONArray();

    /** 赔率配置 */
    private volatile BigDecimal MULT_TRIPLE = new BigDecimal("10");     // 三同号
    private volatile BigDecimal MULT_DOUBLE = new BigDecimal("4");      // 两同号
    private volatile BigDecimal MULT_ALL_DIFF = new BigDecimal("1.8");  // 三不同号

    /** 周期调度器相关 */
    private final AtomicBoolean periodSchedulerStarted = new AtomicBoolean(false);
    private ScheduledExecutorService periodScheduler;
    private final Object PERIOD_LOCK = new Object();

    /** 当前期号信息 */
    private volatile long currentPeriodBucket = -1L; // 时间桶
    private volatile String currentPeriodNo = null;  // 期号
    private volatile long currentPeriodStartMs = 0L; // 开始时间
    private volatile long currentPeriodEndMs = 0L;   // 结束时间

    /** 结算去重集合，防止同一期号重复结算 */
    private final Set<String> settledPeriodNoSet = ConcurrentHashMap.newKeySet();

    /** 全服数据缓存 */
    private volatile BigDecimal lastPoolBalance = null; // 奖池余额
    private volatile String lastServerTime = null;      // 服务器时间

    /** 历史记录缓存 */
    private final Deque<JSONObject> recent16Results = new ArrayDeque<>();  // 近16期详细
    private final Deque<JSONArray> recent100Results = new ArrayDeque<>();  // 近100期简略

    /** 在线用户状态缓存 (UserId -> StatusObj) */
    private final Map<String, JSONObject> onlineUserState = new ConcurrentHashMap<>();

    /** 本期下注数据 (周期重置) */
    private final Map<Integer, BigDecimal> periodElementTotalBet = new ConcurrentHashMap<>();           // 元素总注
    private final Map<String, Map<Integer, BigDecimal>> periodUserElementBet = new ConcurrentHashMap<>(); // 用户各元素注
    private final Map<String, BigDecimal> periodUserTotalBet = new ConcurrentHashMap<>();               // 用户总注

    /** 当前周 Key (yyyyWW) */
    private volatile int currentWeekKey = 0;

    /** 本周玩家累计投入 (用于榜单计算) */
    private final Map<String, BigDecimal> weekUserTotalBet = new ConcurrentHashMap<>();
    /** 上周玩家累计投入快照 */
    private final Map<String, BigDecimal> lastWeekUserTotalBet = new ConcurrentHashMap<>();

    /** 榜单奖池 */
    private volatile BigDecimal weekRankPoolBalance = BigDecimal.ZERO;
    private volatile BigDecimal lastWeekRankPoolBalance = BigDecimal.ZERO;

    // 本周统计显示字段
    private volatile JSONArray weekRankTop10 = new JSONArray();
    private volatile BigDecimal weekConsume = BigDecimal.ZERO;
    private volatile BigDecimal weekReturn = BigDecimal.ZERO;
    private volatile BigDecimal weekProfit = BigDecimal.ZERO;
    private volatile BigDecimal weekDividendPool = BigDecimal.ZERO;
    private volatile boolean weekSettled = false;

    // 上周统计显示字段
    private volatile JSONArray lastWeekRankTop10 = new JSONArray();
    private volatile BigDecimal lastWeekConsume = BigDecimal.ZERO;
    private volatile BigDecimal lastWeekReturn = BigDecimal.ZERO;
    private volatile BigDecimal lastWeekProfit = BigDecimal.ZERO;
    private volatile BigDecimal lastWeekDividendPool = BigDecimal.ZERO;
    private volatile boolean lastWeekSettled = false;

    // 个人周榜数据
    private volatile BigDecimal myWeekConsume = BigDecimal.ZERO;
    private volatile int myWeekRank = -1;
    private volatile BigDecimal myLastWeekConsume = BigDecimal.ZERO;
    private volatile int myLastWeekRank = -1;


    /** 机器人下注概率（0~100） */
    private volatile int NEED_BOT = 0;
    /** 机器人可用筹码 */
    private final List<BigDecimal> BOT_CHIPS = new ArrayList<>();
    /** 本期机器人下注总额 (仅展示用) */
    private final Map<Integer, BigDecimal> periodBotElementTotalBet = new ConcurrentHashMap<>();
    /** 机器人调度器 */
    private ScheduledExecutorService botScheduler;
    private final AtomicBoolean botSchedulerStarted = new AtomicBoolean(false);

    @Autowired
    private GameService gameService;
    @Autowired
    private BattleRoyaleRequsetMangerService2 requsetMangerService2;
    @Autowired
    private BattleRoyaleRecord2Service battleRoyaleRecord2Service;
    @Autowired
    private ConfigService configService;
    @Autowired
    private UserService userService;

    /**
     * 服务启动入口：初始化配置、状态机、调度器及机器人
     */
    @PostConstruct
    public void init() {
        long t1 = System.currentTimeMillis();
        log.info("================ [PBX] 服务初始化 ================");
        try {
            // 初始化游戏静态配置
            initGameSetting();
            // 初始化期号状态
            ensureCurrentPeriod(t1);
            // 启动核心周期调度器 (Tick)
            startPeriodScheduler();
            // 初始化并启动机器人
            initBotUsers();
            initBotConfig();
            startBotScheduler();
            log.info("================ [PBX] 服务已启动 (Cost: " + (System.currentTimeMillis() - t1) + "ms) ================");
        } catch (Exception e) {
            log.error("[PBX] 初始化失败", e);
            throw new RuntimeException("[PBX] 初始化失败", e);
        }
    }

    /**
     * 启动主周期调度器 (1秒/次)，用于驱动期号切换
     */
    private void startPeriodScheduler() {
        if (periodSchedulerStarted.compareAndSet(false, true)) {
            periodScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r);
                t.setName("pbx-period-scheduler");
                t.setDaemon(true);
                return t;
            });
            // 每秒 tick 一次: 检测期号切换、推送倒计时
            periodScheduler.scheduleAtFixedRate(() -> {
                try {
                    tickPeriod();
                } catch (Throwable t) {
                    log.error("[PBX] tickPeriod error", t);
                }
            }, 1, 1, TimeUnit.SECONDS);
        }
    }

    /**
     * 每秒驱动逻辑：状态机推进与广播
     */
    private void tickPeriod() {
        long nowMs = System.currentTimeMillis();
        // 推进期状态机 (切换期号、触发结算)
        ensureCurrentPeriod(nowMs);
        // 若有在线用户，推送倒计时与奖池信息
        if (!onlineUserState.isEmpty()) {
            try {
                pushPbxInfo(lastPoolBalance);
            } catch (Exception e) {
                log.error("[PBX] tickPeriod pushPbxInfo error", e);
            }
        }
    }

    /**
     * 启动机器人调度器 (100ms/次)
     */
    private void startBotScheduler() {
        if (botSchedulerStarted.compareAndSet(false, true)) {
            botScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r);
                t.setName("pbx-bot-scheduler");
                t.setDaemon(true);
                return t;
            });
            botScheduler.scheduleAtFixedRate(() -> {
                try {
                    tickBot();
                } catch (Throwable t) {
                    log.error("[PBX] tickBot error", t);
                }
            }, 0, 100, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * 用户加入房间 处理用户连接，初始化在线状态，返回游戏快照信息
     */
    @ServiceMethod(code = "101", description = "推箱子-加入房间")
    public Object joinRoom(BattleRoyaleSocketServer2 adminSocketServer, Command lotteryCommand, JSONObject data) {
        long nowMs = System.currentTimeMillis();
        checkNull(data);

        String userId = data.getString("userId");
        if (isBlank(userId)) {
            throwExp("参数错误");
        }

        // userNo 兜底
        String userNo = data.getString("userNo");
        if (isBlank(userNo)) {
            userNo = "U" + userId;
        }

        // 更新在线状态
        JSONObject state = new JSONObject();
        state.put("userId", userId);
        state.put("userNo", userNo);
        state.put("status", 1);
        state.put("ts", System.currentTimeMillis());
        onlineUserState.put(userId, state);

        // 实时查询主服奖池
        JSONObject q = queryPoolFromManager(userId);
        BigDecimal poolBalance = q.getBigDecimal("poolBalance");
        String serverTime = q.getString("serverTime");
        BigDecimal myTotalConsume = q.getBigDecimal("myTotalConsume");
        BigDecimal myTotalReturn = q.getBigDecimal("myTotalReturn");
        BigDecimal myTotalNet = q.getBigDecimal("myTotalNet");

        if (myTotalConsume == null) myTotalConsume = BigDecimal.ZERO;
        if (myTotalReturn == null) myTotalReturn = BigDecimal.ZERO;
        if (myTotalNet == null) myTotalNet = BigDecimal.ZERO;

        // 更新缓存
        if (poolBalance != null) {
            lastPoolBalance = poolBalance;
        }
        if (!isBlank(serverTime)) {
            lastServerTime = serverTime;
        }

        // 广播信息变更
        pushPbxInfo(lastPoolBalance);

        // 构造返回
        JSONObject resp = new JSONObject();
        resp.put("success", true);
        resp.put("gameId", String.valueOf(PBX_GAME_ID));
        resp.put("userId", userId);
        resp.put("userNo", userNo);
        resp.put("status", 1);
        resp.put("onlineCount", onlineUserState.size());
        resp.put("gameSetting", PBX_GAME_SETTING);
        resp.put("poolBalance", poolBalance);
        resp.put("serverTimeStr", serverTime);
        resp.put("serverTime", nowMs);

        String periodNo = ensureCurrentPeriod(nowMs);
        resp.put("periodNo", periodNo);
        long periodStartMs = getPeriodStartMs(nowMs);
        long periodEndMs = getPeriodEndMs(nowMs);
        resp.putAll(buildPbxInfoByPeriod(lastPoolBalance, periodNo, periodStartMs, periodEndMs, nowMs));

        // 个人数据
        resp.put("myElementBet", buildMyElementBet(userId));
        resp.put("myTotalBet", buildMyTotalBet(userId));
        resp.put("myTotalConsume", myTotalConsume);
        resp.put("myTotalReturn", myTotalReturn);
        resp.put("myTotalNet", myTotalNet);

        // 获取统一统计
        try {
            JSONObject summary = battleRoyaleRecord2Service.buildUnifiedSummary(Long.valueOf(userId), false);
            if (summary != null) {
                resp.putAll(summary);
                resp.put("serverTime", nowMs);
            }
        } catch (Exception ignore) {
        }

        return resp;
    }

    /**
     * 下注操作 ：处理用户下注请求，透传至 Manager 扣款，成功后记录本地缓存并广播
     */
    @ServiceMethod(code = "103", description = "推箱子-下注/操作")
    public Object operate(BattleRoyaleSocketServer2 adminSocketServer, Command lotteryCommand, JSONObject data) {
        checkNull(data);
        String userId = data.getString("userId");
        if (isBlank(userId)) {
            throwExp("参数错误");
        }

        BigDecimal chip = parseChip(data);
        Integer elementId = parseElementId(data);

        if (chip == null) throwExp("参数错误");
        if (!isAllowedChip(chip)) throwExp("参数错误 chip not allowed, allowed=" + CHIPS);
        if (elementId == null) elementId = 1;
        if (elementId < 1 || elementId > ELEMENT_COUNT) throwExp("参数错误 elementId out of range: 1.." + ELEMENT_COUNT);

        String periodNo = ensureCurrentPeriod(System.currentTimeMillis());
        // 生成本地订单号用于 ACK 追踪
        String orderNoForAck = newOrderNo();

        // 更新用户状态为操作中
        JSONObject state = onlineUserState.get(userId);
        if (state != null) {
            state.put("status", 2);
            state.put("ts", System.currentTimeMillis());
        }

        final AtomicReference<JSONObject> betRespRef = new AtomicReference<>();
        final CountDownLatch betLatch = new CountDownLatch(1);

        // 构造请求 Manager 的参数
        JSONObject betReq = new JSONObject();
        betReq.put("gameId", String.valueOf(PBX_GAME_ID));
        betReq.put("userId", userId);
        betReq.put("betAmount", chip.stripTrailingZeros().toPlainString());
        betReq.put("capitalType", CAPITAL_TYPE);
        betReq.put("feeRate", FEE_RATE);
        betReq.put("periodNo", periodNo);
        betReq.put("elementId", elementId);
        betReq.put("chip", chip.stripTrailingZeros().toPlainString());
        betReq.put("orderNo", orderNoForAck);

        Integer finalElementId = elementId;

        // 发起 RPC 请求
        requsetMangerService2.requestPbxBet(betReq, new Listener() {
            @Override
            public void handle(BaseClientSocket socket, Command command) {
                try {
                    // command 为空
                    if (command == null) {
                        handleBetError("pbxBet command is null");
                        return;
                    }

                    // command 执行失败
                    if (!command.isSuccess()) {
                        String msg = command.getMessage();
                        handleBetError(isBlank(msg) ? "pbxBet failed (manager error)" : msg);
                        return;
                    }

                    // data 为空
                    JSONObject resp = (JSONObject) command.getData();
                    if (resp == null) {
                        handleBetError("pbxBet response data is null");
                        return;
                    }

                    // manager 返回业务失败
                    if (!resp.getBooleanValue("success")) {
                        String msg = resp.getString("message");
                        String retOrderNo = resp.getString("orderNo");
                        if (isBlank(retOrderNo)) retOrderNo = orderNoForAck;
                        resp.put("orderNo", retOrderNo);

                        // 下注失败推送
                        pushBetFailed(userId, retOrderNo, periodNo, finalElementId, chip,
                                isBlank(msg) ? "pbxBet failed" : msg);

                        // 把失败结果写入引用
                        betRespRef.set(resp);
                        return;
                    }

                    // 同步本地内存
                    String managerOrderNo = resp.getString("orderNo");
                    if (isBlank(managerOrderNo)) managerOrderNo = orderNoForAck;

                    BigDecimal balance = resp.getBigDecimal("balance");
                    BigDecimal poolBalance = resp.getBigDecimal("poolBalance");
                    BigDecimal fee = resp.getBigDecimal("fee");
                    BigDecimal feeRate = resp.getBigDecimal("feeRate");

                    if (poolBalance != null) {
                        lastPoolBalance = poolBalance;
                    }

                    // 记录到本地内存
                    recordBet(periodNo, userId, finalElementId, chip);

                    // 推送成功状态
                    JSONObject pushStatus = buildPbxStatusPush(
                            userId, 2, true, managerOrderNo, periodNo, finalElementId, chip,
                            balance, poolBalance, fee, feeRate
                    );

                    try {
                        JSONObject sum = battleRoyaleRecord2Service.buildUnifiedSummary(Long.valueOf(userId), false);
                        if (sum != null) {
                            JSONObject mp = new JSONObject();
                            mp.put(userId, sum);
                            pushStatus.put("userRecordSummaryMap", mp);
                            pushStatus.putAll(sum);
                        }
                    } catch (Exception ignore) {
                    }

                    Push.push(PushCode.updatePbxStatus, null, pushStatus);
                    pushPbxInfo(lastPoolBalance);

                    // 成功结果写入引用
                    betRespRef.set(resp);

                } catch (Exception e) {
                    log.error("[PBX] pbxBet callback exception", e);
                    handleBetError("pbxBet callback exception");
                } finally {
                    // 只负责唤醒等待线程
                    betLatch.countDown();
                }
            }

            // 处理下注错误回调
            private void handleBetError(String msg) {
                JSONObject r = new JSONObject();
                r.put("success", false);
                r.put("orderNo", orderNoForAck);
                r.put("message", msg);

                pushBetFailed(userId, orderNoForAck, periodNo, finalElementId, chip, msg);

                // ★关键：写入引用，让主线程拿得到失败原因
                betRespRef.set(r);
            }
        });

        // 等待结果
        boolean awaited = false;
        try {
            awaited = betLatch.await(6, TimeUnit.SECONDS);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }

        JSONObject resp = betRespRef.get();
        if (!awaited || resp == null) {
            JSONObject fail = new JSONObject();
            fail.put("success", false);
            fail.put("gameId", String.valueOf(PBX_GAME_ID));
            fail.put("userId", userId);
            fail.put("periodNo", periodNo);
            fail.put("elementId", elementId);
            fail.put("chip", chip.stripTrailingZeros().toPlainString());
            fail.put("betAmount", chip.stripTrailingZeros().toPlainString());
            fail.put("orderNo", orderNoForAck);
            fail.put("message", awaited ? "pbxBet response is null" : "pbxBet timeout");
            fail.put("gameSetting", PBX_GAME_SETTING);
            return fail;
        }

        if (!resp.getBooleanValue("success")) {
            if (!resp.containsKey("gameId")) resp.put("gameId", String.valueOf(PBX_GAME_ID));
            if (!resp.containsKey("gameSetting")) resp.put("gameSetting", PBX_GAME_SETTING);
            return resp;
        }

        // 返回 ACK
        JSONObject ack = new JSONObject(resp);
        ack.put("ack", true);
        ack.put("gameId", String.valueOf(PBX_GAME_ID));
        ack.put("userId", userId);
        ack.put("periodNo", periodNo);
        ack.put("elementId", elementId);
        ack.put("chip", chip.stripTrailingZeros().toPlainString());
        ack.put("betAmount", chip.stripTrailingZeros().toPlainString());
        ack.put("gameSetting", PBX_GAME_SETTING);
        if (isBlank(ack.getString("orderNo"))) {
            ack.put("orderNo", orderNoForAck);
        }
        return ack;
    }

    /**
     * 查询游戏信息 主动拉取最新奖池、榜单及个人数据
     */
    @ServiceMethod(code = "105", description = "推箱子-查询奖池")
    public Object query(BattleRoyaleSocketServer2 adminSocketServer, Command lotteryCommand, JSONObject data) {
        long nowMs = System.currentTimeMillis();
        checkNull(data);

        String userId = data.getString("userId");
        if (isBlank(userId)) {
            throwExp("参数错误");
        }

        JSONObject q = queryPoolFromManager(userId);
        BigDecimal poolBalance = q.getBigDecimal("poolBalance");
        String serverTime = q.getString("serverTime");

        // 历史数据
        BigDecimal myTotalConsume = q.getBigDecimal("myTotalConsume");
        BigDecimal myTotalReturn = q.getBigDecimal("myTotalReturn");
        BigDecimal myTotalNet = q.getBigDecimal("myTotalNet");

        if (poolBalance != null) lastPoolBalance = poolBalance;
        if (!isBlank(serverTime)) lastServerTime = serverTime;

        // 推送变更
        pushPbxInfo(lastPoolBalance);

        JSONObject resp = new JSONObject();
        resp.put("success", true);
        resp.put("gameId", String.valueOf(PBX_GAME_ID));
        resp.put("poolBalance", poolBalance);
        resp.put("serverTimeStr", serverTime);
        resp.put("serverTime", nowMs);
        resp.put("onlineCount", onlineUserState.size());
        resp.put("gameSetting", PBX_GAME_SETTING);

        String periodNo = ensureCurrentPeriod(nowMs);
        resp.put("periodNo", periodNo);
        long periodStartMs = getPeriodStartMs(nowMs);
        long periodEndMs = getPeriodEndMs(nowMs);
        resp.putAll(buildPbxInfoByPeriod(lastPoolBalance, periodNo, periodStartMs, periodEndMs, nowMs));

        resp.put("myElementBet", buildMyElementBet(userId));
        resp.put("myTotalBet", buildMyTotalBet(userId));
        resp.put("myTotalConsume", myTotalConsume);
        resp.put("myTotalReturn", myTotalReturn);
        resp.put("myTotalNet", myTotalNet);

        try {
            JSONObject summary = battleRoyaleRecord2Service.buildUnifiedSummary(Long.valueOf(userId), false);
            if (summary != null) {
                resp.putAll(summary);
                resp.put("serverTime", nowMs);
            }
        } catch (Exception ignore) {
        }
        return resp;
    }

    /**
     * 结算
     */
    @ServiceMethod(code = "106", description = "推箱子-结算派奖（debug透传）")
    public Object settle(BattleRoyaleSocketServer2 adminSocketServer, Command lotteryCommand, JSONObject data) {
        checkNull(data);

        String periodNo = data.getString("periodNo");
        if (isBlank(periodNo)) {
            periodNo = ensureCurrentPeriod(System.currentTimeMillis());
        }

        JSONArray payouts = data.getJSONArray("payouts");
        if (payouts == null) payouts = new JSONArray();

        JSONArray winList = new JSONArray();
        for (int i = 0; i < payouts.size(); i++) {
            JSONObject p = payouts.getJSONObject(i);
            if (p == null) continue;
            String uid = p.getString("userId");
            if (isBlank(uid)) continue;
            BigDecimal gross = p.getBigDecimal("gross");
            if (gross == null) gross = p.getBigDecimal("returnAmount");
            if (gross == null) continue;

            JSONObject w = new JSONObject();
            w.put("userId", uid);
            w.put("returnAmount", gross.stripTrailingZeros().toPlainString());
            winList.add(w);
        }

        JSONObject settleReq = new JSONObject();
        settleReq.put("gameId", String.valueOf(PBX_GAME_ID));
        settleReq.put("periodNo", periodNo);
        settleReq.put("capitalType", CAPITAL_TYPE);
        settleReq.put("feeRate", FEE_RATE);
        settleReq.put("winList", winList);

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<JSONObject> ref = new AtomicReference<>();

        requsetMangerService2.requestPbxSettle(settleReq, new Listener() {
            @Override
            public void handle(BaseClientSocket socket, Command command) {
                ref.set((JSONObject) command.getData());
                latch.countDown();
            }
        });

        boolean ok;
        try {
            ok = latch.await(6, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            ok = false;
        }

        if (!ok) throwExp("未知异常");

        JSONObject resp = ref.get();
        if (resp != null && resp.getBooleanValue("success")) {
            BigDecimal poolBalance = resp.getBigDecimal("poolBalance");
            if (poolBalance != null) {
                lastPoolBalance = poolBalance;
            }
            pushPbxInfo(lastPoolBalance);
        }

        return resp == null ? throwExp("pbxSettle response is null") : resp;
    }

    /**
     * 周榜结算 触发周榜结算逻辑，向 Manager 请求结算
     */
    @ServiceMethod(code = "107", description = "PBX 周榜结算")
    public Object processWeekSettle(JSONObject data) {
        Integer gameId = data.getInteger("gameId");
        if (gameId == null) gameId = PBX_GAME_ID;
        if (gameId != PBX_GAME_ID) throwExp("gameId invalid");

        JSONObject req = new JSONObject();
        req.put("gameId", PBX_GAME_ID);
        String weekKey = data.getString("weekKey");
        if (StringUtils.isNotBlank(weekKey)) {
            req.put("weekKey", weekKey);
        }

        AtomicReference<JSONObject> ref = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        requsetMangerService2.requestPbxWeekSettle(req, new Listener() {
            @Override
            public void handle(BaseClientSocket socket, Command command) {
                ref.set((JSONObject) command.getData());
                latch.countDown();
            }
        });

        boolean ok = false;
        try {
            ok = latch.await(6, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        if (!ok) {
            JSONObject fail = new JSONObject();
            fail.put("success", false);
            fail.put("message", "pbxWeekSettle timeout");
            return fail;
        }

        JSONObject resp = ref.get();
        if (resp == null) {
            JSONObject fail = new JSONObject();
            fail.put("success", false);
            fail.put("message", "pbxWeekSettle response is null");
            return fail;
        }
        return resp;
    }

    /**
     * 获取统计记录 获取用户的游戏历史汇总数据
     */
    @ServiceMethod(code = "108", description = "推箱子-获取统计记录")
    public JSONObject getRecord(BattleRoyaleSocketServer2 adminSocketServer, Command lotteryCommand, JSONObject data) {
        checkNull(data);
        checkNull(data.get("userId"));
        Long userId = data.getLong("userId");
        return battleRoyaleRecord2Service.buildUnifiedSummary(userId, false);
    }

    /**
     * 离开房间 清除在线状态，并同步推送
     */
    @ServiceMethod(code = "104", description = "推箱子-离开房间")
    public Object leaveRoom(BattleRoyaleSocketServer2 adminSocketServer, Command lotteryCommand, JSONObject data) {
        checkNull(data);
        String userId = data.getString("userId");
        if (isBlank(userId)) throwExp("参数错误");

        onlineUserState.remove(userId);
        pushPbxInfo(lastPoolBalance);

        JSONObject resp = new JSONObject();
        resp.put("success", true);
        resp.put("gameId", String.valueOf(PBX_GAME_ID));
        resp.put("userId", userId);
        resp.put("status", 0);
        resp.put("onlineCount", onlineUserState.size());
        return resp;
    }

    /**
     * 周期状态机核心：计算当前期号，检测并处理期号切换
     */
    private String ensureCurrentPeriod(long nowMs) {
        long periodMs = (TIME_SEC <= 0) ? 20000L : (TIME_SEC * 1000L);
        long bucket = nowMs / periodMs;
        String periodNo = "PBX_" + bucket;

        PeriodSnapshot snapshotToSettle = null;
        // 检查周切换
        ensureWeek(nowMs);

        synchronized (PERIOD_LOCK) {
            long startMs = bucket * periodMs;
            long endMs = startMs + periodMs;

            // 如果是同一期，仅更新状态
            if (currentPeriodNo == null || bucket == currentPeriodBucket) {
                currentPeriodBucket = bucket;
                currentPeriodNo = periodNo;
                currentPeriodStartMs = startMs;
                currentPeriodEndMs = endMs;
                return currentPeriodNo;
            }

            // 切换新期逻辑
            String prevPeriodNo = currentPeriodNo;
            long prevStartMs = currentPeriodBucket * periodMs;
            long prevEndMs = prevStartMs + periodMs;

            // 广播上一期进入“结算中”状态 (Status=2)
            if (!onlineUserState.isEmpty()) {
                try {
                    BigDecimal pool = (lastPoolBalance == null) ? BigDecimal.ZERO : lastPoolBalance;
                    JSONObject settlingInfo = buildPbxInfoByPeriod(pool, prevPeriodNo, prevStartMs, prevEndMs, nowMs);
                    settlingInfo.put("status", 2);
                    settlingInfo.put("remainSeconds", 0);
                    Push.push(PushCode.updatePbxInfo, null, settlingInfo);
                } catch (Exception e) {
                    log.error("[PBX] push settling info error, periodNo=" + prevPeriodNo, e);
                }
            }

            // 创建上一期快照准备结算
            snapshotToSettle = new PeriodSnapshot(
                    prevPeriodNo, prevStartMs, prevEndMs,
                    new HashMap<>(periodElementTotalBet),
                    new HashMap<>(periodUserElementBet),
                    new HashMap<>(periodUserTotalBet)
            );

            // 清理/重置本期数据
            periodElementTotalBet.clear();
            periodUserElementBet.clear();
            periodUserTotalBet.clear();
            periodBotElementTotalBet.clear();

            currentPeriodBucket = bucket;
            currentPeriodNo = periodNo;
            currentPeriodStartMs = startMs;
            currentPeriodEndMs = endMs;
        }

        // 异步触发结算
        if (snapshotToSettle != null) {
            settlePeriodAsync(snapshotToSettle);
        }
        return currentPeriodNo;
    }

    /**
     * 异步结算入口，避免阻塞主线程
     */
    private void settlePeriodAsync(PeriodSnapshot snapshot) {
        if (snapshot == null || isBlank(snapshot.periodNo)) return;
        // 避免重复结算
        if (!settledPeriodNoSet.add(snapshot.periodNo)) return;

        if (periodScheduler != null) {
            periodScheduler.execute(() -> settlePeriod(snapshot));
        } else {
            settlePeriod(snapshot);
        }
    }

    /**
     * 执行结算逻辑：查询奖池 -> 控盘选择 -> 强制输赢判断 -> 调用 Manager 结算 -> 落库 -> 广播
     */
    private void settlePeriod(PeriodSnapshot snapshot) {
        try {
            if (snapshot == null || isBlank(snapshot.periodNo)) return;
            // 无人下注直接跳过
            if (snapshot.userTotalBet == null || snapshot.userTotalBet.isEmpty()) return;

            // 查询奖池余额
            AtomicReference<JSONObject> queryRespRef = new AtomicReference<>();
            CountDownLatch latch = new CountDownLatch(1);
            JSONObject queryReq = new JSONObject();
            queryReq.put("gameId", String.valueOf(PBX_GAME_ID));
            queryReq.put("userId", "0");

            requsetMangerService2.requestPbxQuery(queryReq, new Listener() {
                @Override
                public void handle(BaseClientSocket socket, Command command) {
                    try {
                        queryRespRef.set((JSONObject) command.getData());
                    } finally {
                        latch.countDown();
                    }
                }
            });
            latch.await(2, TimeUnit.SECONDS);

            BigDecimal poolBalance = lastPoolBalance == null ? BigDecimal.ZERO : lastPoolBalance;
            String serverTime = nowStr();
            JSONObject q = queryRespRef.get();
            if (q != null && q.getBooleanValue("success")) {
                BigDecimal pb = q.getBigDecimal("poolBalance");
                if (pb != null) {
                    poolBalance = pb;
                    lastPoolBalance = pb;
                }
                String st = q.getString("serverTime");
                if (!isBlank(st)) serverTime = st;
            }

            // 计算并选择开奖结果
            OutcomePick pick = pickOutcome(snapshot.elementTotalBet, poolBalance);
            JSONArray resultElements = pick.resultElements;
            String resultType = pick.resultType;
            int forcedNoWinFlag = pick.forceLose ? 1 : 0;

            // 记录历史
            pushResultHistory(snapshot.periodNo, resultElements, resultType, forcedNoWinFlag);

            // 强制全输
            if (pick.forceLose) {
                handleForceLose(snapshot, resultElements, resultType, poolBalance, serverTime);
                return;
            }

            /** 正常派奖 **/
            // 计算赢家列表
            JSONArray winList = new JSONArray();
            for (Map.Entry<String, Map<Integer, BigDecimal>> e : snapshot.userElementBet.entrySet()) {
                String userId = e.getKey();
                BigDecimal gross = calcUserGross(e.getValue(), pick);
                if (gross != null && gross.compareTo(BigDecimal.ZERO) > 0) {
                    JSONObject one = new JSONObject();
                    one.put("userId", userId);
                    one.put("returnAmount", gross.setScale(2, RoundingMode.HALF_UP).toPlainString());
                    winList.add(one);
                }
            }

            // 若无人中奖就自然输掉
            if (winList.isEmpty()) {
                handleNormalNoWin(snapshot, resultElements, resultType, poolBalance, serverTime);
                return;
            }

            // 调用 Manager 派奖
            JSONObject settleReq = new JSONObject();
            settleReq.put("gameId", String.valueOf(PBX_GAME_ID));
            settleReq.put("periodNo", snapshot.periodNo);
            settleReq.put("capitalType", CAPITAL_TYPE);
            settleReq.put("feeRate", FEE_RATE);
            settleReq.put("winList", winList);

            AtomicReference<JSONObject> settleRespRef = new AtomicReference<>();
            CountDownLatch settleLatch = new CountDownLatch(1);
            requsetMangerService2.requestPbxSettle(settleReq, new Listener() {
                @Override
                public void handle(BaseClientSocket socket, Command command) {
                    try {
                        settleRespRef.set((JSONObject) command.getData());
                    } finally {
                        settleLatch.countDown();
                    }
                }
            });
            settleLatch.await(3, TimeUnit.SECONDS);

            JSONObject settleResp = settleRespRef.get();
            // 结算异常兜底
            if (settleResp == null || !settleResp.getBooleanValue("success")) {
                handleSettleError(snapshot, resultElements, resultType, poolBalance, serverTime, settleResp);
                return;
            }

            // 结算成功处理
            handleSettleSuccess(snapshot, resultElements, resultType, poolBalance, serverTime, settleResp);

        } catch (Exception e) {
            log.error("[PBX] auto settle exception, periodNo=" + snapshot.periodNo, e);
        }
    }


    /**
     * 处理强制庄家赢的情况
     */
    private void handleForceLose(PeriodSnapshot snapshot, JSONArray resultElements, String resultType, BigDecimal poolBalance, String serverTime) {
        for (String uid : snapshot.userTotalBet.keySet()) {
            updateUserStatusToIdle(uid);
            JSONObject pushStatus = buildAutoSettleStatusPush(uid, snapshot.periodNo, resultElements,
                    resultType, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    null, poolBalance, serverTime, "pool not enough, force lose", 1);
            appendRecordSummary(uid, pushStatus);
            Push.push(PushCode.updatePbxStatus, null, pushStatus);
        }
        broadcastSettleInfo(snapshot, resultElements, resultType, poolBalance, 1);
    }

    /**
     * 处理正常情况下的无人中奖
     */
    private void handleNormalNoWin(PeriodSnapshot snapshot, JSONArray resultElements, String resultType, BigDecimal poolBalance, String serverTime) {
        for (String uid : snapshot.userTotalBet.keySet()) {
            updateUserStatusToIdle(uid);
            JSONObject pushStatus = buildAutoSettleStatusPush(uid, snapshot.periodNo, resultElements,
                    resultType, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    null, poolBalance, serverTime, null, 0);
            Push.push(PushCode.updatePbxStatus, null, pushStatus);
        }
        broadcastSettleInfo(snapshot, resultElements, resultType, poolBalance, 0);
    }

    /**
     * 处理结算请求异常的情况
     */
    private void handleSettleError(PeriodSnapshot snapshot, JSONArray resultElements, String resultType, BigDecimal poolBalance, String serverTime, JSONObject settleResp) {
        String msg = (settleResp == null) ? "pbxSettle response null" : settleResp.getString("message");
        for (String uid : snapshot.userTotalBet.keySet()) {
            updateUserStatusToIdle(uid);
            JSONObject pushStatus = buildAutoSettleStatusPush(uid, snapshot.periodNo, resultElements,
                    resultType, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    null, poolBalance, serverTime, msg, 0);
            Push.push(PushCode.updatePbxStatus, null, pushStatus);
        }
        broadcastSettleInfo(snapshot, resultElements, resultType, poolBalance, 0);
    }

    /**
     * 处理结算成功后的数据更新与广播
     */
    private void handleSettleSuccess(PeriodSnapshot snapshot, JSONArray resultElements, String resultType, BigDecimal poolBalance, String serverTime, JSONObject settleResp) {
        JSONArray userList = settleResp.getJSONArray("userList");
        BigDecimal newPoolBalance = settleResp.getBigDecimal("poolBalance");
        if (newPoolBalance != null) {
            lastPoolBalance = newPoolBalance;
        } else {
            newPoolBalance = poolBalance;
        }

        Map<String, JSONObject> userInfoMap = new HashMap<>();
        if (userList != null) {
            for (int i = 0; i < userList.size(); i++) {
                JSONObject u = userList.getJSONObject(i);
                if (u == null) continue;
                String uid = u.getString("userId");
                if (isBlank(uid)) continue;
                userInfoMap.put(uid, u);
            }
        }

        // DB
        try {
            JSONObject upd = new JSONObject();
            String lotteryResult = joinResultElements(resultElements);
            for (String uid : snapshot.userTotalBet.keySet()) {
                String orderNo = "PBX-" + snapshot.periodNo + "-" + uid;
                BigDecimal betAmount = snapshot.userTotalBet.get(uid);
                String betInfo = buildBetInfo(snapshot.userElementBet.get(uid));
                JSONObject u = userInfoMap.get(uid);
                BigDecimal gross = (u == null) ? BigDecimal.ZERO : u.getBigDecimal("returnAmount");
                if (gross == null) gross = BigDecimal.ZERO;

                JSONObject row = new JSONObject();
                row.put("winAmount", gross);
                row.put("lotteryResult", lotteryResult);
                row.put("isWin", gross.compareTo(BigDecimal.ZERO) > 0 ? 1 : 0);
                row.put("betAmount", betAmount);
                row.put("betInfo", betInfo);
                upd.put(orderNo, row);
            }
            if (!upd.isEmpty()) {
                battleRoyaleRecord2Service.batchUpdateRecord(upd);
            }
        } catch (Exception ignore) {
        }

        // 推送给用户
        for (String uid : snapshot.userTotalBet.keySet()) {
            updateUserStatusToIdle(uid);
            JSONObject u = userInfoMap.get(uid);
            BigDecimal gross = (u == null) ? BigDecimal.ZERO : u.getBigDecimal("returnAmount");
            BigDecimal fee = (u == null) ? BigDecimal.ZERO : u.getBigDecimal("fee");
            BigDecimal net = (u == null) ? BigDecimal.ZERO : u.getBigDecimal("net");
            BigDecimal balance = (u == null) ? null : u.getBigDecimal("balance");

            JSONObject pushStatus = buildAutoSettleStatusPush(uid, snapshot.periodNo, resultElements,
                    resultType, gross, fee, net, balance, newPoolBalance, serverTime, null, 0);
            appendRecordSummary(uid, pushStatus);
            Push.push(PushCode.updatePbxStatus, null, pushStatus);
        }

        broadcastSettleInfo(snapshot, resultElements, resultType, newPoolBalance, 0);
    }

    /**
     * 将用户在线状态重置为空闲
     */
    private void updateUserStatusToIdle(String uid) {
        JSONObject state = onlineUserState.get(uid);
        if (state != null) {
            state.put("status", 1);
            state.put("ts", System.currentTimeMillis());
        }
    }

    /**
     * 为推送消息附加用户汇总记录
     */
    private void appendRecordSummary(String uid, JSONObject pushStatus) {
        try {
            JSONObject sum = battleRoyaleRecord2Service.buildUnifiedSummary(Long.valueOf(uid), false);
            if (sum != null) {
                JSONObject mp = new JSONObject();
                mp.put(uid, sum);
                pushStatus.put("userRecordSummaryMap", mp);
                pushStatus.putAll(sum);
            }
        } catch (Exception ignore) {
        }
    }

    /**
     * 全服广播开奖结果
     */
    private void broadcastSettleInfo(PeriodSnapshot snapshot, JSONArray resultElements, String resultType, BigDecimal poolBalance, int forcedNoWin) {
        JSONObject infoPush = buildPbxInfoByPeriod(poolBalance, snapshot.periodNo, snapshot.startMs, snapshot.endMs, snapshot.endMs);
        infoPush.put("status", 3);
        infoPush.put("resultElements", resultElements);
        infoPush.put("resultType", resultType);
        infoPush.put("forcedNoWin", forcedNoWin);
        Push.push(PushCode.updatePbxInfo, null, infoPush);
    }


    /**
     * 根据当前奖池选择最佳开奖结果
     *
     * @param elementTotalBet 各元素总下注额
     * @param poolBalance 当前奖池余额
     * @return 选定的开奖结果对象
     */
    private OutcomePick pickOutcome(Map<Integer, BigDecimal> elementTotalBet, BigDecimal poolBalance) {
        Map<Integer, BigDecimal> t = (elementTotalBet == null) ? new HashMap<>() : elementTotalBet;
        List<OutcomePick> candidates = new ArrayList<>();
        List<OutcomePick> all = new ArrayList<>();

        // Triple Logic (三同号)
        for (int e = 1; e <= ELEMENT_COUNT; e++) {
            JSONArray res = new JSONArray();
            res.add(e); res.add(e); res.add(e);
            BigDecimal gross = safe(t.get(e)).multiply(MULT_TRIPLE);
            BigDecimal net = calcNetForControl(gross);
            OutcomePick p = new OutcomePick(res, "TRIPLE", false, e, null, MULT_TRIPLE);
            all.add(p);
            if (poolBalance != null && net.compareTo(poolBalance) <= 0) candidates.add(p);
        }

        // Double Logic (两同号)
        for (int e = 1; e <= ELEMENT_COUNT; e++) {
            for (int f = 1; f <= ELEMENT_COUNT; f++) {
                if (f == e) continue;
                JSONArray res = new JSONArray();
                res.add(e); res.add(e); res.add(f);
                BigDecimal gross = safe(t.get(e)).multiply(MULT_DOUBLE);
                BigDecimal net = calcNetForControl(gross);
                OutcomePick p = new OutcomePick(res, "DOUBLE", false, e, null, MULT_DOUBLE);
                all.add(p);
                if (poolBalance != null && net.compareTo(poolBalance) <= 0) candidates.add(p);
            }
        }

        // All Diff Logic (三不同号)
        for (int a = 1; a <= ELEMENT_COUNT; a++) {
            for (int b = a + 1; b <= ELEMENT_COUNT; b++) {
                for (int c = b + 1; c <= ELEMENT_COUNT; c++) {
                    JSONArray res = new JSONArray();
                    res.add(a); res.add(b); res.add(c);
                    BigDecimal gross = safe(t.get(a)).add(safe(t.get(b))).add(safe(t.get(c))).multiply(MULT_ALL_DIFF);
                    BigDecimal net = calcNetForControl(gross);
                    Set<Integer> winSet = new HashSet<>();
                    winSet.add(a); winSet.add(b); winSet.add(c);
                    OutcomePick p = new OutcomePick(res, "ALL_DIFF", false, null, winSet, MULT_ALL_DIFF);
                    all.add(p);
                    if (poolBalance != null && net.compareTo(poolBalance) <= 0) candidates.add(p);
                }
            }
        }

        // 强制输逻辑: 无候选时选损失最小的
        if (candidates.isEmpty()) {
            OutcomePick min = Collections.min(all, Comparator.comparing(o -> calcNetForControl(calcGrossForOutcome(t, o))));
            return new OutcomePick(min.resultElements, min.resultType, true, min.winElement, min.winElements, min.multiplier);
        }

        // 随机选择一个可行结果
        return candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
    }

    /**
     * 计算某开奖结果对应的平台总赔付额
     */
    private BigDecimal calcGrossForOutcome(Map<Integer, BigDecimal> elementTotalBet, OutcomePick pick) {
        if (pick == null) return BigDecimal.ZERO;
        if ("TRIPLE".equals(pick.resultType)) {
            return safe(elementTotalBet.get(pick.winElement)).multiply(MULT_TRIPLE);
        }
        if ("DOUBLE".equals(pick.resultType)) {
            return safe(elementTotalBet.get(pick.winElement)).multiply(MULT_DOUBLE);
        }
        if ("ALL_DIFF".equals(pick.resultType)) {
            BigDecimal sum = BigDecimal.ZERO;
            if (pick.winElements != null) {
                for (Integer e : pick.winElements) {
                    sum = sum.add(safe(elementTotalBet.get(e)));
                }
            }
            return sum.multiply(MULT_ALL_DIFF);
        }
        return BigDecimal.ZERO;
    }

    /**
     * 计算单个用户的赔付额
     */
    private BigDecimal calcUserGross(Map<Integer, BigDecimal> userBetByElement, OutcomePick pick) {
        if (pick == null) return BigDecimal.ZERO;
        if (userBetByElement == null || userBetByElement.isEmpty()) return BigDecimal.ZERO;

        if ("TRIPLE".equals(pick.resultType) || "DOUBLE".equals(pick.resultType)) {
            BigDecimal bet = safe(userBetByElement.get(pick.winElement));
            return bet.multiply(pick.multiplier).setScale(2, RoundingMode.HALF_UP);
        }
        if ("ALL_DIFF".equals(pick.resultType)) {
            BigDecimal sum = BigDecimal.ZERO;
            for (Integer e : pick.winElements) {
                sum = sum.add(safe(userBetByElement.get(e)));
            }
            return sum.multiply(pick.multiplier).setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    /**
     * 计算净赔付（扣除手续费前的值用于比对奖池）
     */
    private BigDecimal calcNetForControl(BigDecimal gross) {
        if (gross == null) return BigDecimal.ZERO;
        BigDecimal fee = gross.multiply(FEE_RATE).setScale(2, RoundingMode.HALF_UP);
        return gross.setScale(2, RoundingMode.HALF_UP).subtract(fee);
    }

    /**
     * BigDecimal 安全访问工具
     */
    private BigDecimal safe(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    /**
     * 初始化机器人用户池
     */
    private void initBotUsers() {
        try {
            BOT_USER.clear();
            List<User> botList = userService.findBot();
            if (botList != null) {
                for (User u : botList) {
                    if (u != null && u.getId() != null) {
                        BOT_USER.put(String.valueOf(u.getId()), u);
                    }
                }
            }
            log.info("[PBX] 加载机器人完成，数量=" + BOT_USER.size());
        } catch (Exception e) {
            log.error("[PBX] 加载机器人异常", e);
        }
    }

    /**
     * 初始化机器人配置（概率、筹码）
     */
    private void initBotConfig() {
        // NEED_BOT
        try {
            Config cfg = configService.getConfigByKey(Config.GAME_DTS2_NEED_BOT);
            if (cfg != null && cfg.getValue() != null) {
                NEED_BOT = Integer.parseInt(cfg.getValue().trim());
            }
        } catch (Exception e) {
            log.error("[PBX] 初始化 NEED_BOT 异常", e);
            NEED_BOT = 0;
        }
        // BOT_CHIPS
        BOT_CHIPS.clear();
        try {
            if (CHIPS != null) {
                for (int i = 0; i < CHIPS.size(); i++) {
                    BigDecimal c = new BigDecimal(String.valueOf(CHIPS.get(i)))
                            .setScale(2, RoundingMode.DOWN)
                            .stripTrailingZeros();
                    if (c.compareTo(BigDecimal.ZERO) > 0) {
                        BOT_CHIPS.add(c);
                    }
                }
            }
        } catch (Exception ignore) {
        }
        if (BOT_CHIPS.isEmpty()) {
            BOT_CHIPS.add(new BigDecimal("1"));
            BOT_CHIPS.add(new BigDecimal("10"));
            BOT_CHIPS.add(new BigDecimal("100"));
        }
        log.info("[PBX] 机器人配置：NEED_BOT=" + NEED_BOT + ", BOT_CHIPS=" + BOT_CHIPS);
    }

    /**
     * 机器人 Tick 逻辑：随机下注
     */
        private void tickBot() {
            log.error("[PBX][BOT] precheck NEED_BOT=" + NEED_BOT
                    + ", online=" + onlineUserState.size()
                    + ", bot=" + BOT_USER.size());

            if (NEED_BOT <= 0) return;
            // 没真人在线不刷
            if (onlineUserState.isEmpty()) return;
            if (BOT_USER.isEmpty()) return;

            long nowMs = System.currentTimeMillis();
            String periodNo = ensureCurrentPeriod(nowMs);
            if (nowMs >= currentPeriodEndMs) return;

            int rate = ThreadLocalRandom.current().nextInt(100);
            if (rate >= NEED_BOT) return;

            String botUserId = getRandomBotUserId();
            if (botUserId == null) return;

            int elementId = ThreadLocalRandom.current().nextInt(1, ELEMENT_COUNT + 1);
            BigDecimal chip = getRandomBotChip();

            // 机器人下注走同一条 pbxBet 扣款链路（异步不阻塞 tick）
            try {
                String orderNoForAck = newOrderNo();

                JSONObject betReq = new JSONObject();
                betReq.put("gameId", String.valueOf(PBX_GAME_ID));
                betReq.put("userId", botUserId);
                betReq.put("betAmount", chip.stripTrailingZeros().toPlainString());
                betReq.put("capitalType", CAPITAL_TYPE);
                betReq.put("feeRate", FEE_RATE);
                betReq.put("periodNo", periodNo);
                betReq.put("elementId", elementId);
                betReq.put("chip", chip.stripTrailingZeros().toPlainString());
                betReq.put("orderNo", orderNoForAck);

                requsetMangerService2.requestPbxBet(betReq, new Listener() {
                    @Override
                    public void handle(BaseClientSocket socket, Command command) {
                        try {
                            if (command == null || !command.isSuccess()) return;
                            JSONObject resp = (JSONObject) command.getData();
                            if (resp == null || !resp.getBooleanValue("success")) return;

                            // 把机器人下注记入 periodUserTotalBet/periodElementTotalBet 等核心结构
                            recordBet(periodNo, botUserId, elementId, chip);

                            // 同步一下奖池缓存
                            BigDecimal pool = resp.getBigDecimal("poolBalance");
                            if (pool != null) lastPoolBalance = pool;
                        } catch (Exception ignore) {
                        }
                    }
                });
            } catch (Exception e) {
                log.error("[PBX] bot bet error", e);
            }
        }

    /**
     * 随机获取一个机器人 ID
     */
    private String getRandomBotUserId() {
        if (BOT_USER.isEmpty()) return null;
        int idx = ThreadLocalRandom.current().nextInt(BOT_USER.size());
        return BOT_USER.keySet().stream().skip(idx).findFirst().orElse(null);
    }

    /**
     * 随机获取一个筹码
     */
    private BigDecimal getRandomBotChip() {
        if (BOT_CHIPS.isEmpty()) return new BigDecimal("1");
        int idx = ThreadLocalRandom.current().nextInt(BOT_CHIPS.size());
        return BOT_CHIPS.get(idx);
    }


    /**
     * 向主服查询奖池（带重试/兜底）
     * @param userId 操作用户ID
     * @return 奖池与榜单信息
     */
    private JSONObject queryPoolFromManager(String userId) {
        JSONObject queryReq = new JSONObject();
        queryReq.put("gameId", String.valueOf(PBX_GAME_ID));
        queryReq.put("userId", userId);

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<JSONObject> ref = new AtomicReference<>();

        requsetMangerService2.requestPbxQuery(queryReq, new Listener() {
            @Override
            public void handle(BaseClientSocket socket, Command command) {
                try {
                    if (command != null && command.isSuccess()) {
                        ref.set((JSONObject) command.getData());
                    } else {
                        JSONObject err = new JSONObject();
                        err.put("success", false);
                        err.put("message", (command != null) ? command.getMessage() : "manager timeout");
                        ref.set(err);
                    }
                } finally {
                    latch.countDown();
                }
            }
        });

        boolean ok;
        try {
            ok = latch.await(4, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            ok = false;
        }

        if (!ok) {
            JSONObject err = new JSONObject();
            err.put("success", false);
            err.put("poolBalance", lastPoolBalance);
            err.put("serverTime", nowStr());
            err.put("message", "pbxQuery timeout");
            return err;
        }

        JSONObject resp = ref.get();
        // 同步榜单数据
        try {
            if (resp != null) {
                this.weekRankTop10 = resp.getJSONArray("weekRankTop10");
                this.lastWeekRankTop10 = resp.getJSONArray("lastWeekRankTop10");
                this.weekConsume = resp.getBigDecimal("weekConsume");
                this.weekReturn = resp.getBigDecimal("weekReturn");
                this.weekProfit = resp.getBigDecimal("weekProfit");
                this.weekDividendPool = resp.getBigDecimal("weekDividendPool");
                this.weekSettled = resp.getBooleanValue("weekSettled");
                this.lastWeekConsume = resp.getBigDecimal("lastWeekConsume");
                this.lastWeekReturn = resp.getBigDecimal("lastWeekReturn");
                this.lastWeekProfit = resp.getBigDecimal("lastWeekProfit");
                this.lastWeekDividendPool = resp.getBigDecimal("lastWeekDividendPool");
                this.lastWeekSettled = resp.getBooleanValue("lastWeekSettled");
                this.myWeekConsume = resp.getBigDecimal("myWeekConsume");
                this.myWeekRank = resp.getIntValue("myWeekRank");
                this.myLastWeekConsume = resp.getBigDecimal("myLastWeekConsume");
                this.myLastWeekRank = resp.getIntValue("myLastWeekRank");
            }
        } catch (Exception ignore) {
        }
        return resp;
    }

    /**
     * 系统侧查询奖池（默认UserId=0）
     */
    private JSONObject queryPoolFromManager() {
        return queryPoolFromManager("0");
    }

    /**
     * 记录下注到内存并落库
     *
     * @param periodNo 期号
     * @param userId 用户ID
     * @param elementId 元素ID
     * @param chip 筹码
     */
    private void recordBet(String periodNo, String userId, Integer elementId, BigDecimal chip) {
        if (chip == null || elementId == null) return;

        ensureCurrentPeriod(System.currentTimeMillis());

        periodElementTotalBet.merge(elementId, chip, BigDecimal::add);
        Map<Integer, BigDecimal> userMap = periodUserElementBet.computeIfAbsent(userId, k -> new ConcurrentHashMap<>());
        userMap.merge(elementId, chip, BigDecimal::add);
        periodUserTotalBet.merge(userId, chip, BigDecimal::add);

        ensureWeek(System.currentTimeMillis());
        weekUserTotalBet.merge(userId, chip, BigDecimal::add);

        try {
            String orderNo = "PBX-" + periodNo + "-" + userId;
            Map<Integer, BigDecimal> um = periodUserElementBet.get(userId);
            String betInfo = buildBetInfo(um);
            if (isBlank(betInfo)) betInfo = String.valueOf(elementId);

            int upd = battleRoyaleRecord2Service.addBetAmountAndInfo(chip, orderNo, betInfo);
            if (upd < 1) {
                battleRoyaleRecord2Service.addBattleRoyaleRecord(Long.valueOf(userId), orderNo, periodNo, betInfo, chip);
            }
        } catch (Exception ignore) {
        }
    }


    /**
     * 推送全服信息更新（倒计时、奖池、在线人数）
     */
    private void pushPbxInfo(BigDecimal poolBalance) {
        long nowMs = System.currentTimeMillis();
        String periodNo = currentPeriodNo;
        if (periodNo == null) periodNo = ensureCurrentPeriod(nowMs);
        if (periodNo == null) return;
        if (poolBalance == null) poolBalance = BigDecimal.ZERO;

        JSONObject info = buildPbxInfoByPeriod(poolBalance, periodNo, currentPeriodStartMs, currentPeriodEndMs, nowMs);
        Push.push(PushCode.updatePbxInfo, null, info);
    }

    /**
     * 推送下注失败消息
     */
    private void pushBetFailed(String userId, String orderNo, String periodNo, Integer elementId, BigDecimal chip, String message) {
        JSONObject state = onlineUserState.get(userId);
        if (state != null) {
            state.put("status", 1);
            state.put("ts", System.currentTimeMillis());
        }
        JSONObject pushStatus = buildPbxStatusPush(
                userId, 1, false, orderNo, periodNo, elementId, chip,
                null, lastPoolBalance, null, FEE_RATE
        );
        if (!isBlank(message)) {
            pushStatus.put("message", message);
            JSONObject userSettleInfo = pushStatus.getJSONObject("userSettleInfo");
            if (userSettleInfo != null) {
                userSettleInfo.put("message", message);
            }
        }
        Push.push(PushCode.updatePbxStatus, null, pushStatus);
    }

    /**
     * 构建每期信息广播包
     */
    private JSONObject buildPbxInfoByPeriod(BigDecimal poolBalance, String periodNo, long periodStartMs, long periodEndMs, long nowMs) {
        JSONObject info = new JSONObject();
        info.put("gameId", String.valueOf(PBX_GAME_ID));
        info.put("onlineCount", onlineUserState.size());
        info.put("gameSetting", PBX_GAME_SETTING);
        info.put("poolBalance", poolBalance);
        info.put("serverTimeMs", nowMs);
        info.put("serverTime", dateTimeString(nowMs));
        info.put("periodNo", periodNo);
        info.put("startTs", periodStartMs);
        info.put("endTs", periodEndMs);
        info.put("startTime", dateTimeString(periodStartMs));
        info.put("endTime", dateTimeString(periodEndMs));

        long remainSec = (periodEndMs <= nowMs) ? 0L : ((periodEndMs - nowMs + 999L) / 1000L);
        info.put("remainSeconds", (int) remainSec);
        info.put("status", remainSec > 0 ? 1 : 2);

        info.put("recent16", getRecentResults(16));
        info.put("recent100", getRecentResults(100));
        info.put("recent16Stat", buildRecent16Stat());
        info.put("recent100Stat", buildRecent100Stat());

        JSONObject elementTotalBet = new JSONObject();
        BigDecimal totalBet = BigDecimal.ZERO;
        for (int i = 1; i <= ELEMENT_COUNT; i++) {
            BigDecimal real = periodElementTotalBet.getOrDefault(i, BigDecimal.ZERO);
            BigDecimal bot = periodBotElementTotalBet.getOrDefault(i, BigDecimal.ZERO);
            BigDecimal v = real.add(bot);
            totalBet = totalBet.add(v);
            elementTotalBet.put(String.valueOf(i), v.stripTrailingZeros().toPlainString());
        }
        info.put("weekRankTop10", weekRankTop10);
        info.put("lastWeekRankTop10", lastWeekRankTop10);
        info.put("weekConsume", weekConsume);
        info.put("weekReturn", weekReturn);
        info.put("weekProfit", weekProfit);
        info.put("weekDividendPool", weekDividendPool);
        info.put("weekSettled", weekSettled);
        info.put("lastWeekConsume", lastWeekConsume);
        info.put("lastWeekReturn", lastWeekReturn);
        info.put("lastWeekProfit", lastWeekProfit);
        info.put("lastWeekDividendPool", lastWeekDividendPool);
        info.put("lastWeekSettled", lastWeekSettled);
        info.put("myWeekConsume", myWeekConsume);
        info.put("myWeekRank", myWeekRank);
        info.put("myLastWeekConsume", myLastWeekConsume);
        info.put("myLastWeekRank", myLastWeekRank);
        info.put("elementTotalBet", elementTotalBet);
        info.put("totalBet", totalBet.stripTrailingZeros().toPlainString());
        return info;
    }

    /**
     * 构建自动结算状态推送包
     */
    private JSONObject buildAutoSettleStatusPush(String userId, String periodNo, JSONArray resultElements, String resultType,
                                                 BigDecimal gross, BigDecimal fee, BigDecimal net, BigDecimal balance,
                                                 BigDecimal poolBalance, String serverTime, String message, int forcedNoWin) {
        JSONObject push = new JSONObject();
        push.put("gameId", String.valueOf(PBX_GAME_ID));
        JSONArray userIds = new JSONArray();
        userIds.add(userId);
        push.put("userIds", userIds);
        push.put("status", 3);
        push.put("success", true);
        push.put("orderNo", periodNo);

        JSONObject info = new JSONObject();
        info.put("userId", userId);
        info.put("orderNo", periodNo);
        info.put("periodNo", periodNo);
        info.put("resultElements", resultElements);
        if (!isBlank(resultType)) info.put("resultType", resultType);
        info.put("forcedNoWin", forcedNoWin);
        info.put("returnAmount", (gross == null ? BigDecimal.ZERO : gross).setScale(2, RoundingMode.HALF_UP));
        info.put("fee", (fee == null ? BigDecimal.ZERO : fee).setScale(2, RoundingMode.HALF_UP));
        info.put("net", (net == null ? BigDecimal.ZERO : net).setScale(2, RoundingMode.HALF_UP));

        if (balance != null) info.put("balance", balance.setScale(2, RoundingMode.HALF_UP));
        if (poolBalance != null) info.put("poolBalance", poolBalance.setScale(2, RoundingMode.HALF_UP));
        if (!isBlank(serverTime)) info.put("serverTime", serverTime);
        info.put("ts", System.currentTimeMillis());
        if (!isBlank(message)) info.put("message", message);

        push.put("userSettleInfo", info);
        return push;
    }

    /**
     * 构建下注状态推送包
     */
    private JSONObject buildPbxStatusPush(String userId, int status, boolean success, String orderNo, String periodNo,
                                          Integer elementId, BigDecimal chip, BigDecimal balance, BigDecimal poolBalance,
                                          BigDecimal fee, BigDecimal feeRate) {
        JSONObject data = new JSONObject();
        data.put("gameId", String.valueOf(PBX_GAME_ID));
        JSONArray userIds = new JSONArray();
        userIds.add(userId);
        data.put("userIds", userIds);
        data.put("status", status);
        data.put("success", success);
        data.put("orderNo", orderNo);

        JSONObject userSettleInfo = new JSONObject();
        userSettleInfo.put("userId", userId);
        userSettleInfo.put("orderNo", orderNo);
        userSettleInfo.put("periodNo", periodNo);
        userSettleInfo.put("elementId", elementId);

        if (chip != null) {
            userSettleInfo.put("chip", chip.stripTrailingZeros().toPlainString());
            userSettleInfo.put("betAmount", chip.stripTrailingZeros().toPlainString());
        }
        if (balance != null) userSettleInfo.put("balance", balance);
        if (poolBalance != null) userSettleInfo.put("poolBalance", poolBalance);
        else if (lastPoolBalance != null) userSettleInfo.put("poolBalance", lastPoolBalance);
        if (fee != null) userSettleInfo.put("fee", fee);
        if (feeRate != null) userSettleInfo.put("feeRate", feeRate);

        JSONObject myElementBet = new JSONObject();
        Map<Integer, BigDecimal> myMap = periodUserElementBet.get(userId);
        for (int i = 1; i <= ELEMENT_COUNT; i++) {
            BigDecimal v = BigDecimal.ZERO;
            if (myMap != null) v = myMap.getOrDefault(i, BigDecimal.ZERO);
            myElementBet.put(String.valueOf(i), v.stripTrailingZeros().toPlainString());
        }
        BigDecimal myTotalBet = periodUserTotalBet.getOrDefault(userId, BigDecimal.ZERO);
        userSettleInfo.put("myElementBet", myElementBet);
        userSettleInfo.put("myTotalBet", myTotalBet.stripTrailingZeros().toPlainString());
        userSettleInfo.put("ts", System.currentTimeMillis());

        data.put("userSettleInfo", userSettleInfo);
        return data;
    }


    /**
     * 初始化游戏配置（从DB加载或兜底）
     */
    public void initGameSetting() {
        try {
            Game game = gameService.findGameById((long) PBX_GAME_ID);
            if (game == null || game.getGameSetting() == null) {
                log.warn("[PBX] l_game(" + PBX_GAME_ID + ") is null or game_setting is null, use defaults.");
                PBX_GAME_SETTING = defaultGameSetting();
            } else {
                PBX_GAME_SETTING = JSON.parseObject(game.getGameSetting());
                if (PBX_GAME_SETTING == null) {
                    PBX_GAME_SETTING = defaultGameSetting();
                }
            }
        } catch (Exception e) {
            log.error("[PBX] initGameSetting exception, use defaults.", e);
            PBX_GAME_SETTING = defaultGameSetting();
        }

        TIME_SEC = parseInt(PBX_GAME_SETTING.getString("time"), 20);
        CAPITAL_TYPE = parseInt(PBX_GAME_SETTING.getString("capitalType"), UserCapitalTypeEnum.xxxhhb.getValue());
        CHIPS = PBX_GAME_SETTING.getJSONArray("chips");
        if (CHIPS == null) {
            CHIPS = new JSONArray();
            CHIPS.add("1");
            CHIPS.add("10");
            CHIPS.add("100");
        }
        FEE_RATE = parseBigDecimal(PBX_GAME_SETTING.getString("feeRate"), new BigDecimal("0.05"));
        ELEMENT_COUNT = parseInt(PBX_GAME_SETTING.getString("elementCount"), 6);

        JSONObject mult = PBX_GAME_SETTING.getJSONObject("multipliers");
        if (mult == null) mult = new JSONObject();
        MULT_TRIPLE = parseBigDecimal(mult.getString("triple"), new BigDecimal("10"));
        MULT_DOUBLE = parseBigDecimal(mult.getString("double"), new BigDecimal("4"));
        MULT_ALL_DIFF = parseBigDecimal(mult.getString("allDiff"), new BigDecimal("1.8"));

        log.info("[PBX] initGameSetting ok: TIME_SEC=" + TIME_SEC
                + ", CAPITAL_TYPE=" + CAPITAL_TYPE
                + ", CHIPS=" + CHIPS
                + ", FEE_RATE=" + FEE_RATE
                + ", ELEMENT_COUNT=" + ELEMENT_COUNT);
    }

    /**
     * 获取默认配置
     */
    private JSONObject defaultGameSetting() {
        JSONObject setting = new JSONObject();
        setting.put("time", "20");
        setting.put("capitalType", UserCapitalTypeEnum.xxxhhb.getValue());
        JSONArray chips = new JSONArray();
        chips.add("1");
        chips.add("10");
        chips.add("100");
        setting.put("chips", chips);
        setting.put("feeRate", "0.05");
        setting.put("elementCount", "6");
        return setting;
    }

    /**
     * 记录开奖历史
     */
    private void pushResultHistory(String periodNo, JSONArray resultElements, String resultType, int forcedNoWin) {
        try {
            JSONObject r = new JSONObject();
            r.put("periodNo", periodNo);
            r.put("resultElements", resultElements);
            r.put("resultType", resultType);
            r.put("forcedNoWin", forcedNoWin);
            r.put("ts", System.currentTimeMillis());

            synchronized (recent16Results) {
                recent16Results.addLast(r);
                while (recent16Results.size() > 16) recent16Results.removeFirst();
            }
            synchronized (recent100Results) {
                recent100Results.addLast(resultElements);
                while (recent100Results.size() > 100) recent100Results.removeFirst();
            }
        } catch (Exception e) {
            log.error("[PBX] pushResultHistory error", e);
        }
    }

    /**
     * 获取最近N期开奖结果
     */
    private JSONArray getRecentResults(int n) {
        if (n <= 16) return buildRecent16();
        return buildRecent100();
    }

    /**
     * 构建近16期详细历史
     */
    private JSONArray buildRecent16() {
        JSONArray arr = new JSONArray();
        synchronized (recent16Results) {
            List<JSONObject> list = new ArrayList<>(recent16Results);
            Collections.reverse(list);
            for (JSONObject o : list) arr.add(o);
        }
        return arr;
    }

    /**
     * 构建近100期简略历史
     */
    private JSONArray buildRecent100() {
        JSONArray arr = new JSONArray();
        synchronized (recent100Results) {
            List<JSONArray> list = new ArrayList<>(recent100Results);
            Collections.reverse(list);
            for (JSONArray a : list) arr.add(a);
        }
        return arr;
    }

    /**
     * 统计近16期各元素命中次数
     */
    private JSONObject buildRecent16Stat() {
        int[] counts = new int[ELEMENT_COUNT + 1];
        synchronized (recent16Results) {
            for (JSONObject r : recent16Results) {
                if (r == null) continue;
                JSONArray open = r.getJSONArray("resultElements");
                if (open == null) continue;
                for (int i = 0; i < open.size(); i++) {
                    int eid = open.getIntValue(i);
                    if (eid >= 1 && eid <= ELEMENT_COUNT) counts[eid]++;
                }
            }
        }
        JSONObject stat = new JSONObject();
        for (int i = 1; i <= ELEMENT_COUNT; i++) stat.put(String.valueOf(i), counts[i]);
        return stat;
    }

    /**
     * 统计近100期各元素命中次数
     */
    private JSONObject buildRecent100Stat() {
        int[] counts = new int[ELEMENT_COUNT + 1];
        synchronized (recent100Results) {
            for (JSONArray open : recent100Results) {
                if (open == null) continue;
                for (int i = 0; i < open.size(); i++) {
                    int eid = open.getIntValue(i);
                    if (eid >= 1 && eid <= ELEMENT_COUNT) counts[eid]++;
                }
            }
        }
        JSONObject stat = new JSONObject();
        for (int i = 1; i <= ELEMENT_COUNT; i++) stat.put(String.valueOf(i), counts[i]);
        return stat;
    }

    /**
     * 构建用户在当前各元素的下注额
     */
    private JSONObject buildMyElementBet(String userId) {
        JSONObject myElementBet = new JSONObject();
        Map<Integer, BigDecimal> myMap = periodUserElementBet.get(userId);
        for (int i = 1; i <= ELEMENT_COUNT; i++) {
            BigDecimal v = BigDecimal.ZERO;
            if (myMap != null) v = myMap.getOrDefault(i, BigDecimal.ZERO);
            myElementBet.put(String.valueOf(i), v.stripTrailingZeros().toPlainString());
        }
        return myElementBet;
    }

    /**
     * 构建用户当前总下注额
     */
    private String buildMyTotalBet(String userId) {
        BigDecimal myTotalBet = periodUserTotalBet.getOrDefault(userId, BigDecimal.ZERO);
        return myTotalBet.stripTrailingZeros().toPlainString();
    }

    /**
     * 构建下注详情字符串 (1,2,3)
     */
    private String buildBetInfo(Map<Integer, BigDecimal> um) {
        if (um == null || um.isEmpty()) return "";
        List<Integer> ks = new ArrayList<>(um.keySet());
        Collections.sort(ks);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ks.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(ks.get(i));
        }
        return sb.toString();
    }

    /**
     * 拼接开奖结果 (1,2,3)
     */
    String joinResultElements(JSONArray resultElements) {
        if (resultElements == null || resultElements.isEmpty()) {
            return "[]";
        }
        return resultElements.toJSONString();
    }


    /**
     * 解析请求中的筹码
     */
    private BigDecimal parseChip(JSONObject data) {
        String chipStr = data.getString("chip");
        if (isBlank(chipStr)) chipStr = data.getString("betAmount");
        if (isBlank(chipStr)) return null;
        try {
            BigDecimal c = new BigDecimal(chipStr);
            if (c.compareTo(BigDecimal.ZERO) <= 0) return null;
            return c.setScale(2, RoundingMode.DOWN).stripTrailingZeros();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 解析请求中的元素ID
     */
    private Integer parseElementId(JSONObject data) {
        try {
            if (data.containsKey("elementId")) return data.getIntValue("elementId");
        } catch (Exception ignored) {
        }
        return null;
    }

    /**
     * 校验筹码是否在允许列表中
     */
    private boolean isAllowedChip(BigDecimal chip) {
        if (chip == null || CHIPS == null || CHIPS.isEmpty()) return false;
        for (int i = 0; i < CHIPS.size(); i++) {
            Object o = CHIPS.get(i);
            if (o == null) continue;
            try {
                BigDecimal allowed = new BigDecimal(String.valueOf(o)).stripTrailingZeros();
                if (allowed.compareTo(chip.stripTrailingZeros()) == 0) return true;
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    /**
     * 字符串判空
     */
    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    /**
     * 解析整数，带默认值
     */
    private int parseInt(String s, int def) {
        try {
            if (isBlank(s)) return def;
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return def;
        }
    }

    /**
     * 解析金额，带默认值
     */
    private BigDecimal parseBigDecimal(String s, BigDecimal def) {
        try {
            if (isBlank(s)) return def;
            return new BigDecimal(s.trim());
        } catch (Exception e) {
            return def;
        }
    }

    /**
     * 计算周Key (yyyyWW)
     */
    private int calcWeekKey(long nowMs) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Shanghai"));
        cal.setTimeInMillis(nowMs);
        int year = cal.get(Calendar.YEAR);
        int week = cal.get(Calendar.WEEK_OF_YEAR);
        return year * 100 + week;
    }

    /**
     * 确保周数据切换
     */
    private void ensureWeek(long nowMs) {
        int wk = calcWeekKey(nowMs);
        if (currentWeekKey == 0) {
            currentWeekKey = wk;
            return;
        }
        if (wk != currentWeekKey) {
            lastWeekUserTotalBet.clear();
            lastWeekUserTotalBet.putAll(weekUserTotalBet);
            lastWeekRankPoolBalance = weekRankPoolBalance;
            weekUserTotalBet.clear();
            weekRankPoolBalance = BigDecimal.ZERO;
            currentWeekKey = wk;
            log.info("[PBX] week switch -> " + currentWeekKey);
        }
    }

    /**
     * 获取当前格式化时间字符串
     */
    private String nowStr() {
        return PBX_SDF.get().format(new Date());
    }

    /**
     * 获取指定时间戳的格式化时间字符串
     */
    private String dateTimeString(long ms) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date(ms));
    }

    /**
     * 生成新订单号
     */
    private String newOrderNo() {
        return PBX_ORDER_SDF.get().format(new Date()) + ThreadLocalRandom.current().nextInt(10, 99);
    }

    /**
     * 获取当前期开始时间
     */
    private long getPeriodStartMs(long nowMs) {
        ensureCurrentPeriod(nowMs);
        return currentPeriodStartMs;
    }

    /**
     * 获取当前期结束时间
     */
    private long getPeriodEndMs(long nowMs) {
        ensureCurrentPeriod(nowMs);
        return currentPeriodEndMs;
    }

    /**
     * 结算期快照对象
     */
    private static class PeriodSnapshot {
        final String periodNo;
        final long startMs;
        final long endMs;
        final Map<Integer, BigDecimal> elementTotalBet;
        final Map<String, Map<Integer, BigDecimal>> userElementBet;
        final Map<String, BigDecimal> userTotalBet;

        PeriodSnapshot(String periodNo, long startMs, long endMs,
                       Map<Integer, BigDecimal> elementTotalBet,
                       Map<String, Map<Integer, BigDecimal>> userElementBet,
                       Map<String, BigDecimal> userTotalBet) {
            this.periodNo = periodNo;
            this.startMs = startMs;
            this.endMs = endMs;
            this.elementTotalBet = elementTotalBet;
            this.userElementBet = userElementBet;
            this.userTotalBet = userTotalBet;
        }
    }

    /**
     * 开奖结果选择对象
     */
    private static class OutcomePick {
        final JSONArray resultElements;
        final String resultType;
        final boolean forceLose;
        final Integer winElement;
        final Set<Integer> winElements;
        final BigDecimal multiplier;

        OutcomePick(JSONArray resultElements, String resultType, boolean forceLose,
                    Integer winElement, Set<Integer> winElements, BigDecimal multiplier) {
            this.resultElements = resultElements;
            this.resultType = resultType;
            this.forceLose = forceLose;
            this.winElement = winElement;
            this.winElements = winElements;
            this.multiplier = multiplier;
        }
    }
}
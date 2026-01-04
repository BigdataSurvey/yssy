package com.zywl.app.service;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.socket.BaseClientSocket;
import com.live.app.ws.util.Push;
import com.zywl.app.base.service.BaseService;
import com.live.app.ws.enums.PushCode;
import com.live.app.ws.interfacex.Listener;
import com.zywl.app.base.util.StringUtils;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.socket.BattleRoyaleSocketServer2;
import com.live.app.ws.bean.Command;
import com.zywl.app.base.bean.Game;
import com.zywl.app.defaultx.service.GameService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.Map;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.ThreadFactory;
/**
 * @Author: lzx
 * @Create: 2025/12/20
 * @Version: V1.0
 * @Description: 推箱子 (PBX) 核心业务服务
 */
@Service
@ServiceClass(code = "102")
public class PbxService extends BaseService {

    private static final Log log = LogFactory.getLog(PbxService.class);
    private static final ThreadLocal<SimpleDateFormat> PBX_SDF = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"));
    private static final ThreadLocal<SimpleDateFormat> PBX_ORDER_SDF =ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyyMMddHHmmssSSS"));

    /*配置表中默认值*/
    /** 游戏 ID;对应数据库 l_game.id */
    private static final int PBX_GAME_ID = 12;

    /** 游戏完整配置快照*/
    private volatile JSONObject PBX_GAME_SETTING = new JSONObject();

    /** 单期游戏时长 (秒) */
    private volatile int TIME_SEC = 20;

    /** 游戏元素数量 (ElementId 范围: 1 .. ELEMENT_COUNT) */
    private volatile int ELEMENT_COUNT = 6;

    /** 下注扣款的资产类型 ID ( 1002: 游戏消耗货币) */
    private volatile int CAPITAL_TYPE = 1002;

    /** 平台手续费率 (例如 0.05 代表 5%) */
    private volatile BigDecimal FEE_RATE = new BigDecimal("0.05");

    /** 可选筹码档位列表 (格式: ["1", "10", "100"]) */
    private volatile JSONArray CHIPS = new JSONArray();

    /** 三同号赔率 (Triple) */
    private volatile BigDecimal MULT_TRIPLE = new BigDecimal("10");
    /** 两同号赔率 (Double) */
    private volatile BigDecimal MULT_DOUBLE = new BigDecimal("4");
    /** 三不同号赔率 (All Different) */
    private volatile BigDecimal MULT_ALL_DIFF = new BigDecimal("1.8");

    /** 周期调度器启动状态标记*/
    private final AtomicBoolean periodSchedulerStarted = new AtomicBoolean(false);

    /** 期号切换同步锁 */
    private final Object PERIOD_LOCK = new Object();

    /** 周期任务调度线程池 */
    private ScheduledExecutorService periodScheduler;

    /** 当前期号的开始时间 (毫秒) */
    private volatile long currentPeriodStartMs = 0L;

    /** 当前期号的结束时间 (毫秒) */
    private volatile long currentPeriodEndMs = 0L;

    /** 已完成结算的期号集合 */
    private final Set<String> settledPeriodNoSet = java.util.concurrent.ConcurrentHashMap.newKeySet();

    /** 全服奖池余额缓存*/
    private volatile BigDecimal lastPoolBalance = null;

    /** 最近一次同步的服务器时间字符串 */
    private volatile String lastServerTime = null;

    /** 近16期开奖记录*/
    private final Deque<JSONObject> recent16Results = new ArrayDeque<>();

    /** 近100期统计数据*/
    private final Deque<JSONArray> recent100Results = new ArrayDeque<>();

    // 本周统计
    private volatile JSONArray weekRankTop10 = new JSONArray();
    private volatile BigDecimal weekConsume = BigDecimal.ZERO;
    private volatile BigDecimal weekReturn = BigDecimal.ZERO;
    private volatile BigDecimal weekProfit = BigDecimal.ZERO;
    private volatile BigDecimal weekDividendPool = BigDecimal.ZERO;
    private volatile boolean weekSettled = false;

    // 上周统计
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

    /** 在线用户状态缓存 */
    private final Map<String, JSONObject> onlineUserState = new ConcurrentHashMap<>();

    /** 当前期 bucket */
    private volatile long currentPeriodBucket = -1L;

    /** 当前期编号*/
    private volatile String currentPeriodNo = null;

    /** 本期元素总下注额*/
    private final Map<Integer, BigDecimal> periodElementTotalBet = new ConcurrentHashMap<>();

    /** 本期用户-元素下注额*/
    private final Map<String, Map<Integer, BigDecimal>> periodUserElementBet = new ConcurrentHashMap<>();

    /** 本期用户总下注额*/
    private final Map<String, BigDecimal> periodUserTotalBet = new ConcurrentHashMap<>();

    /** 当前周 key：yyyyWW */
    private volatile int currentWeekKey = 0;

    /** 本周玩家累计投入*/
    private final Map<String, BigDecimal> weekUserTotalBet = new ConcurrentHashMap<>();

    /** 上周玩家累计投入快照 */
    private final Map<String, BigDecimal> lastWeekUserTotalBet = new ConcurrentHashMap<>();

    /** 本周榜单奖池*/
    private volatile BigDecimal weekRankPoolBalance = BigDecimal.ZERO;

    /** 上周榜单奖池快照 */
    private volatile BigDecimal lastWeekRankPoolBalance = BigDecimal.ZERO;

    @Autowired
    private GameService gameService;

    @Autowired
    private BattleRoyaleRequsetMangerService2 requsetMangerService2;

    /**
     * 服务启动入口
     */
    @PostConstruct
    public void init() {
        long t1 = System.currentTimeMillis();
        log.info("================ [PBX] 服务初始化 ================");
        try {
            // 初始化游戏静态配置
            initGameSetting();
            // 初始化一个期号状态
            ensureCurrentPeriod(t1);
            // 启动核心周期调度器
            startPeriodScheduler();
            log.info("================ [PBX] 服务已启动 (Cost: " + (System.currentTimeMillis() - t1) + "ms) ================");
        } catch (Exception e) {
            log.error("[PBX] 初始化失败", e);
            throw new RuntimeException("[PBX] 初始化失败", e);
        }
    }
    private void startPeriodScheduler() {
        if (periodSchedulerStarted.compareAndSet(false, true)) {
            periodScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r);
                t.setName("pbx-period-scheduler");
                t.setDaemon(true);
                return t;
            });
            // 每秒 tick 一次：检测期号切换并触发上一期自动结算
            periodScheduler.scheduleAtFixedRate(() -> {
                try {
                    tickPeriod();
                } catch (Throwable t) {
                    log.error("[PBX] tickPeriod error", t);
                }
            }, 1, 1, TimeUnit.SECONDS);
        }
    }

    private void tickPeriod() {
        long nowMs = System.currentTimeMillis();
        // Step1：每秒推进“期状态机”，并对在线玩家推送倒计时与奖池信息
        ensureCurrentPeriod(nowMs);
        if (onlineUserState.isEmpty()) {
            return;
        }
        try {
            pushPbxInfo(lastPoolBalance);
        } catch (Exception e) {
            log.error("[PBX] tickPeriod pushPbxInfo error", e);
        }
    }

    private static class PeriodSnapshot {
        final String periodNo;
        final long startMs;
        final long endMs;
        final Map<Integer, BigDecimal> elementTotalBet;
        final Map<String, Map<Integer, BigDecimal>> userElementBet;
        final Map<String, BigDecimal> userTotalBet;

        PeriodSnapshot(String periodNo,
                       long startMs,
                       long endMs,
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

    private Map<String, Map<Integer, BigDecimal>> deepCopyUserElementBet(Map<String, Map<Integer, BigDecimal>> src) {
        Map<String, Map<Integer, BigDecimal>> copy = new HashMap<>();
        if (src == null) {
            return copy;
        }
        for (Map.Entry<String, Map<Integer, BigDecimal>> e : src.entrySet()) {
            Map<Integer, BigDecimal> inner = new HashMap<>();
            if (e.getValue() != null) {
                inner.putAll(e.getValue());
            }
            copy.put(e.getKey(), inner);
        }
        return copy;
    }

    private void settlePeriodAsync(PeriodSnapshot snapshot) {
        if (snapshot == null || isBlank(snapshot.periodNo)) {
            return;
        }
        // 避免重复结算
        if (!settledPeriodNoSet.add(snapshot.periodNo)) {
            return;
        }
        if (periodScheduler != null) {
            periodScheduler.execute(() -> settlePeriod(snapshot));
        } else {
            // 极端情况下 scheduler 未启动，直接当前线程结算
            settlePeriod(snapshot);
        }
    }

    private static class OutcomePick {
        final JSONArray resultElements;
        final String resultType;
        final boolean forceLose;
        final Integer winElement;        // triple/double 的“中奖元素”；allDiff 为 null
        final Set<Integer> winElements;  // allDiff 的中奖元素集合；triple/double 为 null
        final BigDecimal multiplier;     // 10 / 4 / 1.8

        OutcomePick(JSONArray resultElements,
                    String resultType,
                    boolean forceLose,
                    Integer winElement,
                    Set<Integer> winElements,
                    BigDecimal multiplier) {
            this.resultElements = resultElements;
            this.resultType = resultType;
            this.forceLose = forceLose;
            this.winElement = winElement;
            this.winElements = winElements;
            this.multiplier = multiplier;
        }
    }

    private BigDecimal safe(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private BigDecimal calcNetForControl(BigDecimal gross) {
        if (gross == null) return BigDecimal.ZERO;
        BigDecimal fee = gross.multiply(FEE_RATE).setScale(2, java.math.RoundingMode.HALF_UP);
        return gross.setScale(2, java.math.RoundingMode.HALF_UP).subtract(fee);
    }

    private OutcomePick pickOutcome(Map<Integer, BigDecimal> elementTotalBet, BigDecimal poolBalance) {
        Map<Integer, BigDecimal> t = (elementTotalBet == null) ? new HashMap<>() : elementTotalBet;

        List<OutcomePick> candidates = new ArrayList<>();
        List<OutcomePick> all = new ArrayList<>();

        // triple
        for (int e = 1; e <= ELEMENT_COUNT; e++) {
            JSONArray res = new JSONArray();
            res.add(e);res.add(e);res.add(e);

            BigDecimal gross = safe(t.get(e)).multiply(MULT_TRIPLE);
            BigDecimal net = calcNetForControl(gross);

            OutcomePick p = new OutcomePick(res, "TRIPLE", false, e, null, MULT_TRIPLE);
            all.add(p);
            if (poolBalance != null && net.compareTo(poolBalance) <= 0) {
                candidates.add(p);
            }
        }

        // double: (e,e,f) 只赔付 e
        for (int e = 1; e <= ELEMENT_COUNT; e++) {
            for (int f = 1; f <= ELEMENT_COUNT; f++) {
                if (f == e) continue;
                JSONArray res = new JSONArray();
                res.add(e);res.add(e);res.add(f);

                BigDecimal gross = safe(t.get(e)).multiply(MULT_DOUBLE);
                BigDecimal net = calcNetForControl(gross);

                OutcomePick p = new OutcomePick(res, "DOUBLE", false, e, null, MULT_DOUBLE);
                all.add(p);
                if (poolBalance != null && net.compareTo(poolBalance) <= 0) {
                    candidates.add(p);
                }
            }
        }

        // allDiff: {a,b,c} 赔付 a/b/c
        for (int a = 1; a <= ELEMENT_COUNT; a++) {
            for (int b = a + 1; b <= ELEMENT_COUNT; b++) {
                for (int c = b + 1; c <= ELEMENT_COUNT; c++) {
                    JSONArray res = new JSONArray();
                    res.add(a);res.add(b);res.add(c);

                    BigDecimal gross = safe(t.get(a)).add(safe(t.get(b))).add(safe(t.get(c))).multiply(MULT_ALL_DIFF);
                    BigDecimal net = calcNetForControl(gross);

                    Set<Integer> winSet = new HashSet<>();
                    winSet.add(a);winSet.add(b);winSet.add(c);

                    OutcomePick p = new OutcomePick(res, "ALL_DIFF", false, null, winSet, MULT_ALL_DIFF);
                    all.add(p);
                    if (poolBalance != null && net.compareTo(poolBalance) <= 0) {
                        candidates.add(p);
                    }
                }
            }
        }

        // 无候选：强制全输，但仍需给一个“展示结果”（取最小净支出结果）
        if (candidates.isEmpty()) {
            OutcomePick min = Collections.min(all, Comparator.comparing(o -> calcNetForControl(calcGrossForOutcome(t, o))));
            return new OutcomePick(min.resultElements, min.resultType, true, min.winElement, min.winElements, min.multiplier);
        }

        // 有候选：随机选一个
        OutcomePick picked = candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
        return picked;
    }

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

    private BigDecimal calcUserGross(Map<Integer, BigDecimal> userBetByElement, OutcomePick pick) {
        if (pick == null) return BigDecimal.ZERO;
        if (userBetByElement == null || userBetByElement.isEmpty()) {
            return BigDecimal.ZERO;
        }
        if ("TRIPLE".equals(pick.resultType) || "DOUBLE".equals(pick.resultType)) {
            BigDecimal bet = safe(userBetByElement.get(pick.winElement));
            return bet.multiply(pick.multiplier).setScale(2, java.math.RoundingMode.HALF_UP);
        }
        if ("ALL_DIFF".equals(pick.resultType)) {
            BigDecimal sum = BigDecimal.ZERO;
            for (Integer e : pick.winElements) {
                sum = sum.add(safe(userBetByElement.get(e)));
            }
            return sum.multiply(pick.multiplier).setScale(2, java.math.RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    private void pushResultHistory(String periodNo, JSONArray resultElements, String resultType,int forcedNoWin) {
        try {
            JSONObject r = new JSONObject();
            r.put("periodNo", periodNo);
            r.put("resultElements", resultElements);
            r.put("resultType", resultType);
            r.put("forcedNoWin", forcedNoWin);
            r.put("ts", System.currentTimeMillis());

            synchronized (recent16Results) {
                recent16Results.addLast(r);
                while (recent16Results.size() > 16) {
                    recent16Results.removeFirst();
                }
            }

            synchronized (recent100Results) {
                recent100Results.addLast(resultElements);
                while (recent100Results.size() > 100) {
                    recent100Results.removeFirst();
                }
            }
        } catch (Exception e) {
            log.error("[PBX] pushResultHistory error", e);
        }
    }


    private JSONObject buildAutoSettleStatusPush(String userId,
                                                 String periodNo,
                                                 JSONArray resultElements,
                                                 BigDecimal gross,
                                                 BigDecimal fee,
                                                 BigDecimal net,
                                                 BigDecimal balance,
                                                 BigDecimal poolBalance,
                                                 String serverTime,
                                                 String message,
                                                 int forcedNoWin) {
        JSONObject push = new JSONObject();
        push.put("gameId", String.valueOf(PBX_GAME_ID));

        JSONArray userIds = new JSONArray();
        userIds.add(userId);
        push.put("userIds", userIds);

        push.put("status", 3); // 3=结算完成
        push.put("success", true);
        push.put("orderNo", periodNo); // 结算链路以 periodNo 作为唯一标识即可

        JSONObject info = new JSONObject();
        info.put("userId", userId);
        info.put("orderNo", periodNo);
        info.put("periodNo", periodNo);
        info.put("resultElements", resultElements);
        info.put("forcedNoWin", forcedNoWin);
        info.put("returnAmount", (gross == null ? BigDecimal.ZERO : gross).setScale(2, java.math.RoundingMode.HALF_UP));
        info.put("fee", (fee == null ? BigDecimal.ZERO : fee).setScale(2, java.math.RoundingMode.HALF_UP));
        info.put("net", (net == null ? BigDecimal.ZERO : net).setScale(2, java.math.RoundingMode.HALF_UP));

        if (balance != null) {
            info.put("balance", balance.setScale(2, java.math.RoundingMode.HALF_UP));
        }
        if (poolBalance != null) {
            info.put("poolBalance", poolBalance.setScale(2, java.math.RoundingMode.HALF_UP));
        }
        if (!isBlank(serverTime)) {
            info.put("serverTime", serverTime);
        }
        info.put("ts", System.currentTimeMillis());
        if (!isBlank(message)) {
            info.put("message", message);
        }

        push.put("userSettleInfo", info);
        return push;
    }

    private void settlePeriod(PeriodSnapshot snapshot) {
        try {
            if (snapshot == null || isBlank(snapshot.periodNo)) {
                return;
            }
            // 没有下注直接略过
            if (snapshot.userTotalBet == null || snapshot.userTotalBet.isEmpty()) {
                return;
            }

            // 1) 查询奖池余额（MANAGER 为准）
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
                if (!isBlank(st)) {
                    serverTime = st;
                }
            }

            // 2) 计算并选择开奖结果（控盘）
            OutcomePick pick = pickOutcome(snapshot.elementTotalBet, poolBalance);
            JSONArray resultElements = pick.resultElements;
            String resultType = pick.resultType;
            int forcedNoWinFlag = pick.forceLose ? 1 : 0;
            // 记录近16/近100
            pushResultHistory(snapshot.periodNo, resultElements, resultType,forcedNoWinFlag);

            // 3) 强制全输（不调用 MANAGER 派奖）
            if (pick.forceLose) {
                for (String uid : snapshot.userTotalBet.keySet()) {
                    JSONObject state = onlineUserState.get(uid);
                    if (state != null) {
                        state.put("status", 1);
                        state.put("ts", System.currentTimeMillis());
                    }
                    // 【修复处 1】强制庄赢分支：传入 1
                    JSONObject pushStatus = buildAutoSettleStatusPush(uid, snapshot.periodNo, resultElements,
                            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                            null, poolBalance, serverTime, "pool not enough, force lose",
                            1);
                    Push.push(PushCode.updatePbxStatus, null, pushStatus);
                }

                JSONObject infoPush = buildPbxInfoByPeriod(poolBalance, snapshot.periodNo, snapshot.startMs, snapshot.endMs, snapshot.endMs);
                infoPush.put("status", 3);
                infoPush.put("resultElements", resultElements);
                infoPush.put("resultType", resultType);
                // 建议：广播信息也带上这个标记，方便前端展示“本期轮空”等文案
                infoPush.put("forcedNoWin", 1);
                Push.push(PushCode.updatePbxInfo, null, infoPush);
                return;
            }

            // 4) 构造 winList（gross）并调用 MANAGER 派奖（200721）
            JSONArray winList = new JSONArray();
            for (Map.Entry<String, Map<Integer, BigDecimal>> e : snapshot.userElementBet.entrySet()) {
                String userId = e.getKey();
                BigDecimal gross = calcUserGross(e.getValue(), pick);
                if (gross != null && gross.compareTo(BigDecimal.ZERO) > 0) {
                    JSONObject one = new JSONObject();
                    one.put("userId", userId);
                    one.put("returnAmount", gross.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString());
                    winList.add(one);
                }
            }

            // 如果本期没人中奖（正常输，非强制）
            if (winList.isEmpty()) {
                for (String uid : snapshot.userTotalBet.keySet()) {
                    JSONObject state = onlineUserState.get(uid);
                    if (state != null) {
                        state.put("status", 1);
                        state.put("ts", System.currentTimeMillis());
                    }
                    // 【修复处 2】正常未中奖：传入 0
                    JSONObject pushStatus = buildAutoSettleStatusPush(uid, snapshot.periodNo, resultElements,
                            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                            null, poolBalance, serverTime, null,
                            0);
                    Push.push(PushCode.updatePbxStatus, null, pushStatus);
                }

                JSONObject infoPush = buildPbxInfoByPeriod(poolBalance, snapshot.periodNo, snapshot.startMs, snapshot.endMs, snapshot.endMs);
                infoPush.put("status", 3);
                infoPush.put("resultElements", resultElements);
                infoPush.put("resultType", resultType);
                infoPush.put("forcedNoWin", 0); // 正常情况为 0
                Push.push(PushCode.updatePbxInfo, null, infoPush);
                return;
            }

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
            // 如果结算失败
            if (settleResp == null || !settleResp.getBooleanValue("success")) {
                String msg = (settleResp == null) ? "pbxSettle response null" : settleResp.getString("message");
                for (String uid : snapshot.userTotalBet.keySet()) {
                    JSONObject state = onlineUserState.get(uid);
                    if (state != null) {
                        state.put("status", 1);
                        state.put("ts", System.currentTimeMillis());
                    }
                    // 【修复处 3】结算异常兜底：传入 0
                    JSONObject pushStatus = buildAutoSettleStatusPush(uid, snapshot.periodNo, resultElements,
                            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                            null, poolBalance, serverTime, msg,
                            0);
                    Push.push(PushCode.updatePbxStatus, null, pushStatus);
                }

                JSONObject infoPush = buildPbxInfoByPeriod(poolBalance, snapshot.periodNo, snapshot.startMs, snapshot.endMs, snapshot.endMs);
                infoPush.put("status", 3);
                infoPush.put("resultElements", resultElements);
                infoPush.put("resultType", resultType);
                infoPush.put("forcedNoWin", 0);
                Push.push(PushCode.updatePbxInfo, null, infoPush);
                return;
            }

            // 正常结算成功分支
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

            for (String uid : snapshot.userTotalBet.keySet()) {
                JSONObject state = onlineUserState.get(uid);
                if (state != null) {
                    state.put("status", 1);
                    state.put("ts", System.currentTimeMillis());
                }

                JSONObject u = userInfoMap.get(uid);
                BigDecimal gross = (u == null) ? BigDecimal.ZERO : u.getBigDecimal("returnAmount");
                BigDecimal fee = (u == null) ? BigDecimal.ZERO : u.getBigDecimal("fee");
                BigDecimal net = (u == null) ? BigDecimal.ZERO : u.getBigDecimal("net");
                BigDecimal balance = (u == null) ? null : u.getBigDecimal("balance");

                // 【修复处 4】正常结算推送：传入 0
                JSONObject pushStatus = buildAutoSettleStatusPush(uid, snapshot.periodNo, resultElements,
                        gross, fee, net, balance, newPoolBalance, serverTime, null,
                        0);
                Push.push(PushCode.updatePbxStatus, null, pushStatus);
            }

            JSONObject infoPush = buildPbxInfoByPeriod(newPoolBalance, snapshot.periodNo, snapshot.startMs, snapshot.endMs, snapshot.endMs);
            infoPush.put("status", 3);
            infoPush.put("resultElements", resultElements);
            infoPush.put("resultType", resultType);
            infoPush.put("forcedNoWin", 0);
            Push.push(PushCode.updatePbxInfo, null, infoPush);

        } catch (Exception e) {
            log.error("[PBX] auto settle exception, periodNo={}"+  snapshot.periodNo, e);
        }
    }

    /**
     * 从 l_game(id=12).game_setting 初始化 PBX 玩法配置。
     * <p>注意：不得写死配置；如果 DB 配置缺失则回退默认值，避免启动失败。</p>
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

        // 解析关键字段（确保类型可用）
        TIME_SEC = parseInt(PBX_GAME_SETTING.getString("time"), 20);
        CAPITAL_TYPE = parseInt(PBX_GAME_SETTING.getString("capitalType"), 1002);
        CHIPS = PBX_GAME_SETTING.getJSONArray("chips");
        if (CHIPS == null) {
            CHIPS = new JSONArray();
            CHIPS.add("1");
            CHIPS.add("10");
            CHIPS.add("100");
        }
        FEE_RATE = parseBigDecimal(PBX_GAME_SETTING.getString("feeRate"), new BigDecimal("0.05"));
        ELEMENT_COUNT = parseInt(PBX_GAME_SETTING.getString("elementCount"), 6);



        // multipliers（倍率）
        JSONObject mult = PBX_GAME_SETTING.getJSONObject("multipliers");
        if (mult == null) {
            mult = new JSONObject();
        }
        MULT_TRIPLE = parseBigDecimal(mult.getString("triple"), new BigDecimal("10"));
        MULT_DOUBLE = parseBigDecimal(mult.getString("double"), new BigDecimal("4"));
        MULT_ALL_DIFF = parseBigDecimal(mult.getString("allDiff"), new BigDecimal("1.8"));
        log.info("[PBX] initGameSetting ok: TIME_SEC=" + TIME_SEC
                + ", CAPITAL_TYPE=" + CAPITAL_TYPE
                + ", CHIPS=" + CHIPS
                + ", FEE_RATE=" + FEE_RATE
                + ", ELEMENT_COUNT=" + ELEMENT_COUNT);
    }

    private JSONObject defaultGameSetting() {
        JSONObject setting = new JSONObject();
        setting.put("time", "20");
        setting.put("capitalType", "1002");
        JSONArray chips = new JSONArray();
        chips.add("1");
        chips.add("10");
        chips.add("100");
        setting.put("chips", chips);
        setting.put("feeRate", "0.05");
        // multipliers/elementCount/poolResetCycle/rankProfitPercent/top10Rates 等由 DB 提供，
        // 默认只兜底关键字段，避免 NPE。
        setting.put("elementCount", "6");
        return setting;
    }

    // -------------------------------------------------------------------------
    // 102101：Join
    // -------------------------------------------------------------------------

    @ServiceMethod(code = "101", description = "推箱子-加入房间")
    public Object joinRoom(BattleRoyaleSocketServer2 adminSocketServer, Command lotteryCommand, JSONObject data) {
        long nowMs = System.currentTimeMillis();
        checkNull(data);

        String userId = data.getString("userId");
        if (isBlank(userId)) {
            throwExp("参数错误");
        }

        // userNo 允许不传（debug/接口服可不带），这里兜底生成
        String userNo = data.getString("userNo");
        if (isBlank(userNo)) {
            userNo = "U" + userId;
        }

        // 写在线状态
        JSONObject state = new JSONObject();
        state.put("userId", userId);
        state.put("userNo", userNo);
        state.put("status", 1);
        state.put("ts", System.currentTimeMillis());
        onlineUserState.put(userId, state);

        // 查询主服奖池（join 要返回 poolBalance/serverTime）
        JSONObject q = queryPoolFromManager(userId);
        BigDecimal poolBalance = q.getBigDecimal("poolBalance");
        String serverTime = q.getString("serverTime");
        // 历史总投入
        BigDecimal myTotalConsume = q.getBigDecimal("myTotalConsume");
        // 历史总返还
        BigDecimal myTotalReturn  = q.getBigDecimal("myTotalReturn");
        // 历史总到手
        BigDecimal myTotalNet     = q.getBigDecimal("myTotalNet");

        if (myTotalConsume == null) myTotalConsume = BigDecimal.ZERO;
        if (myTotalReturn  == null) myTotalReturn  = BigDecimal.ZERO;
        if (myTotalNet     == null) myTotalNet     = BigDecimal.ZERO;

        // 记录最近值（供 leave/info push 使用）
        if (poolBalance != null) {
            lastPoolBalance = poolBalance;
        }
        if (!isBlank(serverTime)) {
            lastServerTime = serverTime;
        }

        // 推送信息变更（在线人数 + 奖池）
        pushPbxInfo(lastPoolBalance);

        JSONObject resp = new JSONObject();
        resp.put("success", true);
        resp.put("gameId", String.valueOf(PBX_GAME_ID));
        resp.put("userId", userId);
        resp.put("userNo", userNo);
        resp.put("status", 1);
        resp.put("onlineCount", onlineUserState.size());
        resp.put("gameSetting", PBX_GAME_SETTING);
        resp.put("poolBalance", poolBalance);
        resp.put("serverTime", serverTime);
        String periodNo = ensureCurrentPeriod(nowMs);
        resp.put("periodNo", periodNo);
        long periodStartMs = getPeriodStartMs(nowMs);
        long periodEndMs = getPeriodEndMs(nowMs);
        resp.putAll(buildPbxInfoByPeriod(lastPoolBalance, periodNo, periodStartMs, periodEndMs, nowMs));
        // 个人本期投入（每元素/总计）
        resp.put("myElementBet", buildMyElementBet(userId));
        resp.put("myTotalBet", buildMyTotalBet(userId));
        //个人历史游戏记录
        resp.put("myTotalConsume", myTotalConsume);
        resp.put("myTotalReturn", myTotalReturn);
        resp.put("myTotalNet", myTotalNet);

        return resp;
    }


    @ServiceMethod(code = "103", description = "推箱子-下注/操作")
    public Object operate(BattleRoyaleSocketServer2 adminSocketServer, Command lotteryCommand, JSONObject data) {
        checkNull(data);
        String userId = data.getString("userId");
        if (isBlank(userId)) {
            throwExp("参数错误");
        }

        BigDecimal chip = parseChip(data);
        Integer elementId = parseElementId(data);

        if (chip == null) {
            throwExp("参数错误");
        }
        if (!isAllowedChip(chip)) {
            throwExp("参数错误 chip not allowed, allowed=" + CHIPS);
        }
        if (elementId == null) {
            elementId = 1;
        }
        if (elementId < 1 || elementId > ELEMENT_COUNT) {
            throwExp("参数错误 elementId out of range: 1.." + ELEMENT_COUNT);
        }

        String periodNo = ensureCurrentPeriod(System.currentTimeMillis());

        // 【需求重点】本地生成 orderNoForAck，确保无论主服是否成功，都能返回给前端做幂等/追踪
        String orderNoForAck = newOrderNo();

        JSONObject state = onlineUserState.get(userId);
        if (state != null) {
            state.put("status", 2);
            state.put("ts", System.currentTimeMillis());
        }
        final AtomicReference<JSONObject> betRespRef = new AtomicReference<>();
        final CountDownLatch betLatch = new CountDownLatch(1);

        JSONObject betReq = new JSONObject();
        betReq.put("gameId", String.valueOf(PBX_GAME_ID));
        betReq.put("userId", userId);
        betReq.put("betAmount", chip.stripTrailingZeros().toPlainString());
        betReq.put("capitalType", CAPITAL_TYPE);
        betReq.put("feeRate", FEE_RATE);
        betReq.put("periodNo", periodNo);
        betReq.put("elementId", elementId);
        betReq.put("chip", chip.stripTrailingZeros().toPlainString());
        // 传给 Manager，万一它成功了，它会透传回来；失败了我们用本地的
        betReq.put("orderNo", orderNoForAck);

        Integer finalElementId = elementId;

        requsetMangerService2.requestPbxBet(betReq, new com.live.app.ws.interfacex.Listener() {
            @Override
            public void handle(com.live.app.ws.socket.BaseClientSocket socket, Command command) {
                JSONObject resp = null;
                try {
                    // 1) 检查 RPC 异常
                    if (command == null) {
                        String msg = "pbxBet command is null";
                        resp = new JSONObject();
                        resp.put("success", false);
                        resp.put("orderNo", orderNoForAck); // 兜底订单号
                        resp.put("message", msg);
                        pushBetFailed(userId, orderNoForAck, periodNo, finalElementId, chip, msg);
                        return;
                    }

                    // 2) 检查 业务 异常 (如余额不足)
                    if (!command.isSuccess()) {
                        String msg = command.getMessage();
                        if (isBlank(msg)) msg = "pbxBet failed (manager error)";

                        resp = new JSONObject();
                        resp.put("success", false);
                        resp.put("orderNo", orderNoForAck); // 兜底订单号
                        resp.put("message", msg);
                        pushBetFailed(userId, orderNoForAck, periodNo, finalElementId, chip, msg);
                        return;
                    }

                    // 3) 检查 Data 为空
                    resp = (JSONObject) command.getData();
                    if (resp == null) {
                        String msg = "pbxBet response data is null";
                        resp = new JSONObject();
                        resp.put("success", false);
                        resp.put("orderNo", orderNoForAck);
                        resp.put("message", msg);
                        pushBetFailed(userId, orderNoForAck, periodNo, finalElementId, chip, msg);
                        return;
                    }

                    // 4) 检查 Data 里的 success 字段
                    boolean ok = resp.getBooleanValue("success");
                    if (!ok) {
                        String msg = resp.getString("message");
                        if (isBlank(msg)) msg = "pbxBet failed";
                        String retOrderNo = resp.getString("orderNo");
                        if (isBlank(retOrderNo)) retOrderNo = orderNoForAck;

                        // 确保 resp 里有订单号供前端使用
                        resp.put("orderNo", retOrderNo);

                        pushBetFailed(userId, retOrderNo, periodNo, finalElementId, chip, msg);
                        return;
                    }

                    // === 成功逻辑 ===
                    String managerOrderNo = resp.getString("orderNo");
                    if (isBlank(managerOrderNo)) managerOrderNo = orderNoForAck;

                    BigDecimal balance = resp.getBigDecimal("balance");
                    BigDecimal poolBalance = resp.getBigDecimal("poolBalance");
                    BigDecimal fee = resp.getBigDecimal("fee");
                    BigDecimal feeRate = resp.getBigDecimal("feeRate");

                    if (poolBalance != null) {
                        lastPoolBalance = poolBalance;
                    }

                    recordBet(periodNo, userId, finalElementId, chip);

                    JSONObject pushStatus = buildPbxStatusPush(
                            userId, 2, true, managerOrderNo, periodNo, finalElementId, chip,
                            balance, poolBalance, fee, feeRate
                    );
                    Push.push(PushCode.updatePbxStatus, null, pushStatus);
                    pushPbxInfo(lastPoolBalance);

                } catch (Exception e) {
                    log.error("[PBX] pbxBet callback exception", e);
                    // 异常时也要构造 resp
                    resp = new JSONObject();
                    resp.put("success", false);
                    resp.put("orderNo", orderNoForAck);
                    resp.put("message", "pbxBet callback exception");
                    pushBetFailed(userId, orderNoForAck, periodNo, finalElementId, chip, "pbxBet callback exception");
                } finally {
                    // 关键：现在 resp 无论成败都有值了
                    betRespRef.set(resp);
                    betLatch.countDown();
                }
            }
        });

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
            // [需求落实] 超时也返回 orderNo
            fail.put("orderNo", orderNoForAck);
            fail.put("message", awaited ? "pbxBet response is null" : "pbxBet timeout");
            fail.put("gameSetting", PBX_GAME_SETTING);
            return fail;
        }
        if (!resp.getBooleanValue("success")) {
            // 补齐字段防止前端解析由缺
            if(!resp.containsKey("gameId")) resp.put("gameId", String.valueOf(PBX_GAME_ID));
            if(!resp.containsKey("gameSetting")) resp.put("gameSetting", PBX_GAME_SETTING);
            return resp;
        }
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

    private void pushBetFailed(String userId, String orderNo, String periodNo, Integer elementId, BigDecimal chip, String message) {
        // 失败时状态回到 1（在房间空闲）
        JSONObject state = onlineUserState.get(userId);
        if (state != null) {
            state.put("status", 1);
            state.put("ts", System.currentTimeMillis());
        }
        JSONObject pushStatus = buildPbxStatusPush(
                userId,
                1,
                false,
                orderNo,
                periodNo,
                elementId,
                chip,
                null,
                lastPoolBalance,
                null,
                FEE_RATE
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

    // -------------------------------------------------------------------------
    // 102104：Leave
    // -------------------------------------------------------------------------

    @ServiceMethod(code = "104", description = "推箱子-离开房间")
    public Object leaveRoom(BattleRoyaleSocketServer2 adminSocketServer, Command lotteryCommand, JSONObject data) {
        long nowMs = System.currentTimeMillis();
        checkNull(data);

        String userId = data.getString("userId");
        if (isBlank(userId)) {
            throwExp("参数错误");
        }

        onlineUserState.remove(userId);

        // 推送信息变更（leave 不强制查询主服，使用 lastPoolBalance 即可）
        pushPbxInfo(lastPoolBalance);

        JSONObject resp = new JSONObject();
        resp.put("success", true);
        resp.put("gameId", String.valueOf(PBX_GAME_ID));
        resp.put("userId", userId);
        resp.put("status", 0);
        resp.put("onlineCount", onlineUserState.size());
        return resp;
    }

    // -------------------------------------------------------------------------
    // 102105：Query（DTS2 -> Manager 200722）
    // -------------------------------------------------------------------------

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
        // 历史总投入
        BigDecimal myTotalConsume = q.getBigDecimal("myTotalConsume");
        // 历史总返还
        BigDecimal myTotalReturn  = q.getBigDecimal("myTotalReturn");
        // 历史总到手
        BigDecimal myTotalNet     = q.getBigDecimal("myTotalNet");
        if (poolBalance != null) {
            lastPoolBalance = poolBalance;
        }
        if (!isBlank(serverTime)) {
            lastServerTime = serverTime;
        }

        // push 信息变更（带 poolBalance）
        pushPbxInfo(lastPoolBalance);

        JSONObject resp = new JSONObject();
        resp.put("success", true);
        resp.put("gameId", String.valueOf(PBX_GAME_ID));
        resp.put("poolBalance", poolBalance);
        resp.put("serverTime", serverTime);
        resp.put("onlineCount", onlineUserState.size());
        resp.put("gameSetting", PBX_GAME_SETTING);
        String periodNo = ensureCurrentPeriod(nowMs);
        resp.put("periodNo", periodNo);
        long periodStartMs = getPeriodStartMs(nowMs);
        long periodEndMs = getPeriodEndMs(nowMs);
        resp.putAll(buildPbxInfoByPeriod(lastPoolBalance, periodNo, periodStartMs, periodEndMs, nowMs));
        // 个人本期投入（每元素/总计）
        resp.put("myElementBet", buildMyElementBet(userId));
        resp.put("myTotalBet", buildMyTotalBet(userId));
        //个人历史游戏记录
        resp.put("myTotalConsume", myTotalConsume);
        resp.put("myTotalReturn", myTotalReturn);
        resp.put("myTotalNet", myTotalNet);

        return resp;
    }

    // -------------------------------------------------------------------------
    // 102106：Settle（DTS2 -> Manager 200721）
    // 说明：此处仍为 Debug 透传结算（你传 gross/payouts），后续 Step C 再实现控盘开奖与 winList 计算。
    // -------------------------------------------------------------------------

    @ServiceMethod(code = "106", description = "推箱子-结算派奖（debug透传）")
    public Object settle(BattleRoyaleSocketServer2 adminSocketServer, Command lotteryCommand, JSONObject data) {
        checkNull(data);

        // Manager 721 要求：gameId + periodNo + winList + feeRate 等
        String periodNo = data.getString("periodNo");
        if (isBlank(periodNo)) {
            // 未实现“100期”前，允许不传：用当前期兜底，便于 debug
            periodNo = ensureCurrentPeriod(System.currentTimeMillis());
        }

        // payouts: [{userId:"xxx", gross:"10.00"}] 或 [{userId, returnAmount}] 均可
        JSONArray payouts = data.getJSONArray("payouts");
        if (payouts == null) {
            payouts = new JSONArray();
        }

        JSONArray winList = new JSONArray();
        for (int i = 0; i < payouts.size(); i++) {
            JSONObject p = payouts.getJSONObject(i);
            if (p == null) {
                continue;
            }
            String uid = p.getString("userId");
            if (isBlank(uid)) {
                continue;
            }
            BigDecimal gross = p.getBigDecimal("gross");
            if (gross == null) {
                gross = p.getBigDecimal("returnAmount");
            }
            if (gross == null) {
                continue;
            }
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
            public void handle(com.live.app.ws.socket.BaseClientSocket socket, Command command) {
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

        if (!ok) {
            throwExp("未知异常");
        }

        JSONObject resp = ref.get();
        if (resp != null && resp.getBooleanValue("success")) {
            BigDecimal poolBalance = resp.getBigDecimal("poolBalance");
            if (poolBalance != null) {
                lastPoolBalance = poolBalance;
            }
            // 结算完成后广播一次信息
            pushPbxInfo(lastPoolBalance);
        }

        return resp == null ? throwExp("pbxSettle response is null"): resp;
    }


    @ServiceMethod(code = "107",  description = "PBX 周榜结算")
    public Object processWeekSettle(JSONObject data) {
        Integer gameId = data.getInteger("gameId");
        if (gameId == null) {
            gameId = PBX_GAME_ID;
        }
        if (gameId != PBX_GAME_ID) {
            throwExp("gameId invalid");
        }

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
            public void handle(com.live.app.ws.socket.BaseClientSocket socket, Command command) {
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


    // -------------------------------------------------------------------------
    // 内部：向主服查询奖池（200722）
    // -------------------------------------------------------------------------

    /**
     * 向主服请求 pbxQuery（200722），同步等待结果并返回：
     * {poolBalance: BigDecimal, serverTime: String}
     */
    private JSONObject queryPoolFromManager(String userId) {
        JSONObject queryReq = new JSONObject();
        queryReq.put("gameId", String.valueOf(PBX_GAME_ID));
        queryReq.put("userId", userId);

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<JSONObject> ref = new AtomicReference<>();

        requsetMangerService2.requestPbxQuery(queryReq, new Listener() {
            @Override
            public void handle(com.live.app.ws.socket.BaseClientSocket socket, Command command) {
                try {
                    if (command != null && command.isSuccess()) {
                        ref.set((JSONObject) command.getData());
                    } else {
                        // 构造一个带错误信息的空对象，防止 NPE
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
        } catch (Exception ignore) {}

        return resp;
    }

    /**
     * 兼容：无参查询奖池（默认 userId=0，表示系统侧查询）
     */
    private JSONObject queryPoolFromManager() {
        return queryPoolFromManager("0");
    }


    // -------------------------------------------------------------------------
    // Push 构造与发送
    // -------------------------------------------------------------------------

    /**
     * 推送 PBX 信息变更（第一跳：condition 必须为空/null）。
     * <p>接口服收到后会按 gameId 做二跳转推。</p>
     */
    private void pushPbxInfo(BigDecimal poolBalance) {
        long nowMs = System.currentTimeMillis();
        String periodNo = currentPeriodNo;
        if (periodNo == null) {
            periodNo = ensureCurrentPeriod(nowMs);
        }
        if (periodNo == null) {
            return;
        }
        if (poolBalance == null) {
            poolBalance = BigDecimal.ZERO;
        }

        JSONObject info = buildPbxInfoByPeriod(poolBalance, periodNo, currentPeriodStartMs, currentPeriodEndMs, nowMs);
        Push.push(PushCode.updatePbxInfo, null, info);
    }

    /**
     * 构造 updatePbxInfo 推送体。
     */
    private JSONObject buildPbxInfo(String lastServerTime, BigDecimal poolBalance, String periodNo) {
        long nowMs = System.currentTimeMillis();
        if (poolBalance == null) {
            poolBalance = BigDecimal.ZERO;
        }
        return buildPbxInfoByPeriod(poolBalance, periodNo, currentPeriodStartMs, currentPeriodEndMs, nowMs);
    }

    /**
     * 兼容：按期构建 info（你之前按 buildPbxInfo(poolBalance, periodNo, startMs, endMs, nowMs) 的写法）
     */
    private JSONObject buildPbxInfo(BigDecimal poolBalance, String periodNo, long periodStartMs, long periodEndMs, long nowMs) {
        return buildPbxInfoByPeriod(poolBalance, periodNo, periodStartMs, periodEndMs, nowMs);
    }


    /**
     * 构造推箱子“本期信息”：
     * - 期号 periodNo
     * - 本期开始/结束时间（startTs/endTs + startTime/endTime）
     * - 剩余秒 remainSeconds
     * - 状态 status：1=下注中，2=结算中（结算完成由 settlePeriod 额外写入 3）
     */
    private JSONObject buildPbxInfoByPeriod(BigDecimal poolBalance, String periodNo, long periodStartMs, long periodEndMs, long nowMs) {
        JSONObject info = new JSONObject();
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

        // 1=下注中，2=结算中（结算完成在 settlePeriod 推送中使用 3）
        info.put("status", remainSec > 0 ? 1 : 2);

        info.put("recent16", getRecentResults(16));
        info.put("recent100", getRecentResults(100));
        info.put("recent16Stat", buildRecent16Stat());
        info.put("recent100Stat", buildRecent100Stat());
        // 本期全服各元素总投入（用于前端每张元素卡片显示“全服投入”）
        JSONObject elementTotalBet = new JSONObject();
        BigDecimal totalBet = BigDecimal.ZERO;
        for (int i = 1; i <= ELEMENT_COUNT; i++) {
            BigDecimal v = periodElementTotalBet.getOrDefault(i, BigDecimal.ZERO);
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
     * 兼容封装：按需求文档下发 recent16 / recent100
     * - n<=16 返回 buildRecent16()
     * - n>16 返回 buildRecent100()
     */
    private JSONArray getRecentResults(int n) {
        if (n <= 16) {
            return buildRecent16();
        }
        return buildRecent100();
    }

    private JSONArray buildRecent16() {
        JSONArray arr = new JSONArray();
        synchronized (recent16Results) {
            List<JSONObject> list = new ArrayList<>(recent16Results);
            Collections.reverse(list);
            for (JSONObject o : list) {
                arr.add(o);
            }
        }
        return arr;
    }

    private JSONArray buildRecent100() {
        JSONArray arr = new JSONArray();
        synchronized (recent100Results) {
            List<JSONArray> list = new ArrayList<>(recent100Results);
            Collections.reverse(list);
            for (JSONArray a : list) {
                arr.add(a);
            }
        }
        return arr;
    }

    /** 近16期统计（元素ID -> 命中次数；每期3个元素） */
    private JSONObject buildRecent16Stat() {
        int[] counts = new int[ELEMENT_COUNT + 1];
        synchronized (recent16Results) {
            for (JSONObject r : recent16Results) {
                if (r == null) continue;
                JSONArray open = r.getJSONArray("resultElements");
                if (open == null) continue;
                for (int i = 0; i < open.size(); i++) {
                    int eid = open.getIntValue(i);
                    if (eid >= 1 && eid <= ELEMENT_COUNT) {
                        counts[eid]++;
                    }
                }
            }
        }
        JSONObject stat = new JSONObject();
        for (int i = 1; i <= ELEMENT_COUNT; i++) {
            stat.put(String.valueOf(i), counts[i]);
        }
        return stat;
    }

    /** 近100期统计（元素ID -> 命中次数；每期3个元素，总计300次） */
    private JSONObject buildRecent100Stat() {
        int[] counts = new int[ELEMENT_COUNT + 1];
        synchronized (recent100Results) {
            for (JSONArray open : recent100Results) {
                if (open == null) continue;
                for (int i = 0; i < open.size(); i++) {
                    int eid = open.getIntValue(i);
                    if (eid >= 1 && eid <= ELEMENT_COUNT) {
                        counts[eid]++;
                    }
                }
            }
        }
        JSONObject stat = new JSONObject();
        for (int i = 1; i <= ELEMENT_COUNT; i++) {
            stat.put(String.valueOf(i), counts[i]);
        }
        return stat;
    }


    /**
     * 兼容：将 int[] 转为 JSONArray（用于 resultElements / recent100 等字段）
     */
    private JSONArray toJsonArray(int[] arr) {
        JSONArray ja = new JSONArray();
        if (arr == null) {
            return ja;
        }
        for (int v : arr) {
            ja.add(v);
        }
        return ja;
    }

    private String dateTimeString(long ms) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date(ms));
    }
    private long getPeriodStartMs(long nowMs) {
        // 确保 currentPeriodStartMs/currentPeriodEndMs 是最新期
        ensureCurrentPeriod(nowMs);
        return currentPeriodStartMs;
    }

    private long getPeriodEndMs(long nowMs) {
        ensureCurrentPeriod(nowMs);
        return currentPeriodEndMs;
    }

    /**
     * 构造 updatePbxStatus 推送体（第一跳：condition 必须为空/null）。
     *
     * <p>注意：接口服 BattleRoyale2Socket 只会转发：
     * <ul>
     *     <li>gameStatus（status）</li>
     *     <li>userSettleInfo（原样）</li>
     * </ul>
     * 因此需要把 periodNo/elementId/chip/orderNo 等都放入 userSettleInfo。</p>
     */
    private JSONObject buildPbxStatusPush(
            String userId,
            int status,
            boolean success,
            String orderNo,
            String periodNo,
            Integer elementId,
            BigDecimal chip,
            BigDecimal balance,
            BigDecimal poolBalance,
            BigDecimal fee,
            BigDecimal feeRate
    ) {
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
            userSettleInfo.put("betAmount", chip.stripTrailingZeros().toPlainString()); // 兼容字段
        }
        if (balance != null) {
            userSettleInfo.put("balance", balance);
        }
        if (poolBalance != null) {
            userSettleInfo.put("poolBalance", poolBalance);
        } else if (lastPoolBalance != null) {
            userSettleInfo.put("poolBalance", lastPoolBalance);
        }
        if (fee != null) {
            userSettleInfo.put("fee", fee);
        }
        if (feeRate != null) {
            userSettleInfo.put("feeRate", feeRate);
        }

        // 本期个人各元素投入（用于前端每张元素卡片显示“我的投入”）
        JSONObject myElementBet = new JSONObject();
        Map<Integer, BigDecimal> myMap = periodUserElementBet.get(userId);
        for (int i = 1; i <= ELEMENT_COUNT; i++) {
            BigDecimal v = BigDecimal.ZERO;
            if (myMap != null) {
                v = myMap.getOrDefault(i, BigDecimal.ZERO);
            }
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
     * 当前周期内：某个用户对每个元素的投注额。
     *
     * <p>返回结构：{"1":"0",...,"6":"10"}，value 统一为两位小数去尾零的字符串；未投则为"0"。</p>
     */
    private JSONObject buildMyElementBet(String userId) {
        JSONObject myElementBet = new JSONObject();
        Map<Integer, BigDecimal> myMap = periodUserElementBet.get(userId);
        for (int i = 1; i <= ELEMENT_COUNT; i++) {
            BigDecimal v = BigDecimal.ZERO;
            if (myMap != null) {
                v = myMap.getOrDefault(i, BigDecimal.ZERO);
            }
            myElementBet.put(String.valueOf(i), v.stripTrailingZeros().toPlainString());
        }
        return myElementBet;
    }

    /**
     * 当前周期内：某个用户的总投注额（两位小数去尾零的字符串）。
     */
    private String buildMyTotalBet(String userId) {
        BigDecimal myTotalBet = periodUserTotalBet.getOrDefault(userId, BigDecimal.ZERO);
        return myTotalBet.stripTrailingZeros().toPlainString();
    }

    // -------------------------------------------------------------------------
    // Step C-2.1：内存聚合
    // -------------------------------------------------------------------------

    /**
     * 记录一笔“扣款成功”的下注到本期聚合。
     */
    private void recordBet(String periodNo, String userId, Integer elementId, BigDecimal chip) {
        if (chip == null || elementId == null) {
            return;
        }

        // 确保当前期一致（periodNo 来自 ensureCurrentPeriod）
        ensureCurrentPeriod(System.currentTimeMillis());

        // 元素总下注
        periodElementTotalBet.merge(elementId, chip, BigDecimal::add);

        // 用户-元素下注
        Map<Integer, BigDecimal> userMap = periodUserElementBet.computeIfAbsent(userId, k -> new ConcurrentHashMap<>());
        userMap.merge(elementId, chip, BigDecimal::add);

        // 用户总下注
        periodUserTotalBet.merge(userId, chip, BigDecimal::add);
        // 周榜累计
        ensureWeek(System.currentTimeMillis());
        weekUserTotalBet.merge(userId, chip, BigDecimal::add);
    }

    /**
     * 计算并确保 currentPeriodNo 存在；当检测到 TIME_SEC 切换到新 bucket 时，重置本期聚合。
     * <p>当前实现按 TIME_SEC 秒分桶，仅满足 Step C-2.1（后续 100 期/周期调度再统一替换）。</p>
     */
    private String ensureCurrentPeriod(long nowMs) {
        long periodMs = (TIME_SEC <= 0) ? 20000L : (TIME_SEC * 1000L);
        long bucket = nowMs / periodMs;
        String periodNo = "PBX_" + bucket;

        PeriodSnapshot snapshotToSettle = null;
        // 周榜：跨周切换时做快照/重置
        ensureWeek(nowMs);
        synchronized (PERIOD_LOCK) {
            // 本期开始/结束时间（毫秒）
            long startMs = bucket * periodMs;
            long endMs = startMs + periodMs;

            // 初始化或同一期：只更新当前期信息
            if (currentPeriodNo == null || bucket == currentPeriodBucket) {
                currentPeriodBucket = bucket;
                currentPeriodNo = periodNo;
                currentPeriodStartMs = startMs;
                currentPeriodEndMs = endMs;
                return currentPeriodNo;
            }

            // 进入新一期：先对上一期推送“结算中”，再快照上一期数据并异步结算
            String prevPeriodNo = currentPeriodNo;
            long prevStartMs = currentPeriodBucket * periodMs;
            long prevEndMs = prevStartMs + periodMs;

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

            snapshotToSettle = new PeriodSnapshot(
                    prevPeriodNo,
                    prevStartMs,
                    prevEndMs,
                    new HashMap<>(periodElementTotalBet),
                    new HashMap<>(periodUserElementBet),
                    new HashMap<>(periodUserTotalBet)
            );

            // 清空上一期投注数据，为新一期做准备
            periodElementTotalBet.clear();
            periodUserElementBet.clear();
            periodUserTotalBet.clear();

            currentPeriodBucket = bucket;
            currentPeriodNo = periodNo;
            currentPeriodStartMs = startMs;
            currentPeriodEndMs = endMs;
        }

        // 异步结算上一期
        if (snapshotToSettle != null) {
            settlePeriodAsync(snapshotToSettle);
        }
        return currentPeriodNo;
    }

    // -------------------------------------------------------------------------
    // 参数解析与校验
    // -------------------------------------------------------------------------

    private BigDecimal parseChip(JSONObject data) {
        String chipStr = data.getString("chip");
        if (isBlank(chipStr)) {
            // 兼容旧字段
            chipStr = data.getString("betAmount");
        }
        if (isBlank(chipStr)) {
            return null;
        }
        try {
            BigDecimal c = new BigDecimal(chipStr);
            if (c.compareTo(BigDecimal.ZERO) <= 0) {
                return null;
            }
            // 限制到两位（筹码通常是整数，但这里容错）
            return c.setScale(2, RoundingMode.DOWN).stripTrailingZeros();
        } catch (Exception e) {
            return null;
        }
    }

    private Integer parseElementId(JSONObject data) {
        try {
            if (data.containsKey("elementId")) {
                return data.getIntValue("elementId");
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private boolean isAllowedChip(BigDecimal chip) {
        if (chip == null || CHIPS == null || CHIPS.isEmpty()) {
            return false;
        }
        for (int i = 0; i < CHIPS.size(); i++) {
            Object o = CHIPS.get(i);
            if (o == null) {
                continue;
            }
            try {
                BigDecimal allowed = new BigDecimal(String.valueOf(o)).stripTrailingZeros();
                if (allowed.compareTo(chip.stripTrailingZeros()) == 0) {
                    return true;
                }
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    // -------------------------------------------------------------------------
    // 通用工具
    // -------------------------------------------------------------------------

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private int parseInt(String s, int def) {
        try {
            if (isBlank(s)) {
                return def;
            }
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return def;
        }
    }

    private BigDecimal parseBigDecimal(String s, BigDecimal def) {
        try {
            if (isBlank(s)) {
                return def;
            }
            return new BigDecimal(s.trim());
        } catch (Exception e) {
            return def;
        }
    }
    private int calcWeekKey(long nowMs) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Shanghai"));
        cal.setTimeInMillis(nowMs);
        int year = cal.get(Calendar.YEAR);
        int week = cal.get(Calendar.WEEK_OF_YEAR);
        return year * 100 + week;
    }

    private void ensureWeek(long nowMs) {
        int wk = calcWeekKey(nowMs);
        if (currentWeekKey == 0) {
            currentWeekKey = wk;
            return;
        }
        if (wk != currentWeekKey) {
            // 快照到上周
            lastWeekUserTotalBet.clear();
            lastWeekUserTotalBet.putAll(weekUserTotalBet);
            lastWeekRankPoolBalance = weekRankPoolBalance;

            // 清空本周
            weekUserTotalBet.clear();
            weekRankPoolBalance = BigDecimal.ZERO;

            currentWeekKey = wk;
            log.info("[PBX] week switch -> " + currentWeekKey);
        }
    }
    private String nowStr() {  return PBX_SDF.get().format(new Date());}

    private String newOrderNo() {
        return PBX_ORDER_SDF.get().format(new Date()) + ThreadLocalRandom.current().nextInt(10, 99);
    }
}

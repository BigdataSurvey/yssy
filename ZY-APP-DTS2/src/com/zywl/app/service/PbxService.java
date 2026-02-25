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
import com.zywl.app.defaultx.service.DailyTaskProgressService;
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
import java.util.concurrent.atomic.AtomicInteger;
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
    private static final ThreadLocal<SimpleDateFormat> PBX_SDF = ThreadLocal
            .withInitial(() -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"));
    private static final ThreadLocal<SimpleDateFormat> PBX_ORDER_SDF = ThreadLocal
            .withInitial(() -> new SimpleDateFormat("yyyyMMddHHmmssSSS"));

    /** 游戏 ID (对应数据库 l_game.id) */
    private static final int PBX_GAME_ID = 12;

    @Autowired
    private DailyTaskProgressService dailyTaskProgressService;

    /** 机器人用户池缓存 (UserId -> UserObj) */
    private static final Map<String, User> BOT_USER = new ConcurrentHashMap<>();

    /** 游戏完整配置快照 */
    private volatile JSONObject PBX_GAME_SETTING = new JSONObject();

    /** 单期游戏时长 (秒) */
    private volatile int TIME_SEC = 20;

    /** 游戏元素数量 (ElementId 范围: 1 .. ELEMENT_COUNT) */
    private volatile int ELEMENT_COUNT = 6;
    // 结算中的期号快照（用于接住“跨期晚到”的下注回调，避免丢单导致不结算）
    private final ConcurrentHashMap<String, PeriodSnapshot> settlingSnapshotMap = new ConcurrentHashMap<>();
    /** 下注扣款的资产类型 ID (1002: 游戏消耗货币) */
    private volatile int CAPITAL_TYPE = UserCapitalTypeEnum.xxxhhb.getValue();

    /** 平台手续费率 (例如 0.05 代表 5%) */
    private volatile BigDecimal FEE_RATE = new BigDecimal("0.05");

    /** 可选筹码档位列表 (格式: ["1", "10", "100"]) */
    private volatile JSONArray CHIPS = new JSONArray();

    /** 赔率配置 */
    private volatile BigDecimal MULT_TRIPLE = new BigDecimal("10"); // 三同号
    private volatile BigDecimal MULT_DOUBLE = new BigDecimal("4"); // 两同号
    private volatile BigDecimal MULT_ALL_DIFF = new BigDecimal("1.8"); // 三不同号

    /** 周期调度器相关 */
    private final AtomicBoolean periodSchedulerStarted = new AtomicBoolean(false);
    private ScheduledExecutorService periodScheduler;
    private final Object PERIOD_LOCK = new Object();

    /** 当前期号信息 */
    private volatile long currentPeriodBucket = -1L; // 时间桶
    private volatile String currentPeriodNo = null; // 期号
    private volatile long currentPeriodStartMs = 0L; // 开始时间
    private volatile long currentPeriodEndMs = 0L; // 结束时间

    /** 结算去重集合，防止同一期号重复结算 */
    private final Set<String> settledPeriodNoSet = ConcurrentHashMap.newKeySet();

    /** 全服数据缓存 */
    private volatile BigDecimal lastPoolBalance = null; // 奖池余额
    private volatile String lastServerTime = null; // 服务器时间

    /** 历史记录缓存 */
    private final Deque<JSONObject> recent16Results = new ArrayDeque<>(); // 近16期详细
    private final Deque<JSONArray> recent100Results = new ArrayDeque<>(); // 近100期简略

    /** 在线用户状态缓存 (UserId -> StatusObj) */
    private final Map<String, JSONObject> onlineUserState = new ConcurrentHashMap<>();

    /** 本期下注数据 (周期重置) */
    private final Map<Integer, BigDecimal> periodElementTotalBet = new ConcurrentHashMap<>(); // 元素总注
    private final Map<String, Map<Integer, BigDecimal>> periodUserElementBet = new ConcurrentHashMap<>(); // 用户各元素注
    private final Map<String, BigDecimal> periodUserTotalBet = new ConcurrentHashMap<>(); // 用户总注

    /** 当前周 Key (yyyyWW) */
    private volatile int currentWeekKey = 0;

    /** 本周玩家累计投入 (用于榜单计算) */
    private final Map<String, BigDecimal> weekUserTotalBet = new ConcurrentHashMap<>();
    /** 上周玩家累计投入快照 */
    private final Map<String, BigDecimal> lastWeekUserTotalBet = new ConcurrentHashMap<>();

    /** 榜单奖池 */
    private volatile BigDecimal weekRankPoolBalance = BigDecimal.ZERO;
    private volatile BigDecimal lastWeekRankPoolBalance = BigDecimal.ZERO;

    /** 本期是否已经触发过结算（下注截止后触发一次） */
    private volatile boolean currentPeriodSettleTriggered = false;

    // ✅结算展示期时长（秒）：倒计时结束后“停留”几秒用于展示结算/开奖结果弹窗
    private volatile int SETTLE_SEC = 5;

    // ✅当前周期的“展示期结束时间”(ms) —— 用于判断何时真正切到下一期
    private volatile long currentCycleEndMs = 0L;
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

    /** 本期已开奖缓存（用于展示期内防止 tick 覆盖） */
    private volatile String openedPeriodNo = null;
    private volatile JSONArray openedResultElements = null;
    private volatile String openedResultType = null;
    private volatile int openedForcedNoWin = 0;
    private volatile long lastHistoryPushMs = 0L;
    /** 机器人下注概率（0~100） */
    private volatile int NEED_BOT = 0;
    /** 机器人可用筹码 */
    private final List<BigDecimal> BOT_CHIPS = new ArrayList<>();
    /** 本期机器人下注总额 (仅展示用) */
    private final Map<Integer, BigDecimal> periodBotElementTotalBet = new ConcurrentHashMap<>();
    /** 机器人调度器 */
    private ScheduledExecutorService botScheduler;
    private final AtomicBoolean botSchedulerStarted = new AtomicBoolean(false);
    // === 新增：每期待回调下注计数（解决“回调晚到 -> 本期判空 -> 不结算”）
    private final ConcurrentHashMap<String, AtomicInteger> pendingBetMap = new ConcurrentHashMap<>();
    // ===== PBX 周期调度器保活 =====
    private final Object SCHEDULER_LOCK = new Object();
    private volatile long lastTickAtMs = 0L;
    // ===== PBX 调度器 watchdog（独立线程，防止 tick 卡死后永不恢复）=====
    private ScheduledExecutorService watchdogScheduler;
    private final AtomicBoolean watchdogStarted = new AtomicBoolean(false);
    private volatile long schedulerStartAtMs = 0L;

    private void incPending(String periodNo) {
        if (isBlank(periodNo))
            return;
        pendingBetMap.computeIfAbsent(periodNo, k -> new AtomicInteger(0)).incrementAndGet();
    }

    private void decPending(String periodNo) {
        if (isBlank(periodNo))
            return;
        AtomicInteger ai = pendingBetMap.get(periodNo);
        if (ai == null)
            return;
        int v = ai.decrementAndGet();
        if (v <= 0)
            pendingBetMap.remove(periodNo);
    }

    private int getPending(String periodNo) {
        AtomicInteger ai = pendingBetMap.get(periodNo);
        return ai == null ? 0 : Math.max(0, ai.get());
    }

    /** 推送专用线程池：避免结算线程被大量 Push.push 拖死导致下一期不开奖 */
    private final ExecutorService pushExecutor = new ThreadPoolExecutor(
            4, 4,
            60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(20000),
            r -> {
                Thread t = new Thread(r);
                t.setName("pbx-push-worker-" + t.getId());
                t.setDaemon(true);
                return t;
            },
            new ThreadPoolExecutor.CallerRunsPolicy());

    private static class SummaryCache {
        final long ts;
        final JSONObject data;

        SummaryCache(long ts, JSONObject data) {
            this.ts = ts;
            this.data = data;
        }
    }

    private final ConcurrentHashMap<Long, SummaryCache> recordCache = new ConcurrentHashMap<>();
    private static final long RECORD_CACHE_TTL_MS = 3000L;

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
            // ✅新增：watchdog
            startWatchdogScheduler();
            // 初始化并启动机器人
            initBotUsers();
            initBotConfig();
            startBotScheduler();
            log.info("================ [PBX] 服务已启动 (Cost: " + (System.currentTimeMillis() - t1)
                    + "ms) ================");
        } catch (Exception e) {
            log.error("[PBX] 初始化失败", e);
            throw new RuntimeException("[PBX] 初始化失败", e);
        }
    }

    /**
     * 启动主周期调度器 (1秒/次)，用于驱动期号切换
     * ✅修复：lastTickAtMs 必须在 tick 执行完后更新，否则卡死时 watchdog 无法感知
     */
    private void startPeriodScheduler() {
        if (periodSchedulerStarted.compareAndSet(false, true)) {

            schedulerStartAtMs = System.currentTimeMillis();
            lastTickAtMs = schedulerStartAtMs; // 初始心跳

            periodScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r);
                t.setName("pbx-period-scheduler");
                t.setDaemon(true);
                return t;
            });

            periodScheduler.scheduleAtFixedRate(() -> {
                try {
                    tickPeriod();
                } catch (Throwable t) {
                    log.error("[PBX] tickPeriod error", t);
                } finally {
                    // ✅关键：tick 真正结束后再更新
                    lastTickAtMs = System.currentTimeMillis();
                }
            }, 1, 1, TimeUnit.SECONDS);

            log.info("[PBX] period scheduler started.");
        }
    }

    /**
     * 每秒驱动逻辑：状态机推进与广播
     * 修复跨期时保证上一期一定会结算（避免“最后5秒不开奖/不弹框”）
     */
    private void tickPeriod() {
        long tickStartMs = System.currentTimeMillis();
        long nowMs = tickStartMs;

        PeriodSnapshot rolloverSnapToSettle = null;

        // ===== A) 在换期前做“兜底结算捕获”
        // 关键：如果已经越过 currentCycleEndMs（即本期结算窗口也过了），
        // tick 先 ensureCurrentPeriod 会直接换期并清空下注缓存 -> 上一期就丢了。
        // 所以这里必须先抓一份“上一期快照”并异步结算。
        synchronized (PERIOD_LOCK) {
            // 当前期号已存在，且已经跨过 cycleEnd：说明本期下注期+结算窗口都结束了
            if (!isBlank(currentPeriodNo) && nowMs >= currentCycleEndMs) {
                String prevPeriodNo = currentPeriodNo;

                // 上一期还没结算、也没在结算中 -> 兜底抓快照
                boolean alreadySettled = settledPeriodNoSet.contains(prevPeriodNo);
                boolean alreadySnapshot = settlingSnapshotMap.containsKey(prevPeriodNo);

                if (!alreadySettled && !alreadySnapshot) {
                    // 深拷贝下注缓存（避免 ensureCurrentPeriod 清空后丢失）
                    Map<Integer, BigDecimal> eTotal = new HashMap<>(periodElementTotalBet);

                    Map<String, Map<Integer, BigDecimal>> uE = new HashMap<>();
                    for (Map.Entry<String, Map<Integer, BigDecimal>> en : periodUserElementBet.entrySet()) {
                        String uid = en.getKey();
                        Map<Integer, BigDecimal> m = en.getValue();
                        if (uid == null || m == null)
                            continue;
                        uE.put(uid, new HashMap<>(m));
                    }

                    Map<String, BigDecimal> uTotal = new HashMap<>(periodUserTotalBet);

                    rolloverSnapToSettle = new PeriodSnapshot(
                            prevPeriodNo,
                            currentPeriodStartMs,
                            currentPeriodEndMs,
                            eTotal,
                            uE,
                            uTotal);

                    // 先放入 settlingSnapshotMap：允许“跨期晚到回调”继续归集到该快照
                    settlingSnapshotMap.put(prevPeriodNo, rolloverSnapToSettle);

                    log.warn("[PBX] tick rollover fallback: force settle prev period. periodNo="
                            + prevPeriodNo + ", nowMs=" + nowMs
                            + ", periodEndMs=" + currentPeriodEndMs
                            + ", cycleEndMs=" + currentCycleEndMs
                            + ", userBetCount=" + (rolloverSnapToSettle.userTotalBet == null ? 0
                                    : rolloverSnapToSettle.userTotalBet.size()));
                }
            }
        }

        // ===== B) 先把兜底快照丢给异步结算（避免 ensureCurrentPeriod 清空后无法结算）
        if (rolloverSnapToSettle != null) {
            try {
                settlePeriodAsync(rolloverSnapToSettle);
            } catch (Exception e) {
                log.error("[PBX] tick rollover fallback settlePeriodAsync error, periodNo="
                        + rolloverSnapToSettle.periodNo, e);
            }
        }

        // ===== C) 正常流程：确保当前期号（处理换期）
        ensureCurrentPeriod(nowMs);

        // ===== D) 正常流程：到达下注截止点触发结算（结算窗口内）
        tryTriggerSettleWhenBetEnd(nowMs);

        // ===== E) 推送倒计时/奖池
        if (!onlineUserState.isEmpty()) {
            try {
                pushPbxInfo(lastPoolBalance);
            } catch (Exception e) {
                log.error("[PBX] tickPeriod pushPbxInfo error", e);
            }
        }

        long tickDuration = System.currentTimeMillis() - tickStartMs;
        if (tickDuration > 2000L) {
            log.warn("[PBX] tick slow! duration=" + tickDuration + "ms, periodNo=" + currentPeriodNo);
        }
    }

    /**
     * 到达下注截止点（currentPeriodEndMs）时触发结算：
     * - 先推送 status=2（结算中）
     * - 创建快照 snapshot（允许跨期晚到回调归集）
     * - 异步 settlePeriod(snapshot)
     *
     * 注意：只触发一次，直到换期才重置 currentPeriodSettleTriggered=false
     */
    /**
     * 到达下注截止点（currentPeriodEndMs）时触发结算：
     * - 先推送 status=2（结算中）
     * - 创建快照 snapshot（允许跨期晚到回调归集）
     * - 异步 settlePeriod(snapshot)
     *
     * 注意：只触发一次，直到换期才重置 currentPeriodSettleTriggered=false
     */
    private void tryTriggerSettleWhenBetEnd(long nowMs) {
        // 下注尚未截止
        if (nowMs < currentPeriodEndMs)
            return;

        // 已经触发过结算
        if (currentPeriodSettleTriggered)
            return;

        // 仍处于本期结算窗口内（下注截止 <= now < cycleEnd）
        if (nowMs >= currentCycleEndMs)
            return;

        final PeriodSnapshot snapshot;
        final String periodNo;
        final long startMs;
        final long endMs;
        final boolean needPush;

        synchronized (PERIOD_LOCK) {
            // 双重检查，避免并发触发
            if (currentPeriodSettleTriggered)
                return;
            if (nowMs < currentPeriodEndMs)
                return;
            if (nowMs >= currentCycleEndMs)
                return;

            currentPeriodSettleTriggered = true;

            periodNo = currentPeriodNo;
            startMs = currentPeriodStartMs;
            endMs = currentPeriodEndMs;
            needPush = !onlineUserState.isEmpty();

            // 创建本期下注快照（用于 settle）
            snapshot = new PeriodSnapshot(
                    periodNo,
                    startMs,
                    endMs,
                    new HashMap<>(periodElementTotalBet),
                    new HashMap<>(periodUserElementBet),
                    new HashMap<>(periodUserTotalBet));

            settlingSnapshotMap.put(periodNo, snapshot);
        }

        // ✅ 关键修复：将 Push.push + buildPbxInfoByPeriod 移出 PERIOD_LOCK
        // 避免 JSON 构建 + 网络推送占用锁，导致 tick 线程阻塞（倒计时卡住的根因）
        if (needPush) {
            try {
                BigDecimal pool = (lastPoolBalance == null) ? BigDecimal.ZERO : lastPoolBalance;
                JSONObject settlingInfo = buildPbxInfoByPeriod(pool, periodNo, startMs, endMs, nowMs);
                settlingInfo.put("status", 2);
                Push.push(PushCode.updatePbxInfo, null, settlingInfo);
            } catch (Exception e) {
                log.error("[PBX] push settling info error, periodNo=" + periodNo, e);
            }
        }

        settlePeriodAsync(snapshot);
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
        state.put("ts", nowMs);
        onlineUserState.put(userId, state);

        // ✅关键：joinRoom 必须快返回 —— 这里不做任何“可能耗时秒级”的同步远程/DB统计
        BigDecimal poolBalance = (lastPoolBalance == null ? BigDecimal.ZERO : lastPoolBalance);
        String serverTime = (!isBlank(lastServerTime) ? lastServerTime : nowStr());

        String periodNo = ensureCurrentPeriod(nowMs);
        long periodStartMs = getPeriodStartMs(nowMs);
        long periodEndMs = getPeriodEndMs(nowMs);

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

        resp.put("periodNo", periodNo);
        resp.putAll(buildPbxInfoByPeriod(poolBalance, periodNo, periodStartMs, periodEndMs, nowMs));

        // 个人下注数据（本地内存，轻量）
        resp.put("myElementBet", buildMyElementBet(userId));
        resp.put("myTotalBet", buildMyTotalBet(userId));

        // ✅1) 异步刷新奖池/榜单（不阻塞 joinRoom），刷新后推一波 updatePbxInfo
        pushExecutor.execute(() -> {
            try {
                JSONObject q = queryPoolFromManager(userId);
                BigDecimal pb = (q == null ? null : q.getBigDecimal("poolBalance"));
                String st = (q == null ? null : q.getString("serverTime"));
                if (pb != null)
                    lastPoolBalance = pb;
                if (!isBlank(st))
                    lastServerTime = st;
                pushPbxInfo(lastPoolBalance);
            } catch (Exception e) {
                log.warn("[PBX] async queryPoolFromManager in joinRoom error, uid=" + userId, e);
            }
        });

        // ✅2) 异步预热统一统计缓存（解决你记录页第一次打开 4~5 秒卡顿）
        pushExecutor.execute(() -> {
            try {
                Long uid = Long.valueOf(userId);
                JSONObject sum = battleRoyaleRecord2Service.buildUnifiedSummary(uid, true);
                if (sum == null)
                    sum = new JSONObject();
                recordCache.put(uid, new SummaryCache(System.currentTimeMillis(), sum));
            } catch (Exception e) {
                log.warn("[PBX] async buildUnifiedSummary prewarm error, uid=" + userId, e);
            }
        });

        // joinRoom 返回后，tick 每秒会继续推倒计时（不会再等 4s/5s 才出现）
        return resp;
    }

    @ServiceMethod(code = "103", description = "推箱子-下注/操作")
    public Object operate(BattleRoyaleSocketServer2 adminSocketServer, Command lotteryCommand, JSONObject data) {
        checkNull(data);
        String userId = data.getString("userId");
        if (isBlank(userId))
            throwExp("参数错误");

        BigDecimal chip = parseChip(data);
        Integer elementId = parseElementId(data);

        if (chip == null)
            throwExp("参数错误");
        if (!isAllowedChip(chip))
            throwExp("参数错误 chip not allowed, allowed=" + CHIPS);
        if (elementId == null)
            elementId = 1;
        if (elementId < 1 || elementId > ELEMENT_COUNT)
            throwExp("参数错误 elementId out of range: 1.." + ELEMENT_COUNT);

        long nowMs = System.currentTimeMillis();
        String periodNo = ensureCurrentPeriod(nowMs);

        // ✅关键：结算期禁止下注，提示必须准确（否则你会误以为下注成功但其实 success=false）
        if (nowMs >= currentPeriodEndMs) {
            JSONObject fail = new JSONObject();
            fail.put("success", false);
            fail.put("gameId", String.valueOf(PBX_GAME_ID));
            fail.put("userId", userId);
            fail.put("periodNo", periodNo);
            fail.put("elementId", elementId);
            fail.put("chip", chip.stripTrailingZeros().toPlainString());
            fail.put("betAmount", chip.stripTrailingZeros().toPlainString());
            fail.put("message", "正在结算，请稍后再下注");
            fail.put("gameSetting", PBX_GAME_SETTING);
            return fail;
        }

        // 倒计时<=2秒拒绝下注，避免跨期回调/丢单
        long remainSec = ((currentPeriodEndMs - nowMs + 999L) / 1000L);
        if (remainSec <= 2) {
            JSONObject fail = new JSONObject();
            fail.put("success", false);
            fail.put("gameId", String.valueOf(PBX_GAME_ID));
            fail.put("userId", userId);
            fail.put("periodNo", periodNo);
            fail.put("elementId", elementId);
            fail.put("chip", chip.stripTrailingZeros().toPlainString());
            fail.put("betAmount", chip.stripTrailingZeros().toPlainString());
            fail.put("message", "本期即将截止，请下期再下注");
            fail.put("gameSetting", PBX_GAME_SETTING);
            return fail;
        }

        // 每次下注必须唯一（避免 Manager 幂等吞单）
        final String orderNoForAck = "PBX-" + periodNo + "-" + userId + "-" + elementId + "-" + nowMs;

        // 更新用户状态为操作中
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
        betReq.put("orderNo", orderNoForAck);

        Integer finalElementId = elementId;

        // 标记该期有待回调下注
        incPending(periodNo);

        requsetMangerService2.requestPbxBet(betReq, new Listener() {
            @Override
            public void handle(BaseClientSocket socket, Command command) {
                try {
                    if (command == null) {
                        handleBetError("pbxBet command is null");
                        return;
                    }
                    if (!command.isSuccess()) {
                        String msg = command.getMessage();
                        handleBetError(isBlank(msg) ? "pbxBet failed (manager error)" : msg);
                        return;
                    }

                    JSONObject resp = (JSONObject) command.getData();
                    if (resp == null) {
                        handleBetError("pbxBet response data is null");
                        return;
                    }

                    resp.put("orderNo", orderNoForAck);

                    boolean success = resp.getBooleanValue("success");
                    String msg = resp.getString("message");
                    BigDecimal betAmount = resp.getBigDecimal("betAmount"); // 必须 >0 才算真下注
                    if (betAmount == null)
                        betAmount = BigDecimal.ZERO;

                    // success=true 但 "order processed" / betAmount<=0 一律当失败
                    if (!success || betAmount.compareTo(BigDecimal.ZERO) <= 0
                            || (msg != null && msg.toLowerCase().contains("order processed"))) {

                        pushBetFailed(userId, orderNoForAck, periodNo, finalElementId, chip,
                                isBlank(msg) ? "pbxBet ignored/order processed" : msg);

                        JSONObject r = new JSONObject();
                        r.put("success", false);
                        r.put("orderNo", orderNoForAck);
                        r.put("message", isBlank(msg) ? "pbxBet ignored/order processed" : msg);
                        betRespRef.set(r);
                        return;
                    }

                    BigDecimal balance = resp.getBigDecimal("balance");
                    BigDecimal poolBalance = resp.getBigDecimal("poolBalance");
                    BigDecimal fee = resp.getBigDecimal("fee");
                    BigDecimal feeRate = resp.getBigDecimal("feeRate");

                    if (poolBalance != null)
                        lastPoolBalance = poolBalance;

                    // 只有真下注成功，才本地归集 & 落库
                    recordBet(periodNo, userId, finalElementId, chip, orderNoForAck);

                    // 每日任务推进：真人用户下注成功，推进 gameId=12（开开乐）
                    if (!BOT_USER.containsKey(userId)) {
                        try {
                            dailyTaskProgressService.pushDailyTaskByGameId(Long.parseLong(userId), 12);
                        } catch (Exception dailyEx) {
                            log.error("[DailyTask] 推进每日任务失败 uid=" + userId, dailyEx);
                        }
                    }

                    JSONObject pushStatus = buildPbxStatusPush(
                            userId, 2, true, orderNoForAck, periodNo, finalElementId, chip,
                            balance, poolBalance, fee, feeRate);

                    // ✅关键修复：不要用 putAll(sum) 直接覆盖（避免把 total/totalGain 等字段覆盖成 null）
                    try {
                        JSONObject sum = battleRoyaleRecord2Service.buildUnifiedSummary(Long.valueOf(userId), false);
                        if (sum != null) {
                            JSONObject mp = new JSONObject();
                            mp.put(userId, sum);
                            pushStatus.put("userRecordSummaryMap", mp);

                            // 只补充 summary，不覆盖 pushStatus 已有的关键字段
                            for (String k : sum.keySet()) {
                                if (!pushStatus.containsKey(k)) {
                                    pushStatus.put(k, sum.get(k));
                                }
                            }
                        }
                    } catch (Exception ignore) {
                    }

                    Push.push(PushCode.updatePbxStatus, null, pushStatus);
                    pushPbxInfo(lastPoolBalance);

                    betRespRef.set(resp);

                } catch (Exception e) {
                    log.error("[PBX] pbxBet callback exception", e);
                    handleBetError("pbxBet callback exception");
                } finally {
                    decPending(periodNo);
                    betLatch.countDown();
                }
            }

            private void handleBetError(String msg) {
                JSONObject r = new JSONObject();
                r.put("success", false);
                r.put("orderNo", orderNoForAck);
                r.put("message", msg);

                pushBetFailed(userId, orderNoForAck, periodNo, finalElementId, chip, msg);
                betRespRef.set(r);
            }
        });

        boolean awaited;
        try {
            awaited = betLatch.await(2000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            awaited = false;
        }

        JSONObject resp = betRespRef.get();
        if (!awaited) {
            JSONObject processing = new JSONObject();
            processing.put("success", true);
            processing.put("processing", true);
            processing.put("message", "下注处理中，请稍候");
            processing.put("gameId", String.valueOf(PBX_GAME_ID));
            processing.put("userId", userId);
            processing.put("periodNo", periodNo);
            processing.put("elementId", elementId);
            processing.put("chip", chip.stripTrailingZeros().toPlainString());
            processing.put("betAmount", chip.stripTrailingZeros().toPlainString());
            processing.put("orderNo", orderNoForAck);
            processing.put("gameSetting", PBX_GAME_SETTING);
            return processing;
        }

        if (!resp.getBooleanValue("success")) {
            if (!resp.containsKey("gameId"))
                resp.put("gameId", String.valueOf(PBX_GAME_ID));
            if (!resp.containsKey("gameSetting"))
                resp.put("gameSetting", PBX_GAME_SETTING);
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
        ack.put("orderNo", orderNoForAck);
        return ack;
    }

    private void rollbackBet(String periodNo, String userId, Integer elementId, BigDecimal chip) {
        if (chip == null || elementId == null)
            return;
        if (isBlank(periodNo) || isBlank(userId))
            return;

        // 只回滚当前期内存（失败回调通常发生在当前期）
        String cur = currentPeriodNo;
        if (cur == null)
            cur = ensureCurrentPeriod(System.currentTimeMillis());
        if (!periodNo.equals(cur))
            return;

        // element total
        periodElementTotalBet.compute(elementId, (k, v) -> {
            BigDecimal nv = (v == null ? BigDecimal.ZERO : v).subtract(chip);
            return nv.compareTo(BigDecimal.ZERO) <= 0 ? null : nv;
        });

        // user element
        Map<Integer, BigDecimal> um = periodUserElementBet.get(userId);
        if (um != null) {
            BigDecimal v = um.getOrDefault(elementId, BigDecimal.ZERO).subtract(chip);
            if (v.compareTo(BigDecimal.ZERO) <= 0)
                um.remove(elementId);
            else
                um.put(elementId, v);
            if (um.isEmpty())
                periodUserElementBet.remove(userId);
        }

        // user total
        periodUserTotalBet.compute(userId, (k, v) -> {
            BigDecimal nv = (v == null ? BigDecimal.ZERO : v).subtract(chip);
            return nv.compareTo(BigDecimal.ZERO) <= 0 ? null : nv;
        });
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

        if (poolBalance != null)
            lastPoolBalance = poolBalance;
        if (!isBlank(serverTime))
            lastServerTime = serverTime;

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
        if (payouts == null)
            payouts = new JSONArray();

        JSONArray winList = new JSONArray();
        for (int i = 0; i < payouts.size(); i++) {
            JSONObject p = payouts.getJSONObject(i);
            if (p == null)
                continue;
            String uid = p.getString("userId");
            if (isBlank(uid))
                continue;
            BigDecimal gross = p.getBigDecimal("gross");
            if (gross == null)
                gross = p.getBigDecimal("returnAmount");
            if (gross == null)
                continue;

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

        if (!ok)
            throwExp("未知异常");

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
        if (gameId == null)
            gameId = PBX_GAME_ID;
        if (gameId != PBX_GAME_ID)
            throwExp("gameId invalid");

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
        Long uid = data.getLong("userId");

        // ✅把 TTL 拉长，避免用户连点重复触发大 SQL
        final long TTL_MS = 10_000L;

        long now = System.currentTimeMillis();
        SummaryCache c = recordCache.get(uid);
        if (c != null && (now - c.ts) <= TTL_MS && c.data != null) {
            return c.data;
        }

        JSONObject sum = battleRoyaleRecord2Service.buildUnifiedSummary(uid, true);
        if (sum == null)
            sum = new JSONObject();

        recordCache.put(uid, new SummaryCache(now, sum));
        return sum;
    }

    /**
     * 离开房间 清除在线状态，并同步推送
     */
    @ServiceMethod(code = "104", description = "推箱子-离开房间")
    public Object leaveRoom(BattleRoyaleSocketServer2 adminSocketServer, Command lotteryCommand, JSONObject data) {
        checkNull(data);
        String userId = data.getString("userId");
        if (isBlank(userId))
            throwExp("参数错误");

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
     * 周期状态机核心：按 (TIME_SEC + SETTLE_SEC) 为一个周期。
     * - 下注窗口：[start, start+TIME_SEC)
     * - 结算窗口：[start+TIME_SEC, start+TIME_SEC+SETTLE_SEC)
     * - 到 cycleEnd 才切下一期
     *
     * ✅策略：
     * 1) 跨期时若上一期未结算，短暂 hold（<= settleSec+2s），防止前端丢旧期 status=3。
     * 2) hold 期间必须确保已触发兜底结算（可重试一次），不能把自己锁死。
     * 3) 超过 hold 窗口仍未结算：强制换期，后台继续结算补偿，避免全体卡死。
     */
    private String ensureCurrentPeriod(long nowMs) {
        ensurePeriodSchedulerAlive();

        long betMs = (TIME_SEC <= 0) ? 20000L : (TIME_SEC * 1000L);
        long settleMs = (SETTLE_SEC <= 0) ? 0L : (SETTLE_SEC * 1000L);
        long cycleMs = betMs + settleMs;

        long bucket = nowMs / cycleMs;
        String periodNo = "PBX_" + bucket;

        ensureWeek(nowMs);

        PeriodSnapshot rolloverNeedSettleSnapshot = null;

        // ✅只 hold 一个结算窗口（避免你现在这种“卡死几分钟”）
        // HOLD 至少给 30s：兼容 manager 抖动、IO 抖动、偶发排队
        final long MAX_HOLD_MS = Math.max(15_000L, settleMs + 3000L);

        synchronized (PERIOD_LOCK) {
            long cycleStartMs = bucket * cycleMs;
            long betEndMs = cycleStartMs + betMs;
            long cycleEndMs = cycleStartMs + cycleMs;

            // 初始化 or 同周期
            if (currentPeriodNo == null || bucket == currentPeriodBucket) {
                currentPeriodBucket = bucket;
                currentPeriodNo = periodNo;
                currentPeriodStartMs = cycleStartMs;
                currentPeriodEndMs = betEndMs;
                currentCycleEndMs = cycleEndMs;
                return currentPeriodNo;
            }

            // bucket 已变化：我们正跨期
            String prevPeriodNo = currentPeriodNo;
            long prevStartMs = currentPeriodStartMs;
            long prevEndMs = currentPeriodEndMs;
            long prevCycleEndMs = currentCycleEndMs;

            boolean prevSettled = (!isBlank(prevPeriodNo)) && settledPeriodNoSet.contains(prevPeriodNo);
            boolean prevAlreadySnapshot = (!isBlank(prevPeriodNo)) && settlingSnapshotMap.containsKey(prevPeriodNo);

            if (!isBlank(prevPeriodNo) && !prevSettled) {

                // ✅确保有兜底快照（只建一次）
                if (!prevAlreadySnapshot) {
                    rolloverNeedSettleSnapshot = new PeriodSnapshot(
                            prevPeriodNo,
                            prevStartMs,
                            prevEndMs,
                            new HashMap<>(periodElementTotalBet),
                            new HashMap<>(periodUserElementBet),
                            new HashMap<>(periodUserTotalBet));
                    settlingSnapshotMap.put(prevPeriodNo, rolloverNeedSettleSnapshot);
                }

                long overMs = nowMs - prevCycleEndMs;
                if (overMs <= MAX_HOLD_MS) {
                    // ✅短暂 hold，保持上一期不换
                    // ⚠️不要把 currentPeriodSettleTriggered=true 锁死整个系统
                    // 这里仅返回上一期，让前端持续显示“等待结算/结算倒计时”
                    return prevPeriodNo;
                }

                // 超过短暂 hold：强制换期（避免全体卡死）
                log.warn("[PBX] prev period not settled but exceed HOLD window, force rollover. prevPeriodNo="
                        + prevPeriodNo + ", overMs=" + overMs + ", nowMs=" + nowMs + ", prevCycleEndMs="
                        + prevCycleEndMs);
            }

            // 进入新一期
            currentPeriodBucket = bucket;
            currentPeriodNo = periodNo;
            currentPeriodStartMs = cycleStartMs;
            currentPeriodEndMs = betEndMs;
            currentCycleEndMs = cycleEndMs;

            // 清理本期内存下注（新一期从 0 开始）
            periodElementTotalBet.clear();
            periodUserElementBet.clear();
            periodUserTotalBet.clear();
            periodBotElementTotalBet.clear();

            currentPeriodSettleTriggered = false;

            openedPeriodNo = null;
            openedResultElements = null;
            openedResultType = null;
            openedForcedNoWin = 0;

            // 清理旧期号缓存，防止 settledPeriodNoSet 无限增长导致内存泄漏
            try {
                if (settledPeriodNoSet.size() > 200) {
                    settledPeriodNoSet.clear();
                    log.info("[PBX] settledPeriodNoSet cleared (size exceeded 200)");
                }
            } catch (Exception ignore) {
            }
        }

        // 锁外异步兜底结算（若需要）
        if (rolloverNeedSettleSnapshot != null) {
            try {
                settlePeriodAsync(rolloverNeedSettleSnapshot);
            } catch (Exception e) {
                log.error("[PBX] rollover settlePeriodAsync error, periodNo=" + rolloverNeedSettleSnapshot.periodNo, e);
                try {
                    settlingSnapshotMap.remove(rolloverNeedSettleSnapshot.periodNo);
                } catch (Exception ignore) {
                }
            }
        }

        return currentPeriodNo;
    }

    /**
     * 异步结算入口，避免阻塞 tickPeriod（倒计时线程）
     */
    private void settlePeriodAsync(PeriodSnapshot snapshot) {
        if (snapshot == null || isBlank(snapshot.periodNo))
            return;

        try {
            settleExecutor.execute(() -> {
                try {
                    settlePeriod(snapshot);
                } catch (Throwable t) {
                    log.error("[PBX] settleExecutor error, periodNo=" + snapshot.periodNo, t);
                } finally {
                    // ✅结算结束移除
                    settlingSnapshotMap.remove(snapshot.periodNo);
                    pendingBetMap.remove(snapshot.periodNo);
                }
            });
        } catch (Exception e) {
            log.error("[PBX] settlePeriodAsync schedule error, periodNo=" + snapshot.periodNo, e);
            settlingSnapshotMap.remove(snapshot.periodNo);
        }
    }

    /**
     * 执行结算逻辑：查询奖池 -> 控盘选择 -> 强制输赢判断 -> 调用 Manager 结算 -> 落库 -> 广播
     */
    /**
     * 执行结算逻辑：查询奖池 -> 控盘选择 -> 强制输赢判断 -> 调用 Manager 结算 -> 落库 -> 广播
     */
    private void settlePeriod(PeriodSnapshot snapshot) {
        try {
            if (snapshot == null || isBlank(snapshot.periodNo))
                return;

            // ✅关键修复：等待下注回调归集（避免“回调晚到 -> 本期判空 -> 不结算/不弹框”）
            // 这里用 pending 驱动等待：只要还有待回调下注，就不会提前判空。
            boolean hasBets = waitBetsArrive(snapshot, 3000L);

            // 2) 去重：同一期只允许结算一次
            if (!settledPeriodNoSet.add(snapshot.periodNo)) {
                log.warn("[PBX] settlePeriod duplicate ignored, periodNo=" + snapshot.periodNo);
                return;
            }

            // 3) 稳定视图（防并发写）
            final Map<String, BigDecimal> userTotalBetLocal = new HashMap<>(snapshot.userTotalBet);

            final Map<String, Map<Integer, BigDecimal>> userElementBetLocal = new HashMap<>();
            for (Map.Entry<String, ConcurrentHashMap<Integer, BigDecimal>> e : snapshot.userElementBet.entrySet()) {
                userElementBetLocal.put(e.getKey(), new HashMap<>(e.getValue()));
            }

            final Map<Integer, BigDecimal> elementTotalBetLocal = new HashMap<>(snapshot.elementTotalBet);

            // 4) 查询奖池余额（主服）
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

            try {
                latch.await(2, TimeUnit.SECONDS);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }

            BigDecimal poolBalance = (lastPoolBalance == null) ? BigDecimal.ZERO : lastPoolBalance;
            String serverTime = nowStr();

            JSONObject q = queryRespRef.get();
            if (q != null && q.getBooleanValue("success")) {
                BigDecimal pb = q.getBigDecimal("poolBalance");
                if (pb != null) {
                    poolBalance = pb;
                    lastPoolBalance = pb;
                }
                String st = q.getString("serverTime");
                if (!isBlank(st))
                    serverTime = st;
            }

            // 5) 控盘选结果（稳定视图）
            OutcomePick pick = pickOutcome(elementTotalBetLocal, poolBalance);
            JSONArray resultElements = pick.resultElements;
            String resultType = pick.resultType;
            int forcedNoWinFlag = pick.forceLose ? 1 : 0;

            // 6) 写历史
            pushResultHistory(snapshot.periodNo, resultElements, resultType, forcedNoWinFlag);

            if (!hasBets) {
                log.warn("[PBX] settle: no bets, broadcast only. periodNo=" + snapshot.periodNo);
                cacheOpenedResult(snapshot, resultElements, resultType, forcedNoWinFlag);
                broadcastSettleInfo(snapshot, resultElements, resultType, poolBalance, forcedNoWinFlag);
                return;
            }
            // 7) 写回 snapshot（保证 handleXxx 读到稳定数据）
            PeriodSnapshot stableSnap = snapshot;

            stableSnap.userTotalBet.clear();
            stableSnap.userTotalBet.putAll(userTotalBetLocal);

            stableSnap.userElementBet.clear();
            for (Map.Entry<String, Map<Integer, BigDecimal>> e : userElementBetLocal.entrySet()) {
                ConcurrentHashMap<Integer, BigDecimal> m = new ConcurrentHashMap<>();
                if (e.getValue() != null) {
                    for (Map.Entry<Integer, BigDecimal> ee : e.getValue().entrySet()) {
                        m.put(ee.getKey(), ee.getValue());
                    }
                }
                stableSnap.userElementBet.put(e.getKey(), m);
            }

            stableSnap.elementTotalBet.clear();
            stableSnap.elementTotalBet.putAll(elementTotalBetLocal);

            // 8) 强制全输
            if (pick.forceLose) {
                handleForceLose(stableSnap, resultElements, resultType, poolBalance, serverTime);
                return;
            }

            // 9) 正常派奖：算赢家
            JSONArray winList = new JSONArray();
            for (Map.Entry<String, Map<Integer, BigDecimal>> e : userElementBetLocal.entrySet()) {
                String userId = e.getKey();
                BigDecimal gross = calcUserGross(e.getValue(), pick);
                if (gross != null && gross.compareTo(BigDecimal.ZERO) > 0) {
                    JSONObject one = new JSONObject();
                    one.put("userId", userId);
                    one.put("returnAmount", gross.setScale(2, RoundingMode.HALF_UP).toPlainString());
                    winList.add(one);
                }
            }

            // 10) 无人中奖
            if (winList.isEmpty()) {
                handleNormalNoWin(stableSnap, resultElements, resultType, poolBalance, serverTime);
                return;
            }

            // 11) 调主服派奖
            JSONObject settleReq = new JSONObject();
            settleReq.put("gameId", String.valueOf(PBX_GAME_ID));
            settleReq.put("periodNo", stableSnap.periodNo);
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

            try {
                settleLatch.await(3, TimeUnit.SECONDS);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }

            JSONObject settleResp = settleRespRef.get();

            // 12) 兜底
            if (settleResp == null || !settleResp.getBooleanValue("success")) {
                handleSettleError(stableSnap, resultElements, resultType, poolBalance, serverTime, settleResp);
                return;
            }

            // 13) 成功
            handleSettleSuccess(stableSnap, resultElements, resultType, poolBalance, serverTime, settleResp);

        } catch (Exception e) {
            log.error("[PBX] auto settle exception, periodNo=" + (snapshot == null ? "null" : snapshot.periodNo), e);
        } finally {
            // 结算结束移除 snapshot
            try {
                if (snapshot != null && !isBlank(snapshot.periodNo)) {
                    settlingSnapshotMap.remove(snapshot.periodNo);
                    // ✅清理 pending
                    pendingBetMap.remove(snapshot.periodNo);
                }
            } catch (Exception ignore) {
            }
        }
    }

    /**
     * ✅关键修复：等待下注回调归集
     * 规则：
     * - 已有下注：立即返回 true
     * - pending<=0：说明没有待回调，没必要等，返回当前是否有下注
     * - pending>0：最多等待 maxWaitMs（覆盖线程抖动/WS 抖动）
     */
    private boolean waitBetsArrive(PeriodSnapshot snapshot, long maxWaitMs) {
        if (snapshot == null || isBlank(snapshot.periodNo))
            return false;

        long waitStart = System.currentTimeMillis();
        long maxWait = Math.max(0L, maxWaitMs);

        while (true) {
            boolean hasBet = snapshot.userTotalBet != null && !snapshot.userTotalBet.isEmpty();
            if (hasBet)
                return true;

            int pending = getPending(snapshot.periodNo);
            if (pending <= 0) {
                // 没有待回调下注了，没必要继续等
                return snapshot.userTotalBet != null && !snapshot.userTotalBet.isEmpty();
            }

            if (System.currentTimeMillis() - waitStart >= maxWait) {
                // 超时：返回当前是否有下注（可能仍为空）
                return snapshot.userTotalBet != null && !snapshot.userTotalBet.isEmpty();
            }

            try {
                Thread.sleep(120L);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                return snapshot.userTotalBet != null && !snapshot.userTotalBet.isEmpty();
            }
        }
    }

    /**
     * 处理强制庄家赢的情况（全员输：profit = -totalBet）
     * ✅修复：大循环推送放到 pushExecutor，避免拖死结算线程触发 HOLD 超时 rollover
     * ✅修复：给下注用户定向推 updatePbxInfo(status=3,totalGain)，否则前端弹窗 totalGain=undefined
     */
    private void handleForceLose(PeriodSnapshot snapshot, JSONArray resultElements, String resultType,
            BigDecimal poolBalance, String serverTime) {
        cacheOpenedResult(snapshot, resultElements, resultType, 1);

        try {
            batchUpdateRecordProfit(snapshot, resultElements, resultType, null);
        } catch (Exception e) {
            log.error("[PBX] forceLose batchUpdateRecordProfit error, periodNo=" + snapshot.periodNo, e);
        }

        final BigDecimal finalPool = (poolBalance == null ? BigDecimal.ZERO : poolBalance);

        pushExecutor.execute(() -> {
            for (String uid : snapshot.userTotalBet.keySet()) {
                try {
                    updateUserStatusToIdle(uid);

                    if (isBotUser(uid))
                        continue;

                    BigDecimal totalBet = snapshot.userTotalBet.get(uid);
                    if (totalBet == null)
                        totalBet = BigDecimal.ZERO;

                    BigDecimal profit = BigDecimal.ZERO.subtract(totalBet).setScale(2, RoundingMode.HALF_UP);
                    String profitStr = profit.stripTrailingZeros().toPlainString();

                    JSONObject pushStatus = buildAutoSettleStatusPush(
                            uid, snapshot.periodNo, resultElements, resultType,
                            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                            null, finalPool, serverTime,
                            JSONArray.of("pool not enough, force lose"), 1,
                            totalBet);
                    appendRecordSummary(uid, pushStatus);
                    Push.push(PushCode.updatePbxStatus, null, pushStatus);

                    pushUserStatus3Info(uid, snapshot, resultElements, resultType, finalPool, 1, profitStr, totalBet);

                } catch (Exception e) {
                    log.error("[PBX] handleForceLose push error, uid=" + uid + ", periodNo=" + snapshot.periodNo, e);
                }
            }
        });
    }

    /**
     * 处理正常情况下的无人中奖（全员输：profit = -totalBet）
     * ✅修复：大循环推送放到 pushExecutor
     * ✅修复：定向 updatePbxInfo(status=3,totalGain)
     */
    private void handleNormalNoWin(PeriodSnapshot snapshot, JSONArray resultElements, String resultType,
            BigDecimal poolBalance, String serverTime) {
        cacheOpenedResult(snapshot, resultElements, resultType, 0);

        try {
            batchUpdateRecordProfit(snapshot, resultElements, resultType, null);
        } catch (Exception e) {
            log.error("[PBX] normalNoWin batchUpdateRecordProfit error, periodNo=" + snapshot.periodNo, e);
        }

        final BigDecimal finalPool = (poolBalance == null ? BigDecimal.ZERO : poolBalance);

        pushExecutor.execute(() -> {
            for (String uid : snapshot.userTotalBet.keySet()) {
                try {
                    updateUserStatusToIdle(uid);

                    if (isBotUser(uid))
                        continue;

                    BigDecimal totalBet = snapshot.userTotalBet.get(uid);
                    if (totalBet == null)
                        totalBet = BigDecimal.ZERO;

                    BigDecimal profit = BigDecimal.ZERO.subtract(totalBet).setScale(2, RoundingMode.HALF_UP);
                    String profitStr = profit.stripTrailingZeros().toPlainString();

                    JSONObject pushStatus = buildAutoSettleStatusPush(
                            uid, snapshot.periodNo, resultElements, resultType,
                            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                            null, finalPool, serverTime,
                            null, 0,
                            totalBet);
                    appendRecordSummary(uid, pushStatus);
                    Push.push(PushCode.updatePbxStatus, null, pushStatus);

                    pushUserStatus3Info(uid, snapshot, resultElements, resultType, finalPool, 0, profitStr, totalBet);

                } catch (Exception e) {
                    log.error("[PBX] handleNormalNoWin push error, uid=" + uid + ", periodNo=" + snapshot.periodNo, e);
                }
            }
        });
    }

    /**
     * 处理结算请求异常（按全员输落库，保证记录/总获得不悬空）
     * ✅修复：大循环推送放到 pushExecutor
     * ✅修复：定向 updatePbxInfo(status=3,totalGain)
     */
    private void handleSettleError(PeriodSnapshot snapshot, JSONArray resultElements, String resultType,
            BigDecimal poolBalance, String serverTime, JSONObject settleResp) {
        String errMsg = (settleResp == null) ? "null" : settleResp.getString("message");
        log.warn("[PBX] handleSettleError: periodNo=" + snapshot.periodNo
                + ", poolBalance=" + poolBalance + ", result=" + resultElements
                + ", msg=" + errMsg);
        cacheOpenedResult(snapshot, resultElements, resultType, 0);

        try {
            batchUpdateRecordProfit(snapshot, resultElements, resultType, null);
        } catch (Exception e) {
            log.error("[PBX] settleError batchUpdateRecordProfit error, periodNo=" + snapshot.periodNo, e);
        }

        final BigDecimal finalPool = (poolBalance == null ? BigDecimal.ZERO : poolBalance);
        final String msg = (settleResp == null) ? "pbxSettle response null" : settleResp.getString("message");

        pushExecutor.execute(() -> {
            for (String uid : snapshot.userTotalBet.keySet()) {
                try {
                    updateUserStatusToIdle(uid);

                    if (isBotUser(uid))
                        continue;

                    BigDecimal totalBet = snapshot.userTotalBet.get(uid);
                    if (totalBet == null)
                        totalBet = BigDecimal.ZERO;

                    BigDecimal profit = BigDecimal.ZERO.subtract(totalBet).setScale(2, RoundingMode.HALF_UP);
                    String profitStr = profit.stripTrailingZeros().toPlainString();

                    JSONObject pushStatus = buildAutoSettleStatusPush(
                            uid, snapshot.periodNo, resultElements, resultType,
                            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                            null, finalPool, serverTime,
                            JSONArray.of(msg), 0,
                            totalBet);
                    appendRecordSummary(uid, pushStatus);
                    Push.push(PushCode.updatePbxStatus, null, pushStatus);

                    pushUserStatus3Info(uid, snapshot, resultElements, resultType, finalPool, 0, profitStr, totalBet);

                } catch (Exception e) {
                    log.error("[PBX] handleSettleError push error, uid=" + uid + ", periodNo=" + snapshot.periodNo, e);
                }
            }
        });
    }

    /**
     * 处理结算成功后的数据更新与广播（赢家/输家都落库 profit）
     */
    /**
     * 处理结算成功后的数据更新与广播（赢家/输家都落库 profit）
     * ✅关键修复：
     * 1) 先 broadcastSettleInfo（全服结果）
     * 2) DB 更新走 recordExecutor（已有）
     * 3) 对每个下注用户的 updatePbxStatus + 定向 updatePbxInfo(status=3,totalGain) 走
     * pushExecutor，
     * 避免 settleExecutor 被大量 push 拖死，从而触发 HOLD 超时 rollover（有时不开）
     */
    private void handleSettleSuccess(PeriodSnapshot snapshot,
            JSONArray resultElements,
            String resultType,
            BigDecimal poolBalance,
            String serverTime,
            JSONObject settleResp) {

        JSONArray userList = settleResp.getJSONArray("userList");
        BigDecimal newPoolBalance = settleResp.getBigDecimal("poolBalance");
        log.warn("[PBX] handleSettleSuccess: periodNo=" + snapshot.periodNo
                + ", result=" + resultElements + ", resultType=" + resultType
                + ", oldPool=" + poolBalance + ", newPool=" + newPoolBalance
                + ", winnerCount=" + (userList == null ? 0 : userList.size()));
        if (newPoolBalance != null) {
            lastPoolBalance = newPoolBalance;
        } else {
            newPoolBalance = poolBalance;
        }

        cacheOpenedResult(snapshot, resultElements, resultType, 0);

        Map<String, JSONObject> userInfoMap = new HashMap<>();
        if (userList != null) {
            for (int i = 0; i < userList.size(); i++) {
                JSONObject u = userList.getJSONObject(i);
                if (u == null)
                    continue;
                String uid = u.getString("userId");
                if (isBlank(uid))
                    continue;
                userInfoMap.put(uid, u);
            }
        }

        try {
            batchUpdateRecordProfit(snapshot, resultElements, resultType, userInfoMap);
        } catch (Exception e) {
            log.error("[PBX] batchUpdateRecordProfit error, periodNo=" + snapshot.periodNo, e);
        }

        final BigDecimal finalPool = newPoolBalance;
        pushExecutor.execute(() -> {
            for (String uid : snapshot.userTotalBet.keySet()) {
                updateUserStatusToIdle(uid);

                if (isBotUser(uid))
                    continue;

                BigDecimal totalBet = snapshot.userTotalBet.get(uid);
                if (totalBet == null)
                    totalBet = BigDecimal.ZERO;

                JSONObject u = userInfoMap.get(uid);

                BigDecimal gross = (u == null) ? BigDecimal.ZERO : u.getBigDecimal("returnAmount");
                BigDecimal fee = (u == null) ? BigDecimal.ZERO : u.getBigDecimal("fee");
                BigDecimal net = (u == null) ? BigDecimal.ZERO : u.getBigDecimal("net");
                BigDecimal balance = (u == null) ? null : u.getBigDecimal("balance");

                if (gross == null)
                    gross = BigDecimal.ZERO;
                if (fee == null)
                    fee = BigDecimal.ZERO;
                if (net == null)
                    net = BigDecimal.ZERO;

                BigDecimal profit = net.subtract(totalBet).setScale(2, RoundingMode.HALF_UP);
                String profitStr = profit.stripTrailingZeros().toPlainString();

                JSONObject pushStatus = buildAutoSettleStatusPush(
                        uid, snapshot.periodNo, resultElements, resultType,
                        gross, fee, net, balance, finalPool, serverTime,
                        null, 0, totalBet);
                appendRecordSummary(uid, pushStatus);
                Push.push(PushCode.updatePbxStatus, null, pushStatus);

                pushUserStatus3Info(uid, snapshot, resultElements, resultType, finalPool, 0, profitStr, totalBet);
            }
        });
    }

    /**
     * ✅统一落库：把本局所有人的 profit 写入 battle_royale_record2
     * 规则：
     * - winner: profit = net - betAmount
     * - loser : profit = 0 - betAmount
     *
     * 注意：BattleRoyaleRecord2Mapper.xml 的 batchUpdateRecord 把 profit =
     * #{item.winAmount}
     * 所以这里 row.winAmount 必须放 profit（不是 gross）
     */
    private void batchUpdateRecordProfit(PeriodSnapshot snapshot,
            JSONArray resultElements,
            String resultType,
            Map<String, JSONObject> userInfoMap) {
        if (snapshot == null || snapshot.userTotalBet == null || snapshot.userTotalBet.isEmpty())
            return;

        final JSONArray updList = new JSONArray();
        final String lotteryResult = joinResultElements(resultElements);

        for (String uid : snapshot.userTotalBet.keySet()) {
            BigDecimal betAmount = snapshot.userTotalBet.get(uid);
            if (betAmount == null)
                betAmount = BigDecimal.ZERO;

            BigDecimal net = BigDecimal.ZERO;
            if (userInfoMap != null) {
                JSONObject u = userInfoMap.get(uid);
                if (u != null) {
                    BigDecimal n = u.getBigDecimal("net");
                    if (n != null)
                        net = n;
                }
            }

            BigDecimal profit = net.subtract(betAmount).setScale(2, RoundingMode.HALF_UP);

            JSONObject row = new JSONObject();
            row.put("periodNo", snapshot.periodNo);
            row.put("userId", uid);
            row.put("winAmount", profit);
            row.put("lotteryResult", lotteryResult);
            row.put("isWin", profit.compareTo(BigDecimal.ZERO) > 0 ? 1 : 0);
            updList.add(row);
        }

        if (updList.isEmpty())
            return;

        recordExecutor.execute(() -> {
            try {
                // 给下注回调晚到的插入一点缓冲，降低错过更新概率
                try {
                    Thread.sleep(200L);
                } catch (Exception ignore) {
                }
                battleRoyaleRecord2Service.batchUpdateRecordByPeriodUser(updList);
            } catch (Exception e) {
                log.error("[PBX] batchUpdateRecordByPeriodUser error. periodNo=" + snapshot.periodNo + ", size="
                        + updList.size(), e);
            }
        });
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
    /**
     * 为推送消息附加用户汇总记录
     * ✅修复：合并 summary 时必须保护 total（前端弹窗字段），否则会被 putAll 覆盖成 null/丢失 -> undefined
     */
    private void appendRecordSummary(String uid, JSONObject pushStatus) {
        try {
            JSONObject sum = battleRoyaleRecord2Service.buildUnifiedSummary(Long.valueOf(uid), false);
            if (sum == null)
                return;

            // userRecordSummaryMap 保留
            JSONObject mp = new JSONObject();
            mp.put(uid, sum);
            pushStatus.put("userRecordSummaryMap", mp);

            // ✅合并 summary 时，不允许覆盖本局结算字段
            Object curGain = pushStatus.get("totalGain");
            Object curInvest = pushStatus.get("totalInvest");
            Object curTotal = pushStatus.get("total"); // ✅新增：保护 total
            Object curGain2 = pushStatus.get("gain"); // 可选：也保护 gain

            // 先合并
            pushStatus.putAll(sum);

            // 再恢复
            if (curGain != null)
                pushStatus.put("totalGain", curGain);
            if (curInvest != null)
                pushStatus.put("totalInvest", curInvest);
            if (curTotal != null)
                pushStatus.put("total", curTotal); // ✅关键
            if (curGain2 != null)
                pushStatus.put("gain", curGain2);

            // 同理：userSettleInfo 里的 totalGain/total 也必须保留
            JSONObject si = pushStatus.getJSONObject("userSettleInfo");
            if (si != null) {
                if (si.get("totalGain") == null && curGain != null)
                    si.put("totalGain", curGain);
                if (si.get("total") == null && curTotal != null)
                    si.put("total", curTotal); // ✅关键
            }

        } catch (Exception ignore) {
        }
    }

    /**
     * 全服广播开奖结果
     */
    /**
     * 全服广播开奖结果（并缓存，防止 tick 覆盖）
     */
    private void cacheOpenedResult(PeriodSnapshot snapshot,
            JSONArray resultElements,
            String resultType,
            int forcedNoWin) {
        this.openedPeriodNo = snapshot.periodNo;
        this.openedResultElements = resultElements;
        this.openedResultType = resultType;
        this.openedForcedNoWin = forcedNoWin;
    }

    private void broadcastSettleInfo(PeriodSnapshot snapshot,
            JSONArray resultElements,
            String resultType,
            BigDecimal poolBalance,
            int forcedNoWin) {
        log.warn("[PBX] broadcastSettleInfo called. periodNo=" + snapshot.periodNo
                + ", resultElements=" + resultElements
                + ", resultType=" + resultType
                + ", forcedNoWin=" + forcedNoWin
                + ", poolBalance=" + poolBalance);

        long nowMs = System.currentTimeMillis();
        JSONObject infoPush = buildPbxInfoByPeriod(poolBalance, snapshot.periodNo, snapshot.startMs, snapshot.endMs,
                nowMs);

        infoPush.put("status", 3);
        infoPush.put("remainSeconds", 0);

        infoPush.put("resultElements", resultElements);
        infoPush.put("resultType", resultType);
        infoPush.put("forcedNoWin", forcedNoWin);

        Push.push(PushCode.updatePbxInfo, null, infoPush);
    }

    /**
     * 根据当前奖池选择最佳开奖结果
     *
     * @param elementTotalBet 各元素总下注额
     * @param poolBalance     当前奖池余额
     * @return 选定的开奖结果对象
     */
    /**
     * 根据当前奖池选择最佳开奖结果
     *
     * @param elementTotalBet 各元素总下注额
     * @param poolBalance     当前奖池余额
     * @return 选定的开奖结果对象
     */
    private OutcomePick pickOutcome(Map<Integer, BigDecimal> elementTotalBet, BigDecimal poolBalance) {

        Map<Integer, BigDecimal> t = (elementTotalBet == null) ? new HashMap<>() : elementTotalBet;
        List<OutcomePick> candidates = new ArrayList<>();
        List<OutcomePick> all = new ArrayList<>();

        BigDecimal available = (poolBalance == null ? BigDecimal.ZERO : poolBalance);

        // Triple (三同号)
        for (int e = 1; e <= ELEMENT_COUNT; e++) {
            JSONArray res = new JSONArray();
            res.add(e);
            res.add(e);
            res.add(e);

            BigDecimal gross = safe(t.get(e)).multiply(MULT_TRIPLE);
            BigDecimal net = calcNetForControl(gross);

            OutcomePick p = new OutcomePick(res, "TRIPLE", false, e, null, MULT_TRIPLE);
            p.net = net;

            all.add(p);
            if (net.compareTo(available) <= 0)
                candidates.add(p);
        }

        // Double (两同号)
        for (int e = 1; e <= ELEMENT_COUNT; e++) {
            for (int f = 1; f <= ELEMENT_COUNT; f++) {
                if (f == e)
                    continue;

                JSONArray res = new JSONArray();
                res.add(e);
                res.add(e);
                res.add(f);

                BigDecimal gross = safe(t.get(e)).multiply(MULT_DOUBLE);
                BigDecimal net = calcNetForControl(gross);

                OutcomePick p = new OutcomePick(res, "DOUBLE", false, e, null, MULT_DOUBLE);
                p.net = net;

                all.add(p);
                if (net.compareTo(available) <= 0)
                    candidates.add(p);
            }
        }

        // All Diff (三不同号)
        for (int a = 1; a <= ELEMENT_COUNT; a++) {
            for (int b = a + 1; b <= ELEMENT_COUNT; b++) {
                for (int c = b + 1; c <= ELEMENT_COUNT; c++) {
                    JSONArray res = new JSONArray();
                    res.add(a);
                    res.add(b);
                    res.add(c);

                    BigDecimal gross = safe(t.get(a)).add(safe(t.get(b))).add(safe(t.get(c))).multiply(MULT_ALL_DIFF);
                    BigDecimal net = calcNetForControl(gross);

                    Set<Integer> winSet = new HashSet<>();
                    winSet.add(a);
                    winSet.add(b);
                    winSet.add(c);

                    OutcomePick p = new OutcomePick(res, "ALL_DIFF", false, null, winSet, MULT_ALL_DIFF);
                    p.net = net;

                    all.add(p);
                    if (net.compareTo(available) <= 0)
                        candidates.add(p);
                }
            }
        }

        // 无候选：选“净支出最小”的（强控兜底）
        OutcomePick chosen;
        if (candidates.isEmpty()) {
            OutcomePick min = Collections.min(all, Comparator.comparing(o -> {
                BigDecimal gross = calcGrossForOutcome(t, o);
                return calcNetForControl(gross);
            }));
            chosen = new OutcomePick(min.resultElements, min.resultType, true, min.winElement, min.winElements,
                    min.multiplier);
            chosen.net = calcNetForControl(calcGrossForOutcome(t, min));
        } else {
            candidates.sort(Comparator.comparing(p -> p.net));
            int sz = candidates.size();

            if (sz <= 3) {
                chosen = candidates.get(ThreadLocalRandom.current().nextInt(sz));
            } else {
                int tier1 = Math.max(1, sz / 3);
                int tier2 = Math.max(tier1 + 1, sz * 2 / 3);

                double roll = ThreadLocalRandom.current().nextDouble();
                List<OutcomePick> pool;
                if (roll < 0.55) {
                    pool = candidates.subList(0, tier1);
                } else if (roll < 0.85) {
                    pool = candidates.subList(tier1, tier2);
                } else {
                    pool = candidates.subList(tier2, sz);
                }
                chosen = pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
            }
        }

        // ✅关键：展示顺序洗牌（DOUBLE/TRIPLE 不再永远“前两个一样、最后固定”）
        log.warn("[PBX] pickOutcome: available=" + available + ", candidates=" + candidates.size()
                + ", all=" + all.size() + ", chosen=" + chosen.resultType
                + ", chosenNet=" + chosen.net + ", forceLose=" + chosen.forceLose);

        shuffleFastjson2Array(chosen.resultElements);

        return chosen;
    }

    /**
     * 计算某开奖结果对应的平台总赔付额
     */
    private BigDecimal calcGrossForOutcome(Map<Integer, BigDecimal> elementTotalBet, OutcomePick pick) {
        if (pick == null)
            return BigDecimal.ZERO;
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
        if (pick == null)
            return BigDecimal.ZERO;
        if (userBetByElement == null || userBetByElement.isEmpty())
            return BigDecimal.ZERO;

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
        if (gross == null)
            return BigDecimal.ZERO;
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
    /**
     * 机器人 Tick 逻辑：随机下注
     */
    private void tickBot() {

        if (NEED_BOT <= 0)
            return;
        // 没真人在线不刷
        if (onlineUserState.isEmpty())
            return;
        if (BOT_USER.isEmpty())
            return;

        long nowMs = System.currentTimeMillis();
        String periodNo = ensureCurrentPeriod(nowMs);
        if (nowMs >= currentPeriodEndMs)
            return;

        int rate = ThreadLocalRandom.current().nextInt(100);
        if (rate >= NEED_BOT)
            return;

        String botUserId = getRandomBotUserId();
        if (botUserId == null)
            return;

        int elementId = ThreadLocalRandom.current().nextInt(1, ELEMENT_COUNT + 1);
        BigDecimal chip = getRandomBotChip();

        try {
            // ✅每次下注唯一（防 Manager 幂等吞单）
            String orderNoForAck = "PBX-" + periodNo + "-" + botUserId + "-" + elementId + "-" + nowMs;

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

            // ✅pending++
            incPending(periodNo);

            requsetMangerService2.requestPbxBet(betReq, new Listener() {
                @Override
                public void handle(BaseClientSocket socket, Command command) {
                    try {
                        if (command == null || !command.isSuccess())
                            return;

                        JSONObject resp = (JSONObject) command.getData();
                        if (resp == null || !resp.getBooleanValue("success"))
                            return;

                        // ✅如果主服返回 “order processed” 一律当作没下注成功（避免假象）
                        String msg = resp.getString("message");
                        if (msg != null && msg.toLowerCase().contains("order processed"))
                            return;

                        BigDecimal betAmount = resp.getBigDecimal("betAmount");
                        if (betAmount == null || betAmount.compareTo(BigDecimal.ZERO) <= 0)
                            return;

                        periodBotElementTotalBet.merge(elementId, chip, BigDecimal::add);

                        // ✅机器人下注也走同一套内存归集 + 记录落库
                        recordBet(periodNo, botUserId, elementId, chip, orderNoForAck);

                        // 同步一下奖池缓存
                        BigDecimal pool = resp.getBigDecimal("poolBalance");
                        if (pool != null)
                            lastPoolBalance = pool;

                    } catch (Exception ignore) {
                    } finally {
                        // ✅pending--
                        decPending(periodNo);
                    }
                }
            });
        } catch (Exception e) {
            log.error("[PBX] bot bet error", e);
            // 异常也别卡 pending
            decPending(periodNo);
        }
    }

    /**
     * 随机获取一个机器人 ID
     */
    private String getRandomBotUserId() {
        if (BOT_USER.isEmpty())
            return null;
        int idx = ThreadLocalRandom.current().nextInt(BOT_USER.size());
        return BOT_USER.keySet().stream().skip(idx).findFirst().orElse(null);
    }

    /**
     * 随机获取一个筹码
     */
    private BigDecimal getRandomBotChip() {
        if (BOT_CHIPS.isEmpty())
            return new BigDecimal("1");
        int idx = ThreadLocalRandom.current().nextInt(BOT_CHIPS.size());
        return BOT_CHIPS.get(idx);
    }

    /**
     * 向主服查询奖池（带重试/兜底）
     * 
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
     * @param periodNo  期号
     * @param userId    用户ID
     * @param elementId 元素ID
     * @param chip      筹码
     */
    private void recordBet(String periodNo, String userId, Integer elementId, BigDecimal chip, String orderNo) {
        if (chip == null || elementId == null)
            return;
        if (isBlank(periodNo) || isBlank(userId))
            return;
        if (isBlank(orderNo))
            return;

        PeriodSnapshot settling = settlingSnapshotMap.get(periodNo);
        if (settling != null) {
            settling.elementTotalBet.merge(elementId, chip, BigDecimal::add);
            settling.userElementBet.computeIfAbsent(userId, k -> new ConcurrentHashMap<>());
            settling.userElementBet.get(userId).merge(elementId, chip, BigDecimal::add);
            settling.userTotalBet.merge(userId, chip, BigDecimal::add);

            try {
                Long uidLong = Long.valueOf(userId);
                battleRoyaleRecord2Service.mergeBetForPeriodUser(uidLong, periodNo, elementId, chip);
            } catch (Exception e) {
                log.error("[PBX] mergeBetForPeriodUser(settling) error, userId=" + userId + ", periodNo=" + periodNo,
                        e);
            }
            return;
        }

        String cur;
        synchronized (PERIOD_LOCK) {
            cur = currentPeriodNo;
        }

        if (periodNo.equals(cur)) {
            periodElementTotalBet.merge(elementId, chip, BigDecimal::add);
            periodUserElementBet.computeIfAbsent(userId, k -> new ConcurrentHashMap<>());
            periodUserElementBet.get(userId).merge(elementId, chip, BigDecimal::add);
            periodUserTotalBet.merge(userId, chip, BigDecimal::add);

            try {
                Long uidLong = Long.valueOf(userId);
                battleRoyaleRecord2Service.mergeBetForPeriodUser(uidLong, periodNo, elementId, chip);
            } catch (Exception e) {
                log.error("[PBX] mergeBetForPeriodUser error, userId=" + userId + ", periodNo=" + periodNo, e);
            }
        }
    }

    /**
     * 推送全服信息更新（倒计时、奖池、在线人数）
     *
     * 修复点（解决你视频的“循环开奖/不等倒计时结束”）：
     * 1) tick 每秒推送 updatePbxInfo 时，不要因为 openedPeriodNo 缓存存在就强制 status=3。
     * 2) status 应由 buildPbxInfoByPeriod 根据时间窗口给出（下注期=1，展示期=2）。
     * 3) 若本期已开奖，可附带 resultElements/resultType/forcedNoWin 供展示，但不改变
     * status/remainSeconds。
     */
    private void pushPbxInfo(BigDecimal poolBalance) {
        long nowMs = System.currentTimeMillis();

        String periodNo = currentPeriodNo;
        if (periodNo == null)
            periodNo = ensureCurrentPeriod(nowMs);
        if (periodNo == null)
            return;

        if (poolBalance == null)
            poolBalance = BigDecimal.ZERO;

        // 1) 构造基础信息
        JSONObject info = buildPbxInfoByPeriod(poolBalance, periodNo, currentPeriodStartMs, currentPeriodEndMs, nowMs);

        // 2) tick 每秒推“轻量包”：把大字段裁掉（除非到时间或到开奖）
        boolean needHistory = (nowMs - lastHistoryPushMs) >= 5000L;

        // 本期已开奖：允许附带开奖结果字段（但不强制 status=3）
        boolean opened = (openedPeriodNo != null && openedPeriodNo.length() > 0
                && openedResultElements != null && openedResultElements.size() > 0);

        if (!needHistory && !opened) {
            info.remove("recent16");
            info.remove("recent100");
        } else if (needHistory) {
            lastHistoryPushMs = nowMs;
        }

        // 3) 已开奖时附带开奖结果字段（不改变 status/remainSeconds）
        if (opened) {
            info.put("openedPeriodNo", openedPeriodNo);
            info.put("openedResultElements", openedResultElements);
            info.put("openedResultType", openedResultType);
            info.put("forcedNoWin", openedForcedNoWin);

            // 兼容前端直接读 resultElements/resultType
            info.put("resultElements", openedResultElements);
            info.put("resultType", openedResultType);
        }

        Push.push(PushCode.updatePbxInfo, null, info);
    }

    /**
     * 推送下注失败消息
     */
    private void pushBetFailed(String userId, String orderNo, String periodNo, Integer elementId, BigDecimal chip,
            String message) {
        JSONObject state = onlineUserState.get(userId);
        if (state != null) {
            state.put("status", 1);
            state.put("ts", System.currentTimeMillis());
        }
        JSONObject pushStatus = buildPbxStatusPush(
                userId, 1, false, orderNo, periodNo, elementId, chip,
                null, lastPoolBalance, null, FEE_RATE);
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
    /**
     * 构建每期信息广播包
     * status 约定：
     * 1 = 下注中（倒计时>0）
     * 2 = 结算展示中（倒计时=0，但仍停留在同一期 periodNo）
     * 3 = 已开奖（由 broadcastSettleInfo 推送）
     */
    /**
     * 构建 PBX 基础信息推送包：
     * status=1 下注期：remainSeconds=下注剩余
     * status=2 结算展示期：remainSeconds=结算剩余（关键修复：不再永远为0）
     */
    private JSONObject buildPbxInfoByPeriod(BigDecimal poolBalance,
            String periodNo,
            long periodStartMs,
            long periodEndMs,
            long nowMs) {

        if (poolBalance == null)
            poolBalance = BigDecimal.ZERO;

        // ✅本期 cycleEnd（下注20s + 结算10s）
        long betMs = (TIME_SEC <= 0) ? 20000L : (TIME_SEC * 1000L);
        long settleMs = (SETTLE_SEC <= 0) ? 0L : (SETTLE_SEC * 1000L);
        long cycleEndMs = periodStartMs + betMs + settleMs;

        boolean inBetPhase = nowMs < periodEndMs;
        boolean inSettlePhase = (nowMs >= periodEndMs) && (nowMs < cycleEndMs);

        int remainSeconds;
        int status;

        if (inBetPhase) {
            long remainSec = ((periodEndMs - nowMs + 999L) / 1000L);
            remainSeconds = (int) Math.max(0L, remainSec);
            status = 1;
        } else if (inSettlePhase) {
            // ✅结算展示期也下发剩余秒（否则前端会“卡着等结算”）
            long remainSec = ((cycleEndMs - nowMs + 999L) / 1000L);
            remainSeconds = (int) Math.max(0L, remainSec);
            status = 2;
        } else {
            // 已经跨过cycleEnd，理论上 ensureCurrentPeriod 会切到下一期
            remainSeconds = 0;
            status = 2;
        }

        /**
         * ✅止血关键：避免“有时开奖有时不开（前端丢旧期status=3）”
         *
         * 当处于结算期(status=2)但还没有拿到 openedPeriodNo（说明开奖/结算推送还没到位）
         * 则 periodNo 维持为当前“待结算期”(currentPeriodNo)，不要提前展示新期号。
         *
         * 目的：前端显示的 periodNo 与后端随后推来的 status=3 periodNo 一致，避免被前端过滤掉。
         */
        String displayPeriodNo = periodNo;
        if (status == 2) {
            if (openedPeriodNo == null || openedPeriodNo.length() == 0) {
                // 还没开奖：维持“待开奖”的那一期
                displayPeriodNo = currentPeriodNo;
            } else {
                // 已经有开奖缓存：优先展示已开奖那一期，避免跨期错位
                displayPeriodNo = openedPeriodNo;
            }
        }

        JSONObject info = new JSONObject();
        info.put("gameId", String.valueOf(PBX_GAME_ID));
        info.put("onlineCount", onlineUserState.size());
        info.put("gameSetting", PBX_GAME_SETTING);
        info.put("poolBalance", poolBalance);

        info.put("serverTimeMs", nowMs);
        info.put("serverTime", dateTimeString(nowMs));

        // ✅使用 displayPeriodNo（关键）
        info.put("periodNo", displayPeriodNo);

        // 下注期起止（仍以当前周期边界为准）
        info.put("startTs", periodStartMs);
        info.put("endTs", periodEndMs);
        info.put("startTime", dateTimeString(periodStartMs));
        info.put("endTime", dateTimeString(periodEndMs));

        info.put("remainSeconds", remainSeconds);
        info.put("status", status);

        // ✅如果已开奖缓存存在，可顺便带给前端（看你前端协议是否需要）
        if (openedPeriodNo != null && openedPeriodNo.length() > 0
                && openedResultElements != null && openedResultElements.size() > 0) {
            info.put("openedPeriodNo", openedPeriodNo);
            info.put("openedResultElements", openedResultElements);
            info.put("openedResultType", openedResultType);
        }

        // 历史记录
        info.put("recent16", getRecentResults(16));
        info.put("recent100", getRecentResults(100));
        info.put("recent16Stat", buildRecent16Stat());
        info.put("recent100Stat", buildRecent100Stat());

        // 本期下注统计（展示用）
        JSONObject elementTotalBet = new JSONObject();
        BigDecimal totalBet = BigDecimal.ZERO;
        for (int i = 1; i <= ELEMENT_COUNT; i++) {
            BigDecimal real = periodElementTotalBet.getOrDefault(i, BigDecimal.ZERO);
            BigDecimal bot = periodBotElementTotalBet.getOrDefault(i, BigDecimal.ZERO);
            BigDecimal v = real.add(bot);
            totalBet = totalBet.add(v);
            elementTotalBet.put(String.valueOf(i), v);
        }
        info.put("elementTotalBet", elementTotalBet);
        info.put("totalBet", totalBet);

        return info;
    }

    /**
     * 构建自动结算状态推送包
     */
    private JSONObject buildAutoSettleStatusPush(String uid, String periodNo,
            JSONArray resultElements, String resultType,
            BigDecimal gross, BigDecimal fee, BigDecimal net,
            BigDecimal balance, BigDecimal poolBalance,
            String serverTime, JSONArray winList,
            int isWin, BigDecimal totalBet) {
        JSONObject pushStatus = new JSONObject();
        pushStatus.put("gameId", String.valueOf(PBX_GAME_ID));
        pushStatus.put("periodNo", periodNo);
        // 已开奖/结算
        pushStatus.put("status", 3);
        pushStatus.put("serverTime", serverTime);

        pushStatus.put("resultElements", resultElements);
        pushStatus.put("resultType", resultType);

        // 用户结算信息
        JSONObject info = new JSONObject();
        info.put("userId", uid);

        if (gross == null)
            gross = BigDecimal.ZERO;
        if (fee == null)
            fee = BigDecimal.ZERO;
        if (net == null)
            net = BigDecimal.ZERO;
        if (totalBet == null)
            totalBet = BigDecimal.ZERO;

        info.put("returnAmount", gross);
        info.put("fee", fee);
        info.put("net", net);
        info.put("totalBet", totalBet);

        // profit = net - bet
        BigDecimal profit = net.subtract(totalBet).setScale(2, RoundingMode.HALF_UP);
        String profitStr = profit.stripTrailingZeros().toPlainString();

        // ✅核心兼容字段
        info.put("totalGain", profitStr); // 后端已有
        info.put("total", profitStr); // ✅前端弹窗需要
        info.put("gain", profitStr); // 备用

        if (balance != null) {
            info.put("balance", balance);
        }

        pushStatus.put("userSettleInfo", info);

        // ✅顶层也放一份，兼容不同前端取值路径
        pushStatus.put("totalGain", profitStr);
        pushStatus.put("total", profitStr);
        pushStatus.put("gain", profitStr);

        if (poolBalance != null) {
            pushStatus.put("poolBalance", poolBalance);
        }
        if (winList != null) {
            pushStatus.put("winList", winList);
        }
        pushStatus.put("isWin", isWin);

        return pushStatus;
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
        if (balance != null)
            userSettleInfo.put("balance", balance);
        if (poolBalance != null)
            userSettleInfo.put("poolBalance", poolBalance);
        else if (lastPoolBalance != null)
            userSettleInfo.put("poolBalance", lastPoolBalance);
        if (fee != null)
            userSettleInfo.put("fee", fee);
        if (feeRate != null)
            userSettleInfo.put("feeRate", feeRate);

        JSONObject myElementBet = new JSONObject();
        Map<Integer, BigDecimal> myMap = periodUserElementBet.get(userId);
        for (int i = 1; i <= ELEMENT_COUNT; i++) {
            BigDecimal v = BigDecimal.ZERO;
            if (myMap != null)
                v = myMap.getOrDefault(i, BigDecimal.ZERO);
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
     * ✅新增：settleSec（展示/结算窗口秒数）可配置并下发，避免前端写死导致周期错配
     */
    public void initGameSetting() {
        JSONObject setting = null;

        try {
            Game game = gameService.findGameById((long) PBX_GAME_ID);
            if (game != null && game.getGameSetting() != null) {
                setting = JSON.parseObject(game.getGameSetting());
            }
        } catch (Exception e) {
            log.error("[PBX] initGameSetting parse db json error", e);
        }

        if (setting == null) {
            log.warn("[PBX] l_game(" + PBX_GAME_ID + ") game_setting parse failed, use defaults.");
            setting = defaultGameSetting();
        }

        // ====== 统一补全/归一化，保证 PBX_GAME_SETTING 下发结构完整 ======
        // time
        if (isBlank(setting.getString("time")))
            setting.put("time", "20");

        // ✅新增：settleSec（展示期秒数）——你视频描述更像 10 秒展示期，所以默认 10
        if (isBlank(setting.getString("settleSec")))
            setting.put("settleSec", "10");

        // capitalType
        if (isBlank(setting.getString("capitalType")))
            setting.put("capitalType", String.valueOf(UserCapitalTypeEnum.xxxhhb.getValue()));

        // chips
        JSONArray chips = setting.getJSONArray("chips");
        if (chips == null || chips.isEmpty()) {
            chips = new JSONArray();
            chips.add("1");
            chips.add("10");
            chips.add("100");
            setting.put("chips", chips);
        }

        // feeRate
        if (isBlank(setting.getString("feeRate")))
            setting.put("feeRate", "0.05");

        // elementCount
        if (isBlank(setting.getString("elementCount")))
            setting.put("elementCount", "6");

        // multipliers
        JSONObject mult = setting.getJSONObject("multipliers");
        if (mult == null) {
            mult = new JSONObject();
            setting.put("multipliers", mult);
        }
        if (isBlank(mult.getString("triple")))
            mult.put("triple", "10");
        if (isBlank(mult.getString("double")))
            mult.put("double", "4");
        if (isBlank(mult.getString("allDiff")))
            mult.put("allDiff", "1.8");

        // ====== 最终赋值 ======
        PBX_GAME_SETTING = setting;

        TIME_SEC = parseInt(PBX_GAME_SETTING.getString("time"), 20);
        // ✅把展示期秒数真正落到后端状态机
        SETTLE_SEC = parseInt(PBX_GAME_SETTING.getString("settleSec"), 10);

        CAPITAL_TYPE = parseInt(PBX_GAME_SETTING.getString("capitalType"), UserCapitalTypeEnum.xxxhhb.getValue());
        CHIPS = PBX_GAME_SETTING.getJSONArray("chips");
        FEE_RATE = parseBigDecimal(PBX_GAME_SETTING.getString("feeRate"), new BigDecimal("0.05"));
        ELEMENT_COUNT = parseInt(PBX_GAME_SETTING.getString("elementCount"), 6);

        JSONObject m = PBX_GAME_SETTING.getJSONObject("multipliers");
        MULT_TRIPLE = parseBigDecimal(m.getString("triple"), new BigDecimal("10"));
        MULT_DOUBLE = parseBigDecimal(m.getString("double"), new BigDecimal("4"));
        MULT_ALL_DIFF = parseBigDecimal(m.getString("allDiff"), new BigDecimal("1.8"));

        log.info("[PBX] initGameSetting ok: TIME_SEC=" + TIME_SEC
                + ", SETTLE_SEC=" + SETTLE_SEC
                + ", CAPITAL_TYPE=" + CAPITAL_TYPE
                + ", CHIPS=" + CHIPS
                + ", FEE_RATE=" + FEE_RATE
                + ", ELEMENT_COUNT=" + ELEMENT_COUNT
                + ", MULT=(" + MULT_TRIPLE + "," + MULT_DOUBLE + "," + MULT_ALL_DIFF + ")");
    }

    /**
     * 获取默认配置
     */
    private JSONObject defaultGameSetting() {
        JSONObject setting = new JSONObject();
        setting.put("time", "20");
        setting.put("capitalType", String.valueOf(UserCapitalTypeEnum.xxxhhb.getValue()));

        JSONArray chips = new JSONArray();
        chips.add("1");
        chips.add("10");
        chips.add("100");
        setting.put("chips", chips);

        setting.put("feeRate", "0.05");
        setting.put("elementCount", "6");
        setting.put("settleSec", "10");

        JSONObject mult = new JSONObject();
        mult.put("triple", "10");
        mult.put("double", "4");
        mult.put("allDiff", "1.8");
        setting.put("multipliers", mult);

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
                while (recent16Results.size() > 16)
                    recent16Results.removeFirst();
            }
            synchronized (recent100Results) {
                recent100Results.addLast(resultElements);
                while (recent100Results.size() > 100)
                    recent100Results.removeFirst();
            }
        } catch (Exception e) {
            log.error("[PBX] pushResultHistory error", e);
        }
    }

    /**
     * 获取最近N期开奖结果
     */
    private JSONArray getRecentResults(int n) {
        if (n <= 16)
            return buildRecent16();
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
            for (JSONObject o : list)
                arr.add(o);
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
            for (JSONArray a : list)
                arr.add(a);
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
                if (r == null)
                    continue;
                JSONArray open = r.getJSONArray("resultElements");
                if (open == null)
                    continue;
                for (int i = 0; i < open.size(); i++) {
                    int eid = open.getIntValue(i);
                    if (eid >= 1 && eid <= ELEMENT_COUNT)
                        counts[eid]++;
                }
            }
        }
        JSONObject stat = new JSONObject();
        for (int i = 1; i <= ELEMENT_COUNT; i++)
            stat.put(String.valueOf(i), counts[i]);
        return stat;
    }

    /**
     * 统计近100期各元素命中次数
     */
    private JSONObject buildRecent100Stat() {
        int[] counts = new int[ELEMENT_COUNT + 1];
        synchronized (recent100Results) {
            for (JSONArray open : recent100Results) {
                if (open == null)
                    continue;
                for (int i = 0; i < open.size(); i++) {
                    int eid = open.getIntValue(i);
                    if (eid >= 1 && eid <= ELEMENT_COUNT)
                        counts[eid]++;
                }
            }
        }
        JSONObject stat = new JSONObject();
        for (int i = 1; i <= ELEMENT_COUNT; i++)
            stat.put(String.valueOf(i), counts[i]);
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
            if (myMap != null)
                v = myMap.getOrDefault(i, BigDecimal.ZERO);
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
        if (um == null || um.isEmpty())
            return "";
        List<Integer> ks = new ArrayList<>(um.keySet());
        Collections.sort(ks);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ks.size(); i++) {
            if (i > 0)
                sb.append(",");
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
        if (isBlank(chipStr))
            chipStr = data.getString("betAmount");
        if (isBlank(chipStr))
            return null;
        try {
            BigDecimal c = new BigDecimal(chipStr);
            if (c.compareTo(BigDecimal.ZERO) <= 0)
                return null;
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
            if (data.containsKey("elementId"))
                return data.getIntValue("elementId");
        } catch (Exception ignored) {
        }
        return null;
    }

    /**
     * 校验筹码是否在允许列表中
     */
    private boolean isAllowedChip(BigDecimal chip) {
        if (chip == null || CHIPS == null || CHIPS.isEmpty())
            return false;
        for (int i = 0; i < CHIPS.size(); i++) {
            Object o = CHIPS.get(i);
            if (o == null)
                continue;
            try {
                BigDecimal allowed = new BigDecimal(String.valueOf(o)).stripTrailingZeros();
                if (allowed.compareTo(chip.stripTrailingZeros()) == 0)
                    return true;
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
            if (isBlank(s))
                return def;
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
            if (isBlank(s))
                return def;
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

        // ✅必须线程安全
        final ConcurrentHashMap<Integer, BigDecimal> elementTotalBet;
        final ConcurrentHashMap<String, ConcurrentHashMap<Integer, BigDecimal>> userElementBet;
        final ConcurrentHashMap<String, BigDecimal> userTotalBet;

        PeriodSnapshot(String periodNo, long startMs, long endMs,
                Map<Integer, BigDecimal> elementTotalBet,
                Map<String, Map<Integer, BigDecimal>> userElementBet,
                Map<String, BigDecimal> userTotalBet) {
            this.periodNo = periodNo;
            this.startMs = startMs;
            this.endMs = endMs;

            this.elementTotalBet = new ConcurrentHashMap<>();
            if (elementTotalBet != null)
                this.elementTotalBet.putAll(elementTotalBet);

            this.userElementBet = new ConcurrentHashMap<>();
            if (userElementBet != null) {
                for (Map.Entry<String, Map<Integer, BigDecimal>> e : userElementBet.entrySet()) {
                    ConcurrentHashMap<Integer, BigDecimal> m = new ConcurrentHashMap<>();
                    if (e.getValue() != null)
                        m.putAll(e.getValue());
                    this.userElementBet.put(e.getKey(), m);
                }
            }

            this.userTotalBet = new ConcurrentHashMap<>();
            if (userTotalBet != null)
                this.userTotalBet.putAll(userTotalBet);
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

        // ✅用于控盘比较的“净支出”(gross - fee)
        BigDecimal net = BigDecimal.ZERO;

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

    private void shuffleFastjson2Array(com.alibaba.fastjson2.JSONArray arr) {
        if (arr == null || arr.size() <= 1)
            return;

        java.util.List<Object> list = new java.util.ArrayList<>(arr.size());
        for (int i = 0; i < arr.size(); i++) {
            list.add(arr.get(i));
        }

        java.util.Collections.shuffle(list, java.util.concurrent.ThreadLocalRandom.current());

        arr.clear();
        for (Object o : list) {
            arr.add(o);
        }
    }

    private final ExecutorService recordExecutor = Executors.newFixedThreadPool(2, r -> {
        Thread t = new Thread(r);
        t.setName("pbx-record-worker-" + t.getId());
        t.setDaemon(true);
        return t;
    });

    /** 结算专用线程池：禁止占用 periodScheduler（tick 线程） */
    /** 结算专用线程池：至少 2 线程，避免单线程排队导致“超时换期/看似不开奖” */
    private final ExecutorService settleExecutor = new ThreadPoolExecutor(
            2, 2,
            60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(2000),
            r -> {
                Thread t = new Thread(r);
                t.setName("pbx-settle-worker-" + t.getId());
                t.setDaemon(true);
                return t;
            },
            new ThreadPoolExecutor.CallerRunsPolicy());

    public static boolean isBotUser(String userId) {
        if (userId == null)
            return false;
        try {
            return BOT_USER.containsKey(userId);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * ✅确保主周期调度器存活：如果被终止/心跳超时，自动重启
     */
    private void ensurePeriodSchedulerAlive() {
        try {
            long now = System.currentTimeMillis();
            boolean needRestart = false;
            // 启动后 3 秒内不做超时判定，避免误判
            if (schedulerStartAtMs > 0 && (now - schedulerStartAtMs) < 3000L) {
                return;
            }
            if (periodScheduler == null) {
                needRestart = true;
            } else if (periodScheduler.isShutdown() || periodScheduler.isTerminated()) {
                needRestart = true;
            } else {
                // 心跳超过 5 秒没更新，视为卡死/停摆
                if (lastTickAtMs > 0 && (now - lastTickAtMs) > 5000L) {
                    needRestart = true;
                }
            }

            if (!needRestart)
                return;

            synchronized (SCHEDULER_LOCK) {
                // double-check
                now = System.currentTimeMillis();
                boolean stillNeedRestart = false;

                if (periodScheduler == null) {
                    stillNeedRestart = true;
                } else if (periodScheduler.isShutdown() || periodScheduler.isTerminated()) {
                    stillNeedRestart = true;
                } else if (lastTickAtMs > 0 && (now - lastTickAtMs) > 5000L) {
                    stillNeedRestart = true;
                }

                if (!stillNeedRestart)
                    return;

                try {
                    if (periodScheduler != null) {
                        periodScheduler.shutdownNow();
                    }
                } catch (Exception ignore) {
                }

                // 允许重新启动
                periodSchedulerStarted.set(false);
                log.warn("[PBX] period scheduler dead/hung, restarting... now=" + now + ", lastTickAtMs="
                        + lastTickAtMs);

                startPeriodScheduler();
            }
        } catch (Exception e) {
            log.error("[PBX] ensurePeriodSchedulerAlive error", e);
        }
    }

    /**
     * ✅独立 watchdog：每 2 秒检查一次 tick 是否卡死，卡死则重启 periodScheduler
     */
    private void startWatchdogScheduler() {
        if (watchdogStarted.compareAndSet(false, true)) {
            watchdogScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r);
                t.setName("pbx-period-watchdog");
                t.setDaemon(true);
                return t;
            });

            watchdogScheduler.scheduleAtFixedRate(() -> {
                try {
                    ensurePeriodSchedulerAlive();
                } catch (Throwable t) {
                    log.error("[PBX] watchdog ensurePeriodSchedulerAlive error", t);
                }
            }, 2, 2, TimeUnit.SECONDS);

            log.info("[PBX] watchdog started.");
        }
    }

    /**
     * ✅给下注用户定向推 updatePbxInfo(status=3)，携带 totalGain（前端弹窗用）
     */
    /**
     * ✅给下注用户定向推 updatePbxInfo(status=3)，携带 totalGain（前端弹窗用）
     * 关键点：定向路由用 payload.userIds，而不是 Push.push 的第二个参数
     */
    private void pushUserStatus3Info(String uid,
            PeriodSnapshot snapshot,
            JSONArray resultElements,
            String resultType,
            BigDecimal poolBalance,
            int forcedNoWin,
            String totalGainStr,
            BigDecimal totalBet) {
        try {
            long nowMs = System.currentTimeMillis();

            JSONObject infoPush = buildPbxInfoByPeriod(poolBalance, snapshot.periodNo, snapshot.startMs, snapshot.endMs,
                    nowMs);
            infoPush.put("status", 3);
            infoPush.put("remainSeconds", 0);
            infoPush.put("resultElements", resultElements);
            infoPush.put("resultType", resultType);
            infoPush.put("forcedNoWin", forcedNoWin);

            // ✅前端弹窗取 totalGain（就在 updatePbxInfo.status=3 这包里）
            infoPush.put("totalGain", totalGainStr);
            infoPush.put("total", totalGainStr); // 兼容字段
            infoPush.put("myTotalBet", (totalBet == null ? "0" : totalBet.stripTrailingZeros().toPlainString()));

            // ✅关键：用 userIds 做定向路由
            JSONArray userIds = new JSONArray();
            userIds.add(uid);
            infoPush.put("userIds", userIds);

            // 第二参数保持 null（和你 buildPbxStatusPush 的用法一致）
            Push.push(PushCode.updatePbxInfo, null, infoPush);

        } catch (Exception e) {
            log.error("[PBX] pushUserStatus3Info error, uid=" + uid + ", periodNo="
                    + (snapshot == null ? "null" : snapshot.periodNo), e);
        }
    }
}
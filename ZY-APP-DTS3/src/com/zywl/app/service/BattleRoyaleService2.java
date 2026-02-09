package com.zywl.app.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.bean.Command;
import com.live.app.ws.defaultx.ServiceRunable;
import com.live.app.ws.enums.PushCode;
import com.live.app.ws.enums.TargetSocketType;
import com.live.app.ws.interfacex.Listener;
import com.live.app.ws.socket.BaseClientSocket;
import com.live.app.ws.util.CommandBuilder;
import com.live.app.ws.util.DefaultPushHandler;
import com.live.app.ws.util.Executer;
import com.live.app.ws.util.Push;
import com.zywl.app.base.bean.*;
import com.zywl.app.base.bean.vo.BattleRoyale2Record;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.*;
import com.zywl.app.bean.BattleRoyaleRoom2;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.cache.GameCacheService;
import com.zywl.app.defaultx.enmus.GameTypeEnum;
import com.zywl.app.defaultx.enmus.LogCapitalTypeEnum;
import com.zywl.app.defaultx.enmus.LotteryGameStatusEnum;
import com.zywl.app.defaultx.service.*;
import com.zywl.app.socket.BattleRoyaleSocketServer2;
import com.zywl.app.util.RequestManagerListener;
import org.apache.commons.collections4.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
@ServiceClass(code = "101")
public class BattleRoyaleService2 extends BaseService {

    public static BattleRoyaleRoom2 ROOM;

    public static JSONObject GAME_SETTING;

    public static int PEOPLE_NUM;

    public static BigDecimal MIN_BET;

    public static BigDecimal MAX_BET;

    public static volatile int STATUS;

    public static int OPTIONS_NUM;

    public static int MIN_KILL_COUNT;

    public static int MAX_KILL_COUNT;

    public static int TIME;

    public static int CAPITAL_TYPE;

    public static List<Integer> RATE_LIST = new ArrayList<>();

    public static ConcurrentHashMap<Long, ConcurrentHashMap<String, Object>> ROOLBACK_MAP = new ConcurrentHashMap<>();

    @Autowired
    private GameLotteryResultService gameLotteryResultService;

    @Autowired
    private GameService gameService;

    @Autowired
    private BattleRoyaleRecord3Service battleRoyaleRecordService;

    @Autowired
    private GameCacheService gameCacheService;

    @Autowired
    private UserCapitalService userCapitalService;

    @Autowired
    private BattleRoyaleService2 battleRoyaleService2;

    @Autowired
    private UserService userService;


    @Autowired
    private ConfigService configService;
    @Autowired
    private BattleRoyaleRequsetMangerService2 requsetMangerService;

    private static final Object lock = new Object();

    private static final Object betLock = new Object();

    public static String key = DateUtil.getCurrent5();

    public static Map<String, String> orderMap = new ConcurrentHashMap<>();

    public static Set<String> betUser = new ConcurrentHashSet<>();

    public static Set<String> updateRoomUser = new ConcurrentHashSet<>();

    public static String key2 = DateUtil.getCurrent5();

    private static Random random = new Random();

    public static String key3 = DateUtil.getCurrent5();
    public static BigDecimal rate = new BigDecimal("0.9");

    public static final Map<String, BigDecimal> REAL_ROOM_MONEY = new ConcurrentHashMap<>();

    public static Map<String, User> BOT_USER = new ConcurrentHashMap<>();

    public static int NEED_BOT = 0;

    public static int KILL_RATE = 0;

    public static List<BigDecimal> BOT_MONEY = new ArrayList<>();

    private static final int MANAGER_FLUSH_INTERVAL_MS = 200;
    private static final int MANAGER_BATCH_SIZE = 300;

    private static final java.util.concurrent.ConcurrentLinkedQueue<Map<String, String>> USER_CAPITAL_QUEUE
            = new java.util.concurrent.ConcurrentLinkedQueue<>();

    private static final java.util.concurrent.ConcurrentLinkedQueue<Map<String, String>> USER_RANK_CAPITAL_QUEUE
            = new java.util.concurrent.ConcurrentLinkedQueue<>();

    private transient java.util.concurrent.ScheduledExecutorService managerSyncScheduler;

    private static Map<String, List> orderArray = new ConcurrentHashMap<>();
    private static Map<String, List> orderArray2 = new ConcurrentHashMap<>();
    private static Map<String, JSONArray> pushArray = new ConcurrentHashMap<>();

    public void updateRate(BigDecimal a) {
        rate = a;
    }

    @PostConstruct
    public void _Construct() {
        // 同步游戏配置
        initGameSetting();

        // 初始化大逃杀房间
        ROOM = new BattleRoyaleRoom2(OPTIONS_NUM);

        // 初始化期数与历史数据
        periodsNum();
        initHistoryResult();

        // 推送支持
        Push.addPushSuport(PushCode.updateDts3Info, new DefaultPushHandler());
        Push.addPushSuport(PushCode.updateDts3Status, new DefaultPushHandler());

        // 离开推送支持
        Push.addPushSuport(PushCode.updateDts3UserLeave, new DefaultPushHandler());

        // 先加载机器人用户池（否则 getBotUser 永远为 null）
        initBotUsers();

        // 再加载机器人筹码档位（BOT_MONEY）
        initBotMoney();

        // 再加载机器人开关/概率（NEED_BOT）
        initNeedBot();

        // 其他运行期参数
        initKillRate();
        initRealMoney();

        // 最后启动机器人定时下注
        gameAddBot();

        // 启动定时任务：同步资产内存、滚动推送等
        requestManagerUpdateCapital();
    }



    public void initNeedBot() {
        try {
            Config cfg = configService.getConfigByKey(Config.GAME_DTS2_NEED_BOT);
            if (cfg == null || cfg.getValue() == null || cfg.getValue().trim().isEmpty()) {
                NEED_BOT = 0;
                logger.info("[DTS3] NEED_BOT 未配置，默认关闭（0）");
                return;
            }
            NEED_BOT = Integer.parseInt(cfg.getValue().trim());
            logger.info("[DTS3] 初始化 NEED_BOT=" + NEED_BOT + " (key=" + Config.GAME_DTS2_NEED_BOT + ")");
        } catch (Exception e) {
            NEED_BOT = 0;
            logger.error("[DTS3] 初始化 NEED_BOT 异常，默认关闭（0）", e);
        }
    }


    public void initKillRate() {
        Config config = configService.getConfigByKey(Config.DTS_KILL_RATE);
        if (config != null) {
            String value = config.getValue();
            KILL_RATE = Integer.parseInt(value);
        }
    }
    public void initBotMoney() {
        BOT_MONEY.clear();
        Config config = configService.getConfigByKey(Config.DTS_BOT_MONEY);
        if (config == null) {
            return;
        }
        String value = config.getValue();
        String[] split = value.split(",");
        for (String s : split) {
            BOT_MONEY.add(new BigDecimal(s));
        }
    }
    /**
     * 初始化机器人用户池
     */
    public void initBotUsers() {
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
            logger.info("[DTS3] 加载机器人完成，数量=" + BOT_USER.size());
        } catch (Exception e) {
            logger.error("[DTS3] 加载机器人异常", e);
        }
    }

    public void gameAddBot() {
        new Timer("DTS3-游戏添加人机").schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    // NEED_BOT <= 0 直接不开
                    if (NEED_BOT <= 0) {
                        return;
                    }
                    // 没机器人用户直接跳过
                    if (BOT_USER == null || BOT_USER.isEmpty()) {
                        return;
                    }
                    // 没机器人筹码直接跳过
                    if (BOT_MONEY == null || BOT_MONEY.isEmpty()) {
                        return;
                    }
                    // 仅在 ready / gaming 阶段下注
                    if (ROOM.getStatus() != LotteryGameStatusEnum.ready.getValue()
                            && ROOM.getStatus() != LotteryGameStatusEnum.gaming.getValue()) {
                        return;
                    }
                    // 概率触发：NEED_BOT=30 => 30% 概率下注
                    int r = random.nextInt(100);
                    if (r >= NEED_BOT) {
                        return;
                    }
                    User bot = getBotUser();
                    if (bot == null || bot.getId() == null) {
                        return;
                    }
                    if (STATUS == 0) {
                        return;
                    }
                    int optionCount = ROOM.getOption();
                    if (optionCount <= 0) {
                        optionCount = 9;
                    }

                    String botId = bot.getId().toString();

                    //本局已下注过  固定房间继续加注
                    int option;
                    Map<String, BigDecimal> botBetInfo = ROOM.getUserBetInfo().get(botId);
                    if (botBetInfo != null && !botBetInfo.isEmpty()) {
                        // 同一用户本局只允许押一个房间 切房要走 004003
                        String betKey = botBetInfo.keySet().iterator().next();
                        int parsed;
                        try {
                            parsed = Integer.parseInt(betKey);
                        } catch (Exception ex) {
                            parsed = -1;
                        }
                        // betKey 非法或超出范围
                        if (parsed < 0 || parsed >= optionCount) {
                            option = random.nextInt(optionCount);
                        } else {
                            option = parsed;
                        }
                    } else {
                        // 本局第一次下注 => 随机房间
                        option = random.nextInt(optionCount);
                    }
                    userBetBet(botId, String.valueOf(option), getBotMoney(), null, null);
                } catch (Exception e) {
                    logger.error("[DTS3] robot bet error", e);
                }
            }
        }, 300, 100);
    }


    private void refreshStatusFromDb() {
        try {
            Game game = gameService.findGameById(1L);
            if (game != null) {
                STATUS = game.getStatus();
            }
        } catch (Exception e) {
            logger.error("[DTS3] refreshStatusFromDb error", e);
        }
    }


    public static BigDecimal getBotMoney() {
        if (BOT_MONEY == null || BOT_MONEY.isEmpty()) {
            return BigDecimal.ONE;
        }
        Collections.shuffle(BOT_MONEY);
        return BOT_MONEY.get(0);
    }


    public User getBotUser() {
        return getRandomValue(BOT_USER);
    }


    public static <K, V> V getRandomValue(Map<K, V> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        int randomIndex = java.util.concurrent.ThreadLocalRandom.current().nextInt(map.size());
        return map.values().stream()
                .skip(randomIndex)
                .findFirst()
                .orElse(null);
    }

    private void requestManagerUpdateCapital() {
        if (managerSyncScheduler != null) {
            return;
        }

        managerSyncScheduler = java.util.concurrent.Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            t.setName("dts3-manager-sync");
            return t;
        });

        // flush 投入
        managerSyncScheduler.scheduleAtFixedRate(() -> {
            try {
                List<Map<String, String>> batch = new ArrayList<>(MANAGER_BATCH_SIZE);
                for (int i = 0; i < MANAGER_BATCH_SIZE; i++) {
                    Map<String, String> one = USER_CAPITAL_QUEUE.poll();
                    if (one == null) break;
                    batch.add(one);
                }
                if (batch.isEmpty()) return;

                JSONObject object = new JSONObject();
                object.put("betArray", batch);
                Executer.request(
                        TargetSocketType.dts3,
                        CommandBuilder.builder().request("200821", object).build(),
                        new RequestManagerListener(null)
                );
            } catch (Exception e) {
                logger.error("flush user capital to manager error", e);
            }
        }, 0, MANAGER_FLUSH_INTERVAL_MS, java.util.concurrent.TimeUnit.MILLISECONDS);

        // flush 排名返利
        managerSyncScheduler.scheduleAtFixedRate(() -> {
            try {
                List<Map<String, String>> batch = new ArrayList<>(MANAGER_BATCH_SIZE);
                for (int i = 0; i < MANAGER_BATCH_SIZE; i++) {
                    Map<String, String> one = USER_RANK_CAPITAL_QUEUE.poll();
                    if (one == null) break;
                    batch.add(one);
                }
                if (batch.isEmpty()) return;

                JSONObject object = new JSONObject();
                object.put("betArray", batch);
                Executer.request(
                        TargetSocketType.dts3,
                        CommandBuilder.builder().request("200822", object).build(),
                        new RequestManagerListener(null)
                );
            } catch (Exception e) {
                logger.error("flush rank rebate to manager error", e);
            }
        }, 0, MANAGER_FLUSH_INTERVAL_MS, java.util.concurrent.TimeUnit.MILLISECONDS);
    }


    public void addPushSuport() {
        Push.addPushSuport(PushCode.rollbackCapital, new DefaultPushHandler());
        Push.addPushSuport(PushCode.updateUserCapital, new DefaultPushHandler());
        Push.addPushSuport(PushCode.updateDts3Status, new DefaultPushHandler());
        Push.addPushSuport(PushCode.updateDts3Info, new DefaultPushHandler());
        Push.addPushSuport(PushCode.updateDts3UserLeave, new DefaultPushHandler());

    }

    @Transactional
    @ServiceMethod(code = "101", description = "用户加入大逃杀房间")
    public Object jionRoom(BattleRoyaleSocketServer2 adminSocketServer, Command lotteryCommand, JSONObject data) {
        checkNull(data);
        checkNull(data.get("userId"), data.get("userNo"));

        String userId = data.getString("userId");
        String userNo = data.getString("userNo");
        String userName = data.getString("userName");
        String headImgUrl = String.valueOf(data.getOrDefault("headImgUrl", ""));

        synchronized (lock) {

            // players
            Map<String, String> player = ROOM.getPlayers().get(userId);
            if (player == null) {
                player = new HashedMap<>();
                ROOM.getPlayers().put(userId, player);
            }
            player.put("userNo", userNo);
            player.put("userName", userName);
            player.put("headImgUrl", headImgUrl);

            // 已下注则取下注房间；否则 bet 可选（用于加入默认房间），默认 0
            String roomId = null;
            if (ROOM.getUserBetInfo().containsKey(userId)) {
                for (String rid : ROOM.getUserBetInfo().get(userId).keySet()) {
                    roomId = rid;
                    break;
                }
            } else {
                roomId = data.getString("bet");
                if (StringUtils.isBlank(roomId)) {
                    roomId = ROOM.getUserCheckNum().get(userId);
                }
                if (StringUtils.isBlank(roomId)) {
                    roomId = "0";
                }
                roomId = normalizeRoomId(roomId, ROOM.getOption());
            }

            if (!ROOM.getRoomList().containsKey(roomId)) {
                ROOM.getRoomList().put(roomId, new ConcurrentHashMap<String, JSONObject>());
            }

            // 未下注用户不应该进入任何 roomList 房间（避免默认塞入 roomList[0]）
            if (!ROOM.getUserBetInfo().containsKey(userId)) {

                if (!ROOM.getLookList().containsKey(userId)) {
                    Map<String, Object> look = new HashedMap<>();
                    look.put("userId", userId);
                    look.put("name", userName);
                    ROOM.getLookList().put(userId, look);
                    ROOM.setLookNum(ROOM.getLookNum() + 1);

                    ROOM.setLastWeekTopThree(gameCacheService.getLastWeekTopList(GameTypeEnum.dts2.getValue(), 10));

                    // 加入房间也推送 updateDts3Info（不包含 roomList 用户落房信息）
                    appendDts3Info(ROOM.pushResult(3, userId, null, null));
                }

                // 清理历史残留（防止断线重连/重复 join 导致仍在某个 roomList 里）
                removeUserFromAllRooms(userId);

                // 只记录用户当前选择房间（不影响前端 roomList 展示）
                ROOM.getUserCheckNum().put(userId, roomId);

            } else {
                // 已下注：补齐字段（已投入的用户才会出现在 roomList）
                BigDecimal betAmount = BigDecimal.ZERO;
                try {
                    betAmount = ROOM.getUserBetInfo().get(userId).get(roomId);
                } catch (Exception ignore) {
                }

                JSONObject betInfo = ROOM.getRoomList().get(roomId).get(userId);
                if (betInfo == null) {
                    betInfo = new JSONObject();
                    betInfo.put("userId", userId);
                    betInfo.put("betAmount", betAmount);
                }

                betInfo.put("userNo", userNo);
                betInfo.put("userName", userName);
                betInfo.put("name", userName);
                betInfo.put("headImgUrl", headImgUrl);
                betInfo.put("roomId", roomId);

                ROOM.getRoomList().get(roomId).put(userId, betInfo);
            }
        }

        JSONObject resp = ROOM.getReturnInfo();
        try {
            resp.putAll(battleRoyaleRecordService.buildUnifiedSummary(Long.parseLong(data.getString("userId")), true));
        } catch (Exception ignore) {
        }
        return resp;
    }



    @Transactional
    @ServiceMethod(code = "104", description = "用户离开大逃杀房间")
    public Object leaveRoom(BattleRoyaleSocketServer2 adminSocketServer, Command lotteryCommand, JSONObject data) {
        checkNull(data);
        checkNull(data.get("userId"));

        String userId = data.getString("userId");
        JSONObject pushResult = ROOM.pushResult(2, userId, null, null);

        data.put("server", TargetSocketType.battleRoyale.toString());
        data.put("type", 2);
        data.put("capitalType", CAPITAL_TYPE);

        // 只有 未下注 + 当前在观战列表 才允许真正离开
        if (!ROOM.getUserBetInfo().containsKey(userId) && ROOM.getLookList().containsKey(userId)) {

            String oldRoomId = ROOM.getUserCheckNum().get(userId);

            Map<String, String> p = ROOM.getPlayers().get(userId);
            ROOM.getPlayers().remove(userId);
            ROOM.getLookList().remove(userId);

            if (ROOM.getLookNum() > 0) {
                ROOM.setLookNum(ROOM.getLookNum() - 1);
            }

            // 清理 roomList 占位
            removeUserFromAllRooms(userId);
            ROOM.getUserCheckNum().remove(userId);

            // 推送离开用户信息（仅 gameId=1）
            try {
                JSONObject leave = new JSONObject();
                leave.put("gameId", "1");
                leave.put("userId", userId);

                if (p != null) {
                    leave.put("userNo", p.get("userNo"));
                    leave.put("userName", p.get("userName"));
                    leave.put("headImgUrl", p.get("headImgUrl"));
                }
                leave.put("roomId", oldRoomId);
                Push.push(PushCode.updateDts3UserLeave, "1", leave);
            } catch (Exception ignore) {
            }

            appendDts3Info(pushResult);
        }

        return new JSONObject();
    }


    @ServiceMethod(code = "105", description = "用户更换下注房间")
    public JSONObject updateRoom(BattleRoyaleSocketServer2 adminSocketServer, Command lotteryCommand, JSONObject data) {
        checkNull(data);
        checkNull(data.get("userId"), data.get("userNo"), data.get("userName"), data.get("bet"));
        int status = ROOM.getStatus();
        if (ROOM.getEndTime() > System.currentTimeMillis() && (ROOM.getEndTime() - System.currentTimeMillis()) < 2000) {
            JSONObject resp = new JSONObject();
            resp.put("status", status);
            resp.put("switchEnable", 0);
            resp.put("serverTime", System.currentTimeMillis());
            resp.put("beginTime", ROOM.getBeginTime());
            resp.put("endTime", ROOM.getEndTime());
            return resp;
        }

        String userId = data.getString("userId");
        String newRoomId = data.getString("bet");
        newRoomId = normalizeRoomId(newRoomId, ROOM.getOption());

        synchronized (LockUtil.getlock(userId + "bet")) {
            if (ROOM.getRoomList().get(newRoomId).containsKey(userId)) {
                throwExp("已经在该房间啦~");
            }

            ROOM.getUserCheckNum().put(userId, newRoomId);

            if (!ROOM.getUserBetInfo().containsKey(userId)) {
                throwExp("点击过快");
            }

            String roomId = null;
            for (String r : ROOM.getUserBetInfo().get(userId).keySet()) {
                roomId = r;
                break;
            }

            if (roomId == null) {
                for (String room : ROOM.getRoomList().keySet()) {
                    if (ROOM.getRoomList().get(room).containsKey(userId)) {
                        roomId = room;
                        break;
                    }
                }
            }

            if (roomId == null) {
                throwExp("更换房间频繁");
            }

            updateRoomUser.add(userId);
            try {
                BigDecimal amount = ROOM.getUserBetInfo().get(userId).get(roomId);

                Map<String, BigDecimal> newRoomBetInfo = new HashMap<>();
                newRoomBetInfo.put(newRoomId, amount);
                ROOM.getUserBetInfo().put(userId, newRoomBetInfo);

                JSONObject betInfo = ROOM.getRoomList().get(roomId).get(userId);
                ROOM.getRoomList().get(newRoomId).put(userId, betInfo);
                ROOM.getRoomList().get(roomId).remove(userId);

                // betInfo.roomId 同步更新
                if (betInfo != null) {
                    betInfo.put("roomId", newRoomId);
                }

                ROOM.getBetOptionsInfo().get(roomId).put(
                        "betNumber",
                        String.valueOf(Integer.parseInt(ROOM.getBetOptionsInfo().get(roomId).get("betNumber")) - 1)
                );
                ROOM.getBetOptionsInfo().get(roomId).put(
                        "betAmount",
                        new BigDecimal(ROOM.getBetOptionsInfo().get(roomId).get("betAmount")).subtract(amount).toString()
                );

                ROOM.getBetOptionsInfo().get(newRoomId).put(
                        "betNumber",
                        String.valueOf(Integer.parseInt(ROOM.getBetOptionsInfo().get(newRoomId).get("betNumber")) + 1)
                );
                ROOM.getBetOptionsInfo().get(newRoomId).put(
                        "betAmount",
                        new BigDecimal(ROOM.getBetOptionsInfo().get(newRoomId).get("betAmount")).add(amount).toString()
                );

                appendDts3Info(ROOM.pushResult(1, userId, newRoomId, amount));

                // 给 004003 响应补 roomId
                JSONObject resp = new JSONObject();
                resp.put("roomId", newRoomId);
                return resp;

            } finally {
                updateRoomUser.remove(userId);
            }
        }
    }




    public BigDecimal addBet(String userId, String userBet, BigDecimal amount) {
        BigDecimal allAmount = amount;

        Map<String, BigDecimal> userBets = ROOM.getUserBetInfo().get(userId);
        if (userBets == null || !userBets.containsKey(userBet)) {
            throwExp("非法请求");
        }

        userBets.put(userBet, userBets.get(userBet).add(amount));
        ROOM.getUserBetInfo().put(userId, userBets);

        JSONObject roomUser = ROOM.getRoomList().get(userBet).get(userId);
        if (roomUser == null) {
            throwExp("非法请求");
        }

        allAmount = allAmount.add(roomUser.getBigDecimal("betAmount"));
        roomUser.put("betAmount", roomUser.getBigDecimal("betAmount").add(amount));
        return allAmount;
    }


    public Map<String, String> updateCapital(String userId, BigDecimal amount, String orderNo, Long dataId) {
        Map<String, String> myOrder = new HashMap<>();
        myOrder.put("orderNo", orderNo);
        myOrder.put("dataId", String.valueOf(dataId));
        myOrder.put("betAmount", amount.toString());
        myOrder.put("userId", userId);
        myOrder.put("capitalType", String.valueOf(CAPITAL_TYPE));

        // 机器人不走真实资产扣减与队列
        if (BOT_USER.containsKey(userId)) {
            return myOrder;
        }

        userCapitalService.subUserOccupyBalanceByDtsBet(Long.parseLong(userId), amount,CAPITAL_TYPE);
        USER_CAPITAL_QUEUE.offer(myOrder);
        return myOrder;
    }



    public void rankRebate(String userId, BigDecimal amount, String orderNo) {
        userCapitalService.addUserBalanceByDtsRank(Long.parseLong(userId), amount,CAPITAL_TYPE);
        Map<String, String> myOrder = new HashMap<>();
        myOrder.put("orderNo", orderNo);
        myOrder.put("betAmount", amount.toString());
        myOrder.put("userId", userId);
        myOrder.put("capitalType", String.valueOf(CAPITAL_TYPE));
        USER_RANK_CAPITAL_QUEUE.offer(myOrder);

    }



    public void bet(Map<String, String> myOrder, String userId, String userBet, BigDecimal amount) {
        ROOM.getUserBetOrderInfo().put(userId, myOrder);

        myOrder.put("isBot", "0");
        if (BOT_USER.containsKey(userId)) {
            myOrder.put("isBot", "1");
        }

        // 下注人数+1
        ROOM.setBetNum(ROOM.getBetNum() + 1);

        // 只有在观战列表存在时才 lookNum -1
        if (ROOM.getLookList().containsKey(userId)) {
            ROOM.setLookNum(Math.max(ROOM.getLookNum() - 1, 0));
            ROOM.getLookList().remove(userId);
        }

        // 首次下注：先移除占位用户
        removeUserFromAllRooms(userId);

        JSONObject betInfo = new JSONObject();
        betInfo.put("userId", userId);
        betInfo.put("roomId", userBet);
        betInfo.put("betAmount", amount);

        if (BOT_USER.containsKey(userId)) {
            User bot = BOT_USER.get(userId);
            betInfo.put("userNo", bot.getUserNo());
            betInfo.put("name", bot.getName());
            betInfo.put("userName", bot.getName());
            betInfo.put("headImgUrl", bot.getHeadImageUrl());
        } else {
            Map<String, String> p = ROOM.getPlayers().get(userId);
            betInfo.put("userNo", p == null ? "" : p.get("userNo"));
            betInfo.put("name", p == null ? "" : p.get("userName"));
            betInfo.put("userName", p == null ? "" : p.get("userName"));
            betInfo.put("headImgUrl", p == null ? "" : p.get("headImgUrl"));
        }

        ROOM.getRoomList().get(userBet).put(userId, betInfo);

        Map<String, BigDecimal> bets = new HashMap<>();
        bets.put(userBet, amount);
        ROOM.getUserBetInfo().put(userId, bets);
    }


    @Transactional
    @ServiceMethod(code = "103", description = "用户下注")
    public Object userBet(BattleRoyaleSocketServer2 adminSocketServer, Command lotteryCommand, JSONObject params) {

        checkNull(params);
        checkNull(params.get("userId"), params.get("betAmount"), params.get("bet"));
        String userId = params.getString("userId");
        String userBet = params.getString("bet");
        BigDecimal amount = params.getBigDecimal("betAmount");
        params.put("capitalType", CAPITAL_TYPE);
        return userBetBet(userId, userBet, amount, lotteryCommand, params);
    }

    public JSONObject userBetBet(String userId, String userBet, BigDecimal amount, Command lotteryCommand, JSONObject params) {
        if (STATUS == 0) {
            refreshStatusFromDb();
            if (STATUS == 0) {
                throwExp("神尊护体即将维护，暂时不能进行游戏！");
            }
        }

        if (ROOM.getStatus() == LotteryGameStatusEnum.settle.getValue()) {
            throwExp("上局结算中,请等待结算完成重新渡劫 ~");
        }
        if (System.currentTimeMillis() > ROOM.getReadyTime() && (System.currentTimeMillis() - ROOM.getReadyTime()) < 2000) {
            throwExp("上局结算中,请等待结算完成重新渡劫 ~");
        }
        if (ROOM.getEndTime() != 0L && ROOM.getEndTime() - System.currentTimeMillis() < 2000) {
            throwExp("本局即将结束，请稍后参与 ~");
        }

        ROOM.getUserCheckNum().put(userId, userBet);

        if (!BOT_USER.containsKey(userId)) {
            UserCapital userCapital = userCapitalService.findUserCapitalByUserIdAndCapitalType(Long.parseLong(userId), CAPITAL_TYPE);
            if (userCapital == null) {
                throwExp("资产不存在");
            }
            if (userCapital.getBalance().compareTo(amount) < 0) {
                throwExp("资产不足");
            }
        }

        if (Integer.parseInt(userBet) > ROOM.getOption() - 1 || Integer.parseInt(userBet) < 0) {
            throwExp("下注错误！");
        }

        synchronized (lock) {
            if (betUser.contains(userId)) {
                throwExp("下注中，请稍后");
            }
            betUser.add(userId);
            try {
                String orderNo = String.valueOf(System.currentTimeMillis()) + userId;

                Map<String, String> myOrder = new HashMap<>();
                long dataId = 0L;

                if (ROOM.getUserBetInfo().containsKey(userId)) {
                    Map<String, String> orderInfo = ROOM.getUserBetOrderInfo().get(userId);
                    if (orderInfo != null && orderInfo.get("dataId") != null) {
                        dataId = Long.parseLong(orderInfo.get("dataId"));
                    }
                } else {
                    dataId = battleRoyaleRecordService.addBattleRoyaleRecord(Long.parseLong(userId), orderNo,
                            ROOM.getPeridosNum(), userBet, amount);
                }

                // 无论机器人/真人，都要生成 orderNo/dataId 并写入 userBetOrderInfo
                myOrder = updateCapital(userId, amount, orderNo, dataId);

                BigDecimal allAmount = amount;
                if (ROOM.getUserBetInfo().containsKey(userId)) {
                    allAmount = addBet(userId, userBet, amount);
                } else {
                    bet(myOrder, userId, userBet, amount);
                }

                ROOM.getBetOptionsInfo().get(userBet).put("betNumber",
                        String.valueOf((Integer.parseInt(ROOM.getBetOptionsInfo().get(userBet).get("betNumber")) + 1)));
                ROOM.getBetOptionsInfo().get(userBet).put("betAmount",
                        (new BigDecimal(ROOM.getBetOptionsInfo().get(userBet).get("betAmount")).add(amount)).toString());

                ROOM.setAllBetAmount(ROOM.getAllBetAmount().add(amount));

                // 排行榜累 按本次下注金额
                if (!BOT_USER.containsKey(userId)) {
                    int rankNumber = amount.setScale(0, BigDecimal.ROUND_DOWN).intValue();
                    if (rankNumber > 0) {
                        gameCacheService.addGameRankCache(GameTypeEnum.dts2.getValue(), userId, rankNumber);
                    }
                }

                // 满足开局人数 切换到 gaming
                synchronized (lock) {
                    if (ROOM.getBetNum() >= PEOPLE_NUM && ROOM.getStatus() == LotteryGameStatusEnum.ready.getValue()) {
                        ROOM.setBeginTime(System.currentTimeMillis());
                        ROOM.setEndTime(DateUtil.getTimeByM(TIME));
                        changeRoomStatus(LotteryGameStatusEnum.gaming.getValue(), lotteryCommand);
                    }
                }

                appendDts3Info(ROOM.pushResult(1, userId, userBet, allAmount));

                if (!BOT_USER.containsKey(userId)) {
                    REAL_ROOM_MONEY.put(userBet, REAL_ROOM_MONEY.getOrDefault(userBet, BigDecimal.ZERO).add(amount));
                    Executer.response(CommandBuilder.builder(lotteryCommand)
                            .success(ROOM.pushResult(1, userId, userBet, allAmount)).build());
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (!BOT_USER.containsKey(userId)) {
                    logger.info(e);
                    Push.push(PushCode.cancelBet, null, params);
                }
            } finally {
                betUser.remove(userId);
            }
        }
        return new JSONObject();
    }


    /**
     * 初始化最近开奖结果
     */
    public void initHistoryResult() {
        logger.info("更新大逃杀历史开奖结果");
        long time = System.currentTimeMillis();
        List<GameLotteryResult> result100 = gameLotteryResultService.findHistoryResultByGameId(1L, 100);
        List<GameLotteryResult> result20 = gameLotteryResultService.findHistoryResultByGameId(1L, 20);
        JSONObject result1 = new JSONObject();
        for (GameLotteryResult gameLotteryResult : result100) {
            String lotteryResult = gameLotteryResult.getLotteryResult();
            if (result1.containsKey(lotteryResult)) {
                result1.put(lotteryResult, result1.getIntValue(lotteryResult) + 1);
            } else {
                result1.put(lotteryResult, 1);
            }
        }
        ROOM.setHistory100Reuslt(result1);
        JSONObject result2 = new JSONObject();
        for (GameLotteryResult gameLotteryResult : result20) {
            String lotteryResult = gameLotteryResult.getLotteryResult();
            if (result2.containsKey(lotteryResult)) {
                result2.put(lotteryResult, result2.getIntValue(lotteryResult) + 1);
            } else {
                result2.put(lotteryResult, 1);
            }
        }
        ROOM.setHistory20Reuslt(result2);
        logger.info("更新大逃杀历史开奖结果完成，用时：" + (System.currentTimeMillis() - time));
    }

    public void periodsNum() {
        logger.info("初始化大逃杀期数信息");
        BattleRoyale2Record battleRoyaleRecord = battleRoyaleRecordService.findPeriodsNum();
        if (battleRoyaleRecord == null) {
            ROOM.setPeridosNum("1");
        } else {
            ROOM.setPeridosNum(String.valueOf((Long.parseLong(battleRoyaleRecord.getPeriodsNum()) + 1)));
            ROOM.setLastResult(String.valueOf(battleRoyaleRecord.getLotteryResult()));
        }

        logger.info("初始化大逃杀期数信息完成");
    }


    // 更改房间状态
    @Transactional
    public void startGame(Command lotteryCommand) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            int i = TIME;

            public void run() {
                if (ROOM.getEndTime() <= System.currentTimeMillis()) {
                    logger.info("游戏结束  结算");
                    // 下注期结束 更改状态为结算
                    if (ROOM.getStatus() != LotteryGameStatusEnum.settle.getValue()) {
                        ROOM.setStatus(LotteryGameStatusEnum.settle.getValue());
                        changeRoomStatus(ROOM.getStatus(), lotteryCommand);
                    }
                    timer.cancel();
                }
                i--;
            }
        }, 0, 1000L);
    }

    public void changeRoomStatus(int roomStatus, Command lotteryCommand) {
        ROOM.setStatus(roomStatus);
        JSONObject data = new JSONObject();
        data.put("userIds", ROOM.getPlayers().keySet());
        data.put("gameId", GameTypeEnum.dts2.getValue());
        if (ROOM.getStatus() == LotteryGameStatusEnum.ready.getValue()) {
            // 初始化房间信息 更新历史开奖结果
            ROOM.initRoomInfo();
            initHistoryResult();
            initRealMoney();
            data.put("lookList", new ConcurrentHashMap<String, Map<String, Object>>());
            data.put("roomList", ROOM.getRoomList());
            data.put("status", ROOM.getStatus());
            data.put("periodsNum", ROOM.getPeridosNum());
            data.put("lastResult", ROOM.getLastResult());
            Push.push(PushCode.updateDts3Status, null, data);
        } else if (ROOM.getStatus() == LotteryGameStatusEnum.gaming.getValue()) {
            data.put("status", ROOM.getStatus());
            data.put("endTime", ROOM.getEndTime());
            data.put("gameId", GameTypeEnum.dts2.getValue());
            Executer.executeService(new ServiceRunable(logger) {
                public void service() {
                    startGame(lotteryCommand);
                }
            });
            Push.push(PushCode.updateDts3Status, null, data);
        } else if (ROOM.getStatus() == LotteryGameStatusEnum.settle.getValue()) {
            List<Integer> killList = battleRoyaleService2.draw();
            ROOM.setResult(killList);
            ROOM.setLastResult(killList.toString());
            battleRoyaleService2.settle(killList, lotteryCommand);
            ROOM.setReadyTime(System.currentTimeMillis());
            int status = ROOM.getStatus();
            ConcurrentHashMap<String, Map<String, String>> userBetOrderInfo = ROOM.getUserBetOrderInfo();
            data.put("roomId", killList);
            data.put("status", status);
            data.putAll(ROOM.getSettleDate());
            data.put("userSettleInfo", userBetOrderInfo);
            JSONObject userRecordSummaryMap = new JSONObject();
            try {
                if (userBetOrderInfo != null) {
                    for (String uid : userBetOrderInfo.keySet()) {
                        BigDecimal extraGain = null;
                        try {
                            Map<String, String> si = userBetOrderInfo.get(uid);
                            if (si != null && si.get("winAmount") != null) {
                                extraGain = new BigDecimal(si.get("winAmount"));
                            }
                        } catch (Exception ignore) {
                        }
                        try {
                            userRecordSummaryMap.put(uid, battleRoyaleRecordService.buildUnifiedSummary(Long.valueOf(uid), true, extraGain));
                        } catch (Exception ignore) {
                        }
                    }
                }
            } catch (Exception ignore) {
            }
            data.put("userRecordSummaryMap", userRecordSummaryMap);
            Push.push(PushCode.updateDts3Status, null, data);
            Executer.executeService(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException e) {
                        logger.info(e);
                    }
                    changeRoomStatus(LotteryGameStatusEnum.ready.getValue(), lotteryCommand);
                }
            });
        }
    }

    @Transactional
    public void settle(List<Integer> killList, Command lotteryCommand) {
        List<String> lastWeekTopIds = GameCacheService.getLastWeekTopUserIds(GameTypeEnum.dts2.getValue());

        List<String> result = new ArrayList<>();
        killList.forEach(e -> result.add(e.toString()));
        System.out.println("开奖结果：" + result);
        int winNumber = 0;
        int loseNumber = 0;
        // 结算 获取每个人下注 和 总分红扣掉5%的比例 每个人的比例
        Set<String> bets = ROOM.getBetOptionsInfo().keySet();
        BigDecimal allLoseAmount = BigDecimal.ZERO;
        BigDecimal allWinAmount = BigDecimal.ZERO;
        System.out.println("OptionsInfo:" + ROOM.getBetOptionsInfo());
        for (String bet : bets) {
            String optionBetAmount = ROOM.getBetOptionsInfo().get(bet).get("betAmount");
            if (!result.contains(bet)) {
                allWinAmount = allWinAmount.add(new BigDecimal(optionBetAmount));
            } else {
                allLoseAmount = allLoseAmount.add(new BigDecimal(optionBetAmount));
            }
        }
        Map<String, BigDecimal> map = new HashMap<>();
        System.out.println("赢家" + allWinAmount);
        System.out.println("输家" + allLoseAmount);
        JSONObject data = new JSONObject();
        //免伤金额
        BigDecimal subAmount = BigDecimal.ZERO; // 获胜玩家总投注
        for (String userId : ROOM.getUserBetInfo().keySet()) {
            Map<String, BigDecimal> oneUserbetInfo = ROOM.getUserBetInfo().get(userId);

            for (String s : oneUserbetInfo.keySet()) {
                if (result.contains(s) && lastWeekTopIds.contains(userId)) {
                    // 玩家下的注是输的房间 判断是否是免伤玩家  是的话增加免伤金额
                    BigDecimal loseAmount = oneUserbetInfo.get(s);
                    int index = lastWeekTopIds.indexOf(userId);
                    BigDecimal rate = BigDecimal.ZERO;
                    if (index == 0) {
                        rate = new BigDecimal("0.15");
                    } else if (index >= 1 && index <= 5) {
                        rate = new BigDecimal("0.1");
                    } else {
                        rate = new BigDecimal("0.05");
                    }
                    BigDecimal rebate = loseAmount.multiply(rate);
                    subAmount = subAmount.add(rebate);
                }
            }
        }
        //总输家的金额需要扣除掉免伤的金额
        allLoseAmount = allLoseAmount.subtract(subAmount);
        // 赢的房间 开始计算玩家下注所占比例
        for (String userId : ROOM.getUserBetInfo().keySet()) {
            // 获胜玩家
            Map<String, BigDecimal> oneUserbetInfo = ROOM.getUserBetInfo().get(userId);
            BigDecimal userAllAmount = BigDecimal.ZERO; // 获胜玩家总投注
            for (String s : oneUserbetInfo.keySet()) {
                if (!result.contains(s)) {
                    // 玩家下的注是赢的房间 统计下注金额
                    userAllAmount = userAllAmount.add(oneUserbetInfo.get(s));
                }
            }
            String myBetRoomId = null;
            for (String room : ROOM.getRoomList().keySet()) {
                if (ROOM.getRoomList().get(room).containsKey(userId)) {
                    myBetRoomId = room;
                    break;
                }
            }
            if (myBetRoomId == null) continue;
            if (userAllAmount.compareTo(BigDecimal.ZERO) == 1) {
                // 大于0 则为获胜
                BigDecimal winAmount = null;
                // 全部输家金额为0 则没有人输 金额就为自己下注金额
                if (allLoseAmount.compareTo(BigDecimal.ZERO) == 0) {
                    winAmount = BigDecimal.ZERO;
                } else {
                    winAmount = allWinAmount.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO
                            : new BigDecimal(userAllAmount.toString()).divide(allWinAmount, 6, BigDecimal.ROUND_DOWN)
                            .multiply(allLoseAmount.multiply(rate))
                            .setScale(2, BigDecimal.ROUND_DOWN);
                }
                JSONObject o = new JSONObject();
                BigDecimal add = winAmount.add(new BigDecimal(userAllAmount.toString()));
                o.put("amount", add);
                o.put("capitalType", CAPITAL_TYPE);
                o.put("orderNo", ROOM.getUserBetOrderInfo().get(userId).get("orderNo"));
                o.put("em", LogCapitalTypeEnum.game_bet_win_dts2.getValue());
                if (!BOT_USER.containsKey(userId)) {
                    data.put(userId, o);
                }
                ROOM.getUserBetOrderInfo().get(userId).put("winAmount",
                        add.toString());

                ROOM.getUserBetOrderInfo().get(userId).put("betAmount", ROOM.getRoomList().get(myBetRoomId).get(userId).getBigDecimal("betAmount").toString());
                map.put(userId, ROOM.getRoomList().get(myBetRoomId).get(userId).getBigDecimal("betAmount"));
                ROOM.getUserBetOrderInfo().get(userId).put("isWin", "1");
                winNumber++;
            } else {
                ROOM.getUserBetOrderInfo().get(userId).put("betAmount", ROOM.getRoomList().get(myBetRoomId).get(userId).getBigDecimal("betAmount").toString());
                map.put(userId, ROOM.getRoomList().get(myBetRoomId).get(userId).getBigDecimal("betAmount"));
                ROOM.getUserBetOrderInfo().get(userId).put("winAmount", BigDecimal.ZERO.toString());
                ROOM.getUserBetOrderInfo().get(userId).put("isWin", "0");
                loseNumber++;
            }

        }
        ROOM.getSettleDate().put("winNumber", winNumber);
        ROOM.getSettleDate().put("loseNumber", loseNumber);
        ROOM.getSettleDate().put("allLoseAmount", allLoseAmount);
        ROOM.getSettleDate().put("roomIds", result);
        JSONObject updateRecord = new JSONObject();
        for (String uid : ROOM.getUserBetInfo().keySet()) {
            JSONObject record = new JSONObject();
            record.put("winAmount", ROOM.getUserBetOrderInfo().get(uid).get("winAmount"));
            record.put("lotteryResult", result);
            BigDecimal betAmount = map.get(uid);
            record.put("betAmount", betAmount);
            record.put("betInfo", ROOM.getUserCheckNum().get(uid));
            record.put("isWin", ROOM.getUserBetOrderInfo().get(uid).get("isWin"));
            if (Integer.parseInt(ROOM.getUserBetOrderInfo().get(uid).get("isWin")) == 0 && lastWeekTopIds.contains(uid)) {
                int index = lastWeekTopIds.indexOf(uid);
                BigDecimal rate = BigDecimal.ZERO;
                if (index == 0) {
                    rate = new BigDecimal("0.15");
                } else if (index >= 1 && index <= 5) {
                    rate = new BigDecimal("0.1");
                } else {
                    rate = new BigDecimal("0.05");
                }
                BigDecimal rebate = betAmount.multiply(rate);
                rankRebate(uid, rebate, ROOM.getUserBetOrderInfo().get(uid).get("orderNo"));
            }
            updateRecord.put(ROOM.getUserBetOrderInfo().get(uid).get("orderNo"), record);
        }
        requsetMangerService.requestManagerBet(data, new Listener() {
            @Override
            public void handle(BaseClientSocket clientSocket, Command command) {
                if (command.isSuccess()) {
                    battleRoyaleRecordService.batchUpdateRecord(updateRecord);
                    // 只有玩家请求链路才回包 机器人/定时器触发时 lotteryCommand 为 null
                    if (lotteryCommand != null) {
                        Executer.response(CommandBuilder.builder(lotteryCommand).success(result).build());
                    }
                } else {
                    STATUS = 0;
                    logger.error("结算失败，本期数据：");
                    logger.info(result);
                    if (lotteryCommand != null) {
                        Executer.response(
                                CommandBuilder.builder(lotteryCommand)
                                        .error(command.getMessage(), command.getData())
                                        .build()
                        );
                    }
                }
            }
        });
    }


    @Transactional
    @ServiceMethod(code = "004", description = "获取统计记录")
    public JSONObject getRecord(BattleRoyaleSocketServer2 adminSocketServer, Command lotteryCommand, JSONObject data) {
        checkNull(data);
        checkNull(data.get("userId"));
        Long userId = data.getLong("userId");
        return battleRoyaleRecordService.buildUnifiedSummary(userId, true);
    }



    @Transactional
    public List<Integer> draw() {
        // 先开奖
        List<Integer> killList;
        killList = getKillList(getResultCount());

        gameLotteryResultService.drawLottery(1L, ROOM.getPeridosNum() == null ? "1" : ROOM.getPeridosNum(),
                String.valueOf(killList), ROOM.getAllBetAmount(), BigDecimal.ZERO, BigDecimal.ONE, ROOM.getBetNum(), 0, 0);
        return killList;
    }

    public static int getResultCount() {
        Random random = new Random();
        int i = random.nextInt(100);
        for (int j = 0; j < RATE_LIST.size(); j++) {
            if (i > RATE_LIST.get(j)) {
                continue;
            }
            return j + 1;
        }
        return 0;
    }

    public void initRealMoney() {
        REAL_ROOM_MONEY.clear();
        for (int i = 0; i < ROOM.getOption(); i++) {
            REAL_ROOM_MONEY.put(String.valueOf(i), BigDecimal.ZERO);
        }

    }

    public static List<Integer> getKillList(int count) {
        List<Integer> killList = new ArrayList<>();
        System.out.println("真实下注：" + REAL_ROOM_MONEY);
        List<Map.Entry<String, BigDecimal>> sortedEntries = REAL_ROOM_MONEY.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toList());
        System.out.println("排序：" + sortedEntries);
        int killRate = random.nextInt(100);
        if (killRate < KILL_RATE && betUser.size() > 0) {
            sortedEntries.forEach(entry ->
                    killList.add(Integer.valueOf(entry.getKey())));
            if (killList.size() > count) {
                List<Integer> list = killList.subList(0, count);
                System.out.println("击杀：" + list);
                return list;
            }
            return killList;
        }

        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < OPTIONS_NUM; i++) {
            list.add(i);
        }
        Collections.shuffle(list);
        for (int i = 0; i < count; i++) {
            killList.add(list.get(i));
        }
        return killList;
    }

    public static void main(String[] args) {
        RATE_LIST.add(5);
        RATE_LIST.add(13);
        RATE_LIST.add(43);
        RATE_LIST.add(73);
        RATE_LIST.add(81);
        RATE_LIST.add(88);
        RATE_LIST.add(95);
        RATE_LIST.add(100);
        List<Integer> list = new ArrayList<>();
        list.add(0);
        list.add(0);
        list.add(0);
        list.add(0);
        list.add(0);
        list.add(0);
        list.add(0);
        list.add(0);
        list.add(0);
        list.add(0);
        for (int i = 0; i < 100; i++) {
            int resultCount = getResultCount();
            List<Integer> killList = getKillList(resultCount);
            System.out.println(killList);
        }
        System.out.println(list);
    }


    public void initGameSetting() {
        logger.info("初始化大逃杀游戏配置");
        Game game = gameService.findGameById(1L);
        if (game != null) {
            GAME_SETTING = JSON.parseObject(game.getGameSetting());
            PEOPLE_NUM = GAME_SETTING.getIntValue("peopleNum");
            MIN_BET = GAME_SETTING.getBigDecimal("minBet");
            MAX_BET = GAME_SETTING.getBigDecimal("maxBet");
            STATUS = game.getStatus();
            OPTIONS_NUM = GAME_SETTING.getIntValue("optionsNum");
            TIME = GAME_SETTING.getIntValue("time");
            CAPITAL_TYPE = GAME_SETTING.getIntValue("capitalType");
            MIN_KILL_COUNT = GAME_SETTING.getIntValue("minKillCount", 2);
            MAX_KILL_COUNT = GAME_SETTING.getIntValue("maxKillCount", 8);

        }
        initRateList();
        logger.info("初始化大逃杀游戏配置完成");
    }

    public void initRateList(){
        RATE_LIST.clear();
        Config configByKey = configService.getConfigByKey(Config.QNYH_RATE);
        if (configByKey != null) {
            String value = configByKey.getValue();
            String[] split = value.split(",");
            for (String s : split) {
                int i = Integer.parseInt(s);
                RATE_LIST.add(i);
            }
        }
    }
    private void appendDts3Info(JSONObject pushItem) {
        if (pushItem == null) {
            return;
        }
        Push.push(PushCode.updateDts3Info, null, pushItem);
    }
    /**
     * 统一房间号归一化：
     * 兼容前端传 0-based（0~N-1）
     * 同时兼容历史 1-based（1~N）
     * 如果 roomId 本身就是 roomList 的 key，直接使用（优先 0-based）
     * 如果不是 key，但 (roomId-1) 是 key，则认为是 1-based 输入，转成 (roomId-1)
     * 兜底返回 "0" 或 roomList 的第一个 key
     */
    private String normalizeRoomId(String roomId, int optionNum) {
        int idx;
        try {
            idx = Integer.parseInt(roomId);
        } catch (Exception ignore) {
            idx = 0;
        }

        try {
            if (ROOM != null && ROOM.getRoomList() != null && !ROOM.getRoomList().isEmpty()) {
                String idxKey = String.valueOf(idx);

                // 0-based：直接命中
                if (ROOM.getRoomList().containsKey(idxKey)) {
                    return idxKey;
                }

                // 1-based：尝试 -1
                if (idx > 0) {
                    String minusOneKey = String.valueOf(idx - 1);
                    if (ROOM.getRoomList().containsKey(minusOneKey)) {
                        return minusOneKey;
                    }
                }

                if (ROOM.getRoomList().containsKey("0")) {
                    return "0";
                }

                // 取第一个 key
                return ROOM.getRoomList().keySet().iterator().next();
            }
        } catch (Exception ignore) {
        }

        if (idx < 0 || optionNum <= 0) {
            return "0";
        }
        if (idx > optionNum - 1) {
            return "0";
        }
        return String.valueOf(idx);
    }


    private void removeUserFromAllRooms(String userId) {
        try {
            for (String rid : ROOM.getRoomList().keySet()) {
                Map<String, JSONObject> m = ROOM.getRoomList().get(rid);
                if (m != null) {
                    m.remove(userId);
                }
            }
        } catch (Exception ignore) {
        }
    }


}

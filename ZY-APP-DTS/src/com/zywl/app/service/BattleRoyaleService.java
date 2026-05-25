package com.zywl.app.service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

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
import com.zywl.app.base.constant.TableNameConstant;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.*;
import com.zywl.app.bean.BattleRoyaleRoom;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.cache.GameCacheService;
import com.zywl.app.defaultx.cache.UserCapitalCacheService;
import com.zywl.app.defaultx.enmus.GameTypeEnum;
import com.zywl.app.defaultx.enmus.LogCapitalTypeEnum;
import com.zywl.app.defaultx.enmus.LotteryGameStatusEnum;
import com.zywl.app.defaultx.enmus.UserCapitalTypeEnum;
import com.zywl.app.defaultx.service.*;
import com.zywl.app.socket.BattleRoyaleSocketServer;
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
/**
 * DTS 小游戏服务 狮子：单杀玩法服）
 * **/
@Service
@ServiceClass(code = "101")
public class BattleRoyaleService extends BaseService {
    /** 当前房间内存态：包含本局状态、期号、下注信息、历史统计等 */
    public static BattleRoyaleRoom ROOM;

    public static int KILL_RATE = 0;

    // 官方盈利保障概率，范围0-100，代表击杀真实金额最大房间的概率百分比
    public static int PROFIT_GUARANTEE_RATE = 0;

    public static JSONObject GAME_SETTING;

    public static int PEOPLE_NUM;

    public static BigDecimal MIN_BET;

    public static BigDecimal MAX_BET;

    public static int STATUS;

    public static int OPTIONS_NUM;

    public static int TIME;

    public static int CAPITAL_TYPE;

    public static ConcurrentHashMap<Long, ConcurrentHashMap<String, Object>> ROOLBACK_MAP = new ConcurrentHashMap<>();

    @Autowired
    private GameLotteryResultService gameLotteryResultService;


    @Autowired
    private GameService gameService;

    @Autowired
    private BattleRoyaleRecordService battleRoyaleRecordService;

    @Autowired
    private LogUserCapitalService logUserCapitalService;

    @Autowired
    private UserCapitalService userCapitalService;

    @Autowired
    private BattleRoyaleService battleRoyaleService;

    @Autowired
    private UserCapitalCacheService userCapitalCacheService;

    @Autowired
    private ConfigService configService;

    @Autowired
    private UserService userService;

    @Autowired
    private BattleRoyaleRequsetMangerService requsetMangerService;

    @Autowired
    private GameCacheService gameCacheService;

    @Autowired
    private DailyTaskProgressService dailyTaskProgressService;

    private static final Object lock = new Object();

    private static final Object betLock = new Object();

    public static Map<String, List<Map<String, String>>> userCapitals = new ConcurrentHashMap<>();

    public static String key = DateUtil.getCurrent5();

    public static Map<String, String> orderMap = new ConcurrentHashMap<>();

    public static Set<String> betUser = new ConcurrentHashSet<>();

    public static Set<String> updateRoomUser = new ConcurrentHashSet<>();

    public static final Map<String, BigDecimal> REAL_ROOM_MONEY = new ConcurrentHashMap<>();

    public static final Map<String, JSONArray> pushArray = new ConcurrentHashMap<>();

    public static Map<String, List<Map<String, String>>> userRankCapitals = new ConcurrentHashMap<>();
    public static String key2 = DateUtil.getCurrent5();
    public static String key3 = DateUtil.getCurrent5();
    private static Random random = new Random();
    public static BigDecimal rate = new BigDecimal("0.9");

    public static Map<String, User> BOT_USER = new ConcurrentHashMap<>();


    public static int NEED_BOT = 0;

    public static List<BigDecimal> BOT_MONEY = new ArrayList<>();

    public static Map<String,BigDecimal> ROOM_MONEY = new ConcurrentHashMap<>();

    public void updateRate(BigDecimal a) {
        rate = a;
    }

    @PostConstruct
    public void _Construct() {
        initGameSetting();
        ROOM = new BattleRoyaleRoom(OPTIONS_NUM);
        initHistoryResult();
        addPushSuport();
        periodsNum();
        requestManagerUpdateCapital();
        logger.info("开始加载人机");
        List<User> bot = userService.findBot();
        bot.forEach(e -> BOT_USER.put(e.getId().toString(), e));
        logger.info("加载人机完成，加载数量：" + BOT_USER.size());
        initBotNeed();     // ✅新增：启动就从DB拿 NEED_BOT
        initBotMoney();    // 下注金额
        initRoomMoney();

        gameAddBot();
    }

    public void initRoomMoney() {
        ROOM_MONEY.clear();
        REAL_ROOM_MONEY.clear();
        for (int i = 0; i < OPTIONS_NUM; i++) {
            ROOM_MONEY.put(i + "", BigDecimal.ZERO);
            REAL_ROOM_MONEY.put(i + "", BigDecimal.ZERO);
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

    public void gameAddBot() {
        new Timer("游戏添加人机").schedule(new TimerTask() {
            public void run() {
                try {
                    if ((ROOM.getStatus() == LotteryGameStatusEnum.ready.getValue() || ROOM.getStatus() == LotteryGameStatusEnum.gaming.getValue()) && NEED_BOT > 1) {
                        //游戏阶段 添加人机
                        int rate = random.nextInt(100);
                        if (rate < NEED_BOT) {
                            User user = getBotUser();
                            int kkk = random.nextInt(100);
                            String roomId ;
                            if (kkk<50){
                                roomId = String.valueOf(random.nextInt(OPTIONS_NUM));
                            }else {
                                roomId = findMinValueKey();
                            }
                            userBetBet(user.getId().toString(), roomId, getBotMoney(), null, null);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }, 0, 100);
    }
    private void initBotNeed() {
        try {
            Config cfg = configService.getConfigByKey(Config.GAME_DTS_NEED_BOT);
            if (cfg != null && cfg.getValue() != null) {
                NEED_BOT = Integer.parseInt(cfg.getValue().trim());
            }
            logger.info("DTS人机 NEED_BOT 初始化为：" + NEED_BOT);
        } catch (Exception e) {
            logger.error("DTS人机 NEED_BOT 初始化失败，使用默认值：" + NEED_BOT, e);
        }
    }
    public static String findMinValueKey() {
        if (ROOM_MONEY.isEmpty()) {
            return null;
        }

        return ROOM_MONEY.entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    public static BigDecimal getBotMoney() {
        Collections.shuffle(BOT_MONEY);
        return BOT_MONEY.get(0);
    }

    public User getBotUser() {
        return getRandomValue(BOT_USER);
    }

    public static <K, V> V getRandomValue(Map<K, V> map) {
        return map.values().stream()
                .skip(new Random().nextInt(map.size()))
                .findFirst().orElse(null);
    }

    public void requestManagerUpdateCapital() {
        new Timer("定时推送manager修改内存数据").schedule(new TimerTask() {
            public void run() {
                try {
                    String oldKey = key;
                    String newKey = DateUtil.getCurrent5();
                    userCapitals.put(newKey, new CopyOnWriteArrayList<>());
                    key = newKey;
                    Thread.sleep(100);
                    List data = userCapitals.remove(oldKey);
                    if (data != null && data.size() > 0) {
                        JSONObject object = new JSONObject();
                        object.put("betArray", data);
                        Executer.request(TargetSocketType.battleRoyale, CommandBuilder.builder().request("200801", object).build(), new RequestManagerListener(null));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }, 0, 1);
        new Timer("定时推送manager修改内存数据2").schedule(new TimerTask() {
            public void run() {
                try {
                    String oldKey = key3;
                    String newKey = DateUtil.getCurrent5();
                    userRankCapitals.put(newKey, new CopyOnWriteArrayList<>());
                    key3 = newKey;
                    Thread.sleep(100);
                    List data = userRankCapitals.remove(oldKey);
                    if (data != null && data.size() > 0) {
                        JSONObject object = new JSONObject();
                        object.put("betArray", data);
                        Executer.request(TargetSocketType.battleRoyale, CommandBuilder.builder().request("200823", object).build(), new RequestManagerListener(null));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }, 0, 1);

        new Timer("定时推送SERVER").schedule(new TimerTask() {
            public void run() {
                try {
                    String oldKey = key2;
                    String newKey = DateUtil.getCurrent5();
                    pushArray.put(newKey, new JSONArray());
                    key2 = newKey;
                    Thread.sleep(100);
                    JSONArray data = pushArray.remove(oldKey);
                    if (data != null && data.size() > 0) {
                        Push.push(PushCode.updateRoomDate, null, data);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }, 0, 1);
    }

    public void addPushSuport() {
        Push.addPushSuport(PushCode.rollbackCapital, new DefaultPushHandler());
        Push.addPushSuport(PushCode.updateUserCapital, new DefaultPushHandler());
        Push.addPushSuport(PushCode.updateRoomDate, new DefaultPushHandler());
        Push.addPushSuport(PushCode.updateGameStatus, new DefaultPushHandler());
    }

    @Transactional
    @ServiceMethod(code = "101", description = "用户加入大逃杀房间")
    public Object jionRoom(BattleRoyaleSocketServer adminSocketServer, Command lotteryCommand, JSONObject data) {
        checkNull(data);
        checkNull(data.get("userId"), data.get("userNo"));
        synchronized (lock) {
            String userId = data.getString("userId");
            JSONObject o = new JSONObject();
            o.put("userId", userId);
            o.put("capitalType", CAPITAL_TYPE);
            o.put("type", 1);
            o.put("server", TargetSocketType.starChange.toString());
            if (!ROOM.getUserBetInfo().containsKey(userId)) {
                Map<String, String> map = new HashedMap<String, String>();
                Map<String, Object> map2 = new HashedMap<String, Object>();
                map.put("userNo", data.getString("userNo"));
                map.put("headImgUrl", String.valueOf(data.getOrDefault("headImgUrl", "")));
                map.put("userName", data.getString("userName"));
                map2.put("userId", String.valueOf(userId));
                map2.put("name", data.getString("userName"));
                ROOM.getLookList().put(String.valueOf(userId), map2);
                ROOM.getPlayers().put(data.getString("userId"), map);
                ROOM.setLookNum(ROOM.getLookNum() + 1);

                // Push.push(PushCode.updateRoomDate, null, ROOM.pushResult(3, userId, null, null));
            }
            System.out.println(123);
            Map<String, Double> lastWeekTopList = gameCacheService.getLastWeekTopList(GameTypeEnum.battleRoyale.getValue(), 10);
            ROOM.setLastWeekTopThree(lastWeekTopList);
        }
        JSONObject result = ROOM.getReturnInfo();
        result.put("gameSetting", GAME_SETTING);
        result.put("capitalType", CAPITAL_TYPE);
        return result;
    }

    @Transactional
    @ServiceMethod(code = "104", description = "用户离开大逃杀房间")
    public Object leaveRoom(BattleRoyaleSocketServer adminSocketServer, Command lotteryCommand, JSONObject data) {
        checkNull(data);
        checkNull(data.get("userId"));
        String userId = data.getString("userId");
        JSONObject pushResult = ROOM.pushResult(2, userId, null, null);
        UserCapital userCapital = userCapitalService.findUserCapitalByUserIdAndCapitalType(Long.parseLong(userId), CAPITAL_TYPE);

        if (!ROOM.getUserBetInfo().containsKey(userId)) {
            ROOM.setLookNum(ROOM.getLookNum() - 1);
        }
        if (userCapital == null) {
            Executer.response(CommandBuilder.builder(lotteryCommand).success(new JSONObject()).build());
        }
        data.put("server", TargetSocketType.battleRoyale.toString());
        data.put("type", 2);
        data.put("capitalType", CAPITAL_TYPE);
        if (!ROOM.getUserBetInfo().containsKey(userId)
                && ROOM.getLookList().containsKey(userId)) {
            ROOM.getPlayers().remove(userId);
            ROOM.getLookList().remove(userId);
            // Push.push(PushCode.updateRoomDate, null, pushResult);
        }
        return new JSONObject();

    }

    @Transactional
    @ServiceMethod(code = "105", description = "用户更换下注房间")
    public JSONObject updateRoom(BattleRoyaleSocketServer adminSocketServer, Command lotteryCommand, JSONObject data) {
        checkNull(data);
        checkNull(data.get("userId"), data.get("userNo"), data.get("userName"));
        if (ROOM.getEndTime() > System.currentTimeMillis() && (ROOM.getEndTime() - System.currentTimeMillis()) < 1000) {
            throwExp("狮子即将到来，停止更换");
        }
        System.out.println(ROOM.getRoomList());
        String userId = data.getString("userId");
        String userName = data.getString("userName");
        String newRoomId = data.getString("bet");
        synchronized (LockUtil.getlock(userId + "bet")) {
            if (ROOM.getRoomList().get(newRoomId).containsKey(userId)) {
                throwExp("已经在该房间啦~");
            }
            ROOM.getUserCheckNum().put(userId, newRoomId);
            JSONObject result = new JSONObject();
            if (!ROOM.getUserBetInfo().containsKey(userId)) {
                throwExp("请求频繁");
            }
            Set<String> roomids = ROOM.getUserBetInfo().get(userId).keySet();
            String roomId = null;
            for (String string : roomids) {
                roomId = string;
            }
            Set<String> strings = ROOM.getRoomList().keySet();
            for (String room : strings) {
                if (ROOM.getRoomList().get(room).containsKey(userId)) {
                    roomId = room;
                    break;
                }
            }
            if (roomId == null) {
                throwExp("更换房间频繁");
            }
            updateRoomUser.add(userId);
            BigDecimal amount = ROOM.getUserBetInfo().get(userId).get(roomId);
            // 移除原本房间的信息
            Map<String, BigDecimal> newRoomBetInfo = new HashMap<String, BigDecimal>();
            newRoomBetInfo.put(newRoomId, amount);
            ROOM.getUserBetInfo().remove(userId);
            ROOM.getUserBetInfo().put(userId, newRoomBetInfo);
            if (!ROOM.getRoomList().get(roomId).containsKey(userId)) {
                throwExp("更换房间频繁");
            }
            ROOM.getRoomList().get(newRoomId).put(userId, ROOM.getRoomList().get(roomId).get(userId));
            ROOM.getRoomList().get(roomId).remove(userId);
            ROOM.getBetOptionsInfo().get(roomId).put("betAmount",
                    (new BigDecimal(ROOM.getBetOptionsInfo().get(roomId).get("betAmount")).subtract(amount)).toString());
            ROOM.getBetOptionsInfo().get(newRoomId).put("betNumber",
                    String.valueOf((Integer.parseInt(ROOM.getBetOptionsInfo().get(newRoomId).get("betNumber")) - 1)));
            ROOM.getBetOptionsInfo().get(newRoomId).put("betAmount",
                    (new BigDecimal(ROOM.getBetOptionsInfo().get(newRoomId).get("betAmount")).add(amount)).toString());
            pushArray.get(key2).add(ROOM.pushResult(1, userId, newRoomId, amount));
            //Push.push(PushCode.updateRoomDate, null, ROOM.pushResult(1, userId, newRoomId, amount));
            ROOM_MONEY.put(roomId,ROOM_MONEY.get(roomId).subtract(amount));
            ROOM_MONEY.put(newRoomId,ROOM_MONEY.get(newRoomId).add(amount));
            updateRoomUser.remove(userId);
            result.put("betAmount",amount);
            result.put("gameId",7);
            result.put("name",userName);
            result.put("roomId",newRoomId);
            result.put("type",1);
            return result;
        }
    }


    public Map<String, String> updateCapital(String userId, BigDecimal amount, String orderNo, Long dataId) {
        userCapitalService.subUserOccupyBalanceByDtsBet(Long.parseLong(userId), amount, CAPITAL_TYPE);
        Map<String, String> myOrder = new HashMap<>();
        myOrder.put("orderNo", orderNo);
        myOrder.put("dataId", String.valueOf(dataId));
        myOrder.put("betAmount", amount.toString());
        myOrder.put("userId", userId);
        List<Map<String, String>> maps = userCapitals.get(key);
        maps.add(myOrder);
        return myOrder;
    }

    public BigDecimal addBet(String userId, String userBet, BigDecimal amount) {
        BigDecimal allAmount = amount;
        // 追加投资
        Map<String, BigDecimal> userBets = ROOM.getUserBetInfo().get(userId);
        userBets.put(userBet, userBets.get(userBet).add(amount));
        ROOM.getUserBetInfo().put(userId, userBets);
        allAmount = allAmount.add(ROOM.getRoomList().get(userBet).get(userId).getBigDecimal("betAmount"));
        ROOM.getRoomList().get(userBet).get(userId).put("betAmount",
                ROOM.getRoomList().get(userBet).get(userId).getBigDecimal("betAmount").add(amount));
        return allAmount;
    }

    public void bet(Map<String, String> myOrder, String userId, String userBet, BigDecimal amount) {
        // 添加订单信息
        ROOM.getUserBetOrderInfo().put(userId, myOrder);
        myOrder.put("isBot", "0");
        if (BOT_USER.containsKey(userId)) {
            myOrder.put("isBot", "1");
        }
        // 下注人数+1 观看人数-1
        ROOM.setBetNum(ROOM.getBetNum() + 1);
        ROOM.setLookNum(ROOM.getLookNum() - 1);
        JSONObject betInfo = new JSONObject();
        betInfo.put("userId", userId);
        if (BOT_USER.containsKey(userId)) {
            betInfo.put("name", BOT_USER.get(userId).getName());
        }else {
            betInfo.put("name", ROOM.getPlayers().get(userId).get("userName"));
        }


        betInfo.put("betAmount", amount);
        ROOM.getRoomList().get(userBet).put(userId, betInfo);
        ROOM.getLookList().remove(userId);
        Map<String, BigDecimal> bets = new HashMap<String, BigDecimal>();
        bets.put(userBet, amount);
        ROOM.getUserBetInfo().put(userId, bets);
    }

    @Transactional
    @ServiceMethod(code = "103", description = "用户下注")
    public JSONObject userBet(BattleRoyaleSocketServer adminSocketServer, Command lotteryCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("userId"), params.get("betAmount"), params.get("bet"));
        String userId = params.getString("userId");
        String userBet = params.getString("bet");
        BigDecimal amount = params.getBigDecimal("betAmount");
        return userBetBet(userId, userBet, amount, lotteryCommand, params);


    }


    public JSONObject userBetBet(String userId, String userBet, BigDecimal amount, Command lotteryCommand, JSONObject params) {
        if (STATUS == 0) {
            throwExp("疯狂的狮子即将维护，暂时不能进行游戏！");
        }
        if (amount == null || amount.compareTo(MIN_BET) < 0 || amount.compareTo(MAX_BET) > 0) {
            throwExp("下注金额必须在 " + MIN_BET.stripTrailingZeros().toPlainString() + " 至 "
                    + MAX_BET.stripTrailingZeros().toPlainString() + " 之间");
        }
        if (ROOM.getStatus() == LotteryGameStatusEnum.settle.getValue()) {
            throwExp("上局结算中");
        }
        if (System.currentTimeMillis() > ROOM.getReadyTime() && (System.currentTimeMillis() - ROOM.getReadyTime()) < 2000) {
            throwExp("上局结算中");
        }
        if (ROOM.getEndTime() != 0L && ROOM.getEndTime() - System.currentTimeMillis() < 2000) {
            throwExp("请下局参与");
        }
        ROOM.getUserCheckNum().put(userId, userBet);
        String periodsNum = ROOM.getPeridosNum();
        if (!BOT_USER.containsKey(userId)) {
            UserCapital userCapital = userCapitalService.findUserCapitalByUserIdAndCapitalType(Long.parseLong(userId),
                    CAPITAL_TYPE);
            if (userCapital == null) {
                throwExp(UserCapitalTypeEnum.getName(CAPITAL_TYPE) + "不足");
            }
            if (!ROOM.getPlayers().containsKey(userId)) {
                throwExp("请重新进入游戏");
            }
            if (Integer.parseInt(userBet) > ROOM.getOption() - 1 || Integer.parseInt(userBet) < 0) {
                throwExp("非法投入");
            }
            /*
             * 下注 需更新下注人数信息
             */
        }
        synchronized (LockUtil.getlock(userId + "bet")) {
            if (!BOT_USER.containsKey(userId)) {
                betUser.add(userId);
            }
            String orderNo = OrderUtil.getOrder5Number();
            JSONObject data = new JSONObject();
            JSONObject info = new JSONObject();
            info.put("amount", amount.negate());
            info.put("capitalType", CAPITAL_TYPE);
            info.put("em", LogCapitalTypeEnum.game_bet.getValue());
            info.put("orderNo", orderNo);
            info.put("periodsNum", ROOM.getPeridosNum());
            info.put("tableName", TableNameConstant.BATTLE_ROYALE_RECORD);
            data.put(userId, info);
            long dataId = 0L;
            if (ROOM.getUserBetInfo().containsKey(userId)) {
                // 追加投资 不需要插入投注记录 修改投注订单即可
                Map<String, String> orderInfo = ROOM.getUserBetOrderInfo().get(userId);
                dataId = Long.parseLong(orderInfo.get("dataId"));
            } else {
                dataId = battleRoyaleRecordService.addBattleRoyaleRecord(Long.parseLong(userId), orderNo,
                        ROOM.getPeridosNum(), userBet, amount);
            }
            Map<String, String> myOrder = new HashMap<>();
            if (!BOT_USER.containsKey(userId)) {
                myOrder = updateCapital(userId, amount, orderNo, dataId);
            }
            try {
                BigDecimal allAmount = amount;
                // 用户下注信息增加
                if (ROOM.getUserBetInfo().containsKey(userId)) {
                    allAmount = addBet(userId, userBet, amount);
                } else {
                    bet(myOrder, userId, userBet, amount);
                }
                // 房间下注信息增加
                ROOM.getBetOptionsInfo().get(userBet).put("betNumber",
                        String.valueOf((Integer.parseInt(ROOM.getBetOptionsInfo().get(userBet).get("betNumber")) + 1)));
                ROOM.getBetOptionsInfo().get(userBet).put("betAmount",
                        (new BigDecimal(ROOM.getBetOptionsInfo().get(userBet).get("betAmount")).add(amount)).toString());
                // 用户信息中资产减少
                ROOM.setAllBetAmount(ROOM.getAllBetAmount().add(amount));
                // 如果房间大于开局人数 则更改房间状态 进入游戏状态
                System.out.println("房间下注人数：" + ROOM.getBetNum());
                synchronized (lock) {
                    if (ROOM.getBetNum() >= PEOPLE_NUM && ROOM.getStatus() == LotteryGameStatusEnum.ready.getValue()) {
                        ROOM.setBeginTime(System.currentTimeMillis());
                        ROOM.setEndTime(DateUtil.getTimeByM(TIME));
                        changeRoomStatus(LotteryGameStatusEnum.gaming.getValue(), lotteryCommand);
                    }
                }
                pushArray.get(key2).add(ROOM.pushResult(1, userId, userBet, allAmount));

                if (!BOT_USER.containsKey(userId)) {
                    try {
                        dailyTaskProgressService.pushDailyTaskByGameId(Long.parseLong(userId), 7);
                    } catch (Exception dailyEx) {
                        logger.error("[DailyTask] uid=" + userId, dailyEx);
                    }
                }
                ROOM_MONEY.put(userBet,ROOM_MONEY.get(userBet).add(amount));
                //Push.push(PushCode.updateRoomDate, null, ROOM.pushResult(1, userId, userBet, allAmount));
                if (!BOT_USER.containsKey(userId)) {
                    REAL_ROOM_MONEY.put(userBet, REAL_ROOM_MONEY.getOrDefault(userBet, BigDecimal.ZERO).add(amount));
                    Executer.response(CommandBuilder.builder(lotteryCommand).success(ROOM.pushResult(1, userId, userBet, allAmount)).build());
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (!BOT_USER.containsKey(userId)) {
                    logger.info(e);
                    Push.push(PushCode.cancelBet, null, params);
                    betUser.remove(userId);
                }
            }
        }
        betUser.remove(userId);
        return new JSONObject();
    }

    /**
     * 初始化最近开奖结果
     */
    public void initHistoryResult() {
        logger.info("更新大逃杀历史开奖结果");
        long time = System.currentTimeMillis();
        List<GameLotteryResult> result100 = gameLotteryResultService.findHistoryResultByGameId(7L, 100);
        List<GameLotteryResult> result16 = gameLotteryResultService.findHistoryResultByGameId(7L, 16);
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
        for (GameLotteryResult gameLotteryResult : result16) {
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
        BattleRoyaleRecord battleRoyaleRecord = battleRoyaleRecordService.findPeriodsNum();
        if (battleRoyaleRecord == null) {
            ROOM.setPeridosNum("1");
        } else {
            ROOM.setPeridosNum(String.valueOf((Long.parseLong(battleRoyaleRecord.getPeriodsNum()) + 1)));
            ROOM.setLastResult(battleRoyaleRecord.getLotteryResult());
        }

        logger.info("初始化大逃杀期数信息完成");
    }

    @Transactional
    public void checkNoPrizeInfo() {
        logger.info("检查是否有未开奖的下注，进行资产回退");
        List<BattleRoyaleRecord> records = battleRoyaleRecordService.findNoPrizeInfo();
        if (records.size() > 0) {
            JSONObject data = new JSONObject();
            ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<String, Object>();
            for (BattleRoyaleRecord record : records) {
                map.put("amount", record.getBetAmount());
                map.put("orderNo", record.getOrderNo());
                map.put("id", record.getId());
                ROOLBACK_MAP.put(record.getUserId(), map);
                data.put(record.getUserId().toString(), record.getBetAmount());
            }
            // 推送到Server 进行资产回退
            Push.push(PushCode.rollbackCapital, null, data);
        }

        logger.info("检查是否有未开奖的下注，进行资产回退完成");

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
        data.put("gameId", GameTypeEnum.battleRoyale.getValue());
        if (ROOM.getStatus() == LotteryGameStatusEnum.ready.getValue()) {
            // 初始化房间信息 更新历史开奖结果
            ROOM.initRoomInfo();
            initRoomMoney();
            initHistoryResult();
            data.put("lookList", new ConcurrentHashMap<String, Map<String, Object>>());
            data.put("roomList", ROOM.getRoomList());
            data.put("status", ROOM.getStatus());
            data.put("periodsNum", ROOM.getPeridosNum());
            data.put("lastResult", ROOM.getLastResult());
            runAfterCommit(() -> Push.push(PushCode.updateGameStatus, null, data));
        } else if (ROOM.getStatus() == LotteryGameStatusEnum.gaming.getValue()) {
            data.put("status", ROOM.getStatus());
            data.put("endTime", ROOM.getEndTime());
            data.put("gameId", GameTypeEnum.battleRoyale.getValue());
            Executer.executeService(new ServiceRunable(logger) {
                public void service() {
                    startGame(lotteryCommand);
                }
            });
            runAfterCommit(() -> Push.push(PushCode.updateGameStatus, null, data));
        } else if (ROOM.getStatus() == LotteryGameStatusEnum.settle.getValue()) {
            String result = battleRoyaleService.draw();
            ROOM.setResult(result);
            ROOM.setLastResult(result);
            battleRoyaleService.settle(String.valueOf(result), lotteryCommand);
            ROOM.setReadyTime(System.currentTimeMillis());
            int status = ROOM.getStatus();
            ConcurrentHashMap<String, Map<String, String>> userBetOrderInfo = ROOM.getUserBetOrderInfo();
            data.put("roomId", result);
            data.put("status", status);
            data.putAll(ROOM.getSettleDate());
            data.put("userSettleInfo", userBetOrderInfo);
            runAfterCommit(() -> Push.push(PushCode.updateGameStatus, null, data));
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

    public void rankRebate(String userId, BigDecimal amount, String orderNo) {
        userCapitalService.addUserBalanceByDtsRank(Long.parseLong(userId), amount, CAPITAL_TYPE);
        Map<String, String> myOrder = new HashMap<>();
        myOrder.put("orderNo", orderNo);
        myOrder.put("betAmount", amount.toString());
        myOrder.put("userId", userId);
        List<Map<String, String>> maps = userRankCapitals.get(key3);
        maps.add(myOrder);
    }

    @Transactional
    public void settle(String result, Command lotteryCommand) {

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
            if (!bet.equals(result)) {
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
                if (result.equals(s) && GameCacheService.LAST_WEEK_USER_IDS.contains(userId)) {
                    // 玩家下的注是输的房间 判断是否是免伤玩家  是的话增加免伤金额
                    BigDecimal loseAmount = oneUserbetInfo.get(s);
                    int index = GameCacheService.LAST_WEEK_USER_IDS.indexOf(userId);
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
                if (!s.equals(result)) {
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
                o.put("em", LogCapitalTypeEnum.game_bet_win.getValue());
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
        List<Integer> array = new ArrayList<>();
        array.add(Integer.valueOf(result));
        ROOM.getSettleDate().put("winNumber", winNumber);
        ROOM.getSettleDate().put("loseNumber", loseNumber);
        ROOM.getSettleDate().put("allLoseAmount", allLoseAmount);
        ROOM.getSettleDate().put("roomIds", array);
        JSONObject updateRecord = new JSONObject();
        for (String uid : ROOM.getUserBetInfo().keySet()) {
            JSONObject record = new JSONObject();
            record.put("winAmount", ROOM.getUserBetOrderInfo().get(uid).get("winAmount"));
            record.put("lotteryResult", result);
            BigDecimal betAmount = map.get(uid);
            record.put("betAmount", betAmount);
            record.put("betInfo", ROOM.getUserCheckNum().get(uid));
            record.put("isWin", ROOM.getUserBetOrderInfo().get(uid).get("isWin"));
            updateRecord.put(ROOM.getUserBetOrderInfo().get(uid).get("orderNo"), record);
            if (Integer.parseInt(ROOM.getUserBetOrderInfo().get(uid).get("isWin")) == 0 && GameCacheService.LAST_WEEK_USER_IDS.contains(uid)) {
                int index = GameCacheService.LAST_WEEK_USER_IDS.indexOf(uid);
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
        }
        requsetMangerService.requestManagerBet(data, new Listener() {
            public void handle(BaseClientSocket clientSocket, Command command) {
                if (command.isSuccess()) {
                    battleRoyaleRecordService.batchUpdateRecord(updateRecord);
                    Executer.response(CommandBuilder.builder(lotteryCommand).success(result).build());
                } else {
                    Executer.response(
                            CommandBuilder.builder(lotteryCommand).error(command.getMessage(), command.getData()).build());
                }
            }
        });


    }


    @Transactional
    @ServiceMethod(code = "004", description = "获取统计记录")
    public JSONObject getRecord(BattleRoyaleSocketServer adminSocketServer, Command lotteryCommand, JSONObject data) {
        checkNull(data);
        checkNull(data.get("userId"));
        Long userId = data.getLong("userId");

        // 1) 我的开奖记录（保持原逻辑）
        List<BattleRoyaleRecord> records = battleRoyaleRecordService.findHistoryRecordByUserId(userId);
        JSONArray myRecord = new JSONArray();
        for (BattleRoyaleRecord record : records) {
            JSONObject obj = new JSONObject();
            obj.put("periodsNum", record.getPeriodsNum());
            obj.put("result", record.getLotteryResult());
            obj.put("myBet", record.getBetInfo());
            obj.put("betAmount", record.getBetAmount());
            obj.put("profit", record.getProfit());
            obj.put("create", record.getCreateTime());
            myRecord.add(obj);
        }

        // 2) DB 权威：最近16期逐期结果 —— 直接作为 result16 返回（关键：必须是16条列表）
        List<GameLotteryResult> db16 = gameLotteryResultService.findHistoryResultByGameId(7L, 16);
        JSONArray result16 = new JSONArray();
        for (GameLotteryResult r : db16) {
            JSONObject o = new JSONObject();
            o.put("periodsNum", r.getPeriodsNum());
            o.put("result", r.getLotteryResult());
            // 如果你确定前端完全不用 createTime，就删掉这一行
            o.put("createTime", r.getCreateTime());
            result16.add(o);
        }
        System.out.println("result16运行结果: " + com.alibaba.fastjson.JSON.toJSONString(result16));
        // 3) 近100期统计 —— 仍然给 result100（Map：0..7 -> 次数，补齐不丢号）
        List<GameLotteryResult> db100 = gameLotteryResultService.findHistoryResultByGameId(7L, 100);
        Map<String, Integer> cnt100 = new HashMap<>();
        for (GameLotteryResult r : db100) {
            String k = String.valueOf(r.getLotteryResult()).trim();
            if (k.isEmpty()) continue;
            cnt100.put(k, cnt100.getOrDefault(k, 0) + 1);
        }

        Map<String, Integer> result100 = new LinkedHashMap<>();
        for (int i = 0; i < OPTIONS_NUM; i++) {
            String k = String.valueOf(i);
            result100.put(k, cnt100.getOrDefault(k, 0));
        }

        JSONObject result = new JSONObject();
        result.put("result16", result16);     // ✅ 现在是“最新16期逐期结果列表”
        result.put("result100", result100);   // ✅ 近100期统计
        result.put("myRecord", myRecord);
        return result;
    }

    /**
     * 兼容解析：
     * - "7" -> [7]
     * - "[4,0]" / "[4, 0]" -> [4,0]
     */
    private List<Integer> parseLotteryNumsCompat(String raw) {
        if (raw == null) return Collections.emptyList();
        String s = raw.trim();
        if (s.isEmpty()) return Collections.emptyList();

        if (s.startsWith("[") && s.endsWith("]")) {
            JSONArray arr = JSON.parseArray(s);
            List<Integer> out = new ArrayList<>(arr.size());
            for (Object o : arr) {
                if (o == null) continue;
                out.add(Integer.parseInt(String.valueOf(o).trim()));
            }
            return out;
        }

        if (s.indexOf(',') >= 0) {
            String[] parts = s.split(",");
            List<Integer> out = new ArrayList<>(parts.length);
            for (String p : parts) {
                String t = p == null ? "" : p.trim();
                if (t.isEmpty()) continue;
                out.add(Integer.parseInt(t));
            }
            return out;
        }

        return Collections.singletonList(Integer.parseInt(s));
    }

    public Integer getNext() {
        return ROOM.getNextResult();
    }

    /**
     * 获取真实玩家金额最大的房间ID
     * @return 房间ID，如果没有数据或所有房间金额都为0则返回null
     */
    private String getMaxRealMoneyRoomId() {
        if (REAL_ROOM_MONEY.isEmpty()) {
            return null;
        }
        // 找到金额最大的房间
        Map.Entry<String, BigDecimal> maxEntry = REAL_ROOM_MONEY.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElse(null);

        // 如果最大金额为0或null，说明没有真实玩家下注，返回null
        if (maxEntry == null || maxEntry.getValue() == null || maxEntry.getValue().compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        return maxEntry.getKey();
    }

    @Transactional
    public String draw() {
        // 先开奖
        Random r = new Random();
        int result;

        // 根据官方盈利保障概率决定开奖方式
        int randomRate = r.nextInt(100); // 生成0-99的随机数

        if (randomRate < KILL_RATE) {
            // 击杀真实玩家金额最大的房间（保障官方盈利）
            String maxRoomId = getMaxRealMoneyRoomId();
            if (maxRoomId != null) {
                result = Integer.parseInt(maxRoomId);
            } else {
                // 如果没有真实玩家数据，则随机开奖
                if (ROOM.getNextResult() != null) {
                    result = ROOM.getNextResult();
                } else {
                    result = r.nextInt(OPTIONS_NUM);
                }
            }
        } else {
            // 随机开奖（原有逻辑）
            if (ROOM.getNextResult() != null) {
                result = ROOM.getNextResult();
            } else {
                result = r.nextInt(OPTIONS_NUM);
            }
        }
        ROOM.setNextResult(r.nextInt(OPTIONS_NUM));
        // 疯狂的狮子：game_id = 7（不要再写死 1）
        gameLotteryResultService.drawLottery(
                7L,
                ROOM.getPeridosNum() == null ? "1" : ROOM.getPeridosNum(),
                String.valueOf(result),
                ROOM.getAllBetAmount(),
                BigDecimal.ZERO,
                BigDecimal.ONE,
                ROOM.getBetNum(),
                0,
                0,
                1
        );
        return String.valueOf(result);
    }

    public void initGameSetting() {
        logger.info("初始化大逃杀游戏配置");
        // 疯狂的狮子：game_id = 7
        Game game = gameService.findGameById(7L);
        if (game != null) {
            GAME_SETTING = JSON.parseObject(game.getGameSetting());
            PEOPLE_NUM = GAME_SETTING.getIntValue("peopleNum");
            MIN_BET = GAME_SETTING.getBigDecimal("minBet");
            MAX_BET = GAME_SETTING.getBigDecimal("maxBet");
            STATUS = game.getStatus();
            OPTIONS_NUM = GAME_SETTING.getIntValue("optionsNum");
            TIME = GAME_SETTING.getIntValue("time");
            CAPITAL_TYPE = GAME_SETTING.getIntValue("capitalType");
        }
        logger.info("初始化大逃杀游戏配置完成");
    }

    public void reloadGameSetting() {
        initGameSetting();
        JSONObject data = ROOM.getReturnInfo();
        data.put("userIds", ROOM.getPlayers().keySet());
        data.put("gameId", GameTypeEnum.battleRoyale.getValue());
        data.put("gameSetting", GAME_SETTING);
        data.put("capitalType", CAPITAL_TYPE);
        data.put("configUpdated", 1);
        Push.push(PushCode.updateGameStatus, null, data);
    }



    private void runAfterCommit(Runnable r) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            r.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

            @Override public void suspend() {}
            @Override public void resume() {}
            @Override public void flush() {}
            @Override public void beforeCommit(boolean readOnly) {}
            @Override public void beforeCompletion() {}
            @Override public void afterCommit() { r.run(); }
            @Override public void afterCompletion(int status) {}

        });
    }
}

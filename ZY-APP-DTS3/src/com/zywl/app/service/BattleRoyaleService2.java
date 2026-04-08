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
import com.zywl.app.base.constant.TableNameConstant;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.ConcurrentHashSet;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.base.util.LockUtil;
import com.zywl.app.base.util.OrderUtil;
import com.zywl.app.bean.BattleRoyaleRoom2;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.cache.GameCacheService;
import com.zywl.app.defaultx.enmus.GameTypeEnum;
import com.zywl.app.defaultx.enmus.LogCapitalTypeEnum;
import com.zywl.app.defaultx.enmus.LotteryGameStatusEnum;
import com.zywl.app.defaultx.enmus.UserCapitalTypeEnum;
import com.zywl.app.defaultx.service.*;
import com.zywl.app.socket.BattleRoyaleSocketServer2;
import com.zywl.app.util.RequestManagerListener;
import org.apache.commons.collections4.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

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

    public static int STATUS;

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
    private DailyTaskProgressService dailyTaskProgressService;

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

    public static Map<String, List<Map<String, String>>> userCapitals = new ConcurrentHashMap<>();

    public static Map<String, List<Map<String, String>>> userRankCapitals = new ConcurrentHashMap<>();
    public static String key = DateUtil.getCurrent5();

    public static Map<String, String> orderMap = new ConcurrentHashMap<>();

    public static Set<String> betUser = new ConcurrentHashSet<>();

    public static Set<String> updateRoomUser = new ConcurrentHashSet<>();

    public static final Map<String, JSONArray> pushArray = new ConcurrentHashMap<>();

    public static String key2 = DateUtil.getCurrent5();

    private static Random random = new Random();

    public static String key3 = DateUtil.getCurrent5();
    public static BigDecimal rate = new BigDecimal("0.9");

    public static final Map<String, BigDecimal> REAL_ROOM_MONEY = new ConcurrentHashMap<>();

    public static Map<String, User> BOT_USER = new ConcurrentHashMap<>();


    public static int NEED_BOT = 0;

    public static int KILL_RATE = 0;

    public static List<BigDecimal> BOT_MONEY = new ArrayList<>();


    public void updateRate(BigDecimal a) {
        rate = a;
    }

    @PostConstruct
    public void _Construct() {
        initGameSetting();

        ROOM = new BattleRoyaleRoom2(OPTIONS_NUM);
        initHistoryResult();
        addPushSuport();
        periodsNum();

        requestManagerUpdateCapital();
        logger.info("开始加载人机");
        List<User> bot = userService.findBot();
        bot.forEach(e -> BOT_USER.put(e.getId().toString(), e));
        logger.info("加载人机完成，加载数量：" + BOT_USER.size());
        initBotNeed();
        gameAddBot();
        initBotMoney();
        initKillRate();
        initRealMoney();
    }

    public void initKillRate() {
        Config config = configService.getConfigByKey(Config.DTS3_KILL_RATE);
        if (config != null) {
            String value = config.getValue();
            KILL_RATE = Integer.parseInt(value);
        }
    }
    public void initBotMoney() {
        BOT_MONEY.clear();
        Config config = configService.getConfigByKey(Config.DTS3_BOT_MONEY);
        if (config == null) {
            return;
        }
        String value = config.getValue();
        String[] split = value.split(",");
        for (String s : split) {
            BOT_MONEY.add(new BigDecimal(s));
        }
    }
    private void initBotNeed() {
        try {
            Config cfg = configService.getConfigByKey(Config.GAME_DTS3_NEED_BOT);
            if (cfg != null && cfg.getValue() != null) {
                NEED_BOT = Integer.parseInt(cfg.getValue().trim());
            }
            logger.info("DTS3人机 NEED_BOT 初始化为：" + NEED_BOT);
        } catch (Exception e) {
            logger.error("DTS3人机 NEED_BOT 初始化失败，使用默认值：" + NEED_BOT, e);
        }
    }
    public void gameAddBot() {
        new Timer("游戏添加人机").schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    // 游戏被关闭/维护时，直接不再加人机，避免刷屏
                    if (STATUS == 0) {
                        return;
                    }

                    // 只有准备中 / 游戏中 才允许加人机
                    if (ROOM.getStatus() != LotteryGameStatusEnum.ready.getValue()
                            && ROOM.getStatus() != LotteryGameStatusEnum.gaming.getValue()) {
                        return;
                    }

                    // 小于等于1 视为关闭
                    if (NEED_BOT <= 1) {
                        return;
                    }

                    // 按概率触发
                    int rate = random.nextInt(100);
                    if (rate >= NEED_BOT) {
                        return;
                    }

                    // 只取“本局还没有下注过”的机器人
                    User user = getCanBetBotUser();
                    if (user == null) {
                        return;
                    }

                    int i = random.nextInt(ROOM.getOption());
                    userBetBet(String.valueOf(user.getId()), String.valueOf(i), getBotMoney(), null, null);

                } catch (Exception e) {
                    logger.error("DTS3人机下注异常", e);
                }
            }
        }, 0, 100);
    }
    private User getCanBetBotUser() {
        if (BOT_USER == null || BOT_USER.isEmpty()) {
            return null;
        }

        List<User> canBetBots = new ArrayList<>();
        for (User user : BOT_USER.values()) {
            if (user == null) {
                continue;
            }

            String botUserId = String.valueOf(user.getId());

            // 只挑选“本局还没下注过”的机器人
            if (!ROOM.getUserBetInfo().containsKey(botUserId)) {
                canBetBots.add(user);
            }
        }

        if (canBetBots.isEmpty()) {
            return null;
        }

        return canBetBots.get(random.nextInt(canBetBots.size()));
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
                        Executer.request(TargetSocketType.battleRoyale2, CommandBuilder.builder().request("200821", object).build(), new RequestManagerListener(null));
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
                        Executer.request(TargetSocketType.dts2, CommandBuilder.builder().request("200822", object).build(), new RequestManagerListener(null));
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
                        Push.push(PushCode.updateDts2Info, null, data);
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
        Push.addPushSuport(PushCode.updateDts2Status, new DefaultPushHandler());
        Push.addPushSuport(PushCode.updateDts2Info, new DefaultPushHandler());
    }

    @Transactional
    @ServiceMethod(code = "101", description = "用户加入大逃杀房间")
    public Object jionRoom(BattleRoyaleSocketServer2 adminSocketServer, Command lotteryCommand, JSONObject data) {
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
                ROOM.setLastWeekTopThree(gameCacheService.getLastWeekTopList(GameTypeEnum.dts2.getValue(), 10));
            }
        }
        return ROOM.getReturnInfo();
    }

    @Transactional
    @ServiceMethod(code = "104", description = "用户离开大逃杀房间")
    public Object leaveRoom(BattleRoyaleSocketServer2 adminSocketServer, Command lotteryCommand, JSONObject data) {
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
            // Push.push(PushCode.updateDts2Info, null, pushResult);
        }
        return new JSONObject();

    }

    @Transactional
    @ServiceMethod(code = "105", description = "用户更换下注房间")
    public JSONObject updateRoom(BattleRoyaleSocketServer2 adminSocketServer, Command lotteryCommand, JSONObject data) {
        checkNull(data);
        checkNull(data.get("userId"), data.get("userNo"), data.get("userName"));
        if (ROOM.getEndTime() > System.currentTimeMillis() && (ROOM.getEndTime() - System.currentTimeMillis()) < 1000) {
            throwExp("火山即将喷发，停止更换岛屿");
        }
        System.out.println(ROOM.getRoomList());
        String userId = data.getString("userId");
        String newRoomId = data.getString("bet");
        if (Integer.parseInt(newRoomId) > ROOM.getOption() - 1 || Integer.parseInt(newRoomId) < 0) {
            throwExp("非法操作");
        }
        synchronized (LockUtil.getlock(userId + "bet")) {
            if (ROOM.getRoomList().get(newRoomId).containsKey(userId)) {
                throwExp("已经在该房间");
            }
            ROOM.getUserCheckNum().put(userId, newRoomId);
            JSONObject result = new JSONObject();
            if (!ROOM.getUserBetInfo().containsKey(userId)) {
                throwExp("频繁更换房间");
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
                throwExp("频繁更换房间");
            }
            updateRoomUser.add(userId);
            BigDecimal amount = ROOM.getUserBetInfo().get(userId).get(roomId);
            // 移除原本房间的信息
            Map<String, BigDecimal> newRoomBetInfo = new HashMap<String, BigDecimal>();
            newRoomBetInfo.put(newRoomId, amount);
            ROOM.getUserBetInfo().remove(userId);
            ROOM.getUserBetInfo().put(userId, newRoomBetInfo);
            if (!ROOM.getRoomList().get(roomId).containsKey(userId)) {
                throwExp("频繁更换房间");
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
            //Push.push(PushCode.updateDts2Info, null, ROOM.pushResult(1, userId, newRoomId, amount));
            updateRoomUser.remove(userId);
            REAL_ROOM_MONEY.put(roomId, REAL_ROOM_MONEY.getOrDefault(roomId, BigDecimal.ZERO).subtract(amount));
            REAL_ROOM_MONEY.put(newRoomId, REAL_ROOM_MONEY.getOrDefault(newRoomId, BigDecimal.ZERO).add(amount));
            result.put("betAmount",amount);
            result.put("gameId",1);
            result.put("name",data.getString("userName"));
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

    public void rankRebate(String userId, BigDecimal amount, String orderNo) {
        userCapitalService.addUserBalanceByDtsRank(Long.parseLong(userId), amount, CAPITAL_TYPE);
        Map<String, String> myOrder = new HashMap<>();
        myOrder.put("orderNo", orderNo);
        myOrder.put("betAmount", amount.toString());
        myOrder.put("userId", userId);
        List<Map<String, String>> maps = userRankCapitals.get(key3);
        maps.add(myOrder);
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
    public Object userBet(BattleRoyaleSocketServer2 adminSocketServer, Command lotteryCommand, JSONObject params) {

        checkNull(params);
        checkNull(params.get("userId"), params.get("betAmount"), params.get("bet"));
        String userId = params.getString("userId");
        String userBet = params.getString("bet");
        BigDecimal amount = params.getBigDecimal("betAmount");
        return userBetBet(userId, userBet, amount, lotteryCommand, params);

    }

    public JSONObject userBetBet(String userId, String userBet, BigDecimal amount, Command lotteryCommand, JSONObject params) {
        if (STATUS == 0) {
            throwExp("消失的兔子即将维护，暂时不能进行游戏！");
        }
        if (ROOM.getStatus() == LotteryGameStatusEnum.settle.getValue()) {
            throwExp("上局结算中,请等待结算完成重新渡劫 ~");
        }
        if (System.currentTimeMillis() > ROOM.getReadyTime()
                && (System.currentTimeMillis() - ROOM.getReadyTime()) < 2000) {
            throwExp("上局结算中,请等待结算完成重新渡劫 ~");
        }
        if (ROOM.getEndTime() != 0L && ROOM.getEndTime() - System.currentTimeMillis() < 2000) {
            throwExp("本局即将结束，请稍后参与 ~");
        }

        logger.error("DTS3下注 userId=" + userId + ", isBot=" + BOT_USER.containsKey(userId));

        ROOM.getUserCheckNum().put(userId, userBet);

        if (!BOT_USER.containsKey(userId)) {
            UserCapital userCapital = userCapitalService.findUserCapitalByUserIdAndCapitalType(
                    Long.parseLong(userId), CAPITAL_TYPE);
            if (userCapital == null) {
                throwExp(UserCapitalTypeEnum.getName(CAPITAL_TYPE) + "不足");
            }
            if (!ROOM.getPlayers().containsKey(userId)) {
                throwExp("请返回大厅后重新进入游戏");
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

            boolean alreadyBet = ROOM.getUserBetInfo().containsKey(userId);
            long dataId = 0L;

            if (alreadyBet) {
                // 已经下注过：真人允许追加；机器人直接跳过，避免重复下注异常
                Map<String, String> orderInfo = ROOM.getUserBetOrderInfo().get(userId);
                if (orderInfo != null && "1".equals(orderInfo.get("isBot"))) {
                    return null;
                }
                dataId = Long.parseLong(orderInfo.get("dataId"));
            } else {
                // 首投：无论真人还是机器人，都插入开奖记录
                dataId = battleRoyaleRecordService.addBattleRoyaleRecord(
                        Long.parseLong(userId), orderNo, ROOM.getPeridosNum(), userBet, amount);
            }

            Map<String, String> myOrder;
            if (!BOT_USER.containsKey(userId)) {
                // 真人：正常扣款并生成订单信息
                myOrder = updateCapital(userId, amount, orderNo, dataId);
            } else {
                // 机器人：不扣真实资产，但必须补齐订单信息，供后续结算更新记录使用
                myOrder = new HashMap<>();
                myOrder.put("orderNo", orderNo);
                myOrder.put("dataId", String.valueOf(dataId));
                myOrder.put("betAmount", amount.toString());
                myOrder.put("userId", userId);
                myOrder.put("isBot", "1");
            }

            try {
                BigDecimal allAmount = amount;

                // 用户下注信息增加
                if (alreadyBet) {
                    allAmount = addBet(userId, userBet, amount);
                } else {
                    bet(myOrder, userId, userBet, amount);
                }

                // 房间下注信息增加
                ROOM.getBetOptionsInfo().get(userBet).put(
                        "betNumber",
                        String.valueOf(Integer.parseInt(
                                ROOM.getBetOptionsInfo().get(userBet).get("betNumber")) + 1)
                );
                ROOM.getBetOptionsInfo().get(userBet).put(
                        "betAmount",
                        new BigDecimal(ROOM.getBetOptionsInfo().get(userBet).get("betAmount"))
                                .add(amount).toString()
                );

                // 房间总下注额增加
                ROOM.setAllBetAmount(ROOM.getAllBetAmount().add(amount));

                // 如果房间大于开局人数 则更改房间状态 进入游戏状态
                System.out.println("房间下注人数：" + ROOM.getBetNum());
                synchronized (lock) {
                    if (ROOM.getBetNum() >= PEOPLE_NUM
                            && ROOM.getStatus() == LotteryGameStatusEnum.ready.getValue()) {
                        ROOM.setBeginTime(System.currentTimeMillis());
                        ROOM.setEndTime(DateUtil.getTimeByM(TIME));
                        changeRoomStatus(LotteryGameStatusEnum.gaming.getValue(), lotteryCommand);
                    }
                }

                pushArray.get(key2).add(ROOM.pushResult(1, userId, userBet, allAmount));

                if (!BOT_USER.containsKey(userId)) {
                    try {
                        dailyTaskProgressService.pushDailyTaskByGameId(Long.parseLong(userId), 1);
                    } catch (Exception dailyEx) {
                        logger.error("[DailyTask] uid=" + userId, dailyEx);
                    }
                }

                //Push.push(PushCode.updateDts2Info, null, ROOM.pushResult(1, userId, userBet, allAmount));
                if (!BOT_USER.containsKey(userId)) {
                    REAL_ROOM_MONEY.put(userBet,
                            REAL_ROOM_MONEY.getOrDefault(userBet, BigDecimal.ZERO).add(amount));
                    Executer.response(
                            CommandBuilder.builder(lotteryCommand)
                                    .success(ROOM.pushResult(1, userId, userBet, allAmount))
                                    .build()
                    );
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

        return new JSONObject();
    }

    /**
     * 初始化最近开奖结果
     */
    public void initHistoryResult() {
        logger.info("更新大逃杀历史开奖结果");
        long time = System.currentTimeMillis();
        List<GameLotteryResult> result100 = gameLotteryResultService.findHistoryResultByGameId(1L, 100);
        List<GameLotteryResult> result16 = gameLotteryResultService.findHistoryResultByGameId(1L, 16);
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

    void periodsNum() {
        logger.info("初始化大逃杀期数信息");
        // 取最新一期
        String lastPeriods = gameLotteryResultService.findLastPeriodsNumByGameId(1L);

        if (lastPeriods == null || lastPeriods.trim().isEmpty()) {
            ROOM.setPeridosNum("1");
        } else {
            ROOM.setPeridosNum(String.valueOf(Long.parseLong(lastPeriods.trim()) + 1));
        }

        logger.info("初始化大逃杀期数信息完成, periodsNum=" + ROOM.getPeridosNum());
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
            runAfterCommit(() -> Push.push(PushCode.updateDts2Status, null, data));
        } else if (ROOM.getStatus() == LotteryGameStatusEnum.gaming.getValue()) {
            data.put("status", ROOM.getStatus());
            data.put("endTime", ROOM.getEndTime());
            data.put("gameId", GameTypeEnum.dts2.getValue());
            Executer.executeService(new ServiceRunable(logger) {
                public void service() {
                    startGame(lotteryCommand);
                }
            });
            runAfterCommit(() -> Push.push(PushCode.updateDts2Status, null, data));
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
            runAfterCommit(() -> Push.push(PushCode.updateDts2Status, null, data));
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
                if (result.contains(s) && GameCacheService.LAST_WEEK_USER_IDS.contains(userId)) {
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
            updateRecord.put(ROOM.getUserBetOrderInfo().get(uid).get("orderNo"), record);
        }

        /**
         * 没有需要给真人结算的钱时，不要去调 manager
         * 直接更新开奖记录并返回成功，避免把 STATUS 误置为 0
         */
        if (data == null || data.isEmpty()) {
            battleRoyaleRecordService.batchUpdateRecord(updateRecord);
            Executer.response(CommandBuilder.builder(lotteryCommand).success(result).build());
            return;
        }

        requsetMangerService.requestManagerBet(data, new Listener() {
            public void handle(BaseClientSocket clientSocket, Command command) {
                if (command.isSuccess()) {
                    battleRoyaleRecordService.batchUpdateRecord(updateRecord);
                    Executer.response(CommandBuilder.builder(lotteryCommand).success(result).build());
                } else {
                    STATUS = 0;
                    logger.error("结算失败，本期数据：");
                    logger.info(result);
                    logger.error("requestManagerBet失败，data=" + data);
                    Executer.response(
                            CommandBuilder.builder(lotteryCommand).error(command.getMessage(), command.getData()).build());
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

        // 1) 我的开奖记录（原逻辑）
        List<BattleRoyale2Record> records = battleRoyaleRecordService.findHistoryRecordByUserId(userId);
        JSONArray myRecord = new JSONArray();
        for (BattleRoyale2Record record : records) {
            JSONObject obj = new JSONObject();
            obj.put("periodsNum", record.getPeriodsNum());
            obj.put("result", record.getLotteryResult()); // 可能是 JSONArray/字符串
            obj.put("myBet", record.getBetInfo());
            obj.put("betAmount", record.getBetAmount());
            obj.put("profit", record.getProfit());
            obj.put("create", record.getCreateTime());
            myRecord.add(obj);
        }

        // 2) DB 权威：最近16条（game_id=1 兔子）
        List<GameLotteryResult> db16 = gameLotteryResultService.findHistoryResultByGameId(1L, 16);

        // 2.1 你要求：result16Group = 16条逐期列表（periodsNum + result数组）
        JSONArray result16Group = new JSONArray();
        for (GameLotteryResult r : db16) {
            JSONObject o = new JSONObject();
            o.put("periodsNum", r.getPeriodsNum());
            o.put("result", parseLotteryResultArrayCompat(r.getLotteryResult())); // ✅始终数组
            result16Group.add(o);
        }

        // 3) 其他字段维持原样（你现在前端主要用 result16Group）
        // 仍然保留旧的统计（可选：如果你后面仍要展示统计）
        // 这里用 ROOM 的历史统计，避免你现有页面别的地方依赖它
        JSONObject history16Result = ROOM.getHistory20Reuslt();
        JSONObject history100Result = ROOM.getHistory100Reuslt();

        Map<String, Integer> his16Info = new HashMap<>();
        for (String raw : history16Result.keySet()) {
            int times = history16Result.getIntValue(raw);
            for (Integer num : parseLotteryResultNumsCompat(raw)) {
                if (num == null) continue;
                String k = String.valueOf(num);
                his16Info.put(k, his16Info.getOrDefault(k, 0) + times);
            }
        }

        Map<String, Integer> his100Info = new HashMap<>();
        for (String raw : history100Result.keySet()) {
            int times = history100Result.getIntValue(raw);
            for (Integer num : parseLotteryResultNumsCompat(raw)) {
                if (num == null) continue;
                String k = String.valueOf(num);
                his100Info.put(k, his100Info.getOrDefault(k, 0) + times);
            }
        }

        // 补齐 0..OPTIONS_NUM-1，避免缺号
        Map<String, Integer> result16 = new LinkedHashMap<>();
        Map<String, Integer> result100 = new LinkedHashMap<>();
        for (int i = 0; i < OPTIONS_NUM; i++) {
            String k = String.valueOf(i);
            result16.put(k, his16Info.getOrDefault(k, 0));
            result100.put(k, his100Info.getOrDefault(k, 0));
        }

        // 你原来还有 result100Group（按击杀个数分组），如果后面还用，保留原逻辑即可
        // JSONObject result100Group = buildKillCountGroup(history100Result, MAX_KILL_COUNT);

        JSONObject result = new JSONObject();
        result.put("result16", result16);
        result.put("result100", result100);
        result.put("result16Group", result16Group);   // ✅你要的新结构
        // result.put("result100Group", result100Group);
        result.put("myRecord", myRecord);
        return result;
    }

    /**
     * 兼容：把 lottery_result 转成 JSONArray（始终数组）
     * - "7" -> ["7"]
     * - "[4, 0]" -> ["4","0"] （保持字符串，前端怎么用随它）
     */
    private JSONArray parseLotteryResultArrayCompat(String raw) {
        JSONArray arr = new JSONArray();
        if (raw == null) return arr;
        String s = raw.trim();
        if (s.isEmpty()) return arr;

        try {
            if (s.startsWith("[") && s.endsWith("]")) {
                JSONArray a = JSON.parseArray(s);
                for (int i = 0; i < a.size(); i++) {
                    arr.add(String.valueOf(a.get(i)).trim());
                }
            } else {
                arr.add(s);
            }
        } catch (Exception e) {
            // 解析失败也别炸接口，兜底当单值
            arr.add(s);
        }
        return arr;
    }

    /**
     * 输入：rawLotteryResult -> times
     * 输出：killCountZeroBased(str) -> JSONArray(房间号)，按频次降序
     *
     * 约定：
     * - key 从 0 开始：0 表示 1杀，1 表示 2杀 ... (k+1)杀
     * - key 不允许断档：始终输出 [0 .. maxKillCount-1]，没有数据则返回空数组
     */
    private JSONObject buildKillCountGroup(JSONObject history, int maxKillCount) {
        // killCountZeroBased -> (roomId -> freq)
        Map<Integer, Map<Integer, Integer>> groupFreq = new HashMap<>();

        for (String raw : history.keySet()) {
            int times = history.getIntValue(raw);
            List<Integer> nums = parseLotteryResultNumsCompat(raw);
            int killCount = (nums == null) ? 0 : nums.size();
            if (killCount <= 0) continue;

            // 关键修复点 1：把 1..N 映射为 0..N-1
            int k0 = killCount - 1;

            // 关键修复点 2：限制最大分组，避免异常数据撑爆 UI
            if (k0 < 0) continue;
            if (maxKillCount > 0 && k0 >= maxKillCount) {
                // 超过最大击杀数的结果，统一归到最后一档（可选：也可以直接 continue 丢弃）
                k0 = maxKillCount - 1;
            }

            Map<Integer, Integer> freq = groupFreq.computeIfAbsent(k0, k -> new HashMap<>());
            for (Integer n : nums) {
                if (n == null) continue;
                freq.put(n, freq.getOrDefault(n, 0) + times);
            }
        }

        // 关键修复点 3：保证输出 key 连续（即使没有数据也输出空数组）
        JSONObject out = new JSONObject();
        int end = Math.max(0, maxKillCount); // maxKillCount=0 时输出空对象
        for (int k0 = 0; k0 < end; k0++) {
            Map<Integer, Integer> freq = groupFreq.get(k0);
            JSONArray arr = new JSONArray();
            if (freq != null && !freq.isEmpty()) {
                List<Map.Entry<Integer, Integer>> sorted = new ArrayList<>(freq.entrySet());
                sorted.sort((a, b) -> {
                    int cmp = Integer.compare(b.getValue(), a.getValue()); // freq desc
                    if (cmp != 0) return cmp;
                    return Integer.compare(a.getKey(), b.getKey());         // id asc
                });
                for (Map.Entry<Integer, Integer> x : sorted) {
                    arr.add(x.getKey());
                }
            }
            out.put(String.valueOf(k0), arr);
        }
        return out;
    }

    private List<Integer> parseLotteryResultNumsCompat(String raw) {
        if (raw == null) return Collections.emptyList();
        String s = raw.trim();
        if (s.isEmpty()) return Collections.emptyList();

        // 标准 JSON 数组
        if (s.startsWith("[") && s.endsWith("]")) {
            JSONArray arr = JSON.parseArray(s);
            List<Integer> out = new ArrayList<>(arr.size());
            for (Object o : arr) {
                if (o == null) continue;
                out.add(Integer.parseInt(String.valueOf(o).trim()));
            }
            return out;
        }

        // 裸数字：7 -> [7]
        // （如果未来出现 "7,8" 这种也能顺手兼容）
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

        // 单值
        return Collections.singletonList(Integer.parseInt(s));
    }


    @Transactional
    public List<Integer> draw() {
        // 先开奖
        List<Integer> killList;
        killList = getKillList(getResultCount());

        gameLotteryResultService.drawLottery(1L, ROOM.getPeridosNum() == null ? "1" : ROOM.getPeridosNum(),
                String.valueOf(killList), ROOM.getAllBetAmount(), BigDecimal.ZERO, BigDecimal.ONE, ROOM.getBetNum(), 0, 0,
                1);
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

        //System.out.println("排序：" + sortedEntries);

        int killRate = random.nextInt(100);
        if (killRate < KILL_RATE && betUser.size() > 0) {
            sortedEntries.forEach(entry -> killList.add(Integer.valueOf(entry.getKey())));
            if (killList.size() > count) {
                List<Integer> list = killList.subList(0, count);
                // System.out.println("击杀：" + list);
                return list;
            }
            return killList;
        }

        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < ROOM.getOption(); i++) {
            list.add(i);
        }
        Collections.shuffle(list);

        for (int i = 0; i < count && i < list.size(); i++) {
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
        Config configByKey = configService.getConfigByKey(Config.SZHT_RATE);
        if (configByKey != null) {
            String value = configByKey.getValue();
            String[] split = value.split(",");
            for (String s : split) {
                int i = Integer.parseInt(s);
                RATE_LIST.add(i);
            }
        }
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

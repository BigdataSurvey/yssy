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
import java.util.concurrent.atomic.AtomicInteger;

@Service
@ServiceClass(code = "101")
public class BattleRoyaleService extends BaseService {

    public static BattleRoyaleRoom ROOM;

    public static JSONObject GAME_SETTING;

    public static int PEOPLE_NUM;

    public static BigDecimal MIN_BET;

    public static BigDecimal MAX_BET;

    public static int STATUS;

    public static int OPTIONS_NUM;

    public static int TIME;

    public static int CAPITAL_TYPE;

    public static ConcurrentHashMap<Long, ConcurrentHashMap<String, Object>> ROOLBACK_MAP = new ConcurrentHashMap<>();

    // 机器人用户池
    public static final Map<String, User> BOT_USER = new ConcurrentHashMap<>();
    // 机器人下注概率（0~100
    public static volatile int NEED_BOT = 0;
    // 机器人下注金额池
    public static final List<BigDecimal> BOT_MONEY = new ArrayList<>();

    private static final Random BOT_RANDOM = new Random();

    @Autowired
    private ConfigService configService;

    @Autowired
    private UserService userService;

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

    private final ConcurrentHashMap<String, AtomicInteger> pendingBetCountByPeriod = new ConcurrentHashMap<>();
    @Autowired
    private BattleRoyaleRequsetMangerService requsetMangerService;

    @Autowired
    private GameCacheService gameCacheService;

    private static final Object lock = new Object();
    private static final Object betLock = new Object();
    public static String key = DateUtil.getCurrent5();
    public static Map<String, String> orderMap = new ConcurrentHashMap<>();
    public static Set<String> betUser = new ConcurrentHashSet<>();
    public static Set<String> updateRoomUser = new ConcurrentHashSet<>();
    public static String key2 = DateUtil.getCurrent5();
    public static String key3 = DateUtil.getCurrent5();
    public static BigDecimal rate = new BigDecimal("0.9");

    private static Map<String, List> userCapitals = new ConcurrentHashMap<>();
    private static Map<String, List> userRankCapitals = new ConcurrentHashMap<>();
    private static Map<String, List> orderArray = new ConcurrentHashMap<>();
    private static Map<String, List> orderArray2 = new ConcurrentHashMap<>();
    private static Map<String, JSONArray> pushArray = new ConcurrentHashMap<>();


    public void updateRate(BigDecimal a){
        rate=a;
    }

    @PostConstruct
    public void _Construct() {
        // 同步游戏配置
        initGameSetting();

        // 初始化大逃杀房间
        ROOM = new BattleRoyaleRoom(OPTIONS_NUM);

        // 初始化期数与历史数据
        periodsNum();
        initHistoryResult();

        // 推送支持
        addPushSuport();

        //启动定时任务：同步资产内存、滚动推送
        requestManagerUpdateCapital();

        // 机器人初始化与启动
        initBotUsers();
        initBotMoney();
        initNeedBot();
        gameAddBot();
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
                        // Push.push(PushCode.updateRoomDate, String.valueOf(GameTypeEnum.battleRoyale.getValue()), data);
                        // condition 置空 确保 SERVER 侧能收到 否则 SERVER condition="" 与 DTS condition="7" 不匹配
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
        Push.addPushSuport(PushCode.updateGameDiyData, new DefaultPushHandler());
    }


    @Transactional
    @ServiceMethod(code = "101", description = "用户加入大逃杀房间")
    public Object jionRoom(BattleRoyaleSocketServer adminSocketServer, Command lotteryCommand, JSONObject data) {
        checkNull(data);
        checkNull(data.get("userId"), data.get("userNo"));

        String userId = data.getString("userId");
        String userNo = data.getString("userNo");
        String userName = data.getString("userName");
        String headImgUrl = String.valueOf(data.getOrDefault("headImgUrl", ""));

        synchronized (lock) {

            // players 信息
            Map<String, String> player = ROOM.getPlayers().get(userId);
            if (player == null) {
                player = new HashedMap<>();
                ROOM.getPlayers().put(userId, player);
            }
            player.put("userNo", userNo);
            player.put("userName", userName);
            player.put("headImgUrl", headImgUrl);

            // 确定 roomId; 如果已下注：使用下注房间;否则;优先用 data.bet ，否则默认 0
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

            // 未下注用户：只进入观战/准备状态
            if (!ROOM.getUserBetInfo().containsKey(userId)) {

                if (!ROOM.getLookList().containsKey(userId)) {
                    Map<String, Object> look = new HashedMap<>();
                    look.put("userId", userId);
                    look.put("name", userName);
                    ROOM.getLookList().put(userId, look);
                    ROOM.setLookNum(ROOM.getLookNum() + 1);
                }

                // 如果以前逻辑写过 roomList，这里清掉
                removeUserFromAllRooms(userId);

                // 记录用户当前所在房间 前端切房/下注时也能拿到用户当前房间概念
                ROOM.getUserCheckNum().put(userId, roomId);

                // 这里不再 appendInfo(pushResult(type=3))  join 不产生 updateRoomDate 推送
            } else {

                // 已下注用户：roomList
                if (!ROOM.getRoomList().containsKey(roomId)) {
                    ROOM.getRoomList().put(roomId, new ConcurrentHashMap<String, JSONObject>());
                }

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
                ROOM.getUserCheckNum().put(userId, roomId);
            }

            // lastWeekTopThree
            Map<String, Double> lastWeekTopList = gameCacheService.getLastWeekTopList(GameTypeEnum.battleRoyale.getValue(), 10);
            ROOM.setLastWeekTopThree(lastWeekTopList);
        }

        JSONObject resp = ROOM.getReturnInfo();

        // join 返回的 roomList 强制为空结构
        try {
            JSONObject emptyRoomList = new JSONObject();
            for (int i = 0; i < ROOM.getOption(); i++) {
                emptyRoomList.put(String.valueOf(i), new JSONObject());
            }
            resp.put("roomList", emptyRoomList);
        } catch (Exception ignore) {
        }

        // 统一补充 summary（近16/近100/总投入总收益等）
        try {
            resp.putAll(battleRoyaleRecordService.buildUnifiedSummary(Long.parseLong(data.getString("userId")), true));
        } catch (Exception ignore) {
        }

        return resp;
    }



    @Transactional
    @ServiceMethod(code = "103", description = "用户下注")
    public Object userBet(BattleRoyaleSocketServer adminSocketServer, Command lotteryCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("userId"), params.get("betAmount"), params.get("bet"));

        String userId = params.getString("userId");
        String userBet = params.getString("bet");
        BigDecimal amount = params.getBigDecimal("betAmount");
        params.put("capitalType", CAPITAL_TYPE);
        if (amount == null) {
            throwExp("下注金额错误");
        }

        return userBetBet(userId, userBet, amount, lotteryCommand, params);
    }

    @Transactional
    @ServiceMethod(code = "104", description = "用户离开大逃杀房间")
    public Object leaveRoom(BattleRoyaleSocketServer adminSocketServer, Command lotteryCommand, JSONObject data) {
        checkNull(data);
        checkNull(data.get("userId"));

        String userId = data.getString("userId");
        JSONObject pushResult = ROOM.pushResult(2, userId, null, null);

        data.put("server", TargetSocketType.battleRoyale.toString());
        data.put("type", 2);
        data.put("capitalType", CAPITAL_TYPE);

        // 只有未下注 + 当前在观战列表 才允许真正离开并产生房间态变更推送
        if (!ROOM.getUserBetInfo().containsKey(userId) && ROOM.getLookList().containsKey(userId)) {

            ROOM.getPlayers().remove(userId);
            ROOM.getLookList().remove(userId);

            if (ROOM.getLookNum() > 0) {
                ROOM.setLookNum(ROOM.getLookNum() - 1);
            }

            // 从 roomList 移除占位用户
            removeUserFromAllRooms(userId);
            ROOM.getUserCheckNum().remove(userId);

            // 离开房间推送变更
            appendInfo(pushResult);
        }

        return new JSONObject();
    }



    @ServiceMethod(code = "105", description = "用户更换下注房间")
    public JSONObject updateRoom(BattleRoyaleSocketServer adminSocketServer, Command lotteryCommand, JSONObject data) {
        checkNull(data);
        checkNull(data.get("userId"), data.get("userNo"), data.get("userName"), data.get("bet"));
        int status = ROOM.getStatus();
        if (ROOM.getEndTime() > System.currentTimeMillis() && (ROOM.getEndTime() - System.currentTimeMillis()) < 1000) {
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

            // 找到用户当前所在房间 roomId
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

                if (!ROOM.getRoomList().get(roomId).containsKey(userId)) {
                    throwExp("更换房间频繁");
                }

                // roomList 迁移
                JSONObject betInfo = ROOM.getRoomList().get(roomId).get(userId);
                ROOM.getRoomList().get(newRoomId).put(userId, betInfo);
                ROOM.getRoomList().get(roomId).remove(userId);

                // betInfo.roomId 同步更新
                if (betInfo != null) {
                    betInfo.put("roomId", newRoomId);
                }

                // 旧房间 betNumber -1，betAmount -amount
                ROOM.getBetOptionsInfo().get(roomId).put("betNumber",
                        String.valueOf(Integer.parseInt(ROOM.getBetOptionsInfo().get(roomId).get("betNumber")) - 1));
                ROOM.getBetOptionsInfo().get(roomId).put("betAmount",
                        new BigDecimal(ROOM.getBetOptionsInfo().get(roomId).get("betAmount")).subtract(amount).toString());

                // 新房间 betNumber +1，betAmount +amount
                ROOM.getBetOptionsInfo().get(newRoomId).put("betNumber",
                        String.valueOf(Integer.parseInt(ROOM.getBetOptionsInfo().get(newRoomId).get("betNumber")) + 1));
                ROOM.getBetOptionsInfo().get(newRoomId).put("betAmount",
                        new BigDecimal(ROOM.getBetOptionsInfo().get(newRoomId).get("betAmount")).add(amount).toString());

                // 推送统一走聚合队列
                appendInfo(ROOM.pushResult(1, userId, newRoomId, amount));

                // 给 004003 响应补 roomId
                JSONObject resp = new JSONObject();
                resp.put("roomId", newRoomId);
                return resp;

            } finally {
                updateRoomUser.remove(userId);
            }
        }
    }


    public Map<String, String> updateCapital(String userId, BigDecimal amount, String orderNo, Long dataId) {
        userCapitalService.subUserOccupyBalanceByDtsBet(Long.parseLong(userId), amount,CAPITAL_TYPE);
        Map<String, String> myOrder = new HashMap<>();
        myOrder.put("orderNo", orderNo);
        myOrder.put("dataId", String.valueOf(dataId));
        myOrder.put("betAmount", amount.toString());
        myOrder.put("userId", userId);
        myOrder.put("capitalType", String.valueOf(CAPITAL_TYPE));
        List<Map<String, String>> maps = userCapitals.get(key);
        maps.add(myOrder);
        return myOrder;
    }

    BigDecimal addBet(String userId, String userBet, BigDecimal amount) {

        Map<String, BigDecimal> userBets = ROOM.getUserBetInfo().get(userId);
        if (userBets == null) {
            userBets = new HashMap<>();
            ROOM.getUserBetInfo().put(userId, userBets);
        }

        BigDecimal old = userBets.get(userBet);
        if (old == null) {
            old = BigDecimal.ZERO;
        }

        BigDecimal allAmount = old.add(amount);
        userBets.put(userBet, allAmount);

        Map<String, JSONObject> roomUsers = ROOM.getRoomList().get(userBet);
        if (roomUsers == null) {
            throwExp("房间不存在:" + userBet);
        }

        JSONObject betInfo = roomUsers.get(userId);

        if (betInfo == null) {
            betInfo = new JSONObject();
            betInfo.put("userId", userId);
            betInfo.put("roomId", userBet);

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
        }

        betInfo.put("betAmount", allAmount);

        roomUsers.put(userId, betInfo);

        return allAmount;
    }



    public void bet(Map<String, String> myOrder, String userId, String userBet, BigDecimal amount) {
        // 添加订单信息
        ROOM.getUserBetOrderInfo().put(userId, myOrder);
        ROOM.setBetNum(ROOM.getBetNum() + 1);

        // 标记机器人
        myOrder.put("isBot", "0");
        if (BOT_USER.containsKey(userId)) {
            myOrder.put("isBot", "1");
        }

        // 如果用户处于观战列表，则观战人数-1，并移除 lookList
        if (ROOM.getLookList().containsKey(userId)) {
            ROOM.setLookNum(Math.max(ROOM.getLookNum() - 1, 0));
            ROOM.getLookList().remove(userId);
        }

        // 首次下注前：先从所有房间移除“占位用户”，避免 roomList 同一用户出现在多个房间
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



    public JSONObject userBetBet(String userId, String userBet, BigDecimal amount, Command lotteryCommand, JSONObject params) {
        if (STATUS == 0) {
            throwExp("游戏维护中，暂时不能进行游戏！");
        }
        if (ROOM.getStatus() == LotteryGameStatusEnum.settle.getValue()) {
            throwExp("上局结算中,请等待结算完成重新开始 ~");
        }
        if (System.currentTimeMillis() > ROOM.getReadyTime() && (System.currentTimeMillis() - ROOM.getReadyTime()) < 2000) {
            throwExp("上局结算中,请等待结算完成重新开始 ~");
        }
        if (ROOM.getEndTime() != 0L && ROOM.getEndTime() - System.currentTimeMillis() < 2000) {
            throwExp("本局即将结束，请稍后参与 ~");
        }

        // 非机器人
        if (!BOT_USER.containsKey(userId)) {
            if (!ROOM.getPlayers().containsKey(userId)) {
                throwExp("请返回大厅后重新进入游戏");
            }
        }

        ROOM.getUserCheckNum().put(userId, userBet);

        UserCapital userCapital = userCapitalService.findUserCapitalByUserIdAndCapitalType(Long.parseLong(userId), CAPITAL_TYPE);
        if (userCapital == null) {
            throwExp("资产不存在");
        }
        BigDecimal balance = userCapital.getBalance();
        if (balance.compareTo(amount) < 0) {
            throwExp("资产不足");
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
                // 下注记录
                String orderNo = OrderUtil.getOrder5Number();
                boolean firstBetThisRoom = true;
                Map<String, BigDecimal> userBets = ROOM.getUserBetInfo().get(userId);
                if (userBets != null && userBets.containsKey(userBet)) {
                    firstBetThisRoom = false;
                }

                // 扣除资产 + 下注记录
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

                // 机器人也扣资产保证测试真实
                myOrder = updateCapital(userId, amount, orderNo, dataId);

                // 下注入内存
                BigDecimal allAmount = amount;
                if (ROOM.getUserBetInfo().containsKey(userId)) {
                    allAmount = addBet(userId, userBet, amount);
                } else {
                    myOrder.put("orderNo", orderNo);
                    myOrder.put("betAmount", amount.toString());
                    myOrder.put("userId", userId);
                    myOrder.put("dataId", String.valueOf(dataId));
                    bet(myOrder, userId, userBet, amount);
                }

                // 房间下注信息增加
                if (firstBetThisRoom) {
                    ROOM.getBetOptionsInfo().get(userBet).put("betNumber",
                            String.valueOf((Integer.parseInt(ROOM.getBetOptionsInfo().get(userBet).get("betNumber")) + 1)));
                }

                ROOM.getBetOptionsInfo().get(userBet).put("betAmount",
                        (new BigDecimal(ROOM.getBetOptionsInfo().get(userBet).get("betAmount")).add(amount)).toString());

                ROOM.setAllBetAmount(ROOM.getAllBetAmount().add(amount));

                // 满足开局人数
                synchronized (lock) {
                    if (ROOM.getBetNum() >= PEOPLE_NUM && ROOM.getStatus() == LotteryGameStatusEnum.ready.getValue()) {
                        ROOM.setBeginTime(System.currentTimeMillis());
                        ROOM.setEndTime(DateUtil.getTimeByM(TIME));
                        changeRoomStatus(LotteryGameStatusEnum.gaming.getValue(), lotteryCommand);
                    }
                }

                // 推送下注信息
                appendInfo(ROOM.pushResult(1, userId, userBet, allAmount));
                // 仅当是正常玩家请求
                if (lotteryCommand != null) {
                    Executer.response(CommandBuilder.builder(lotteryCommand).success(ROOM.pushResult(1, userId, userBet, allAmount)).build());
                }
            } catch (Exception e) {
                e.printStackTrace();
                logger.info(e);

                if (params != null) {
                    Push.push(PushCode.cancelBet, null, params);
                }
            } finally {
                betUser.remove(userId);
            }
        }
        return new JSONObject();
    }

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
            logger.info("[DTS-7] 加载机器人完成，数量=" + BOT_USER.size());
        } catch (Exception e) {
            logger.error("[DTS-7] 加载机器人异常", e);
        }
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

    private void initNeedBot() {
        try {
            Config cfg = configService.getConfigByKey(Config.GAME_DTS2_NEED_BOT);
            if (cfg == null || cfg.getValue() == null || cfg.getValue().trim().isEmpty()) {
                NEED_BOT = 0;
                return;
            }
            NEED_BOT = Integer.parseInt(cfg.getValue().trim());
            logger.info("[DTS-7] 初始化 NEED_BOT=" + NEED_BOT + " (key=" + Config.GAME_DTS2_NEED_BOT + ")");
        } catch (Exception e) {
            NEED_BOT = 0;
            logger.error("[DTS-7] 初始化 NEED_BOT 异常", e);
        }
    }

    private void initBotMoney() {
        BOT_MONEY.clear();
        try {
            Config cfg = configService.getConfigByKey(Config.DTS_BOT_MONEY);
            if (cfg == null || cfg.getValue() == null || cfg.getValue().trim().isEmpty()) {
                // 兜底
                BOT_MONEY.add(new BigDecimal("1"));
                BOT_MONEY.add(new BigDecimal("10"));
                BOT_MONEY.add(new BigDecimal("100"));
                return;
            }
            String[] split = cfg.getValue().split(",");
            for (String s : split) {
                if (s != null && !s.trim().isEmpty()) {
                    BOT_MONEY.add(new BigDecimal(s.trim()));
                }
            }
            if (BOT_MONEY.isEmpty()) {
                BOT_MONEY.add(new BigDecimal("1"));
                BOT_MONEY.add(new BigDecimal("10"));
                BOT_MONEY.add(new BigDecimal("100"));
            }
            logger.info("[DTS-7] 初始化 BOT_MONEY=" + BOT_MONEY);
        } catch (Exception e) {
            BOT_MONEY.clear();
            BOT_MONEY.add(new BigDecimal("1"));
            BOT_MONEY.add(new BigDecimal("10"));
            BOT_MONEY.add(new BigDecimal("100"));
            logger.error("[DTS-7] 初始化 BOT_MONEY 异常", e);
        }
    }

    private static BigDecimal getBotMoney() {
        if (BOT_MONEY.isEmpty()) {
            return BigDecimal.ONE;
        }
        Collections.shuffle(BOT_MONEY);
        return BOT_MONEY.get(0);
    }

    private User getBotUser() {
        return getRandomValue(BOT_USER);
    }

    private static <K, V> V getRandomValue(Map<K, V> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        int randomIndex = java.util.concurrent.ThreadLocalRandom.current().nextInt(map.size());
        return map.values().stream().skip(randomIndex).findFirst().orElse(null);
    }

    /**
     * 机器人定时下注
     */
    private void gameAddBot() {
        new Timer("DTS-7-robot-bet").schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    if (NEED_BOT <= 0) {
                        return;
                    }
                    int status = ROOM.getStatus();
                    if (status != LotteryGameStatusEnum.ready.getValue() && status != LotteryGameStatusEnum.gaming.getValue()) {
                        return;
                    }

                    // 概率触发
                    int rate = BOT_RANDOM.nextInt(100);
                    if (rate >= NEED_BOT) {
                        return;
                    }

                    User bot = getBotUser();
                    if (bot == null) {
                        return;
                    }

                    // 选一个随机房间
                    int opt = BOT_RANDOM.nextInt(OPTIONS_NUM);
                    String betOpt = String.valueOf(opt);

                    // 直接走 userBetBet
                    userBetBet(String.valueOf(bot.getId()), betOpt, getBotMoney(), null, null);

                } catch (Exception e) {
                    // 机器人异常不影响主流程
                    logger.error("[DTS-7] robot bet error", e);
                }
            }
        }, 300, 100);
    }
    private void appendInfo(JSONObject pushItem) {
        if (pushItem == null) return;
        String k = key2;
        pushArray.computeIfAbsent(k, kk -> new JSONArray()).add(pushItem);
    }

    /**
     * 初始化最近开奖结果
     */
    public void initHistoryResult() {
        logger.info("更新大逃杀历史开奖结果");
        long time = System.currentTimeMillis();
        List<GameLotteryResult> result100 = gameLotteryResultService.findHistoryResultByGameId(7L, 100);
        List<GameLotteryResult> result20 = gameLotteryResultService.findHistoryResultByGameId(7L, 20);
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

        com.alibaba.fastjson.JSONObject data = new com.alibaba.fastjson.JSONObject();
        data.put("userIds", ROOM.getPlayers().keySet());
        data.put("gameId", GameTypeEnum.battleRoyale.getValue());

        if (ROOM.getStatus() == LotteryGameStatusEnum.ready.getValue()) {

            ROOM.initRoomInfo();
            initHistoryResult();

            data.put("lookList", new ConcurrentHashMap<String, Map<String, Object>>());
            data.put("roomList", ROOM.getRoomList());
            data.put("status", ROOM.getStatus());
            data.put("periodsNum", ROOM.getPeridosNum());
            data.put("lastResult", ROOM.getLastResult());

            Push.push(PushCode.updateGameStatus, null, data);

        } else if (ROOM.getStatus() == LotteryGameStatusEnum.gaming.getValue()) {

            data.put("status", ROOM.getStatus());
            data.put("endTime", ROOM.getEndTime());
            data.put("gameId", GameTypeEnum.battleRoyale.getValue());

            Executer.executeService(new ServiceRunable(logger) {
                public void service() {
                    startGame(lotteryCommand);
                }
            });

            Push.push(PushCode.updateGameStatus, null, data);

        } else if (ROOM.getStatus() == LotteryGameStatusEnum.settle.getValue()) {

            String result = battleRoyaleService.draw();
            ROOM.setResult(result);
            ROOM.setLastResult(result);

            // 结算（这里会把 ROOM.getUserBetOrderInfo() 填好 winAmount/betAmount/isWin 等）
            battleRoyaleService.settle(String.valueOf(result), lotteryCommand);

            ROOM.setReadyTime(System.currentTimeMillis());

            int status = ROOM.getStatus();
            ConcurrentHashMap<String, Map<String, String>> userBetOrderInfo = ROOM.getUserBetOrderInfo();

            // ===== 构建“公共部分” =====
            com.alibaba.fastjson.JSONObject base = new com.alibaba.fastjson.JSONObject();
            base.put("roomId", result);
            base.put("status", status);
            base.put("gameId", GameTypeEnum.battleRoyale.getValue());
            base.putAll(ROOM.getSettleDate()); // 你原来的：winNumber/loseNumber/allLoseAmount/roomIds 等

            base.put("userSettleInfo", userBetOrderInfo);

            // userSettleList（你原来的列表结构，保留）
            com.alibaba.fastjson.JSONArray userSettleList = new com.alibaba.fastjson.JSONArray();
            if (userBetOrderInfo != null) {
                for (String uid : userBetOrderInfo.keySet()) {
                    com.alibaba.fastjson.JSONObject one = new com.alibaba.fastjson.JSONObject();
                    one.put("userId", uid);
                    Map<String, String> si = userBetOrderInfo.get(uid);
                    if (si != null) one.putAll(si);
                    userSettleList.add(one);
                }
            }
            base.put("userSettleList", userSettleList);

            // userRecordSummaryMap（你原来的：包含 totalInvest/totalGain 历史汇总，保留）
            com.alibaba.fastjson.JSONObject userRecordSummaryMap = new com.alibaba.fastjson.JSONObject();
            try {
                if (userBetOrderInfo != null) {
                    for (String uid : userBetOrderInfo.keySet()) {
                        BigDecimal extraGain = null;
                        try {
                            Map<String, String> si = userBetOrderInfo.get(uid);
                            if (si != null && si.get("winAmount") != null) {
                                extraGain = new BigDecimal(si.get("winAmount"));
                            }
                        } catch (Exception ignore) { }
                        try {
                            userRecordSummaryMap.put(uid,
                                    battleRoyaleRecordService.buildUnifiedSummary(Long.valueOf(uid), true, extraGain));
                        } catch (Exception ignore) { }
                    }
                }
            } catch (Exception ignore) { }
            base.put("userRecordSummaryMap", userRecordSummaryMap);

            // ===== ✅关键改动：按 userId 拆包单播，顶层补齐 totalGain =====
            if (ROOM.getPlayers() != null && ROOM.getPlayers().keySet() != null) {
                for (String uid : ROOM.getPlayers().keySet()) {

                    com.alibaba.fastjson.JSONObject onePayload = new com.alibaba.fastjson.JSONObject();
                    onePayload.putAll(base);

                    // 单人结算字段：顶层 totalGain/totalInvest + 兼容字段
                    onePayload.putAll(buildUserSettlePayload(uid, GameTypeEnum.battleRoyale.getValue()));

                    // ✅让 Push 层明确只推给这个人
                    onePayload.put("userIds", java.util.Collections.singleton(uid));

                    Push.push(PushCode.updateGameStatus, null, onePayload);
                }
            } else {
                // 兜底：如果 players 异常，仍然广播一次（避免直接没推送）
                Push.push(PushCode.updateGameStatus, null, base);
            }

            // 回到 ready
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
        userCapitalService.addUserBalanceByDtsRank(Long.parseLong(userId), amount,CAPITAL_TYPE);
        Map<String, String> myOrder = new HashMap<>();
        myOrder.put("orderNo", orderNo);
        myOrder.put("betAmount", amount.toString());
        myOrder.put("userId", userId);
        myOrder.put("capitalType", String.valueOf(CAPITAL_TYPE));
        List<Map<String, String>> maps = userRankCapitals.get(key3);
        maps.add(myOrder);
    }

    @Transactional
    public void settle(String result, Command lotteryCommand) {
        List<String> lastWeekTopIds = GameCacheService.getLastWeekTopUserIds(GameTypeEnum.battleRoyale.getValue());
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

        // 免伤金额
        BigDecimal subAmount = BigDecimal.ZERO;
        for (String userId : ROOM.getUserBetInfo().keySet()) {
            Map<String, BigDecimal> oneUserbetInfo = ROOM.getUserBetInfo().get(userId);
            for (String s : oneUserbetInfo.keySet()) {
                if (result.equals(s) && lastWeekTopIds.contains(userId)) {
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

        // 总输家的金额需要扣除掉免伤的金额
        allLoseAmount = allLoseAmount.subtract(subAmount);

        // 赢的房间 开始计算玩家下注所占比例
        for (String userId : ROOM.getUserBetInfo().keySet()) {

            Map<String, BigDecimal> oneUserbetInfo = ROOM.getUserBetInfo().get(userId);

            BigDecimal userAllAmount = BigDecimal.ZERO; // 获胜玩家总投注（下注在非result房间）
            for (String s : oneUserbetInfo.keySet()) {
                if (!s.equals(result)) {
                    userAllAmount = userAllAmount.add(oneUserbetInfo.get(s));
                }
            }

            // 找到玩家所在房间（ROOM.roomList: roomId -> (userId -> betInfo)）
            String myBetRoomId = null;
            for (String room : ROOM.getRoomList().keySet()) {
                if (ROOM.getRoomList().get(room).containsKey(userId)) {
                    myBetRoomId = room;
                    break;
                }
            }
            if (myBetRoomId == null) {
                continue;
            }

            // 统一拿本房间该用户下注金额（用于记录/展示）
            BigDecimal betAmountInRoom = ROOM.getRoomList().get(myBetRoomId).get(userId).getBigDecimal("betAmount");
            if (betAmountInRoom == null) betAmountInRoom = BigDecimal.ZERO;
            String betAmountStr = betAmountInRoom.stripTrailingZeros().toPlainString();

            // 先把 betAmount 写入 userBetOrderInfo，保证结算页/记录页都有
            ROOM.getUserBetOrderInfo().get(userId).put("betAmount", betAmountStr);
            map.put(userId, betAmountInRoom);

            if (userAllAmount.compareTo(BigDecimal.ZERO) == 1) {
                // 大于0 则为获胜（下注在非result房间）
                BigDecimal winAmount;
                if (allLoseAmount.compareTo(BigDecimal.ZERO) == 0) {
                    winAmount = BigDecimal.ZERO;
                } else {
                    winAmount = allWinAmount.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO
                            : new BigDecimal(userAllAmount.toString()).divide(allWinAmount, 6, BigDecimal.ROUND_DOWN)
                            .multiply(allLoseAmount.multiply(rate))
                            .setScale(2, BigDecimal.ROUND_DOWN);
                }

                // 最终可获得 = 自己获胜房间下注总额 + 分红
                BigDecimal add = winAmount.add(new BigDecimal(userAllAmount.toString()));
                String addStr = add.stripTrailingZeros().toPlainString();

                // 给 manager 记账/加钱
                JSONObject o = new JSONObject();
                o.put("amount", add); // 注意：这里仍然给 BigDecimal，给 manager 处理
                o.put("capitalType", CAPITAL_TYPE);
                o.put("orderNo", ROOM.getUserBetOrderInfo().get(userId).get("orderNo"));
                o.put("em", LogCapitalTypeEnum.game_bet_win.getValue());
                data.put(userId, o);

                // ✅【关键】结算展示字段：不管前端取哪个都不会 undefined
                Map<String, String> settleMap = ROOM.getUserBetOrderInfo().get(userId);
                settleMap.put("winAmount", addStr);   // 推荐字段
                // ✅ 统一字段：前端结算页用 totalGain/totalInvest（截图中“获得”取 totalGain）
                settleMap.put("totalGain", addStr);
                settleMap.put("totalInvest", betAmountStr);
                settleMap.put("amount", addStr);      // 兼容：很多前端直接取 amount
                settleMap.put("award", addStr);       // 兼容
                settleMap.put("gain", addStr);        // 兼容
                settleMap.put("getAmount", addStr);   // 兼容
                settleMap.put("isWin", "1");

                winNumber++;

            } else {
                // 输家：获得为 0，同样补齐字段避免 undefined
                Map<String, String> settleMap = ROOM.getUserBetOrderInfo().get(userId);

                String zero = "0";
                settleMap.put("winAmount", zero);
                settleMap.put("totalGain", zero);
                settleMap.put("totalInvest", betAmountStr);
                settleMap.put("amount", zero);
                settleMap.put("award", zero);
                settleMap.put("gain", zero);
                settleMap.put("getAmount", zero);
                settleMap.put("isWin", "0");

                loseNumber++;
            }
        }

        List<String> array = new ArrayList<>();
        array.add(result);

        ROOM.getSettleDate().put("winNumber", winNumber);
        ROOM.getSettleDate().put("loseNumber", loseNumber);
        ROOM.getSettleDate().put("allLoseAmount", allLoseAmount);
        ROOM.getSettleDate().put("roomIds", array);

        // ✅ 可选：顺便补齐全局字段（如果前端结算页用 totalGain/totalInvest）
        // 不影响现有逻辑，只是让字段更完整
        try {
            ROOM.getSettleDate().put("totalInvest", ROOM.getAllBetAmount() == null ? "0"
                    : ROOM.getAllBetAmount().stripTrailingZeros().toPlainString());
        } catch (Exception ignore) {}
        try {
            ROOM.getSettleDate().put("totalGain", allWinAmount == null ? "0"
                    : allWinAmount.stripTrailingZeros().toPlainString());
        } catch (Exception ignore) {}

        JSONObject updateRecord = new JSONObject();
        for (String uid : ROOM.getUserBetInfo().keySet()) {

            JSONObject record = new JSONObject();

            // ✅ record 里同样补齐：避免前端从记录结构取“获得”也 undefined
            String winStr = ROOM.getUserBetOrderInfo().get(uid).get("winAmount");
            if (winStr == null) winStr = "0";

            record.put("winAmount", winStr);
            // ✅ 统一字段：记录页/结算页前端如果取 totalGain/totalInvest，也不会 undefined
            record.put("totalGain", winStr);
            record.put("amount", winStr);
            record.put("award", winStr);
            record.put("gain", winStr);
            record.put("getAmount", winStr);

            record.put("lotteryResult", result);

            BigDecimal betAmount = map.get(uid);
            record.put("betAmount", betAmount);
            record.put("totalInvest", betAmount);

            record.put("betInfo", ROOM.getUserCheckNum().get(uid));
            record.put("isWin", ROOM.getUserBetOrderInfo().get(uid).get("isWin"));

            updateRecord.put(ROOM.getUserBetOrderInfo().get(uid).get("orderNo"), record);

            // 免伤返利（你原逻辑保留）
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
        }

        requsetMangerService.requestManagerBet(data, new Listener() {
            public void handle(BaseClientSocket clientSocket, Command command) {
                if (command.isSuccess()) {
                    battleRoyaleRecordService.batchUpdateRecord(updateRecord);
                    Executer.response(CommandBuilder.builder(lotteryCommand).success(result).build());
                } else {
                    Executer.response(CommandBuilder.builder(lotteryCommand).error(command.getMessage(), command.getData()).build());
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
        return battleRoyaleRecordService.buildUnifiedSummary(userId, true);
    }

    public Integer getNext(){
        return ROOM.getNextResult();
    }
    @Transactional
    public String draw() {
        // 先开奖
        Random r = new Random();
        int result;
        if (ROOM.getNextResult()!=null){
            result = ROOM.getNextResult();
        }else{
            result = r.nextInt(OPTIONS_NUM);
        }
        ROOM.setNextResult(r.nextInt(OPTIONS_NUM));
        gameLotteryResultService.drawLottery(7L, ROOM.getPeridosNum() == null ? "1" : ROOM.getPeridosNum(),
                String.valueOf(result), ROOM.getAllBetAmount(), BigDecimal.ZERO, BigDecimal.ONE, ROOM.getBetNum(), 0, 0);
        return String.valueOf(result);
    }

    public void initGameSetting() {
        logger.info("初始化大逃杀游戏配置");
        Long gid = Long.valueOf(GameTypeEnum.battleRoyale.getValue());
        Game game = gameService.findGameById(gid);

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

    /**
     * 给“某一个用户”构建结算页 payload（用于 updateGameStatus 单播）
     * 重点：顶层补齐 totalGain / totalInvest，前端直接取 totalGain 不会再 undefined
     */
    private com.alibaba.fastjson.JSONObject buildUserSettlePayload(String userId, int gameId) {

        com.alibaba.fastjson.JSONObject payload = new com.alibaba.fastjson.JSONObject();

        payload.put("userId", userId);
        payload.put("gameId", gameId);

        // userSettleInfo 里你本来就有 betAmount / winAmount / isWin / orderNo 等信息
        Map<String, String> one = ROOM.getUserBetOrderInfo() == null ? null : ROOM.getUserBetOrderInfo().get(userId);

        String betAmountStr = "0";
        String winAmountStr = "0";
        String isWinStr = "0";
        String orderNo = null;

        if (one != null) {
            if (one.get("betAmount") != null) betAmountStr = one.get("betAmount");
            if (one.get("winAmount") != null) winAmountStr = one.get("winAmount");
            if (one.get("isWin") != null) isWinStr = one.get("isWin");
            if (one.get("orderNo") != null) orderNo = one.get("orderNo");
        }

        payload.put("betAmount", betAmountStr);      // 本局投入（单人）
        payload.put("totalInvest", betAmountStr);    // ✅ 顶层：前端要投入也能直接取
        payload.put("winAmount", winAmountStr);      // 本局获得（单人）
        payload.put("totalGain", winAmountStr);      // ✅ 顶层：前端说取 totalGain，就给它

        payload.put("isWin", isWinStr);
        if (orderNo != null) payload.put("orderNo", orderNo);

        // 兼容字段（如果前端有人写死 award/gain/amount/getAmount 也不会 undefined）
        payload.put("award", winAmountStr);
        payload.put("gain", winAmountStr);
        payload.put("amount", winAmountStr);
        payload.put("getAmount", winAmountStr);

        return payload;
    }



}

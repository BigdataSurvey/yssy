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
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.base.util.LockUtil;
import com.zywl.app.base.util.OrderUtil;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.enmus.GameTypeEnum;
import com.zywl.app.defaultx.enmus.LogCapitalTypeEnum;
import com.zywl.app.defaultx.enmus.LotteryGameStatusEnum;
import com.zywl.app.defaultx.enmus.UserCapitalTypeEnum;
import com.zywl.app.defaultx.service.*;
import com.zywl.app.util.RequestManagerListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@ServiceClass(code = "101")
public class LhdService extends BaseService {


    @Autowired
    private UserCapitalService userCapitalService;

    @Autowired
    private BackpackService backpackService;


    @Autowired
    private GameService gameService;


    @Autowired
    private LhdRequestMangerService requestMangerService;


    @Autowired
    private LhdBetRecordService lhdBetRecordService;


    @Autowired
    private UserDtsAmountService userDtsAmountService;

    @Autowired
    private UserYyScoreService userYyScoreService;

    @Autowired
    private GameLotteryResultService gameLotteryResultService;

    @Autowired
    private UserService userService;

    @Autowired
    private LhdService lhdService;

    public static JSONObject GAME_SETTING;


    public static int PEOPLE_NUM;


    private static Object lock = new Object();
    public static int STATUS;


    public static int TIME;

    public static int CAPITAL_TYPE;


    public static int PERIODS_NUM;

    public static Integer LAST_RESULT;

    public static Long beginTime;

    public static Long endTime;

    public static int GAME_STATUS = 1;

    public static BigDecimal ALL_PRIZE = BigDecimal.ZERO;

    public static final Map<String, LhdBetRecord> userOrders = new ConcurrentHashMap<>();

    public static final Map<String,BigDecimal> ROOM_MONEY = new ConcurrentHashMap<>();

    public static int KKK=0;

    public static final Set<String> users = new HashSet<>();

    public static final Set<String> BET_USERS = new HashSet<>();

    public static final Map<String, Object> USER_MAP = new ConcurrentHashMap<>();

    public static Map<String, List<Map<String, String>>> userCapitals = new ConcurrentHashMap<>();

    public static String key = DateUtil.getCurrent5();


    public static Map<String, Map<String, JSONObject>> ROOM_LIST = new ConcurrentHashMap<>();


    private static JSONArray history10Result = new JSONArray();


    private static JSONObject result100All = new JSONObject();

    private BigDecimal RATE;

    private BigDecimal BET_AMOUNT;

    private int needPush = 0;

    private static final List<Integer> canBet = new ArrayList<>();


    private static final Map<String, Map<String, BigDecimal>> settleInfo = new ConcurrentHashMap<>();

    public static String key2 = DateUtil.getCurrent5();

    public static final Map<String, JSONArray> pushArray = new ConcurrentHashMap<>();

    public static Map<String, User> BOT_USER = new ConcurrentHashMap<>();

    public static int NEED_BOT = 0;

    public static List<BigDecimal> BOT_MONEY= new ArrayList<>();


    public static final Random random = new Random();

    @PostConstruct
    public void _construct() {
        initRate();
        addPushSuport();
        initGameSetting();
        initHistoryResult();
        init();
        periodsNum();
        requestManagerUpdateCapital();
        pushRoomDate();
        canBet.add(0);
        canBet.add(1);
        canBet.add(2);
        canBet.add(3);
        canBet.add(4);
        canBet.add(5);
        canBet.add(6);
        logger.info("开始加载人机");
        List<User> bot = userService.findBot();
        bot.forEach(e -> BOT_USER.put(e.getId().toString(), e));
        logger.info("加载人机完成，加载数量：" + BOT_USER.size());
        gameAddBot();
        BOT_MONEY.add(new BigDecimal("15"));
        BOT_MONEY.add(new BigDecimal("16"));
        BOT_MONEY.add(new BigDecimal("14"));
        BOT_MONEY.add(new BigDecimal("17"));


    }

    public void init() {
        userOrders.clear();
        settleInfo.clear();
        ALL_PRIZE = BigDecimal.ZERO;
        PERIODS_NUM = PERIODS_NUM + 1;
        ROOM_LIST.clear();
        BET_USERS.clear();
        ROOM_LIST.put("0", new ConcurrentHashMap<>());
        ROOM_LIST.put("1", new ConcurrentHashMap<>());
    }

    public void initRate() {
        RATE = new BigDecimal("1.9");
    }

    public void updateRate(BigDecimal rate) {
        RATE = rate;
    }

    public void updateStatus(int status) {
        STATUS = status;
        GAME_STATUS = status;
        NEED_BOT=0;
    }


    public static <K, V> V getRandomValue(Map<K, V> map) {
        return map.values().stream()
                .skip(new Random().nextInt(map.size()))
                .findFirst().orElse(null);
    }

    public static BigDecimal getBotMoney(){
        Collections.shuffle(BOT_MONEY);
        return BOT_MONEY.get(0);
    }


    public User getBotUser() {
        return getRandomValue(BOT_USER);
    }

    public void gameAddBot() {
        new Timer("游戏添加人机").schedule(new TimerTask() {
            public void run() {
                try {
                    if ((STATUS == LotteryGameStatusEnum.ready.getValue() || STATUS == LotteryGameStatusEnum.gaming.getValue()) && NEED_BOT > 0) {
                        int rate = random.nextInt(100);
                        if (rate<NEED_BOT){
                            //  概率添加人机  每100毫秒判断1次  NEED_BOT值越大概率越高
                            //游戏阶段 添加人机
                            User user = getBotUser();
                            int i = random.nextInt(2);
                            userBetBet(user.getId().toString(), String.valueOf(i), getBotMoney(), null, null);
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }, 0, 100);
    }


    public Map<String, String> updateItem(String userId, BigDecimal number, String orderNo, Long dataId) {
        backpackService.subItemNumberByDts(Long.parseLong(userId), 3L, Integer.parseInt(number.toString()));
        Map<String, String> myOrder = new HashMap<>();
        myOrder.put("orderNo", orderNo);
        myOrder.put("dataId", String.valueOf(dataId));
        myOrder.put("betAmount", number.toString());
        myOrder.put("userId", userId);
        List<Map<String, String>> maps = userCapitals.get(key);
        maps.add(myOrder);
        return myOrder;
    }

    public Map<String, String> updateCapital(String userId, BigDecimal amount, String orderNo, Long dataId) {
        userCapitalService.subUserOccupyBalanceByLotteryBet(Long.parseLong(userId), amount);
        Map<String, String> myOrder = new HashMap<>();
        myOrder.put("orderNo", orderNo);
        myOrder.put("dataId", String.valueOf(dataId));
        myOrder.put("betAmount", amount.toString());
        myOrder.put("userId", userId);
        List<Map<String, String>> maps = userCapitals.get(key);
        maps.add(myOrder);
        return myOrder;
    }



    public void addPushSuport() {
        Push.addPushSuport(PushCode.updateNhInfo, new DefaultPushHandler());
        Push.addPushSuport(PushCode.updateNhStatus, new DefaultPushHandler());
    }

    public void pushRoomDate() {
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
                        Push.push(PushCode.updateNhInfo, null, data);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }, 0, 1);

    }

    public void initGameSetting() {
        logger.info("初始化2选1游戏配置");
        Game game = gameService.findGameById(5L);
        if (game != null) {
            GAME_SETTING = JSON.parseObject(game.getGameSetting());
            PEOPLE_NUM = GAME_SETTING.getIntValue("peopleNum");
            STATUS = game.getStatus();
            TIME = GAME_SETTING.getIntValue("time");
            CAPITAL_TYPE = GAME_SETTING.getIntValue("capitalType");
            BET_AMOUNT = GAME_SETTING.getBigDecimal("betAmount");
        }
        logger.info("初始化2选1游戏配置完成");
    }

    public void periodsNum() {
        logger.info("初始化2选1期数信息");
        LhdBetRecord records = lhdBetRecordService.findPeriodsNum();
        if (records == null) {
            PERIODS_NUM = 1;
        } else {
            PERIODS_NUM = records.getPeriodsNum() + 1;
        }
        logger.info("初始化2选1期数信息完成");
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
                        Executer.request(TargetSocketType.nh, CommandBuilder.builder().request("200811", object).build(), new RequestManagerListener(null));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }, 0, 1);

    }

    public void initHistoryResult() {
        logger.info("更新2选1历史开奖结果");
        long time = System.currentTimeMillis();
        List<GameLotteryResult> result10 = gameLotteryResultService.findHistoryResultByGameId(5L, 10);
        history10Result.clear();
        for (GameLotteryResult gameLotteryResult : result10) {
            String lotteryResult = gameLotteryResult.getLotteryResult();
            history10Result.add(Integer.parseInt(lotteryResult));
        }
        List<GameLotteryResult> result100 = gameLotteryResultService.findHistoryResultByGameId(5L, 100);
        result100All.clear();
        for (GameLotteryResult gameLotteryResult : result100) {
            String lotteryResult = gameLotteryResult.getLotteryResult();
            result100All.put(lotteryResult,result100All.getIntValue(lotteryResult,0)+1);
        }
        logger.info("更新2选1历史开奖结果完成，用时：" + (System.currentTimeMillis() - time));
    }

    public JSONObject getReturnInfo() {
        JSONObject result = new JSONObject();
        result.put("periods", PERIODS_NUM);
        result.put("status", STATUS);
        result.put("beginTime", beginTime);
        result.put("endTime", endTime);
        result.put("roomList", ROOM_LIST);
        result.put("userNum", BET_USERS.size());
        result.put("lastResult", LAST_RESULT);
        return result;
    }

    public JSONObject getPushInfo() {
        JSONObject result = new JSONObject();
        result.put("allPrize", ALL_PRIZE);
        result.put("userNum", BET_USERS.size());
        return result;
    }


    @Transactional
    @ServiceMethod(code = "101", description = "用户加入房间")
    public Object jionRoom(LhdService adminSocketServer, Command lotteryCommand, JSONObject data) {
        checkNull(data);
        checkNull(data.get("userId"));
        String userId = data.getString("userId");
        String name = data.getString("userName");
        USER_MAP.put(userId, name);
        users.add(userId);
        JSONObject returnInfo = getReturnInfo();
        if (ROOM_LIST.get("1").containsKey(userId)) {
            returnInfo.put("myBetRoom", 1);
            returnInfo.put("myBetAmount", ROOM_LIST.get("1").get(userId).getBigDecimal("amount"));
        }
        if (ROOM_LIST.get("0").containsKey(userId)) {
            returnInfo.put("myBetRoom", 0);
            returnInfo.put("myBetAmount", ROOM_LIST.get("0").get(userId).getBigDecimal("amount"));
        }
        returnInfo.put("history10Result", history10Result);
        return returnInfo;
    }

    @Transactional
    @ServiceMethod(code = "104", description = "用户离开房间")
    public Object leveRoom(LhdService adminSocketServer, Command lotteryCommand, JSONObject data) {
        String userId = data.getString("userId");
        users.remove(userId);
        USER_MAP.remove(userId);
        return new JSONObject();
    }

    public Object userBetBet(String userId, String betInfo, BigDecimal amount, Command lotteryCommand, JSONObject data) {
        synchronized (LockUtil.getlock(userId)) {
            if (STATUS == 0) {
                throwExp("小游戏即将更新");
            }
            if (STATUS == LotteryGameStatusEnum.settle.getValue()) {
                throwExp("结算中,请等待");
            }
            if (STATUS != LotteryGameStatusEnum.ready.getValue() && endTime != 0L && endTime - System.currentTimeMillis() < 1000) {
                throwExp("本局已停止参与，请等待下一局游戏开始");
            }
            if (!betInfo.equals("0") && !betInfo.equals("1")) {
                throwExp("非法操作");
            }
            logger.info(userId+"加入游戏，投入选项："+betInfo+",参与金额："+amount);
            String orderNo;
            Long dataId;
            //得到订单信息
            if (BET_USERS.contains(userId)) {
                //已经投入过了  寻找他的订单
                LhdBetRecord lhdBetRecord = userOrders.get(userId);
                orderNo = lhdBetRecord.getOrderNo();
                dataId = lhdBetRecord.getId();
                if (!betInfo.equals(lhdBetRecord.getBetInfo())){
                    throwExp("请选择自己所在的房间投入");
                }
            } else {
                orderNo = OrderUtil.getOrder5Number();
                LhdBetRecord record = lhdBetRecordService.addRecord(Long.parseLong(userId), orderNo, PERIODS_NUM, betInfo, amount);
                dataId = record.getId();
                userOrders.put(userId, record);
            }
            BET_USERS.add(userId);
            if (!BOT_USER.containsKey(userId)) {
                //处理资产信息
                updateCapital(userId, amount, orderNo, dataId);
            }
            //本局总金额
            ALL_PRIZE = ALL_PRIZE.add(amount);
            //处理内存信息
            BigDecimal myAllAmount = amount;
            if (ROOM_LIST.get(betInfo).containsKey(userId)) {
                //已经参与过了
                ROOM_LIST.get(betInfo).get(userId).put("betAmount", ROOM_LIST.get(betInfo).get(userId).getBigDecimal("betAmount").add(amount));
                myAllAmount = ROOM_LIST.get(betInfo).get(userId).getBigDecimal("betAmount");
                LhdBetRecord lhdBetRecord = userOrders.get(userId);
                lhdBetRecord.setBetAmount(lhdBetRecord.getBetAmount().add(amount));
                lhdBetRecord.setBetInfo(betInfo);
                lhdBetRecordService.updateRecord(lhdBetRecord);
            } else {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("userId", userId);
                if (BOT_USER.containsKey(userId)) {
                    String name = BOT_USER.get(userId).getName();
                    if (name.length() > 2) {
                        name = name.substring(0, 2);
                    }
                    jsonObject.put("name", name);
                } else {
                    jsonObject.put("name", USER_MAP.getOrDefault(userId, "***"));
                }
                jsonObject.put("betAmount", amount);
                ROOM_LIST.get(betInfo).put(userId, jsonObject);
            }
            ROOM_MONEY.put(betInfo,ROOM_MONEY.getOrDefault(betInfo,BigDecimal.ZERO).add(amount));
            pushArray.get(key2).add(pushResult(1, userId, betInfo, myAllAmount));

            synchronized (lock) {
                if (BET_USERS.size() >= PEOPLE_NUM && STATUS == LotteryGameStatusEnum.ready.getValue()) {
                    logger.info("更改状态为游戏阶段");
                    beginTime = System.currentTimeMillis();
                    endTime = DateUtil.getTimeByM(TIME);
                    changeRoomStatus(LotteryGameStatusEnum.gaming.getValue(), lotteryCommand);
                }
            }
            JSONObject result = new JSONObject();
            result.put("myBetInfo", data);
            needPush = 1;
            return result;
        }
    }

    @Transactional
    @ServiceMethod(code = "103", description = "用户参与投入")
    public Object play(LhdService adminSocketServer, Command lotteryCommand, JSONObject data) {
        String betInfo = data.getString("bet");
        BigDecimal amount = data.getBigDecimal("betAmount");
        String userId = data.getString("userId");
        if (GAME_STATUS == 0) {
            throwExp("小游戏维护中。");
        }
        return userBetBet(userId, betInfo, amount, lotteryCommand, data);

    }

    @Transactional
    @ServiceMethod(code = "105", description = "用户更换下注房间")
    public JSONObject updateRoom(LhdService adminSocketServer, Command lotteryCommand, JSONObject data) {
        checkNull(data);
        checkNull(data.get("userId"), data.get("userNo"), data.get("userName"));

        if (STATUS != LotteryGameStatusEnum.ready.getValue() && endTime != 0L && endTime - System.currentTimeMillis() < 1000) {
            throwExp("游戏即将开始，禁止更换");
        }

        String userId = data.getString("userId");
        String newRoomId = data.getString("bet");
        if (!newRoomId.equals("0") && !newRoomId.equals("1")) {
            throwExp("异常操作");
        }
        synchronized (LockUtil.getlock(userId )) {
            if (ROOM_LIST.get(newRoomId).containsKey(userId)) {
                throwExp("已经在该房间");
            }
            if (!BET_USERS.contains(userId)) {
                throwExp("请选择进入的房间");
            }
            String oldRoomId = "";
            if (ROOM_LIST.get("0").containsKey(userId)) {
                oldRoomId = "0";
            } else {
                oldRoomId = "1";
            }
            logger.info(userId+"用户更换房间，新房间ID："+newRoomId+",旧房间ID："+oldRoomId);
            //处理内存信息
            JSONObject userBetInfo = ROOM_LIST.get(oldRoomId).get(userId);
            ROOM_LIST.get(oldRoomId).remove(userId);
            BigDecimal amount = userBetInfo.getBigDecimal("betAmount");
            userOrders.get(userId).setBetInfo(newRoomId);
            ROOM_LIST.get(newRoomId).put(userId, userBetInfo);
            pushArray.get(key2).add(pushResult(1, userId, newRoomId, amount));
            ROOM_MONEY.put(newRoomId,ROOM_MONEY.getOrDefault(newRoomId,BigDecimal.ZERO).add(amount));
            ROOM_MONEY.put(oldRoomId,ROOM_MONEY.getOrDefault(oldRoomId,BigDecimal.ZERO).subtract(amount));
            JSONObject result = new JSONObject();
            return result;
        }

    }

    public JSONObject pushResult(int type, String userId, String bet, BigDecimal amount) {
        JSONObject pushResult = new JSONObject();
        pushResult.put("type", type);
        pushResult.put("userId", userId);
        pushResult.put("gameId", GameTypeEnum.nh.getValue());
        if (type == 1) {
            //下注 更换房间
            pushResult.put("roomId", bet);
            pushResult.put("betAmount", amount);
        }
        if (BOT_USER.containsKey(userId)) {
            String name = BOT_USER.get(userId).getName();
            if (name.length() > 2) {
                name = name.substring(0, 2);
            }
            pushResult.put("name", name);
        } else {
            pushResult.put("name", USER_MAP.getOrDefault(userId, "***"));
        }
        return pushResult;
    }

    public void changeRoomStatus(int roomStatus, Command lotteryCommand) {
        if (STATUS == roomStatus) {
            return;
        }
        STATUS = roomStatus;
        JSONObject data = new JSONObject();
        data.put("gameId", GameTypeEnum.nh.getValue());
        data.put("userIds", users);
        if (STATUS == LotteryGameStatusEnum.ready.getValue()) {
            // 初始化房间信息 更新历史开奖结果
            init();
            initHistoryResult();
            data.put("status", STATUS);
            data.put("periods", PERIODS_NUM);
            data.put("userIds", users);
            data.put("lookList", new ConcurrentHashMap<String, Map<String, Object>>());
            data.put("roomList", ROOM_LIST);
            data.put("lastResult", LAST_RESULT);
            data.put("history10Result", history10Result);
            data.put("history100Result",result100All);
            Push.push(PushCode.updateNhStatus, null, data);
        } else if (STATUS == LotteryGameStatusEnum.gaming.getValue()) {
            data.put("status", STATUS);
            data.put("endTime", endTime);
            data.put("gameId", GameTypeEnum.nh.getValue());
            data.put("userIds", users);
            Executer.executeService(new ServiceRunable(logger) {
                public void service() {
                    startGame(lotteryCommand);
                }
            });
            Push.push(PushCode.updateNhStatus, null, data);
        } else if (STATUS == LotteryGameStatusEnum.settle.getValue()) {
            int result = lhdService.settle(lotteryCommand);
            int status = STATUS;
            data.put("roomIds", result);
            data.put("status", status);
            data.put("userSettleInfo", settleInfo);
            Push.push(PushCode.updateNhStatus, null, data);
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
    public void startGame(Command lotteryCommand) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            int i = TIME;

            public void run() {
                if (endTime <= System.currentTimeMillis()) {
                    logger.info("游戏结束  结算");
                    // 下注期结束 更改状态为结算
                    if (STATUS != LotteryGameStatusEnum.settle.getValue()) {
                        changeRoomStatus(LotteryGameStatusEnum.settle.getValue(), lotteryCommand);
                    }
                    timer.cancel();
                }
                i--;
            }
        }, 0, 1000L);
    }


    public int getResult(){
        int result = random.nextInt(2);
        if (KKK>0){
            KKK=KKK-1;
            BigDecimal money0 = ROOM_MONEY.get("0");
            BigDecimal money1 = ROOM_MONEY.get("1");
            if (money0.compareTo(money1) > 0){
                result=0;
            }else {
                result=1;
            }
        }
        return result;
    }


    @Transactional
    public int settle(Command lotteryCommand) {

        Random random = new Random();
        int result = getResult();
        JSONObject data = new JSONObject();
        JSONObject updateRecord = new JSONObject();
        String winRoom;
        String loseRoom;
        if (result == 0) {
            winRoom = "1";
            loseRoom = "0";
        } else {
            winRoom = "0";
            loseRoom = "1";
        }
        Map<String, JSONObject> winMap = ROOM_LIST.get(winRoom);
        Map<String, JSONObject> loseMap = ROOM_LIST.get(loseRoom);
        BigDecimal allWinAmount = BigDecimal.ZERO;
        for (String uid : winMap.keySet()) {
            JSONObject o = new JSONObject();
            BigDecimal winAmount = ROOM_LIST.get(winRoom).get(uid).getBigDecimal("betAmount").multiply(new BigDecimal("1.9"));
            allWinAmount = allWinAmount.add(winAmount);
            o.put("amount", winAmount);
            o.put("capitalType", UserCapitalTypeEnum.yyb.getValue());
            o.put("orderNo", userOrders.get(uid).getOrderNo());
            o.put("em", LogCapitalTypeEnum.game_bet_win_nh.getValue());
            if (!BOT_USER.containsKey(uid)) {
                data.put(uid, o);
            }
            JSONObject record = new JSONObject();
            record.put("winAmount", winAmount);
            record.put("lotteryResult", result);
            record.put("betInfo", winRoom);
            record.put("winOrLose", 1);
            updateRecord.put(userOrders.get(uid).getOrderNo(), record);
            Map<String, BigDecimal> map = new HashMap<>();
            map.put("winAmount", winAmount);
            map.put("betAmount", ROOM_LIST.get(winRoom).get(uid).getBigDecimal("betAmount"));
            map.put("roomResult", BigDecimal.ONE);
            map.put("isWin", BigDecimal.ONE);
            settleInfo.put(uid, map);
        }
        for (String uid : loseMap.keySet()) {
            userDtsAmountService.addDtsAmount(Long.valueOf(uid), ROOM_LIST.get(loseRoom).get(uid).getBigDecimal("betAmount").multiply(new BigDecimal("0.05")));
            userYyScoreService.addYyScore(Long.valueOf(uid), ROOM_LIST.get(loseRoom).get(uid).getBigDecimal("betAmount").multiply(new BigDecimal("0.1")));
            JSONObject record = new JSONObject();
            record.put("winAmount", 0);
            record.put("lotteryResult", result);
            record.put("betInfo", loseRoom);
            record.put("winOrLose", 0);
            updateRecord.put(userOrders.get(uid).getOrderNo(), record);
            Map<String, BigDecimal> map = new HashMap<>();
            map.put("winAmount", BigDecimal.ZERO);
            map.put("betAmount", ROOM_LIST.get(loseRoom).get(uid).getBigDecimal("betAmount"));
            map.put("roomResult", BigDecimal.ZERO);
            map.put("isWin", BigDecimal.ZERO);
            settleInfo.put(uid, map);
        }
        ROOM_MONEY.clear();
        gameLotteryResultService.drawLottery(5L, String.valueOf(PERIODS_NUM == 0 ? 1 : PERIODS_NUM),
                String.valueOf(result), ALL_PRIZE, allWinAmount, ALL_PRIZE.subtract(allWinAmount), BET_USERS.size(), winMap.size(), loseMap.size(),
                1);
        logger.info("期号："+PERIODS_NUM+",LIST:"+ROOM_LIST);
        requestMangerService.requestManagerBet(data, new Listener() {
            public void handle(BaseClientSocket clientSocket, Command command) {
                if (command.isSuccess()) {
                    lhdBetRecordService.batchUpdateRecord(updateRecord);
                    Executer.response(CommandBuilder.builder(lotteryCommand).success(result).build());
                } else {
                    Executer.response(
                            CommandBuilder.builder(lotteryCommand).error(command.getMessage(), command.getData()).build());
                }
            }
        });

        return result;
    }


    @ServiceMethod(code = "004", description = "获取统计记录")
    public JSONObject getRecord(LhdService adminSocketServer, Command lotteryCommand, JSONObject data) {
        checkNull(data);
        checkNull(data.get("userId"));
        Long userId = data.getLong("userId");
        List<LhdBetRecord> records = lhdBetRecordService.findHistoryRecordByUserId(userId);
        JSONArray resultArray = new JSONArray();
        for (LhdBetRecord record : records) {
            JSONObject obj = new JSONObject();
            obj.put("periods", record.getPeriodsNum());
            obj.put("result", record.getLotteryResult());
            obj.put("myBet", record.getBetInfo());
            obj.put("betAmount", record.getBetAmount());
            obj.put("profit", record.getProfit());
            obj.put("create", record.getCreateTime());
            resultArray.add(obj);
        }
        JSONObject result = new JSONObject();
        result.put("historyList", history10Result);
        result.put("historyList100",result100All);
        result.put("myRecord", resultArray);
        return result;
    }


}

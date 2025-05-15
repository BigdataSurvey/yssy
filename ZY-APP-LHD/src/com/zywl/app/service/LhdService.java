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
    private GameService gameService;


    @Autowired
    private LhdRequestMangerService requestMangerService;

    @Autowired
    private ConfigService configService;

    @Autowired
    private LhdBetRecordService lhdBetRecordService;

    @Autowired
    private GameLotteryResultService gameLotteryResultService;

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

    public static BigDecimal ALL_PRIZE = BigDecimal.ZERO;

    public static final Map<String, LhdBetRecord> userOrders = new ConcurrentHashMap<>();

    public static final Set<String> users = new HashSet<>();

    public static final Set<String> BET_USERS = new HashSet<>();

    public static final Map<String, Object> USER_MAP = new ConcurrentHashMap<>();

    public static Map<String, List<Map<String, String>>> userCapitals = new ConcurrentHashMap<>();

    public static String key = DateUtil.getCurrent5();


    public static Map<String, Map<String, JSONObject>> ROOM_LIST = new ConcurrentHashMap<>();



    private static JSONArray history10Result = new JSONArray();

    private BigDecimal RATE;

    private BigDecimal BET_AMOUNT;

    private int needPush = 0;

    private static final List<Integer> canBet = new ArrayList<>();


    private static final Map<String, Map<String, BigDecimal>> settleInfo = new ConcurrentHashMap<>();


    public void init() {
        userOrders.clear();
        settleInfo.clear();
        ALL_PRIZE = BigDecimal.ZERO;
        PERIODS_NUM = PERIODS_NUM + 1;
        ROOM_LIST.clear();
        BET_USERS.clear();
    }

    public void initRate() {
        RATE = new BigDecimal("1.9");
    }

    public void updateRate(BigDecimal rate) {
        RATE = rate;
    }

    public void updateStatus(int status) {
        STATUS = status;
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

    @PostConstruct
    public void _construct() {
        initRate();
        addPushSuport();
        initGameSetting();
        initHistoryResult();
        addPushSuport();
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
    }

    public void pushRoomDate() {
        new Timer("定时推送房间数据").schedule(new TimerTask() {
            public void run() {
                try {
                    if (needPush == 1) {
                        Push.push(PushCode.updateNhInfo, null, getPushInfo());
                        needPush = 0;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0, 500);

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
        List<GameLotteryResult> result20 = gameLotteryResultService.findHistoryResultByGameId(5L, 10);
        JSONObject result2 = new JSONObject();
        this.history10Result.clear();
        for (GameLotteryResult gameLotteryResult : result20) {
            String lotteryResult = gameLotteryResult.getLotteryResult();
            JSONArray result = JSONArray.parseArray(lotteryResult);
            this.history10Result.add(result);
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
        String name = data.getString("name");
        USER_MAP.put(userId, name);
        users.add(userId);
        JSONObject returnInfo = getReturnInfo();
        if (ROOM_LIST.get("1").containsKey(userId) ){
            returnInfo.put("myBetRoom",1 );
            returnInfo.put("myBetAmount", ROOM_LIST.get("1").get(userId).getBigDecimal("amount"));
        }
        if (ROOM_LIST.get("0").containsKey(userId) ){
            returnInfo.put("myBetRoom",0 );
            returnInfo.put("myBetAmount", ROOM_LIST.get("0").get(userId).getBigDecimal("amount"));
        }
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


    @Transactional
    @ServiceMethod(code = "103", description = "用户参与投入")
    public Object play(LhdService adminSocketServer, Command lotteryCommand, JSONObject data) {
        String betInfo = data.getString("bet");
        BigDecimal amount = data.getBigDecimal("amount");
        String userId = data.getString("userId");
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


        /*
            1.生成订单
            2.扣款
            3.处理内存信息
         */
            logger.info("加入游戏");
            String orderNo;
            Long dataId;
            //得到订单信息
            if (BET_USERS.contains(userId)) {
                //已经投入过了  寻找他的订单
                LhdBetRecord lhdBetRecord = userOrders.get(userId);
                orderNo = lhdBetRecord.getOrderNo();
                dataId = lhdBetRecord.getId();
            } else {
                orderNo = OrderUtil.getOrder5Number();
                LhdBetRecord record = lhdBetRecordService.addRecord(Long.parseLong(userId), orderNo, PERIODS_NUM, betInfo, amount);
                dataId = record.getId();
                userOrders.put(userId, record);
            }
            //处理资产信息
            Map<String, String> map = updateCapital(userId, amount, orderNo, dataId);
            //本局总金额
            ALL_PRIZE = ALL_PRIZE.add(amount);
            //处理内存信息
            if (ROOM_LIST.get(betInfo).containsKey(userId)) {
                //已经参与过了
                ROOM_LIST.get(betInfo).get(userId).put("amount", ROOM_LIST.get(betInfo).get(userId).getBigDecimal("amount").add(amount));
            } else {
                BET_USERS.add(userId);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("userId", userId);
                jsonObject.put("name", USER_MAP.getOrDefault(userId, "***"));
                jsonObject.put("amount", amount);
                ROOM_LIST.get(betInfo).put(userId, jsonObject);
            }
            LhdBetRecord lhdBetRecord = userOrders.get(userId);
            lhdBetRecord.setBetAmount(lhdBetRecord.getBetAmount().add(amount));
            lhdBetRecord.setBetInfo(betInfo);
            lhdBetRecordService.updateRecord(lhdBetRecord);


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


    public void changeRoomStatus(int roomStatus, Command lotteryCommand) {
        if (STATUS == roomStatus) {
            return;
        }
        STATUS = roomStatus;
        JSONObject data = new JSONObject();
        data.put("gameId", GameTypeEnum.nh.getValue());
        if (STATUS == LotteryGameStatusEnum.ready.getValue()) {
            // 初始化房间信息 更新历史开奖结果
            init();
            initHistoryResult();
            data.put("status", STATUS);
            data.put("periodsNum", PERIODS_NUM);
            data.put("userIds", users);
            data.put("lookList", new ConcurrentHashMap<String, Map<String, Object>>());
            data.put("roomList", ROOM_LIST);
            data.put("lastResult", LAST_RESULT);

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
            data.put("roomId", result);
            data.put("status", status);
            data.put("userSettleInfo", settleInfo);
            Push.push(PushCode.updateDts2Status, null, data);
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


    @Transactional
    public int  settle(Command lotteryCommand) {

        Random random = new Random();
        int result = random.nextInt(2);
        JSONObject data = new JSONObject();
        JSONObject updateRecord = new JSONObject();
        String winRoom ;
        String loseRoom;
        if (result==0){
            winRoom="0";
            loseRoom="1";
        }else {
            winRoom="1";
            loseRoom="0";
        }
        Map<String, JSONObject> winMap = ROOM_LIST.get(winRoom);
        Map<String,JSONObject> loseMap = ROOM_LIST.get(loseRoom);
        BigDecimal allWinAmount = BigDecimal.ZERO;
        for (String uid : winMap.keySet()) {
            JSONObject o = new JSONObject();
            BigDecimal winAmount = ROOM_LIST.get(winRoom).get(uid).getBigDecimal("amount").multiply(new BigDecimal("1.9"));
            allWinAmount = allWinAmount.add(winAmount);
            o.put("amount", winAmount);
            o.put("capitalType", 2);
            o.put("orderNo", userOrders.get(uid).getOrderNo());
            o.put("em", LogCapitalTypeEnum.game_bet_win_nh.getValue());
            data.put(uid, o);
            JSONObject record = new JSONObject();
            record.put("winAmount", winAmount);
            record.put("lotteryResult", result);
            record.put("betInfo", winRoom);
            record.put("winOrLose",1);
            updateRecord.put(userOrders.get(uid).getOrderNo(), record);
            Map<String,BigDecimal> map = new HashMap<>();
            map.put("winAmount",winAmount);
            map.put("betAmount", ROOM_LIST.get(winRoom).get(uid).getBigDecimal("amount"));
            map.put("roomResult",BigDecimal.ONE);
            settleInfo.put(uid,map);
        }
        for (String uid : loseMap.keySet()) {
            JSONObject record = new JSONObject();
            record.put("winAmount", 0);
            record.put("lotteryResult", result);
            record.put("betInfo", loseRoom);
            record.put("winOrLose",0);
            updateRecord.put(userOrders.get(uid).getOrderNo(), record);
            Map<String,BigDecimal> map = new HashMap<>();
            map.put("winAmount",BigDecimal.ZERO);
            map.put("betAmount", ROOM_LIST.get(winRoom).get(uid).getBigDecimal("amount"));
            map.put("roomResult",BigDecimal.ZERO);
            settleInfo.put(uid,map);
        }
        gameLotteryResultService.drawLottery(5L, String.valueOf(PERIODS_NUM == 0 ? 1 : PERIODS_NUM),
                String.valueOf(result), ALL_PRIZE, allWinAmount, allWinAmount.subtract(ALL_PRIZE), BET_USERS.size(), winMap.size(), loseMap.size(),
                1);
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
            obj.put("periodsNum", record.getPeriodsNum());
            obj.put("result", record.getLotteryResult());
            obj.put("myBet", record.getBetInfo());
            obj.put("betAmount", record.getBetAmount());
            obj.put("profit", record.getProfit());
            obj.put("create", record.getCreateTime());
            resultArray.add(obj);
        }
        JSONObject result = new JSONObject();
        result.put("historyList", history10Result);
        result.put("myRecord", resultArray);
        return result;
    }


}

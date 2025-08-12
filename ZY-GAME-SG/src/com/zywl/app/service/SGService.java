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
public class SGService extends BaseService {


    @Autowired
    private SGRequestMangerService sgRequestMangerService;
    @Autowired
    private UserCapitalService userCapitalService;

    @Autowired
    private GameService gameService;


    @Autowired
    private SgBetRecordService sgBetRecordService;

    @Autowired
    private GameLotteryResultService gameLotteryResultService;

    @Autowired
    private SGService SGService;

    public static JSONObject GAME_SETTING;


    public static int STATUS;

    public static int GAME_STATUS;


    public static int TIME;

    public static int CAPITAL_TYPE;


    public static int PERIODS_NUM;

    public static Integer LAST_RESULT;

    public static Long beginTime;

    public static Long endTime;


    public static BigDecimal ALL_PRIZE = BigDecimal.ZERO;

    public static final Map<String, SgBetRecord> userOrders = new ConcurrentHashMap<>();

    public static final Set<String> users = new HashSet<>();

    public static final Set<String> jionUserIds = new HashSet<>();

    public static Map<String, List<Map<String, String>>> userCapitals = new ConcurrentHashMap<>();

    public static String key = DateUtil.getCurrent5();


    private static JSONArray history10Result = new JSONArray();


    private static final List<Integer> canBet = new ArrayList<>();

    private static final Map<String, Set<String>> optionsInfo = new ConcurrentHashMap<>();

    private static final Map<String, Map<String, Object>> settleInfo = new ConcurrentHashMap<>();

    private static final Map<String, Map<String, BigDecimal>> betInfo = new ConcurrentHashMap<>();

    //Map<摊位,Map<下注的0123, Map<用户id, 下注的金额>>>
    private static final Map<String,Map<String, Map<String, BigDecimal>>> btBetInfo = new ConcurrentHashMap<>();

    private static final Map<String, BigDecimal> optionRate = new ConcurrentHashMap<>();

    private static final Map<String,Map<String, BigDecimal>> roomOptionRate = new ConcurrentHashMap<>();

    private static final JSONObject lastBetUserInfo = new JSONObject();

    private static int needPush = 0;

    private static int isK = 0;

    public void init() {
        optionsInfo.clear();
        userOrders.clear();
        settleInfo.clear();
        ALL_PRIZE = BigDecimal.ZERO;
        btBetInfo.clear();
        betInfo.clear();
        for (int i = 0; i < 5; i++) {
            betInfo.put(String.valueOf(i), new HashMap<>());
        }
        Map<String, Map<String, BigDecimal>> betInfo1 = new HashMap<>();
        Map<String, Map<String, BigDecimal>> betInfo2 = new HashMap<>();
        for (int i = 0; i < 4; i++) {
            betInfo1.put(String.valueOf(i), new HashMap<>());
            betInfo2.put(String.valueOf(i), new HashMap<>());
        }
        btBetInfo.put(String.valueOf(1),betInfo1);
        btBetInfo.put(String.valueOf(2),betInfo2);
    }


    public void updateStatus(int status) {
        init();
        GAME_STATUS = status;
        STATUS = status;
        if (status == 1) {
            gameStart();
        }
    }


    public void updateIsK(int k) {
        isK = k;
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
        Push.addPushSuport(PushCode.updateSgInfo, new DefaultPushHandler());
        Push.addPushSuport(PushCode.updateSgStatus, new DefaultPushHandler());
    }

    @PostConstruct
    public void _construct() {
        STATUS = LotteryGameStatusEnum.cantPlay.getValue();
        addPushSuport();
        initGameSetting();
        initHistoryResult();
        addPushSuport();
        init();
        periodsNum();
        requestManagerUpdateCapital();
        canBet.add(0);
        canBet.add(1);
        canBet.add(2);
        canBet.add(3);
        canBet.add(4);
        canBet.add(5);
        canBet.add(6);
        optionRate.put("0", new BigDecimal("16"));
        optionRate.put("1", new BigDecimal("4"));
        optionRate.put("2", new BigDecimal("2"));
        optionRate.put("3", new BigDecimal("4"));
        optionRate.put("4", new BigDecimal("16"));

        Map<String , BigDecimal> map1 = new HashMap<>();
        Map<String , BigDecimal> map2 = new HashMap<>();

        map1.put("0",new BigDecimal("4.0"));
        map1.put("1",new BigDecimal("4.0"));
        map1.put("2",new BigDecimal("3.6"));
        map1.put("3",new BigDecimal("3.6"));

        map2.put("0",new BigDecimal("3.6"));
        map2.put("1",new BigDecimal("3.6"));
        map2.put("2",new BigDecimal("4.0"));
        map2.put("3",new BigDecimal("4.0"));
        roomOptionRate.put("1",  map1);
        roomOptionRate.put("2",  map2);
        pushRoomDate();
    }

    public void pushRoomDate() {
        new Timer("定时推送房间数据").schedule(new TimerTask() {
            public void run() {
                try {
                    if (needPush == 1) {
                        Push.push(PushCode.updateSgInfo, null, lastBetUserInfo);
                        needPush = 0;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0, 500);

    }

    public void initGameSetting() {
        logger.info("初始化算卦游戏配置");
        Game game = gameService.findGameById(8L);
        if (game != null) {
            GAME_SETTING = JSON.parseObject(game.getGameSetting());
            STATUS = game.getStatus();
            TIME = GAME_SETTING.getIntValue("time");
            CAPITAL_TYPE = GAME_SETTING.getIntValue("capitalType");
        }
        logger.info("初始化算卦游戏配置完成");
    }

    public void periodsNum() {
        logger.info("初始化算卦期数信息");
        GameLotteryResult gameResult = gameLotteryResultService.findPeriodsNum(GameTypeEnum.sg.getValue());
        if (gameResult == null) {
            PERIODS_NUM = 1;
        } else {
            PERIODS_NUM = Integer.parseInt(gameResult.getPeriodsNum()) + 1;
        }
        logger.info("初始化算卦期数信息完成");
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
                        Executer.request(TargetSocketType.sg, CommandBuilder.builder().request("200812", object).build(), new RequestManagerListener(null));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }, 0, 1);

    }

    public void initHistoryResult() {
        logger.info("更新算卦历史开奖结果");
        long time = System.currentTimeMillis();
        List<GameLotteryResult> result20 = gameLotteryResultService.findHistoryResultByGameId(8L, 10);
        JSONObject result2 = new JSONObject();
        this.history10Result.clear();
        for (GameLotteryResult gameLotteryResult : result20) {
            String lotteryResult = gameLotteryResult.getLotteryResult();
            //JSONArray result = JSONArray.parseArray(lotteryResult);
            this.history10Result.add(lotteryResult);
        }
        logger.info("更新算卦历史开奖结果完成，用时：" + (System.currentTimeMillis() - time));
    }

    public JSONObject getReturnInfo() {
        JSONObject result = new JSONObject();
        result.put("periods", PERIODS_NUM);
        result.put("status", GAME_STATUS);
        result.put("beginTime", beginTime);
        result.put("endTime", endTime);
        result.put("allPrize", ALL_PRIZE);
        result.put("lastResult", LAST_RESULT);
        return result;
    }

    public JSONObject getPushInfo() {
        JSONObject result = new JSONObject();
        result.put("allPrize", ALL_PRIZE);
        result.put("userNum", btBetInfo.size());
        return result;
    }

    /*
         游戏状态：未开始  进行中，结算，进行中，结算
        1.游戏被开启时，调用开始游戏接口
        1.开始游戏时，进入游戏中的状态
     */
    public void gameStart() {

        beginTime = System.currentTimeMillis();
        endTime = DateUtil.getTimeByM(TIME);
        changeRoomStatus(LotteryGameStatusEnum.gaming.getValue());
    }

    @Transactional
    @ServiceMethod(code = "101", description = "用户加入算卦房间")
    public Object jionRoom(SGService adminSocketServer, Command lotteryCommand, JSONObject data) {
        checkNull(data);
        checkNull(data.get("userId"));
        String userId = data.getString("userId");
        users.add(userId);
        JSONObject returnInfo = getReturnInfo();
        returnInfo.put("myBetInfo", getMyBtBet(userId));
        return returnInfo;
    }

    public Map<String, BigDecimal> getMyBet(String userId) {
        Set<String> options = betInfo.keySet();
        Map<String, BigDecimal> myBetInfo = new HashMap<>();
        for (String option : options) {
            Map<String, BigDecimal> optionBet = betInfo.getOrDefault(option, new HashMap<>());
            if (optionBet.containsKey(userId)) {
                myBetInfo.put(option, optionBet.get(userId));
            }
        }
        return myBetInfo;
    }

    public  Map<String,Map<String, Map<String, BigDecimal>>> getMyBtBet(String type,String userId) {
        Map<String, Map<String, BigDecimal>> info = btBetInfo.get(type);
        Set<String> options = info.keySet();
        Map<String, BigDecimal> myBetInfo = new HashMap<>();
        for (String option : options) {
            Map<String, BigDecimal> optionBet = info.getOrDefault(option, new HashMap<>());
            if (optionBet.containsKey(userId)) {
                myBetInfo.put(option, optionBet.get(userId));
            }
        }
        return btBetInfo;
    }

    public  Map<String,Map<String, Map<String, BigDecimal>>> getMyBtBet(String userId) {
        Map<String,Map<String, Map<String, BigDecimal>>> newBtBetInfo = new ConcurrentHashMap<>();
        for (Map.Entry<String, Map<String, Map<String, BigDecimal>>> twEntry : btBetInfo.entrySet()) {

            Map<String, Map<String, BigDecimal>> bookMap = twEntry.getValue();

            for (Map.Entry<String, Map<String, BigDecimal>> bookBetEntry : bookMap.entrySet()) {

                Map<String, BigDecimal> userMap = bookBetEntry.getValue();

                for (Map.Entry<String, BigDecimal> userEntry : userMap.entrySet()) {

                    String userKey = userEntry.getKey();
                    if (userKey.equals(userId)){
                        newBtBetInfo.put(twEntry.getKey(),bookMap);
                        return newBtBetInfo;
                    }
                }
            }
        }
        return null;
    }

    @Transactional
    @ServiceMethod(code = "104", description = "用户离开算卦房间")
    public Object leveRoom(SGService adminSocketServer, Command lotteryCommand, JSONObject data) {
        String userId = data.getString("userId");
        users.remove(userId);
        return new JSONObject();
    }

    @Transactional
    @ServiceMethod(code = "105", description = "切换房间")
    public Object cutRoom(SGService adminSocketServer, Command lotteryCommand, JSONObject data) {
        String userId = data.getString("userId");
        String type = data.getString("bet");
        users.remove(userId);

        Map<String, Map<String, BigDecimal>> currRoom = btBetInfo.get(type);
        for(Map.Entry<String,Map<String, BigDecimal>> entry : currRoom.entrySet()){
           if( entry.getValue().get(userId)!=null){
               if(type.equals("1")){
                   btBetInfo.get("2").put(entry.getKey(),entry.getValue());
               }else {
                   btBetInfo.get("1").put(entry.getKey(),entry.getValue());
               }
               entry.getValue().clear();
           }
        }
        return new JSONObject();
    }


    @Transactional
    @ServiceMethod(code = "102", description = "用户参与投入")
    public Object playBtGame(SGService adminSocketServer, Command lotteryCommand, JSONObject data) {
        checkNull(data);
        //type为摊位
        checkNull(data.get("userId"), data.get("bet"), data.get("betAmount"),data.get("type"));
        String userId = data.getString("userId");
        //选择的摊位
        String type = data.getString("type");
        String bet = data.getString("bet");
        BigDecimal amount = data.getBigDecimal("betAmount");
        if (Integer.parseInt(bet) < 0 || Integer.parseInt(bet) > 3) {
            throwExp("非法请求");
        }
        synchronized (LockUtil.getlock(userId)) {
            if (STATUS == 0) {
                throwExp("游戏即将维护，暂时不能进行游戏！");
            }
            if (GAME_STATUS == LotteryGameStatusEnum.settle.getValue()) {
                throwExp("上局结算中,请等待结算完成重新参与 ~");
            }
            if (GAME_STATUS != LotteryGameStatusEnum.ready.getValue() && endTime != 0L && endTime - System.currentTimeMillis() < 2000) {
                throwExp("本局即将结束，请稍后参与 ~");
            }
            //处理内存信息
            Map<String, Map<String, BigDecimal>> info = btBetInfo.get(type);
            Map<String, BigDecimal> betMap = info.get(bet);

            if (betMap.getOrDefault(userId, BigDecimal.ZERO).add(amount).compareTo(new BigDecimal("500")) > 0) {
                throwExp("单个签最大参与500灵石~");
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
            if (userOrders.containsKey(userId)) {
                //已经投入过了  寻找他的订单
                SgBetRecord SgBetRecord = userOrders.get(userId);
                orderNo = SgBetRecord.getOrderNo();
                dataId = SgBetRecord.getId();
            } else {
                orderNo = OrderUtil.getOrder5Number();
                Map<String, BigDecimal> map = new HashMap<>();
                map.put(bet, amount);
                SgBetRecord record = sgBetRecordService.addRecord(Long.parseLong(userId), orderNo, PERIODS_NUM, JSONObject.from(map).toJSONString(), amount);
                dataId = record.getId();
                userOrders.put(userId, record);
            }
            //处理资产信息
            Map<String, String> map = updateCapital(userId, amount, orderNo, dataId);
            //本局总金额
            ALL_PRIZE = ALL_PRIZE.add(amount);

            betMap.put(userId, betMap.getOrDefault(userId, betMap.getOrDefault(userId, BigDecimal.ZERO)).add(amount));
            btBetInfo.put(type,info);
            JSONObject result = new JSONObject();
            result.put("myBetInfo", getMyBtBet(type,userId));
            lastBetUserInfo.put("name", data.getOrDefault("name", ""));
            lastBetUserInfo.put("headImgUrl", data.getOrDefault("headImgUrl", ""));
            jionUserIds.add(userId);
            needPush = 1;
            return result;
        }
    }



    public void changeRoomStatus(int roomStatus) {
        if (GAME_STATUS == roomStatus) {
            return;
        }
        GAME_STATUS = roomStatus;
        JSONObject data = new JSONObject();
        data.put("gameId", GameTypeEnum.sg.getValue());
        if (GAME_STATUS == LotteryGameStatusEnum.gaming.getValue()) {
            data.put("status", GAME_STATUS);
            data.put("endTime", endTime);
            data.put("gameId", GameTypeEnum.sg.getValue());
            data.put("userIds", users);
            Executer.executeService(new ServiceRunable(logger) {
                public void service() {
                    startGame();
                }
            });
            Push.push(PushCode.updateSgStatus, null, data);
        } else if (GAME_STATUS == LotteryGameStatusEnum.settle.getValue()) {
            settleInfo.clear();
            Integer result1 = btDraw();
            Integer result2 = btDraw();
            JSONObject jsonObject1 = SGService.settle1(result1, "1");
            JSONObject jsonObject2 = SGService.settle1(result2,"2");
            BigDecimal room1Amount = (BigDecimal) jsonObject1.get("allWinAmount");
            BigDecimal room2Amount = (BigDecimal) jsonObject2.get("allWinAmount");
            BigDecimal allWinAmount = room1Amount.add(room2Amount);
            int type1UserNum = jsonObject1.getIntValue("typeUserNum");
            int type2UserNum = jsonObject2.getIntValue("typeUserNum");
            Integer userNum = type1UserNum+type2UserNum;
            gameLotteryResultService.drawLottery(8L, String.valueOf(PERIODS_NUM),
                    result1+","+result2, ALL_PRIZE, allWinAmount, ALL_PRIZE.compareTo(allWinAmount)>0?BigDecimal.ONE:BigDecimal.ONE.negate(), jionUserIds.size(),userNum , jionUserIds.size()-userNum,
                    1);

            int status = GAME_STATUS;
            JSONObject settleData = new JSONObject();
            data.put("status", status);
            data.put("userIds", users);
            data.put("settleInfo", settleInfo);
            data.put("result1", result1);
            data.put("result2", result2);
            //房间人数 金额 我参与的哪个房间 以及对应的金额
            Push.push(PushCode.updateSgStatus, null, data);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            init();
            //期数加1
            PERIODS_NUM = PERIODS_NUM + 1;
            initHistoryResult();
            LAST_RESULT = result1+result2;
            jionUserIds.clear();
            if (STATUS != 0) {
                gameStart();
            }
        }
    }

    @Transactional
    public List<Integer> draw() {
        // 先开奖
        Random r = new Random();
        List<Integer> result = new ArrayList<>();
        if (isK == 0) {
            for (int i = 0; i < 4; i++) {
                result.add(r.nextInt(2));
            }
        } else {
            result = computeResult();
            isK = 0;
        }
        return result;
    }

    @Transactional
    public Integer btDraw() {
        // 先开奖
        Random r = new Random();
        return r.nextInt(4);
    }

    public List<Integer> computeResult() {
        Set<String> options = betInfo.keySet();
        BigDecimal amount = new BigDecimal("99999999");
        String result = "0";
        for (String option : options) {
            BigDecimal winAmount = BigDecimal.ZERO;
            Map<String, BigDecimal> map = betInfo.get(option);
            BigDecimal rate = optionRate.get(option);
            for (BigDecimal value : map.values()) {
                winAmount = winAmount.add(value.multiply(rate));
            }
            if (winAmount.compareTo(amount) < 0) {
                amount = winAmount;
                result = option;
            }
        }
        List<Integer> list = new ArrayList<>();
        if (result.equals("0")) {
            list.add(0);
            list.add(0);
            list.add(0);
            list.add(0);
        } else if (result.equals("1")) {
            list.add(0);
            list.add(0);
            list.add(0);
            list.add(1);
        } else if (result.equals("2")) {
            list.add(0);
            list.add(0);
            list.add(1);
            list.add(1);
        } else if (result.equals("3")) {
            list.add(0);
            list.add(1);
            list.add(1);
            list.add(1);
        } else if (result.equals("4")) {
            list.add(1);
            list.add(1);
            list.add(1);
            list.add(1);
        }
        Collections.shuffle(list);
        return list;
    }


    @Transactional
    public void startGame() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            int i = TIME;

            public void run() {
                logger.info("剩余时间"+i);
                if (endTime <= System.currentTimeMillis()) {
                    logger.info("游戏结束  结算");
                    // 下注期结束 更改状态为结算
                    if (GAME_STATUS != LotteryGameStatusEnum.settle.getValue()) {
                        changeRoomStatus(LotteryGameStatusEnum.settle.getValue());
                    }
                    timer.cancel();
                }
                i--;
            }

        }, 0, 1000L);
    }

    private String getSgResult(List<Integer> result) {
        int ji = 0;
        int xiong = 0;
        for (Integer integer : result) {
            if (integer == 0) {
                ji++;
            }
            if (integer == 1) {
                xiong++;
            }
        }
        if (ji == 4) {
            return "0";
        }
        if (ji == 3 && xiong == 1) {
            return "1";
        }
        if (ji == 2 && xiong == 2) {
            return "2";
        }
        if (ji == 1 && xiong == 3) {
            return "3";
        }
        if (xiong == 4) {
            return "4";
        }
        return null;
    }



    @Transactional
    public JSONObject settle1(Integer result,String type) {
        /*
             下注等于结果的  增加获得金额
         */
        //String result = getSgResult(list);
        JSONObject jsonObject = new JSONObject();
        Map<String, Map<String, BigDecimal>> infoMap = btBetInfo.get(type);
        Map<String, BigDecimal> drawUsers = infoMap.get(String.valueOf(result));
        Set<String> winsUserIds = drawUsers.keySet();
        BigDecimal rate = roomOptionRate.get(type).get(result);
        JSONObject data = new JSONObject();
        JSONObject updateRecord = new JSONObject();
        BigDecimal allWinAmount = BigDecimal.ZERO;
        for (String userId : jionUserIds) {
            Map<String, Object> map = new HashMap<>();
            map.put("result", result);
            JSONObject record = new JSONObject();
            if (winsUserIds.contains(userId)) {
                map.put("winOrLose", 1);
                JSONObject o = new JSONObject();
                BigDecimal winAmount = drawUsers.getOrDefault(userId, BigDecimal.ZERO).multiply(rate);
                map.put("winAmount", winAmount);
                allWinAmount = allWinAmount.add(winAmount);
                o.put("amount", winAmount);
                o.put("capitalType", 2);
                o.put("orderNo", userOrders.get(userId).getOrderNo());
                o.put("em", LogCapitalTypeEnum.game_bet_win_sg.getValue());
                data.put(userId, o);
                record.put("winAmount", winAmount);
                record.put("winOrLose", 1);
            } else {
                record.put("winAmount", BigDecimal.ZERO);
                record.put("winOrLose", 0);
                map.put("winOrLose", 0);
                map.put("winAmount", BigDecimal.ZERO);
                map.put("result", result);
            }
            record.put("lotteryResult", result);
            settleInfo.put(userId, map);
            record.put("settleInfo", map);
            Map<String, BigDecimal> map1 = new HashMap<>();
            Set<String> strings = betInfo.keySet();
            for (String option : strings) {
                if (betInfo.get(option).containsKey(userId)) {
                    map1.put(option, betInfo.get(option).get(userId));
                }
            }
            record.put("betInfo", JSONObject.from(map1).toJSONString());
            updateRecord.put(userOrders.get(userId).getOrderNo(), record);
        }
        sgRequestMangerService.requestManagerBet(data, new Listener() {
            public void handle(BaseClientSocket clientSocket, Command command) {
                if (command.isSuccess()) {
                    sgBetRecordService.batchUpdateRecord(updateRecord);
                } else {
                    STATUS = 0;
                }
            }
        });
        jsonObject.put("allWinAmount",allWinAmount);
        jsonObject.put("typeUserNum",btBetInfo.get(type).get(String.valueOf(result)).size());
        return jsonObject;

    }


    @Transactional
    public void settle(List<Integer> list) {
        /*
             下注等于结果的  增加获得金额
         */
        String result = getSgResult(list);
        Map<String, BigDecimal> betMap = betInfo.get(result);
        Set<String> winsUserIds = betMap.keySet();
        BigDecimal rate = optionRate.get(result);
        JSONObject data = new JSONObject();
        JSONObject updateRecord = new JSONObject();
        BigDecimal allWinAmount = BigDecimal.ZERO;
        for (String userId : jionUserIds) {
            Map<String, Object> map = new HashMap<>();
            map.put("result", list);
            JSONObject record = new JSONObject();
            if (winsUserIds.contains(userId)) {
                map.put("winOrLose", 1);
                JSONObject o = new JSONObject();
                BigDecimal winAmount = betMap.getOrDefault(userId, BigDecimal.ZERO).multiply(rate).multiply(new BigDecimal("0.9"));
                map.put("winAmount", winAmount);
                allWinAmount = allWinAmount.add(winAmount);
                o.put("amount", winAmount);
                o.put("capitalType", 2);
                o.put("orderNo", userOrders.get(userId).getOrderNo());
                o.put("em", LogCapitalTypeEnum.game_bet_win_sg.getValue());
                data.put(userId, o);
                record.put("winAmount", winAmount);
                record.put("winOrLose", 1);
            } else {
                record.put("winAmount", BigDecimal.ZERO);
                record.put("winOrLose", 0);
                map.put("winOrLose", 0);
                map.put("winAmount", BigDecimal.ZERO);
                map.put("result", list);
            }
            record.put("lotteryResult", result);
            settleInfo.put(userId, map);
            record.put("settleInfo", map);
            Map<String, BigDecimal> map1 = new HashMap<>();
            Set<String> strings = betInfo.keySet();
            for (String option : strings) {
                if (betInfo.get(option).containsKey(userId)) {
                    map1.put(option, betInfo.get(option).get(userId));
                }
            }
            record.put("betInfo", JSONObject.from(map1).toJSONString());
            updateRecord.put(userOrders.get(userId).getOrderNo(), record);
        }
        gameLotteryResultService.drawLottery(8L, String.valueOf(PERIODS_NUM),
                String.valueOf(list), ALL_PRIZE, allWinAmount, ALL_PRIZE.compareTo(allWinAmount)>0?BigDecimal.ONE:BigDecimal.ONE.negate(), jionUserIds.size(), betInfo.get(result).size(), jionUserIds.size()-betInfo.get(result).size(),
                1);
        sgRequestMangerService.requestManagerBet(data, new Listener() {
            public void handle(BaseClientSocket clientSocket, Command command) {
                if (command.isSuccess()) {
                    sgBetRecordService.batchUpdateRecord(updateRecord);
                } else {
                    STATUS = 0;
                }
            }
        });

    }


    /**
     * 分配金额
     *
     * @param m     剩余金额
     * @param n     分包数
     * @param list  各包金额
     * @param scale 精确额度
     */
    public static void getAmount(Integer m, Integer n, Random random, List<Double> list, Integer scale) {
        //每次分到的金额,均值乘2
        int a = random.nextInt(2 * m / n) + 1;
        //如果人数大于1，并且剩余金额小于剩余人数，则重新计算本次分包金额，保证每个人最少能领到0.01元
        if (n > 1 && m < n) {
            //重随机
            getAmount(m, n, random, list, scale);
        } else if (n == 1) {
            list.add((BigDecimal.valueOf(m).divide(BigDecimal.valueOf(scale)).doubleValue()));
            m = 0;
        } else {
            m = m - a;
            n = n - 1;
            list.add((BigDecimal.valueOf(a).divide(BigDecimal.valueOf(scale)).doubleValue()));
            getAmount(m, n, random, list, scale);
        }
    }


    @ServiceMethod(code = "004", description = "获取统计记录")
    public JSONObject getRecord(SGService adminSocketServer, Command lotteryCommand, JSONObject data) {
        checkNull(data);
        checkNull(data.get("userId"));
        Long userId = data.getLong("userId");
        List<SgBetRecord> records = sgBetRecordService.findHistoryRecordByUserId(userId);
        JSONArray resultArray = new JSONArray();
        for (SgBetRecord record : records) {
            JSONObject obj = new JSONObject();
            obj.put("periodsNum", record.getPeriodsNum());
            obj.put("result", record.getLotteryResult());
            obj.put("myBet", record.getBetInfo());
            obj.put("betAmount", record.getBetAmount());
            obj.put("profit", record.getProfit());
            obj.put("create", record.getCreateTime());
            obj.put("settleInfo", record.getSettleInfo());
            resultArray.add(obj);
        }
        JSONObject result = new JSONObject();
        result.put("historyList", history10Result);
        result.put("myRecord", resultArray);
        return result;
    }

    public static void main(String[] args) {
        Random random = new Random();
        int a1 = 0;
        int a2 = 0;
        int a3 = 0;
        int a4 = 0;
        int a5 = 0;
        BigDecimal amount1 = BigDecimal.ZERO;
        BigDecimal amount2 = BigDecimal.ZERO;
        for (int j = 0; j < 10000; j++) {
            int a = 0;
            int b = 0;
            amount1 = amount1.add(new BigDecimal("30"));
            for (int i = 0; i < 4; i++) {
                int index = random.nextInt(2);
                if (index == 0) {
                    a++;
                }
                if (index == 1) {
                    b++;
                }
            }
            if (a == 4) {
                a1++;
            }
            if (a == 3 && b == 1) {
                a2++;
                amount2 = amount2.add(new BigDecimal("36"));
            }
            if (a == 2 && b == 2) {
                a3++;
                amount2 = amount2.add(new BigDecimal("18"));
            }
            if (a == 1 && b == 3) {
                a4++;
                amount2 = amount2.add(new BigDecimal("36"));
            }
            if (b == 4) {
                a5++;
            }
        }
        System.out.println(a1);
        System.out.println(a2);
        System.out.println(a3);
        System.out.println(a4);
        System.out.println(a5);
        System.out.println(amount1);
        System.out.println(amount2);
    }


}

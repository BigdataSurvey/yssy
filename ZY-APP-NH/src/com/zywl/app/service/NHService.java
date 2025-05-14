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
public class NHService extends BaseService {


    @Autowired
    private UserCapitalService userCapitalService;


    @Autowired
    private GameService gameService;


    @Autowired
    private NhRequestMangerService requestMangerService;

    @Autowired
    private ConfigService configService;

    @Autowired
    private NhBetRecordService nhBetRecordService;

    @Autowired
    private GameLotteryResultService gameLotteryResultService;

    @Autowired
    private NHService nhService;

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

    public static final Map<String, NhBetRecord> userOrders = new ConcurrentHashMap<>();
    public static final Map<String, Set<Integer>> userBetInfo = new ConcurrentHashMap<>();

    public static final Set<String> users = new HashSet<>();

    public static Map<String, List<Map<String, String>>> userCapitals = new ConcurrentHashMap<>();

    public static String key = DateUtil.getCurrent5();


    private static JSONArray history10Result = new JSONArray();

    private BigDecimal RATE;

    private BigDecimal BET_AMOUNT;

    private int needPush = 0;

    private static final List<Integer> canBet = new ArrayList<>();

    private static final Map<String, Set<String>> optionsInfo = new ConcurrentHashMap<>();

    private static final Map<String, Map<String, BigDecimal>> settleInfo = new ConcurrentHashMap<>();


    public void init() {
        optionsInfo.clear();
        for (int i = 0; i < 7; i++) {
            optionsInfo.put(String.valueOf(i), new HashSet<>());
            userBetInfo.clear();
        }
        userBetInfo.clear();
        userOrders.clear();
        settleInfo.clear();
        ALL_PRIZE = BigDecimal.ZERO;
        PERIODS_NUM = PERIODS_NUM + 1;
    }

    public void initRate() {
        Config config = configService.getConfigByKey(Config.NH_RATE);
        String rate = config.getValue();
        RATE = new BigDecimal(rate);
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
        logger.info("初始化年货游戏配置");
        Game game = gameService.findGameById(5L);
        if (game != null) {
            GAME_SETTING = JSON.parseObject(game.getGameSetting());
            PEOPLE_NUM = GAME_SETTING.getIntValue("peopleNum");
            STATUS = game.getStatus();
            TIME = GAME_SETTING.getIntValue("time");
            CAPITAL_TYPE = GAME_SETTING.getIntValue("capitalType");
            BET_AMOUNT = GAME_SETTING.getBigDecimal("betAmount");
        }
        logger.info("初始化年货游戏配置完成");
    }

    public void periodsNum() {
        logger.info("初始化年货期数信息");
        NhBetRecord records = nhBetRecordService.findPeriodsNum();
        if (records == null) {
            PERIODS_NUM = 1;
        } else {
            PERIODS_NUM = records.getPeriodsNum() + 1;
        }
        logger.info("初始化年货期数信息完成");
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
        logger.info("更新年货历史开奖结果");
        long time = System.currentTimeMillis();
        List<GameLotteryResult> result20 = gameLotteryResultService.findHistoryResultByGameId(5L, 10);
        JSONObject result2 = new JSONObject();
        this.history10Result.clear();
        for (GameLotteryResult gameLotteryResult : result20) {
            String lotteryResult = gameLotteryResult.getLotteryResult();
            JSONArray result = JSONArray.parseArray(lotteryResult);
            this.history10Result.add(result);
        }
        logger.info("更新年货历史开奖结果完成，用时：" + (System.currentTimeMillis() - time));
    }

    public JSONObject getReturnInfo() {
        JSONObject result = new JSONObject();
        result.put("periods", PERIODS_NUM);
        result.put("status", STATUS);
        result.put("beginTime", beginTime);
        result.put("endTime", endTime);
        result.put("allPrize", ALL_PRIZE);
        result.put("userNum", userBetInfo.size());
        result.put("lastResult", LAST_RESULT);
        return result;
    }

    public JSONObject getPushInfo() {
        JSONObject result = new JSONObject();
        result.put("allPrize", ALL_PRIZE);
        result.put("userNum", userBetInfo.size());
        return result;
    }


    @Transactional
    @ServiceMethod(code = "101", description = "用户加入年货房间")
    public Object jionRoom(NHService adminSocketServer, Command lotteryCommand, JSONObject data) {
        checkNull(data);
        checkNull(data.get("userId"));
        String userId = data.getString("userId");
        users.add(userId);
        JSONObject returnInfo = getReturnInfo();
        returnInfo.put("myBetInfo", userBetInfo.getOrDefault(userId, new HashSet<>()));
        return returnInfo;
    }

    @Transactional
    @ServiceMethod(code = "104", description = "用户加入年货房间")
    public Object leveRoom(NHService adminSocketServer, Command lotteryCommand, JSONObject data) {
        String userId = data.getString("userId");
        users.remove(userId);
        return new JSONObject();
    }


    @Transactional
    @ServiceMethod(code = "103", description = "用户参与投入")
    public Object play(NHService adminSocketServer, Command lotteryCommand, JSONObject data) {
        JSONArray betInfo = data.getJSONArray("betInfo");
        String userId = data.getString("userId");
        List<Integer> bets = betInfo.toList(Integer.class);
        Set<Integer> betInfos = new HashSet<>(bets);
        for (Integer info : betInfos) {
            if (canBet.indexOf(info) < 0) {
                throwExp("非法参与");
            }
        }
        synchronized (LockUtil.getlock(userId)) {
            Set<Integer> old = userBetInfo.getOrDefault(userId, new HashSet<>());
            Set<Integer> newSet = new HashSet<>();
            newSet.addAll(old);
            newSet.addAll(betInfos);
            if (newSet.size() >= 7) {
                throwExp("最多只能选择6个摊位~");
            }
            for (Integer info : betInfos) {
                if (old.contains(info)){
                    throwExp("已经选择过相同的摊位啦~");
                }
            }
            if (STATUS == 0) {
                throwExp("游戏即将维护，暂时不能进行游戏！");
            }
            if (STATUS == LotteryGameStatusEnum.settle.getValue()) {
                throwExp("上局结算中,请等待结算完成重新参与 ~");
            }
            if (STATUS != LotteryGameStatusEnum.ready.getValue() && endTime != 0L && endTime - System.currentTimeMillis() < 2000) {
                throwExp("本局即将结束，请稍后参与 ~");
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
            if (userBetInfo.containsKey(userId) && userOrders.containsKey(userId)) {
                //已经投入过了  寻找他的订单
                NhBetRecord nhBetRecord = userOrders.get(userId);
                orderNo = nhBetRecord.getOrderNo();
                dataId = nhBetRecord.getId();
            } else {
                orderNo = OrderUtil.getOrder5Number();
                NhBetRecord record = nhBetRecordService.addRecord(Long.parseLong(userId), orderNo, PERIODS_NUM, betInfo.toJSONString(), BET_AMOUNT.multiply(new BigDecimal(String.valueOf(betInfo.size()))));
                dataId = record.getId();
                userOrders.put(userId, record);
            }
            //处理资产信息
            Map<String, String> map = updateCapital(userId, new BigDecimal(String.valueOf(betInfos.size())).multiply(BET_AMOUNT), orderNo, dataId);
            //本局总金额
            ALL_PRIZE = ALL_PRIZE.add(BET_AMOUNT.multiply(new BigDecimal(String.valueOf(betInfos.size()))).multiply(RATE));
            //处理内存信息
            if (userBetInfo.containsKey(userId) && userOrders.containsKey(userId)) {
                NhBetRecord nhBetRecord = userOrders.get(userId);
                nhBetRecord.setBetAmount(nhBetRecord.getBetAmount().add(BET_AMOUNT.multiply(new BigDecimal(String.valueOf(betInfos.size())))));
                JSONArray myBet = JSONArray.parseArray(nhBetRecord.getBetInfo());
                for (Integer info : betInfos) {
                    optionsInfo.get(info.toString()).add(userId);
                    if (myBet.contains(info)) continue;
                    myBet.add(info);
                }
                userBetInfo.get(userId).addAll(betInfos);
                nhBetRecord.setBetInfo(myBet.toJSONString());
                nhBetRecordService.updateRecord(nhBetRecord);
            } else {
                for (Integer info : betInfos) {
                    optionsInfo.get(info.toString()).add(userId);
                }
                userBetInfo.put(userId, betInfos);
            }

            synchronized (lock) {
                if (userBetInfo.size() >= PEOPLE_NUM && STATUS == LotteryGameStatusEnum.ready.getValue()) {
                    logger.info("更改状态为游戏阶段");
                    beginTime = System.currentTimeMillis();
                    endTime = DateUtil.getTimeByM(TIME);
                    changeRoomStatus(LotteryGameStatusEnum.gaming.getValue(), lotteryCommand);
                }
            }
            JSONObject result = new JSONObject();
            result.put("myBetInfo", userBetInfo.getOrDefault(userId, new HashSet<>()));
            needPush = 1;
            return result;
        }
    }

    public JSONObject getSettleData(List<Double> list) {
        JSONObject settleData = new JSONObject();
        Map<String, Integer> data = new ConcurrentHashMap<>();
        Map<String, Double> data2 = new ConcurrentHashMap<>();
        for (String s : optionsInfo.keySet()) {
            data.put(s, optionsInfo.get(s).size());
            data2.put(s, list.get(Integer.parseInt(s)));
        }
        settleData.put("roomUserNum", data);
        settleData.put("roomAmount", data2);
        return settleData;
    }

    public void changeRoomStatus(int roomStatus, Command lotteryCommand) {
        if (STATUS==roomStatus){
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
            List<Double> list = draw();
            nhService.settle(list, lotteryCommand);
            int status = STATUS;
            JSONObject settleData = getSettleData(list);
            data.put("status", status);
            data.put("roomData", settleData);
            Map<String, Map<String, BigDecimal>> map = new HashMap();
            map.putAll(settleInfo);
            data.put("settleInfo", map);
            data.put("userIds", users);
            //房间人数 金额 我参与的哪个房间 以及对应的金额
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
    public List<Double> draw() {
        // 先开奖
        Random r = new Random();
        int result;
        result = r.nextInt(6);
        BigDecimal amount = ALL_PRIZE.setScale(2, BigDecimal.ROUND_DOWN);
        List<Double> list = initAmount(Double.parseDouble(amount.toString()));
        list.add(result, 0D);
        result = r.nextInt(7);
        list.add(result, 0D);
        gameLotteryResultService.drawLottery(5L, String.valueOf(PERIODS_NUM == 0 ? 1 : PERIODS_NUM),
                list.toString(), ALL_PRIZE, BigDecimal.ZERO, BigDecimal.ONE, userBetInfo.size(), 0, 0,
                1);
        return list;
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
    public void settle(List<Double> list, Command lotteryCommand) {
        /*
         挨个房间判断输赢 如果为被杀的房间  所有人分到0个钻石
         为赢  则判断该房间多少灵石 多少人  平均分
         */


        JSONObject data = new JSONObject();
        for (int i = 0; i < list.size(); i++) {
            //当前房间人数
            int count = optionsInfo.get(String.valueOf(i)).size();
            if (count == 0) continue;
            //当前房间钱数
            BigDecimal money = new BigDecimal(list.get(i).toString());
            BigDecimal avgMoney = money.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : money.divide(new BigDecimal(String.valueOf(count)), 2, BigDecimal.ROUND_DOWN);
            for (String userId : optionsInfo.get(String.valueOf(i))) {
                if (settleInfo.containsKey(userId)) {
                    Map<String, BigDecimal> map = settleInfo.get(userId);
                    map.put(String.valueOf(i), map.getOrDefault(String.valueOf(i), BigDecimal.ZERO).add(avgMoney));
                    map.put("allMoney", map.getOrDefault("allMoney", BigDecimal.ZERO).add(avgMoney));
                } else {
                    Map<String, BigDecimal> map = new ConcurrentHashMap<>();
                    map.put(String.valueOf(i), avgMoney);
                    map.put("allMoney", avgMoney);
                    settleInfo.put(userId, map);
                }
            }

        }
        JSONObject updateRecord = new JSONObject();
        for (String uid : settleInfo.keySet()) {
            JSONObject o = new JSONObject();
            o.put("amount", settleInfo.get(uid).get("allMoney"));
            o.put("capitalType", 2);
            o.put("orderNo", userOrders.get(uid).getOrderNo());
            o.put("em", LogCapitalTypeEnum.game_bet_win_nh.getValue());
            data.put(uid, o);
            JSONObject record = new JSONObject();
            record.put("winAmount", settleInfo.get(uid).get("allMoney"));
            JSONArray array = new JSONArray();
            array.addAll(list);
            record.put("settleInfo", settleInfo.get(uid));
            record.put("lotteryResult", array.toJSONString());
            record.put("betInfo", userBetInfo.get(uid));
            updateRecord.put(userOrders.get(uid).getOrderNo(), record);
        }
        requestMangerService.requestManagerBet(data, new Listener() {
            public void handle(BaseClientSocket clientSocket, Command command) {
                if (command.isSuccess()) {
                    nhBetRecordService.batchUpdateRecord(updateRecord);
                    Executer.response(CommandBuilder.builder(lotteryCommand).success(list).build());
                } else {
                    Executer.response(
                            CommandBuilder.builder(lotteryCommand).error(command.getMessage(), command.getData()).build());
                }
            }
        });

    }

    /**
     * 初始化金额
     *
     * @param m 总金额
     * @return 每个包的金额
     */
    public static List<Double> initAmount(Double m) {
        List<Double> list = new ArrayList<>();
        Random random = new Random();
        //金额乘100，使每个包精确到分，10元==1000分
        System.out.println((int) (m * 100));
        getAmount((int) (m * 100), 5, random, list, 100);
        return list;
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
    public JSONObject getRecord(NHService adminSocketServer, Command lotteryCommand, JSONObject data) {
        checkNull(data);
        checkNull(data.get("userId"));
        Long userId = data.getLong("userId");
        List<NhBetRecord> records = nhBetRecordService.findHistoryRecordByUserId(userId);
        JSONArray resultArray = new JSONArray();
        for (NhBetRecord record : records) {
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


}

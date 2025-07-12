package com.zywl.app.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.google.common.collect.Lists;
import com.live.app.ws.bean.Command;
import com.live.app.ws.enums.PushCode;
import com.live.app.ws.enums.TargetSocketType;
import com.live.app.ws.interfacex.Listener;
import com.live.app.ws.socket.BaseClientSocket;
import com.live.app.ws.util.CommandBuilder;
import com.live.app.ws.util.DefaultPushHandler;
import com.live.app.ws.util.Executer;
import com.live.app.ws.util.Push;
import com.sun.org.apache.bcel.internal.generic.NEW;
import com.zywl.app.base.bean.Monster;
import com.zywl.app.base.bean.*;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.base.util.OrderUtil;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.cache.UserCapitalCacheService;
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
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
@ServiceClass(code = "101")
public class DgsService extends BaseService {

    //public static ConcurrentHashMap<Monster, TargetSocketType> userLotteryPush = new ConcurrentHashMap<String, TargetSocketType>();

    @Autowired
    private UserCapitalService userCapitalService;


    @Autowired
    private DgsRequestMangerService requestMangerService;


    @Autowired
    private DgsBetRecordService dgsBetRecordService;


    @Autowired
    private MonsterService monsterService;
    @Autowired
    private UserCapitalCacheService userCapitalCacheService;
    @Autowired
    private GameService gameService;

    private static Object lock = new Object();
    public static int STATUS;


    public static int PERIODS_NUM;

    public static Integer LAST_RESULT;

    public static Long beginTime;

    public static Long endTime;

    public static BigDecimal ALL_PRIZE = BigDecimal.ZERO;
    private BigDecimal PERIOD;
    private Integer gameStatus1;
    private Integer gameStatus2;
    private Integer gameStatus3;


    public static final Map<String, DgsBetRecord> userOrders = new ConcurrentHashMap<>();


    public static final Set<String> BET_USERS = new HashSet<>();

    public static Integer people_num = 0;

    public static  Integer blood = 0;

    public static final Map<String, JSONObject> USER_MAP = new ConcurrentHashMap<>();
    public static final Set<String> users = new HashSet<>();

    //加入房间中的所有人
    public static final Map<String, Set<String>> userMap = new ConcurrentHashMap<>();

    //攻击过的用户map
    public static final Map<String, Set<String>> atkMap = new ConcurrentHashMap<>();

    //怪兽信息
    public static final Map<String,  Monster> atkMonsterMap = new ConcurrentHashMap<>();

    //所有攻击过的用户订单
    public static final Map<String, Map<String, DgsBetRecord>> atkRecordMap = new ConcurrentHashMap<>();

    //结算信息



    public static Map<String, List<Map<String, String>>> userCapitals = new ConcurrentHashMap<>();

    public static String key = DateUtil.getCurrent5();
    public static String key2 = DateUtil.getCurrent5();
    public static final Map<String, JSONArray> pushArray = new ConcurrentHashMap<>();

    public static String key3 = DateUtil.getCurrent5();
    public static Map<String, List<Map<String, String>>> userRankCapitals = new ConcurrentHashMap<>();


    public static Map<String, Integer> MONSTER_STATUS = new ConcurrentHashMap<>();


    private static final Map<String, Map<String, BigDecimal>> settleInfo = new ConcurrentHashMap<>();




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


    @PostConstruct
    public void _construct() {
        initGameSetting();
        initUserMap();
        initMonster();
        initOrder();
        requestManagerUpdateCapital();
        init();
        initGameStatus();
        addPushSuport();
    }

    public void init() {
        MONSTER_STATUS.clear();
        userOrders.clear();
        settleInfo.clear();
        PERIODS_NUM = PERIODS_NUM + 1;
        BET_USERS.clear();
    }

    public void initGameSetting() {
        logger.info("初始化2选1游戏配置");
        Game game = gameService.findGameById(10L);
        if (game != null) {
            JSONObject gameSetting = JSON.parseObject(game.getGameSetting());
            people_num = gameSetting.getIntValue("people");
            STATUS = game.getStatus();
            blood = gameSetting.getIntValue("blood");
        }
        logger.info("初始化2选1游戏配置完成");
    }


    public void addPushSuport() {
        Push.addPushSuport(PushCode.updateDgsInfo, new DefaultPushHandler());
        Push.addPushSuport(PushCode.updateDgsStatus, new DefaultPushHandler());
        Push.addPushSuport(PushCode.updateGameStatus, new DefaultPushHandler());
    }



    public void  initMonster(){
        //查询dieStatus为未死亡的怪兽
        Monster monster1 = monsterService.findMonsterByStatus(1, 0,blood);
        Monster monster2 =monsterService.findMonsterByStatus(10,0,blood);
        Monster monster3 =monsterService.findMonsterByStatus(100,0,blood);
        atkMonsterMap.put("1",monster1);
        atkMonsterMap.put("10",monster2);
        atkMonsterMap.put("100",monster3);

    }
    public void  initOrder(){
        //查询dieStatus为未死亡的怪兽
        List<DgsBetRecord> unSettleByMonsterNoList1 = dgsBetRecordService.findUnSettleByMonsterNo(atkMonsterMap.get("1").getMonsterNo());
        List<DgsBetRecord> unSettleByMonsterNoList2 = dgsBetRecordService.findUnSettleByMonsterNo(atkMonsterMap.get("10").getMonsterNo());
        List<DgsBetRecord> unSettleByMonsterNoList3 = dgsBetRecordService.findUnSettleByMonsterNo(atkMonsterMap.get("100").getMonsterNo());
        for (DgsBetRecord dgsBetRecord : unSettleByMonsterNoList1) {
            atkRecordMap.get("1").put(String.valueOf(dgsBetRecord.getUserId()),dgsBetRecord);
        }
        for (DgsBetRecord dgsBetRecord : unSettleByMonsterNoList2) {
            atkRecordMap.get("10").put(String.valueOf(dgsBetRecord.getUserId()),dgsBetRecord);
        }
        for (DgsBetRecord dgsBetRecord : unSettleByMonsterNoList3) {
            atkRecordMap.get("100").put(String.valueOf(dgsBetRecord.getUserId()),dgsBetRecord);
        }
    }



    public void initGameStatus() {
        //正常是游戏状态，可下注；结算时是结算状态不可下注
        gameStatus1 = 1;
        gameStatus2 = 1;
        gameStatus3 = 1;

    }

    public void initUserMap() {
        //正常是游戏状态，可下注；结算时是结算状态不可下注
        userMap.put("1", new HashSet<>());
        userMap.put("10", new HashSet<>());
        userMap.put("100", new HashSet<>());
        atkMap.put("1", new HashSet<>());
        atkMap.put("10", new HashSet<>());
        atkMap.put("100", new HashSet<>());
        atkRecordMap.put("1", new HashMap<>());
        atkRecordMap.put("10", new HashMap<>());
        atkRecordMap.put("100", new HashMap<>());
    }

    public void updateStatus(int status) {
        STATUS = status;
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
                        Executer.request(TargetSocketType.dgs, CommandBuilder.builder().request("200808", object).build(), new RequestManagerListener(null));
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
                        Push.push(PushCode.updateDgsInfo, null, data);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }, 0, 1);
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


    @Transactional
    @ServiceMethod(code = "101", description = "用户加入房间")
    public Object jionRoom(DgsService adminSocketServer, Command lotteryCommand, JSONObject data) {
        checkNull(data);
        checkNull(data.get("userId"));
        String userId = data.getString("userId");
        String name = data.getString("userName");
        String monsterType = data.getString("bet");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("headImgUrl", data.getString("headImgUrl"));
        jsonObject.put("userNo", data.getString("userNo"));
        jsonObject.put("userName", data.getString("userName"));
        USER_MAP.put(userId, jsonObject);
        userMap.get(monsterType).add(userId);
        JSONObject returnInfo = getReturnInfo(monsterType);
        return returnInfo;
    }

    public JSONObject getReturnInfo(String monsterType) {
        JSONObject result = new JSONObject();
        HashMap<String, Integer> newHashMap = new HashMap<>();
        newHashMap.put(monsterType, MONSTER_STATUS.get(monsterType));
        result.put("monsterStatus", newHashMap);
        if ("1".equals(monsterType)) {
            result.put("gameStatus", gameStatus1);
        }
        if ("10".equals(monsterType)) {
            result.put("gameStatus", gameStatus2);
        }
        if ("100".equals(monsterType)) {
            result.put("gameStatus", gameStatus3);
        }
        result.put("monsterInfo",atkMonsterMap.get(monsterType));
        result.put("isUpdate",-1);
        return result;
    }

    @Transactional
    @ServiceMethod(code = "104", description = "用户离开房间")
    public Object leveRoom(DgsService adminSocketServer, Command lotteryCommand, JSONObject data) {
        String userId = data.getString("userId");
        if (userMap.get("1").contains(userId)) {
            userMap.get("1").remove(userId);
        }
        if (userMap.get("10").contains(userId)) {
            userMap.get("10").remove(userId);
        }
        if (userMap.get("100").contains(userId)) {
            userMap.get("100").remove(userId);
        }
        USER_MAP.remove(userId);
        return new JSONObject();
    }


    public void ifStatus(Integer monsterType){
        if(1 == monsterType && gameStatus1 != 1){
            throwExp("当前禁止参与");
        }
        if(10 == monsterType && gameStatus2 != 1){
            throwExp("当前禁止参与");
        }
        if(100 == monsterType && gameStatus3 != 1){
            throwExp("当前禁止参与");
        }
    }

    public JSONObject pushResult(int monsterType, String userId) {
        JSONObject pushResult = new JSONObject();
        pushResult.put("monsterType",monsterType);
        pushResult.put("userInfo",USER_MAP.get(userId));
        pushResult.put("monsterInfo",atkMonsterMap.get(String.valueOf(monsterType)));
        pushResult.put("gameStatus",getGameStatus(monsterType));
        pushResult.put("gameId","10");
        pushResult.put("isUpdate",0);
        return pushResult;
    }

    /**
     * @param
     */
    @Transactional
    @ServiceMethod(code = "103", description = "击杀")
    public JSONObject attackBoss(DgsService adminSocketServer, Command lotteryCommand, JSONObject data) throws InterruptedException {
        Integer monsterType = data.getInteger("bet");
        Integer userId = data.getInteger("userId");
        String orderNo = OrderUtil.getBatchOrder32Number();
        //判断游戏状态
        //ifStatus(monsterType);
        synchronized (lock) {
            if (atkRecordMap.get(monsterType.toString()).containsKey(userId.toString())) {
                throwExp("当前用户已经参加过本次击打活动");
            }
            //生成订单

            DgsBetRecord record = dgsBetRecordService.addRecord(Long.valueOf(userId), orderNo, monsterType,atkMonsterMap.get(monsterType.toString()).getMonsterNo());
            //更改资产
            updateCapital(String.valueOf(userId), BigDecimal.valueOf(monsterType), orderNo, record.getId());
            atkMap.get(monsterType.toString()).add(String.valueOf(userId));
            atkRecordMap.get(monsterType.toString()).put(userId.toString(),record);
            atkMonsterMap.get(monsterType.toString()).setCurrBlood(atkMonsterMap.get(monsterType.toString()).getCurrBlood()-100);
            monsterService.updateMonster(atkMonsterMap.get(monsterType.toString()));
            //推送怪兽数据+用户信息
            JSONObject jsonObject = pushResult(monsterType, userId.toString());
            //判断是否需要结算
            if(atkMonsterMap.get(monsterType.toString()).getCurrBlood() == 0 ){
                Thread.sleep(300);
                Map<String, Map<String, Object>> settle = settle(lotteryCommand, monsterType);
                jsonObject = pushResult(monsterType, userId.toString());
                jsonObject.put("settleInfo",settle);
                jsonObject.put("isUpdate",1);
            }
            Push.push(PushCode.updateDgsInfo, null, jsonObject);
        }
        return  new JSONObject();
    }

    private Map<String, Map<String, Object>> settle(Command lotteryCommand,Integer monsterType) {
        Map<String, Map<String, Object>> settleInfo = new ConcurrentHashMap<>();
        JSONObject result = new JSONObject();
        //怪兽最后一击时返回游戏状态为结算中
        if (1 == monsterType) {
            gameStatus1 = 2;
            result.put("gameStatus", gameStatus1);
        }
        if (10 == monsterType) {
            gameStatus2 = 2;
            result.put("gameStatus", gameStatus2);
        }
        if (100 == monsterType) {
            gameStatus3 = 2;
            result.put("gameStatus", gameStatus3);
        }
        //开始瓜分红包
        //获取暴雷的人
        List<DgsBetRecord> newList = new ArrayList<>(atkRecordMap.get(monsterType.toString()).values());
        Collections.shuffle(newList);
        //扣除10%的手续费，剩余的为奖励金额
        double rewardAmount = (monsterType - monsterType * 0.1) * 1000;
        List<BigDecimal> redPacketList = divideRedPacket(rewardAmount, people_num-1);
        JSONObject jsonObject = new JSONObject();
        for (int i = 0; i < newList.size(); i++) {
            Map<String , Object> map = new HashMap<>();
            JSONObject data = new JSONObject();
            if(i!= people_num-1){
                //赢的人
                newList.get(i).setProfit(newList.get(i).getBetAmount().add(redPacketList.get(i)));
                newList.get(i).setStatus(1);
                //server用来推送的用户的
                map.put("winAmount", redPacketList.get(i).add(newList.get(i).getBetAmount()));
                map.put("betAmount", newList.get(i).getBetAmount());
                map.put("isWin", BigDecimal.ONE);
                //manger用来更改资产
                data.put("amount", redPacketList.get(i).add(newList.get(i).getBetAmount()));
                data.put("capitalType", UserCapitalTypeEnum.yyb.getValue());
                data.put("orderNo",newList.get(i).getOrderNo());
                data.put("em", LogCapitalTypeEnum.dgs_bet.getValue());
                data.put("type", 1);

            }else{
              //输的人
                newList.get(i).setProfit(BigDecimal.ZERO);
                newList.get(i).setStatus(1);
                map.put("winAmount", 0);
                map.put("betAmount", newList.get(i).getBetAmount());
                map.put("isWin", BigDecimal.ZERO);
                data.put("orderNo",newList.get(i).getOrderNo());
                data.put("type", 2);
                data.put("userId", newList.get(i).getUserId());

            }
            jsonObject.put(String.valueOf(newList.get(i).getUserId()),data);
            //推送给server
            settleInfo.put(String.valueOf(newList.get(i).getUserId()),map);
        }
        //更改怪兽为已死亡状态
        atkMonsterMap.get(monsterType.toString()).setDieStatus(1);
        monsterService.updateMonster(atkMonsterMap.get(monsterType.toString()));
        dgsBetRecordService.batchUpdateRecord(newList);
        //结算
        requestMangerService.requestManagerBet(jsonObject, new Listener() {
            public void handle(BaseClientSocket clientSocket, Command command) {
                if (command.isSuccess()) {
                    //批量更新订单到数据库
                   //dgsBetRecordService.batchUpdateRecord(newList);
                    if (lotteryCommand != null) {
                        Executer.response(CommandBuilder.builder(lotteryCommand).success(result).build());
                    }
                } else {
                    STATUS = 0;
                    logger.error("结算失败，本期数据：");
                    logger.info(result);
                    Executer.response(
                            CommandBuilder.builder(lotteryCommand).error(command.getMessage(), command.getData()).build());
                }
            }
        });
        JSONObject pushData = new JSONObject();
        pushData.put("status",getGameStatus(monsterType));
        pushData.put("userSettleInfo",settleInfo);
        pushData.put("gameId",10);
        pushData.put("userIds",userMap.get(monsterType.toString()));

        //推送结算状态为结算中
        logger().info("第一次结算推送pushdata"+pushData);
       // Push.push(PushCode.updateDgsStatus,null,pushData);
        //初始化数据 （新怪兽、clear map、）map
        beginGame(monsterType);
        return settleInfo;
        //更改状态为游戏中 推送
    }

    public void beginGame(Integer monsterType){
        atkMap.get(monsterType.toString()).clear();
        atkRecordMap.get(monsterType.toString()).clear();
        Long newMonsterNo =  (atkMonsterMap.get(monsterType.toString()).getMonsterNo()+1);
        Monster monster = monsterService.addMonster(monsterType, 0, newMonsterNo,blood);
        atkMonsterMap.put(monsterType.toString(),monster);
        if(monsterType == 1){
            gameStatus1 = 1;
        }
        if(monsterType == 10){
            gameStatus2 = 1;
        }
        if(monsterType == 100){
            gameStatus3 = 1;
        }
        /*JSONObject pushData = new JSONObject();
        pushData.put("gameId",10);
        pushData.put("userIds",userMap.get(monsterType.toString()));
        pushData.put("status",getGameStatus(monsterType));
        pushData.put("monsterInfo",atkMonsterMap.get(monsterType.toString()));
        logger().info("第二次重置怪兽推送pushdata"+pushData);
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            logger.info(e);
        }
        Push.push(PushCode.updateDgsStatus,null,pushData);*/
    }
    public  Integer getGameStatus(Integer monsterType){
        if(monsterType == 1){
            return gameStatus1;
        }
        if(monsterType == 10){
            return gameStatus2;
        }
        if(monsterType == 100){
            return gameStatus3;
        }
        return null;
    }


    private DgsBetRecord buildCurrentUserRecord(Integer monsterType, Integer userId, String orderNo) {
        DgsBetRecord dgsBetRecord1 = new DgsBetRecord();
        dgsBetRecord1.setMonsterId(monsterType);
        dgsBetRecord1.setUserId(userId);
        dgsBetRecord1.setStatus(1);
        dgsBetRecord1.setCreateTime(new Date());
        dgsBetRecord1.setUpdateTime(new Date());
        dgsBetRecord1.setOrderNo(orderNo);
        return dgsBetRecord1;
    }

    /**
     * 二倍均值法的算法实现 - 算法里面的金额以 灵石的1000倍后 为单位 ，相当于1灵石=1000子灵石
     *
     * @param totalAmount 红包总金额
     * @param totalPeople 红包总人数
     * @return
     */
    public static List<BigDecimal> divideRedPacket(final Double totalAmount, final Integer totalPeople) {
        List<BigDecimal> list = Lists.newLinkedList();
        if (totalAmount > 0 && totalPeople > 0) {
            Double restAmount = totalAmount;
            Double restPeople = Double.valueOf(totalPeople);
            BigDecimal realAmount;
            Random random = new Random();
            Double amount;
            for (int i = 0; i < totalPeople - 1; i++) {
                //左闭右开 [1,剩余金额/剩余人数 的除数 的两倍 )
                double v = restAmount / restPeople * 2.0 - 1.0;
                amount = 0 + (v - 0) * random.nextDouble() + 1;
                realAmount = new BigDecimal(amount).divide(new BigDecimal(1000)).setScale(2, RoundingMode.HALF_UP);
                list.add(realAmount);
                //剩余金额
                restAmount -= amount;
                restPeople--;
            }
            //最后的剩余金额
            list.add(new BigDecimal(restAmount).divide(new BigDecimal(1000)));
        }
        return list;
    }


    @ServiceMethod(code = "004", description = "获取统计记录")
    public JSONObject getRecord(DgsService adminSocketServer, Command lotteryCommand, JSONObject data) {
        checkNull(data);
        checkNull(data.get("userId"));
        Long userId = data.getLong("userId");
        int page = data.getIntValue("page");
        int num = data.getIntValue("num");
        Long monsterId = data.getLong("bet");
        //获取我投入的以及我获得的游园券总值
        HashMap<String,BigDecimal> yybNum = dgsBetRecordService.findYybNum(userId);
        //获取所有记录
        List<DgsBetRecord> dgsBetRecords = dgsBetRecordService.findByStatusLimit( userId, page, num);

        JSONArray resultArray = new JSONArray();
        for (DgsBetRecord record : dgsBetRecords) {
            JSONObject obj = new JSONObject();
            obj.put("monsterType", record.getMonsterId());
            obj.put("betAmount", (record.getBetAmount() != null) ? record.getBetAmount() : BigDecimal.ZERO);
            obj.put("profit", (record.getProfit() != null) ? record.getProfit() : BigDecimal.ZERO);
            obj.put("orderNo", record.getOrderNo());
            obj.put("status", record.getStatus());
            obj.put("create", record.getCreateTime());
            obj.put("update", record.getUpdateTime());
            resultArray.add(obj);
        }
        JSONObject result = new JSONObject();
        result.put("myRecord", resultArray);
        result.put("yybNum",yybNum);
        return result;
    }




}



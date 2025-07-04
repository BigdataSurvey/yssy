package com.zywl.app.service;

import cn.hutool.core.lang.Snowflake;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.google.common.collect.Lists;
import com.live.app.ws.bean.Command;
import com.live.app.ws.enums.PushCode;
import com.live.app.ws.util.DefaultPushHandler;
import com.live.app.ws.util.Push;
import com.zywl.app.base.bean.Monster;
import com.zywl.app.base.bean.*;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.base.util.LockUtil;
import com.zywl.app.base.util.OrderUtil;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.enmus.LogCapitalTypeEnum;
import com.zywl.app.defaultx.enmus.LotteryGameStatusEnum;
import com.zywl.app.defaultx.enmus.UserCapitalTypeEnum;
import com.zywl.app.defaultx.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@ServiceClass(code = "111")
public class DgsService extends BaseService {

     //public static ConcurrentHashMap<Monster, TargetSocketType> userLotteryPush = new ConcurrentHashMap<String, TargetSocketType>();

    @Autowired
    private UserCapitalService userCapitalService;




    @Autowired
    private GameService gameService;


    @Autowired
    private DgsRequestMangerService requestMangerService;


    @Autowired
    private DgsBetRecordService dgsBetRecordService;



    @Autowired
    private MonsterService monsterService;

    private static Object lock = new Object();
    public static int STATUS;

    public static int CAPITAL_TYPE;

    public static int PERIODS_NUM;

    public static Integer LAST_RESULT;

    public static Long beginTime;

    public static Long endTime;

    public static BigDecimal ALL_PRIZE = BigDecimal.ZERO;
    private BigDecimal PERIOD;
    private BigDecimal BLOOD;
    private BigDecimal GAME_STATUS;


    public static final Map<String, DgsBetRecord> userOrders = new ConcurrentHashMap<>();


    public static final Set<String> BET_USERS = new HashSet<>();

    public static final Map<String, Object> USER_MAP = new ConcurrentHashMap<>();
    public static final Set<String> users = new HashSet<>();

    public static Map<String, List<Map<String, String>>> userCapitals = new ConcurrentHashMap<>();

    public static String key = DateUtil.getCurrent5();


    public static Map<Integer, Integer> MONSTER_STATUS = new ConcurrentHashMap<>();


    private static final Map<String, Map<String, BigDecimal>> settleInfo = new ConcurrentHashMap<>();




    @PostConstruct
    public void _construct() {
        init();
        initPeriod();
        initGameStatus();
        initBlood();
        addPushSuport();
    }
    public void init() {
        MONSTER_STATUS.clear();
        userOrders.clear();
        settleInfo.clear();
        PERIODS_NUM = PERIODS_NUM + 1;
        BET_USERS.clear();
    }
    public void addPushSuport() {
        Push.addPushSuport(PushCode.updateDgsInfo, new DefaultPushHandler());
        Push.addPushSuport(PushCode.updateDgsStatus, new DefaultPushHandler());
    }
    public void initPeriod() {
        PERIOD = new BigDecimal("1");
    }
    public void initGameStatus() {
        //正常是游戏状态，可下注；结算时是结算状态不可下注
        GAME_STATUS = new BigDecimal("1");
    }

    public void initBlood() {
        Map<Integer, Integer> initStatus = monsterService.findMonsterInitStatus(0);
        if(initStatus.get(1) == null ){
            MONSTER_STATUS.put(1,2000);
        }else {
            MONSTER_STATUS.put(1,initStatus.get(1));
        }
        if(initStatus.get(10) == null ){
            MONSTER_STATUS.put(10,2000);
        }else {
            MONSTER_STATUS.put(10,initStatus.get(10));
        }
        if(initStatus.get(100) == null ){
            MONSTER_STATUS.put(100,2000);
        }else {
            MONSTER_STATUS.put(100,initStatus.get(100));
        }

    }

    public void updateStatus(int status) {
        STATUS = status;
    }
    public void updatePeriod(BigDecimal period) {
        PERIOD = period;
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
        USER_MAP.put(userId, name);
        users.add(userId);
        JSONObject returnInfo = getReturnInfo();
        return returnInfo;
    }

    public JSONObject getReturnInfo() {
        JSONObject result = new JSONObject();
        result.put("monsterStatus", MONSTER_STATUS);
        result.put("gameStatus", GAME_STATUS);
        return result;
    }

    @Transactional
    @ServiceMethod(code = "104", description = "用户离开房间")
    public Object leveRoom(DgsService adminSocketServer, Command lotteryCommand, JSONObject data) {
        String userId = data.getString("userId");
        users.remove(userId);
        USER_MAP.remove(userId);
        return new JSONObject();
    }

    /**
     * @param
     */
    @Transactional
    @ServiceMethod(code = "103", description = "击杀")
    public void attackBoss(DgsService adminSocketServer, Command lotteryCommand, JSONObject data){
        Integer monsterType = data.getInteger("bet");
        Integer userId = data.getInteger("userId");

            //定义怪兽的血量
            int dieStatus = 0 ;
            List<Object> list = new ArrayList<>();
            String orderNo = OrderUtil.getBatchOrder32Number();
            synchronized (lock) {
                //没有死亡的以及当前类型的怪兽
                List<Monster> monsterList = monsterService.findMonsterByStatus(monsterType, dieStatus);
                List<DgsBetRecord> recordList = dgsBetRecordService.findByStatus(Long.valueOf(monsterType));
                monsterList.stream().forEach(item->list.add(item.getUserId()));
                List<Monster> newList = new ArrayList<>(monsterList);
                Monster monster = new Monster();
                monster.setCreateTime(new Date());
                monster.setUpdateTime(new Date());
                monster.setUserId(userId);
                monster.setMonsterType(monsterType);
                monster.setDieStatus(dieStatus);
                //判断当前怪兽的血量（查询当前类型下的最低血量 是否为0 如果为0 修改当前类型下的怪兽所有类型下的死亡状态为已死亡，如果不为0的话，继续击杀）
                if(list.contains(userId)){
                    throwExp("当前用户已经参加过本次击打活动");
                }
                JSONObject result = new JSONObject();
                if(monsterList != null && monsterList.size()>0){
                    Integer currBlood= monsterList.get(0).getCurrBlood()-100;
                    monster.setCurrBlood(currBlood);
                     monsterService.save(monster);
                    dgsBetRecordService.addRecord(Long.valueOf(userId),orderNo,monsterType);
                    newList.add(monster);
                    if(monsterList.get(0).getCurrBlood() == 100) {
                        //开始瓜分红包
                        List<BigDecimal> redPacketList = getRedPacket(monsterList);

                        for (int i = 0; i < newList.size(); i++) {
                            JSONObject monsterObject = new JSONObject();
                            Monster monster1 = newList.get(i);
                            BigDecimal profit = redPacketList.get(i);
                            BigDecimal amount = BigDecimal.valueOf(monsterType).add(profit);
                            monster1.setProfit(profit);
                            monster1.setBetAmount(amount);
                            monster1.setDieStatus(1);
                            monsterObject.put("orderNo", orderNo);
                            monsterObject.put("profit", profit);
                            monsterObject.put("betAmount", amount);
                            monsterObject.put("userId", userId);
                            userCapitalService.addUserBalanceByAddReward(amount, Long.valueOf(userId), UserCapitalTypeEnum.currency_2.getValue(), LogCapitalTypeEnum.yyb_winning);
                        }
                        result.put("monsterList",newList);

                        //批量修改怪兽表本期怪兽的状态以及获奖金额
                        monsterService.batchUpdateBetAmount(newList);
                        //构建当前用户订单+之前订单 一起结算
                        DgsBetRecord dgsBetRecord1 = buildCurrentUserRecord(monsterType, userId, orderNo);
                        recordList.add(dgsBetRecord1);
                        //批量修改订单金额
                        for (DgsBetRecord dgsBetRecord : recordList) {
                            for (Monster monster1 : newList) {
                                if(dgsBetRecord.getUserId()==monster1.getUserId() ){
                                    dgsBetRecord.setProfit(monster1.getProfit());
                                    dgsBetRecord.setBetAmount(monster1.getBetAmount());
                                    dgsBetRecord.setStatus(1);
                                }
                            }
                        }
                        dgsBetRecordService.batchUpdateRecord(recordList);
                        result.put("recordList",recordList);
                    }
                }else{
                    monster.setCurrBlood(2000 - 100);
                    monsterService.save(monster);
                    dgsBetRecordService.addRecord(Long.valueOf(userId),orderNo,monsterType);
                }
                Push.push(PushCode.updateDgsStatus,String.valueOf(userId), result);
            }
        }

        private DgsBetRecord buildCurrentUserRecord(Integer monsterType,Integer userId,String orderNo){
            DgsBetRecord dgsBetRecord1 = new DgsBetRecord();
            dgsBetRecord1.setMonsterId(monsterType);
            dgsBetRecord1.setUserId(userId);
            dgsBetRecord1.setStatus(1);
            dgsBetRecord1.setCreateTime(new Date());
            dgsBetRecord1.setUpdateTime(new Date());
            dgsBetRecord1.setOrderNo(orderNo);
            return dgsBetRecord1;
        }

        public static List<BigDecimal> getRedPacket(List<Monster> monsterList){
            Random random = new Random();
            int randomIndex = random.nextInt(monsterList.size()); // 生成一个[0, list.size())范围内的随机索引
            Monster monster1 = monsterList.get(randomIndex);// 通过随机索引获取元素
            monsterList.remove(monsterList.get(randomIndex)); //删掉该未中奖用户
            //需要瓜分的金额
            Integer monsterType1 = Math.toIntExact(monster1.getMonsterType());
            //扣除10%的手续费，剩余的为奖励金额
            double rewardAmount = (monsterType1 - monsterType1 * 0.1) * 1000;
            return divideRedPacket(rewardAmount, 5);
        }
    /**
     *
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
            BigDecimal realAmount ;
            Random random = new Random();
            Double amount;
            for (int i = 0; i < totalPeople - 1; i++) {
                //左闭右开 [1,剩余金额/剩余人数 的除数 的两倍 )
                double v = restAmount / restPeople * 2.0 - 1.0;
                amount = 0 + (v - 0) * random.nextDouble()+1;
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


    

    public static void main(String[] args) {
        List<BigDecimal> bigDecimals = divideRedPacket(200000.0, 19);
        System.out.println(bigDecimals);
    }








    }



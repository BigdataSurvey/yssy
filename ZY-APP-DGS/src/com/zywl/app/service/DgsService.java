package com.zywl.app.service;

import cn.hutool.core.lang.Snowflake;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.google.common.collect.Lists;
import com.live.app.ws.bean.Command;
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
    private BackpackService backpackService;


    @Autowired
    private GameService gameService;


    @Autowired
    private DgsRequestMangerService requestMangerService;


    @Autowired
    private DgsBetRecordService dgsBetRecordService;


    @Autowired
    private UserDtsAmountService userDtsAmountService;

    @Autowired
    private GameLotteryResultService gameLotteryResultService;

    @Autowired
    private DgsService dgsService;

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

    private static final String RedPacketKey = "SpringRedis:RedPacket:%s:%s";
    private BigDecimal PERIOD;


    public static final Map<String, DgsBetRecord> userOrders = new ConcurrentHashMap<>();

    public static final Set<String> users = new HashSet<>();

    public static final Set<String> BET_USERS = new HashSet<>();

    public static final Map<String, Object> USER_MAP = new ConcurrentHashMap<>();

    public static Map<String, List<Map<String, String>>> userCapitals = new ConcurrentHashMap<>();

    public static String key = DateUtil.getCurrent5();


    public static Map<String, Map<String, JSONObject>> ROOM_LIST = new ConcurrentHashMap<>();


    private static final Map<String, Map<String, BigDecimal>> settleInfo = new ConcurrentHashMap<>();

    public static String key2 = DateUtil.getCurrent5();

    public static final Map<String, JSONArray> pushArray = new ConcurrentHashMap<>();


    @PostConstruct
    public void _construct() {
        init();
        initPeriod();
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
    public void initPeriod() {
        PERIOD = new BigDecimal("1");
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


    public Object play(DgsService adminSocketServer, Command lotteryCommand, JSONObject data) {
        String betInfo = data.getString("bet");
        BigDecimal amount = data.getBigDecimal("betAmount");
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
            String orderNo = "";
            Long dataId = null;
            //得到订单信息
            if (BET_USERS.contains(userId)) {
                // todo 查询怪兽表 已经投入过了  寻找他的订单
                DgsBetRecord dgsBetRecord = userOrders.get(userId);
                if (dgsBetRecord != null) {
                    throwExp("已参与过该活动");
                }
            } else {
                orderNo = OrderUtil.getOrder5Number();
                DgsBetRecord record = dgsBetRecordService.addRecord(Long.parseLong(userId), orderNo, amount);
                dataId = record.getId();
                userOrders.put(userId, record);
            }
            //处理资产信息
            updateCapital(userId, amount, orderNo, dataId);
            //本局总金额
            ALL_PRIZE = ALL_PRIZE.add(amount);
            //处理内存信息
            BigDecimal myAllAmount = amount;
            if (ROOM_LIST.get(betInfo).containsKey(userId)) {
                //已经参与过了
                ROOM_LIST.get(betInfo).get(userId).put("betAmount", ROOM_LIST.get(betInfo).get(userId).getBigDecimal("betAmount").add(amount));
                myAllAmount = ROOM_LIST.get(betInfo).get(userId).getBigDecimal("betAmount");
                DgsBetRecord dgsBetRecord = userOrders.get(userId);
                dgsBetRecord.setBetAmount(dgsBetRecord.getBetAmount().add(amount));
                dgsBetRecordService.updateRecord(dgsBetRecord);
            } else {
                BET_USERS.add(userId);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("name", USER_MAP.getOrDefault(userId, "***"));
                jsonObject.put("betAmount", amount);
                ROOM_LIST.get(betInfo).put(userId, jsonObject);
            }
                JSONObject result = new JSONObject();
                result.put("myBetInfo", data);
                return result;
            }
        }

    /**
     * @param
     */
    @Transactional
    @ServiceMethod(code = "103", description = "击杀")
    public void attackBoss(DgsService adminSocketServer, Command lotteryCommand, JSONObject data){
        Long monsterType = data.getLong("bet");
        BigDecimal betAmount = data.getBigDecimal("betAmount");
        Long userId = data.getLong("userId");
            Monster monster = new Monster();
            //定义怪兽的血量
            int dieStatus = 0 ;
            List<Object> list = new ArrayList<>();
            synchronized (lock) {
                //没有死亡的以及当前类型的怪兽
                List<Monster> monsterList = monsterService.findMonsterByStatus(monsterType, dieStatus);
                monsterList.stream().forEach(item->list.add(item.getUserId()));
                monster.setCreateTime(new Date());
                monster.setUpdateTime(new Date());
                monster.setUserId(userId);
                monster.setMonsterType(monsterType);
                monster.setDieStatus(dieStatus);
                //判断当前怪兽的血量（查询当前类型下的最低血量 是否为0 如果为0 修改当前类型下的怪兽所有类型下的死亡状态为已死亡，如果不为0的话，继续击杀）
                if(list.contains(userId)){
                    throwExp("当前用户已经参加过本次击打活动");
                }
                if(monsterList != null && monsterList.size()>0){
                    if(monsterList.get(0).getCurrBlood() == 100){
                        monster.setCurrBlood(monsterList.get(0).getCurrBlood()-100);
                        monsterService.insert(monster);
                        //开始瓜分红包
                        Random random = new Random();
                        int randomIndex = random.nextInt(monsterList.size()); // 生成一个[0, list.size())范围内的随机索引
                        Monster monster1 = monsterList.get(randomIndex);// 通过随机索引获取元素
                        monsterList.remove(monsterList.get(randomIndex)); //删掉该未中奖用户
                        //需要瓜分的金额
                        Integer monsterType1 = Math.toIntExact(monster1.getMonsterType());
                        //扣除10%的手续费，剩余的为奖励金额
                        double rewardAmount = (monsterType1 - monsterType1 * 0.1) * 1000;
                        List<BigDecimal> bigDecimals = divideRedPacket(rewardAmount, 3);
                        JSONObject jsonObject = new JSONObject();
                        for (BigDecimal profit : bigDecimals) {
                            for (Monster monster2 : monsterList) {
                                JSONObject monsterObject = new JSONObject();
                                String orderNo = OrderUtil.getBatchOrder32Number();
                                BigDecimal amount = BigDecimal.valueOf(monsterType).add(profit);
                                monster2.setProfit(profit);
                                monster2.setBetAmount(amount);
                                monster2.setDieStatus(1);
                                monsterObject.put("orderNo",orderNo);
                                monsterObject.put("profit",profit);
                                monsterObject.put("betAmount",betAmount);
                                monsterObject.put("userId",userId);
                                jsonObject.put("monsterObject",monsterObject);
                                monsterService.updateMonSterStatus(monster2);
                                userCapitalService.addUserBalanceByAddReward(amount,userId, UserCapitalTypeEnum.currency_2.getValue(), LogCapitalTypeEnum.yyb_winning);
                            }
                        }
                        jsonObject.put("orderNo",jsonObject);
                        //批量新增怪兽订单
                        dgsBetRecordService.batchAddBetAmount(jsonObject);
                    }else{
                        monster.setCurrBlood(monsterList.get(0).getCurrBlood()-100);
                        monsterService.insert(monster);
                    }
                }else{
                    monster.setCurrBlood(2000 - 100);
                    monsterService.insert(monster);
                }


            }
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



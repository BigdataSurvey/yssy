package com.zywl.app.service;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.bean.Command;
import com.live.app.ws.enums.PushCode;
import com.live.app.ws.enums.TargetSocketType;
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
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.enmus.UserCapitalTypeEnum;
import com.zywl.app.defaultx.service.*;
import com.zywl.app.socket.NSSocketServer;
import com.zywl.app.util.RequestManagerListener;
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
public class DnsService extends BaseService {


    @Autowired
    private NsService nsService;

    @Autowired
    private UserCapitalService userCapitalService;

    @Autowired
    private UserNsPrizeService userNsPrizeService;

    @Autowired
    private UserAttackNsRecordService userAttackNsRecordService;

    @Autowired
    private UserCacheService userCacheService;

    @Autowired
    private GameNsService gameNsService;

    @Autowired
    private OpenBoxRecordService openBoxRecordService;

    @Autowired
    private NsSettleService nsSettleService;

    @Autowired
    private KillNsRecordService killNsRecordService;

    public static int STATUS = 0;

    public static Map<String, List<Map<String, String>>> userCapitals = new ConcurrentHashMap<>();

    public static Map<String, List<Map<String, String>>> userBoxCapitals = new ConcurrentHashMap<>();

    public static Map<String, List<Map<String, String>>> userAttackCapitals = new ConcurrentHashMap<>();

    public static String key = DateUtil.getCurrent5();

    public static String key2 = DateUtil.getCurrent5();

    public static String key3 = DateUtil.getCurrent5();

    private static final Map<String, BigDecimal> nsInfo = new ConcurrentHashMap<>();

    private static final Map<String, Map<String, BigDecimal>> userBetInfo = new ConcurrentHashMap<>();

    private static final Map<String, Map<String, UserNsPrize>> userBoxInfo = new ConcurrentHashMap<>();

    private static int ROUNDS;

    private static String NOW_ID;

    private static BigDecimal NOW_HP;

    private static BigDecimal ALL_HP;

    private static BigDecimal ALL_PRIZE;


    private static String LAST_USER_NO;

    private static String LAST_HEAD_IMG;

    private static String LAST_USER_NAME;

    private static Long LAST_USER_ID;

    private static int isDie;

    private static int needPush = 0;

    private static BigDecimal AMOUNT = BigDecimal.ZERO;
    @Autowired
    private DnsService dnsService;

    private static final Object lock = new Object();

    private static final List<String> USER_QUEUE = new CopyOnWriteArrayList<>();

    static final AtomicInteger boss_red_buff = new AtomicInteger(-1);

    public void resetBoss(int hp) {
        boss_red_buff.set(hp);
    }

    /**
     * @param attack ×10
     * @return [boss状态, 剩余血条]
     * 未死亡			[0, 剩余血条]
     * 死亡最后一击		[1]
     * 已死亡非最后一击	[2]
     */
    public final int[] attack(int attack) {
        int newredbuff = boss_red_buff.addAndGet(-attack);
        if (newredbuff < 0) {
            if (newredbuff + attack >= 0) {
                //最后一击
                return new int[]{1};
            } else {
                return new int[]{-1};
            }
        }
        return new int[]{0, newredbuff};
    }

    /*
        1.轮数
        2.当前年兽等级
        3.当前年兽血量
        4.当前轮数奖池金额
        5.当前年兽逃跑剩余时间
        6.当前游戏是否暂停
     */
    public void addPushSuport() {
        Push.addPushSuport(PushCode.updateDnsInfo, new DefaultPushHandler());
        Push.addPushSuport(PushCode.updateDnsStatus, new DefaultPushHandler());
    }

    @PostConstruct
    public void _construct() {
        initBetINfo();
        logger.info("初始化年兽等级信息");
        List<Ns> allNs = nsService.findAllNs();
        allNs.forEach(e -> nsInfo.put(e.getId().toString(), e.getHp()));
        logger.info("初始化年兽等级信息完成");
        requestManagerUpdateCapital();
        addPushSuport();
        initNsInfo();
    }

    public void initBetINfo() {
        for (int i = 1; i <= 1000; i++) {
            userBetInfo.put(String.valueOf(i), new ConcurrentHashMap<>());
        }
    }

    public void initGame(GameNs nowRound) {
        ROUNDS = nowRound.getRound();
        NOW_ID = nowRound.getNsId().toString();
        Random random = new Random();
        NOW_HP = new BigDecimal( String.valueOf(random.nextInt(290000)+10000));
        LAST_USER_ID = nowRound.getLastUserId();
        User userInfoById = userCacheService.getUserInfoById(LAST_USER_ID);
        if (userInfoById != null) {
            LAST_USER_NAME = userInfoById.getName();
            LAST_HEAD_IMG = userInfoById.getHeadImageUrl();
            LAST_USER_NO = userInfoById.getUserNo();
        }
        ALL_PRIZE = nowRound.getNowPrize();
    }

    public void initBetInfo(GameNs nowRound){
        //初始化本只年兽的投注信息
        JSONObject bet = JSONObject.parseObject(nowRound.getBetInfo());
        Map<String, BigDecimal> map = userBetInfo.get(NOW_ID);
        Set<String> ids = bet.keySet();
        for (String id : ids) {
            map.put(id,bet.getBigDecimal(id));
        }
        //初始化最近炸年兽的人员信息
        JSONArray users = JSONArray.parseArray(nowRound.getLastIds());
        List<String> list = users.toList(String.class);
        for (String s : list) {
            USER_QUEUE.add(s);
        }
    }


    public void initNsInfo() {
        logger.info("加载进行中的游戏信息");
        GameNs nowRound = gameNsService.findNowRound();
        if (nowRound==null){
            ROUNDS = 1;
            NOW_ID = "1";
            NOW_HP = nsInfo.get(NOW_ID);
            ALL_PRIZE = new BigDecimal("1000");
            gameNsService.addGameNs(ROUNDS, Long.parseLong(NOW_ID), NOW_HP, BigDecimal.ZERO,0);
            userBetInfo.put(NOW_ID, new ConcurrentHashMap<>());
        }
        if (nowRound != null && nowRound.getStatus()!=2) {
            //设置游戏为暂停状态
            STATUS = 1;
        } else{
            STATUS = 2;
        }
        initGame(nowRound);
        initBetInfo(nowRound);


    }
    public void updateStatus(int status) {
        if (status == 1) {
            //调整为开始运行
            GameNs gameNs = gameNsService.findByRound(ROUNDS);
            if (gameNs.getStatus()==2){
                //上轮已结束 清空数据 开启新的一轮
                USER_QUEUE.clear();
                gameNs = gameNsService.addGameNs(ROUNDS + 1, Long.valueOf("1"), nsInfo.get("1"), BigDecimal.ZERO,1);
                userBoxInfo.clear();
                initBetINfo();
            }
            initGame(gameNs);
            STATUS = 1;
        } else if (status == 0) {
            //调整为暂停游戏 保存进度
            STATUS = 0;
            Map<String, BigDecimal> map = userBetInfo.get(NOW_ID);
            JSONObject betInfo = JSONObject.from(map);
            JSONArray lastIdsArray = JSONArray.copyOf(USER_QUEUE);
            gameNsService.pauseGameNs(ROUNDS, Long.parseLong(NOW_ID), NOW_HP, ALL_PRIZE, null, LAST_USER_ID,betInfo.toJSONString(),lastIdsArray.toJSONString());
        } else if (status == 2) {
            //游戏结束 开始结算 等待开启下一轮
            STATUS = 2;
            Map<String, BigDecimal> map = userBetInfo.get(NOW_ID);
            JSONObject betInfo = JSONObject.from(map);
            JSONArray lastIdsArray = JSONArray.copyOf(USER_QUEUE);
            gameNsService.gameOver(ROUNDS, Long.parseLong(NOW_ID), NOW_HP, ALL_PRIZE, null, LAST_USER_ID,betInfo.toJSONString(),lastIdsArray.toJSONString());
            if (LAST_USER_ID!=null){
                //bigWinnerSettle();
                //otherSettle();
            }
        }
        JSONObject result = getRoomData();
        result.put("status", status);
        Push.push(PushCode.updateDnsStatus, null, result);
    }

    @Transactional
    public void bigWinnerSettle(){
        BigDecimal bigPrize = ALL_PRIZE;
        userCapitalService.addUserBalanceByNsWin(bigPrize,LAST_USER_ID,UserCapitalTypeEnum.currency_2.getValue());
        Map<String, BigDecimal> map = userBetInfo.get(NOW_ID);
        BigDecimal amount = map.get(LAST_USER_ID.toString());
        Long dataId = nsSettleService.addSettleInfo(LAST_USER_ID, amount, bigPrize, Long.valueOf(NOW_ID), ROUNDS);
        JSONObject object = new JSONObject();
        object.put("dataId",dataId);
        object.put("userId",LAST_USER_ID);
        object.put("profit",bigPrize);
        JSONArray array = new JSONArray();
        array.add(object);
        JSONObject pushData = new JSONObject();
        pushData.put("prizeArray",array);
        Executer.request(TargetSocketType.dns, CommandBuilder.builder().request("200804", pushData).build(), new RequestManagerListener(null));
    }

    @Transactional
    public void otherSettle(){
        BigDecimal otherPrize = ALL_PRIZE.multiply(new BigDecimal("0.95")).multiply(new BigDecimal("0.7"));
        Map<String, BigDecimal> map = userBetInfo.get(NOW_ID);
        BigDecimal allAmount = BigDecimal.ZERO;
        for (BigDecimal value : map.values()) {
            allAmount=allAmount.add(value);
        }
        Set<String> userIds = map.keySet();
        if (userIds != null) {
            JSONArray settles = new JSONArray();
            for (String userId : userIds) {
                JSONObject object = new JSONObject();
                object.put("userId",userId);
                BigDecimal my = map.get(userId);
                object.put("amount",my);
                object.put("profit",my.divide(allAmount,6, BigDecimal.ROUND_DOWN).multiply(otherPrize).setScale(4,BigDecimal.ROUND_DOWN));
                object.put("nsId",NOW_ID);
                object.put("round",ROUNDS);
                settles.add(object);
                if (settles.size()%1000==0){
                    nsSettleService.batchInsertSettle(settles);
                    userCapitalService.batchUpdateByNs(settles);
                    JSONObject pushData = new JSONObject();
                    pushData.put("prizeArray",settles);
                    Executer.request(TargetSocketType.dns, CommandBuilder.builder().request("200804", pushData).build(), new RequestManagerListener(null));
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            if (!settles.isEmpty()) {
                nsSettleService.batchInsertSettle(settles);
                userCapitalService.batchUpdateByNs(settles);
                JSONObject pushData = new JSONObject();
                pushData.put("prizeArray",settles);
                Executer.request(TargetSocketType.dns, CommandBuilder.builder().request("200804", pushData).build(), new RequestManagerListener(null));
            }
        }

    }



    public void requestManagerUpdateCapital() {
        new Timer("定时推送manager修改内存数据1").schedule(new TimerTask() {
            public void run() {
                try {
                    long time = System.currentTimeMillis();
                    String oldKey = key;
                    String newKey = DateUtil.getCurrent5();
                    userCapitals.put(newKey, new CopyOnWriteArrayList<>());
                    userAttackCapitals.put(newKey, new CopyOnWriteArrayList<>());
                    key = newKey;
                    Thread.sleep(100);
                    List data = userCapitals.remove(oldKey);
                    if (data != null && data.size() > 0) {
                        JSONObject object = new JSONObject();
                        object.put("betArray", data);
                        Executer.request(TargetSocketType.dns, CommandBuilder.builder().request("200802", object).build(), new RequestManagerListener(null));
                    }
                    Thread.sleep(20);
                    List data2 = userAttackCapitals.remove(oldKey);
                    if (data2 != null && data2.size() > 0) {
                        JSONObject object = new JSONObject();
                        object.put("prizeArray", data2);
                        Executer.request(TargetSocketType.dns, CommandBuilder.builder().request("200805", object).build(), new RequestManagerListener(null));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }, 0, 1);


        new Timer("定时推送manager修改内存数据2").schedule(new TimerTask() {
            public void run() {
                try {
                    long time = System.currentTimeMillis();
                    String oldKey = key2;
                    String newKey = DateUtil.getCurrent5();
                    userBoxCapitals.put(newKey, new CopyOnWriteArrayList<>());
                    userAttackCapitals.put(newKey, new CopyOnWriteArrayList<>());
                    key2 = newKey;
                    Thread.sleep(100);
                    List data = userBoxCapitals.remove(oldKey);
                    if (data != null && data.size() > 0) {
                        JSONObject object = new JSONObject();
                        object.put("prizeArray", data);
                        Executer.request(TargetSocketType.dns, CommandBuilder.builder().request("200803", object).build(), new RequestManagerListener(null));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0, 1);





        new Timer("每1分钟秒修改下注投入信息").schedule(new TimerTask() {
            public void run() {
                try {
                    if (STATUS == 1) {
                        Map<String, BigDecimal> map = userBetInfo.get(NOW_ID);
                        JSONObject betInfo = JSONObject.from(map);
                        JSONArray lastIdsArray = JSONArray.copyOf(USER_QUEUE);
                        gameNsService.updateBetInfo(betInfo.toJSONString(), lastIdsArray.toJSONString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }, 0, 1000*60);


        new Timer("每100ms推送数据").schedule(new TimerTask() {
            public void run() {
                try {
                    if (needPush==1){
                        synchronized (lock){
                            JSONObject pushData = getRoomData();
                            Push.push(PushCode.updateDnsInfo, null, pushData);
                            needPush=0;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }, 0, 100);
    }


    @Transactional
    @ServiceMethod(code = "101", description = "用户加入打年兽游戏")
    public Object jionRoom(NSSocketServer adminSocketServer, Command lotteryCommand, JSONObject data) {
        checkNull(data);
        checkNull(data.get("userId"), data.get("userNo"));
        Long userId = data.getLong("userId");
        if (!userBoxInfo.containsKey(userId.toString())) {
            Map<String, UserNsPrize> prizes = userNsPrizeService.findByUserIdAndRound(userId, ROUNDS);
            userBoxInfo.put(userId.toString(), prizes);
        }

        JSONObject result = new JSONObject();
        //result.put("rounds", ROUNDS);
        //result.put("nsId", NOW_ID);
        //result.put("allHp", nsInfo.get(NOW_ID));
        //result.put("nowHp", NOW_HP);
        result.put("allPrize", ALL_PRIZE == null ? BigDecimal.ZERO : ALL_PRIZE);
        result.put("lastUserNo", LAST_USER_NO==null?"":LAST_USER_NO);
        result.put("lastUserName", LAST_USER_NAME==null?"":LAST_USER_NAME);
        result.put("lastHeadImg", LAST_HEAD_IMG==null?"":LAST_HEAD_IMG);
        result.put("prizes", userBoxInfo.get(userId.toString()).values());
        result.put("status", STATUS);
        return result;
    }

    @Transactional
    @ServiceMethod(code = "104", description = "用户离开打年兽游戏")
    public Object leaveRoom(NSSocketServer adminSocketServer, Command lotteryCommand, JSONObject data) {
        checkNull(data);
        checkNull(data.get("userId"));
        Long userId = data.getLong("userId");
        return new JSONObject();
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
        Map<String, BigDecimal> betInfo = userBetInfo.get(NOW_ID);
        betInfo.put(userId, betInfo.getOrDefault(userId, BigDecimal.ZERO).add(amount));
        return myOrder;
    }

    public BigDecimal addPrizeAmount(BigDecimal amount,String userId,String orderNo,Long dataId){
        Random random = new Random();
        double i = random.nextInt(91)+50;
        i = i/100;
        System.out.println(i);
        BigDecimal rate = new BigDecimal(String.valueOf(i));
        BigDecimal get =  amount.multiply(rate);
        Map<String, String> myOrder = new HashMap<>();
        userCapitalService.addUserBalanceByAttackNs(get, Long.valueOf(userId),2);
        myOrder.put("orderNo", orderNo);
        myOrder.put("dataId", String.valueOf(dataId));
        myOrder.put("amount", String.valueOf(get));
        myOrder.put("userId", userId);
        List<Map<String, String>> maps = userAttackCapitals.get(key);
        maps.add(myOrder);
        return  get;
    }


    public JSONObject getRoomData() {
        JSONObject result = new JSONObject();
        result.put("rounds", ROUNDS);
        result.put("nsId", NOW_ID);
        //result.put("allHp", nsInfo.get(NOW_ID));
        //result.put("nowHp", NOW_HP);
        result.put("allPrize", ALL_PRIZE);
        result.put("lastUserName", LAST_USER_NAME==null?"":LAST_USER_NAME);
        result.put("lastUserNo", LAST_USER_NO==null?"":LAST_USER_NO);
        result.put("lastHeadImg", LAST_HEAD_IMG==null?"":LAST_HEAD_IMG);
        result.put("amount",AMOUNT);
        result.put("isDie",isDie);
        return result;
    }

    public int getRate(BigDecimal amount) {
        int a = 1;
        if (amount.compareTo(BigDecimal.ZERO) == 0) {
            a = 10;
        }
        if (amount.compareTo(BigDecimal.TEN) == 0) {
            a = 100;
        }
        return a;
    }

    @Transactional
    @ServiceMethod(code = "002", description = "攻击年兽")
    public Object attackNs(NSSocketServer adminSocketServer, Command lotteryCommand, JSONObject data) {
        checkNull(data);
        checkNull(data.get("userId"), data.get("amount"));
        final String thisId = NOW_ID;
        String userId = data.getString("userId");
        BigDecimal amount = data.getBigDecimal("amount");
        String orderNo = OrderUtil.getOrder5Number();
        synchronized (lock) {
            if (STATUS != 1) {
                throwExp("游戏暂未开始，当前不能进行攻击！");
            }
            UserAttackNsRecord record = userAttackNsRecordService.addRecord(Long.valueOf(userId), Long.parseLong(NOW_ID), ROUNDS, amount, orderNo);
            updateCapital(userId, amount, orderNo, record.getId());
            if (!userBoxInfo.containsKey(userId)) {
                Map<String, UserNsPrize> prizes = userNsPrizeService.findByUserIdAndRound(Long.valueOf(userId), ROUNDS);
                userBoxInfo.put(userId, prizes);
            }
            User userInfo = userCacheService.getUserInfoById(userId);
            LAST_USER_NO = userInfo.getUserNo();
            LAST_HEAD_IMG = userInfo.getHeadImageUrl();
            LAST_USER_NAME = userInfo.getName();
            LAST_USER_ID = userInfo.getId();
            USER_QUEUE.remove(userId);
            USER_QUEUE.add(userId);
            if (USER_QUEUE.size() > 20) {
                USER_QUEUE.remove(0);
            }
            Map<String, UserNsPrize> prizeMap = userBoxInfo.get(userId);
            ALL_PRIZE = ALL_PRIZE.add(amount.multiply(new BigDecimal("0.02")));
            if (NOW_HP.subtract(amount.multiply(new BigDecimal("10"))).compareTo(BigDecimal.ZERO) > 0) {
                NOW_HP = NOW_HP.subtract(amount.multiply(new BigDecimal("10")));
                isDie=0;
            } else {
                //年兽死亡 结算全部宝箱 本轮结束 最后发奖取消
                isDie=1;
                Random random = new Random();
                int id = random.nextInt(3) + 1;
                NOW_ID = String.valueOf(id );
                NOW_HP = new BigDecimal( String.valueOf(random.nextInt(290000)+10000));
                USER_QUEUE.clear();
                bigWinnerSettle();
                killNsRecordService.addRecord(ALL_PRIZE, Long.valueOf(userId));
                ALL_PRIZE=BigDecimal.ZERO;
            }
            //玩家攻击奖励
            BigDecimal getAmount = addPrizeAmount(amount, userId, orderNo, record.getId());
            AMOUNT=amount;
            JSONObject result = new JSONObject();
            //result.put("prize", userNsPrize);
            result.putAll(getRoomData());
            result.put("getAmount",getAmount);
            if (isDie==0){
                needPush=1;
            }else{
                Push.push(PushCode.updateDnsInfo, null, getRoomData());
            }
            return result;
        }
    }


    @Transactional
    @ServiceMethod(code = "009", description = "打开宝箱")
    public Object openBox(NSSocketServer adminSocketServer, Command lotteryCommand, JSONObject data) {
        checkNull(data);
        checkNull(data.get("userId"), data.get("nsId"));
        String userId = data.getString("userId");
        String id = data.getString("nsId");
        synchronized (LockUtil.getlock(userId + "box")) {
            if (!userBoxInfo.containsKey(userId)) {
                Map<String, UserNsPrize> prizes = userNsPrizeService.findByUserIdAndRound(Long.valueOf(userId), ROUNDS);
                if (prizes.size() <= 0) {
                    throwExp("您没有可以打开的宝箱");
                }
                userBoxInfo.put(userId, prizes);
            }
            Map<String, UserNsPrize> userNsPrizeMap = userBoxInfo.get(userId);
            UserNsPrize userNsPrize = userNsPrizeMap.get(id);
            if (!userNsPrize.getUserId().toString().equals(userId)) {
                throwExp("宝箱信息有误");
            }
            if (userNsPrize.getRound() != ROUNDS) {
                throwExp("您只能打开本轮游戏的宝箱");
            }
            if (userNsPrize.getNsId() + 6 > Long.parseLong(NOW_ID)) {
                throwExp("宝箱还没有解锁哟~");
            }
            if (userNsPrize.getStatus() != 0) {
                throwExp("宝箱已经领取过啦~");
            }
            userNsPrizeService.updateStatus(Long.parseLong(id), 1, ROUNDS, Long.parseLong(userId));
            BigDecimal amount = userNsPrize.getAmount();
            String orderNo = OrderUtil.getOrder5Number();
            Long dataId = openBoxRecordService.addRecord(Long.valueOf(userId), amount, Long.parseLong(id), orderNo);
            userCapitalService.addUserBalanceByOpenBox(amount, Long.parseLong(userId), UserCapitalTypeEnum.currency_2.getValue());
            Map<String, String> myOrder = new HashMap<>();
            myOrder.put("orderNo", orderNo);
            myOrder.put("dataId", String.valueOf(dataId));
            myOrder.put("amount", String.valueOf(amount));
            myOrder.put("userId", userId);
            List<Map<String, String>> maps = userBoxCapitals.get(key2);
            maps.add(myOrder);
            JSONObject result = new JSONObject();
            result.put("amount", userNsPrize.getAmount());
            result.put("nsId", id);
            userNsPrize.setStatus(1);
            return result;
        }
    }

    @Transactional
    @ServiceMethod(code = "010", description = "总奖池详情")
    public Object allPrizeInfo(NSSocketServer adminSocketServer, Command lotteryCommand, JSONObject data) {
        checkNull(data);
        checkNull(data.get("userId"));
        String userId =data.getString("userId");
        Map<String, BigDecimal> map = userBetInfo.get(NOW_ID);
        JSONObject result = new JSONObject();
        List<JSONObject> users = new ArrayList<>();
        for (String id : USER_QUEUE) {
            User user = userCacheService.getUserInfoById(id);
            JSONObject userInfo = new JSONObject();
            userInfo.put("userNo",user.getUserNo());
            userInfo.put("headImg",user.getHeadImageUrl());
            userInfo.put("name",user.getName());
            userInfo.put("amount",map.get(id));
            users.add(userInfo);
        }
        Collections.reverse(users);
        result.put("userList", users);
        result.put("allCount",map.size());
        BigDecimal allAmount = BigDecimal.ZERO;
        for (BigDecimal value : map.values()) {
            allAmount=allAmount.add(value);
        }
        result.put("allAmount",allAmount);
        result.put("myAmount",map.get(userId));
        return result;
    }

    public void unlockBox(String nsId){
        List<UserNsPrize> byNsId = userNsPrizeService.findByNsId(Long.parseLong(nsId), 1);
        for (UserNsPrize userNsPrize : byNsId) {
            userNsPrizeService.updateStatus(Long.parseLong(nsId), 1, ROUNDS, userNsPrize.getUserId());
            BigDecimal amount = userNsPrize.getAmount().divide(new BigDecimal("1.2"),4,BigDecimal.ROUND_DOWN);
            String orderNo = OrderUtil.getOrder5Number();
          //  Long dataId = openBoxRecordService.addRecord(Long.valueOf(userId), amount, Long.parseLong(id), orderNo);
            userCapitalService.addUserBalanceByOpenBox(amount, userNsPrize.getUserId(), UserCapitalTypeEnum.currency_2.getValue());
            Map<String, String> myOrder = new HashMap<>();
            myOrder.put("orderNo", orderNo);
            myOrder.put("dataId", "");
            myOrder.put("amount", String.valueOf(amount));
            myOrder.put("userId", userNsPrize.getUserId().toString());
            List<Map<String, String>> maps = userBoxCapitals.get(key2);
            maps.add(myOrder);
            JSONObject result = new JSONObject();
            result.put("amount", userNsPrize.getAmount());
            result.put("nsId", nsId);
            if (userBoxInfo.containsKey(userNsPrize.getUserId().toString())){
                Map<String, UserNsPrize> prizeMap = userBoxInfo.get(userNsPrize.getUserId().toString());
                if (prizeMap.containsKey(nsId)){
                    prizeMap.get(nsId).setStatus(1);
                }
            }
        }

    }

}

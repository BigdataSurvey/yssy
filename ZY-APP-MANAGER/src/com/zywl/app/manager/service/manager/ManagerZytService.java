package com.zywl.app.manager.service.manager;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.*;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.LockUtil;
import com.zywl.app.base.util.OrderUtil;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.enmus.LogCapitalTypeEnum;
import com.zywl.app.defaultx.enmus.UserCapitalTypeEnum;
import com.zywl.app.defaultx.enmus.ZytHighIncomeEnum;
import com.zywl.app.defaultx.service.*;
import com.zywl.app.manager.context.MessageCodeContext;
import com.zywl.app.manager.service.PlayGameService;
import com.zywl.app.manager.socket.ManagerSocketServer;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@ServiceClass(code = MessageCodeContext.ZYT)
public class ManagerZytService extends BaseService {

    @Autowired
    private UserProcessService userProcessService;


    @Autowired
    private UserIncomeService userIncomeService;

    @Autowired
    private DicZytService dicZytService;

    @Autowired
    private UserCacheService userCacheService;
    @Autowired
    private ZytRecordService zytRecordService;

    @Autowired
    private UserCapitalService userCapitalService;
    @Autowired
    private ManagerGameBaseService managerGameBaseService;

    @Autowired
    private PlayGameService gameService;

    private static List<Map<String, JSONArray>> ytInfo = new ArrayList<>();
    private static String LBD = "59";
    private static List<DicZyt> zytList = new ArrayList<>();
    private static Map<String, Map<String, Integer>> userCurrProcessMap = new HashMap<>();


    @PostConstruct
    public void _ServerMineService() {
        init();
    }

    public void init() {
        ytInfo = dicZytService.findDicZytList();
        zytList = dicZytService.findZyt();
        userCurrProcessMap = find();
    }

    public Map<String, Map<String, Integer>> find() {
        List<UserProcess> userProcesses = userProcessService.findUserCurrProcess();
        Map<String, Integer> stringIntegerHashMap;
        Map<String, Map<String, Integer>> userMap = new HashMap<>();
        for (UserProcess userProcess : userProcesses) {
            stringIntegerHashMap = new HashMap<>();
            stringIntegerHashMap.put("currProcess", userProcess.getCurrProcessNumber());
            stringIntegerHashMap.put("highNum", userProcess.getHighNum());
            userMap.put(userProcess.getUserId().toString(), stringIntegerHashMap);
        }
        return userMap;
    }

    @Transactional
    @ServiceMethod(code = "001", description = "激活")
    public Object open(ManagerSocketServer adminSocketServer, JSONObject params) {
        checkNull(params);
        Long userId = params.getLong("userId");
        synchronized (LockUtil.getlock(userId)) {
            JSONObject jsonObject = new JSONObject();
            //初始化上次领取时间 上次查看时间 未领取
            jsonObject.put("userId", userId);
            //扣取通宝 开通金矿洞需要“1000通宝”，开通银矿洞需要“500通宝”
            List<UserProcess> userProcesses = userProcessService.findByUserId(userId);
            if (userProcesses.size() > 0) {
                if (1 == userProcesses.get(0).getActivaStatus()) {
                    throwExp("已经开通过九层妖塔");
                }
            }
            managerGameBaseService.checkBalance(userId, BigDecimal.valueOf(500), UserCapitalTypeEnum.yyb);
            userCapitalService.subUserBalanceByOpenPit(userId, BigDecimal.valueOf(500));
            managerGameBaseService.pushCapitalUpdate(userId, UserCapitalTypeEnum.yyb.getValue());
            Integer i = userProcessService.addUserProecss(params);
            return i;
        }
    }

    @Transactional
    @ServiceMethod(code = "002", description = "抽奖")
    public Object draw(ManagerSocketServer adminSocketServer, JSONObject params) {
        checkNull(params);
        JSONObject jsonObject = new JSONObject();
        Long userId = params.getLong("userId");
        String type = params.getString("type");
        Integer rondom = params.getInteger("random");
        Random random = new Random();
        HashMap<String, Integer> putMap = new HashMap<>();
        int szRandomNumber = 0;
        int pkpRandomNumber = 0;
        synchronized (LockUtil.getlock(userId)) {
            List<UserProcess> userProcesses = userProcessService.findByUserId(userId);
            if (userProcesses.size() > 0) {
                if (1 != userProcesses.get(0).getActivaStatus()) {
                    throwExp("当前用户未开通九层妖塔");
                }
            }
            Map<String, Integer> userCurrProcessAndHighMap = userCurrProcessMap.get(userId.toString());
            Integer currProcess = 0;
            if (null != userCurrProcessAndHighMap) {
                currProcess = userCurrProcessAndHighMap.get("currProcess");
            } else {
                userCurrProcessAndHighMap = putMap;

            }
            //用户的当前进度

            //type1为摇骰子,2为扑克牌
            if ("1".equals(type)) {
                // 生成1-6的随机数
                szRandomNumber = random.nextInt(6) + 1;
                rondom = szRandomNumber;
            } else {
                pkpRandomNumber = random.nextInt(3) + 1;
                userCurrProcessAndHighMap.put("currProcess", currProcess);
                Integer newProcess = 0;
                //1为上 2为下 3为平
                //扑克牌摇到1，加上骰子点数
                if (1 == pkpRandomNumber) {
                    newProcess = currProcess + rondom;
                    //60为塔的顶端,60进来的时候将不再加值
                    if (currProcess == 60) {
                        newProcess = 60;
                    }
                    //扑克牌摇到3，减去骰子点数(如果当前进度条已经为0，那么将不再往下减 依旧为0)
                } else if (3 == pkpRandomNumber) {
                    newProcess = currProcess - rondom;
                    //扑克牌摇到2，将赋值原来的值 （此处有一个逻辑，刚激活的时候为-1，如果第一次战斗抽到平，也属于开始战斗，赋值为0，可领取第一层收益）
                } else {
                    newProcess = currProcess;
                }
                if (newProcess <= 0) {
                    newProcess = 0;
                }
                userCurrProcessAndHighMap.put("currProcess", newProcess);
                //动态修改当前进度所在的层数
                JSONArray array = countBound(newProcess);
                for (DicZyt dicZyt : zytList) {
                    if (dicZyt.getProgress().toString().equals(array.toString())) {
                        userCurrProcessAndHighMap.put("highNum", dicZyt.getHighNum());
                    }
                }
                //更新当前用户的最新进度
                userCurrProcessMap.put(userId.toString(), userCurrProcessAndHighMap);
                userProcessService.updateUserProcess(userId, userCurrProcessAndHighMap);
            }
        }
        jsonObject.put("szRandomNumber", rondom);
        jsonObject.put("pkpRandomNumber", pkpRandomNumber);
        jsonObject.put("userNewProcess", userCurrProcessMap);
        return jsonObject;
    }


    public JSONArray countBound(int number) {
        int lowerBound = (number / 10) * 10;
        int upperBound = lowerBound + 9;
        JSONArray array = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("start", lowerBound);
        jsonObject.put("end", upperBound);
        array.add(jsonObject);
        return array;
    }

    @Transactional
    @ServiceMethod(code = "003", description = "进入页面")
    public Object see(ManagerSocketServer adminSocketServer, JSONObject params) {
        checkNull(params);
        Long userId = params.getLong("userId");
        JSONObject jsonObject = new JSONObject();
        HashMap<String, BigDecimal> stringBigDecimalHashMap = new HashMap<>();
        Map<String, Integer> stringIntegerMap = userCurrProcessMap.get(userId.toString());
        List<UserProcess> userProcesses = userProcessService.findByUserId(userId);
        if (userProcesses.size() == 0) {
            jsonObject.put("activaStatus", 0);
            return jsonObject;
        }
        synchronized (LockUtil.getlock(userId)) {
            // 查询如果一天没玩游戏的话 会倒退
            for (UserProcess userProcess : userProcesses) {
                if (((System.currentTimeMillis() - userProcess.getUpdtTime().getTime()) / 1000 / 60 / 60 / 24) > 3) {
                    stringIntegerMap.put("currProcess", userProcess.getCurrProcessNumber() - 10);
                    stringIntegerMap.put("highNum", userProcess.getHighNum() - 1);
                    userProcessService.updateUserProcess(userId, stringIntegerMap);
                }
            }

            //查看当前用户当前的进度条和塔层
            List<UserIncome> byUserId = userIncomeService.findUserIncomeByUserIdAndStatus(userId);
            if (byUserId.size() > 0) {
                UserIncome userIncome = byUserId.get(0);
                stringBigDecimalHashMap.put(userId.toString(), userIncome.getUnreceiveIncome());
                jsonObject.put("userIncome", userIncome);
            }
        }
        jsonObject.put("userHighAndProcess", userCurrProcessMap);
        jsonObject.put("userIncome1", stringBigDecimalHashMap);
        jsonObject.put("activaStatus", userProcesses.get(0).getActivaStatus());
        return jsonObject;
    }


    @Transactional
    @ServiceMethod(code = "004", description = "领取收益")
    public Object receiveIncome(ManagerSocketServer adminSocketServer, JSONObject params) {
        checkNull(params);
        Long userId = params.getLong("userId");
        synchronized (LockUtil.getlock(userId)) {
            //先获取用户当前未领取收益
            List<UserIncome> byUserId = userIncomeService.findByUserId(userId);
            List<UserIncome> userIncomes = new ArrayList<>();
            List<ZytRecord> zytRecordList = new ArrayList<>();
            for (UserIncome userIncome : byUserId) {
                ZytRecord zytRecord = new ZytRecord();
                String orderNo = OrderUtil.getOrder5Number();
                userIncome.setReceiveIncome(userIncome.getUnreceiveIncome());
                userIncome.setUnreceiveIncome(BigDecimal.ZERO);
                userIncome.setCrteTime(new Date());
                userIncome.setUpdtTime(new Date());
                userIncome.setStatus(0);
                userIncomes.add(userIncome);
                zytRecord.setOrderNo(orderNo);
                zytRecord.setCrteTime(new Date());
                zytRecord.setAmount(userIncome.getReceiveIncome());
                zytRecord.setReceiveTime(new Date());
                zytRecord.setUserId(userId);
                zytRecordList.add(zytRecord);
                userCapitalService.addUserBalanceByAddReward(zytRecord.getAmount(), userId, UserCapitalTypeEnum.yyb.getValue(), LogCapitalTypeEnum.zyt_receive);
                managerGameBaseService.pushCapitalUpdate(userId, UserCapitalTypeEnum.yyb.getValue());
            }
            int i = userIncomeService.batchUpdate(userIncomes);
            zytRecordService.batchInsert(zytRecordList);
            return i;
        }
    }

    @Transactional
    @ServiceMethod(code = "005", description = "爆炸")
    public Object boom(ManagerSocketServer adminSocketServer, JSONObject params) {
        checkNull(params);
        Long userId = params.getLong("userId");
        Random random = new Random();
        Map<String, Integer> userCurrProcessAndHighMap = userCurrProcessMap.get(userId.toString());
        synchronized (LockUtil.getlock(userId)) {
            //爆炸层数
            int boomHignNum = random.nextInt(6) + 1;
            //查询所有参与的用户
            List<UserProcess> find = userProcessService.findUserCurrProcess().stream().filter(b -> b.getHighNum() != null && b.getHighNum() > 1).collect(Collectors.toList());
            if (find == null || find.isEmpty()) {
                return new ArrayList<>();
            }
            // 计算需要抽取的数量（至少5条）
            int totalSize = find.size();
            int sampleSize = calculateSampleSize(totalSize);
            // 转换为列表便于随机访问
            List<UserProcess> list = new ArrayList<>(find);

            // 如果样本数量大于等于集合大小，返回整个集合
            if (sampleSize >= totalSize) {
                return new ArrayList<>(list);
            }
            // 随机抽取
            //暴雷的5%的用户，需要掉血，并掉层数，判断有没有防护罩
            List<UserProcess> userProcesses = randomSelect(list, sampleSize);
            for (UserProcess userProcess : userProcesses) {
                double LBDNumber = gameService.getUserItemNumber(userId, LBD);
                if (LBDNumber < 1) {
                    throwExp("数量不足");
                }
                userCurrProcessAndHighMap.put("currProcess", userProcess.getCurrProcessNumber() - 5);
                //动态修改当前进度所在的层数
                JSONArray array = countBound(userProcess.getCurrProcessNumber() - 5);
                for (DicZyt dicZyt : zytList) {
                    if (dicZyt.getProgress().equals(array)) {
                        userCurrProcessAndHighMap.put("highum", dicZyt.getHighNum());
                    }
                }
                //更新当前用户的最新进度
                userCurrProcessMap.put(userId.toString(), userCurrProcessAndHighMap);
                userProcessService.updateUserProcess(userId, userCurrProcessAndHighMap);
            }
            return null;
        }
    }

    @Transactional
    @ServiceMethod(code = "007", description = "掉血或保护")
    public Object subBlood(ManagerSocketServer adminSocketServer, JSONObject params) {
        checkNull(params);
        Long userId = params.getLong("userId");
        Map<String, Integer> userCurrProcessAndHighMap = userCurrProcessMap.get(userId.toString());
        synchronized (LockUtil.getlock(userId)) {

        }
        return null;
    }


    @Transactional
    @ServiceMethod(code = "006", description = "查询镇妖塔订单")
    public Object findRecord(ManagerSocketServer adminSocketServer, JSONObject params) {
        checkNull(params);
        Long userId = params.getLong("userId");
        List<ZytRecord> zytRecords = zytRecordService.findzytRecordByUserId(userId);
        return zytRecords;
    }

    private static int calculateSampleSize(int totalSize) {
        // 计算5%
        double fivePercent = totalSize * 0.05;

        // 至少抽取5条，向上取整
        int sampleSize = (int) Math.ceil(fivePercent);
        return Math.max(sampleSize, 5); // 保证至少5条
    }

    /**
     * 从列表中随机选择指定数量的元素
     */
    private static <T> List<T> randomSelect(List<T> list, int sampleSize) {
        List<T> copy = new ArrayList<>(list);
        Collections.shuffle(copy); // 随机打乱顺序
        return copy.subList(0, sampleSize); // 取前sampleSize个
    }

    public Object performIncome() {
        //1.先获取所有的用户的塔数（点击开始战斗即有收益）
        //获取所有用户的塔数和进度
        JSONObject jsonObject = new JSONObject();
        List<UserProcess> userCurrProcess = userProcessService.findUserCurrProcess();
        List<UserIncome> userIncomes = new ArrayList<>();
        for (UserProcess currProcess : userCurrProcess) {
            //查看当前用户当前的进度条和塔层
            UserIncome userIncome = new UserIncome();
            userIncome.setUserId(currProcess.getUserId());
            //修改历史数据可领取状态为0
            List<UserIncome> byUserId = userIncomeService.findByUserId(Long.valueOf(currProcess.getUserId()));
            for (UserIncome income : byUserId) {
                income.setStatus(0);
                userIncomes.add(income);
            }
            //获取当前用户的层数和进度值，并获得当前层数的收益，同步到收益表中的未领取字段中
            Map<String, Integer> userHighMap = userCurrProcessMap.get(currProcess.getUserId().toString());
            Integer highNum = userHighMap.get("highNum");
            BigDecimal todayIncome = ZytHighIncomeEnum.getValue(highNum);
            userIncome.setUnreceiveIncome(todayIncome);
            userIncome.setCrteTime(new Date());
            userIncome.setStatus(1);
            userIncome.setUpdtTime(new Date());
            userIncomeService.insert(userIncome);
        }
        //批量修改用户收益表
        userIncomeService.batchUpdate(userIncomes);
        return jsonObject;
    }


    // 每天午夜执行,计算当前当前用户的收益
    //直接入库到数据库中
    //@Scheduled(cron = "0 0 0 * * ?")
    @Scheduled(cron = "0 */3 * * * *")
    @Transactional
    public void calculateAtMidnight() {
        System.out.println("Spring Scheduled - 开始计算数值，当前时间: " + LocalTime.now());
        performIncome();
    }


}

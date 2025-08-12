package com.zywl.app.manager.service.manager;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.google.common.collect.Lists;
import com.live.app.ws.enums.PushCode;
import com.live.app.ws.util.Push;
import com.zywl.app.base.bean.Activity;
import com.zywl.app.base.bean.Config;
import com.zywl.app.base.bean.PitUserParent;
import com.zywl.app.base.bean.User;
import com.zywl.app.base.bean.hongbao.RecordSheet;
import com.zywl.app.base.bean.hongbao.RedEnvelope;
import com.zywl.app.base.bean.hongbao.RedEnvelopeVo;
import com.zywl.app.base.bean.hongbao.RedPosition;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.BeanUtils;
import com.zywl.app.base.util.LockUtil;
import com.zywl.app.base.util.OrderUtil;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.cache.GameCacheService;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.enmus.ItemIdEnum;
import com.zywl.app.defaultx.enmus.UserCapitalTypeEnum;
import com.zywl.app.defaultx.service.*;
import com.zywl.app.manager.context.MessageCodeContext;
import com.zywl.app.manager.service.AdminMailService;
import com.zywl.app.manager.socket.ManagerSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 * 红包雨
 */
@Service
@ServiceClass(code = MessageCodeContext.RECORD)
public class ManagerRedEnvelopeService extends BaseService {


    @Autowired
    private RedEnvelopeService redEnvelopeService;

    @Autowired
    private AdminMailService adminMailService;

    @Autowired
    private MailService mailService;
    @Autowired
    private UserCapitalService userCapitalService;

    @Autowired
    private UserCacheService userCacheService;

    @Autowired
    private RecordSheetService recordSheetService;

    @Autowired
    private ManagerGameBaseService managerGameBaseService;

    @Autowired
    private ManagerConfigService managerConfigService;

    @Autowired
    private RedPositionService redPositionService;

    @Autowired
    private GameCacheService gameCacheService;


    private final Map<String, RedEnvelope> redEnvelopeMap = new ConcurrentHashMap<>();//可以被抢的红包


    @PostConstruct
    public void _ManagerRecordService() {
        initRed();
        pushRed();
    }

    public void initRed() {
        //查询哪些可以被抢 插入到map
        List<RedEnvelope> queryResult = redEnvelopeService.findAllRedEnvelope();
        //遍历红包列表，将可抢的红包放入map
        queryResult.forEach(e -> redEnvelopeMap.put(e.getId().toString(), e));


    }

    public void pushRed() {
        new Timer("推送红包雨").schedule(new TimerTask() {
            public void run() {
                try {
                    //推送map中的包
                    if (!redEnvelopeMap.isEmpty()) {
                        Collection<RedEnvelope> values = redEnvelopeMap.values();
                        List<RedEnvelope> list = new ArrayList<>(values);
                        Collections.shuffle(list);
                        RedEnvelopeVo newRedEnvelope = new RedEnvelopeVo();
                        BeanUtils.copy(list.get(0), newRedEnvelope);
                        Push.push(PushCode.pushRed, null, newRedEnvelope);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 1000, 500);
    }


    private void initRedEnvelopeAmounts(BigDecimal amount, Long userId) {
        RedEnvelope redEnvelope = new RedEnvelope();
        int totalNumber = managerConfigService.getInteger(Config.RED_NUMBER);
        // 如果是炸弹红包，随机生成炸弹位置(1-最大值之间)
        Random random = new Random();
        int bombPos = random.nextInt(totalNumber);//生成0-最大值-1的随机数
        redEnvelope.setBombIndex(bombPos);
        redEnvelope.setNowIndex(0);
        redEnvelope.setSurplusAmount(amount);
        redEnvelope.setReleasedQuantity(10);
        redEnvelope.setUserId(userId);
        redEnvelope.setCreateTime(new Date());
        redEnvelope.setUpdateTime(new Date());
        redEnvelope.setStatus(1);
        redEnvelope.setTotalNumber(totalNumber);
        redEnvelope.setRemark("发红包");
        redEnvelope.setRedAward(amount.multiply(BigDecimal.valueOf(1.0 / 20)));
        // 生成普通红包
        redEnvelope.setTotalAmount(amount);
        int zdCount = managerConfigService.getInteger(Config.RED_SEND_COUNT);
        double rate = 1 - (1.0 / zdCount);
        BigDecimal redRate = managerConfigService.getBigDecimal(Config.RED_RATE);
        List<BigDecimal> normalAmounts = divideRedPacket(Double.parseDouble((redEnvelope.getTotalAmount().multiply(redRate)).toString()) * 1000 * rate, totalNumber);
        JSONArray array = new JSONArray(normalAmounts);
        redEnvelope.setAmount(array);
        redEnvelope.setStatus(1);
        Long id = redEnvelopeService.saveRedEnvelope(redEnvelope);
        redEnvelopeMap.put(String.valueOf(id), redEnvelope);
    }

    public static void main(String[] args) {
        System.out.println(1 - 1.0 / 20);
    }

    /**
     * 二倍均值法的算法实现 - 算法里面的金额以 货币的1000倍后 为单位
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
                //左闭右开 [1,剩余金额/剩余人数 的除数 的两倍  )
                double v = restAmount / restPeople * 2.0 - 1.0;
                amount = 0 + (v - 0) * random.nextDouble() + 1;
                realAmount = new BigDecimal(amount).divide(new BigDecimal(1000)).setScale(2, RoundingMode.HALF_UP);
                list.add(realAmount);
                //剩余金额
                restAmount -= amount;
                restPeople--;
            }
            //最后的剩余金额
            list.add(new BigDecimal(restAmount).divide(new BigDecimal(1000)).setScale(2, RoundingMode.HALF_UP));
        }
        return list;
    }

    //发包检查
    public void checkSendRed(Long userId, BigDecimal amount, int number) {
        RedPosition redPosition = redPositionService.findByUserId(userId);
        //判断次数够不够发  不够抛异常 够了减次数
        if (amount.compareTo(BigDecimal.TEN) == 0) {
            assert redPosition != null;
            if (redPosition.getCount1() < number) {
                throwExp("次数不足");
            } else {
                //扣次数
                redPosition.setCount1(redPosition.getCount1() - number);
            }
        }
        if (amount.compareTo(new BigDecimal("20")) == 0) {
            if (redPosition.getCount2() < number) {
                throwExp("次数不足");
            } else {
                //扣次数
                redPosition.setCount2(redPosition.getCount2() - number);
            }
        }

        if (amount.compareTo(new BigDecimal("50")) == 0) {
            if (redPosition.getCount3() < number) {
                throwExp("次数不足");
            } else {
                //扣次数
                redPosition.setCount3(redPosition.getCount3() - number);
            }
        }

        if (amount.compareTo(new BigDecimal("100")) == 0) {
            if (redPosition.getCount4() < number) {
                throwExp("次数不足");
            } else {
                //扣次数
                redPosition.setCount4(redPosition.getCount4() - number);
            }
        }
        //更改数据库对象
        redPositionService.updatePosition(redPosition);
    }

    @Transactional
    @ServiceMethod(code = "001", description = "发包")
    public Object sendRed(ManagerSocketServer adminSocketServer, JSONObject data) {
        checkNull(data.get("userId"), data.get("totalNumber"), data.get("amount"));
        Long userId = data.getLong("userId");
        synchronized (LockUtil.getlock(userId)) {
            int totalNumber = data.getIntValue("totalNumber");
            BigDecimal amount = BigDecimal.valueOf(data.getDoubleValue("amount"));
            synchronized (LockUtil.getlock(userId)) {
                //生成实体 插入db  插入map
                checkSendRed(userId, amount, totalNumber);
                managerGameBaseService.checkBalance(userId, amount.multiply(BigDecimal.valueOf(totalNumber)), UserCapitalTypeEnum.currency_2);
                userCapitalService.subUserBalanceByBuyRed(userId, amount.multiply(BigDecimal.valueOf(totalNumber)), (long) totalNumber);
                managerGameBaseService.pushCapitalUpdate(userId, UserCapitalTypeEnum.currency_2.getValue());
                for (int i = 0; i < totalNumber; i++) {
                    initRedEnvelopeAmounts(amount, userId);
                    addUserActive2Score(userId,getSendRedScore(amount));
                }
            }
            return new JSONObject();
        }
    }

    public double getSendRedScore(BigDecimal amount){
        if (amount.compareTo(BigDecimal.TEN)==0){
            return 5;
        } else if (amount.compareTo(new BigDecimal("20"))==0) {
            return 10;
        } else if (amount.compareTo(new BigDecimal("50"))==0) {
            return 25;
        } else if (amount.compareTo(new BigDecimal("100"))==0) {
            return 50;
        }
        return 0;
    }

    public double getGetRedScore(BigDecimal amount){
        if (amount.compareTo(BigDecimal.TEN)==0){
            return 1;
        } else if (amount.compareTo(new BigDecimal("20"))==0) {
            return 2;
        } else if (amount.compareTo(new BigDecimal("50"))==0) {
            return 5;
        } else if (amount.compareTo(new BigDecimal("100"))==0) {
            return 100;
        }
        return 0;
    }

    public void addUserActive2Score(Long userId, double score) {
        Activity activity = gameCacheService.getActivity2();
        if (activity == null) {
            return;
        }
        if (activity.getAddPointEvent() == 5) {
            gameCacheService.addPointMySelf(userId, score);
        }
    }

    @Transactional
    @ServiceMethod(code = "002", description = "抢包")
    public Object getRed(JSONObject data) {
        checkNull(data.get("userId"));
        Long userId = data.getLong("userId");
        String redId = data.getString("redId");
        synchronized (LockUtil.getlock(redId)) {
            //获取红包对象
            RedEnvelope redBean = redEnvelopeMap.get(redId);
            if (redBean == null) {
                throwExp("已经被抢光，换一个吧");
            }
            //抢红包之前判断玩家余额是否足够
            managerGameBaseService.checkBalance(userId, redBean.getTotalAmount(), UserCapitalTypeEnum.currency_2);
            Integer nowIndex = redBean.getNowIndex();
            if (nowIndex >= redBean.getTotalNumber()) {
                throwExp("已经被抢光，换一个吧");
            }
            //判断之前是否抢过这个红包
            List<RecordSheet> byRedId = recordSheetService.findByRedId(Long.valueOf(redId));
            for (RecordSheet recordSheet : byRedId) {
                if (Objects.equals(recordSheet.getUserId(), userId)) {
                    throwExp("已经点燃过此鞭炮，换一个吧");
                }
            }
            //获取这个下标的金额
            BigDecimal getAmount = redBean.getAmount().getBigDecimal(nowIndex);
            //添加抢红包金额
            String orderNo = OrderUtil.getOrder5Number();
            User user = userCacheService.getUserInfoById(userId);
            //插入抢红包记录
            RecordSheet recordSheet = recordSheetService.addRecord(userId, orderNo, getAmount, user.getName(), Long.valueOf(redId), nowIndex == redBean.getBombIndex() ? 1 : 0, user.getHeadImageUrl(), redBean.getTotalAmount());
            byRedId.add(recordSheet);
            //增加玩家余额
            if (getAmount.compareTo(new BigDecimal("0.01")) < 0) {
                getAmount = new BigDecimal("0.01");
            }
            userCapitalService.addUserBalanceByGetRed(getAmount, userId, orderNo, recordSheet.getId());
            //判断是不是炸弹
            if (nowIndex == redBean.getBombIndex()) {
                //如果是炸弹  扣除红包的钱
                int count = managerConfigService.getInteger(Config.RED_SEND_COUNT);
                userCapitalService.subUserBalanceByRedZd(userId, redBean.getTotalAmount(), recordSheet.getId(), orderNo);
                redPositionService.addCountByUserId(userId, redBean.getTotalAmount(), count);
            }
            //推送玩家余额变动
            managerGameBaseService.pushCapitalUpdate(userId, UserCapitalTypeEnum.currency_2.getValue());
            redBean.setNowIndex(redBean.getNowIndex() + 1);
            redBean.setSurplusAmount(redBean.getSurplusAmount().subtract(getAmount));
            if (redBean.getNowIndex() == redBean.getTotalNumber()) {
                redBean.setStatus(0);
                //这是最后一个人抢  从map中移除
                redEnvelopeMap.remove(redId);
                checkSendRedReward(redBean.getUserId(), redBean.getTotalAmount());
            }
            //更改数据库中的数据
            redEnvelopeService.updateRed(redBean);
            //查询这个红包的记录
            byRedId = recordSheetService.findByRedId(Long.valueOf(redId));
            JSONObject result = new JSONObject();
            result.put("recordList", byRedId);
            RedPosition byUserId = redPositionService.findByUserId(userId);
            result.put("count", byUserId.getCount1() + byUserId.getCount2() + byUserId.getCount3() + byUserId.getCount4());
            addUserActive2Score(userId,getGetRedScore(redBean.getTotalAmount()));
            return result;
        }
    }

    //返还奖励
    public void checkSendRedReward(Long userId, BigDecimal amount) {
        RedPosition redPosition = redPositionService.findByUserId(userId);
        if (redPosition != null) {
            // 计算奖励金额（原金额的105%）
            int count = managerConfigService.getInteger(Config.RED_SEND_COUNT);
            double rate = 1 + 1.0 / count;
            BigDecimal reward = amount.multiply(BigDecimal.valueOf(rate))
                    .setScale(2, RoundingMode.HALF_UP);
            //添加余额资产  改为发邮件
            JSONArray userIdArr = new JSONArray();
            User user = userCacheService.getUserInfoById(userId);
            userIdArr.add(user.getUserNo());
            JSONObject params = new JSONObject();
            params.put("userArr", userIdArr);
            params.put("title", "天官赐福奖励");
            params.put("context", "您的鞭炮已点燃完毕。请领取您的奖励");
            params.put("mailType", 1);
            JSONArray itemArr = new JSONArray();
            JSONObject itemInfo = new JSONObject();
            itemInfo.put("itemId", 2);
            itemInfo.put("itemNum", reward);
            itemArr.add(itemInfo);
            params.put("itemArr", itemArr);
            adminMailService.sendMail(null, params, null);
            //userCapitalService.addUserBalanceByGetRed(amount,userId,null,null);
            //推送玩家余额变动
            //managerGameBaseService.pushCapitalUpdate(userId,UserCapitalTypeEnum.currency_2.getValue());
        }
    }


}

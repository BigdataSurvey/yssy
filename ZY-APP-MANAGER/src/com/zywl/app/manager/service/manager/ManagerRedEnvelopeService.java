package com.zywl.app.manager.service.manager;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.google.common.collect.Lists;
import com.live.app.ws.enums.PushCode;
import com.live.app.ws.util.Push;
import com.zywl.app.base.bean.User;
import com.zywl.app.base.bean.hongbao.RecordSheet;
import com.zywl.app.base.bean.hongbao.RedEnvelope;
import com.zywl.app.base.bean.hongbao.RedEnvelopeVo;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.LockUtil;
import com.zywl.app.base.util.OrderUtil;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.enmus.UserCapitalTypeEnum;
import com.zywl.app.defaultx.service.RecordSheetService;
import com.zywl.app.defaultx.service.RedEnvelopeService;
import com.zywl.app.defaultx.service.UserCapitalService;
import com.zywl.app.manager.context.MessageCodeContext;
import com.zywl.app.manager.socket.ManagerSocketServer;
import org.springframework.beans.BeanUtils;
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
    private UserCapitalService userCapitalService;

    @Autowired
    private UserCacheService userCacheService;

    @Autowired
    private RecordSheetService recordSheetService;

    @Autowired
    private ManagerGameBaseService managerGameBaseService;

    private final Map<String, RedEnvelope> redEnvelopeMap = new ConcurrentHashMap<>();//可以被抢的红包
    private final ConcurrentMap<String, Integer> bombPositions = new ConcurrentHashMap<>(); // 存储炸弹位置
    private final ConcurrentMap<String, Integer> currentGrabIndex = new ConcurrentHashMap<>(); // 当前抢到第几个

    @PostConstruct
    public void _ManagerRecordService(){
        initRed();
        pushRed();
    }

    public void initRed(){
        //查询哪些可以被抢 插入到map
        List<RedEnvelope> queryResult = redEnvelopeService.findAllRedEnvelope();
        //遍历红包列表，将可抢的红包放入map
        queryResult.forEach(e->redEnvelopeMap.put(e.getId().toString(),e));


    }

    public void pushRed(){
        new Timer("推送红包雨").schedule(new TimerTask() {
            public void run() {
                try{
                    //推送map中的包
                    if(redEnvelopeMap.size()>0){
                        JSONObject jsonObject = new JSONObject();
                        Collection<RedEnvelope> values = redEnvelopeMap.values();
                        List<RedEnvelope> list = new ArrayList<>(values);
                        Collections.shuffle(list);
                        RedEnvelopeVo newRedEnvelope = new RedEnvelopeVo();

                        BeanUtils.copyProperties(list.get(0),newRedEnvelope);

                        jsonObject.put("queryResult",newRedEnvelope);
                        logger.info(jsonObject.toJSONString("推送红包数据"));
                        Push.push(PushCode.pushRed, null, jsonObject);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 1000, 200);
    }




    private RedEnvelope initRedEnvelopeAmounts() {
        RedEnvelope  redEnvelope =  new RedEnvelope();
        // 如果是炸弹红包，随机生成炸弹位置(1-9之间)
            Random random = new Random();
            int bombPos = random.nextInt(9)+1;//生成1-9的随机数
            bombPositions.put(String.valueOf(redEnvelope.getId()),bombPos);
            currentGrabIndex.put(String.valueOf(redEnvelope.getId()),0);
        // 生成普通红包
        List<BigDecimal> normalAmounts = divideRedPacket((double) (redEnvelope.getTotalAmount() * 1000),10);
        JSONArray array = new JSONArray(normalAmounts);
        redEnvelope.setAmount(array);
        redEnvelope.setStatus(1);
        redEnvelopeMap.put(String.valueOf(redEnvelope.getId()),redEnvelope);
        redEnvelopeService.save(redEnvelope);
        return redEnvelope;
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
            list.add(new BigDecimal(restAmount).divide(new BigDecimal(1000)).setScale(2,RoundingMode.HALF_UP));
        }
        return list;
    }



    @Transactional
    @ServiceMethod(code = "001", description = "发包")
    public Object sendRed(ManagerSocketServer adminSocketServer, JSONObject data) {
        checkNull(data.get("userId"), data.get("num"), data.get("amount"));
        Long userId = data.getLong("userId");
        synchronized (LockUtil.getlock(userId)){
            int totalNumber = data.getIntValue("totalNumber");
            BigDecimal amount = BigDecimal.valueOf(data.getDoubleValue("amount"));
            RedEnvelope redEnvelope = new RedEnvelope();
            synchronized (LockUtil.getlock(userId)) {
                //生成实体 插入db  插入map
                managerGameBaseService.checkBalance( userId,  amount.multiply(BigDecimal.valueOf(totalNumber)), UserCapitalTypeEnum.currency_2);
                userCapitalService.subUserBalanceByBuyRed( userId,  amount.multiply(BigDecimal.valueOf(totalNumber)), (long) totalNumber);
                managerGameBaseService.pushCapitalUpdate(userId,UserCapitalTypeEnum.currency_2.getValue());
                for (int i = 0; i < totalNumber; i++) {
                    initRedEnvelopeAmounts();
                }
            }
            return new JSONObject();
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
            //抢红包之前判断玩家余额是否足够
            managerGameBaseService.checkBalance(userId,redBean.getTotalAmouns(), UserCapitalTypeEnum.currency_2);
            Integer nowIndex = redBean.getNowIndex();
            if (nowIndex==10){
                throwExp("已经被抢光，换一个吧");
            }
            //获取这个下标的金额
            BigDecimal getAmount= redBean.getAmount().getBigDecimal(nowIndex);
            //添加抢红包金额
            String orderNo = OrderUtil.getOrder5Number();
            User user = userCacheService.getUserInfoById(userId);

            //插入抢红包记录
            Long dataId = recordSheetService.addRecord(userId, orderNo, getAmount, user.getName(), Long.valueOf(redId),nowIndex==redBean.getBombIndex()?1:0);
            //增加玩家余额
            userCapitalService.addUserBalanceByGetRed(getAmount,userId,orderNo,dataId);
            //判断是不是炸弹
            if (nowIndex==redBean.getBombIndex()){
                //如果是炸弹  扣除红包的钱
                userCapitalService.subUserBalanceByRedZd(userId,redBean.getTotalAmouns(),dataId,orderNo);
            }
            //推送玩家余额变动
            managerGameBaseService.pushCapitalUpdate(userId,UserCapitalTypeEnum.currency_2.getValue());
            redBean.setNowIndex(redBean.getNowIndex()+1);
            if (redBean.getNowIndex()==10){
                redBean.setStatus(0);
                //这是最后一个人抢  从map中移除
                redEnvelopeMap.remove(redId);
            }
            //更改数据库中的数据
            redEnvelopeService.updateRed(redBean);
            //查询这个红包的记录
            List<RecordSheet> byRedId = recordSheetService.findByRedId(Long.valueOf(redId));
            JSONObject result = new JSONObject();
            result.put("recordList",byRedId);
            return result;
        }
    }

    // 查询用户发出的红包记录
    public List<RedEnvelope> queryRecordsBySender(Long userId) {
        return redEnvelopeService.findRedEnvelopeById(userId);
    }


}

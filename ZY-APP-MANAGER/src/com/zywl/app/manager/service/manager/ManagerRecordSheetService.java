package com.zywl.app.manager.service.manager;

import com.alibaba.fastjson2.JSONObject;
import com.google.common.collect.Lists;
import com.live.app.ws.enums.PushCode;
import com.live.app.ws.util.Push;
import com.zywl.app.base.bean.hongbao.RedEnvelope;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.LockUtil;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.service.RecordSheetService;
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


/**
 * 红包雨
 */
@Service
@ServiceClass(code = MessageCodeContext.RECORD)
public class ManagerRecordSheetService extends BaseService {


    @Autowired
    private RecordSheetService recordSheetService;


    //可以被抢的红包
//    private final Map<String, RedEnvelope> redEnvelopeMap = new ConcurrentHashMap<>();
//    private final Random random = new Random();
//
//
//
//    public static void main(String[] args) {
//        // 创建控制器
//       /* ManagerRecordService managerRecordService = new ManagerRecordService();
//
//        // 创建普通红包
////        System.out.println("===== 创建普通红包 =====");
////        RedEnvelope redEnvelope = managerRecordService.createRedEnvelope(1001, 100);
////        System.out.println("普通红包创建成功，ID: " + redEnvelope.getId());
//
//        // 创建炸弹红包
//        System.out.println("\n===== 创建炸弹红包 =====");
//        RedEnvelope bombRedEnvelope = managerRecordService.createRedEnvelope(1,100);
//        System.out.println("炸弹红包创建成功，ID: " + bombRedEnvelope.getId());
//
//        // 模拟用户抢炸弹红包
//        System.out.println("\n===== 抢炸弹红包 =====");
//        for (int i = 1; i <= 10; i++) {
//            String userId = "user" + (1010 + i);
//            System.out.println(userId + " 抢红包: " + bombRedEnvelope.getId());
//        }*/
//
//        List<BigDecimal> a = divideRedPacket(10000.0,10);
//        System.out.println(a);
//    }
//
//
//
//
//
//
//
//
//    private void initRedEnvelopeAmounts() {
//        RedEnvelope   redEnvelope =  new RedEnvelope();
//        // 炸弹随机数
//
//
//        // 生成普通红包
//        List<BigDecimal> normalAmounts = divideRedPacket((double) (redEnvelope.getTotalAmount() * 1000),10);
//
//
//        // 打乱普通红包顺序
//        Collections.shuffle(normalAmounts);
//
//
//
//        // 初始化已抢红包列表
//
//
//        // 设置红包金额和状态
//
//    }
//
//
//
//
//
//
//
//
//    /**
//     * 二倍均值法的算法实现 - 算法里面的金额以 灵石的1000倍后 为单位 ，相当于1灵石=1000子灵石
//     *
//     * @param totalAmount 红包总金额
//     * @param totalPeople 红包总人数
//     * @return
//     */
//    public static List<BigDecimal> divideRedPacket(final Double totalAmount, final Integer totalPeople) {
//        List<BigDecimal> list = Lists.newLinkedList();
//        if (totalAmount > 0 && totalPeople > 0) {
//            Double restAmount = totalAmount;
//            Double restPeople = Double.valueOf(totalPeople);
//            BigDecimal realAmount;
//            Random random = new Random();
//            Double amount;
//            for (int i = 0; i < totalPeople - 1; i++) {
//                //左闭右开 [1,剩余金额/剩余人数 的除数 的两倍  )
//                double v = restAmount / restPeople * 2.0 - 1.0;
//                amount = 0 + (v - 0) * random.nextDouble() + 1;
//                realAmount = new BigDecimal(amount).divide(new BigDecimal(1000)).setScale(2, RoundingMode.HALF_UP);
//                list.add(realAmount);
//                //剩余金额
//                restAmount -= amount;
//                restPeople--;
//            }
//            //最后的剩余金额
//            list.add(new BigDecimal(restAmount).divide(new BigDecimal(1000)).setScale(2,RoundingMode.HALF_UP));
//        }
//        return list;
//    }
//
//
//
//    @Transactional
//    @ServiceMethod(code = "001", description = "发包")
//    public Object sendRed(ManagerSocketServer adminSocketServer, JSONObject data) {
//        checkNull(data.get("userId"),data.get("num"),data.get("amount"));
//        Long userId = data.getLong("userId");
//        int num = data.getIntValue("num");
//        double amount = data.getDoubleValue("amount");
//        synchronized (LockUtil.getlock(userId)){
//            //生成实体 插入db  插入map
//        }
//        return null;
//    }
//
//    @Transactional
//    @ServiceMethod(code = "002", description = "抢包")
//    public Object getRed(ManagerSocketServer adminSocketServer, JSONObject data) {
//        checkNull(data.get("userId"),data.get("num"),data.get("amount"));
//        Long userId = data.getLong("userId");
//        String redId = data.getString("redId");
//        synchronized (LockUtil.getlock(userId)){
//            //从map里获取红包  update 红包对象  updateDb
//            //如果是最后一个  map就remove
//        }
//        return null;
//    }
//
//    @PostConstruct
//    public void _ManagerRecordService(){
//        //查询哪些可以被抢 插入到map
//        initRed();
//        pushRed();
//    }
//
//    public void initRed(){
//
//        //查询红包 插入到Map
//        List<RedEnvelope> queryResult = recordSheetService.findAllRecord();
//        RedEnvelope newRedEnvelope = new RedEnvelope();
//        newRedEnvelope.setAmount(queryResult.get(0).getAmount());
//        newRedEnvelope.setReleasedQuantity(queryResult.get(0).getReleasedQuantity());
//        newRedEnvelope.setRedAward(queryResult.get(0).getRedAward());
//        newRedEnvelope.setSurplusAmount(queryResult.get(0).getSurplusAmount());
//        newRedEnvelope.setCreateTime(queryResult.get(0).getCreateTime());
//        newRedEnvelope.setUpdateTime(queryResult.get(0).getUpdateTime());
//        newRedEnvelope.setAllocationAmount(queryResult.get(0).getAllocationAmount());
//        newRedEnvelope.setBombAmount(queryResult.get(0).getBombAmount());
//        recordSheetService.save(newRedEnvelope);
//
//    }
//
//    public void pushRed(){
//        new Timer("推送红包雨").schedule(new TimerTask() {
//            public void run() {
//                try{
//                    //推送map中的包
//                    JSONObject jsonObject = new JSONObject();
//                    List<RedEnvelope> queryResult = recordSheetService.findAllRecord();
//                    RedEnvelope  newRedEnvelope = new RedEnvelope();
//                    BeanUtils.copyProperties(queryResult.get(0),newRedEnvelope);
//                    jsonObject.put("queryResult",newRedEnvelope);
//                    logger.info(jsonObject.toJSONString("推送红包数据"));
//                    Push.push(PushCode.pushRed, null, jsonObject);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }, 1000, 200);
//    }



}

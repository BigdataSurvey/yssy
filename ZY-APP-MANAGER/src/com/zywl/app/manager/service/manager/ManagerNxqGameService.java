package com.zywl.app.manager.service.manager;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.enums.PushCode;
import com.live.app.ws.enums.TargetSocketType;
import com.live.app.ws.util.CommandBuilder;
import com.live.app.ws.util.Executer;
import com.live.app.ws.util.Push;
import com.zywl.app.base.bean.*;
import com.zywl.app.base.constant.RedisKeyConstant;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.base.util.LockUtil;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.cache.GameCacheService;
import com.zywl.app.defaultx.enmus.ActivityAddPointEventEnum;
import com.zywl.app.defaultx.enmus.LogCapitalTypeEnum;
import com.zywl.app.defaultx.enmus.UserCapitalTypeEnum;
import com.zywl.app.defaultx.service.*;
import com.zywl.app.manager.context.MessageCodeContext;
import com.zywl.app.manager.service.PlayGameService;
import com.zywl.app.manager.socket.ManagerSocketServer;
import com.zywl.app.manager.util.RequestManagerListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@ServiceClass(code = MessageCodeContext.NXQ)
public class ManagerNxqGameService extends BaseService {



    @Autowired
    private InvestDetailService investDetailService;

    @Autowired
    private  PlayGameService gameService;
    @Autowired
    private DicJackpotService dicJackpotService;
    @Autowired
    private UserCapitalService userCapitalService;
    @Autowired
    private ManagerGameBaseService managerGameBaseService;
    @Autowired
    private BackpackService backpackService;

    private static final String JP = "47";
    private static final String FZ = "48";
    public static String key = DateUtil.getCurrent5();

    public static List<DicJackpot> dicNccList  = new ArrayList<>();

    @Autowired
    private PlayGameService gameCacheService;
    public static Map<String, List<Map<String, String>>> userCapitals = new ConcurrentHashMap<>();

    @PostConstruct
    public void _ServerMineService() {
        init();
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
                        Executer.request(TargetSocketType.nxq, CommandBuilder.builder().request("200901", object).build(), new RequestManagerListener(null));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }, 0, 1);



    }


    public void init() {
        dicNccList = dicJackpotService.findAll();
    }

    @Transactional
    @ServiceMethod(code = "001", description = "投入信物")
    public Object invest(ManagerSocketServer adminSocketServer, JSONObject params) {
        checkNull(params);
        Long userId = params.getLong("userId");
        Integer betAmount = params.getIntValue("betAmount");
        synchronized (LockUtil.getlock(userId)) {

            /**
             * 1.投入信物 周期一个月 从奖池中获取游园券 产出 投入道具 每天产出游园券
             * 2.投入信物需要查询当前玩家背包中是否足够投入当前数量
             * 3.如果足够直接插入
             */
            double JPNumber = gameService.getUserItemNumber(userId, JP);
            double FZNumber = gameService.getUserItemNumber(userId, FZ);
            if(JPNumber < betAmount || FZNumber < betAmount){
                throwExp("晶魄或者发簪数量不足");
            }
            long i  = investDetailService.addInvestDetail(userId, betAmount);
            backpackService.subItemNumberByDts(userId, Long.valueOf(JP), betAmount);
            backpackService.subItemNumberByDts(userId, Long.valueOf(FZ), betAmount);
            gameCacheService.addRankCache(String.valueOf(userId),betAmount,11);
            return i;
        }
    }




    @Transactional
    @ServiceMethod(code = "002", description = "查询宁采臣心动值")
    public Object selectNccHeart(ManagerSocketServer adminSocketServer, JSONObject params) {
        checkNull(params);
        Long userId = params.getLong("userId");
        JSONObject jsonObject = new JSONObject();
        synchronized (LockUtil.getlock(userId)) {
            /**
             * 1.每次查询宁采臣的心动值的时候 查询锻造访小倩生成的表内的数据
             * （查询时需要限制一下没有过期的数据，如果查询时跟投入时间相差一个月，赋值状态为已过期）
             * 2.进行累加 并同步到宁采臣信息表中 下次查询直接读取宁采臣的表中的数据饿 重新进行同步累加
             */
            List<InvestDetail> investDetailList = investDetailService.findInvestDetail(params);
            List<InvestDetail> newInvestDetailList = new ArrayList<>();
            for (InvestDetail investDetail : investDetailList) {
                BigDecimal heartNumber = BigDecimal.ZERO;
                //如果当前时间已经超过结束时间 将状态设置为过期
                if (((System.currentTimeMillis() - investDetail.getEndDate().getTime()) / 1000 / 60 / 60 / 24) > 1) {
                    investDetail.setInvestSealStatus(1);
                    //如果当前时间大于投入时间一天 才可以进行累加收益
                }else  if((System.currentTimeMillis() - investDetail.getInvestDate().getTime() / 1000 / 60 / 60 / 24)>1){
                    if(investDetail.getGenerYyq()==null){
                        heartNumber = heartNumber.add(dicNccList.get(0).getProduction().multiply(BigDecimal.valueOf(investDetail.getInvestNumber())));
                        investDetail.setUnReceive(heartNumber);
                    }else {
                        heartNumber = heartNumber.add(investDetail.getUnReceive());
                        investDetail.setUnReceive(heartNumber);
                    }
                }
                newInvestDetailList.add(investDetail);
            }
            if (newInvestDetailList.size()>0){
                investDetailService.batchUpdateInvestDetail(newInvestDetailList);
            }
            investDetailList.removeIf(item -> ((System.currentTimeMillis() - item.getEndDate().getTime()) / 1000 / 60 / 60 / 24) > 1);

            BigDecimal totalAmount = investDetailList.stream()
                    .map(InvestDetail::getUnReceive)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal receiveAmount30days = investDetailList.stream()
                    .map(InvestDetail::getGenerYyq).filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add).multiply(BigDecimal.valueOf(30));
            jsonObject.put("investDetailList",investDetailList);
            jsonObject.put("dicNccList",dicNccList);
            jsonObject.put("returnNum",investDetailList.size());
            jsonObject.put("receiveAmount",totalAmount);
            jsonObject.put("receiveAmount30days",receiveAmount30days);
        }
        return jsonObject;
    }


    @Transactional
    @ServiceMethod(code = "003", description = "领取收益")
    public Object getReceive(ManagerSocketServer adminSocketServer, JSONObject params) {
        checkNull(params);
        Long userId = params.getLong("userId");
        synchronized (LockUtil.getlock(userId)) {

            /**
             * 1.获取未过期的未领取收益
             * 2.清除未领取收益
             * 3.奖池中扣掉值
             */
            List<InvestDetail> investDetailList = investDetailService.findInvestDetail(params);
            //判断收益
            if(!(investDetailList.size() >0) || investDetailList == null){
                throwExp("当前没有可领取收益");
            }
            //当前用户所有投入之后未领取的收益累加进行奖池扣除
            BigDecimal allUnReceive = investDetailList.stream()
                    .map(InvestDetail::getUnReceive)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal amonut = dicNccList.get(0).getAmonut();
            amonut = amonut.subtract(allUnReceive);
            dicNccList.get(0).setAmonut(amonut);
            dicJackpotService.update(dicNccList.get(0));
            //领取的收益加钱
            userCapitalService.addUserBalanceByAddReward(allUnReceive,userId, UserCapitalTypeEnum.yyb.getValue(), LogCapitalTypeEnum.add_receive_nxq);
            managerGameBaseService.pushCapitalUpdate(userId, UserCapitalTypeEnum.yyb.getValue());
            for (InvestDetail investDetail : investDetailList) {
                investDetail.setGenerYyq(investDetail.getUnReceive());
                investDetail.setUnReceive(BigDecimal.ZERO);
            }
            long i = investDetailService.batchUpdateInvestDetail(investDetailList);
            return i;
        }

    }

    @Transactional
    @ServiceMethod(code = "006", description = "领取记录")
    public Object getReceiveRecord(ManagerSocketServer adminSocketServer, JSONObject params) {
        checkNull(params);
        Long userId = params.getLong("userId");
        synchronized (LockUtil.getlock(userId)) {
            List<InvestDetail> investDetailList = investDetailService.getReceiveRecord(params);
            return investDetailList;
        }
    }



    public int getItemNumber(Long userId, String itemId) {
        Map<String, Backpack> userBackpack = gameService.getUserBackpack(userId.toString());
        if (userBackpack.containsKey(itemId)) {
            return (int) userBackpack.get(itemId).getItemNumber();
        }
        return 0;
    }




}

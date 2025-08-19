package com.zywl.app.manager.service.manager;

import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.DicJackpot;
import com.zywl.app.base.bean.InvestDetail;
import com.zywl.app.base.bean.Backpack;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.LockUtil;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.enmus.LogCapitalTypeEnum;
import com.zywl.app.defaultx.enmus.UserCapitalTypeEnum;
import com.zywl.app.defaultx.service.DicJackpotService;
import com.zywl.app.defaultx.service.InvestDetailService;
import com.zywl.app.defaultx.service.NccInfoService;
import com.zywl.app.defaultx.service.UserCapitalService;
import com.zywl.app.manager.context.MessageCodeContext;
import com.zywl.app.manager.service.PlayGameService;
import com.zywl.app.manager.socket.ManagerSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    private static final String JP = "47";
    private static final String FZ = "48";

    public static List<DicJackpot> dicNccList  = new ArrayList<>();

    @PostConstruct
    public void _ServerMineService() {
        init();
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
            int JPNumber = getItemNumber(userId, JP);
            int FZNumber = getItemNumber(userId, FZ);
            if(JPNumber == 0  || FZNumber == 0){
                throwExp("晶魄或者发簪数量不足");
            }
            long i  = investDetailService.addInvestDetail(userId, betAmount);
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
            BigDecimal heartNumber = BigDecimal.ZERO;
            List<InvestDetail> investDetailList = investDetailService.findInvestDetail(params);
            for (InvestDetail investDetail : investDetailList) {
                if (((System.currentTimeMillis() - investDetail.getEndDate().getTime()) / 1000 / 60 / 60 / 24) > 1) {
                    investDetail.setInvestSealStatus(1);
                }else {
                    heartNumber = heartNumber.add(dicNccList.get(0).getProduction().multiply(BigDecimal.valueOf(investDetail.getInvestNumber())));
                    investDetail.setUnReceive(heartNumber);
                }
                investDetailService.updateInvestDetail(investDetail);
            }
            jsonObject.put("investDetailList",investDetailList);
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
            //当前用户所有投入之后未领取的收益累加进行奖池扣除
            BigDecimal allUnReceive = investDetailList.stream()
                    .map(InvestDetail::getUnReceive)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal amonut = dicNccList.get(0).getAmonut();
            amonut = amonut.subtract(allUnReceive);
            dicNccList.get(0).setAmonut(amonut);
            dicJackpotService.update(dicNccList.get(0));
            //领取的收益加钱
            userCapitalService.addUserBalanceByAddReward(allUnReceive,userId, UserCapitalTypeEnum.currency_2.getValue(), LogCapitalTypeEnum.add_receive_nxq);
            managerGameBaseService.pushCapitalUpdate(userId, UserCapitalTypeEnum.currency_2.getValue());
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

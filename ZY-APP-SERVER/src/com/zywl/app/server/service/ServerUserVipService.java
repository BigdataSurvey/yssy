package com.zywl.app.server.service;

import cn.hutool.core.lang.hash.Hash;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.bean.Command;
import com.live.app.ws.enums.PushCode;
import com.live.app.ws.enums.TargetSocketType;
import com.live.app.ws.util.CommandBuilder;
import com.live.app.ws.util.Executer;
import com.live.app.ws.util.Push;
import com.zywl.app.base.bean.*;
import com.zywl.app.base.bean.vo.UserDailyTaskVo;
import com.zywl.app.base.constant.KafkaEventContext;
import com.zywl.app.base.constant.KafkaTopicContext;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.Async;
import com.zywl.app.base.util.LockUtil;
import com.zywl.app.base.util.OrderUtil;
import com.zywl.app.defaultx.annotation.KafkaProducer;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.cache.UserCapitalCacheService;
import com.zywl.app.defaultx.enmus.LogCapitalTypeEnum;
import com.zywl.app.defaultx.enmus.UserCapitalTypeEnum;
import com.zywl.app.defaultx.enmus.VipLevelTypeEnum;
import com.zywl.app.defaultx.service.DicVipService;
import com.zywl.app.defaultx.service.UserCapitalService;
import com.zywl.app.defaultx.service.UserVipService;
import com.zywl.app.defaultx.service.VipReceiveRecordService;
import com.zywl.app.server.context.MessageCodeContext;
import com.zywl.app.server.socket.AppSocket;
import com.zywl.app.server.util.RequestManagerListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@ServiceClass(code = MessageCodeContext.USER_VIP)
public class ServerUserVipService extends BaseService {

    @Autowired
    private ServerConfigService serverConfigService;
    @Autowired
    private UserVipService userVipService;

    @Autowired
    private DicVipService dicVipService;

    @Autowired
    private VipReceiveRecordService vipReceiveRecordService;
    @Autowired
    private UserCapitalService userCapitalService;
    @Autowired
    private UserCapitalCacheService userCapitalCacheService;


    private final static Map<String, DicVip> DIC_VIP_MAP = new ConcurrentHashMap<>();
    private final static String RECEIVED  = "1";
    private final static String UNRECEIVE = "0";

    @PostConstruct
    public void _ServerUserVipService() {
        initDicVip();
    }

    public void initDicVip(){
        List<DicVip> allVip = dicVipService.findAllVip();
        allVip.forEach(e->DIC_VIP_MAP.put(String.valueOf(e.getLv()),e));
    }

    @ServiceMethod(code = "001", description = "获取vip信息")
    public Object getVipInfo(final AppSocket appSocket, Command appCommand, JSONObject params) {
        //获取当前等级
        checkNull(params);
        String receiveState = "";
        Long userId = appSocket.getWsidBean().getUserId();
        JSONObject result = new JSONObject();
        UserVip rechargeAmountByUserId = userVipService.findRechargeAmountByUserId(userId);
        //判断领取状态
        List<VipReceiveRecord> vipReceiveRecord = vipReceiveRecordService.findVipReceiveRecordByLevel(userId,rechargeAmountByUserId.getVipLevel());
        if(vipReceiveRecord.size()>0){
            //说明该用户已领取过该等级奖励
            receiveState = RECEIVED;
        }else {
            receiveState = UNRECEIVE;
        }
        // 当前经验 升到下一级需要多少经验
        //BigDecimal differ = comparToRechargeAmount(rechargeAmountByUserId.getRechargeAmount());
        int endExp = DIC_VIP_MAP.get(String.valueOf(rechargeAmountByUserId.getVipLevel()-1)).getEndExp();
        JSONArray reward = DIC_VIP_MAP.get(String.valueOf(rechargeAmountByUserId.getVipLevel()-1)).getReward();
        result.put("endExp", endExp);
        result.put("receiveState", receiveState);
        result.put("reward", reward);
        result.put("vipInfo", rechargeAmountByUserId);
        return result;
    }


    @Transactional
    @ServiceMethod(code = "002", description = "领取vip特权礼包")
    @KafkaProducer(topic = KafkaTopicContext.RED_POINT, event = KafkaEventContext.DO_DAILY_TASK, sendParams = true)
    public Async receiveUserVipGift(final AppSocket appSocket, Command appCommand, JSONObject params) {
            checkNull(params);
            params.put("userId", appSocket.getWsidBean().getUserId());
            Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("9008011", params).build(),
                    new RequestManagerListener(appCommand));
            return async();
        }



    private BigDecimal comparToRechargeAmount(BigDecimal rechargeAmount) {
        BigDecimal differ = BigDecimal.ZERO;
        if (rechargeAmount.compareTo(VipLevelTypeEnum.VIP1.getValue()) < 0) {
            differ = VipLevelTypeEnum.VIP1.getValue().subtract(rechargeAmount);
        }
        if (rechargeAmount.compareTo(VipLevelTypeEnum.VIP2.getValue()) < 0 && rechargeAmount.compareTo(VipLevelTypeEnum.VIP1.getValue()) > 0) {
            differ = VipLevelTypeEnum.VIP2.getValue().subtract(rechargeAmount);
        }
        if (rechargeAmount.compareTo(VipLevelTypeEnum.VIP3.getValue()) < 0 && rechargeAmount.compareTo(VipLevelTypeEnum.VIP2.getValue()) > 0) {
            differ = VipLevelTypeEnum.VIP3.getValue().subtract(rechargeAmount);

        }
        if (rechargeAmount.compareTo(VipLevelTypeEnum.VIP4.getValue()) < 0 && rechargeAmount.compareTo(VipLevelTypeEnum.VIP3.getValue()) > 0) {
            differ = VipLevelTypeEnum.VIP4.getValue().subtract(rechargeAmount);

        }
        if (rechargeAmount.compareTo(VipLevelTypeEnum.VIP5.getValue()) < 0 && rechargeAmount.compareTo(VipLevelTypeEnum.VIP4.getValue()) > 0) {
            differ = VipLevelTypeEnum.VIP5.getValue().subtract(rechargeAmount);

        }
        if (rechargeAmount.compareTo(VipLevelTypeEnum.VIP6.getValue()) < 0 && rechargeAmount.compareTo(VipLevelTypeEnum.VIP5.getValue()) > 0) {
            differ = VipLevelTypeEnum.VIP6.getValue().subtract(rechargeAmount);

        }
        if (rechargeAmount.compareTo(VipLevelTypeEnum.VIP7.getValue()) < 0 && rechargeAmount.compareTo(VipLevelTypeEnum.VIP6.getValue()) > 0) {
            differ = VipLevelTypeEnum.VIP7.getValue().subtract(rechargeAmount);

        }
        if (rechargeAmount.compareTo(VipLevelTypeEnum.VIP8.getValue()) < 0 && rechargeAmount.compareTo(VipLevelTypeEnum.VIP7.getValue()) > 0) {
            differ = VipLevelTypeEnum.VIP8.getValue().subtract(rechargeAmount);

        }
        if (rechargeAmount.compareTo(VipLevelTypeEnum.VIP9.getValue()) < 0 && rechargeAmount.compareTo(VipLevelTypeEnum.VIP8.getValue()) > 0) {
            differ = VipLevelTypeEnum.VIP9.getValue().subtract(rechargeAmount);

        }
        if (rechargeAmount.compareTo(VipLevelTypeEnum.VIP10.getValue()) < 0 && rechargeAmount.compareTo(VipLevelTypeEnum.VIP9.getValue()) > 0) {
            differ = VipLevelTypeEnum.VIP10.getValue().subtract(rechargeAmount);


        }
        return differ;
    }

}

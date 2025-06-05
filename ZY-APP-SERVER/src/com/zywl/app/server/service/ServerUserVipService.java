package com.zywl.app.server.service;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.bean.Command;
import com.zywl.app.base.bean.Config;
import com.zywl.app.base.bean.DicVip;
import com.zywl.app.base.bean.UserVip;
import com.zywl.app.base.bean.VipReceiveRecord;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.enmus.VipLevelTypeEnum;
import com.zywl.app.defaultx.service.DicVipService;
import com.zywl.app.defaultx.service.UserVipService;
import com.zywl.app.defaultx.service.VipReceiveRecordService;
import com.zywl.app.server.context.MessageCodeContext;
import com.zywl.app.server.socket.AppSocket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
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
        int endExp = DIC_VIP_MAP.get(String.valueOf(rechargeAmountByUserId.getVipLevel())).getEndExp();
        JSONArray reward = DIC_VIP_MAP.get(String.valueOf(rechargeAmountByUserId.getVipLevel())).getReward();
        result.put("endExp", endExp);
        result.put("receiveState", receiveState);
        result.put("reward", reward);
        result.put("vipInfo", rechargeAmountByUserId);
        return result;
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

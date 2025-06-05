package com.zywl.app.server.service;

import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.bean.Command;
import com.zywl.app.base.bean.Config;
import com.zywl.app.base.bean.UserVip;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.enmus.VipLevelTypeEnum;
import com.zywl.app.defaultx.service.UserVipService;
import com.zywl.app.server.context.MessageCodeContext;
import com.zywl.app.server.socket.AppSocket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@ServiceClass(code = MessageCodeContext.USER_VIP)
public class ServerUserVipService extends BaseService {

    @Autowired
    private ServerConfigService serverConfigService;
    @Autowired
    private UserVipService userVipService;

    @ServiceMethod(code = "001", description = "获取vip信息")
    public Object getVipInfo(final AppSocket appSocket, Command appCommand, JSONObject params) {
        //获取当前等级
        checkNull(params);
        Long userId = appSocket.getWsidBean().getUserId();
        JSONObject result = new JSONObject();
        UserVip rechargeAmountByUserId = userVipService.findRechargeAmountByUserId(userId);
        // 当前经验 升到下一级需要多少经验
        BigDecimal differ = comparToRechargeAmount(rechargeAmountByUserId.getRechargeAmount());
        result.put("differ",differ);
        result.put("vipInfo",rechargeAmountByUserId);
        return result;
    }
    private BigDecimal comparToRechargeAmount(BigDecimal rechargeAmount){
        BigDecimal differ = BigDecimal.ZERO;
        if(rechargeAmount.compareTo(VipLevelTypeEnum.VIP1.getValue())<0 ){
            differ = VipLevelTypeEnum.VIP1.getValue().subtract(rechargeAmount);
        }
        if(rechargeAmount.compareTo(VipLevelTypeEnum.VIP2.getValue())<0 && rechargeAmount.compareTo(VipLevelTypeEnum.VIP1.getValue())>0 ){
            differ = VipLevelTypeEnum.VIP2.getValue().subtract(rechargeAmount);
        }
        if(rechargeAmount.compareTo(VipLevelTypeEnum.VIP3.getValue())<0 && rechargeAmount.compareTo(VipLevelTypeEnum.VIP2.getValue())>0){
            differ = VipLevelTypeEnum.VIP3.getValue().subtract(rechargeAmount);

        }
        if(rechargeAmount.compareTo(VipLevelTypeEnum.VIP4.getValue())<0 && rechargeAmount.compareTo(VipLevelTypeEnum.VIP3.getValue())>0){
            differ = VipLevelTypeEnum.VIP4.getValue().subtract(rechargeAmount);

        }
        if(rechargeAmount.compareTo(VipLevelTypeEnum.VIP5.getValue())<0 && rechargeAmount.compareTo(VipLevelTypeEnum.VIP4.getValue())>0 ){
            differ = VipLevelTypeEnum.VIP5.getValue().subtract(rechargeAmount);

        }
        if(rechargeAmount.compareTo(VipLevelTypeEnum.VIP6.getValue())<0 && rechargeAmount.compareTo(VipLevelTypeEnum.VIP5.getValue())>0){
            differ = VipLevelTypeEnum.VIP6.getValue().subtract(rechargeAmount);

        }
        if(rechargeAmount.compareTo(VipLevelTypeEnum.VIP7.getValue())<0 && rechargeAmount.compareTo(VipLevelTypeEnum.VIP6.getValue())>0){
            differ = VipLevelTypeEnum.VIP7.getValue().subtract(rechargeAmount);

        }if(rechargeAmount.compareTo(VipLevelTypeEnum.VIP8.getValue())<0 && rechargeAmount.compareTo(VipLevelTypeEnum.VIP7.getValue())>0){
            differ = VipLevelTypeEnum.VIP8.getValue().subtract(rechargeAmount);

        }if(rechargeAmount.compareTo(VipLevelTypeEnum.VIP9.getValue())<0 && rechargeAmount.compareTo(VipLevelTypeEnum.VIP8.getValue())>0){
            differ = VipLevelTypeEnum.VIP9.getValue().subtract(rechargeAmount);

        }if(rechargeAmount.compareTo(VipLevelTypeEnum.VIP10.getValue())<0 && rechargeAmount.compareTo(VipLevelTypeEnum.VIP9.getValue())>0){
            differ = VipLevelTypeEnum.VIP10.getValue().subtract(rechargeAmount);


        }
         return  differ;
    }

}

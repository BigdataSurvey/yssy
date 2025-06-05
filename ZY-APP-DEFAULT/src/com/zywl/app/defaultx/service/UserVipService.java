package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.UserVip;
import com.zywl.app.base.util.LockUtil;
import com.zywl.app.defaultx.dbutil.DaoService;
import com.zywl.app.defaultx.enmus.UserCapitalTypeEnum;
import com.zywl.app.defaultx.enmus.VipLevelTypeEnum;
import org.apache.commons.collections4.map.HashedMap;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserVipService extends DaoService {

    public UserVipService() {
        super("UserVipMapper");
    }

    public void addExper(UserVip userVip) {
        // 插入或者修改数据 第一次买就插入 否则数量+1
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userVip.getUserId());
        //vip等级判断
        long vipLevel = buildVipLevel(userVip.getRechargeAmount());

        params.put("vipLevel",vipLevel);
        params.put("rechargeAmount", userVip.getRechargeAmount());
        params.put("createTime", new Date());
        params.put("updateTime", new Date());
        int a = execute("insertOrUpdate", params);
        if (a<1){
            throwExp("购买礼包失败，请联系客服");
        }
    }

    public UserVip findRechargeAmountByUserId(Long userId) {
        Map<String,Object> parameters = new HashedMap<>();
        parameters.put("userId", userId);
        return (UserVip) findOne("findRechargeAmountByUserId", parameters);

    }
    public long buildVipLevel(BigDecimal rechargeAmount){
        long vipLevel= 0;
        if(rechargeAmount.compareTo(VipLevelTypeEnum.VIP1.getValue())<0 ){
            vipLevel=VipLevelTypeEnum.VIP1.getLevel();
        }
        if(rechargeAmount.compareTo(VipLevelTypeEnum.VIP2.getValue())<0 && rechargeAmount.compareTo(VipLevelTypeEnum.VIP1.getValue())>0 ){
            vipLevel=VipLevelTypeEnum.VIP2.getLevel();
        }
        if(rechargeAmount.compareTo(VipLevelTypeEnum.VIP3.getValue())<0 && rechargeAmount.compareTo(VipLevelTypeEnum.VIP2.getValue())>0){
            vipLevel=VipLevelTypeEnum.VIP3.getLevel();
        }
        if(rechargeAmount.compareTo(VipLevelTypeEnum.VIP4.getValue())<0 && rechargeAmount.compareTo(VipLevelTypeEnum.VIP3.getValue())>0){
            vipLevel=VipLevelTypeEnum.VIP4.getLevel();
        }
        if(rechargeAmount.compareTo(VipLevelTypeEnum.VIP5.getValue())<0 && rechargeAmount.compareTo(VipLevelTypeEnum.VIP4.getValue())>0 ){
            vipLevel=VipLevelTypeEnum.VIP5.getLevel();
        }
        if(rechargeAmount.compareTo(VipLevelTypeEnum.VIP6.getValue())<0 && rechargeAmount.compareTo(VipLevelTypeEnum.VIP5.getValue())>0){
            vipLevel=VipLevelTypeEnum.VIP6.getLevel();
        }
        if(rechargeAmount.compareTo(VipLevelTypeEnum.VIP7.getValue())<0 && rechargeAmount.compareTo(VipLevelTypeEnum.VIP6.getValue())>0){
            vipLevel=VipLevelTypeEnum.VIP7.getLevel();
        }if(rechargeAmount.compareTo(VipLevelTypeEnum.VIP8.getValue())<0 && rechargeAmount.compareTo(VipLevelTypeEnum.VIP7.getValue())>0){
            vipLevel=VipLevelTypeEnum.VIP8.getLevel();
        }if(rechargeAmount.compareTo(VipLevelTypeEnum.VIP9.getValue())<0 && rechargeAmount.compareTo(VipLevelTypeEnum.VIP8.getValue())>0){
            vipLevel=VipLevelTypeEnum.VIP9.getLevel();
        }if(rechargeAmount.compareTo(VipLevelTypeEnum.VIP10.getValue())<0 && rechargeAmount.compareTo(VipLevelTypeEnum.VIP9.getValue())>0){
            vipLevel=VipLevelTypeEnum.VIP10.getLevel();
        }
        return vipLevel;
    }
}

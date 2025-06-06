package com.zywl.app.manager.service.manager;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.*;
import com.zywl.app.base.bean.vo.UserDailyTaskVo;
import com.zywl.app.base.constant.KafkaEventContext;
import com.zywl.app.base.constant.KafkaTopicContext;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.LockUtil;
import com.zywl.app.base.util.OrderUtil;
import com.zywl.app.defaultx.annotation.KafkaProducer;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.cache.UserCapitalCacheService;
import com.zywl.app.defaultx.enmus.LogCapitalTypeEnum;
import com.zywl.app.defaultx.enmus.UserCapitalTypeEnum;
import com.zywl.app.defaultx.service.UserVipService;
import com.zywl.app.defaultx.service.VipReceiveRecordService;
import com.zywl.app.manager.context.MessageCodeContext;
import com.zywl.app.manager.service.PlayGameService;
import com.zywl.app.manager.socket.ManagerSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Service
@ServiceClass(code = MessageCodeContext.USER_VIP)
public class ManagerUserVipService extends BaseService {

    @Autowired
    private ManagerConfigService managerConfigService;
    @Autowired
    private UserCapitalCacheService userCapitalCacheService;
    @Autowired
    private UserVipService userVipService;

    @Autowired
    private VipReceiveRecordService vipReceiveRecordService;


    /**
     * 新增经验
     */
    public void addExper(long userId, BigDecimal rechargeAmount) {
        //vip等级
        UserVip uservip = userVipService.findRechargeAmountByUserId(userId);
        //充值金额（经验）
        uservip.setRechargeAmount(uservip.getRechargeAmount().add(rechargeAmount));
        //获取当前等级vip信息
        DicVip dicVip = PlayGameService.DIC_VIP_MAP.get(String.valueOf(uservip.getVipLevel()));
        //如果当前的经验大于了当前等级的最大经验  就是升级了  遍历map判断升到几级了
        if (Integer.parseInt(uservip.getRechargeAmount().toString()) > dicVip.getEndExp()) {
            Collection<DicVip> values = PlayGameService.DIC_VIP_MAP.values();
            for (DicVip value : values) {
                if (uservip.getRechargeAmount().compareTo(new BigDecimal(value.getBeginExp())) > 0
                        && uservip.getRechargeAmount().compareTo(new BigDecimal(value.getEndExp())) < 0) {
                    uservip.setVipLevel(value.getLv());
                    break;
                }
            }
        }
        userVipService.updateUserVipInfo(uservip);
    }




}

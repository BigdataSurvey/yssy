package com.zywl.app.manager.service.manager;

import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.UserCapital;
import com.zywl.app.base.bean.UserVip;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.OrderUtil;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.cache.UserCapitalCacheService;
import com.zywl.app.defaultx.enmus.UserCapitalTypeEnum;
import com.zywl.app.defaultx.service.UserVipService;
import com.zywl.app.manager.context.MessageCodeContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@ServiceClass(code = MessageCodeContext.USER_VIP)
public class ManagerUserVipService extends BaseService {

    @Autowired
    private ManagerConfigService managerConfigService;
    @Autowired
    private UserCapitalCacheService userCapitalCacheService;
    @Autowired
    private UserVipService userVipService;


    /**
     * 新增经验
     */
    @Transactional
    @ServiceMethod(code = "011", description = "新增经验")
    public JSONObject addExper(long userId, BigDecimal rechargeAmount) {
        //vip等级
        UserVip uservip = userVipService.findRechargeAmountByUserId(userId);
        //充值金额（经验）
        uservip.getRechargeAmount().add( rechargeAmount);
        userVipService.addExper(uservip);
        return new JSONObject();
    }







}

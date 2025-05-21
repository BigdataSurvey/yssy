package com.zywl.app.manager.service.manager;

import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.enums.PushCode;
import com.live.app.ws.util.Push;
import com.zywl.app.base.bean.TradingRecord;
import com.zywl.app.base.bean.UserCapital;
import com.zywl.app.base.constant.TableNameConstant;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.OrderUtil;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.cache.UserCapitalCacheService;
import com.zywl.app.defaultx.enmus.LogCapitalTypeEnum;
import com.zywl.app.defaultx.enmus.TradingRecordTypeEnum;
import com.zywl.app.defaultx.enmus.UserCapitalTypeEnum;
import com.zywl.app.defaultx.service.UserCapitalService;
import com.zywl.app.defaultx.service.UserGiftRecordService;
import com.zywl.app.defaultx.service.UserGiftService;
import com.zywl.app.manager.context.MessageCodeContext;
import com.zywl.app.manager.socket.ManagerLhdSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;

@Service
@ServiceClass(code = MessageCodeContext.USER_GIFT_SERVER)
public class ManagerBuyGiftService extends BaseService {

    @Autowired
    private ManagerConfigService managerConfigService;
    @Autowired
    private UserCapitalService userCapitalService;
    @Autowired
    private UserCapitalCacheService userCapitalCacheService;
    @Autowired
    private ManagerSocketService managerSocketService;
    @Autowired
    private UserGiftService userGiftService;
    @Autowired
    private UserGiftRecordService userGiftRecordService;





    @Transactional
    @ServiceMethod(code = "011", description = "购买礼包")
    public JSONObject nhSettle( JSONObject data) throws Exception {
        checkNull(data);
        Set<String> set = data.keySet();
        LogCapitalTypeEnum roleGift = null;
        Long userId = null;
        Long number=null;
        userGiftService.betUpdateBalance(data);
        String orderNo = OrderUtil.getOrder5Number();


        for (String key : set) {
            JSONObject o = JSONObject.parse(data.getString(key));
            roleGift = LogCapitalTypeEnum.getEm(o.getIntValue("role_gift"));
            userId = Long.parseLong(key);
            //获取当前userid以及余额
            UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.currency_2.getValue());

            BigDecimal balance = userCapital.getBalance();
            BigDecimal price = new BigDecimal("499");
            int result = balance.compareTo(price);
           if (result < 0) {
                throw new Exception("余额不足");
            }
           //扣除金额
            userCapitalService.subUserGiftMoney(balance,userId,number,userCapital.getCapitalType(),roleGift,TableNameConstant.USER_GIFT_RECORD,price,orderNo);
            Long userGiftId = userGiftRecordService.addGiftRecord(userId,orderNo,userCapital.getCapitalType(),number,price);
            JSONObject pushData = new JSONObject();
            pushData.put("userId", userId);
            if (roleGift.getValue() == LogCapitalTypeEnum.bug_role_gift.getValue()) {
                pushData.put("isDts", 1);
            }
            pushData.put("capitalType", UserCapitalTypeEnum.currency_2.getValue());
            pushData.put("balance", userCapital.getBalance());
            Push.push(PushCode.updateUserCapital, managerSocketService.getServerIdByUserId(userId), pushData);
        }
        return new JSONObject();
    }




}

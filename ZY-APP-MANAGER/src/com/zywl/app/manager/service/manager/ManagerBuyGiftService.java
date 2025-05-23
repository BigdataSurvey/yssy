package com.zywl.app.manager.service.manager;

import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.Config;
import com.zywl.app.base.bean.UserCapital;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.OrderUtil;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.cache.UserCapitalCacheService;
import com.zywl.app.defaultx.enmus.UserCapitalTypeEnum;
import com.zywl.app.defaultx.service.UserCapitalService;
import com.zywl.app.defaultx.service.UserGiftRecordService;
import com.zywl.app.defaultx.service.UserGiftService;
import com.zywl.app.manager.context.MessageCodeContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@ServiceClass(code = MessageCodeContext.USER_GIFT_SERVER)
public class ManagerBuyGiftService extends BaseService {

    @Autowired
    private ManagerConfigService managerConfigService;
    @Autowired
    private ManagerGameBaseService managerGameBaseService;
    @Autowired
    private UserCapitalService userCapitalService;
    @Autowired
    private UserGiftService userGiftService;
    @Autowired
    private UserGiftRecordService userGiftRecordService;

    public BigDecimal getGiftPriceById(Long giftId){
        if (giftId == 1L){
            return  managerConfigService.getBigDecimal(Config.GIFT_PRICE_1);
        } else if (giftId==2L) {
            return  managerConfigService.getBigDecimal(Config.GIFT_PRICE_2);
        }else {
            throwExp("非法请求");
        }
        return null;
    }


    @Transactional
    @ServiceMethod(code = "011", description = "购买礼包")
    public JSONObject buy(JSONObject data) throws Exception {
        checkNull(data);
        checkNull(data.get("userId"),data.get("giftId"));
        //根据礼包ID获取礼包价格
        Long giftId = data.getLong("giftId");
        BigDecimal price = getGiftPriceById(giftId);
        //购买礼包的用户ID
        Long userId = data.getLong("userId");
        //礼包加数量之前先判断用户余额是否足够
        managerGameBaseService.checkBalance(userId,price,UserCapitalTypeEnum.currency_2);
        //余额充足 1.插入订单 2.扣钱  3.加礼包数量
        //1.插入订单
        String orderNo = OrderUtil.getOrder5Number();
        Long recordId = userGiftRecordService.addGiftRecord(userId, orderNo, UserCapitalTypeEnum.currency_2.getValue(), 1, price);
        //2.扣钱
        userCapitalService.subBalanceByGift(price,userId,orderNo,recordId);
        //3.礼包数+1
        userGiftService.addUserGiftNumber(userId);
        //推送用户余额变化
        managerGameBaseService.pushCapitalUpdate(userId,UserCapitalTypeEnum.currency_2.getValue());
        return new JSONObject();
    }
}

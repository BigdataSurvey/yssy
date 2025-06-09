package com.zywl.app.manager.service.manager;

import cn.hutool.core.date.DateTime;
import com.alibaba.fastjson2.JSONObject;
import com.ijpay.core.utils.DateTimeZoneUtil;
import com.zywl.app.base.bean.Config;
import com.zywl.app.base.bean.UserCapital;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.HTTPUtil;
import com.zywl.app.base.util.MD5Util;
import com.zywl.app.base.util.OrderUtil;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.cache.UserCapitalCacheService;
import com.zywl.app.defaultx.enmus.UserCapitalTypeEnum;
import com.zywl.app.defaultx.service.TsgPayOrderService;
import com.zywl.app.defaultx.service.UserCapitalService;
import com.zywl.app.defaultx.service.UserGiftRecordService;
import com.zywl.app.defaultx.service.UserGiftService;
import com.zywl.app.manager.context.MessageCodeContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

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
    private TsgPayOrderService tsgPayOrderService;
    @Autowired
    private UserGiftRecordService userGiftRecordService;

    @Autowired
    private ManagerUserVipService managerUserVipService;

    public static final String VERSION = "v1.0";
    public static final String TYPE = "10005";
    public static final String USER_ID = "88162050";

    public static final String SECRET = "e7a15a9d4e6946bb97edf329035297d1";

    public BigDecimal getGiftPriceById(int giftType) {
        if (giftType == 1) {
            return managerConfigService.getBigDecimal(Config.GIFT_PRICE_1_GAME);
        } else {
            return managerConfigService.getBigDecimal(Config.GIFT_PRICE_2_GAME);
        }
    }




    @Transactional
    @ServiceMethod(code = "011", description = "购买礼包")
    public JSONObject buy(JSONObject data) throws Exception {
        checkNull(data);
        checkNull(data.get("userId"), data.get("giftType"));
        //根据礼包ID获取礼包价格
        int giftType = data.getIntValue("giftType");
        BigDecimal price = getGiftPriceById( giftType);
        //购买礼包的用户ID
        Long userId = data.getLong("userId");
        //礼包加数量之前先判断用户余额是否足够
        managerGameBaseService.checkBalance(userId, price, UserCapitalTypeEnum.currency_2);
        //余额充足 1.插入订单 2.扣钱  3.加礼包数量
        //1.插入订单
        String orderNo = OrderUtil.getOrder5Number();
        Long recordId = userGiftRecordService.addGiftRecord(userId, orderNo, UserCapitalTypeEnum.currency_2.getValue(), 1, price);
        //2.扣钱
        userCapitalService.subBalanceByGift(price, userId, orderNo, recordId);
        managerUserVipService.addExper(userId, price);
        //3.礼包数+1
        userGiftService.addUserGiftNumber(userId, giftType);
        //推送用户余额变化
        managerGameBaseService.pushCapitalUpdate(userId, UserCapitalTypeEnum.currency_2.getValue());

        return new JSONObject();
    }


}

package com.zywl.app.manager.service.manager;

import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.Activity;
import com.zywl.app.base.bean.Config;
import com.zywl.app.base.bean.User;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.LockUtil;
import com.zywl.app.base.util.OrderUtil;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.cache.GameCacheService;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.enmus.ActivityAddPointEventEnum;
import com.zywl.app.defaultx.enmus.UserCapitalTypeEnum;
import com.zywl.app.defaultx.service.*;
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
    private TsgPayOrderService tsgPayOrderService;
    @Autowired
    private UserGiftRecordService userGiftRecordService;

    @Autowired
    private ManagerUserVipService managerUserVipService;

    @Autowired
    private ActivityService activityService;

    @Autowired
    private GameCacheService gameCacheService;

    @Autowired
    private UserCacheService userCacheService;



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
        synchronized (LockUtil.getlock(userId)){
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
            userGiftService.addUserGiftNumber(userId, giftType,1);
            //推送用户余额变化
            managerGameBaseService.pushCapitalUpdate(userId, UserCapitalTypeEnum.currency_2.getValue());
            return new JSONObject();
        }
    }

    public void addGift(String userNo, int number){
        User user = userCacheService.getUserInfoByUserNo(userNo);
        if (user==null){
            throwExp("用户不存在");
        }
        userGiftService.addUserGiftNumber(user.getId(), 2,number);
        managerUserVipService.addExper(user.getId(), BigDecimal.valueOf(number* 499L));
    }


}

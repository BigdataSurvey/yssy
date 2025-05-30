package com.zywl.app.defaultx.service;

import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.UserCapital;
import com.zywl.app.base.bean.UserGift;
import com.zywl.app.defaultx.cache.UserCapitalCacheService;
import com.zywl.app.defaultx.dbutil.DaoService;
import com.zywl.app.defaultx.enmus.LogCapitalTypeEnum;
import com.zywl.app.defaultx.enmus.UserCapitalTypeEnum;
import org.apache.commons.collections4.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
public class UserGiftService extends DaoService {

    @Autowired
    private UserCapitalCacheService userCapitalCacheService;


    public UserGiftService() {
        super("UserGiftMapper");
    }

    @Transactional
    public void addUserGiftNumber(Long userId, int giftType) {
        UserGift ug = findUserGift(userId, giftType);
        if (ug==null){
            UserGift userGift = new UserGift();
            userGift.setUserId(userId);
            userGift.setGiftNum(1);
            userGift.setGiftType(giftType);
            save(userGift);
        }else {
            Map<String, Object> params = new HashMap<>();
            params.put("userId", userId);
            params.put("giftType", giftType);
            int a = execute("addUserGiftNumber", params);
        }
    }


    public UserGift findUserGift(Long userId,int type) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("giftType",type);
        return (UserGift) findOne("findUserGift",params);
    }

    @Transactional
    public void useGift(Long userId,int type) {
        //使用或者赠送礼包，数量-1
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("giftType",type);
        int a = execute("useGift", params);
        if (a < 1) {
            throwExp("激活失败，请联系客服");
        }
    }




}

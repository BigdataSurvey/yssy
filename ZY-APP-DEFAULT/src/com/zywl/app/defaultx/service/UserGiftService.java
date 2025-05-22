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
    public void addUserGiftNumber(Long userId) {
        // 插入或者修改数据 第一次买就插入 否则数量+1
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        int a = execute("insertOrUpdate", params);
        if (a<1){
            throwExp("购买礼包失败，请联系客服");
        }
    }

    @Transactional
    public void useGift(Long userId){
        //使用或者赠送礼包，数量-1
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        int a = execute("useGift",params);
        if (a<1){
            throwExp("激活失败，请联系客服");
        }
    }


}

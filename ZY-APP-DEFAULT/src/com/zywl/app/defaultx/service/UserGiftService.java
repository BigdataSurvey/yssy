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


    public UserGiftService(String mapper) {
        super(mapper);
    }

    @Transactional
    public void betUpdateBalance(JSONObject obj) {
        Set<String> set = obj.keySet();

        for (String key : set) {
            Map<String, Object> map = new HashedMap<>();
            map.put("userId", key);
            UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(Long.parseLong(key), UserCapitalTypeEnum.currency_2.getValue());
            List<UserGift> byConditions = findByConditions(userCapital.getUserId());

            if(null != byConditions.get(0)){
                UserGift userGift = byConditions.get(0);
                map.put("giftNum", String.valueOf(userGift.getGiftNum().add(BigDecimal.valueOf(1))));
            }else {
                map.put("giftNum", String.valueOf(1));
            }
            map.put("userId",userCapital.getUserId());
            map.put("createTime",new Date());
            save(map);
        }

    }


}

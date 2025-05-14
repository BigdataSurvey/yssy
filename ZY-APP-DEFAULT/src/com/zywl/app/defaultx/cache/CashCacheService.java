package com.zywl.app.defaultx.cache;

import com.zywl.app.base.constant.RedisKeyConstant;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.defaultx.cache.impl.RedisService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;

@Service
public class CashCacheService extends RedisService {

    public String getTodayCashCount() {
        String key = RedisKeyConstant.APP_CASH_DAY_COUNT + DateUtil.format2(new Date());
        String str = get(key);

        if(StringUtils.isEmpty(str)) {
            str = "0";
        }
        return str;
    }

    public double sumTodayCash(Double amount) {
        String key = RedisKeyConstant.APP_CASH_DAY_COUNT + DateUtil.format2(new Date());
        return incrDouble(key,amount);
    }
}

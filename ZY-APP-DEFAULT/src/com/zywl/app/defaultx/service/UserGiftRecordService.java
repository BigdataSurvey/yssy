package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.TradingRecord;
import com.zywl.app.base.bean.UserGiftRecord;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;

@Service
public class UserGiftRecordService extends DaoService {

    public UserGiftRecordService() {
        super("UserGiftRecordMapper");
    }

    public Long addGiftRecord(Long userId, String orderNo,  int type, long number, BigDecimal price) {
        UserGiftRecord userGiftRecord = new UserGiftRecord();
        userGiftRecord.setUserId(userId);
        userGiftRecord.setOrderNo(orderNo);
        userGiftRecord.setNumber(number);
        userGiftRecord.setPrice(price);
        userGiftRecord.setType(type);
        userGiftRecord.setCreateTime(new Date());
        return userGiftRecord.getId();
    }


}

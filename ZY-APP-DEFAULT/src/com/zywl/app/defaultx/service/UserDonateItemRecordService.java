package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.UserDonateItemRecord;
import com.zywl.app.base.bean.UserGiftRecord;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;

@Service
public class UserDonateItemRecordService extends DaoService {
    public UserDonateItemRecordService() {
        super("UserDonateItemRecordMapper");
    }

    public Long addDonateItemRecord(long userId, String orderNo, int value, int number, BigDecimal balance) {
        UserDonateItemRecord userDonateItemRecord = new UserDonateItemRecord();
        userDonateItemRecord.setUserId(userId);
        userDonateItemRecord.setOrderNo(orderNo);
        userDonateItemRecord.setNumber(number);
        userDonateItemRecord.setPrice(balance);
        userDonateItemRecord.setType(value);
        userDonateItemRecord.setCreateTime(new Date());
        save(userDonateItemRecord);
        return userDonateItemRecord.getId();
    }


}

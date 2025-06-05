package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.UserDonateItemRecord;
import com.zywl.app.base.bean.VipReceiveRecord;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.apache.commons.collections4.map.HashedMap;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
@Service
public class VipReceiveRecordService extends DaoService {

    public VipReceiveRecordService() {
        super("VipReceiveRecordMapper");
    }


    public List<VipReceiveRecord> findVipReceiveRecordByLevel(Long userId, long vipLevel) {
        Map<String,Object> params = new HashedMap<>();
        params.put("userId", userId);
        params.put("vipLevel", vipLevel);
        return findByConditions(params);
    }

    public Long addVipReceiveRecord(long userId, String orderNo, int value, int number, BigDecimal balance) {
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

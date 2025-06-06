package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.UserDonateItemRecord;
import com.zywl.app.base.bean.VipReceiveRecord;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.apache.commons.collections4.map.HashedMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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


    @Transactional
    public long addVipReceiveRecord(Long userId, String orderNo, Long vipLevel, String reward, Date crteTime, Date upteTime) {
        VipReceiveRecord vipReceiveRecord = new VipReceiveRecord();
        vipReceiveRecord.setUserId(userId);
        vipReceiveRecord.setOrderNo(orderNo);
        vipReceiveRecord.setReward(reward);
        vipReceiveRecord.setVipLevel(vipLevel);
        vipReceiveRecord.setCreateTime(crteTime);
        vipReceiveRecord.setCreateTime(crteTime);
        save(vipReceiveRecord);
        return vipReceiveRecord.getId();
    }
}

package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.MzBuyOrder;
import com.zywl.app.base.bean.MzBuyRecord;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
public class MzBuyRecordService extends DaoService {

    public MzBuyRecordService() {
        super("MzBuyOrderMapper");
    }

    public List<MzBuyRecord> findAllRecord() {
        return findAll();
    }


    @Transactional
    public Long addRecord(Long userId, int type, Long sellId, Long tradId, BigDecimal fee,String orderNo,BigDecimal amount){
        MzBuyRecord record = new MzBuyRecord();
        record.setUserId(userId);
        record.setBuyType(type);
        record.setAmount(amount);
        record.setFee(fee);
        record.setOrderNo(orderNo);
        record.setTradId(tradId);
        record.setSellUserId(sellId);
        record.setCreateTime(new Date());
        save(record);
        return record.getId();
    }

    @Transactional
    public Long addShopBuyRecord(Long userId, int type,String orderNo,BigDecimal amount){
        MzBuyRecord record = new MzBuyRecord();
        record.setUserId(userId);
        record.setBuyType(type);
        record.setAmount(amount);
        record.setFee(BigDecimal.ZERO);
        record.setOrderNo(orderNo);
        record.setTradId(null);
        record.setSellUserId(null);
        record.setCreateTime(new Date());
        save(record);
        return record.getId();
    }
}

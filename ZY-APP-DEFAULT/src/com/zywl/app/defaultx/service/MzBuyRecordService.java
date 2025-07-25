package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.MzBuyRecord;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MzBuyRecordService extends DaoService {

    public MzBuyRecordService() {
        super("MzBuyRecordMapper");
    }

    public List<MzBuyRecord> findAllRecord() {
        return findAll();
    }


    @Transactional
    public Long addRecord(Long userId, int type, Long sellId, Long tradId, BigDecimal fee,String orderNo,BigDecimal amount,String name,int icon,Long mzItemId){
        MzBuyRecord record = new MzBuyRecord();
        record.setUserId(userId);
        record.setBuyType(type);
        record.setAmount(amount);
        record.setFee(fee);
        record.setOrderNo(orderNo);
        record.setTradId(tradId);
        record.setSellUserId(sellId);
        record.setCreateTime(new Date());
        record.setName(name);
        record.setMzItemId(mzItemId);
        record.setIcon(icon);
        save(record);
        return record.getId();
    }

    @Transactional
    public Long addShopBuyRecord(Long userId, int type,String orderNo,BigDecimal amount,boolean isWhite){
        MzBuyRecord record = new MzBuyRecord();
        record.setUserId(userId);
        record.setBuyType(type);
        record.setAmount(amount);
        record.setFee(BigDecimal.ZERO);
        record.setOrderNo(orderNo);
        record.setTradId(null);
        record.setSellUserId(null);
        record.setCreateTime(new Date());
        record.setIsWhite(isWhite?1:0);
        save(record);
        return record.getId();
    }

    public List<MzBuyRecord> findTodayByUserId(Long userId){
        Map<String,Object> map = new HashMap<>();
        map.put("userId",userId);
        map.put("time", DateUtil.getToDayDateBegin());
        return findList("findTodayByUserId",map);
    }

    public List<MzBuyRecord> findTodayByUserId2(Long userId,int isWhite){
        Map<String,Object> map = new HashMap<>();
        map.put("userId",userId);
        map.put("time", DateUtil.getToDayDateBegin());
        map.put("isWhite",isWhite);
        return findList("findTodayByUserId2",map);
    }

    public List<MzBuyRecord> finUserTradRecord(Long userId){
        Map<String,Object> map = new HashMap<>();
        map.put("userId",userId);
        return findList("finUserTradRecord",map);
    }

    public List<MzBuyRecord> findAllRecord2() {
        Map<String,Object> map = new HashMap<>();
        map.put("time","2025-07-19 14:27:00");
        return findList("findAllRecord2",map);
    }



}

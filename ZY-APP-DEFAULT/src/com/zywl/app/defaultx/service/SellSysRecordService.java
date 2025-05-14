package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.SellSysRecord;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
public class SellSysRecordService extends DaoService{
	
	
	public SellSysRecordService() {
		super("SellSysRecordMapper");
	}

	
	@Transactional
	public Long addRecord(Long userId,Long itemId, int number,BigDecimal totalAmount ,String orderNo) {
		SellSysRecord record = new SellSysRecord();
		record.setUserId(userId);
		record.setCreateTime(new Date());
		record.setItemId(itemId);
		record.setNumber(number);
		record.setOrderNo(orderNo);
		record.setTotalAmount(totalAmount);
		save(record);
		return record.getId();
	}
	
	

	public List<SellSysRecord> getAllRecord() {
		return findAll();
	}

}

package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.ExchangeRecord;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.apache.commons.collections4.map.HashedMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class ExchangeRecordService extends DaoService{
	
	
	public ExchangeRecordService() {
		super("ExchangeRecordMapper");
	}

	
	
	
	
	
	
	@Transactional
	public long addRecord(String orderNo,Long userId,Long productId,BigDecimal spentAmount) {
		ExchangeRecord record = new ExchangeRecord();
		record.setOrderNo(orderNo);
		record.setUserId(userId);
		record.setProductId(productId);
		record.setSpentAmount(spentAmount);
		record.setTrackNo(null);
		save(record);
		return record.getId();
	}
	
	
	
	@Transactional
	public void updateRecord(String orderNo,int status,String trackNo) {
		Map<String, Object> params = new HashedMap<String, Object>();
		params.put("orderNo", orderNo);
		params.put("status", status);
		params.put("trackNo", trackNo);
		execute("updateRecord", params);
	}

	public ExchangeRecord getRecordByOrderNo(String orderNo) {
		Map<String, Object> params = new HashedMap<String, Object>();
		params.put("orderNo", orderNo);
		return findOne(params);
	}

}

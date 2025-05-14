package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.ConvertIncomeRecord;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
public class ConvertIncomeRecordService extends DaoService{

	public ConvertIncomeRecordService( ) {
		super("ConvertIncomeRecordMapper");
	}

	@Transactional
	public Long addRecord(Long userId,String orderNo,BigDecimal amount,BigDecimal before,BigDecimal after) {
		ConvertIncomeRecord record = new ConvertIncomeRecord();
		record.setAfterIncome(after);
		record.setAmount(amount);
		record.setBeforeIncome(before);
		record.setCreateTime(new Date());
		record.setUserId(userId);
		record.setOrderNo(orderNo);
		save(record);
		return record.getId();
	}
	
	
	public List<ConvertIncomeRecord> findAllRecord(){
		return findAll();
	}
}

package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.CashChannelIncomeRecord;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
public class CashChannelIncomeRecordService extends DaoService{

	public CashChannelIncomeRecordService( ) {
		super("CashChannelIncomeRecordMapper");
	}


	@Transactional
	public Long addRecord(Long userId, BigDecimal amount,String orderNo){
		CashChannelIncomeRecord record = new CashChannelIncomeRecord();
		record.setAmount(amount);
		record.setCreateTime(new Date());
		record.setOrderNo(orderNo);
		record.setUserId(userId);
		save(record);
		return record.getId();
	}
	
	public List<CashChannelIncomeRecord> findAllRecord(){
		return findAll();
	}
}

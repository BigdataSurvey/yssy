package com.zywl.app.defaultx.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zywl.app.base.bean.PrizeDrawRecord;
import com.zywl.app.defaultx.dbutil.DaoService;

@Service
public class PrizeDrawRecordService extends DaoService{
	
	public PrizeDrawRecordService( ) {
		super("PrizeDrawRecordMapper");
	}

	@Transactional
	public Long addRecord(Long userId,String orderNo,String ids, String reward) {
		PrizeDrawRecord obj = new PrizeDrawRecord();
		obj.setUserId(userId);
		obj.setOrderNo(orderNo);
		obj.setCreateTime(new Date());
		obj.setDrawIds(ids);
		obj.setReward(reward);
		save(obj);
		return obj.getId();
	}
	
	
	public List<PrizeDrawRecord> findAllRecord(){
		return findAll();
	}
	
	
	public List<PrizeDrawRecord> findAllRecord(Long userId){
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("userId", userId);
		return findList("findByUserId", params);
	}
	
}

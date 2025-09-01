package com.zywl.app.defaultx.service;

import com.alibaba.fastjson2.JSONArray;
import com.zywl.app.base.bean.ConvertIncomeRecord;
import com.zywl.app.base.bean.HandBookRewardRecord;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HandBookRewardRecordService extends DaoService{

	public HandBookRewardRecordService( ) {
		super("HandBookRewardRecordMapper");
	}

	@Transactional
	public Long addRecord(Long userId, Long handbookId, int dayNum, JSONArray reward) {
		HandBookRewardRecord record = new HandBookRewardRecord();
		record.setCreateTime(new Date());
		record.setUserId(userId);
		record.setHandbookId(handbookId);
		record.setDayNum(dayNum);
		record.setReward(reward);
		save(record);
		return record.getId();
	}
	
	

	public List<HandBookRewardRecord> findByUserId(Long userId){
		Map<String,Object> map = new HashMap<>();
		map.put("userId",userId);
		return findList("findByUserId",map);
	}


	public HandBookRewardRecord findUserIdOneRecord(Long userId,Long handbookId){
		Map<String,Object> map = new HashMap<>();
		map.put("userId",userId);
		map.put("handbookId",handbookId);
		return (HandBookRewardRecord) findOne("findUserIdOneRecord",map);
	}
}

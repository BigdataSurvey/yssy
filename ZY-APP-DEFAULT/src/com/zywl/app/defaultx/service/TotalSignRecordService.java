package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.TotalSignRecord;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.apache.commons.collections4.map.HashedMap;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class TotalSignRecordService extends DaoService{
	
	
	
	public TotalSignRecordService() {
		super("TotalSignRecordMapper");
	}

	public Long addRecord(Long userId,int day,String reward,String orderNo) {
		TotalSignRecord record = new TotalSignRecord();
		record.setUserId(userId);
		record.setTotalDay(day);
		record.setOrderNo(orderNo);
		record.setReward(reward);
		record.setCreateTime(new Date());
		save(record);
		return record.getId();
	}
	
	
	
	public List<TotalSignRecord> getRecordById(Long userId) {
		Map<String, Object> params = new HashedMap<String, Object>();
		params.put("userId",userId);
		params.put("createTime",DateUtil.getFirstDayOfMonth());
		return  findList("findThisMonthRecord", params);
	}
	
}

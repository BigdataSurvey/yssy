package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.UserDailyTaskRecord;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserDailyTaskRecordService extends DaoService{
	
	

	public UserDailyTaskRecordService() {
		super("UserDailyTaskRecordMapper");
	}
	
	@Transactional
	public Long addRecord(Long userId,String taskId,String orderNo) {
		UserDailyTaskRecord record = new UserDailyTaskRecord();
		record.setUserId(userId);
		record.setOrderNo(orderNo);
		record.setTaskId(Long.parseLong(taskId));
		record.setCreateTime(new Date());
		save(record);
		return record.getId();
	}
	
	public List<UserDailyTaskRecord> findAllDailyTasRecord() {
		return findAll();
	}

	public UserDailyTaskRecord findByUserIdAndTaskId(Long userId,String taskId){
		Map<String,Object> params = new HashMap<>();
		params.put("userId",userId);
		params.put("taskId",taskId);
		params.put("createTime", DateUtil.getToDayDateBegin());
		return (UserDailyTaskRecord) findOne("findByUserIdAndTaskId",params);

	}

}

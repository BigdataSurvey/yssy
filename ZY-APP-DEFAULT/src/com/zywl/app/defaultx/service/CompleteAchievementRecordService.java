package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.CompleteAchievementRecord;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.apache.commons.collections4.map.HashedMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class CompleteAchievementRecordService extends DaoService{
	
	public CompleteAchievementRecordService() {
		super("CompleteAchievementRecordMapper");
	}

	@Transactional
	public Long addRecord(Long userId,Long achievementId,String reward,String orderNo) {
		CompleteAchievementRecord record = new CompleteAchievementRecord();
		record.setUserId(userId);
		record.setAchievementId(achievementId);
		record.setReward(reward);
		record.setOrderNo(orderNo);
		save(record);
		return record.getId();
	}
	
	
	public List<CompleteAchievementRecord> findByUserId(Long userId){
		Map<String, Object> params = new HashedMap<String, Object>();
		params.put("userId", userId);
		return findList("findByUserId", params);
	}


}

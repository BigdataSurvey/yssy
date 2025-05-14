package com.zywl.app.defaultx.service;

import com.sun.corba.se.spi.ior.ObjectKey;
import com.zywl.app.base.bean.UserReceiveInviteRecord;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserReceiveInviteRecordService extends DaoService{

	public UserReceiveInviteRecordService( ) {
		super("UserReceiveInviteRecordMapper");
	}

	@Transactional
	public UserReceiveInviteRecord addRecord(Long userId,int issue,Long rewardId,BigDecimal rewardAmount) {
		UserReceiveInviteRecord record = new UserReceiveInviteRecord();
		record.setUserId(userId);
		record.setCreateTime(new Date());
		record.setIssue(issue);
		record.setRewardAmount(rewardAmount);
		record.setRewardId(rewardId);
		save(record);
		return record;
	}
	
	
	public List<UserReceiveInviteRecord> findAllRecord(){
		return findAll();
	}

	public List<UserReceiveInviteRecord> findByUserId(Long userId,int issue) {
		Map<String, Object> params = new HashMap<>();
		params.put("userId",userId);
		params.put("issue",issue);
		return findList("findByUserId",params);

	}

	public UserReceiveInviteRecord findByUserId(Long userId,int issue,Long rewardId) {
		Map<String, Object> params = new HashMap<>();
		params.put("userId",userId);
		params.put("issue",issue);
		params.put("rewardId",rewardId);
		return (UserReceiveInviteRecord) findOne("findByUserIdAndRewardId",params);

	}
}

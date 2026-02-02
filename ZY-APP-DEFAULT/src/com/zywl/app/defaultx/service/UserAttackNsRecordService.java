package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.UserAttackNsRecord;
import com.zywl.app.base.bean.UserNsPrize;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.logging.Log;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class UserAttackNsRecordService extends DaoService {

	public UserAttackNsRecordService() {
		super("UserAttackNsRecordMapper");
	}

	@Transactional
	public UserAttackNsRecord addRecord(Long userId, Long nsId, int round, BigDecimal amount,String orderNo){
		UserAttackNsRecord record = new UserAttackNsRecord();
		record.setUserId(userId);
		record.setAmount(amount);
		record.setCreateTime(new Date());
		record.setRound(round);
		record.setNsId(nsId);
		record.setOrderNo(orderNo);
		insert(record);
		return record;
	}


	public List<UserAttackNsRecord> findRecord(Long userId, int round) {
		Map<String, Object> params = new HashedMap<>();
		params.put("id", userId);
		params.put("round", round);
		return findList("findRecord",params);
	}

	@Transactional
	public void updateGameStatus(int gameId,int status){
		Map<String, Object> params = new HashedMap<String, Object>();
		params.put("id", gameId);
		params.put("status",status);
		execute("updateGameStatus",params);
	}
	

	@Override
	protected Log logger() {
		return logger;
	}
	
	
}

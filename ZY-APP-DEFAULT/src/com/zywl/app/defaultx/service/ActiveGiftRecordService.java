package com.zywl.app.defaultx.service;

import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.ActiveGiftRecord;
import com.zywl.app.base.bean.BattleRoyaleRecord;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
public class ActiveGiftRecordService extends DaoService {

	public ActiveGiftRecordService() {
		super("ActiveGiftRecordMapper");
		// TODO Auto-generated constructor stub
	}


	private static final Log logger = LogFactory.getLog(ActiveGiftRecordService.class);
	
	
	
	@Transactional
	public Long addRecord(Long operatorId,Long userId,int type) {
		ActiveGiftRecord record = new ActiveGiftRecord();
		record.setOperatorId(operatorId);
		record.setUserId(userId);
		record.setGiftType(type);
		record.setCreateTime(new Date());
		save(record);
		return record.getId();
	}

	public List<ActiveGiftRecord> findByUserId(Long userId,int type){
		Map<String,Object> params = new HashMap<>();
		params.put("userId",userId);
		params.put("time",DateUtil.getDateByDay(-30));
		params.put("type",type);
		List<ActiveGiftRecord> findByUserId = findList("findByUserId", params);
		return findByUserId;
	}
	
	
	

	
}

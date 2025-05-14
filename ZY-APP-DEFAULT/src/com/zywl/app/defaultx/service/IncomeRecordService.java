package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.IncomeRecord;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.defaultx.cache.AppConfigCacheService;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class IncomeRecordService extends DaoService {

	private static final Log logger = LogFactory.getLog(IncomeRecordService.class);

	@Autowired
	private AppConfigCacheService appConfigCacheService;

	@Autowired
	private LogUserCapitalService logUserCapitalService;



	public IncomeRecordService() {
		super("IncomeRecordMapper");
	}

	

	@Transactional
	public void addIncomeRecord(BigDecimal amount, Long contributionUserId, int type, Long userId) {
		IncomeRecord incomeRecord = new IncomeRecord();
		incomeRecord.setAmount(amount);
		incomeRecord.setContributionUserId(contributionUserId);
		incomeRecord.setType(type);
		incomeRecord.setUserId(userId);
		int a = save(incomeRecord);
		if (a < 1) {
			throwExp("插入收益记录失败-userId：" + userId + "好友id：" + contributionUserId + "金额：" + amount);
		}
	}

	@Transactional
	public int receivedIncomeByType(String orderNo, int type, Long userId) {
		Map<String, Object> params = new HashedMap<String, Object>();
		params.put("orderNo", orderNo);
		params.put("userId", userId);
		params.put("type", type);
		return execute("receivedIncomeByType", params);
	}

	
	public String getTodayIncom(Long myId,Long sonId) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("myId", myId);
		params.put("sonId", sonId);
		params.put("today", DateUtil.getToDayBegin());
		return (String) findOne("getTodayIncom", params);
	}
	
	

	public String getAllIncom(Long myId,Long sonId) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("myId", myId);
		params.put("sonId", sonId);
		return (String) findOne("getAllIncom", params);
	}
	
	public List<Map<String, Object>> getUserUnReceivedIncom() {
		return findList("getUserUnReceivedIncom", null);
	}


}

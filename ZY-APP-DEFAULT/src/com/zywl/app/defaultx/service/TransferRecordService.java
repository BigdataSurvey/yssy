package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.TransferRecord;
import com.zywl.app.defaultx.cache.AppConfigCacheService;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TransferRecordService extends DaoService{
	
	private static final Log logger = LogFactory.getLog(TransferRecordService.class);
	
	
	@Autowired
	private AppConfigCacheService appConfigCacheService;
	
	@Autowired
	private UserCacheService userCacheService;
	
	public TransferRecordService() {
		super("TransferRecordMapper");
	}

	//添加用户货币转增记录
	@Transactional
	public Long addTransferRecord(String  orderNo,Long fromUserId,Long toUserId,BigDecimal amount) {
		TransferRecord transferRecord = new TransferRecord();
		transferRecord.setOrderNo(orderNo);
		transferRecord.setFromUserId(fromUserId);
		transferRecord.setToUserId(toUserId);
		transferRecord.setAmount(amount);
		transferRecord.setMark("邮件赠送货币");
		transferRecord.setCreateTime(new Date());
		transferRecord.setUpdateTime(new Date());
		save(transferRecord);
		return transferRecord.getId();
	}
	
	
	public TransferRecord getRecordByOrderNo(String orderNo) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("orderNo", orderNo);
		return findOne(params);
	}
	
	public List<TransferRecord> getRecordsByUserId(Long userId){
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("userId", userId);
		return findList("findByUserId", params);
	}
}

package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.Game;
import com.zywl.app.base.bean.OpenBoxRecord;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class OpenBoxRecordService extends DaoService {

	public OpenBoxRecordService() {
		super("OpenBoxRecordMapper");
		// TODO Auto-generated constructor stub
	}


	private static final Log logger = LogFactory.getLog(OpenBoxRecordService.class);
	
	
	
	@Transactional
	public Long addRecord(Long userId, BigDecimal amount,Long prizeId,String orderNo){
		OpenBoxRecord record = new OpenBoxRecord();
		record.setCreateTime(new Date());
		record.setAmount(amount);
		record.setPrizeId(prizeId);
		record.setOrderNo(orderNo);
		insert(record);
		return record.getId();
	}
	
	
	

	


	
}

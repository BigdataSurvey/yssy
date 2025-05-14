package com.zywl.app.defaultx.service.card;

import com.zywl.app.base.bean.card.UserOpenMineRecord;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;

@Service
public class UserOpenMineRecordService extends DaoService {

	public UserOpenMineRecordService() {
		super("UserOpenMineRecordMapper");
		// TODO Auto-generated constructor stub
	}


	private static final Log logger = LogFactory.getLog(UserOpenMineRecordService.class);

	
	@Transactional
	public Long addRecord(Long userId, String orderNo, BigDecimal amount){
		UserOpenMineRecord userOpenMineRecord = new UserOpenMineRecord();
		userOpenMineRecord.setUserId(userId);
		userOpenMineRecord.setOrderNo(orderNo);
		userOpenMineRecord.setAmount(amount);
		userOpenMineRecord.setCreateTime(new Date());
		insert(userOpenMineRecord);
		return userOpenMineRecord.getId();
	}



	@Override
	protected Log logger() {
		return logger;
	}
	
	
}

package com.zywl.app.defaultx.service.card;

import com.zywl.app.base.bean.card.BuyVipRecord;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;

@Service
public class BuyVipRecordService extends DaoService{

	public BuyVipRecordService( ) {
		super("BuyVipRecordMapper");
	}

	@Transactional
	public Long addRecord(Long userId, String orderNo, int vipType, BigDecimal price) {

		BuyVipRecord buyVipRecord = new BuyVipRecord();
		buyVipRecord.setCreateTime(new Date());
		buyVipRecord.setVipType(vipType);
		buyVipRecord.setOrderNo(orderNo);
		buyVipRecord.setUserId(userId);
		buyVipRecord.setAmount(price);
		save(buyVipRecord);
		return buyVipRecord.getId();
	}


}

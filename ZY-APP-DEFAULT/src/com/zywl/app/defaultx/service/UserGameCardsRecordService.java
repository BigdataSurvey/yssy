package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.card.UserGameCardsRecord;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;

@Service
public class UserGameCardsRecordService extends DaoService{

	public UserGameCardsRecordService( ) {
		super("UserGameCardsRecordMapper");
	}

	@Transactional
	public Long addRecord(Long gameId,Long userId,int index,int result,BigDecimal amount,String orderNo) {
		UserGameCardsRecord obj = new UserGameCardsRecord();
		obj.setUserId(userId);
		obj.setGameId(gameId);
		obj.setCardIndex(index);
		obj.setAmount(amount);
		obj.setResult(result);
		obj.setOrderNo(orderNo);
		obj.setCreateTime(new Date());
		save(obj);
		return obj.getId();
	}
	
	

}

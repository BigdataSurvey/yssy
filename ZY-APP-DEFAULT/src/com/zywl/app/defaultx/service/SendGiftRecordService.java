package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.SendGiftRecord;
import com.zywl.app.base.bean.ShoppingRecord;
import com.zywl.app.base.bean.vo.SendGiftRecordVo;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SendGiftRecordService extends DaoService{

	public SendGiftRecordService( ) {
		super("SendGiftRecordMapper");
	}

	@Transactional
	public Long addRecord(Long userid,String toUserNo,Long toUserId,int giftType,int number) {
		SendGiftRecord record= new SendGiftRecord();
		record.setCreateTime(new Date());
		record.setNumber(number);
		record.setGiftType(giftType);
		record.setToUserId(toUserId);
		record.setToUserNo(toUserNo);
		record.setUserId(userid);
		save(record);
		return record.getId();
	}
	
	

	public List<SendGiftRecord> findByUserId(Long userId,int start, int limit){
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("userId", userId);
		params.put("start", start*limit);
		params.put("limit", limit);
		return findList("findByUserId", params);
	}


	public List<SendGiftRecord> findByUserIdAndSendNo(Long userId,String toUserNo,int start, int limit){
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("userId", userId);
		params.put("start", start*limit);
		params.put("limit", limit);
		params.put("toUserNo",toUserNo);
		return findList("findByUserIdAndSendNo", params);
	}

	public List<SendGiftRecordVo> findByToUserId(Long userId, int start, int limit){
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("userId", userId);
		params.put("start", start*limit);
		params.put("limit", limit);
		return findList("findByToUserId", params);
	}
}

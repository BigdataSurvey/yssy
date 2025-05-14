package com.zywl.app.defaultx.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zywl.app.base.bean.ShoppingRecord;
import com.zywl.app.defaultx.dbutil.DaoService;

@Service
public class ShoppingRecordService extends DaoService{
	
	public ShoppingRecordService( ) {
		super("ShoppingRecordMapper");
	}

	@Transactional
	public Long addRecord(Long userid,Long itemId,int number,String orderNo,BigDecimal amount,int shopType) {
		ShoppingRecord record= new ShoppingRecord();
		record.setAmount(amount);
		record.setShopType(shopType);
		record.setItemId(itemId);
		record.setNumber(number);
		record.setOrderNo(orderNo);
		record.setUserId(userid);
		record.setCreateTime(new Date());
		save(record);
		return record.getId();
	}
	
	
	public List<ShoppingRecord> findAllShoppingRecord(){
		return findAll();
	}
	
	public List<ShoppingRecord> findByUserId(Long userId){
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("userId", userId);
		return findList("findByUserId", params);
	}
	
	public List<ShoppingRecord> findByItemId(Long itemId){
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("itemId", itemId);
		return findList("findByItemId", params);
	}
}

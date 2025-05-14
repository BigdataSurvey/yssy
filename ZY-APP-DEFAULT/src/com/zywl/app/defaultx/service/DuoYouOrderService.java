package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.DuoYouOrder;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DuoYouOrderService extends DaoService{

	public DuoYouOrderService( ) {
		super("DuoYouOrderMapper");
	}

	@Transactional
	public Long addOrder(Long userId,String orderId,String advertName,String advertId,String created,String mediaIncome,
						 String memberIncome,String mediaId,String deviceId,String content) {
		DuoYouOrder order = new DuoYouOrder();
		order.setOrderId(orderId);
		order.setAdvertId(advertId);
		order.setAdvertName(advertName);
		order.setContent(content);
		order.setUserId(userId);
		order.setMediaId(mediaId);
		order.setMemberIncome(memberIncome);
		order.setCreated(created);
		order.setMediaIncome(mediaIncome);
		order.setDeviceId(deviceId);
		save(order);
		return order.getId();
	}
	
	public DuoYouOrder getOrderByID(String orderId){
		Map<String,Object> params = new HashMap<>();
		params.put("orderId",orderId);
		return (DuoYouOrder) findOne("getOrderByID",params);
	}
	public List<DuoYouOrder> findAllDuoYouOrder(){
		return findAll();
	}
}

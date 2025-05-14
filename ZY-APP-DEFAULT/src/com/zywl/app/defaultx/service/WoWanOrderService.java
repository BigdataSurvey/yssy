package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.WoWanOrder;
import com.zywl.app.base.bean.XianWanOrder;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class WoWanOrderService extends DaoService{

	public WoWanOrderService( ) {
		super("WoWanOrderMapper");
	}

	@Transactional
	public Long addOrder(WoWanOrder order) {
		 save(order);
		return order.getId();
	}

	public WoWanOrder getOrderByID(int orderId){
		Map<String,Object> params = new HashMap<>();
		params.put("orderId",orderId);
		return (WoWanOrder) findOne("getOrderByID",params);
	}
	
}

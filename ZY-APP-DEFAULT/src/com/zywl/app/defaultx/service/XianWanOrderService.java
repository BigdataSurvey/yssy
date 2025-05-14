package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.DuoYouOrder;
import com.zywl.app.base.bean.XianWanOrder;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class XianWanOrderService extends DaoService{

	public XianWanOrderService( ) {
		super("XianWanOrderMapper");
	}

	@Transactional
	public Long addOrder(XianWanOrder order) {
		 save(order);
		return order.getId();
	}

	public XianWanOrder getOrderByID(String orderId){
		Map<String,Object> params = new HashMap<>();
		params.put("orderId",orderId);
		return (XianWanOrder) findOne("getOrderByID",params);
	}
	
}

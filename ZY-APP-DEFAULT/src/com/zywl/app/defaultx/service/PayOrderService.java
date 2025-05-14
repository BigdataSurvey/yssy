package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.PayOrder;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.apache.commons.collections4.map.HashedMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class PayOrderService extends DaoService{



	public PayOrderService() {
		super("PayOrderMapper");
	}

	public PayOrder addOrder(Long userId,String orderNo,Long productId,int channelId,int price) {
		PayOrder order = new PayOrder();
		order.setOrderNo(orderNo);
		order.setChannelId(channelId);
		order.setCreateTime(new Date());
		order.setUpdateTime(new Date());
		order.setProductId(productId);
		order.setUserId(userId);
		order.setPrice(price);
		order.setStatus(0);
		save(order);
		return order;
	}


	@Transactional
	public void paySuccess(String orderNo){
		updateStatus(1,orderNo);
	}
	@Transactional
	public void payFail(String orderNo){
		updateStatus(2,orderNo);
	}

	@Transactional
	public void orderExpire(String orderNo){
		updateStatus(3,orderNo);
	}
	
	@Transactional
	private int updateStatus(int status,String orderNo) {
		Map<String,Object> params = new HashMap<>();
		params.put("status",status);
		params.put("orderNo",orderNo);
		return execute("updateStatus",params);

	}
	
	public PayOrder findOrderByOrderNo(String orderNo) {
		Map<String, Object> params = new HashedMap<String, Object>();
		params.put("orderNo",orderNo);
		return (PayOrder) findOne("findByOrderNo",params);
	}

	public PayOrder findOrderByOrderNoAndUserId(String orderNo,String userId) {
		Map<String, Object> params = new HashedMap<String, Object>();
		params.put("orderNo",orderNo);
		params.put("userId",userId);
		return (PayOrder) findOne("findByOrderNoAndUserId",params);
	}

	public PayOrder findByUserId(Long userId){
		Map<String, Object> params = new HashedMap<String, Object>();
		params.put("userId",userId);
		return (PayOrder) findOne("findByUserId",params);
	}

	public PayOrder findByUserIdAndPrice(Long userId,Integer price){
		Map<String, Object> params = new HashedMap<String, Object>();
		params.put("userId",userId);
		params.put("price",price);
		return (PayOrder) findOne("findByUserIdAndPrice",params);
	}
	
}

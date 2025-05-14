package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.IOSOrder;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
public class IOSOrderService extends DaoService{

	public IOSOrderService( ) {
		super("IOSOrderMapper");
	}


	@Transactional
	public Long insertOrder(Long productId, String orderNo, String receiptData, String inApp, BigDecimal price,Long userId){
		IOSOrder iosOrder = new IOSOrder();
		iosOrder.setOrderNo(orderNo);
		iosOrder.setCreateTime(new Date());
		iosOrder.setInApp(inApp);
		iosOrder.setUserId(userId);
		iosOrder.setPrice(price);
		iosOrder.setReceiptData(receiptData);
		iosOrder.setProductId(productId);
		insert(iosOrder);
		return iosOrder.getId();
	}
	
	public List<IOSOrder> findAllIOSOrder(){
		return findAll();
	}
}

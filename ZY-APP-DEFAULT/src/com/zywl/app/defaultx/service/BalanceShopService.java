package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.BalanceShop;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.apache.commons.collections4.map.HashedMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class BalanceShopService extends DaoService{
	
	
	public BalanceShopService() {
		super("BalanceShopMapper");
	}

	
	
	
	
	
	
	@Transactional
	public void addProduct(String context,String image,String name,int type,int status,BigDecimal price) {
		BalanceShop shop = new BalanceShop();
		shop.setContext(context);
		shop.setImage(image);
		shop.setName(name);
		shop.setPrice(price);
		shop.setType(type);
		shop.setStatus(status);
		save(shop);
	}
	
	
	public BalanceShop findById(Long id) {
		Map<String, Object> params = new HashedMap<String, Object>();
		params.put("id", id);
		return findOne(params);
	}
	
	
}

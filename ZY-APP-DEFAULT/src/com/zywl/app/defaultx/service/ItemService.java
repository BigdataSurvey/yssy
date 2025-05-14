package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.Item;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.apache.commons.collections4.map.HashedMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

@Service
public class ItemService extends DaoService{
	
	
	
	public ItemService() {
		super("ItemMapper");
	}

	public void addItem(int currencyType,String description,int durationDays,String icon,int isDuration,
			int isOverlap,int isSell,int isTrading,String itemName,int number,int positon,BigDecimal price
			,int quality,int type) {
		Item item = new Item();
		item.setCurrencyType(currencyType);
		item.setContext(description);
		item.setDurationDays(durationDays);
		item.setIcon(icon);
		item.setIsDuration(isDuration);
		item.setIsOverlap(isOverlap);
		item.setIsSell(isSell);
		item.setIsTrading(isTrading);
		item.setName(itemName);
		item.setNumber(number);
		item.setPositon(positon);
		item.setPrice(price);
		item.setQuality(quality);
		item.setType(type);
		item.setCreateTime(new Date());
		item.setUpdateTime(new Date());
		save(item);
	}
	
	
	@Transactional
	public int updateItem(Item item) {
		return update(item);
	}
	
	public Item getItemById(Long itemId) {
		Map<String, Object> params = new HashedMap<String, Object>();
		params.put("id",itemId);
		return findOne(params);
	}
	
}

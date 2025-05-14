package com.zywl.app.defaultx.cache;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zywl.app.base.bean.Item;
import com.zywl.app.base.bean.vo.ItemVo;
import com.zywl.app.base.constant.RedisKeyConstant;
import com.zywl.app.base.util.BeanUtils;
import com.zywl.app.defaultx.cache.impl.RedisService;
import com.zywl.app.defaultx.service.ItemService;


@Service
public class ItemCacheService extends RedisService{
	
	@Autowired
	private ItemService itemService;
	
	private static final Log logger = LogFactory.getLog(ItemCacheService.class);
	
	public Item getItemInfoById(Long itemId){
		String key = RedisKeyConstant.APP_ITEM_INFO+itemId+"-";
		Item item = get(key,Item.class);
		if (item==null) {
			item = itemService.getItemById(itemId);
			if (item!=null) {
				set(key, item, 6000L);
			}
		}
		return item;
	}
	
	public List<ItemVo> getAllItem(){
		String key = RedisKeyConstant.APP_ALL_ITEM_INFO;
		List<Item> items = getList(key,Item.class);
		if (items==null || items.size()==0) {
			items = itemService.findAll();
			if (items!=null) {
				set(key, items,6000L);
			}
		}
		List<ItemVo> vos = new ArrayList<ItemVo>();
		for (Item item : items) {
			ItemVo vo = new ItemVo();
			BeanUtils.copy(item, vo);
			vos.add(vo);
		}
		return vos;
	}
	
	public void removeItemCache(Long itemId) {
		String key = RedisKeyConstant.APP_ITEM_INFO+itemId+"-";
		del(key);
	}
	
	public void removeAllItemCache() {
		String key = RedisKeyConstant.APP_ITEM_INFO;
		deleteByLikeKey(key+"*");
	}

	
	

}

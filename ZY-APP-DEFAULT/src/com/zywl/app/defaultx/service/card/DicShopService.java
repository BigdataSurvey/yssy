package com.zywl.app.defaultx.service.card;

import com.zywl.app.base.bean.card.DicShop;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DicShopService extends DaoService {

	public DicShopService() {
		super("DicShopMapper");
		// TODO Auto-generated constructor stub
	}


	private static final Log logger = LogFactory.getLog(DicShopService.class);

	
	public List<DicShop> findAllShop() {
		return findAll();
	}

	public List<DicShop> findByShopType(int shopType) {
		Map<String,Object> map = new HashMap<>();
		map.put("shopType",shopType);
		return findList("findByShopType",map);
	}




	@Override
	protected Log logger() {
		return logger;
	}
	
	
}

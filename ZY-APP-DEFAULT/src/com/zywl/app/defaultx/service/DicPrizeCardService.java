package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.hongbao.DicPrizeCard;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DicPrizeCardService extends DaoService {

	public DicPrizeCardService() {
		super("DicPrizeCardMapper");
		// TODO Auto-generated constructor stub
	}



	private static final Log logger = LogFactory.getLog(DicPrizeCardService.class);

	
	public List<DicPrizeCard> findAllPrize() {
		return findAll();
	}

	@Override
	protected Log logger() {
		return logger;
	}

	public void updatePrizeTotal(Long id) {
		Map<String,Object> map = new HashMap<>();
		map.put("id",id);
		execute("updatePrizeTotal",map);
	}

	public DicPrizeCard findByUserId(Long userId) {
		Map<String, Object> params = new HashMap<>();
		params.put("userId", userId);
		return (DicPrizeCard) findOne("findByUserId", params);
	}

	public void initPrize(){
		execute("initPrize",null);
	}
}

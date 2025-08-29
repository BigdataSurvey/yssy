package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.hongbao.DicPrize;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DicPrizeService extends DaoService {

	public DicPrizeService() {
		super("DicPrizeMapper");
		// TODO Auto-generated constructor stub
	}



	private static final Log logger = LogFactory.getLog(DicPrizeService.class);

	
	public List<DicPrize> findAllPrize() {
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

	public DicPrize findByUserId(Long userId) {
		Map<String, Object> params = new HashMap<>();
		params.put("userId", userId);
		return (DicPrize) findOne("findByUserId", params);
	}
}

package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.DicPirzeDraw;
import com.zywl.app.base.bean.card.DicShop;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DicPrizeDrawService extends DaoService {

	public DicPrizeDrawService() {
		super("DicPrizeDrawMapper");
		// TODO Auto-generated constructor stub
	}


	private static final Log logger = LogFactory.getLog(DicPrizeDrawService.class);

	
	public List<DicPirzeDraw> findAllPrizeDraw() {
		return findAll();
	}





	@Override
	protected Log logger() {
		return logger;
	}
	
	
}

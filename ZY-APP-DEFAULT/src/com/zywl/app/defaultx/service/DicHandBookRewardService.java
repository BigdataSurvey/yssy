package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.DicHandBook;
import com.zywl.app.base.bean.DicHandBookReward;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DicHandBookRewardService extends DaoService {

	public DicHandBookRewardService() {
		super("DicHandBookRewardMapper");
		// TODO Auto-generated constructor stub
	}


	private static final Log logger = LogFactory.getLog(DicHandBookRewardService.class);

	
	public List<DicHandBookReward> findAllHandBookReward() {
		return findAll();
	}





	@Override
	protected Log logger() {
		return logger;
	}
	
	
}

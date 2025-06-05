package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.DicVip;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DicVipService extends DaoService {

	public DicVipService() {
		super("DicVipMapper");
	}


	private static final Log logger = LogFactory.getLog(DicVipService.class);

	
	public List<DicVip> findAllVip() {
		return findAll();
	}



	@Override
	protected Log logger() {
		return logger;
	}
	
	
}

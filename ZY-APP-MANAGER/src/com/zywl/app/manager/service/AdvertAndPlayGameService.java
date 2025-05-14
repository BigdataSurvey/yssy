package com.zywl.app.manager.service;

import com.zywl.app.base.service.BaseService;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.service.IncomeRecordService;
import com.zywl.app.manager.service.manager.ManagerCapitalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 广告和试玩
 * @author 1
 *
 */
@Service
public class AdvertAndPlayGameService extends BaseService{
	
	
	@Autowired
	private UserCacheService userCacheService;
	
	
	
	@Autowired
	private IncomeRecordService incomeRecordService;
	
	@Autowired
	private ManagerCapitalService managerCapitalService;
	
	



}

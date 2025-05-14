package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.ChannelIncome;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ChannelIncomeService extends DaoService{

	public ChannelIncomeService( ) {
		super("ChannelIncomeMapper");
	}


	
	public List<ChannelIncome> findAllChannelIncome(){
		return findAll();
	}
}

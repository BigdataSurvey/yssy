package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.GiveGrandfaIncome;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GiveGrandfaIncomeService extends DaoService {



	public GiveGrandfaIncomeService( ) {
		super("GiveGrandfaIncomeMapper");
	}

	@Transactional
	public void insertIncome(){

	}

	@Transactional
	public  int updateIncome(GiveGrandfaIncome income){
		return execute("update",income);
	}

	public List<GiveGrandfaIncome> findAllIncome(){
		return findAll();
	}
	

}

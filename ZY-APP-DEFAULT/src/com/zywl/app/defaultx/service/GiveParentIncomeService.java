package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.GiveParentIncome;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GiveParentIncomeService extends DaoService {



	public GiveParentIncomeService( ) {
		super("GiveParentIncomeMapper");
	}

	@Transactional
	public void insertIncome(){

	}

	@Transactional
	public  int updateIncome(GiveParentIncome income){
		return execute("update",income);
	}

	public List<GiveParentIncome> findAllIncome(){
		return findAll();
	}
	

}

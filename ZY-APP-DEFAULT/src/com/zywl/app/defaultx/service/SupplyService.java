package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.Supply;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SupplyService extends DaoService{

	public SupplyService( ) {
		super("SupplyMapper");
	}


	
	public List<Supply> findAllSupply(){
		return findAll();
	}
}

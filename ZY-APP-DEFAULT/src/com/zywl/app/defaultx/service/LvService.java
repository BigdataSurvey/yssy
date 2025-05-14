package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.Lv;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LvService extends DaoService{
	
	public LvService( ) {
		super("LvMapper");
	}


	
	public List<Lv> findAllLv(){
		return findAll();
	}
}

package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.Lv;
import com.zywl.app.base.bean.Ns;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NsService extends DaoService{

	public NsService( ) {
		super("NsMapper");
	}


	
	public List<Ns> findAllNs(){
		return findAll();
	}
}

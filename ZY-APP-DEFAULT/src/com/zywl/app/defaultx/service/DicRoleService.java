package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.DicRole;
import com.zywl.app.base.bean.card.DicShop;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DicRoleService extends DaoService {

	public DicRoleService() {
		super("DicRoleMapper");
		// TODO Auto-generated constructor stub
	}


	private static final Log logger = LogFactory.getLog(DicRoleService.class);

	
	public List<DicRole> findAllRole() {
		return findAll();
	}

	public DicRole findById(Long id) {
		Map<String,Object> map = new HashMap<>();
		map.put("id",id);
		return (DicRole) findOne("findById",map);
	}




	@Override
	protected Log logger() {
		return logger;
	}
	
	
}

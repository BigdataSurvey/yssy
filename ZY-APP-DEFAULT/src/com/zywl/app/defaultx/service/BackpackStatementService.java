package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.BackpackStatement;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class BackpackStatementService extends DaoService{

	public BackpackStatementService( ) {
		super("BackpackStatementMapper");
	}

	@Transactional
	public void addStatement(Long itemId) {
		BackpackStatement obj = new BackpackStatement();
		obj.setYmd(DateUtil.format9(DateUtil.getDateByM(60 * 10)));
		obj.setItemId(itemId);
		save(obj);
	}

	@Transactional
	public void addTodayStatement(Long itemId) {
		BackpackStatement obj = new BackpackStatement();
		obj.setYmd(DateUtil.format9(new Date()));
		obj.setItemId(itemId);
		save(obj);
	}

	
	
	public List<BackpackStatement> findAllBackpackStatement(){
		return findAll();
	}


	@Transactional
	public int updateStatement(Map<String,Object> params){
		return execute("update",params);
	}
}

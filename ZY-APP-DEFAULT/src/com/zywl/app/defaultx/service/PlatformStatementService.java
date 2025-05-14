package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.PlatformStatement;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PlatformStatementService extends DaoService{

	public PlatformStatementService( ) {
		super("PlatformStatementMapper");
	}

	@Transactional
	public void addStatement() {
		PlatformStatement obj = new PlatformStatement();
		obj.setYmd(DateUtil.format9(DateUtil.getDateByM(60 * 10)));
		save(obj);
	}

	@Transactional
	public void addTodayStatement() {
		PlatformStatement obj = new PlatformStatement();
		obj.setYmd(DateUtil.format9(new Date()));
		save(obj);
	}

	
	
	public List<PlatformStatement> findAllPlatformStatement(){
		return findAll();
	}

	public PlatformStatement findByYmd(String ymd){
		Map<String,Object> params = new HashMap<>();
		params.put("ymd",ymd);
		return (PlatformStatement) findOne("findByYmd",params);
	}

	public PlatformStatement findByPage(int page,int limit){
		Map<String,Object> params = new HashMap<>();
		params.put("start",(page-1)*limit);
		params.put("limit",limit);
		return (PlatformStatement) findOne("findByPage",params);
	}

	@Transactional
	public int updateStatement(Map<String,Object> params){
		return execute("update",params);
	}
}

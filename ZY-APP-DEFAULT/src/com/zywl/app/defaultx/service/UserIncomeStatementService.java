package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.UserIncomeStatement;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserIncomeStatementService extends DaoService{

	public UserIncomeStatementService( ) {
		super("UserIncomeStatementMapper");
	}

	@Transactional
	public UserIncomeStatement addStatement(Long userId) {
		UserIncomeStatement obj = new UserIncomeStatement();
		obj.setUserId(userId);
		obj.setYmd(DateUtil.format9(new Date()));
		obj.setOneIncome(BigDecimal.ZERO);
		obj.setTwoIncome(BigDecimal.ZERO);
		save(obj);
		return obj;
	}
	
	
	public List<UserIncomeStatement> findAllUserIncomeStatement(){
		return findAll();
	}
	
	@Transactional
	public int addOneIncome(Long userId,BigDecimal income,String ymd){
		Map<String,Object> params = new HashMap<>();
		params.put("userId",userId);
		params.put("ymd",ymd);
		params.put("income",income);
		return execute("addOneIncome",params);
	}

	public int addTwoIncome(Long userId,BigDecimal income,String ymd){
		Map<String,Object> params = new HashMap<>();
		params.put("userId",userId);
		params.put("ymd",ymd);
		params.put("income",income);
		return execute("addTwoIncome",params);
	}

	public UserIncomeStatement findByUserIdAndYmd(Long userId){
		Map<String,Object> params = new HashMap<>();
		params.put("userId",userId);
		params.put("ymd",DateUtil.format9(new Date()));
		return (UserIncomeStatement) findOne("findByUserIdAndYmd",params);
	}

	public List<UserIncomeStatement> findByUserId(Long userId){
		Map<String,Object> params = new HashMap<>();
		params.put("userId",userId);
		params.put("start",0);
		params.put("limit",7);
		return  findList("findByUserId",params);
	}


	public List<UserIncomeStatement> findByUserIdSync(Long userId){
		Map<String,Object> params = new HashMap<>();
		params.put("userId",userId);
		params.put("start",0);
		params.put("limit",7);
		return  findList("findByUserIdSync",params);
	}
}

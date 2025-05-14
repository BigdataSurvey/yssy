package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.ApplyFor;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ApplyForService extends DaoService{

	public ApplyForService( ) {
		super("ApplyForMapper");
	}

	@Transactional
	public void addApplyFor(Long userId,BigDecimal income,BigDecimal sill) {
		ApplyFor applyFor = new ApplyFor();
		applyFor.setSill(sill);
		applyFor.setCreateAdmin(null);
		applyFor.setCreateTime(new Date());
		applyFor.setIncome(income);
		applyFor.setStatus(0);
		applyFor.setUserId(userId);
		save(applyFor);
	}
	
	
	public List<ApplyFor> findAllApplyFor(){
		return findAll();
	}

	public ApplyFor findByUserId(Long userId){
		Map<String,Object> params = new HashMap<>();
		params.put("userId",userId);
		return (ApplyFor) findOne("findByUserId",params);
	}

	@Transactional
	public int examine(Long userId,int status){
		//1通过 2拒绝
		Map<String,Object> params = new HashMap<>();
		params.put("userId",userId);
		params.put("status",status);
		return execute("pass",params);
	}

}

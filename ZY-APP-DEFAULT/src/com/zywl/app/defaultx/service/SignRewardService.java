package com.zywl.app.defaultx.service;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.map.HashedMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zywl.app.base.bean.SignReward;
import com.zywl.app.defaultx.dbutil.DaoService;

@Service
public class SignRewardService extends DaoService{
	
	

	public SignRewardService() {
		super("SignRewardMapper");
	}
	
	@Transactional
	public void addSignReward(int month,int days,String context) {
		SignReward signReward = new SignReward();
		signReward.setContext(context);
		signReward.setDays(days);
		signReward.setMonth(month);
		save(signReward);
	}
	
	@Transactional
	public int updateReward(int month,String context) {
		Map<String, Object> params = new HashedMap<String, Object>();
		params.put("month", month);
		params.put("context", context);
		return execute("updateReward", params);
	}
	
	public List<SignReward> findAllReward() {
		return findAll();
	}

}

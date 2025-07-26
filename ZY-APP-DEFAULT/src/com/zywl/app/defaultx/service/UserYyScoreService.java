package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.UserYyScore;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserYyScoreService extends DaoService{

	public UserYyScoreService( ) {
		super("UserYyScoreMapper");
	}





	@Transactional
	public int addYyScore(Long userId,BigDecimal score){
		Map<String,Object> params = new HashMap<>();
		params.put("userId",userId);
		params.put("score",score);
		return execute("insertOrUpdate",params);
	}




	@Transactional
	public int subScore(Long userId,BigDecimal score){
		Map<String,Object> params = new HashMap<>();
		params.put("userId",userId);
		params.put("score",score.negate());
		return execute("insertOrUpdate",params);
	}

	public UserYyScore findByUserId(Long userId){
		Map<String,Object> params = new HashMap<>();
		params.put("userId",userId);
		UserYyScore findByUserId = (UserYyScore) findOne("findByUserId", params);
		if (findByUserId == null){
			addYyScore(userId,BigDecimal.ZERO);
		}
		findByUserId = (UserYyScore) findOne("findByUserId", params);
		return findByUserId;
	}



}

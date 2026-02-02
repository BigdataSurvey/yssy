package com.zywl.app.defaultx.service;

import com.alibaba.fastjson2.JSONArray;
import com.zywl.app.base.bean.GameNs;
import com.zywl.app.base.bean.NsSettle;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.logging.Log;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

@Service
public class NsSettleService extends DaoService {

	public NsSettleService() {
		super("NsSettleMapper");
	}

	@Transactional
	public Long addSettleInfo(Long userId,BigDecimal betAmount,BigDecimal profit,Long nsId,int round){
		NsSettle nsSettle = new NsSettle();
		nsSettle.setCreateTime(new Date());
		nsSettle.setUserId(userId);;
		nsSettle.setBetAmount(betAmount);
		nsSettle.setProfit(profit);
		nsSettle.setRound(round);
		nsSettle.setNsId(nsId);
		nsSettle.setStatus(1);
		insert(nsSettle);
		return nsSettle.getId();
	}



	@Transactional
	public void pauseGameNs(int round,Long nowId,BigDecimal hp,BigDecimal prize,Long runTime,Long userId,String betInfo,String lastIds){
		Map<String, Object> params = new HashedMap<>();
		params.put("round", round);
		params.put("nowId",nowId);
		params.put("hp", hp);
		params.put("prize",prize);
		params.put("runTime", runTime);
		params.put("lastUserId",userId);
		params.put("pauseTime",new Date());
		params.put("betInfo", betInfo);
		params.put("lastIds",lastIds);
		execute("pauseGameNs",params);
	}

	@Transactional
	public int batchInsertSettle(JSONArray settles){
		return execute("batchInsertSettle",settles);
	}

	@Transactional
	public void gameOver(int round,Long nowId,BigDecimal hp,BigDecimal prize,Long runTime,Long userId,String betInfo,String lastIds){
		Map<String, Object> params = new HashedMap<>();
		params.put("round", round);
		params.put("nowId",nowId);
		params.put("hp", hp);
		params.put("prize",prize);
		params.put("runTime", runTime);
		params.put("lastUserId",userId);
		params.put("betInfo", betInfo);
		params.put("lastIds",lastIds);
		execute("gameOver",params);
	}

	public GameNs findByRound(int round){
		Map<String, Object> params = new HashedMap<>();
		params.put("round", round);
		return (GameNs) findOne("findByRound",params);
	}



	@Transactional
	public int updateRunTime(Long runTime,int round){
		Map<String, Object> params = new HashedMap<>();
		params.put("round", round);
		params.put("runTime",runTime);
		return execute("updateRunTime",params);
	}

	@Transactional
	public int updateBetInfo(String betInfo,String lastIds){
		Map<String, Object> params = new HashedMap<>();
		params.put("betInfo", betInfo);
		params.put("lastIds",lastIds);
		return execute("updateBetInfo",params);
	}

	public GameNs findNowRound(){
		return (GameNs) findOne("findNowRound",null);
	}

	@Override
	protected Log logger() {
		return logger;
	}
	
	
}

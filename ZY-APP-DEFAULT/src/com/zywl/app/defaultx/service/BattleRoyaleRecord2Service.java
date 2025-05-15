package com.zywl.app.defaultx.service;

import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.vo.BattleRoyale2Record;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
public class BattleRoyaleRecord2Service extends DaoService {

	public BattleRoyaleRecord2Service() {
		super("BattleRoyaleRecord2Mapper");
		// TODO Auto-generated constructor stub
	}


	private static final Log logger = LogFactory.getLog(BattleRoyaleRecord2Service.class);



	/**
	 * 增加大逃杀下注记录
	 * @param userId
	 */
	@Transactional
	public Long addBattleRoyaleRecord(Long userId,String orderNo,String periodsNum,String betInfo,BigDecimal amount) {
		BattleRoyale2Record record = new BattleRoyale2Record();
		record.setUserId(userId);
		record.setOrderNo(orderNo);
		if (periodsNum==null|| periodsNum.equals("0")) {
			periodsNum="1";
		}
		record.setPeriodsNum(periodsNum);
		record.setBetInfo(betInfo);
		record.setBetAmount(amount);
		record.setStatus(0);
		record.setCreateTime(new Date());
		record.setUpdateTime(new Date());
		save(record);
		return record.getId();
	}




	public List<BattleRoyale2Record> findHistoryRecordByUserId(Long userId) {
		Map<String, Object> params = new HashedMap<String, Object>();
		params.put("userId", userId);
		params.put("start",0);
		params.put("limit",20);
		return  findList("findByUserId", params);
	}
	
	//查找未开奖的下注信息
	public List<BattleRoyale2Record> findNoPrizeInfo(){
		return findList("findNoPrize", null);
	}
	
	public BattleRoyale2Record findPeriodsNum() {
		return (BattleRoyale2Record) findOne("findPeriodsNum", null);
	}
	@Transactional
	public void batchUpdateRecord(JSONObject obj) {
		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
		Set<String> set = obj.keySet();
		for (String key : set) {
			Map<String, Object> map = new HashedMap<String, Object>();
			map.put("orderNo", key);
			JSONObject o = (JSONObject) obj.get(key);
			map.put("winAmount", o.get("winAmount"));
			map.put("lotteryResult", o.getString("lotteryResult"));
			map.put("isWin", o.get("isWin"));
			map.put("betAmount",o.get("betAmount"));
			map.put("betInfo",o.get("betInfo"));
			list.add(map);
		}
		 execute("batchUpdateRecord", list);
	}
	@Transactional
	public void addBetAmount(BigDecimal betAmount,String orderNo) {
		Map<String, Object> params = new HashedMap<String, Object>();
		params.put("betAmount", betAmount);
		params.put("orderNo", orderNo);
		int a = execute("addBetAmount", params);
		if (a<1) {
			throwExp("参与失败！");
		}
	}
	
	@Override
	protected Log logger() {
		return logger;
	}


	@Transactional
	public void deletedThreeDayRecord(){
		Map<String, Object> params  = new HashMap<>();
		params.put("time", DateUtil.getDateByDay(-3));
		execute("deletedThreeDayRecord",params);
	}
	
}

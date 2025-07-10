package com.zywl.app.defaultx.service;

import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.DgsBetRecord;
import com.zywl.app.base.bean.LhdBetRecord;
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
public class DgsBetRecordService extends DaoService {

	public DgsBetRecordService() {
		super("DgsBetRecordMapper");
	}


	private static final Log logger = LogFactory.getLog(DgsBetRecordService.class);
	
	
	
	/**
	 * 增加大逃杀下注记录
	 * @param userId
	 */
	@Transactional
	public DgsBetRecord addRecord(Long userId, String orderNo,Integer monsterType,Long monsterNo) {
		DgsBetRecord record = new DgsBetRecord();
		record.setUserId(userId);
		record.setMonsterId(monsterType);
		record.setOrderNo(orderNo);
		record.setBetAmount(BigDecimal.valueOf(monsterType));
		record.setStatus(0);
		record.setMonsterNo(Math.toIntExact(monsterNo));
		record.setCreateTime(new Date());
		record.setUpdateTime(new Date());
		execute("insert",record);
		return record;
	}
	
	
	
	
	public List<DgsBetRecord> findHistoryRecordByUserId(Long userId) {
		Map<String, Object> params = new HashedMap<String, Object>();
		params.put("userId", userId);
		params.put("start",0);
		params.put("limit",10);
		return  findList("findByUserId", params);
	}

	public List<DgsBetRecord> findUnSettleByMonsterNo(Long monsterNo) {
		Map<String, Object> params = new HashedMap<>();
		params.put("monsterNo", monsterNo);
		params.put("status",0);
		return  findList("findUnSettleByMonsterNo", params);
	}
	public List<DgsBetRecord> findByStatus(Long monsterId,Integer status) {
		Map<String, Object> params = new HashedMap<String, Object>();
		params.put("status", status);
		params.put("monsterId",monsterId);
		return  findList("findByStatus", params);
	}
	public List<DgsBetRecord> findByStatusLimit(Long monsterId,Long userId,Integer status,Integer page,Integer num) {
		Map<String, Object> params = new HashedMap<String, Object>();
		params.put("status", status);
		params.put("monsterId",monsterId);
		params.put("userId",userId);
		params.put("start", page*num);
		params.put("limit", num);
		return  findList("findByStatus", params);
	}

	@Transactional
	public int updateRecord(DgsBetRecord dgsBetRecord){
		return execute("updateRecord",dgsBetRecord);

	}
	
	//查找未开奖的下注信息
	public List<LhdBetRecord> findNoPrizeInfo(){
		return findList("findNoPrize", null);
	}
	
	public LhdBetRecord findPeriodsNum() {
		return (LhdBetRecord) findOne("findPeriodsNum", null);
	}
	@Transactional
	public void batchUpdateRecord(List<DgsBetRecord> dgsBetRecords) {
		 execute("batchUpdateRecord", dgsBetRecords);
	}
	@Transactional
	public void addBetAmount(BigDecimal betAmount,BigDecimal profit,String orderNo) {
		Map<String, Object> params = new HashedMap<String, Object>();
		params.put("betAmount", betAmount);
		params.put("profit", profit);
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

	@Transactional
	public void batchAddBetAmount(JSONObject obj) {
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

}

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
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSON;
import java.math.RoundingMode;

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

	public BattleRoyale2Record findByOrderNo(String orderNo) {
		if (orderNo == null) {
			return null;
		}
		Map<String, Object> params = new HashMap<>();
		params.put("orderNo", orderNo);
		return (BattleRoyale2Record) findOne("findByOrderNo", params);
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


	/**
	 * 返回最近16期汇总/最近100期明细/总投入/总获得/服务器时间。
	 */
	public JSONObject buildUnifiedSummary(Long userId) {
		return buildUnifiedSummary(userId, false, null);
	}

	public JSONObject buildUnifiedSummary(Long userId, boolean zeroBasedRoomId) {
		return buildUnifiedSummary(userId, zeroBasedRoomId, null);
	}

	/**
	 * 结算推送阶段可传入本期实际获得（包含本金），用于弥补 profit 尚未落库的时间差；可为 null。
	 */
	public JSONObject buildUnifiedSummary(Long userId, boolean zeroBasedRoomId, BigDecimal extraGain) {
		JSONObject res = new JSONObject();
		if (userId == null) {
			res.put("recent16Summary", new JSONArray());
			res.put("recent100Periods", new JSONArray());
			res.put("totalInvest", BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
			res.put("totalGain", BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
			res.put("serverTime", System.currentTimeMillis());
			return res;
		}

		Map<String, Object> p = new HashMap<>();
		p.put("userId", userId);
		p.put("start", 0);
		p.put("limit", 100);
		List<BattleRoyale2Record> records = findList("findByUserId", p);
		if (records == null) {
			records = Collections.emptyList();
		}

		Map<Integer, Integer> cnt = new HashMap<>();
		int take = Math.min(16, records.size());
		for (int i = 0; i < take; i++) {
			BattleRoyale2Record r = records.get(i);
			for (Integer rid : parseRoomIds(r.getBetInfo(), zeroBasedRoomId)) {
				cnt.put(rid, cnt.getOrDefault(rid, 0) + 1);
			}
		}

		JSONArray recent16Summary = new JSONArray();
		List<Integer> rooms = new ArrayList<>(cnt.keySet());
		Collections.sort(rooms);
		for (Integer rid : rooms) {
			JSONObject item = new JSONObject();
			item.put("roomId", String.valueOf(rid));
			item.put("roomName", "房间" + rid);
			item.put("count", cnt.get(rid));
			recent16Summary.add(item);
		}

		JSONArray recent100Periods = new JSONArray();
		for (BattleRoyale2Record r : records) {
			JSONObject item = new JSONObject();
			item.put("periodsNum", r.getPeriodsNum());

			JSONArray rs = new JSONArray();
			LinkedHashSet<Integer> set = new LinkedHashSet<>(parseRoomIds(r.getBetInfo(), zeroBasedRoomId));
			for (Integer rid : set) {
				JSONObject rr = new JSONObject();
				rr.put("roomId", String.valueOf(rid));
				rr.put("roomName", "房间" + rid);
				rs.add(rr);
			}
			item.put("rooms", rs);
			recent100Periods.add(item);
		}

		Map<String, Object> tp = new HashMap<>();
		tp.put("userId", userId);
		Map<String, Object> totals = (Map<String, Object>) findOne("sumTotalsByUserId", tp);
		BigDecimal totalInvest = safeBigDecimal(totals == null ? null : totals.get("totalInvest"));
		BigDecimal totalGain = safeBigDecimal(totals == null ? null : totals.get("totalGain"));
		if (extraGain != null) {
			totalGain = totalGain.add(extraGain);
		}

		res.put("recent16Summary", recent16Summary);
		res.put("recent100Periods", recent100Periods);
		res.put("totalInvest", totalInvest.setScale(2, RoundingMode.HALF_UP));
		res.put("totalGain", totalGain.setScale(2, RoundingMode.HALF_UP));
		res.put("serverTime", System.currentTimeMillis());
		return res;
	}

	private BigDecimal safeBigDecimal(Object v) {
		if (v == null) {
			return BigDecimal.ZERO;
		}
		if (v instanceof BigDecimal) {
			return (BigDecimal) v;
		}
		try {
			return new BigDecimal(String.valueOf(v));
		} catch (Exception e) {
			return BigDecimal.ZERO;
		}
	}

	private List<Integer> parseRoomIds(String betInfo, boolean zeroBasedRoomId) {
		if (betInfo == null || betInfo.trim().isEmpty()) {
			return Collections.emptyList();
		}
		String s = betInfo.trim();
		List<String> raw;
		try {
			if (s.startsWith("[") && s.endsWith("]")) {
				JSONArray arr = JSON.parseArray(s);
				raw = new ArrayList<>();
				for (int i = 0; i < arr.size(); i++) {
					Object o = arr.get(i);
					if (o != null) {
						raw.add(String.valueOf(o));
					}
				}
			} else {
				raw = Arrays.asList(s.split(","));
			}
		} catch (Exception e) {
			raw = Arrays.asList(s.split(","));
		}

		LinkedHashSet<Integer> dedup = new LinkedHashSet<>();
		for (String r : raw) {
			if (r == null) continue;
			String t = r.trim();
			if (t.isEmpty()) continue;
			try {
				int rid = Integer.parseInt(t);
				if (zeroBasedRoomId) {
					rid = rid + 1;
				}
				dedup.add(rid);
			} catch (Exception ignore) {
			}
		}
		return new ArrayList<>(dedup);
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
	public int addBetAmountAndInfo(BigDecimal betAmount, String orderNo, String betInfo) {
		Map<String, Object> params = new HashedMap<String, Object>();
		params.put("betAmount", betAmount);
		params.put("orderNo", orderNo);
		params.put("betInfo", betInfo);
		return execute("addBetAmountAndInfo", params);
	}

}

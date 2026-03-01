package com.zywl.app.defaultx.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.BattleRoyaleRecord;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service ;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class BattleRoyaleRecordService extends DaoService {

	public BattleRoyaleRecordService() {
		super("BattleRoyaleRecordMapper");
		// TODO Auto-generated constructor stub
	}


	private static final Log logger = LogFactory.getLog(BattleRoyaleRecordService.class);



	/**
	 * 增加大逃杀下注记录
	 * @param userId
	 */
	@Transactional
	public Long addBattleRoyaleRecord(Long userId,String orderNo,String periodsNum,String betInfo,BigDecimal amount) {
		// System.out.println(123);
		BattleRoyaleRecord record = new BattleRoyaleRecord();
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




	public List<BattleRoyaleRecord> findHistoryRecordByUserId(Long userId) {
		Map<String, Object> params = new HashedMap<String, Object>();
		params.put("userId", userId);
		params.put("start",0);
		params.put("limit",20);
		return  findList("findByUserId", params);
	}

	//查找未开奖的下注信息
	public List<BattleRoyaleRecord> findNoPrizeInfo(){
		return findList("findNoPrize", null);
	}

	public BattleRoyaleRecord findPeriodsNum() {
		return (BattleRoyaleRecord) findOne("findPeriodsNum", null);
	}
	@Transactional
	public void batchUpdateRecord(JSONObject obj) {
		if (obj!=null && !obj.isEmpty()) {
			List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
			Set<String> set = obj.keySet();
			for (String key : set) {
				Map<String, Object> map = new HashedMap<String, Object>();
				map.put("orderNo", key);
				JSONObject o = (JSONObject) obj.get(key);
				map.put("winAmount", o.get("winAmount"));
				map.put("lotteryResult", o.get("lotteryResult"));
				map.put("isWin", o.get("isWin"));
				map.put("betAmount",o.get("betAmount"));
				map.put("betInfo",o.get("betInfo"));
				list.add(map);
			}
			execute("batchUpdateRecord", list);
		}
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


	/**
	 * 返回最近16期汇总/最近100期明细/总投入/总获得/服务器时间。
	 */
	public JSONObject buildUnifiedSummary(Long userId) {
		return buildUnifiedSummary(userId, false, null, null);
	}

	public JSONObject buildUnifiedSummary(Long userId, boolean zeroBasedRoomId) {
		return buildUnifiedSummary(userId, zeroBasedRoomId, null, null);
	}

	/**
	 * optionNum：房间数（DTS7=8）。为空则默认 8（兼容旧调用方）。
	 */
	public JSONObject buildUnifiedSummary(Long userId, boolean zeroBasedRoomId, BigDecimal extraGain, Integer optionNum) {
		int opt = (optionNum == null || optionNum <= 0) ? 8 : optionNum;
		return buildUnifiedSummary0(userId, zeroBasedRoomId, extraGain, opt);
	}

	/**
	 * 结算推送阶段可传入本期实际获得弥补 profit 未落库的时间差
	 */
	@SuppressWarnings("unchecked")
	public JSONObject buildUnifiedSummary(Long userId, boolean zeroBasedRoomId, BigDecimal extraGain) {
		// 兼容旧签名：默认 8 个房间
		return buildUnifiedSummary0(userId, zeroBasedRoomId, extraGain, 8);
	}

	@SuppressWarnings("unchecked")
	private JSONObject buildUnifiedSummary0(Long userId, boolean zeroBasedRoomId, BigDecimal extraGain, int optionNum) {
		JSONObject res = new JSONObject();
		if (userId == null) {
			res.put("recent16Summary", new JSONArray());
			res.put("recent100Periods", new JSONArray());
			res.put("totalInvest", BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
			res.put("totalGain", BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
			res.put("serverTime", System.currentTimeMillis());
			// 兼容字段
			res.put("totalBetAmount", BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
			res.put("totalWinAmount", BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
			res.put("recent16Result", new JSONArray());
			return res;
		}

		Map<String, Object> p = new HashMap<>();
		p.put("userId", userId);
		p.put("start", 0);
		p.put("limit", 100);
		// ✅ DTS(单杀) 的 Mapper(BattleRoyaleRecordMapper.findByUserId) resultType = BattleRoyaleRecord
		// 之前这里误写成 BattleRoyale2Record，导致记录页 ClassCastException -> 前端“未知异常”
		List<BattleRoyaleRecord> records = findList("findByUserId", p);
		if (records == null) {
			records = Collections.emptyList();
		}

		// 近100期统计（开奖结果房间次数统计） -> recent100Periods
		// 使用 lotteryResult（狮子袭击的房间）而非 betInfo（用户下注的房间）
		Map<Integer, Integer> cnt = new HashMap<>();
		int take100 = Math.min(100, records.size());
		for (int i = 0; i < take100; i++) {
			BattleRoyaleRecord r = records.get(i);
			String lr = r.getLotteryResult();
			if (lr == null || lr.trim().isEmpty()) continue;
			for (Integer rid : parseRoomIds(lr, zeroBasedRoomId)) {
				cnt.put(rid, cnt.getOrDefault(rid, 0) + 1);
			}
		}

		// ✅必须把 1~optionNum 全部返回（缺省 count=0），否则前端只显示“出现过的房间”
		JSONArray recent100Periods = new JSONArray();
		for (int rid = 1; rid <= optionNum; rid++) {
			JSONObject item = new JSONObject();
			item.put("roomId", String.valueOf(rid));
			item.put("roomName", "房间" + rid);
			item.put("count", cnt.getOrDefault(rid, 0));
			recent100Periods.add(item);
		}

		// 近16期结果 -> recent16Summary（使用 lotteryResult 开奖结果）
		JSONArray recent16Summary = new JSONArray();
		JSONArray recent16Result = new JSONArray();
		int added16 = 0;
		for (int i = 0; i < records.size() && added16 < 16; i++) {
			BattleRoyaleRecord r = records.get(i);
			String lr = r.getLotteryResult();
			if (lr == null || lr.trim().isEmpty()) continue;

			JSONObject item = new JSONObject();
			item.put("periodsNum", r.getPeriodsNum());

			JSONArray rs = new JSONArray();
			LinkedHashSet<Integer> set = new LinkedHashSet<>(parseRoomIds(lr, zeroBasedRoomId));
			for (Integer rid : set) {
				JSONObject rr = new JSONObject();
				rr.put("roomId", String.valueOf(rid));
				rr.put("roomName", "房间" + rid);
				rs.add(rr);
				// 兼容：给前端一个“扁平化”的 16 期结果列表（只取第一个 roomId）
				if (rs.size() == 1) {
					recent16Result.add(String.valueOf(rid));
				}
			}
			item.put("rooms", rs);
			recent16Summary.add(item);
			added16++;
		}

		Map<String, Object> tp = new HashMap<>();
		tp.put("userId", userId);
		Map<String, Object> totals = (Map<String, Object>) findOne("sumTotalsByUserId", tp);
		BigDecimal totalInvest = safeBigDecimal(totals == null ? null : totals.get("totalInvest"));
		BigDecimal totalGain = safeBigDecimal(totals == null ? null : totals.get("totalGain"));
		if (extraGain != null) {
			totalGain = totalGain.add(extraGain);
		}

		res.put("recent100Periods", recent100Periods); // 近100期统计
		res.put("recent16Summary", recent16Summary);   // 近16期结果
		res.put("recent16Result", recent16Result);
		res.put("totalInvest", totalInvest.setScale(2, RoundingMode.HALF_UP));
		res.put("totalGain", totalGain.setScale(2, RoundingMode.HALF_UP));
		// 兼容字段：避免前端写死 totalBetAmount/totalWinAmount
		res.put("totalBetAmount", totalInvest.setScale(2, RoundingMode.HALF_UP));
		res.put("totalWinAmount", totalGain.setScale(2, RoundingMode.HALF_UP));
		res.put("serverTime", System.currentTimeMillis());
		return res;
	}


	private BigDecimal safeBigDecimal(Object v) {
		if (v == null) return BigDecimal.ZERO;
		if (v instanceof BigDecimal) return (BigDecimal) v;
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
					if (o != null) raw.add(String.valueOf(o));
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
				if (zeroBasedRoomId) rid = rid + 1;
				dedup.add(rid);
			} catch (Exception ignore) {}
		}
		return new ArrayList<>(dedup);
	}

}

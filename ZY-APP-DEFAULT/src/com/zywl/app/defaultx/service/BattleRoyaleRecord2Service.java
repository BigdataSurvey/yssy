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
	 * 返回最近16期汇总/最近100期明细/总投入/总获得/服务器时间。
	 *
	 * zeroBasedRoomId == true  : 输出 roomId = 0..5（给前端显示用）
	 * zeroBasedRoomId == false : 输出 roomId = 1..6（内部默认）
	 *
	 * 注意：解析 DB lotteryResult 时会自动兼容：
	 * - 若数组里包含 0，则视为旧 0..5 存储，内部统一 +1 变成 1..6
	 * - 否则视为新 1..6 存储，内部直接使用
	 */
	public JSONObject buildUnifiedSummary(Long userId, boolean zeroBasedRoomId, BigDecimal extraGain) {
		JSONObject res = new JSONObject();

		JSONArray recent100Periods = new JSONArray();
		JSONArray recent16Summary = new JSONArray();

		// DTS2 固定 6 个元素：内部统一用 1..6
		String[] names = new String[]{"小丑", "帽子", "喇叭", "大象", "狮子", "兔子"};

		// ===== 1) 最近100期：统计开奖结果分布（全服口径，按 periodsNum 去重）=====
		Map<Integer, Integer> cnt = new HashMap<>();
		try {
			Map<String, Object> p100 = new HashMap<>();
			p100.put("limit", 100);
			List<Map<String, Object>> periods100 = findList("findRecentPeriodsDistinct", p100);
			if (periods100 != null) {
				for (Map<String, Object> row : periods100) {
					String lottery = row == null ? null : String.valueOf(row.get("lotteryResult"));
					for (Integer eidInternal : parseElementIdsFromLotteryResultInternal(lottery)) {
						if (eidInternal == null) continue;
						cnt.put(eidInternal, cnt.getOrDefault(eidInternal, 0) + 1);
					}
				}
			}
		} catch (Exception ignore) {
		}

		for (int eidInternal = 1; eidInternal <= 6; eidInternal++) {
			int outRoomId = zeroBasedRoomId ? (eidInternal - 1) : eidInternal;

			JSONObject item = new JSONObject();
			item.put("roomId", String.valueOf(outRoomId));
			item.put("roomName", names[eidInternal - 1]);
			item.put("count", cnt.getOrDefault(eidInternal, 0));
			recent100Periods.add(item);
		}

		// ===== 近16期：返回每期的开奖结果 rooms 明细 =====
		try {
			Map<String, Object> p16 = new HashMap<>();
			p16.put("limit", 16);
			List<Map<String, Object>> periods16 = findList("findRecentPeriodsDistinct", p16);
			if (periods16 != null) {
				for (Map<String, Object> row : periods16) {
					String periodsNum = row == null ? null : String.valueOf(row.get("periodsNum"));
					String lottery = row == null ? null : String.valueOf(row.get("lotteryResult"));

					JSONObject item = new JSONObject();
					item.put("periodsNum", periodsNum);

					JSONArray rooms = new JSONArray();
					List<Integer> eidsInternal = parseElementIdsFromLotteryResultInternal(lottery);
					for (Integer eidInternal : eidsInternal) {
						if (eidInternal == null) continue;

						int outRoomId = zeroBasedRoomId ? (eidInternal - 1) : eidInternal;

						JSONObject rr = new JSONObject();
						rr.put("roomId", String.valueOf(outRoomId));
						rr.put("roomName", names[Math.max(0, Math.min(5, eidInternal - 1))]);
						rooms.add(rr);
					}
					item.put("rooms", rooms);

					recent16Summary.add(item);
				}
			}
		} catch (Exception ignore) {
		}

		// ===== 2) 我的游戏记录：用户口径，仍然按 DB 汇总 =====
		BigDecimal totalInvest = BigDecimal.ZERO;
		BigDecimal totalGain = BigDecimal.ZERO;

		if (userId != null) {
			try {
				Map<String, Object> tp = new HashMap<>();
				tp.put("userId", userId);
				Map<String, Object> totals = (Map<String, Object>) findOne("sumTotalsByUserId", tp);

				totalInvest = safeBigDecimal(totals == null ? null : totals.get("totalInvest"));
				totalGain = safeBigDecimal(totals == null ? null : totals.get("totalGain"));

				if (extraGain != null) {
					totalGain = totalGain.add(extraGain);
				}
			} catch (Exception ignore) {
			}
		}

		res.put("recent100Periods", recent100Periods);
		res.put("recent16Summary", recent16Summary);
		res.put("totalInvest", totalInvest.setScale(2, RoundingMode.HALF_UP));
		res.put("totalGain", totalGain.setScale(2, RoundingMode.HALF_UP));
		res.put("serverTime", System.currentTimeMillis());
		return res;
	}

	/**
	 * DTS2 的 lotteryResult 是 JSON 数组字符串，例如：[1,2,2] 或历史：[0,1,1]
	 * 这里统一转换成内部 1..6 的 elementId 列表。
	 */
	private List<Integer> parseElementIdsFromLotteryResultInternal(String lotteryResult) {
		if (lotteryResult == null || lotteryResult.trim().isEmpty()) {
			return Collections.emptyList();
		}
		try {
			JSONArray arr = JSON.parseArray(lotteryResult);
			if (arr == null || arr.isEmpty()) return Collections.emptyList();

			boolean hasZero = false;
			List<Integer> raw = new ArrayList<>();
			for (int i = 0; i < arr.size(); i++) {
				Integer v = arr.getInteger(i);
				if (v == null) continue;
				if (v == 0) hasZero = true;
				raw.add(v);
			}
			if (raw.isEmpty()) return Collections.emptyList();

			List<Integer> out = new ArrayList<>();
			for (Integer v : raw) {
				if (v == null) continue;
				int internal = hasZero ? (v + 1) : v; // 0..5 -> 1..6
				out.add(internal);
			}
			return out;
		} catch (Exception e) {
			return Collections.emptyList();
		}
	}


	/**
	 * DTS2 的 lotteryResult 是 JSON 数组字符串，例如：[1,2,2]
	 */
	private List<Integer> parseElementIdsFromLotteryResult(String lotteryResult, boolean zeroBasedRoomId) {
		if (lotteryResult == null || lotteryResult.trim().isEmpty()) {
			return Collections.emptyList();
		}
		try {
			JSONArray arr = JSON.parseArray(lotteryResult);
			if (arr == null || arr.isEmpty()) return Collections.emptyList();

			List<Integer> out = new ArrayList<>();
			for (int i = 0; i < arr.size(); i++) {
				Integer v = arr.getInteger(i);
				if (v == null) continue;
				// 兼容历史 0~5 表示法（如有）
				if (zeroBasedRoomId) v = v + 1;
				out.add(v);
			}
			return out;
		} catch (Exception e) {
			return Collections.emptyList();
		}
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

	@Transactional
	public void batchUpdateRecordByPeriodUser(JSONArray list) {
		if (list == null || list.isEmpty()) return;
		List<Map<String, Object>> paramsList = new ArrayList<Map<String, Object>>();
		for (int i = 0; i < list.size(); i++) {
			JSONObject item = list.getJSONObject(i);
			if (item == null) continue;
			String periodNo = item.getString("periodNo");
			String userId = item.getString("userId");
			BigDecimal winAmount = item.getBigDecimal("winAmount");
			String lotteryResult = item.getString("lotteryResult");
			Integer isWin = item.getInteger("isWin");
			if (periodNo == null || userId == null) continue;
			if (winAmount == null) winAmount = BigDecimal.ZERO;
			if (isWin == null) isWin = 0;

			Map<String, Object> map = new HashedMap<String, Object>();
			map.put("periodNo", periodNo);
			map.put("userId", Long.valueOf(userId));
			map.put("winAmount", winAmount);
			map.put("lotteryResult", lotteryResult);
			map.put("isWin", isWin);
			paramsList.add(map);
		}
		if (paramsList.isEmpty()) return;
		execute("batchUpdateRecordByPeriodUser", paramsList);
	}
	/**
	 * 按【期号 + 用户】聚合下注：同一期同一用户只保留一条 status=0 记录
	 * - 存在：bet_amount += chip，bet_info(JSON) 合并累加
	 * - 不存在：插入新记录
	 */
	@Transactional
	public void mergeBetForPeriodUser(Long userId, String periodNo, Integer elementId, BigDecimal chip) {
		if (userId == null || periodNo == null || elementId == null || chip == null) return;

		// 1) 先查该用户该期未结算的聚合记录（只取 id + bet_info）
		Map<String, Object> q = new HashMap<>();
		q.put("userId", userId);
		q.put("periodNo", periodNo);
		Map<String, Object> row = (Map<String, Object>) findOne("findUnsettledIdAndInfoByPeriodUser", q);

		Date now = new Date();

		if (row != null && row.get("id") != null) {
			Long id = Long.valueOf(String.valueOf(row.get("id")));
			String oldBetInfo = row.get("betInfo") == null ? null : String.valueOf(row.get("betInfo"));

			// 2) Java 合并 JSON：同 elementId 累加
			JSONObject merged = new JSONObject();
			try {
				if (oldBetInfo != null && !oldBetInfo.trim().isEmpty()) {
					merged = JSONObject.parseObject(oldBetInfo);
					if (merged == null) merged = new JSONObject();
				}
			} catch (Exception ignore) {
				merged = new JSONObject();
			}

			String k = String.valueOf(elementId);
			BigDecimal old = BigDecimal.ZERO;
			try {
				Object v = merged.get(k);
				if (v != null) old = new BigDecimal(String.valueOf(v));
			} catch (Exception ignore) {
				old = BigDecimal.ZERO;
			}
			BigDecimal nv = old.add(chip);
			merged.put(k, nv.stripTrailingZeros().toPlainString());

			Map<String, Object> up = new HashMap<>();
			up.put("id", id);
			up.put("chip", chip);
			up.put("betInfo", merged.toJSONString());
			up.put("now", now);

			int updated = execute("addBetAmountAndMergeInfoById", up);
			if (updated > 0) return;

			// 极小概率：被别的线程更新/删除，继续走插入
		}

		// 3) 插入新聚合记录
		BattleRoyale2Record record = new BattleRoyale2Record();
		record.setUserId(userId);
		record.setOrderNo("PBXREC-" + periodNo + "-" + userId);
		record.setPeriodsNum(periodNo);

		JSONObject betInfo = new JSONObject();
		betInfo.put(String.valueOf(elementId), chip.stripTrailingZeros().toPlainString());
		record.setBetInfo(betInfo.toJSONString());

		record.setBetAmount(chip);
		record.setProfit(BigDecimal.ZERO);
		record.setLotteryResult(null);
		record.setWinOrLose(0);
		record.setStatus(0);
		record.setCreateTime(now);
		record.setUpdateTime(now);
		save(record);
	}

}

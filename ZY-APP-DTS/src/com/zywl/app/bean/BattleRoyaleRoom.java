package com.zywl.app.bean;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.zywl.app.defaultx.enmus.GameTypeEnum;
import org.apache.commons.collections4.map.HashedMap;

import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.BaseBean;

public class BattleRoyaleRoom extends BaseBean{

	public  String bet;

	public  Map<String,Double> lastWeekTopThree = new ConcurrentHashMap<>();
	private  Long readyTime = System.currentTimeMillis();

	private ConcurrentHashMap<String, Map<String, String>> players;

	//上一期的结果
	private String lastResult;

	//下一期的结果
	public static Integer nextResult;
	
	//用户下注信息
	private ConcurrentHashMap<String, Map<String, BigDecimal>> userBetInfo ;

	private Map<String,String> userCheckNum = new ConcurrentHashMap<>();

	private Map<String,BigDecimal> userBetAmount = new ConcurrentHashMap<>();
	
	
	//可下注选项对应金额以及人数
	private ConcurrentHashMap<String, Map<String, String>> betOptionsInfo;
	
	
	//用户下注订单信息
	private ConcurrentHashMap<String, Map<String, String>> userBetOrderInfo;
	
	private String peridosNum;
	
	private String lotteryResult;
	
	private JSONObject history100Reuslt;
	
	private JSONObject history20Reuslt;
	
	private int status;
	
	//最近一期开奖结果
	private String result;

	//状态为游戏中时的 游戏开始时间
	private long  beginTime;
	
	//本局结束时间
	private long endTime;
	
	//下注人数
	private int betNum;
	
	//观看人数
	private int lookNum;
	
	private Map<String,  Map<String, JSONObject>> roomList;
	
	private Map<String, Map<String, Object>> lookList ;
	
	//下注金额
	private BigDecimal allBetAmount;
	
	private int option;
	
	private JSONObject settleDate;
	
	public void initRoomInfo() {
		betNum=0;
		allBetAmount=BigDecimal.ZERO;
		lookNum = getPlayers().size();
		beginTime = 0L;
		endTime = 0L;
		userCheckNum = new ConcurrentHashMap<>();
		userBetAmount = new ConcurrentHashMap<>();
		peridosNum=peridosNum==null?"1": String.valueOf( (Integer.parseInt(peridosNum)+1));
		userBetInfo=new ConcurrentHashMap<String, Map<String,BigDecimal>>();
		betOptionsInfo=new ConcurrentHashMap<String, Map<String,String>>();
		userBetOrderInfo=new ConcurrentHashMap<String, Map<String,String>>();
		roomList = new ConcurrentHashMap<String, Map<String,JSONObject>>();
		for (int i =0; i < option; i++) {
			roomList.put(String.valueOf(i), new ConcurrentHashMap<String, JSONObject>());
		}
		for (String userId : players.keySet()) {
			if (lookList.containsKey(userId)) {
				continue;
			}
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("userId", userId);
			map.put("name", players.get(userId).get("userName"));
			lookList.put(userId, map);
		}
		for (int i = 0; i < option; i++) {
			Map<String, String> map = new HashMap<String, String>();
			map.put("betNumber", "0");
			map.put("betAmount", BigDecimal.ZERO.toString());
			betOptionsInfo.put(String.valueOf(i), map);
		}
		
	}

	
	
	public BattleRoyaleRoom() {
		status = 1;
		players=new ConcurrentHashMap<String, Map<String,String>>();
		userBetInfo=new ConcurrentHashMap<String, Map<String,BigDecimal>>();
		betOptionsInfo=new ConcurrentHashMap<String, Map<String,String>>();
		userBetOrderInfo=new ConcurrentHashMap<String, Map<String,String>>();
		history100Reuslt=new JSONObject();
		history20Reuslt=new JSONObject();
		roomList = new ConcurrentHashMap<String, Map<String,JSONObject>>();
		for (int i =0; i < option; i++) {
			roomList.put(String.valueOf(i), new ConcurrentHashMap<String, JSONObject>());
		}
		lookList = new ConcurrentHashMap<String, Map<String,Object>>();
		allBetAmount=BigDecimal.ZERO;
		settleDate = new JSONObject();
	}
	
	public BattleRoyaleRoom(int option) {
		status = 1;
		players=new ConcurrentHashMap<String, Map<String,String>>();
		userBetInfo=new ConcurrentHashMap<String, Map<String,BigDecimal>>();
		betOptionsInfo=new ConcurrentHashMap<String, Map<String,String>>();
		userBetOrderInfo=new ConcurrentHashMap<String, Map<String,String>>();
		history100Reuslt=new JSONObject();
		history20Reuslt=new JSONObject();
		roomList = new ConcurrentHashMap<String, Map<String,JSONObject>>();
		lookList = new ConcurrentHashMap<String, Map<String,Object>>();
		for (int i = 0; i < option; i++) {
			Map<String, String> map = new HashMap<String, String>();
			map.put("betNumber", "0");
			map.put("betAmount", BigDecimal.ZERO.toString());
			betOptionsInfo.put(String.valueOf(i), map);
			roomList.put(String.valueOf(i), new ConcurrentHashMap<String, JSONObject>());
		}
		allBetAmount=BigDecimal.ZERO;
		this.option=option;
		settleDate = new JSONObject();
	}
	
	
	
	public Map<String, Map<String, String>> getPlayers() {
		return players;
	}

	public void setPlayers(ConcurrentHashMap<String, Map<String, String>> players) {
		this.players = players;
	}

	public String getPeridosNum() {
		return peridosNum;
	}

	public void setPeridosNum(String peridosNum) {
		this.peridosNum = peridosNum;
	}

	public String getLotteryResult() {
		return lotteryResult;
	}

	public void setLotteryResult(String lotteryResult) {
		this.lotteryResult = lotteryResult;
	}

	public JSONObject getHistory100Reuslt() {
		return history100Reuslt;
	}

	public int getLookNum() {
		return lookNum;
	}

	public void setLookNum(int lookNum) {
		this.lookNum = lookNum;
	}

	public void setHistory100Reuslt(JSONObject history100Reuslt) {
		this.history100Reuslt = history100Reuslt;
	}

	public JSONObject getHistory20Reuslt() {
		return history20Reuslt;
	}

	public void setHistory20Reuslt(JSONObject history20Reuslt) {
		this.history20Reuslt = history20Reuslt;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}


	public long getBeginTime() {
		return beginTime;
	}

	public void setBeginTime(long beginTime) {
		this.beginTime = beginTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public int getBetNum() {
		return betNum;
	}

	public void setBetNum(int betNum) {
		this.betNum = betNum;
	}

	public BigDecimal getAllBetAmount() {
		return allBetAmount;
	}

	public void setAllBetAmount(BigDecimal allBetAmount) {
		this.allBetAmount = allBetAmount;
	}

	public int getOption() {
		return option;
	}

	public void setOption(int option) {
		this.option = option;
	}


	public ConcurrentHashMap<String, Map<String, BigDecimal>> getUserBetInfo() {
		return userBetInfo;
	}

	public void setUserBetInfo(ConcurrentHashMap<String, Map<String, BigDecimal>> userBetInfo) {
		this.userBetInfo = userBetInfo;
	}

	public ConcurrentHashMap<String, Map<String, String>> getBetOptionsInfo() {
		return betOptionsInfo;
	}

	public void setBetOptionsInfo(ConcurrentHashMap<String, Map<String, String>> betOptionsInfo) {
		this.betOptionsInfo = betOptionsInfo;
	}

	public ConcurrentHashMap<String, Map<String, String>> getUserBetOrderInfo() {
		return userBetOrderInfo;
	}

	public void setUserBetOrderInfo(ConcurrentHashMap<String, Map<String, String>> userBetOrderInfo) {
		this.userBetOrderInfo = userBetOrderInfo;
	}







	public Map<String, Map<String, JSONObject>> getRoomList() {
		return roomList;
	}



	public void setRoomList(Map<String, Map<String, JSONObject>> roomList) {
		this.roomList = roomList;
	}



	public Map<String, Map<String, Object>> getLookList() {
		return lookList;
	}



	public void setLookList(Map<String, Map<String, Object>> lookList) {
		this.lookList = lookList;
	}


	public JSONObject getReturnInfo() {
		JSONObject result = new JSONObject();
		result.put("status", String.valueOf(status));
		result.put("beginTime", beginTime);
		result.put("endTime", endTime);

		// roomList 补齐每个玩家对象的 roomId / userId
		JSONObject roomListResp = new JSONObject();
		try {
			if (roomList != null) {
				for (String rid : roomList.keySet()) {
					Map<String, JSONObject> users = roomList.get(rid);

					JSONObject usersObj = new JSONObject();
					if (users != null) {
						for (Map.Entry<String, JSONObject> e : users.entrySet()) {
							String uid = e.getKey();
							JSONObject info = e.getValue();
							if (info == null) {
								info = new JSONObject();
							}

							if (!info.containsKey("userId")) {
								info.put("userId", uid);
							}
							if (!info.containsKey("roomId")) {
								info.put("roomId", rid);
							}

							usersObj.put(uid, info);
						}
					}
					roomListResp.put(rid, usersObj);
				}
			}
		} catch (Exception ignore) {
			roomListResp = JSONObject.from(roomList);
		}

		result.put("roomList", roomListResp);

		result.put("lookList", new ConcurrentHashMap<String, Map<String, Object>>());
		result.put("lastResult", lastResult);
		result.put("periodsNum", getPeridosNum());
		result.put("lastWeekTopThree", lastWeekTopThree);
		result.put("gameUserNumber", userBetInfo.size());
		result.put("roomInfo", betOptionsInfo);
		return result;
	}


	public JSONObject pushResult(int type, String userId, String bet, BigDecimal amount) {
		JSONObject pushResult = new JSONObject();
		pushResult.put("type", type);
		pushResult.put("userId", userId);
		pushResult.put("gameId", GameTypeEnum.battleRoyale.getValue());

		String roomId = "";
		if (bet != null) {
			roomId = String.valueOf(bet);
			if ("null".equalsIgnoreCase(roomId)) {
				roomId = "";
			}
		}

		// type=2/3 时，如果 roomId 为空，反查用户所在房间（避免空 roomId）
		if ((type == 2 || type == 3) && (roomId == null || roomId.length() == 0)) {
			try {
				if (roomList != null) {
					for (String rid : roomList.keySet()) {
						Map<String, JSONObject> m = roomList.get(rid);
						if (m != null && m.containsKey(userId)) {
							roomId = String.valueOf(rid);
							break;
						}
					}
				}
			} catch (Exception ignore) {
			}
		}
		pushResult.put("roomId", roomId);

		String name = "";
		try {
			if (players != null && players.containsKey(userId)) {
				Map<String, String> p = players.get(userId);
				if (p != null && p.get("userName") != null) {
					name = p.get("userName");
				}
			}
		} catch (Exception ignore) {
		}

		if (name == null || name.length() == 0) {
			try {
				// 先查当前 roomId
				if (roomList != null && roomId != null && roomId.length() > 0) {
					Map<String, JSONObject> m = roomList.get(roomId);
					if (m != null) {
						JSONObject info = m.get(userId);
						if (info != null) {
							name = info.getString("userName");
							if (name == null || name.length() == 0) {
								name = info.getString("name");
							}
						}
					}
				}
				// 再全房间扫描兜底
				if (name == null || name.length() == 0) {
					if (roomList != null) {
						for (String rid : roomList.keySet()) {
							Map<String, JSONObject> m = roomList.get(rid);
							if (m != null && m.containsKey(userId)) {
								JSONObject info = m.get(userId);
								if (info != null) {
									name = info.getString("userName");
									if (name == null || name.length() == 0) {
										name = info.getString("name");
									}
								}
								break;
							}
						}
					}
				}
			} catch (Exception ignore) {
			}
		}

		if (name == null) {
			name = "";
		}

		if (type == 1) {
			// 下注 / 更换房间
			pushResult.put("name", name);
			pushResult.put("betAmount", amount);
		} else if (type == 2 || type == 3) {
			// 离开房间 / 加入房间
			pushResult.put("name", name);
		}

		pushResult.put("roomInfo", betOptionsInfo);
		return pushResult;
	}




	public String getResult() {
		return result;
	}



	public void setResult(String result) {
		this.result = result;
	}



	public JSONObject getSettleDate() {
		return settleDate;
	}



	public void setSettleDate(JSONObject settleDate) {
		this.settleDate = settleDate;
	}


	public Map<String, String> getUserCheckNum() {
		return userCheckNum;
	}

	public void setUserCheckNum(Map<String, String> userCheckNum) {
		this.userCheckNum = userCheckNum;
	}

	public Map<String, BigDecimal> getUserBetAmount() {
		return userBetAmount;
	}

	public void setUserBetAmount(Map<String, BigDecimal> userBetAmount) {
		this.userBetAmount = userBetAmount;
	}

	public String getLastResult() {
		return lastResult;
	}

	public void setLastResult(String lastResult) {
		this.lastResult = lastResult;
	}

	public Integer getNextResult() {
		return nextResult;
	}

	public void setNextResult(Integer nextResult) {
		this.nextResult = nextResult;
	}

	public Long getReadyTime() {
		return readyTime;
	}

	public void setReadyTime(Long readyTime) {
		this.readyTime = readyTime;
	}

	public Map<String, Double> getLastWeekTopThree() {
		return lastWeekTopThree;
	}

	public void setLastWeekTopThree(Map<String, Double> lastWeekTopThree) {
		this.lastWeekTopThree = lastWeekTopThree;
	}

	public void setBet(String bet) {
		this.bet = bet;
	}

	public String getBet() {
		return bet;
	}
}

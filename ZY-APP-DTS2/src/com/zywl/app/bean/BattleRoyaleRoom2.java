package com.zywl.app.bean;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.zywl.app.defaultx.enmus.GameTypeEnum;

import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.BaseBean;

public class BattleRoyaleRoom2 extends BaseBean{

	public  Map<String,Double> lastWeekTopThree = new ConcurrentHashMap<>();

	private  Long readyTime = System.currentTimeMillis();

	private ConcurrentHashMap<String, Map<String, String>> players;

	//上一期的结果
	private String lastResult;

	//下一期的结果
	public static List<Integer> nextResult;
	
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
	private List<Integer> result;

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

	
	
	public BattleRoyaleRoom2() {
		status = 1;
		players= new ConcurrentHashMap<>();
		userBetInfo= new ConcurrentHashMap<>();
		betOptionsInfo= new ConcurrentHashMap<>();
		userBetOrderInfo= new ConcurrentHashMap<>();
		history100Reuslt=new JSONObject();
		history20Reuslt=new JSONObject();
		roomList = new ConcurrentHashMap<>();
		for (int i =0; i < option; i++) {
			roomList.put(String.valueOf(i), new ConcurrentHashMap<>());
		}
		lookList = new ConcurrentHashMap<>();
		allBetAmount=BigDecimal.ZERO;
		settleDate = new JSONObject();
	}
	
	public BattleRoyaleRoom2(int option) {
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
		result.put("beginTime", String.valueOf( beginTime));
		result.put("endTime",  String.valueOf(endTime));
		result.put("roomList", roomList);
		result.put("lookList", new ConcurrentHashMap<String, Map<String,Object>>());
		result.put("lastResult",lastResult);
		result.put("periodsNum",getPeridosNum());
		result.put("lastWeekTopThree",lastWeekTopThree);
		return result;
	}
	
	public JSONObject pushResult(int type,String userId,String bet,BigDecimal amount) {
		JSONObject pushResult = new JSONObject();
		pushResult.put("type", type);
		pushResult.put("userId", userId);
		pushResult.put("gameId", GameTypeEnum.dts2.getValue());
		if (type==1) {
			//下注 更换房间
			pushResult.put("roomId", bet);
			pushResult.put("name", players.containsKey(userId)? players.get(userId).get("userName"):"");
			pushResult.put("betAmount", amount);
		}else if(type==2 || type==3) {
			// 离开房间 或者 加入房间
			pushResult.put("name",players.containsKey(userId)? players.get(userId).get("userName"):"");
		}
		return pushResult;
	}


	public List<Integer> getResult() {
		return result;
	}

	public void setResult(List<Integer> result) {
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

	public static List<Integer> getNextResult() {
		return nextResult;
	}

	public static void setNextResult(List<Integer> nextResult) {
		BattleRoyaleRoom2.nextResult = nextResult;
	}

	public Long getReadyTime() {
		return readyTime;
	}

	public void setReadyTime(Long readyTime) {
		this.readyTime = readyTime;
	}

	public  Map<String, Double> getLastWeekTopThree() {
		return lastWeekTopThree;
	}

	public  void setLastWeekTopThree(Map<String, Double> lastWeekTopThree) {
		this.lastWeekTopThree = lastWeekTopThree;
	}
}

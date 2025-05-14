package com.zywl.app.room;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.bean.Command;
import com.live.app.ws.interfacex.Listener;
import com.live.app.ws.socket.BaseClientSocket;
import com.zywl.app.bean.FoodGameConfig;
import com.zywl.app.bean.FoodInfo;
import com.zywl.app.bean.UserInfo;
import com.zywl.app.defaultx.enmus.LogCapitalTypeEnum;


public class FoodRoom {
	private RoomManager roomManager;
	public Timer m_timer;
	private int m_roomid;
	private int m_roomstate;
	private int m_minplayer = 2;
	private int m_maxplayer = 8;
	private int m_costtype = 0;
	private int m_maxround = 3;
	private int m_curround = 0;
	private BigDecimal m_costsit = BigDecimal.ZERO;
	private BigDecimal m_commissionrate = BigDecimal.ZERO;

	/**
	 * 观战玩家 <用户ID，玩家实例>
	 */
	private Map<String,FoodPlayer> m_watchplayers = new ConcurrentHashMap<>();
	/**
	 * 等待退出玩家 <用户ID，玩家实例>
	 */
	private ArrayList<String> m_waitexit = new ArrayList<>();
	
	private int READYDELAYTIME = 10000;
	private int STARTDELAYTIME = 5000;
	private int OPERATIME = 5000;
	private int OPERENDTIME = 10000;
	private int ENDDELAYTIME = 10000;
	
	private Long m_starttime = 0L;

	private FoodPlayer[] m_joinplayers;
	private boolean[] m_optag;
	
	public FoodRoom init(int roomid, FoodGameConfig cfg, RoomManager roomManager) {
		this.roomManager = roomManager;
		m_roomstate = GameState._IDLE;
		m_timer = new Timer();
		m_roomid = roomid;
		m_minplayer = cfg.getMinPlayer();
		m_maxplayer = cfg.getMaxPlayer();
		m_maxround = cfg.getMaxRound();
		m_costtype = cfg.getCostType();
		m_costsit = cfg.getCostSit();
		m_commissionrate = cfg.getCommission();
		
		READYDELAYTIME = cfg.getReadyDelayTime();
		STARTDELAYTIME = cfg.getStartDelayTime();
		ENDDELAYTIME = cfg.getEndDelayTime();

		m_joinplayers = new FoodPlayer[m_maxplayer];
		m_optag = new boolean[m_maxplayer];
		for (int i = 0; i < m_maxplayer; i++) {
			m_joinplayers[i] = null;
			m_optag[i] = false;
		}
		return this;
	}
		
	
	/** 用户进入
	 * @param uid 用户id
	 */
	public void onEnter(String uid, String userName, String headUrl, String userNo) {
		if(!isInRoom(uid)) {
			FoodPlayer player = new FoodPlayer();
			player.setChairID(-1);
			player.setUserID(uid);
			player.setNickName(userName);
			headUrl = headUrl == null ? "" : headUrl;
			player.setHeadUrl(headUrl);
			player.setUserNo(userNo);
			m_watchplayers.put(uid, player);
		}
		if(m_waitexit.contains(uid)) {
			m_waitexit.remove(uid);
		}
	}
	
	/** 坐下
	 * @param uid
	 * @param chairid 座位ID 小于0自动分配ID，大于等于0指定座位ID
	 */
	public synchronized Integer onSitDown(String uid, Integer chairid) {
		if(m_roomstate >= GameState._START) {
			return -1;
		}
		
		if(chairid < 0 && chairid > this.m_maxplayer - 1) {
			return -2;
		}

		for ( FoodPlayer player : m_joinplayers) {
			if(player == null) {
				continue;
			}
			if(player.getUserID().equals(uid)) {
				return -4;
			}
		}

		if(chairid >= 0) {
			if(!chairIsFree(chairid)) {
				return -5;
			}
		} else {
			chairid = getFreeChairID();
			if(chairid < 0) {
				return -6;
			}
		}
		
		FoodPlayer player;
		if(m_watchplayers.containsKey(uid)) {
			player = m_watchplayers.get(uid);
			m_watchplayers.remove(uid);
		}else {
			player = new FoodPlayer();
		}
		
		player.setChairID(chairid);
		player.addBetScore(m_costsit);
		m_joinplayers[chairid] = player;
		roomManager.setRoomWait(m_roomid);
//		checkStart();
		removeWatchPlayer(uid);
		return 0;
	}

	public boolean standUp(int chairid) {
		if(m_roomstate >= GameState._READY) {
			return false;
		}
		FoodPlayer player = m_joinplayers[chairid];
		if(player != null) {
			m_watchplayers.put(player.getUserID(),player);
			m_joinplayers[chairid] = null;
		}
		return true;
	}
	
	public Boolean chairIsFree(Integer chairid) {
		return m_joinplayers[chairid] == null;
	}
	
	public synchronized Integer getFreeChairID() {
		for (int i = 0; i < m_maxplayer; i++) {
			if(m_joinplayers[i] == null) {
				return i;
			}
		}
		return -1;
	}
	
	public Integer getChairIDByUid(String uid) {
		for (int i = 0; i < m_maxplayer; i++) {
			if(m_joinplayers[i] == null) {
				continue;
			}
			if(m_joinplayers[i].getUserID().equals(uid)) {
				return i;
			}
		}
		return -1;
	}

	/** 请求加菜
	 * @param uid
	 */
	public synchronized int opFood(String uid, boolean pass) {
		if(m_roomstate != GameState._OPERA) {
			return -1;
		}
		
		int card = 0;
		for (FoodPlayer player : m_joinplayers) {
			if(player == null) {
				continue;
			}
			if (player.getUserID().equals(uid)) {
				int chairId = player.getChairID();
				if (m_optag[chairId]) {
					return -2;
				}

				if(pass) {
//					player.addCard(0);
					m_optag[chairId] = true;
					break;
				}
				card = FoodLogic.dispatchOne(m_curround);
				if (card < 0) {
					return -3;//获取失败退出，理论不可能出现
				}
				player.addCard(card);
				m_optag[chairId] = true;
				break;
			}
		}

		int cnt = 0;
		for (int i = 0; i < m_maxplayer; i++) {
			if(m_joinplayers[i] == null) {
				continue;
			}
			cnt++;
			if(m_optag[i]) {
				cnt--;
			}
		}
		if(cnt <= 0 && m_roomstate == GameState._OPERA) {
			m_timer.cancel();
			m_timer = new Timer();
			gameOperaEnd();
		}
		
		return card;
	}
	
	public void checkStart() {
		int cnt = 0;
		for (FoodPlayer player : m_joinplayers) {
			if (player != null) {
				cnt++;
			}
		}
		if(cnt >= m_minplayer) {
			if(m_roomstate >= GameState._READY) {return;}
			m_roomstate = GameState._READY;

			m_timer = new Timer();
			//由于消息返回顺序问题做延时处理
			m_timer.schedule(new TimerTask() {
				public void run() {
					gameReady();
				}
			}, 100);
		}else {
			m_roomstate = GameState._IDLE;
			m_timer.cancel();
		}
	}

	private void gameReady() {
		m_starttime = System.currentTimeMillis();
		
		//通知用户 游戏准备
		ArrayList<String> noticeList = getPushUID(null);
		JSONObject data = getStatusData();
		data.put("endTime",READYDELAYTIME + System.currentTimeMillis());
		roomManager.getFoodservice().noticeGameStatus(noticeList, data);
		
		m_timer.schedule(new TimerTask() {
			public void run() {
//					m_timer.cancel();
					gameStart();
			}
		}, READYDELAYTIME);
	}
	
	private void gameStart() {
		m_roomstate = GameState._START;
		m_starttime = System.currentTimeMillis();
		FoodLogic.Init();
		m_curround++;


		for (FoodPlayer player : m_joinplayers) {
			if(player == null) { continue;}
			player.addCard(FoodLogic.dispatchOne(m_curround));
			player.addCard(FoodLogic.dispatchOne(m_curround));
		}
		roomManager.setRoomStart(m_roomid);
		sendGameInfo(STARTDELAYTIME);

		m_timer.schedule(new TimerTask() {
			public void run() {
//				m_timer.cancel();
//				checkOp(m_curround);
				gameOpera();
			}
		}, this.STARTDELAYTIME);
	}

	private void sendGameInfo(int startdelaytime) {
		int max = m_maxplayer;
		if(m_watchplayers.size() > 0) {
			max++;
		}
		for (int i = 0; i < max; i++) {
			if(i < m_maxplayer && m_joinplayers[i] == null) { continue;}

			JSONArray arr = new JSONArray();
			for (int j = 0; j < m_maxplayer; j++) {
				if(m_joinplayers[j] == null) { continue;}
				ArrayList<Integer> cards = i == j ? m_joinplayers[j].getAllCard() : m_joinplayers[j].getDisplayCard();
				FoodInfo foodInfo = new FoodInfo();
				foodInfo.setUserId(m_joinplayers[j].getUserID());
				foodInfo.setChairId(m_joinplayers[j].getChairID());
				foodInfo.setFoodInfo(cards);
				arr.add(foodInfo);
			}

			ArrayList<String> noticeList = new ArrayList<>();
			if(i < m_maxplayer) {
				noticeList.add(m_joinplayers[i].getUserID());
			}else {
				for (Map.Entry entry : m_watchplayers.entrySet()) {
					noticeList.add(entry.getKey().toString());
				}
			}
			JSONObject data = getStatusData();
			data.put("endTime", startdelaytime + System.currentTimeMillis());
			data.put("round",m_curround);
			data.put("foodInfo",arr);
			roomManager.getFoodservice().noticeGameStatus(noticeList, data);
		}
	}

	private void gameOpera() {
		m_curround++;
		m_roomstate = GameState._OPERA;
		m_starttime = System.currentTimeMillis();
		sendGameInfo(OPERATIME);
		m_timer.schedule(new TimerTask() {
			public void run() {
				checkOp(m_curround);
				gameOperaEnd();
			}
		}, OPERATIME);
	}

	private void gameOperaEnd() {
		m_roomstate = GameState._OPERAEND;
		m_starttime = System.currentTimeMillis();
		sendGameInfo(OPERENDTIME);

		m_timer.schedule(new TimerTask() {
			public void run() {
				for (int i = 0; i < m_maxplayer; i++) {
					m_optag[i] = false;
				}
				if(m_curround < m_maxround) {
					gameOpera();
				}else {
					gameEnd();
				}
			}
		}, OPERENDTIME);
	}

	private void checkOp(int round) {
		for (int i = 0; i < m_maxplayer; i++) {
			if(!m_optag[i]) {
				m_optag[i] = true;
			}
		}
	}

	private void gameEnd() {
		m_roomstate = GameState._END;
		m_starttime = System.currentTimeMillis();

		roomManager.setRoomIdle(m_roomid);
		int[] allscore = new int[m_maxplayer];
		int maxscore = 0;
		BigDecimal allbetscore = BigDecimal.ZERO;
		for (int i = 0; i < m_joinplayers.length; i++) {
			if(m_joinplayers[i] == null) {
				allscore[i] = 0;
				continue;
			}
			allscore[i] = FoodLogic.calcScore(m_joinplayers[i].getAllCard());
			if(maxscore < allscore[i]) {
				maxscore = allscore[i];
			}
			allbetscore = allbetscore.add(m_costsit);
		}

		ArrayList<String> winarr = new ArrayList();
		for (int i = 0; i < allscore.length; i++) {
			if(m_joinplayers[i] == null) {
				continue;
			}
			if(allscore[i] == maxscore) {
				m_joinplayers[i].setWinScore(maxscore);
				winarr.add(m_joinplayers[i].getUserID());
			}
		}

		BigDecimal winscore = allbetscore.subtract( m_commissionrate.multiply(allbetscore) );
		winscore =  winscore.divide( new BigDecimal(String.valueOf(winarr.size())) );

		JSONArray infoArr = new JSONArray();
		for (int i = 0; i < allscore.length; i++) {
			if(m_joinplayers[i] == null) {
				continue;
			}

			ArrayList<Integer> cards = m_joinplayers[i].getAllCard();
			JSONObject userInfo = new JSONObject();
			userInfo.put("userId", m_joinplayers[i].getUserID());
			userInfo.put("chairId", m_joinplayers[i].getChairID());
			userInfo.put("foodInfo",cards);
			userInfo.put("score", allscore[i]);
			userInfo.put("winScore",allscore[i] == maxscore ? winscore : BigDecimal.ZERO);
			infoArr.add(userInfo);

			String uid = m_joinplayers[i].getUserID();
			/*if(allscore[i] == maxscore) {
				try {
					BigDecimal finalWinscore = winscore;
					roomManager.getFoodservice().costMoney(uid,
							LogCapitalTypeEnum.game_bet_win_food.getValue(),
							m_costtype,
							winscore,
							new Listener() {
								@Override
								public void handle(BaseClientSocket clientSocket, Command command) {
									if (!command.isSuccess()) {
										roomManager.getFoodservice().getLogger().error("结算失败 uid:" + uid + " roomid:" + m_roomid  + " amount:" + finalWinscore);
									}
								}
							});
				}catch (Exception e) {
					e.printStackTrace();
					roomManager.getFoodservice().getLogger().error("结算异常 uid:" + uid + " roomid:" + m_roomid  + " amount:" + winscore);
				}
			}*/
			int winLose = allscore[i] == maxscore ? 1 : 0;
			roomManager.getFoodservice().recordInfo(Long.parseLong(uid),m_roomid,m_costsit,winscore,cards.toString(),maxscore,winLose,winarr.toString());
		}

		roomManager.getFoodservice().foodGameSettle(m_costtype, infoArr, new Listener() {
			@Override
			public void handle(BaseClientSocket clientSocket, Command command) {
				if (!command.isSuccess()) {
					roomManager.getFoodservice().getLogger().error("结算失败 roomid:" + m_roomid  + " info:" + infoArr.toJSONString());
				}
			}
		});
		// 通知用户游戏结束
		ArrayList<String> noticeList = getPushUID(null);
		JSONObject data = getStatusData();
		data.put("endTime",ENDDELAYTIME + System.currentTimeMillis());
		data.put("resultInfo", infoArr);
		roomManager.getFoodservice().noticeGameStatus(noticeList, data);
		m_timer.schedule(new TimerTask() {
			public void run() {
//				m_timer.cancel();
				reSet();
				// 通知用户空闲时间
				ArrayList<String> noticeList = getPushUID(null);
				JSONObject jdata = getStatusData();
				jdata.put("endTime",0);
				roomManager.getFoodservice().noticeGameStatus(noticeList, jdata);
			}
		}, ENDDELAYTIME);
	}
	
	private void reSet() {
		m_roomstate = GameState._IDLE;
		m_curround = 0;
		for (int i = 0; i < m_maxplayer; i++) {
			if( m_joinplayers[i] == null ) {
				continue;
			}
			m_joinplayers[i].clearData();
			m_watchplayers.put(m_joinplayers[i].getUserID(), m_joinplayers[i]);
			m_joinplayers[i] = null;
			m_optag[i] = false;
		}

		for (String uid : m_waitexit) {
			roomManager.getFoodservice().removePlayerCache(uid);
		}
		m_waitexit.clear();
		m_timer.cancel();
	}
	
	public Boolean isJoinGame(String uid) {
		for (FoodPlayer player : m_joinplayers) {
			if (player == null) {
				continue;
			}

			if (player.getUserID().equals(uid)) {
				return true;
			}
		}
		return false;
	}
	
	public Boolean isInRoom(String uid) {
		if (uid==null)return false;
		return m_watchplayers.containsKey(uid) || isJoinGame(uid);
	}

	public int getCostType() {
		return m_costtype;
	}

	public BigDecimal getCostMoney() {
		return m_costsit;
	}
	
	public Boolean isCanJoin() {
		if(m_roomstate >= GameState._START) {
			return false;
		}

		if(m_joinplayers.length < m_maxplayer) {
			return true;
		}

		for (FoodPlayer player : m_joinplayers) {
			if (player == null) {
				return true;
			}
		}

		return false;
	}

	public ArrayList<String> getPushUID(String filter) {
		ArrayList<String> arr = new ArrayList<String>();
		m_watchplayers.forEach((k,v) -> {
			if(!k.equals(filter)) {
				arr.add(k);
			}
		});

		for (FoodPlayer player : m_joinplayers) {
			if (player == null) {
				continue;
			}
			String uid = player.getUserID();
			if (uid.equals(filter)) {
				continue;
			}

			arr.add(uid);
		}

		return arr;
	}

	public int getJoinCount() {
		int cnt = 0;
		for (FoodPlayer player : m_joinplayers) {
			if (player != null) {
				cnt++;
			}
		}
		return cnt;
	}

	public void exitRoom(String uid) {
		m_watchplayers.remove(uid);
		if(isJoinGame(uid)) {
			if(!m_waitexit.contains(uid)) {
				m_waitexit.add(uid);
			}
		}else{
			roomManager.getFoodservice().removePlayerCache(uid);
		}
	}

	public void removeWatchPlayer(String uid) {
		m_watchplayers.remove(uid);
//		if(m_roomstate >= GameState._READY) {
//
//		}else {
//			for (int i = 0; i < m_joinplayers.length; i++) {
//				if(m_joinplayers[i].getUserID() == uid) {
//					m_joinplayers[i] = null;
//					return;
//				}
//			}
//		}
	}

	public Object getPlayerInfo(String uid) {
		for (FoodPlayer player : m_joinplayers) {
			if (player == null) {
				continue;
			}
			if(player.getUserID().equals(uid)) {
				UserInfo userInfo = new UserInfo();
				userInfo.setUserId(player.getUserID());
				userInfo.setChairId(player.getChairID());
				userInfo.setUserName(player.getNickName());
				userInfo.setHeadUrl(player.getHeadUrl());
				userInfo.setUserNo(player.getUserNo());
				return userInfo;
			}
		}
		return  null;
	}

	public JSONObject getRoomInfo(String uid) {
		Long endtime = getCurEndTIme();
		JSONObject info = new JSONObject();
		JSONArray userArr = new JSONArray();
		JSONArray resultInfo = new JSONArray();
		for (FoodPlayer player : m_joinplayers) {
			if (player == null) {
				continue;
			}

			UserInfo userInfo = new UserInfo();
			userInfo.setUserId(player.getUserID());
			userInfo.setChairId(player.getChairID());
			userInfo.setUserName(player.getNickName());
			userInfo.setHeadUrl(player.getHeadUrl());
			userInfo.setUserNo(player.getUserNo());
			userArr.add(userInfo);

			JSONObject foodInfo = new JSONObject();
			foodInfo.put("userId", player.getUserID());
			foodInfo.put("chairId", player.getChairID());

			if(m_roomstate == GameState._END){
				foodInfo.put("foodInfo", player.getAllCard());
				foodInfo.put("score",FoodLogic.calcScore(player.getAllCard()));
				foodInfo.put("winScore",player.getWinScore());
			}else {
				foodInfo.put("foodInfo", player.getUserID().equals(uid) ? player.getAllCard() : player.getDisplayCard());
			}
			resultInfo.add(foodInfo);

			if (player.getUserID().equals(uid)) {
				info.put("chairId", player.getChairID());
				if(m_roomstate == GameState._OPERA){
					info.put("operaState",m_optag[player.getChairID()] ? 1 : 0);
				}
				info.put("betScore", player.getBetScore());
				info.put("foods", player.getAllCard());
			}
		}

		info.put("roomId",m_roomid);
		info.put("userInfo", userArr);
		info.put("sitCost",m_costsit);
		if(m_roomstate == GameState._END){
			info.put("resultInfo",resultInfo);
		}else {
			info.put("foodInfo",resultInfo);
		}
		info.put("gameState", m_roomstate);
		info.put("endTime", endtime);

		return info;
	}

	private long getCurEndTIme() {
		long endtime = 0;
		if(m_roomstate == GameState._READY) {
			endtime = m_starttime + READYDELAYTIME;
		}else if(m_roomstate == GameState._START) {
			endtime = m_starttime + STARTDELAYTIME;
		}else if(m_roomstate == GameState._OPERA) {
			endtime = m_starttime + OPERATIME;
		}else if(m_roomstate == GameState._OPERAEND) {
			endtime = m_starttime + OPERENDTIME;
		}else if(m_roomstate == GameState._END) {
			endtime = m_starttime + ENDDELAYTIME;
		}else {
			endtime = 0L;
		}
		return endtime;
	}

	private JSONObject getStatusData() {
		long endtime = getCurEndTIme();
		JSONObject info = new JSONObject();
		info.put("roomId",m_roomid);
		info.put("gameState", m_roomstate);
		info.put("endTime", endtime);

		return info;
	}

	public int getRoomId() {
		return  m_roomid;
	}

	public void clearAll() {
		if(m_roomstate > GameState._IDLE) {
			return;
		}

		for (int i = 0; i < m_maxplayer; i++) {
			if( m_joinplayers[i] == null ) {
				continue;
			}
			String uid = m_joinplayers[i].getUserID();
			roomManager.getFoodservice().removePlayerCache(uid);
			roomManager.getFoodservice().returnMoney(m_roomid, uid, LogCapitalTypeEnum.food_cancel_bet.getValue(), m_costtype, m_costsit);
			m_joinplayers[i] = null;
			m_optag[i] = false;
		}
		m_waitexit.clear();
		m_timer.cancel();
	}
}

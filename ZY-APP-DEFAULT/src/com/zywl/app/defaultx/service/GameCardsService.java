package com.zywl.app.defaultx.service;

import com.alibaba.fastjson2.JSONArray;
import com.zywl.app.base.bean.Game;
import com.zywl.app.base.bean.card.GameCards;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GameCardsService extends DaoService {

	public GameCardsService() {
		super("GameCardsMapper");
		// TODO Auto-generated constructor stub
	}


	private static final Log logger = LogFactory.getLog(GameCardsService.class);
	
	
	
	
	@Transactional
	public GameCards beginGame(Long userId, JSONArray cardsInfo,int type){
		GameCards gameCards = new GameCards();
		gameCards.setCardsInfo(cardsInfo);
		gameCards.setUserId(userId);
		gameCards.setStatus(1);
		gameCards.setTotalTurns(0);
		gameCards.setType(type);
		gameCards.setTrapCount(0);
		gameCards.setCreateTime(new Date());
		gameCards.setUpdateTime(new Date());
		save(gameCards);
		return gameCards;
	}
	
	

	@Transactional
	public void updateGameCards(GameCards gameCards){
		execute("updateGameCards",gameCards);
	}
	


	public List<GameCards> findGamingByUserId(Long userId){
		Map<String,Object> params = new HashMap<>();
		params.put("userId",userId);
		return  findList("findGamingByUserId",params);
	}

	public GameCards findByUserIdAndType(Long userId,int type){
		Map<String,Object> params = new HashMap<>();
		params.put("userId",userId);
		params.put("type",type);
		return (GameCards) findOne("findByUserIdAndType",params);
	}


	public List<GameCards> findRecord(Long userId){
		Map<String,Object> params = new HashMap<>();
		params.put("userId",userId);
		return findList("findRecord",params);
	}

	
}

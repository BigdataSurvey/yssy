package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.Game;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class GameService extends DaoService {

	public GameService() {
		super("GameMapper");
		// TODO Auto-generated constructor stub
	}


	private static final Log logger = LogFactory.getLog(GameService.class);
	
	
	
	
	
	
	
	public List<Game> findAllGame() {
		return findAll();
	}
	
	
	
	public Game findGameById(Long id) {
		Map<String, Object> params = new HashedMap<String, Object>();
		params.put("id", id);
		return findOne(params);
	}

	@Transactional
	public void updateGameStatus(int gameId,int status){
		Map<String, Object> params = new HashedMap<String, Object>();
		params.put("id", gameId);
		params.put("status",status);
		execute("updateGameStatus",params);
	}
	

	@Override
	protected Log logger() {
		return logger;
	}
	
	
}

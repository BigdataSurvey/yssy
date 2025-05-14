package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.GameLotteryResult;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GameLotteryResultService extends DaoService {

	public GameLotteryResultService() {
		super("GameLotteryResultMapper");
		// TODO Auto-generated constructor stub
	}


	private static final Log logger = LogFactory.getLog(GameLotteryResultService.class);
	
	
	
	/**
	 * 增加开奖记录
	 * @param
	 */
	@Transactional
	public void addGameLotteryResult(Long gameId,String periodsNum,String lotteryResult) {
		GameLotteryResult result = new GameLotteryResult();
		result.setGameId(gameId);
		result.setPeriodsNum(periodsNum);
		result.setLotteryResult(lotteryResult);
		result.setCreateTime(new Date());
		
		int a = save(result);
		if (a<1) {
			throwExp("开奖失败");
		}
	}
	
	
	public GameLotteryResult findByGameIdAndPeriodsNum(Long gameId,String periodsNum) {
		Map<String, Object> params = new HashedMap<String, Object>();
		params.put("gameId", gameId);
		params.put("periodsNum", periodsNum);
		List<GameLotteryResult> list = findList("findByGameIdAndPeriodsNum",params);
		if (list!=null && list.size()>0) {
			return list.get(0);
		}
		return  null;
	}
	
	
	public GameLotteryResult findPeriodsNum(int gameId) {
		Map<String, Object> params = new HashedMap<String, Object>();
		params.put("gameId", gameId);
		return (GameLotteryResult) findOne("findPeriodsNum", params);
	}
	
	public List<GameLotteryResult> findHistoryResultByGameId(Long gameId,int limit) {
		Map<String, Object> params = new HashedMap<String, Object>();
		params.put("gameId", gameId);
		params.put("limit", limit);
		return findList("findHistoryResultByGameId", params);
	}
	
	//开奖
	@Transactional
	public int drawLottery(Long gameId,String periodsNum,String lottryResult,BigDecimal playerBet,
			BigDecimal playerProfit,BigDecimal winLose,int allTakeNum,int winNum,int loseNum,int status) {
		GameLotteryResult result = new GameLotteryResult();
		result.setGameId(gameId);
		result.setPeriodsNum(periodsNum);
		result.setAllTakeNum(allTakeNum);
		result.setCreateTime(new Date());
		result.setLoseNum(loseNum);
		result.setLotteryResult(lottryResult);
		result.setPlayerBet(playerBet);
		result.setPlayerProfit(playerProfit);
		result.setWinLose(winLose);
		result.setWinNum(winNum);
		result.setStatus(status);
		return save(result);
		
	}
	
	@Transactional
	public int updateLotteryStatus(Long gameId,String periodsNum,String result) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("gameId", gameId);
		params.put("periodsNum", periodsNum);
		params.put("lotteryResult", result);
		params.put("status", 1);
		return execute("drawLottery", params);
	}
	

	@Override
	protected Log logger() {
		return logger;
	}
	
	
}

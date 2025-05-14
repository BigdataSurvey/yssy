package com.zywl.app.defaultx.service.card;

import com.zywl.app.base.bean.ApplyFor;
import com.zywl.app.base.bean.card.GameEscortRecord;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GameEscortRecordService extends DaoService{

	public GameEscortRecordService( ) {
		super("GameEscortRecordMapper");
	}



	@Transactional
	public Long addRecord(Long userId,BigDecimal amount,String orderNo) {
		GameEscortRecord gameEscortRecord = new GameEscortRecord();
		gameEscortRecord.setUserId(userId);
		gameEscortRecord.setGameOrder(orderNo);
		gameEscortRecord.setBeginNumber(100);
		gameEscortRecord.setNowNumber(100);
		gameEscortRecord.setNowCheckpoint(1);
		gameEscortRecord.setAmount(amount);
		gameEscortRecord.setCreateTime(new Date());
		gameEscortRecord.setUpdateTime(new Date());
		gameEscortRecord.setGameStatus(1);// 1进行中   0 已结束
		save(gameEscortRecord);
		return gameEscortRecord.getId();
	}
	
	

	@Transactional
	public int updateRecord(GameEscortRecord record){
		int updateGameRecord = execute("updateGameRecord", record);
		if (updateGameRecord<1){
			throwExp("游戏异常，请稍后再试");
		}
		return updateGameRecord;
	}

	public GameEscortRecord findByUserId(Long userId){
		Map<String,Object> params = new HashMap<>();
		params.put("userId",userId);
		return (GameEscortRecord) findOne("findByUserId",params	);
	}

	public List<GameEscortRecord> findRecordByUserId(Long userId){
		Map<String,Object> params = new HashMap<>();
		params.put("userId",userId);
		return findList("findRecordByUserId",params	);
	}

}

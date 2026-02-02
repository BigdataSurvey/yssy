package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.Game;
import com.zywl.app.base.bean.User;
import com.zywl.app.base.bean.UserNsPrize;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserNsPrizeService extends DaoService {

	public UserNsPrizeService() {
		super("UserNsPrizeMapper");
	}



	
	@Transactional
	public UserNsPrize addUserNsPrize(Long userId, int round, Long nsId, BigDecimal amount){
		UserNsPrize userNsPrize = new UserNsPrize()	;
		userNsPrize.setUserId(userId);
		userNsPrize.setCreateTime(new Date());
		userNsPrize.setRound(round);
		userNsPrize.setAmount(amount);
		userNsPrize.setStatus(0);
		userNsPrize.setNsId(nsId);
		insert(userNsPrize);
		return userNsPrize;
	}
	
	
	
	@Transactional
	public UserNsPrize updateUserNsPrize(Long userId,Long nsId,BigDecimal amount,int round){
		Map<String, Object> params = new HashedMap<>();
		params.put("userId", userId);
		params.put("round", round);
		params.put("nsId", nsId);
		params.put("amount", amount);
		int a = execute("updateUserPrize",params);
		if (a<1){
			return addUserNsPrize(userId,round,nsId,amount);
		}
		return null;
	}


	public BigDecimal findByRoundAndNsId(Long nsId,int round) {
		Map<String, Object> params = new HashedMap<>();
		params.put("nsId", nsId);
		params.put("round", round);
		List<UserNsPrize> findByUserIdAndRound = findList("findByRoundAndNsId", params);
		BigDecimal allAmount = BigDecimal.ZERO;
		for (UserNsPrize userNsPrize : findByUserIdAndRound) {
			allAmount=allAmount.add(userNsPrize.getAmount());
		}
		allAmount = allAmount.divide(new BigDecimal("1.2")).setScale(4,BigDecimal.ROUND_DOWN);
		return allAmount;
	}


	
	public Map<String,UserNsPrize> findByUserIdAndRound(Long userId,int round) {
		Map<String, Object> params = new HashedMap<>();
		params.put("userId", userId);
		params.put("round", round);
		List<UserNsPrize> findByUserIdAndRound = findList("findByUserIdAndRound", params);
		Map<String, UserNsPrize> map = new ConcurrentHashMap<>();
		findByUserIdAndRound.forEach(e->map.put(e.getNsId().toString(),e));
		return map;
	}

	@Transactional
	public void updateStatus(Long nsId,int status,int round,Long userId){
		Map<String, Object> params = new HashedMap<String, Object>();
		params.put("nsId", nsId);
		params.put("status",status);
		params.put("round",round);
		params.put("userId",userId);
		execute("updateStatus",params);
	}
	

	@Override
	protected Log logger() {
		return logger;
	}
	
	
}

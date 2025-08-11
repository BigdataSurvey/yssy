package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.Config;
import com.zywl.app.base.bean.UserStatistic;
import com.zywl.app.base.bean.vo.AnimaTopVo;
import com.zywl.app.base.bean.vo.CapitalTopVo;
import com.zywl.app.base.bean.vo.OneJuniorNumTopVo;
import com.zywl.app.base.constant.RedisKeyConstant;
import com.zywl.app.defaultx.cache.AppConfigCacheService;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserStatisticService extends DaoService{
	
	private static final Log logger = LogFactory.getLog(UserStatisticService.class);
	

	@Autowired
	private AppConfigCacheService appConfigCacheService;
	
	public UserStatisticService() {
		super("UserStatisticMapper");
	}

	//初始化个人统计信息
	@Transactional
	public void addUserStatistic(Long userId) {
		UserStatistic userStatistic = new UserStatistic();
		userStatistic.setUserId(userId);
		userStatistic.setAcquireMoney(BigDecimal.ZERO);
		userStatistic.setAcquireRmb(BigDecimal.ZERO);
		userStatistic.setAdvertTimes(0);
		userStatistic.setCashRmb(BigDecimal.ZERO);
		userStatistic.setCashTimes(0);
		userStatistic.setExpendMoney(BigDecimal.ZERO);
		userStatistic.setLoginTimes(0);
		userStatistic.setOneJuniorNum(0);
		userStatistic.setCreateSw(BigDecimal.ZERO);
		userStatistic.setCreateGrandfaAnima(BigDecimal.ZERO);
		userStatistic.setTwoJuniorNum(0);
		userStatistic.setChannelIncome(BigDecimal.ZERO);
		userStatistic.setCreateGrandfaIncome(BigDecimal.ZERO);
		userStatistic.setCreateIncome(BigDecimal.ZERO);
		userStatistic.setCreateAnima2(BigDecimal.ZERO);
		userStatistic.setGetAnima2(BigDecimal.ZERO);
		userStatistic.setCreateGrandfaAnima2(BigDecimal.ZERO);
		userStatistic.setCreateAnima(BigDecimal.ZERO);
		userStatistic.setGetAnima(BigDecimal.ZERO);
		userStatistic.setGetAllIncome(BigDecimal.ZERO);
		userStatistic.setGetIncome(BigDecimal.ZERO);
		userStatistic.setNowChannelIncome(BigDecimal.ZERO);
		int a=save(userStatistic);
		if (a<1) {
			throwExp("初始化个人统计信息失败！");
		}
	}

	public BigDecimal findHisAllFriend(Long userId){
		Map<String,Object> params = new HashMap<>();
		params.put("userId",userId);
		return (BigDecimal) findOne("findHisAllFriend",params);
	}
	
	public UserStatistic findByUserId(Long userId) {
		Map<String, Object> params = new HashedMap<String, Object>();
		params.put("userId", userId);
		UserStatistic findOneByUserId = (UserStatistic) findOne("findOneByUserId", params);
		if (findOneByUserId==null){
			addUserStatistic(userId);
			return  (UserStatistic) findOne("findOneByUserId", params);
		}
		return findOneByUserId;
	}

	public List<UserStatistic> findByIds(List<Long> ids){
		Map<String, Object> params = new HashedMap<String, Object>();
		params.put("ids",ids);
		return findList("findByIds",params);

	}


	public UserStatistic findCreateAnimaByUserId(Long userId) {
		Map<String, Object> params = new HashedMap<String, Object>();
		params.put("userId", userId);
		UserStatistic findOneByUserId = (UserStatistic) findOne("findCreateAnimaByUserId", params);
		if (findOneByUserId==null){
			addUserStatistic(userId);
			return  (UserStatistic) findOne("findCreateAnimaByUserId", params);
		}
		return findOneByUserId;
	}

	public UserStatistic findCreateAnima2ByUserId(Long userId) {
		Map<String, Object> params = new HashedMap<String, Object>();
		params.put("userId", userId);
		UserStatistic findOneByUserId = (UserStatistic) findOne("findCreateAnima2ByUserId", params);
		if (findOneByUserId==null){
			addUserStatistic(userId);
			return  (UserStatistic) findOne("findCreateAnima2ByUserId", params);
		}
		return findOneByUserId;
	}

	public UserStatistic findGetAnimaByUserId(Long userId) {
		Map<String, Object> params = new HashedMap<String, Object>();
		params.put("userId", userId);
		UserStatistic findOneByUserId = (UserStatistic) findOne("findGetAnimaByUserId", params);
		if (findOneByUserId==null){
			addUserStatistic(userId);
			return  (UserStatistic) findOne("findGetAnimaByUserId", params);
		}
		return findOneByUserId;
	}

	public UserStatistic findGetAnima2ByUserId(Long userId) {
		Map<String, Object> params = new HashedMap<String, Object>();
		params.put("userId", userId);
		UserStatistic findOneByUserId = (UserStatistic) findOne("findGetAnima2ByUserId", params);
		if (findOneByUserId==null){
			addUserStatistic(userId);
			return  (UserStatistic) findOne("findGetAnima2ByUserId", params);
		}
		return findOneByUserId;
	}

	public List<CapitalTopVo>  findTop() {
		Map<String, Object> params = new HashedMap<String, Object>();
		Integer number = Integer.parseInt(appConfigCacheService.getConfigByKey(RedisKeyConstant.APP_TOP_NUMBER, Config.TOP_NUMBER)) ;
		params.put("limit",number );
		return findList("findTop", params);
	}



	public void addOneCount(Long userId){
		Map<String, Object> params = new HashedMap<String, Object>();
		params.put("userId",userId);
		execute("updateOneCount",params);
	}
	public void addTwoCount(Long userId){
		Map<String, Object> params = new HashedMap<String, Object>();
		params.put("userId",userId);
		execute("updateTwoCount",params);
	}

	@Transactional
	public int updateUserCreateAnima(Long userId,BigDecimal anima){
		Map<String, Object> params = new HashedMap<String, Object>();
		params.put("userId",userId);
		params.put("anima",anima);
		return execute("updateUserCreateAnima",params);
	}
	@Transactional
	public int updateUserCreateAnima2(Long userId,BigDecimal anima){
		Map<String, Object> params = new HashedMap<String, Object>();
		params.put("userId",userId);
		params.put("anima",anima);
		return execute("updateUserCreateAnima2",params);
	}

	@Transactional
	public int updateUserCreateSw(Long userId,BigDecimal sw){
		Map<String, Object> params = new HashedMap<String, Object>();
		params.put("userId",userId);
		params.put("sw",sw);
		return execute("updateUserCreateSw",params);
	}

	@Transactional
	public int addChannelIncome(Long userId,BigDecimal addIncome){
		Map<String, Object> params = new HashedMap<String, Object>();
		params.put("userId",userId);
		params.put("addIncome",addIncome);
		return execute("addChannelIncome",params);
	}

	public int updateUserCreateGrandfaAnima(Long userId,BigDecimal anima){
		Map<String, Object> params = new HashedMap<String, Object>();
		params.put("userId",userId);
		params.put("anima",anima);
		return execute("updateUserCreateGrandfaAnima",params);
	}

	public int updateUserCreateIncome(Long userId,BigDecimal income,BigDecimal grandfaIncome){
		if (income==null && grandfaIncome==null){
			return -1;
		}
		Map<String, Object> params = new HashedMap<String, Object>();
		params.put("userId",userId);
		params.put("income",income);
		params.put("grandfaIncome",grandfaIncome);
		return execute("updateUserCreateIncome",params);
	}


	public int updateStatic(UserStatistic statistic){
		return execute("updateStatic",statistic);
	}

	public void batchUpdateStatic(List<UserStatistic> userStatistics){
		if (userStatistics != null) {
			List<UserStatistic> newList = new ArrayList<>();
			for (int i = 0; i < userStatistics.size(); i++) {
				newList.add(userStatistics.get(i));
				if (i % 5000 == 0) {
					execute("batchUpdate", newList);
					newList.clear();
				}
			}
			if (!newList.isEmpty()) {
				execute("batchUpdate", newList);
			}
		}
	}

	@Transactional
	public int updateUserGetAnima(Long userId,BigDecimal anima){
		Map<String, Object> params = new HashedMap<>();
		params.put("userId",userId);
		params.put("getAnima",anima);
		return execute("updateUserGetAnima",params);
	}

	@Transactional
	public int updateUserGetAnima2(Long userId,BigDecimal anima){
		Map<String, Object> params = new HashedMap<>();
		params.put("userId",userId);
		params.put("getAnima",anima);
		return execute("updateUserGetAnima2",params);
	}

	public List<AnimaTopVo> findToByAnima(){
		return findList("findToByAnima",null);
	}

	public List<OneJuniorNumTopVo> findToByJuniorNum(){
		return findList("findToByJuniorNum",null);
	}

	public OneJuniorNumTopVo findMyJuniorNum(Long userId){
		Map<String,Object> map = new HashMap<>();
		map.put("userId",userId);
		return (OneJuniorNumTopVo) findOne("findMyJuniorNum",map);
	}

	public List<UserStatistic> findSonNumberByPrizePool(){
		return findList("findSonNumberByPrizePool",null);
	}

	public void updateStaticChannel(UserStatistic userStatistic) {
		Map<String, Object> params = new HashedMap<>();
		params.put("userId",userStatistic.getUserId());
		execute("updateStaticChannel", params);

	}

	@Transactional
	public void addUserStatisticInfo(UserStatistic userStatistic) {
		execute("addStatisticInfo",userStatistic);
	}

	public UserStatistic findEarningByUserId(Long userId) {
		Map<String,Object> params = new HashMap<>();
		params.put("userId",userId);
		return (UserStatistic) findOne("findEarningByUserId",params);
	}

	public void updateNowChannelIncome(UserStatistic userStatistic) {
		execute("updateNowChannelIncome",userStatistic);
	}
}

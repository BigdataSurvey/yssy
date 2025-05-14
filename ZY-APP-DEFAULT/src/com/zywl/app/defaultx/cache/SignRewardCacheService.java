package com.zywl.app.defaultx.cache;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.Sign;
import com.zywl.app.base.bean.SignReward;
import com.zywl.app.base.constant.RedisKeyConstant;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.defaultx.cache.impl.RedisService;
import com.zywl.app.defaultx.service.SignRewardService;
import com.zywl.app.defaultx.service.SignService;

@Service
public class SignRewardCacheService  extends RedisService{

	@Autowired
	private SignRewardService signRewardService;
	
	@Autowired
	private SignService signService;
	
	public List<SignReward> getAllSignReward(){
		String key = RedisKeyConstant.APP_SIGN_REWARD;
		List<SignReward> rewards = getList(key, SignReward.class);
		if (rewards==null) {
			rewards = signRewardService.findAllReward();
			set(key, rewards);
		}
		return rewards;
	}
	
	public JSONArray getRewardInfo() {
		List<SignReward> rewards = getAllSignReward();
		String reward = null;
		for (SignReward signReward : rewards) {
			if (DateUtil.getMonthValue()==signReward.getMonth()) {
				reward = signReward.getContext();
				break;
			}
		}
		return JSON.parseArray(reward);
	}
	
	public JSONArray getTotalRewardInfo() {
		List<SignReward> rewards = getAllSignReward();
		String totalReward = null;
		for (SignReward signReward : rewards) {
			if (DateUtil.getMonthValue()==signReward.getMonth()) {
				totalReward = signReward.getTotalReward();
				break;
			}
		}
		return JSON.parseArray(totalReward);
	}
	
	//获取某天的奖励
	public JSONObject getRewardInfoByDay(int day) {
		JSONArray obj = getRewardInfo();
		return obj.getJSONObject(day-1);
	}
	
	//获取玩家的签到信息
	public Sign  getUserSignInfo(Long userId) {
		String key = RedisKeyConstant.APP_SIGN_REWARD+userId+"-";
		Sign sign = get(key, Sign.class);
		if (sign==null) {
			sign = signService.findUserSign(userId);
			if (sign!=null) {
				set(key, sign, 6000L);
			}
		}
		return sign;
	}
}

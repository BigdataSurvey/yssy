package com.zywl.app.defaultx.cache;

import java.math.BigDecimal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zywl.app.base.bean.Config;
import com.zywl.app.base.constant.RedisKeyConstant;
import com.zywl.app.defaultx.cache.impl.RedisService;
import com.zywl.app.defaultx.service.ConfigService;

@Service
public class AppConfigCacheService extends RedisService {


	@Autowired
	private ConfigService configService;

	// 获取交易行手续费
	public BigDecimal getTradingRate() {
		return getRate(RedisKeyConstant.APP_CONFIG_TRADING_FEE, Config.TRADING_FEE);
	}



	// 获取好友转增手续费

	// 获取提现手续费
	public BigDecimal getCashRate() {
		return getRate(RedisKeyConstant.APP_CASH_FEE, Config.CASH_FEE);
	}





	//移除余额兑换比例
	public void removerConvertRate() {
		del(RedisKeyConstant.APP_CONVERT_RATE, Config.CONVERT_RATE);
	}

	// 移除提现手续费
	public void removeConvertRate() {
		del(RedisKeyConstant.APP_CASH_FEE);
	}

	// 移除交易行手续费
	public void removeTradingRate() {
		del(RedisKeyConstant.APP_CONFIG_TRADING_FEE);
	}


	public BigDecimal getRate(String redisKey, String configKey) {
		BigDecimal fee = null;
		String result = get(redisKey);
		if (result == null) {
			Config condfig = configService.getConfigByKey(configKey);
			if (condfig != null) {
				fee = new BigDecimal(condfig.getValue());
				set(redisKey, fee);
				return fee;
			}
		}
		fee = new BigDecimal(result);
		return fee;
	}
	
	public String getConfigByKey(String redisKey, String configKey) {
		String value = get(redisKey);
		if (value == null) {
			Config condfig = configService.getConfigByKey(configKey);
			if (condfig != null) {
				value = condfig.getValue();
				setNumber(redisKey, condfig.getValue());
			}
		}
		return value;
	}
	
	public void removeKey(String redisKey){
		del(redisKey);
	}

}

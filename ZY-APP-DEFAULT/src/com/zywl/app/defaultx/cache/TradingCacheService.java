package com.zywl.app.defaultx.cache;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zywl.app.base.bean.Trading;
import com.zywl.app.base.bean.vo.TradingVo;
import com.zywl.app.base.constant.RedisKeyConstant;
import com.zywl.app.base.util.BeanUtils;
import com.zywl.app.defaultx.cache.impl.RedisService;
import com.zywl.app.defaultx.enmus.TradingTypeEnum;
import com.zywl.app.defaultx.service.TradingService;
/**
 * 交易行缓存
 * **/
@Service
public class TradingCacheService extends RedisService {

	private static final Log logger = LogFactory.getLog(TradingCacheService.class);

	@Autowired
	private TradingService tradingService;

	/**
	 * 通过type和itemId获取该物品在交易行数据
	 * **/
	public List<TradingVo> getTradingCache(int start, int limit, Long itemId, Integer itemType, Long userId, Integer type) {
		Integer normalizedType = type;
		if (normalizedType != null && normalizedType > 1) {
			normalizedType = normalizedType - 2;
		}

		String keyPrefix = RedisKeyConstant.APP_TRADING_SELL;
		if (normalizedType != null && normalizedType == TradingTypeEnum.askbuy.getValue()) {
			keyPrefix = RedisKeyConstant.APP_TRADING_ASK_BUY;
		}
		String itemIdStr = (itemId == null) ? "all" : itemId.toString();
		String key = keyPrefix + itemIdStr + "-" + start + limit + itemType + userId + type;
		List<TradingVo> list = getList(key, TradingVo.class);
		if (list == null) {
			list = tradingService.findTradingsByConditon(start, limit, itemId, itemType, userId, type);
			set(key, list, 5);
		}
		return list;
	}


	// 通过type和itemId删除该物品在交易行数据
	public void removerTradingByIdAndType(int type, Long itemId) {
		String keyPrefix = "";
		if (type == TradingTypeEnum.sell.getValue()) {
			// 出售中
			keyPrefix = RedisKeyConstant.APP_TRADING_SELL;
		} else if (type == TradingTypeEnum.askbuy.getValue()) {
			keyPrefix = RedisKeyConstant.APP_TRADING_ASK_BUY;
		}

		String itemIdStr = (itemId == null) ? "all" : itemId.toString();

		//todo lzx:因为上面的set是带了分页这里要加上通配符、也不能用del,要用deleteByLikeKey
		String pattern = keyPrefix + itemIdStr + "-*";

		deleteByLikeKey(pattern);
	}

	// 交易行获取指定用户上架或者求购信息
	public List<TradingVo> getUserListingOrAskBuyInfo(Long userId, int tradingType) {
		String key = "";
		if (tradingType == TradingTypeEnum.sell.getValue()) {
			// 卖出 （上架）
			key = RedisKeyConstant.APP_USER_TRADING_LISTING + userId + "-";
		} else if (tradingType == TradingTypeEnum.askbuy.getValue()) {
			key = RedisKeyConstant.APP_USER_TRADING_ASKBUY + userId + "-";
		}
		List<Trading> tradings = getList(key, Trading.class);
		if (tradings == null) {
			tradings = tradingService.findTradingsInfoByUserId(userId);
			if (tradings != null) {
				set(key, tradings, 600L);
			}
		}

		List<TradingVo> vos = new ArrayList<TradingVo>();
		if (tradings != null) {
			for (Trading trading : tradings) {
				TradingVo vo = new TradingVo();
				BeanUtils.copy(trading, vo);
				vos.add(vo);
			}
		}
		return vos;
	}

	// 交易行移除指定用户上架或者求购信息
	public void removeUserListingOrAskBuyInfo(Long userId, int tradingType) {
		String key = "";
		if (tradingType == TradingTypeEnum.sell.getValue()) {
			// 卖出 （上架）
			key = RedisKeyConstant.APP_USER_TRADING_LISTING + userId + "-";
		} else if (tradingType == TradingTypeEnum.askbuy.getValue()) {
			key = RedisKeyConstant.APP_USER_TRADING_ASKBUY + userId + "-";
		}
		// 这个是精准key就可以del...
		del(key);
	}

	//通过tradingId获取信息
	public Trading getTradingInfoById(Long tradingId) {
		String key = RedisKeyConstant.APP_TRADING_ID + tradingId + "-";
		Trading trading = get(key, Trading.class);
		if (trading == null) {
			trading = tradingService.findById(tradingId);
			if (trading != null) {
				set(key, trading, 2L);
			}
		}
		return trading;
	}

	//通过tradingId 移除信息
	public void removeByTradingId(Long tradingId) {
		String key = RedisKeyConstant.APP_TRADING_ID + tradingId + "-";
		del(key);
	}

}
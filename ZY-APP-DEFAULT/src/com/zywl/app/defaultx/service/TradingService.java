package com.zywl.app.defaultx.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zywl.app.base.bean.Trading;
import com.zywl.app.base.bean.vo.TradingVo;
import com.zywl.app.base.util.BeanUtils;
import com.zywl.app.defaultx.cache.TradingCacheService;
import com.zywl.app.defaultx.dbutil.DaoService;
import com.zywl.app.defaultx.enmus.TradingStatusEnum;

@Service
public class TradingService extends DaoService {

    private static final Log logger = LogFactory.getLog(TradingService.class);

    @Autowired
    private TradingCacheService tradingCacheService;

    public TradingService() {
        super("TradingMapper");
        // TODO Auto-generated constructor stub
    }

    @Transactional
    public void addTrading(Long userId, Long itemId, int number, BigDecimal price, int type, int itemType) {
        Trading trading = new Trading();
        trading.setUserId(userId);
        trading.setItemId(itemId);
        trading.setItemType(itemType);
        trading.setItemNumber(number);
        trading.setItemAllNumber(number);
        trading.setItemPrice(price);
        trading.setType(type);
        trading.setStatus(TradingStatusEnum.listing.getValue());
        trading.setCreateTime(new Date());
        trading.setUpdateTime(new Date());
        save(trading);
    }

    public List<TradingVo> findTradingsByConditon(int start, int limit, Long itemId, Integer itemType, Long userId,
                                                  Integer type) {
        Map<String, Object> params = new HashedMap<>();
        params.put("start", start * limit);
        params.put("limit", limit);
        if (itemId != null) {
            params.put("itemId", itemId);
        }
        int itemType1 = 0;
        int itemType2 = 0;

        if (itemType != null) {
            if (itemType == 0) {
                itemType1 = 1;
                itemType2 = 2;
            } else if (itemType == 1) {
                itemType1 = 3;
                itemType2 = 4;
            } else if (itemType == 2) {
                itemType1 = 5;
                itemType2 = 6;
            } else if (itemType == 3) {
                itemType1 = 7;
                itemType2 = 8;
            }
            List<Integer> list = new ArrayList<>();
            list.add(itemType1);
            list.add(itemType2);
            params.put("itemTypes", list);
            params.put("itemType1", itemType1);
            params.put("itemType2", itemType2);
        }
        if (userId != null) {
            params.put("userId", userId);
            params.put("status", 1);
        }
        if (type <= 1) {
            params.put("status", TradingStatusEnum.listing.getValue());
        }
        type = type > 1 ? type - 2 : type;
        if (type != null) {
            params.put("type", type);
        }
        List<Trading> tradings = findByConditions(params);
        List<TradingVo> vos = new ArrayList<>();

        for (Trading trading : tradings) {
            TradingVo vo = new TradingVo();
            BeanUtils.copy(trading, vo);
            vos.add(vo);
        }
        return vos;
    }

    public List<Trading> findTradingsInfoByUserId(Long userId) {
        Map<String, Object> params = new HashedMap<>();
        params.put("userId", userId);
        List<Trading> tradings = findByConditions(params);
        return tradings;
    }

    public Trading findById(Long tradingId) {
        Map<String, Object> params = new HashedMap<>();
        params.put("id", tradingId);
        return findOne(params);
    }

    @Transactional
    public int cancelListingOrAskBuyOrAskbuyAll(Long tradingId, int type, Long itemId, Long userId, int status) {
        Map<String, Object> params = new HashedMap<>();
        params.put("id", tradingId);
        params.put("status", status);
        int a = execute("cancelListOrAskBuy", params);
        tradingCacheService.removerTradingByIdAndType(type, itemId);
        tradingCacheService.removeUserListingOrAskBuyInfo(userId, type);
        tradingCacheService.removeByTradingId(tradingId);
        return a;
    }

    @Transactional
    public void subItemNumberByTradingId(Long tradingId, int type, Long itemId, Long userId, int itemNumber,
                                         int tradingItemNumber) {
        Map<String, Object> params = new HashedMap<>();
        params.put("id", tradingId);
        params.put("itemNumber", itemNumber);
        int a = execute("subItemNumber", params);
        if (a < 1) {
            throwExp("道具数量不足，请刷新");
        }
        tradingCacheService.removerTradingByIdAndType(type, itemId);
        tradingCacheService.removeUserListingOrAskBuyInfo(userId, type);
        tradingCacheService.removeByTradingId(tradingId);
        if (itemNumber == tradingItemNumber) {
            // 出售数量等于求购数量 全部求购完毕 更改statue
            cancelListingOrAskBuyOrAskbuyAll(tradingId, type, itemId, userId, TradingStatusEnum.finsh.getValue());
        }
    }


    public Long getCountByUserId(Long userId) {
        Map<String, Object> params = new HashedMap<>();
        params.put("userId", userId);
        return count("getMyTradingCount", params);
    }

    @Transactional
    public void deletedNumberZero() {
        execute("deleteNumberZero", null);
    }
}

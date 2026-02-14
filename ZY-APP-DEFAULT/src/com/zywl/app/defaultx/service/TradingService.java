package com.zywl.app.defaultx.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.zywl.app.defaultx.enmus.TradingTabEnum;
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
        params.put("start", (start - 1) * limit);
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

    /**
     * 交易行列表 （四个Tab 支持关键字搜索 + 支持按时间/价格排序）
     * @param tabEnum 0=售卖商城(sell mall) 1=求购商城(askbuy mall) 2=我的售卖(my sell) 3=我的求购(my askbuy)
     */
    public List<TradingVo> findTradingsByConditionV2(int page, int limit,
                                                     List<Long> itemIds,
                                                     Long itemId,
                                                     Integer itemType,
                                                     Long userId,
                                                     TradingTabEnum tabEnum,
                                                     String sortField,
                                                     String sortOrder) {
        if (tabEnum == null) {
            throwExp("tab error");
        }

        Map<String, Object> params = new HashedMap<>();
        int start = (page - 1) * limit;
        params.put("start", start);
        params.put("limit", limit);

        if (itemId != null) {
            params.put("itemId", itemId);
        } else if (itemIds != null && !itemIds.isEmpty()) {
            params.put("itemIds", itemIds);
        }

        // itemType 分组映射（保持你原逻辑）
        if (itemType != null) {
            int itemType1 = 0, itemType2 = 0;
            if (itemType == 0) { itemType1 = 1; itemType2 = 2; }
            else if (itemType == 1) { itemType1 = 3; itemType2 = 4; }
            else if (itemType == 2) { itemType1 = 5; itemType2 = 6; }
            else if (itemType == 3) { itemType1 = 7; itemType2 = 8; }

            List<Integer> list = new ArrayList<>();
            list.add(itemType1);
            list.add(itemType2);
            params.put("itemTypes", list);
        }

        if (tabEnum.isMine()) {
            if (userId == null || userId < 1) throwExp("userId error");
            params.put("userId", userId);
        }

        // DB type 永远只允许 0/1
        params.put("type", tabEnum.toDbType());
        params.put("status", TradingStatusEnum.listing.getValue());

        // 排序强校验（Mapper 用 ${}）
        String sf = (sortField == null || sortField.trim().isEmpty()) ? "time" : sortField.trim();
        if (!"time".equals(sf) && !"price".equals(sf)) throwExp("sortField error");

        String so = (sortOrder == null || sortOrder.trim().isEmpty()) ? "desc" : sortOrder.trim().toLowerCase();
        if (!"asc".equals(so) && !"desc".equals(so)) throwExp("sortOrder error");

        params.put("sortField", sf);
        params.put("sortOrderSql", so);

        List<Trading> tradings = findByConditions(params);
        List<TradingVo> vos = new ArrayList<>();
        for (Trading trading : tradings) {
            TradingVo vo = new TradingVo();
            BeanUtils.copy(trading, vo);
            vos.add(vo);
        }
        return vos;
    }

    public Long countTradingsByConditionV2(List<Long> itemIds,
                                           Long itemId,
                                           Integer itemType,
                                           Long userId,
                                           TradingTabEnum tabEnum) {
        if (tabEnum == null) throwExp("tab error");

        Map<String, Object> params = new HashedMap<>();

        if (itemId != null) {
            params.put("itemId", itemId);
        } else if (itemIds != null && !itemIds.isEmpty()) {
            params.put("itemIds", itemIds);
        }

        if (itemType != null) {
            int itemType1 = 0, itemType2 = 0;
            if (itemType == 0) { itemType1 = 1; itemType2 = 2; }
            else if (itemType == 1) { itemType1 = 3; itemType2 = 4; }
            else if (itemType == 2) { itemType1 = 5; itemType2 = 6; }
            else if (itemType == 3) { itemType1 = 7; itemType2 = 8; }

            List<Integer> list = new ArrayList<>();
            list.add(itemType1);
            list.add(itemType2);
            params.put("itemTypes", list);
        }

        if (tabEnum.isMine()) {
            if (userId == null || userId < 1) throwExp("userId error");
            params.put("userId", userId);
        }

        params.put("type", tabEnum.toDbType());
        params.put("status", TradingStatusEnum.listing.getValue());

        return count("countByConditions", params);
    }


}

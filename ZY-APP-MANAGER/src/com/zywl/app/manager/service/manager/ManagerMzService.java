package com.zywl.app.manager.service.manager;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.*;
import com.zywl.app.base.bean.card.DicMine;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.base.util.LockUtil;
import com.zywl.app.base.util.OrderUtil;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.cache.GameCacheService;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.enmus.ItemIdEnum;
import com.zywl.app.defaultx.enmus.LogUserBackpackTypeEnum;
import com.zywl.app.defaultx.enmus.UserCapitalTypeEnum;
import com.zywl.app.defaultx.service.*;
import com.zywl.app.manager.context.MessageCodeContext;
import com.zywl.app.manager.service.PlayGameService;
import com.zywl.app.manager.socket.ManagerSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;


@Service
@ServiceClass(code = MessageCodeContext.MZ)
public class ManagerMzService extends BaseService {

    @Autowired
    private DicMzItemService dicMzItemService;

    @Autowired
    private MzUserItemService mzUserItemService;


    @Autowired
    private ManagerGameBaseService managerGameBaseService;

    @Autowired
    private ManagerConfigService managerConfigService;

    @Autowired
    private MzBuyRecordService mzBuyRecordService;

    @Autowired
    private UserCapitalService userCapitalService;

    @Autowired
    private UserCacheService userCacheService;

    @Autowired
    private ManagerUserService managerUserService;

    @Autowired
    private MzTradService mzTradService;

    @Autowired
    private PlayGameService gameService;

    @Autowired
    private GameCacheService gameCacheService;

    private static Object shopLock = new Object();


    @Transactional
    @ServiceMethod(code = "001", description = "获取商店信息")
    public Object getShopInfo(ManagerSocketServer adminSocketServer, JSONObject params) {
        List<DicMzItem> canBuy = dicMzItemService.findCanBuy();
        return canBuy;
    }

    @Transactional
    @ServiceMethod(code = "002", description = "商店购买慢涨道具")
    public Object buy(ManagerSocketServer adminSocketServer, JSONObject params) {
        checkNull(params);
        checkNull(params.get("id"), params.get("userId"));
        Long id = params.getLongValue("id");
        Long userId = params.getLong("userId");
        synchronized (LockUtil.getlock(shopLock + "")) {
            DicMzItem dicMzItem = dicMzItemService.findById(id);
            if (dicMzItem == null) {
                throwExp("道具不存在");
            }
            if (dicMzItem.getIsShop() == 0) {
                throwExp("该道具不可购买");
            }
            if (dicMzItem.getShopNumber() <= 0) {
                throwExp("道具已售罄");
            }
            int needWhite = managerConfigService.getInteger(Config.MZ_NEED_WHITE);
            boolean sub = false;
            if (needWhite == 1) {
                //需要白名单购买
                gameService.checkUserItemNumber(userId, ItemIdEnum.BUY_WHITE.getValue(), 1);
                List<MzBuyRecord> todayByUserId = mzBuyRecordService.findTodayByUserId(userId);
                if (todayByUserId != null && todayByUserId.size() > 0) {
                    throwExp("超过每日限购次数");
                }
                sub = true;
            } else if (needWhite == 2) {
                //即可用白名单买 也可以直接买
                List<MzBuyRecord> todayByUserId = mzBuyRecordService.findTodayByUserId2(userId, 1);
                List<MzBuyRecord> todayByUserId2 = mzBuyRecordService.findTodayByUserId2(userId, 0);
                if (todayByUserId != null && todayByUserId.size() > 0) {
                    //白名单已经买过了
                    if (todayByUserId2 != null && todayByUserId2.size() > 0) {
                        //非白名单买过了
                        throwExp("超过每日限购次数");
                    }
                } else {
                    //gameService.checkUserItemNumber(userId, ItemIdEnum.BUY_WHITE.getValue(), 1);
                    Map<String, Backpack> userBackpack = gameService.getUserBackpack(userId);
                    if (userBackpack.containsKey(ItemIdEnum.BUY_WHITE.getValue()) && userBackpack.get(ItemIdEnum.BUY_WHITE.getValue()).getItemNumber() > 0) {
                        //有白名单  白名单购买
                        sub = true;
                    } else {
                        if (todayByUserId2 != null && todayByUserId2.size() > 0) {
                            //非白名单买过了
                            throwExp("超过每日限购次数");
                        }
                    }
                }
            }

            if (System.currentTimeMillis() < dicMzItem.getCanBuyTime().getTime()) {
                throwExp("未到开售时间");
            }
            //修改全服剩余道具数量
            dicMzItemService.updateNumber(id);
            //检查资产
            managerGameBaseService.checkBalance(userId, dicMzItem.getPrice(), UserCapitalTypeEnum.currency_2);
            String orderNo = OrderUtil.getOrder5Number();
            //添加购买记录
            Long dataId = mzBuyRecordService.addShopBuyRecord(userId, 1, orderNo, dicMzItem.getPrice(),sub);
            if (sub) {
                //扣除白名单
                gameService.updateUserBackpack(userId, ItemIdEnum.BUY_WHITE.getValue(), -1, LogUserBackpackTypeEnum.use);
            }
            //添加玩家的道具
            mzUserItemService.addMzItem(userId, dicMzItem.getId(), null, null, dicMzItem.getIcon(), dicMzItem.getName(), dicMzItem.getContext());
            //减少资产
            userCapitalService.subUserBalanceByBuyMz(userId, dicMzItem.getPrice(), orderNo, dataId);
            //推送资产变动后
            managerGameBaseService.pushCapitalUpdate(userId, UserCapitalTypeEnum.currency_2.getValue());
            addActiveScore(userId);
            JSONArray array = new JSONArray();
            JSONObject info = new JSONObject();
            info.put("type", 1);
            info.put("id", ItemIdEnum.MZ_LZ.getValue());
            info.put("number", 1);
            array.add(info);
            return array;
        }
    }

    public void addActiveScore(Long userId) {
        User user = userCacheService.getUserInfoById(userId);
        double score = 10;
        String time = managerConfigService.getString(Config.NEW_USER_TIME);
        Date dateTimeByString = DateUtil.getDateTimeByString(time);
        if (user.getRegistTime().getTime() > dateTimeByString.getTime()) {
            //新用户
            score = 15;
        }
        Activity activity = gameCacheService.getActivity();
        if (activity != null && activity.getAddPointEvent() == 3) {
            if (user.getParentId() != null) {
                gameCacheService.addPointMySelf(user.getParentId(), score);
            }
        }
    }

    @Transactional
    @ServiceMethod(code = "003", description = "升级慢涨道具")
    public Object up(ManagerSocketServer adminSocketServer, JSONObject params) {
        checkNull(params);
        checkNull(params.get("id"), params.get("userId"));
        Long id = params.getLongValue("id");
        Long userId = params.getLong("userId");
        synchronized (LockUtil.getlock(userId)) {
            MzUserItem byId = mzUserItemService.findById(id);
            if (byId == null) {
                throwExp("道具不存在");
            }
            if (!Objects.equals(byId.getUserId(), userId) || byId.getStatus() == -1) {
                throwExp("非法请求");
            }
            if (byId.getStatus() == 1) {
                throwExp("该道具正在修复中");
            }
            if (byId.getStatus() == 2) {
                throwExp("已经修复过了，无法连续修复");
            }
            DicMzItem dicMzItem = dicMzItemService.findById(byId.getMzItemId());
            byId.setStatus(1);
            byId.setUpTime(new Date());
            byId.setUpEndTime(DateUtil.getDateBeginByDay(dicMzItem.getDays()));
            mzUserItemService.updateMzUserItem(byId);
        }
        return new JSONObject();
    }


    @Transactional
    @ServiceMethod(code = "004", description = "领取升级完成的道具")
    public Object receive(ManagerSocketServer adminSocketServer, JSONObject params) {
        checkNull(params);
        checkNull(params.get("id"), params.get("userId"));
        Long id = params.getLongValue("id");
        Long userId = params.getLong("userId");
        synchronized (LockUtil.getlock(userId)) {
            MzUserItem byId = mzUserItemService.findById(id);
            if (byId == null) {
                throwExp("道具不存在");
            }
            if (!Objects.equals(byId.getUserId(), userId) || byId.getStatus() != 1) {
                throwExp("非法请求");
            }
            //升级道具ID 变成新道具
            byId.setMzItemId(byId.getMzItemId() + 1);
            DicMzItem dicMzItem = dicMzItemService.findById(byId.getMzItemId());
            User user = userCacheService.getUserInfoById(userId);
            byId.setLastUserNo(user.getUserNo());
            byId.setContext(dicMzItem.getContext());
            byId.setIcon(dicMzItem.getIcon());
            byId.setName(dicMzItem.getName());
            byId.setPrice(dicMzItem.getTradPrice());
            byId.setLastUserName(user.getName());
            byId.setStatus(2);
            mzUserItemService.updateMzUserItem(byId);
            return new JSONObject();
        }
    }

    @Transactional
    @ServiceMethod(code = "005", description = "上架慢涨道具")
    public Object sell(ManagerSocketServer adminSocketServer, JSONObject params) {
        checkNull(params);
        checkNull(params.get("id"), params.get("userId"));
        Long id = params.getLongValue("id");
        Long userId = params.getLong("userId");
        synchronized (LockUtil.getlock(userId)) {
            MzUserItem byId = mzUserItemService.findById(id);
            long beginTime = DateUtil.getToDayDateByHour(15);
            long endTime = DateUtil.getToDayDateByHour(16);
            /*if (System.currentTimeMillis() < beginTime || System.currentTimeMillis() > endTime) {
                throwExp("未到出售时间。出售时间15:00-16:00");
            }*/
            if (byId == null) {
                throwExp("道具不存在");
            }
            if (!Objects.equals(byId.getUserId(), userId)) {
                throwExp("非法请求");
            }
            if (byId.getStatus() != 2) {
                throwExp("当前不可上架该道具");
            }
            byId.setStatus(3);
            mzUserItemService.updateMzUserItem(byId);
            DicMzItem dicMzItem = dicMzItemService.findById(byId.getMzItemId());
            BigDecimal sellPrice = dicMzItem.getTradPrice();
            BigDecimal fee = sellPrice.multiply(new BigDecimal("0.3"));
            mzTradService.addMzTrad(byId.getMzItemId(), byId.getId(), userId, sellPrice, fee, sellPrice.subtract(fee), dicMzItem.getName(), dicMzItem.getIcon());
        }
        return new JSONObject();
    }

    @Transactional
    @ServiceMethod(code = "007", description = "交易行购买慢涨道具")
    public Object tradBuy(ManagerSocketServer adminSocketServer, JSONObject params) {
        checkNull(params);
        checkNull(params.get("id"), params.get("userId"));
        Long id = params.getLongValue("id");
        Long userId = params.getLong("userId");
        synchronized (LockUtil.getlock(id)) {
            MzTrad trad = mzTradService.findById(id);
            long beginTime = DateUtil.getToDayDateByHour(17);
            long endTime = DateUtil.getToDayDateByHour(18);
            /*if (System.currentTimeMillis() < beginTime || System.currentTimeMillis() > endTime) {
                throwExp("未到购买时间。出售时间17:00-18:00");
            }*/
            if (trad.getStatus() != 1) {
                throwExp("已下架或被买走，清刷新后查看");
            }
            if (Objects.equals(trad.getSellUserId(), userId)) {
                throwExp("不能购买自己上架的道具");
            }
            //检查货币余额
            managerGameBaseService.checkBalance(userId, trad.getSellPrice(), UserCapitalTypeEnum.currency_2);
            //更改订单状态
            trad.setStatus(0);
            //更改DB
            mzTradService.updateTrad(trad);
            //查询道具 更改道具所属主人 更改道具未可升级可交易状态
            MzUserItem byId = mzUserItemService.findById(trad.getUserItemId());
            if (byId == null) {
                throwExp("道具不存在");
            }

            byId.setStatus(0);
            byId.setUserId(userId);
            mzUserItemService.updateMzUserItem(byId);
            String orderNo = OrderUtil.getOrder5Number();
            Long dataId = mzBuyRecordService.addRecord(userId, 2, trad.getSellUserId(), trad.getId(), trad.getFee(), orderNo, trad.getSellPrice(), trad.getName(), trad.getIcon(), trad.getMzItemId());
            //更改余额
            userCapitalService.subUserBalanceByMzTradingBuy(userId, trad.getSellPrice(), trad.getId());
            //增加卖方余额
            userCapitalService.addUserBalanceByMzTrad(trad.getSellUserId(), trad.getGetAmount(), orderNo, dataId);
            //给上级 上上级添加收益
            managerUserService.addAnimaToInviter(trad.getSellUserId(), trad.getSellPrice().subtract(trad.getGetAmount()),new BigDecimal("0.1"));
            //推送余额
            managerGameBaseService.pushCapitalUpdate(userId, UserCapitalTypeEnum.currency_2.getValue());
            managerGameBaseService.pushCapitalUpdate(trad.getSellUserId(), UserCapitalTypeEnum.currency_2.getValue());
        }
        return new JSONObject();
    }


    @Transactional
    @ServiceMethod(code = "008", description = "交易行下架道具")
    public Object cancelTrad(ManagerSocketServer adminSocketServer, JSONObject params) {
        checkNull(params);
        checkNull(params.get("id"), params.get("userId"));
        Long id = params.getLongValue("id");
        Long userId = params.getLong("userId");
        synchronized (LockUtil.getlock(id)) {
            MzTrad trad = mzTradService.findById(id);
            if (trad.getStatus() != 1) {
                throwExp("已下架或被买走，清刷新后查看");
            }
            if (!Objects.equals(trad.getSellUserId(), userId)) {
                throwExp("非法请求");
            }
            trad.setStatus(-1);
            mzTradService.updateTrad(trad);
            MzUserItem byId = mzUserItemService.findById(trad.getUserItemId());
            byId.setStatus(2);
            mzUserItemService.updateMzUserItem(byId);
            return new JSONObject();
        }
    }
}

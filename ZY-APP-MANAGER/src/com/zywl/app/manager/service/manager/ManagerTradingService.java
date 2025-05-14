package com.zywl.app.manager.service.manager;

import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.enums.PushCode;
import com.live.app.ws.util.Push;
import com.zywl.app.base.bean.*;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.LockUtil;
import com.zywl.app.base.util.OrderUtil;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.cache.*;
import com.zywl.app.defaultx.enmus.LogUserBackpackTypeEnum;
import com.zywl.app.defaultx.enmus.TradingStatusEnum;
import com.zywl.app.defaultx.enmus.TradingTypeEnum;
import com.zywl.app.defaultx.enmus.UserCapitalTypeEnum;
import com.zywl.app.defaultx.service.BackpackService;
import com.zywl.app.defaultx.service.ConfigService;
import com.zywl.app.defaultx.service.TradingService;
import com.zywl.app.defaultx.service.UserCapitalService;
import com.zywl.app.manager.context.MessageCodeContext;
import com.zywl.app.manager.service.PlayGameService;
import com.zywl.app.manager.socket.AdminSocketServer;
import com.zywl.app.manager.socket.ManagerSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.Map;

@Service
@ServiceClass(code = MessageCodeContext.TRADING_SERVER)
public class ManagerTradingService extends BaseService {


    @Autowired
    private BackpackService backpackService;

    @Autowired
    private TradingService tradingService;

    @Autowired
    private UserBackpackCacheService userBackpackCacheService;

    @Autowired
    private UserCapitalService userCapitalService;


    @Autowired
    private LockCacheService lockCacheService;

    @Autowired
    private ItemCacheService itemCacheService;

    @Autowired
    private TradingCacheService tradingCacheService;

    @Autowired
    private PlayGameService gameService;


    @Autowired
    private UserCapitalCacheService userCapitalCacheService;

    @Autowired
    private ManagerConfigService managerConfigService;

    @Autowired
    private ManagerGameBaseService gameBaseService;

    @Autowired
    private ManagerSocketService managerSocketService;

    @Autowired
    private ConfigService configService;

    private Long sysUserId;

    @PostConstruct
    public void _construct() {
        Config config = configService.getConfigByKey(Config.SYS_TRADING_USER_ID);
        sysUserId = Long.parseLong(config.getValue());

       /* new Timer("每秒清理已完成的交易订单").schedule(new TimerTask() {
            public void run() {
                try {
                     tradingService.deletedNumberZero();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0, 2000 );*/
    }

    public void setSysUserId(long userId) {
        sysUserId = userId;
    }

    @Transactional
    @ServiceMethod(code = "001", description = "系统上架道具")
    public synchronized JSONObject sysListingItem(AdminSocketServer adminSocketServer, JSONObject data) {
        checkNull(data);
        checkNull(data.get("price"), data.get("itemId"), data.get("number"));
        Long userId = sysUserId;
        Long itemId = data.getLong("itemId");
        int number = data.getIntValue("number");
        BigDecimal price = data.getBigDecimal("price");
        Item item = itemCacheService.getItemInfoById(itemId);
        // 验证用户是否有这个道具以及道具是否充足
        Map<String, Backpack> backs = gameService.getUserBackpack(userId.toString());
        if (!backs.containsKey(itemId.toString()) || backs.get(itemId.toString()).getItemNumber() < number) {
            throwExp("道具数量不足！");
        }
        if (number < 0 || number > 99999) {
            throwExp("请输入0-99999的数量");
        }
        //更新用户背包同时更新用户道具流水  再更新交易行数据
        gameService.updateUserBackpack(userId.toString(), itemId.toString(), -number, LogUserBackpackTypeEnum.listing);
        tradingService.addTrading(userId, itemId, number, price, TradingTypeEnum.sell.getValue(), item.getType());
        JSONObject result = new JSONObject();
        result.put("itemId", itemId);
        result.put("number", number);
        return result;
    }

    public synchronized int sysAddOrder(Long itemId, int number, BigDecimal price, int orderType) {
        Long userId = sysUserId;
        Item item = itemCacheService.getItemInfoById(itemId);
        if (item.getIsTrading() != 1) {
            throwExp("不可出售");
        }
        BigDecimal itemPrice = item.getTradPrice();
        if (price.compareTo(itemPrice.multiply(new BigDecimal("0.8"))) < 0 || price.compareTo(itemPrice.multiply(new BigDecimal("1.2"))) > 0) {
            String str = "道具价格区间：" + itemPrice.multiply(new BigDecimal("0.8")) + "~" + itemPrice.multiply(new BigDecimal("1.2"));
            throwExp(str);
        }

        // 验证用户是否有这个道具以及道具是否充足
        Map<String, Backpack> backs = gameService.getUserBackpack(userId.toString());
        if (!backs.containsKey(itemId.toString()) || backs.get(itemId.toString()).getItemNumber() < number) {
            throwExp("道具不足");
        }
        if (number < 0 || number > 99999) {
            throwExp("道具数量错误");
        }
        //更新用户背包同时更新用户道具流水  再更新交易行数据
        if (orderType == 0) {
            gameService.updateUserBackpack(userId.toString(), itemId.toString(), -number, LogUserBackpackTypeEnum.listing);
        } else {
            UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.currency_2.getValue());
            userCapitalService.subUserBalanceByAskBuy(userId, itemId, price, userCapital.getBalance(), userCapital.getOccupyBalance());
            JSONObject pushData = new JSONObject();
            userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.currency_2.getValue());
            pushData.put("userId", userId);
            pushData.put("capitalType", UserCapitalTypeEnum.currency_2.getValue());
            pushData.put("balance", userCapital.getBalance());
            Push.push(PushCode.updateUserCapital, managerSocketService.getServerIdByUserId(userId), pushData);
        }
        tradingService.addTrading(userId, itemId, number, price, orderType, item.getType());
        return 1;
    }

    @Transactional
    @ServiceMethod(code = "100", description = "用户上架道具")
    public synchronized JSONObject userListingItem(ManagerSocketServer adminSocketServer, JSONObject data) {
        checkNull(data);
        checkNull(data.get("price"), data.get("userId"), data.get("itemId"), data.get("number"));
        Long userId = data.getLong("userId");
        Long itemId = data.getLong("itemId");
        int number = data.getIntValue("number");
        BigDecimal price = data.getBigDecimal("price");
        Item item = itemCacheService.getItemInfoById(itemId);
        BigDecimal itemPrice = PlayGameService.itemMap.get(itemId.toString()).getTradPrice();
        BigDecimal min = itemPrice.subtract(itemPrice.multiply(managerConfigService.getBigDecimal(Config.TRAD_MIN)));
        BigDecimal max = itemPrice.add(itemPrice.multiply(managerConfigService.getBigDecimal(Config.TRAD_MAX)));
        if (price.compareTo(min)<0 || price.compareTo(max)>0){
            throwExp("价格区间不合理");
        }
        // 验证用户是否有这个道具以及道具是否充足
        Map<String, Backpack> backs = gameService.getUserBackpack(userId.toString());
        if (!backs.containsKey(itemId.toString()) || backs.get(itemId.toString()).getItemNumber() < number) {
            throwExp("道具数量不足！");
        }
        //更新用户背包同时更新用户道具流水  再更新交易行数据
        gameService.updateUserBackpack(userId.toString(), itemId.toString(), -number, LogUserBackpackTypeEnum.listing);
        tradingService.addTrading(userId, itemId, number, price, TradingTypeEnum.sell.getValue(), item.getType());
        JSONObject result = new JSONObject();
        result.put("itemId", itemId);
        result.put("number", number);
        return result;
    }

    @Transactional
    @ServiceMethod(code = "200", description = "用户下架道具/撤销求购")
    public JSONObject userCancelListingOrAskBuy(ManagerSocketServer adminSocketServer, JSONObject data) {

        checkNull(data);
        checkNull(data.get("tradingId"), data.get("userId"), data.get("type"), data.get("itemId"), data.get("number"), data.get("price"));
        Long userId = data.getLong("userId");
        Long tradingId = data.getLong("tradingId");
        /* throwExp("功能维护中");*/
        synchronized (LockUtil.getlock(tradingId.toString()+"trading")) {
            Long itemId = data.getLong("itemId");
            int type = data.getIntValue("type");
            int number = data.getIntValue("number");
            BigDecimal price = data.getBigDecimal("price");
            Trading trading = tradingService.findById(tradingId);
            if (trading.getStatus() == 2) {
                throwExp("该订单已取消");
            }
            if (type == TradingTypeEnum.sell.getValue()) {
                int a = tradingService.cancelListingOrAskBuyOrAskbuyAll(tradingId, type, userId, itemId, TradingStatusEnum.unlisting.getValue());
                //更改交易行数据status为2
                if (a < 1) {
                    throwExp("订单已完成");
                }
                //下架道具
                //获取玩家此时该道具的数量
                //修改玩家道具数量
                Trading byId = tradingService.findById(tradingId);
                number = byId.getItemNumber();
                gameService.updateUserBackpack(userId.toString(), itemId.toString(), number, LogUserBackpackTypeEnum.delist);
            } else if (type == TradingTypeEnum.askbuy.getValue()) {
                //撤销求购   先更改交易行数据  再更新用户资产
                int b = tradingService.cancelListingOrAskBuyOrAskbuyAll(tradingId, TradingTypeEnum.askbuy.getValue(), itemId, userId, TradingStatusEnum.unlisting.getValue());
                if (b < 1) {
                    throwExp("订单已完成");
                }
                userCapitalService.addUserBalanceByCancelAskBuy(userId, itemId, price.multiply(new BigDecimal(number)));
                gameBaseService.pushCapitalUpdate(userId,UserCapitalTypeEnum.currency_2.getValue());
                lockCacheService.deleteLock("sell" + tradingId);
            }
            JSONObject result = new JSONObject();
            result.put("tradingId", tradingId);
            return result;
        }

    }


    @Transactional
    @ServiceMethod(code = "300", description = "用户添加求购信息")
    public synchronized JSONObject userAddTradingBuy(ManagerSocketServer adminSocketServer, JSONObject data) {
        checkNull(data);
        checkNull(data.get("price"), data.get("userId"), data.get("itemId"), data.get("number"));
        Long userId = data.getLong("userId");
        Long itemId = data.getLong("itemId");
        int number = data.getIntValue("number");
        BigDecimal price = data.getBigDecimal("price");
        BigDecimal itemPrice = PlayGameService.itemMap.get(itemId.toString()).getTradPrice();
        BigDecimal min = itemPrice.subtract(itemPrice.multiply(managerConfigService.getBigDecimal(Config.TRAD_MIN)));
        BigDecimal max = itemPrice.add(itemPrice.multiply(managerConfigService.getBigDecimal(Config.TRAD_MAX)));
        if (price.compareTo(min)<0 || price.compareTo(max)>0){
            throwExp("价格区间不合理");
        }
        Item item = itemCacheService.getItemInfoById(itemId);
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId,
                UserCapitalTypeEnum.currency_2.getValue());
        BigDecimal balance = userCapital.getBalance();
        BigDecimal occupyBalance = userCapital.getOccupyBalance();
        if (userCapital.getBalance().compareTo(price.multiply(new BigDecimal(number))) <= 0) {
            // 余额不够 无法添加求购信息
            throwExp("余额不足");
        }
        //更新用户资产  添加交易行数据
        userCapitalService.subUserBalanceByAskBuy(userId, itemId, price.multiply(new BigDecimal(number)), balance, occupyBalance);
        JSONObject pushData = new JSONObject();
        gameBaseService.pushCapitalUpdate(userId,UserCapitalTypeEnum.currency_2.getValue());
        tradingService.addTrading(userId, itemId, number, price, TradingTypeEnum.askbuy.getValue(), item.getType());
        JSONObject result = new JSONObject();
        // TODO 返回数据未定义
        return result;
    }


    @Transactional
    @ServiceMethod(code = "500", description = "交易行用户购买道具")
    public JSONObject tradingUserBuyItem(ManagerSocketServer adminSocketServer, JSONObject data) {
        checkNull(data);
        checkNull(data.get("tradingId"), data.get("userId"), data.get("number"));
        Long tradingId = data.getLong("tradingId");
        Trading trading = tradingService.findById(tradingId);
        if (trading==null){
            throwExp("道具已经被买走啦~请刷新后重新购买");
        }
        synchronized (LockUtil.getlock(trading.getUserId().toString())) {
            Long userId = data.getLong("userId");
            int number = data.getIntValue("number");
            UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId,
                    UserCapitalTypeEnum.currency_2.getValue());
            if (userCapital.getBalance().compareTo(trading.getItemPrice().multiply(new BigDecimal(number))) == -1) {
                throwExp(UserCapitalTypeEnum.currency_2.getName() + "不足");
            }
            if (trading.getUserId().toString().equals(userId.toString())){
                throwExp("不能购买自己发布的订单~");
            }
            Long itemId = trading.getItemId();
            Long sellUserId = trading.getUserId();
            UserCapital sellUserCapital = userCapitalCacheService.getUserCapitalCacheByType(trading.getUserId(),
                    UserCapitalTypeEnum.currency_2.getValue());
            BigDecimal price = trading.getItemPrice();
            BigDecimal balance = userCapital.getBalance();
            BigDecimal sellUserBalance = sellUserCapital.getBalance();
            int tradingItemNumber = trading.getItemNumber();
            BigDecimal occupyBalance = userCapital.getBalance();
            BigDecimal sellUserOccupyBalance = sellUserCapital.getOccupyBalance();
            //更新购买者用户资产 更新售卖者用户资产  更新玩家道具数量  更新交易行数据
            String orderNo = OrderUtil.getOrder5Number();
            tradingService.subItemNumberByTradingId(tradingId, TradingTypeEnum.sell.getValue(), itemId, sellUserId, number, tradingItemNumber);
            userCapitalService.subUserBalanceByTradingBuy(userId,tradingId, itemId, price.multiply(new BigDecimal(number)), balance, occupyBalance, orderNo, number, price);
            gameBaseService.pushCapitalUpdate(userId,UserCapitalTypeEnum.currency_2.getValue());
            userCapitalService.addUserBalanceByTradingSell(sellUserId, tradingId,itemId, price.multiply(new BigDecimal(number)), sellUserBalance, sellUserOccupyBalance, orderNo, number, price);
            gameBaseService.pushCapitalUpdate(sellUserId,UserCapitalTypeEnum.currency_2.getValue());
            gameService.updateUserBackpack(userId.toString(), itemId.toString(), number, LogUserBackpackTypeEnum.buy);
            JSONObject result = new JSONObject();
            result.put("itemId", itemId);
            result.put("number", number);
            return result;
        }

    }


    @Transactional
    @ServiceMethod(code = "600", description = "交易行-用户卖给求购")
    public JSONObject userGiveItemToBuy(ManagerSocketServer adminSocketServer, JSONObject data) {
        checkNull(data);
        checkNull(data.get("tradingItemNumber"), data.get("tradingId"), data.get("askBuyUserId"), data.get("userId"), data.get("itemId"), data.get("number"));
        Long tradingId = data.getLong("tradingId");
        Trading trading = tradingService.findById(tradingId);
        if (trading==null){
            throwExp("订单已经被完成啦~请刷新后重新出售");
        }
        synchronized (LockUtil.getlock(trading.getUserId().toString())) {
            Long userId = data.getLong("userId");
            UserCapital myCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.currency_2.getValue());
            Long askBuyUserId = data.getLong("askBuyUserId");
            UserCapital askBuyCapital = userCapitalCacheService.getUserCapitalCacheByType(askBuyUserId, UserCapitalTypeEnum.currency_2.getValue());
            BigDecimal askBuyBalance = askBuyCapital.getBalance();
            BigDecimal askBuyUserOccupyBalance = askBuyCapital.getOccupyBalance();
            BigDecimal balance = myCapital.getBalance();
            BigDecimal occupyBalance = myCapital.getOccupyBalance();
            Long itemId = data.getLong("itemId");
            int number = data.getIntValue("number");
            BigDecimal price = data.getBigDecimal("price");
            System.out.println("manager：" + price);
            int tradingItemNumber = data.getIntValue("tradingItemNumber");
            Map<String, Backpack> backs = gameService.getUserBackpack(userId.toString());
            if (backs == null || backs.size() == 0 || !backs.containsKey(itemId.toString()) || backs.get(itemId.toString()).getItemNumber() < number) {
                throwExp("道具数量不足");
            }
            //更新交易行数据
            tradingService.subItemNumberByTradingId(tradingId, TradingTypeEnum.askbuy.getValue(), itemId, askBuyUserId, number, tradingItemNumber);
            //1.更新交易行交易记录2.更新求购者用户冻结资产资产 3.更新售出者用户资产  4.更新出售者道具数量
            //5.更新双方道具流水 6.更新求购者道具数量  7. 更新交易行数据
            String orderNo = OrderUtil.getOrder5Number();
            BigDecimal amount = price.multiply(new BigDecimal(number));
            //添加用户资产  包含了交易记录 流水记录
            userCapitalService.addUserBalanceByTradingSell(userId,tradingId, itemId, amount, balance, occupyBalance, orderNo, number, price);
            UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.currency_2.getValue());
            JSONObject pushData = new JSONObject();
            pushData.put("userId", userId);
            pushData.put("capitalType", UserCapitalTypeEnum.currency_2.getValue());
            pushData.put("balance", userCapital.getBalance());
            Push.push(PushCode.updateUserCapital, managerSocketService.getServerIdByUserId(userId), pushData);
            //减少用户冻结资产  包含交易记录 流水记录
            int res = userCapitalService.subUserOccupyBalanceByAskBuyItem(askBuyUserId, tradingId,itemId, price, number, askBuyBalance, askBuyUserOccupyBalance, orderNo);
            if (res < 1) {
                userCapitalCacheService.deltedUserCapitalCache(userId, UserCapitalTypeEnum.currency_2.getValue());
                PlayGameService.playerItems.remove(userId.toString());
            }
            //更新道具数量 售出者减 求购者加  包含物品流水
            gameService.updateUserBackpack(userId.toString(), itemId.toString(), -number, LogUserBackpackTypeEnum.sell);
            gameService.updateUserBackpack(askBuyUserId.toString(), itemId.toString(), number, LogUserBackpackTypeEnum.askbuy);
            JSONObject result = new JSONObject();
            // TODO 返回数据未定义
            lockCacheService.deleteLock("sell" + tradingId);
            return result;
        }

    }


}

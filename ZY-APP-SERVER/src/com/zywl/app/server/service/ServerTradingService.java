package com.zywl.app.server.service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.live.app.ws.enums.TargetSocketType;
import com.zywl.app.base.bean.*;
import com.zywl.app.base.constant.RedisKeyConstant;
import com.zywl.app.base.util.DateUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.bean.Command;
import com.live.app.ws.interfacex.Listener;
import com.live.app.ws.socket.BaseClientSocket;
import com.live.app.ws.util.CommandBuilder;
import com.live.app.ws.util.Executer;
import com.zywl.app.base.bean.vo.TradingRecordVo;
import com.zywl.app.base.bean.vo.TradingVo;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.Async;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.cache.ItemCacheService;
import com.zywl.app.defaultx.cache.LockCacheService;
import com.zywl.app.defaultx.cache.TradingCacheService;
import com.zywl.app.defaultx.cache.UserBackpackCacheService;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.cache.UserCapitalCacheService;
import com.zywl.app.defaultx.enmus.ItemTypeEnum;
import com.zywl.app.defaultx.enmus.TradingTypeEnum;
import com.zywl.app.defaultx.enmus.UserCapitalTypeEnum;
import com.zywl.app.defaultx.service.TradingRecordService;
import com.zywl.app.defaultx.service.TradingService;
import com.zywl.app.server.context.MessageCodeContext;
import com.zywl.app.server.socket.AppSocket;

import javax.annotation.PostConstruct;

@Service
@ServiceClass(code = MessageCodeContext.TRADING_SERVER)
public class ServerTradingService extends BaseService {

    private static final Log logger = LogFactory.getLog(ServerTradingService.class);

    @Autowired
    private UserBackpackCacheService userBackpackCacheService;

    @Autowired
    private RequestManagerService requestManagerService;

    @Autowired
    private TradingCacheService tradingCacheService;


    @Autowired
    private ItemCacheService itemCacheService;

    @Autowired
    private UserCacheService userCacheService;

    @Autowired
    private TradingService tradingService;

    @Autowired
    private LockCacheService lockCacheService;

    @Autowired
    private TradingRecordService tradingRecordService;






    @ServiceMethod(code = "001", description = "交易行物品上架")
    public Async listing(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("price"), params.get("itemId"), params.get("num"));
        Long userId = appSocket.getWsidBean().getUserId();

        BigDecimal price = params.getBigDecimal("price");
        User user = userCacheService.getUserInfoById(userId);
        if (user.getRiskPlus() != null && user.getRiskPlus() == 1) {
            throwExp("请求超时，请更换网络环境再试");
        }
        Long itemId = params.getLong("itemId");
        if (!GameBaseService.itemMap.containsKey(itemId.toString())){
            throwExp("非法请求");
        }
        int number = params.getIntValue("num");
        if (number < 1 || number > 99999) {
            throwExp("请输入合理的道具数量");
        }
        try {
            price = params.getBigDecimal("price");
        } catch (Exception e) {
            throwExp("道具价格有误！");
        }
        System.out.println(price);
        if (price.compareTo(BigDecimal.ZERO) < 1) {
            throwExp("道具价格不能小于0");
        }
        Long countByUserId = tradingService.getCountByUserId(userId);
        if (countByUserId >= 99) {
            throwExp("超过可发布订单数量");
        }
        if (GameBaseService.itemMap.containsKey(itemId.toString()) && GameBaseService.itemMap.get(itemId.toString()).getIsTrading()==0){
            throwExp("非法请求");
        }
        // 校验通过 物品充足 通知manager处理上架数据
        JSONObject data = new JSONObject();
        data.put("itemId", itemId);
        data.put("userId", userId);
        data.put("number", number);
        data.put("price", price);
        requestManagerService.requestManagerUserListingItem(data, new Listener() {
            public void handle(BaseClientSocket clientSocket, Command command) {
                if (command.isSuccess()) {
                    JSONObject result = JSONObject.from(command.getData());
                    Executer.response(CommandBuilder.builder(appCommand).success(result).build());
                } else {
                    Executer.response(
                            CommandBuilder.builder(appCommand).error(command.getMessage(), command.getData()).build());
                }
            }
        });
        return async();
    }

    @ServiceMethod(code = "002", description = "交易行物品下架")
    public Async delist(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("tradingId"));
        String key = "buy";
        long tradingId = params.getLong("tradingId");
        long userId = appSocket.getWsidBean().getUserId();
        User user = userCacheService.getUserInfoById(userId);
        // 获取用户上架信息，判断传过来的是否真的是自己的上架信息
        Trading trading = tradingCacheService.getTradingInfoById(tradingId);
        long itemId = trading.getItemId();
        if (trading.getId() == tradingId && trading.getUserId() == userId) {
            // 存在该物品 可以下架 通知manager修改数据
            JSONObject data = new JSONObject();
            data.put("tradingId", tradingId);
            data.put("userId", userId);
            data.put("itemId", itemId);
            data.put("type", TradingTypeEnum.sell.getValue());
            data.put("number", trading.getItemNumber());
            data.put("price", trading.getItemPrice());
            requestManagerService.requestManagerUserDelistItemOrCancelAskBuy(data, new Listener() {
                public void handle(BaseClientSocket clientSocket, Command command) {
                    if (command.isSuccess()) {
                        JSONObject result = JSONObject.from(command.getData());
                        Executer.response(CommandBuilder.builder(appCommand).success(result).build());
                    } else {
                        Executer.response(CommandBuilder.builder(appCommand)
                                .error(command.getMessage(), command.getData()).build());
                    }
                }
            });
        } else {
            lockCacheService.deleteLock(key);
            throwExp("下架失败！请稍后重试！");
        }
        return async();
    }

    @ServiceMethod(code = "003", description = "交易行-用户添加求购信息")
    public Async userAddTradingBuy(final AppSocket appSocket, Command appCommand, JSONObject params) {
        Long time = System.currentTimeMillis();
        checkNull(params);
        checkNull(params.get("price"), params.get("itemId"), params.get("num"));
        BigDecimal price = null;
        long userId = appSocket.getWsidBean().getUserId();
        User user = userCacheService.getUserInfoById(userId);
        if (user.getRiskPlus() != null && user.getRiskPlus() == 1) {
            throwExp("请求超时，请更换网络环境再试");
        }
        Long itemId = params.getLong("itemId");
        if (!GameBaseService.itemMap.containsKey(itemId.toString())){
            throwExp("非法请求");
        }
        int number = params.getIntValue("num");
        try {
            price = params.getBigDecimal("price");
        } catch (Exception e) {
            logger.error("求购价格不合理:" + e.getMessage());
            throwExp("求购价格不合理");
        }
        if (price.compareTo(BigDecimal.ZERO) < 1) {
            throwExp("求购价格不能小于0");
        }
        if (number < 1 || number > 99999) {
            throwExp("请输入合理的道具数量");
        }
        price = price.setScale(4, BigDecimal.ROUND_DOWN);
        // 验证是否存在该物品信息
        Long countByUserId = tradingService.getCountByUserId(userId);
        if (countByUserId > 99) {
            throwExp("超过可上架数量");
        }
        if (GameBaseService.itemMap.containsKey(itemId.toString()) && GameBaseService.itemMap.get(itemId.toString()).getIsTrading()==0){
            throwExp("非法请求");
        }

        // 校验通过 通知manager处理求购信息
        JSONObject data = new JSONObject();
        data.put("itemId", itemId);
        data.put("userId", userId);
        data.put("number", number);
        data.put("price", price);
        requestManagerService.requestManagerUserAddTradingAskBuy(data, new Listener() {
            public void handle(BaseClientSocket clientSocket, Command command) {
                if (command.isSuccess()) {
                    logger.info("处理时间：" + (System.currentTimeMillis() - time));
                    JSONObject result = JSONObject.from(command.getData());
                    Executer.response(CommandBuilder.builder(appCommand).success(result).build());
                } else {
                    Executer.response(
                            CommandBuilder.builder(appCommand).error(command.getMessage(), command.getData()).build());
                }
            }
        });

        return async();
    }

    @ServiceMethod(code = "004", description = "交易行-用户撤销求购信息")
    public Async userCancelTradingBuy(final AppSocket appSocket, Command appCommand, JSONObject params) {
        Long time = System.currentTimeMillis();
        checkNull(params);
        checkNull(params.get("tradingId"));

        long userId = appSocket.getWsidBean().getUserId();
        User user = userCacheService.getUserInfoById(userId);
        long tradingId = params.getLong("tradingId");
        String key = "sell" + tradingId;
        // 验证是否能取消求购 判断是否是本人上架 判断该求购数量是否小于等于0
        Trading trading = tradingCacheService.getTradingInfoById(tradingId);
        if (tradingId == trading.getId() && trading.getUserId() == userId && trading.getItemNumber() > 0) {
            // 是本人上架
            JSONObject data = new JSONObject();
            data.put("tradingId", tradingId);
            data.put("userId", userId);
            data.put("itemId", trading.getItemId());
            data.put("type", TradingTypeEnum.askbuy.getValue());
            data.put("number", trading.getItemNumber());
            data.put("price", trading.getItemPrice());
            requestManagerService.requestManagerUserDelistItemOrCancelAskBuy(data, new Listener() {
                public void handle(BaseClientSocket clientSocket, Command command) {
                    if (command.isSuccess()) {
                        logger.info("处理时间：" + (System.currentTimeMillis() - time));
                        JSONObject result = JSONObject.from(command.getData());
                        Executer.response(CommandBuilder.builder(appCommand).success(result).build());
                    } else {
                        Executer.response(CommandBuilder.builder(appCommand)
                                .error(command.getMessage(), command.getData()).build());
                    }
                }
            });
        } else {
            lockCacheService.deleteLock(key);
            throwExp("撤销求购失败，请稍后重试！");
        }
        return async();
    }

    @ServiceMethod(code = "005", description = "交易行-用户购买道具")
    public Async tradingUserBuyItem(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        Long time = System.currentTimeMillis();
        checkNull(params.get("tradingId"), params.get("num"));
        long userId = appSocket.getWsidBean().getUserId();
        User user = userCacheService.getUserInfoById(userId);
        if (user.getRiskPlus() != null && user.getRiskPlus() == 1) {
            throwExp("请求超时，请更换网络环境再试");
        }
        int number = params.getIntValue("num");
        if (number < 1 || number > 99999) {
            throwExp("请输入合理的道具数量");
        }
        long tradingId = params.getLongValue("tradingId");
        // 验证用户资产是否足够 验证交易行道具数量是否还充足


        JSONObject data = new JSONObject();
        data.put("tradingId", tradingId);
        data.put("userId", userId);
        data.put("number", number);
        requestManagerService.requestManagerTradingUserBuy(data, new Listener() {
            public void handle(BaseClientSocket clientSocket, Command command) {
                if (command.isSuccess()) {
                    logger.info("处理时间：" + (System.currentTimeMillis() - time));
                    JSONObject result = JSONObject.from(command.getData());
                    Executer.response(CommandBuilder.builder(appCommand).success(result).build());
                } else {
                    Executer.response(CommandBuilder.builder(appCommand)
                            .error(command.getMessage(), command.getData()).build());
                }
            }
        });
        return async();
    }

    @ServiceMethod(code = "006", description = "交易行-用户卖给求购")
    public Async userGiveItemToBuy(final AppSocket appSocket, Command appCommand, JSONObject params) {
        Long time = System.currentTimeMillis();
        checkNull(params);
        checkNull(params.get("tradingId"), params.get("num"));
        long userId = appSocket.getWsidBean().getUserId();
        User user = userCacheService.getUserInfoById(userId);
        if (user.getRiskPlus() != null && user.getRiskPlus() == 1) {
            throwExp("请求超时，请更换网络环境再试");
        }
        int number = params.getIntValue("num");
        if (number < 1 || number > 99999) {
            throwExp("请输入合理的道具数量");
        }
        long tradingId = params.getLong("tradingId");
        Trading trading = tradingCacheService.getTradingInfoById(tradingId);
        BigDecimal price = trading.getItemPrice();
        long itemId = trading == null ? 0L : trading.getItemId();
        // 验证用户是否有足够的道具 并且该求购数据正常
        if (trading != null && trading.getType() == TradingTypeEnum.askbuy.getValue()
                && trading.getItemNumber() >= number) {
            // 验证通过 获取求购人冻结余额
            JSONObject data = new JSONObject();
            data.put("itemId", itemId);
            data.put("userId", userId);
            data.put("number", number);
            data.put("tradingItemNumber", trading.getItemNumber());
            data.put("price", price);
            data.put("tradingId", tradingId);
            data.put("askBuyUserId", trading.getUserId());
            requestManagerService.requestManagerTradingUserSell(data, new Listener() {
                public void handle(BaseClientSocket clientSocket, Command command) {
                    if (command.isSuccess()) {
                        logger.info("处理时间：" + (System.currentTimeMillis() - time));
                        JSONObject result = JSONObject.from(command.getData());
                        Executer.response(CommandBuilder.builder(appCommand).success(result).build());
                    } else {
                        Executer.response(CommandBuilder.builder(appCommand)
                                .error(command.getMessage(), command.getData()).build());
                    }
                }
            });
        } else {
            throwExp("售卖失败，请刷新交易行后重试！");
        }

        return async();
    }

    @ServiceMethod(code = "007", description = "交易行-获取交易行物品数据")
    public JSONObject getTradingInfo(final AppSocket appSocket, Command appCommand, JSONObject params) {
        Long time = System.currentTimeMillis();
        checkNull(params);
        checkNull(params.get("type"), params.get("page"), params.get("num"));
        Integer type = params.getIntValue("type");
        if (type!=0 && type!=1){
            throwExp("非法参数");
        }
        Long userId = null;
        Long itemId = !params.containsKey("itemId") || params.getLong("itemId") == 0L ? null : params.getLong("itemId");
        Integer itemType = null;
        User user = userCacheService.getUserInfoById(appSocket.getWsidBean().getUserId());
        if (params.containsKey("itemType")) {
            itemType = params.getIntValue("itemType");
        }
        int page = params.getIntValue("page");
        int num = params.getIntValue("num");
        List<TradingVo> tradings;
        tradings = tradingCacheService.getTradingCache(page,
                num, itemId, itemType, userId, type);
        JSONObject result = new JSONObject();
        result.put("tradings", tradings);
        result.put("type", type);
        result.put("itemId", itemId);
        return result;
    }

    @ServiceMethod(code = "009", description = "交易行-获取我的交易行物品数据")
    public JSONObject getMyTradingInfo(final AppSocket appSocket, Command appCommand, JSONObject params) {
        Long time = System.currentTimeMillis();
        checkNull(params);
        checkNull(params.get("type"), params.get("page"), params.get("num"));
        Integer type = params.getIntValue("type");
        if (type!=2 && type!=3){
            throwExp("非法参数");
        }
        Long userId = appSocket.getWsidBean().getUserId();
        Long itemId = !params.containsKey("itemId") || params.getLong("itemId") == 0L ? null : params.getLong("itemId");
        Integer itemType = null;
        User user = userCacheService.getUserInfoById(appSocket.getWsidBean().getUserId());
        if (params.containsKey("itemType")) {
            itemType = params.getIntValue("itemType");
        }
        int page = params.getIntValue("page");
        int num = params.getIntValue("num");
        List<TradingVo> tradings;
        tradings = tradingCacheService.getTradingCache(page,
                num, itemId, itemType, userId, type);
        JSONObject result = new JSONObject();
        result.put("tradings", tradings);
        result.put("type", type);
        result.put("itemId", itemId);
        return result;
    }

    @ServiceMethod(code = "008", description = "获取交易行记录")
    public JSONObject getTradingRecord(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("page"), params.get("num"),params.get("type"));
        Long userId = appSocket.getWsidBean().getUserId();
        int type = params.getIntValue("type");
        User user = userCacheService.getUserInfoById(appSocket.getWsidBean().getUserId());
        JSONObject result = new JSONObject();
        List<TradingRecordVo> list = tradingRecordService.getMyRecord(userId, params.getIntValue("page"),
                params.getIntValue("num"),type);
        result.put("myRecord", list);
        return result;
    }

    public static void main(String[] args) {

        Text t = new Text();
        List<Text> list = new ArrayList<Text>();
        list.add(t);
        JSONObject obj = new JSONObject();
        obj.put("list", list);
        System.out.println(obj);
    }
}

package com.zywl.app.manager.service.manager;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.bean.PushBean;
import com.live.app.ws.enums.PushCode;
import com.live.app.ws.socket.BaseSocket;
import com.live.app.ws.util.DefaultPushHandler;
import com.live.app.ws.util.Push;
import com.zywl.app.base.bean.Config;
import com.zywl.app.base.bean.User;
import com.zywl.app.base.bean.UserCapital;
import com.zywl.app.base.bean.UserStatistic;
import com.zywl.app.base.constant.RedisKeyConstant;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.LockUtil;
import com.zywl.app.base.util.OrderUtil;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.cache.AppConfigCacheService;
import com.zywl.app.defaultx.cache.GameCacheService;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.cache.UserCapitalCacheService;
import com.zywl.app.defaultx.enmus.LogCapitalTypeEnum;
import com.zywl.app.defaultx.enmus.UserCapitalTypeEnum;
import com.zywl.app.defaultx.service.*;
import com.zywl.app.manager.context.MessageCodeContext;
import com.zywl.app.manager.service.AliPayCashService;
import com.zywl.app.manager.service.CheckAchievementService;
import com.zywl.app.manager.service.PlayGameService;
import com.zywl.app.manager.service.WXCashService;
import com.zywl.app.manager.socket.AdminSocketServer;
import com.zywl.app.manager.socket.ManagerDTSSocketServer;
import com.zywl.app.manager.socket.ManagerSGSocketServer;
import com.zywl.app.manager.socket.ManagerSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.Set;

@Service
@ServiceClass(code = MessageCodeContext.CAPITAL_SERVER)
public class ManagerCapitalService extends BaseService {


    @Autowired
    private UserCapitalService userCapitalService;

    @Autowired
    private CashRecordService cashRecordService;

    @Autowired
    private UserCapitalCacheService userCapitalCacheService;

    @Autowired
    private BalanceConvertRecordService balanceConvertRecordService;

    @Autowired
    private ManagerSocketService managerSocketService;

    @Autowired
    private BalanceShopService balanceShopService;
    @Autowired
    private RoleService roleService;

    @Autowired
    private UserCacheService userCacheService;

    @Autowired
    private GameCacheService gameCacheService;

    @Autowired
    private CheckAchievementService checkAchievementService;

    @Autowired
    private AppConfigCacheService appConfigCacheService;


    @Autowired
    private UserIncomeStatementService userIncomeStatementService;

    @Autowired
    private ExchangeRecordService exchangeRecordService;

    @Autowired
    private CashChannelIncomeRecordService cashChannelIncomeRecordService;


    @Autowired
    private ConfigService configService;

    @Autowired
    private UserStatisticService userStatisticService;

    @Autowired
    private ManagerConfigService managerConfigService;

    @Autowired
    private ManagerGameBaseService managerGameBaseService;


    @Autowired
    private UserService userService;

    @Autowired
    private PlayGameService gameService;


    @PostConstruct
    public void _ManagerCapitalService() {


        Push.addPushSuport(PushCode.syncUserCapital, new DefaultPushHandler());
        Push.addPushSuport(PushCode.updateUserCapital, new DefaultPushHandler() {
            public void onRegist(BaseSocket baseSocket, PushBean pushBean) {
                if (baseSocket instanceof ManagerSocketServer) {
                    ManagerSocketServer managerSocketServer = (ManagerSocketServer) baseSocket;
                    pushBean.setCondition(managerSocketServer.getId());
                }
            }
        });

        Push.addPushSuport(PushCode.updateUserBackpack, new DefaultPushHandler() {
            public void onRegist(BaseSocket baseSocket, PushBean pushBean) {
                if (baseSocket instanceof ManagerSocketServer) {
                    ManagerSocketServer managerSocketServer = (ManagerSocketServer) baseSocket;
                    pushBean.setCondition(managerSocketServer.getId());
                }
            }
        });

    }


    public JSONObject cash(Long userId, int type, BigDecimal amount, String userNo, String userName, String realName, String openId) {
        synchronized (String.valueOf(userId)) {
            //checkAmount(userId, amount);
            UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.rmb.getValue());
            BigDecimal balance = userCapital.getBalance();
            BigDecimal occupyBalance = userCapital.getOccupyBalance();
            if (balance.compareTo(amount) == -1) {
                throwExp("余额不足");
            }
            // 添加提现订单 扣除资产到冻结余额
            String orderNo = OrderUtil.getBatchOrder32Number();
            User user = userCacheService.getUserInfoById(userId);
            Long cashOrderId = cashRecordService.addCashOrder(openId, userId, userNo, userName, realName, orderNo,
                    amount, type, user.getPhone());
            if (type == 1) {
                WXCashService.cashOrderNos.add(orderNo);
            }
            if (type == 2) {
                AliPayCashService.cashOrderNos.add(orderNo);
            }
            int a = userCapitalService.subBalanceByCash(userId, orderNo, cashOrderId, amount, balance, occupyBalance);
            if (a < 1 || cashOrderId == null) {
                throwExp("提交提现订单失败！");
            }
            JSONObject pushData = new JSONObject();
            pushData.put("userId", userId);
            pushData.put("capitalType", UserCapitalTypeEnum.rmb);
            pushData.put("balance", balance.subtract(amount));
            Push.push(PushCode.updateUserCapital, managerSocketService.getServerIdByUserId(userId), pushData);
            JSONObject result = new JSONObject();
            userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId,
                    UserCapitalTypeEnum.rmb.getValue());
            result.put("rmbBalance", userCapital.getBalance());
            return result;
        }
    }


    @Transactional
    @ServiceMethod(code = "100", description = "用户提交提现申请")
    public JSONObject userCash(ManagerSocketServer adminSocketServer, JSONObject data) {
        checkNull(data);
        checkNull(data.get("userId"), data.get("userNo"), data.get("userName"),
                data.get("amount"));
        long userId = data.getLongValue("userId");
        String userNo = data.getString("userNo");
        String userName = data.getString("userName");
        BigDecimal amount = data.getBigDecimal("amount");
        User user = userCacheService.getUserInfoById(userId);
        String openId = user.getOpenId();
        int type = 2;
        String realName = data.getString("realName");
        if (managerConfigService.getInteger(Config.ALIPAY_CASH_TYPE) == 1 && user.getAlipayId() == null) {
            throwExp("玩家暂未绑定支付宝用户信息，无法提现。");
        }
        return cash(userId, type, amount, userNo, userName, realName, openId);
    }

    @Transactional
    @ServiceMethod(code = "200", description = "余额转换")
    public JSONObject assetConversion(ManagerSocketServer adminSocketServer, JSONObject data) {
        checkNull(data);
        checkNull(data.get("userId"), data.get("amount"), data.get("type"));
        long userId = data.getLongValue("userId");
        synchronized (String.valueOf(userId)) {
            BigDecimal amount = data.getBigDecimal("amount");
            if (amount.compareTo(BigDecimal.ZERO)==0){
                throwExp("请输入大于0的数值");
            }
            int type = data.getIntValue("type");
            int sourceType;
            int targetType;
            BigDecimal sourceAmount ;
            if (type == 1) {
                sourceType = UserCapitalTypeEnum.currency_1.getValue();
                targetType = UserCapitalTypeEnum.currency_2.getValue();
                sourceAmount = managerConfigService.getBigDecimal(Config.CONVERT_RATE).multiply(amount);
            }else {
                sourceType = UserCapitalTypeEnum.currency_2.getValue();
                targetType = UserCapitalTypeEnum.currency_1.getValue();
                sourceAmount = amount.divide(managerConfigService.getBigDecimal(Config.CONVERT_RATE));
            }
            // 减少sourceType余额 增加targetType余额
            String orderNo = OrderUtil.getOrder5Number();
            User user = userCacheService.getUserInfoById(userId);
            LogCapitalTypeEnum em = LogCapitalTypeEnum.balance_convert;
            UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, sourceType);
            if (userCapital.getBalance().compareTo(sourceAmount) < 0) {
                throwExp(UserCapitalTypeEnum.getName(sourceType) + "不足！");
            }
            String remark = user.getUserNo() + "兑换【" + sourceAmount + "】" + UserCapitalTypeEnum.getName(sourceType) + ",获得【" + amount + "】"
                    + UserCapitalTypeEnum.getName(targetType);
            // 添加记录 修改资产
            Long recordId = balanceConvertRecordService.addBalanceConvertOrder(userId, orderNo, sourceAmount, amount,
                    remark);
            userCapitalService.assetConversion(sourceType, targetType, sourceAmount, userId, orderNo, recordId,
                    amount, em);
            managerGameBaseService.pushCapitalUpdate(userId, sourceType);
            managerGameBaseService.pushCapitalUpdate(userId, targetType);
            JSONObject result = new JSONObject();
            return result;
        }
    }

    @Transactional
    @ServiceMethod(code = "101", description = "后台操作提现订单")
    public synchronized JSONObject adminUpdateCashRecord(AdminSocketServer adminSocketServer, JSONObject data) {
        if (!roleService.isAdmin(adminSocketServer.getAdmin())) {
            throwExp("权限不足");
        }
        return new JSONObject();
    }


    @Transactional
    @ServiceMethod(code = "500", description = "获取个人资产页面信息")
    public JSONObject getMyCapital(ManagerSocketServer adminSocketServer, JSONObject data) {
        checkNull(data);
        checkNull(data.get("userId"));
        Long userId = data.getLong("userId");
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.rmb.getValue());
        JSONObject result = new JSONObject();
        User user = userCacheService.getUserInfoById(userId);
        result.put("isChannel", user.getIsChannel());
        UserStatistic userStatistic = gameService.getUserStatistic(userId.toString());
        if (user.getIsChannel() == 1) {
            result.put("todayIncome", userCacheService.getTodayChannelIncome(userId));
            result.put("allIncome", userStatistic == null ? 0 : userStatistic.getChannelIncome());
        }
        result.put("toChannelNeed", managerConfigService.getString(Config.CHANNEL_FEE));
        result.put("balance", userCapital.getBalance());
        result.put("allAnima", userStatistic == null ? 0 : userStatistic.getGetAnima());
        result.put("animaNeed", new BigDecimal("20000"));
        result.put("nowRmb", userStatistic == null ? 0 : userStatistic.getGetIncome());
        result.put("allRmb", userStatistic == null ? 0 : userStatistic.getGetAllIncome());
        return result;
    }


    @Transactional
    @ServiceMethod(code = "011", description = "渠道收益提取到余额")
    public JSONObject cashChannelIncome(ManagerSocketServer adminSocketServer, JSONObject data) {
        checkNull(data);
        String userId = data.getString("userId");
        synchronized (LockUtil.getlock(userId)) {
            UserStatistic userStatistic = gameService.getUserStatistic(userId);
            BigDecimal nowIncome = userStatistic.getNowChannelIncome();
            BigDecimal cashSill = new BigDecimal(appConfigCacheService.getConfigByKey(RedisKeyConstant.APP_CONFIG_CHANNEL_CASH_SILL, Config.CHANNEL_CASH_SILL));
            if (nowIncome.compareTo(cashSill) < 0) {
                throwExp("未达到提取条件");
            }
            userStatistic.setNowChannelIncome(BigDecimal.ZERO);
            userStatisticService.updateStatic(userStatistic);
            String orderNo = OrderUtil.getOrder5Number();
            Long dataId = cashChannelIncomeRecordService.addRecord(Long.parseLong(userId), cashSill, orderNo);
            userCapitalService.addUserBalanceByCashChannelIncome(cashSill, Long.parseLong(userId), orderNo, dataId);
            UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(Long.parseLong(userId), UserCapitalTypeEnum.currency_2.getValue());
            JSONObject pushData = new JSONObject();
            pushData.put("userId", userId);
            pushData.put("capitalType", UserCapitalTypeEnum.currency_2.getValue());
            pushData.put("balance", userCapital.getBalance());
            Push.push(PushCode.updateUserCapital, managerSocketService.getServerIdByUserId(Long.parseLong(userId)), pushData);
            JSONObject result = new JSONObject();
            result.put("nowIncome", userStatistic.getNowChannelIncome());
            return result;
        }
    }


    public void cancelBet(JSONObject data) {
        checkNull(data);
        checkNull(data.get("userId"), data.get("betAmount"));
        BigDecimal betAmount = data.getBigDecimal("betAmount");
        Long userId = data.getLong("userId");
        userCapitalService.addUserBalanceByCancelBet(betAmount, userId, UserCapitalTypeEnum.currency_2.getValue(), null, null);
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.currency_2.getValue());
        JSONObject pushData = new JSONObject();
        pushData.put("userId", userId);
        pushData.put("capitalType", UserCapitalTypeEnum.currency_2.getValue());
        pushData.put("balance", userCapital.getBalance());
        Push.push(PushCode.updateUserCapital, managerSocketService.getServerIdByUserId(userId), pushData);
    }

    public void pushCapitalUpdate(Long userId, LogCapitalTypeEnum em, BigDecimal balance) {
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, em.getValue());
        JSONObject pushData = new JSONObject();
        pushData.put("userId", userId);
        pushData.put("capitalType", em.getValue());
        pushData.put("balance", balance);
        Push.push(PushCode.updateUserCapital, managerSocketService.getServerIdByUserId(userId), pushData);
    }

    public void pushCapitalUpdate(String userId, LogCapitalTypeEnum em, BigDecimal balance) {
        pushCapitalUpdate(Long.parseLong(userId), em, balance);
    }

    @Transactional
    @ServiceMethod(code = "700", description = "大逃杀结算")
    public JSONObject dtsSettle(ManagerDTSSocketServer adminSocketServer, JSONObject data) {
        checkNull(data);
        Set<String> set = data.keySet();
        LogCapitalTypeEnum em = null;
        Long userId = null;
        for (String key : set) {
            JSONObject o = JSONObject.parse(data.getString(key));
            em = LogCapitalTypeEnum.getEm(o.getIntValue("em"));
            userId = Long.parseLong(key);
        /*if (em.getValue() == LogCapitalTypeEnum.game_bet_food.getValue() || em.getValue() == LogCapitalTypeEnum.game_bet_win.getValue()) {
            userCacheService.addTodayUserPlayCount(userId);
            checkAchievementService.checkDailyTaskPlayerArea(userId);
        }*/
        }
        if (!data.isEmpty()) {
            userCapitalService.betUpdateBalance2(data);
        }
        for (String key : set) {
            JSONObject o = JSONObject.parse(data.getString(key));
            em = LogCapitalTypeEnum.getEm(o.getIntValue("em"));
            userId = Long.parseLong(key);
            UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.currency_2.getValue());
            JSONObject pushData = new JSONObject();
            pushData.put("userId", userId);
            if (em.getValue() == LogCapitalTypeEnum.game_bet_win.getValue()) {
                pushData.put("isDts", 1);
            }
            pushData.put("capitalType", UserCapitalTypeEnum.currency_2.getValue());
            pushData.put("balance", userCapital.getBalance());

            Push.push(PushCode.updateUserCapital, managerSocketService.getServerIdByUserId(userId), pushData);
        }


        return new JSONObject();
    }

    @Transactional
    @ServiceMethod(code = "801", description = "大逃杀下注修改内存")
    public JSONObject updateCacheByDts(ManagerDTSSocketServer adminSocketServer, JSONObject data) throws InterruptedException {
        checkNull(data);
        checkNull(data.get("betArray"));
        JSONArray betArray = data.getJSONArray("betArray");
        for (Object o : betArray) {
            try {
                JSONObject orderInfo = (JSONObject) o;
                String id = orderInfo.getString("userId");
                String orderNo = orderInfo.getString("orderNo");
                Long dataId = orderInfo.getLong("dataId");
                BigDecimal amount = orderInfo.getBigDecimal("betAmount");
                UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(Long.parseLong(id), UserCapitalTypeEnum.currency_2.getValue());
                BigDecimal beforeAmount = userCapital.getBalance();
                userCacheService.addTodayUserPlayCount(Long.valueOf(id));
                userCapitalCacheService.sub(Long.parseLong(id), UserCapitalTypeEnum.currency_2.getValue(), amount, BigDecimal.ZERO);
                userCapital = userCapitalCacheService.getUserCapitalCacheByType(Long.parseLong(id), UserCapitalTypeEnum.currency_2.getValue());
                userCapitalService.pushLog(1, Long.parseLong(id), UserCapitalTypeEnum.currency_2.getValue(), userCapital.getBalance(), userCapital.getOccupyBalance(), amount.negate(), LogCapitalTypeEnum.game_bet, orderNo, dataId, null);
                JSONObject pushData = new JSONObject();
                pushData.put("userId", id);
                pushData.put("capitalType", UserCapitalTypeEnum.currency_2.getValue());
                pushData.put("balance", userCapital.getBalance());
                Push.push(PushCode.updateUserCapital, managerSocketService.getServerIdByUserId(id), pushData);

            } catch (Exception e) {
                logger.error(e);
                e.printStackTrace();
            }
        }
        return new JSONObject();
    }

    @Transactional
    @ServiceMethod(code = "712", description = "算卦结算")
    public JSONObject sgSettle(ManagerSGSocketServer adminSocketServer, JSONObject data) {
        checkNull(data);
        Set<String> set = data.keySet();
        if (set.size() == 0) {
            return new JSONObject();
        }
        LogCapitalTypeEnum em = null;
        Long userId = null;
        userCapitalService.betUpdateBalance2(data);
        for (String key : set) {
            JSONObject o = JSONObject.parse(data.getString(key));
            em = LogCapitalTypeEnum.getEm(o.getIntValue("em"));
            userId = Long.parseLong(key);
            UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.currency_2.getValue());
            JSONObject pushData = new JSONObject();
            pushData.put("userId", userId);
            if (em.getValue() == LogCapitalTypeEnum.game_bet_win_sg.getValue()) {
                pushData.put("isDts", 1);
            }
            pushData.put("capitalType", UserCapitalTypeEnum.currency_2.getValue());
            pushData.put("balance", userCapital.getBalance());
            Push.push(PushCode.updateUserCapital, managerSocketService.getServerIdByUserId(userId), pushData);
        }


        return new JSONObject();
    }

    @Transactional
    @ServiceMethod(code = "812", description = "抽签下注修改内存")
    public JSONObject updateCacheBySg(ManagerSGSocketServer adminSocketServer, JSONObject data) throws InterruptedException {
        checkNull(data);
        checkNull(data.get("betArray"));
        JSONArray betArray = data.getJSONArray("betArray");
        for (Object o : betArray) {
            try {
                JSONObject orderInfo = (JSONObject) o;
                String id = orderInfo.getString("userId");
                String orderNo = orderInfo.getString("orderNo");
                Long dataId = orderInfo.getLong("dataId");
                BigDecimal amount = orderInfo.getBigDecimal("betAmount");
                userCacheService.addTodayUserPlayCount(Long.valueOf(id));
                userCapitalCacheService.sub(Long.parseLong(id), UserCapitalTypeEnum.currency_2.getValue(), amount, BigDecimal.ZERO);
                UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(Long.parseLong(id), UserCapitalTypeEnum.currency_2.getValue());
                userCapitalService.pushLog(1, Long.parseLong(id), UserCapitalTypeEnum.currency_2.getValue(), userCapital.getBalance(), userCapital.getOccupyBalance(), amount.negate(), LogCapitalTypeEnum.game_bet_sg, orderNo, dataId, null);
                JSONObject pushData = new JSONObject();
                pushData.put("userId", id);
                pushData.put("capitalType", UserCapitalTypeEnum.currency_2.getValue());
                pushData.put("balance", userCapital.getBalance());
                Push.push(PushCode.updateUserCapital, managerSocketService.getServerIdByUserId(id), pushData);
            } catch (Exception e) {
                logger.error(e);
                e.printStackTrace();
            }
        }
        return new JSONObject();
    }

}

package com.zywl.app.manager.service.manager;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.stream.Collectors;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.bean.PushBean;
import com.live.app.ws.enums.PushCode;
import com.live.app.ws.socket.BaseSocket;
import com.live.app.ws.util.DefaultPushHandler;
import com.live.app.ws.util.Push;
import com.zywl.app.base.bean.*;
import com.zywl.app.base.constant.RedisKeyConstant;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.base.util.LockUtil;
import com.zywl.app.base.util.OrderUtil;
import com.zywl.app.base.util.StringUtils;
import com.zywl.app.defaultx.annotation.KafkaProducer;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.cache.AppConfigCacheService;
import com.zywl.app.defaultx.cache.GameCacheService;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.cache.UserCapitalCacheService;
import com.zywl.app.defaultx.enmus.GameTypeEnum;
import com.zywl.app.defaultx.enmus.LogCapitalTypeEnum;
import com.zywl.app.defaultx.enmus.LogUserBackpackTypeEnum;
import com.zywl.app.defaultx.enmus.UserCapitalTypeEnum;
import com.zywl.app.defaultx.service.*;
import com.zywl.app.manager.context.KafkaEventContext;
import com.zywl.app.manager.context.KafkaTopicContext;
import com.zywl.app.manager.context.MessageCodeContext;
import com.zywl.app.manager.service.AliPayCashService;
import com.zywl.app.manager.service.CheckAchievementService;
import com.zywl.app.manager.service.PlayGameService;
import com.zywl.app.manager.service.WXCashService;
import com.zywl.app.manager.socket.*;
import org.apache.commons.collections4.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@ServiceClass(code = MessageCodeContext.CAPITAL_SERVER)
public class ManagerCapitalService extends BaseService {


    public static JSONObject itemResult =new JSONObject();

    public static JSONObject yybResult =new JSONObject();
// ====================== PBX（推箱子）Step2/Step3：记录页 & 周榜/上周榜 & 周结算分红 ======================
    /** 本周/某周：玩家投入(消耗)（weekKey -> {userId -> consumeCents}） */
    private static final ConcurrentHashMap<String, ConcurrentHashMap<Long, Long>> PBX_WEEK_USER_CONSUME_CENTS = new ConcurrentHashMap<>();
    /** 本周/某周：玩家净返还（weekKey -> {userId -> netReturnCents}） */
    private static final ConcurrentHashMap<String, ConcurrentHashMap<Long, Long>> PBX_WEEK_USER_NET_RETURN_CENTS = new ConcurrentHashMap<>();
    /** 本周/某周：玩家周榜分红（weekKey -> {userId -> dividendCents}） */
    private static final ConcurrentHashMap<String, ConcurrentHashMap<Long, Long>> PBX_WEEK_USER_DIVIDEND_CENTS = new ConcurrentHashMap<>();

    /** 本周/某周：总投入(消耗)（weekKey -> consumeCents） */
    private static final ConcurrentHashMap<String, Long> PBX_WEEK_TOTAL_CONSUME_CENTS = new ConcurrentHashMap<>();
    /** 本周/某周：总净返还（weekKey -> netReturnCents） */
    private static final ConcurrentHashMap<String, Long> PBX_WEEK_TOTAL_NET_RETURN_CENTS = new ConcurrentHashMap<>();
    /** 本周/某周：利润（派生值，weekKey -> profitCents） */
    private static final ConcurrentHashMap<String, Long> PBX_WEEK_TOTAL_PROFIT_CENTS = new ConcurrentHashMap<>();
    /** 本周/某周：分红池（派生值，weekKey -> dividendPoolCents） */
    private static final ConcurrentHashMap<String, Long> PBX_WEEK_TOTAL_DIVIDEND_POOL_CENTS = new ConcurrentHashMap<>();
// ========================= PBX 周榜/上周榜（200723） begin =========================

    /** 周榜：每周每人累计投注（分） */
    private static final Map<String, Map<Long, Long>> PBX_WEEK_USER_BET_CENTS = new ConcurrentHashMap<>();
    /** 周榜：每周总投注（分） */
    private static final Map<String, Long> PBX_WEEK_TOTAL_BET_CENTS = new ConcurrentHashMap<>();
    /** 周榜：每周总“返还(总额,含手续费前)”（分） */
    private static final Map<String, Long> PBX_WEEK_TOTAL_RETURN_CENTS = new ConcurrentHashMap<>();
    /** 周榜：每周总“实返(net)”（分） */
    private static final Map<String, Long> PBX_WEEK_TOTAL_NET_CENTS = new ConcurrentHashMap<>();
    /** 周榜：每周总手续费（分） */
    private static final Map<String, Long> PBX_WEEK_TOTAL_FEE_CENTS = new ConcurrentHashMap<>();
    /** 周榜奖池（分）：从每周利润按比例抽取进入 */
    private static final Map<String, Long> PBX_WEEK_RANK_POOL_CENTS = new ConcurrentHashMap<>();
    /** 周榜是否已结算 */
    private static final Map<String, Boolean> PBX_WEEK_SETTLED = new ConcurrentHashMap<>();
    /** 周结算后快照（用于“上周榜”展示），key=weekKey(yyyy-MM-dd) */
    private static final ConcurrentHashMap<String, JSONArray> PBX_WEEK_TOP10_SNAPSHOT = new ConcurrentHashMap<>();
    /** 已结算周榜 Top10 结果缓存（用于后续“上周榜查询”接口直接复用；阶段1先放内存） */
    private static final ConcurrentHashMap<String, JSONArray> PBX_WEEK_SETTLED_TOP10_CACHE = new ConcurrentHashMap<>();


    private transient Timer pbxWeekSettleTimer;

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
    private RoleService roleService;

    @Autowired
    private UserCacheService userCacheService;

    @Autowired
    private GameCacheService gameCacheService;

    @Autowired
    private AppConfigCacheService appConfigCacheService;

    /** l_game 配置读取 */
    @Autowired
    private GameService gameDbService;

    @Autowired
    private CashChannelIncomeRecordService cashChannelIncomeRecordService;


    @Autowired
    private UserStatisticService userStatisticService;

    @Autowired
    private ManagerConfigService managerConfigService;

    @Autowired
    private ManagerGameBaseService managerGameBaseService;


    @Autowired
    private PlayGameService gameService;


    @PostConstruct
    public void _ManagerCapitalService() {

        Push.addPushSuport(PushCode.updateUserBackpack, new DefaultPushHandler() {
            public void onRegist(BaseSocket baseSocket, PushBean pushBean) {
                if (baseSocket instanceof ManagerSocketServer) {
                    ManagerSocketServer managerSocketServer = (ManagerSocketServer) baseSocket;
                    pushBean.setCondition(managerSocketServer.getId());
                }
            }
        });

        Push.addPushSuport(PushCode.updateUserCapital, new DefaultPushHandler() {
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
            UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.rmb.getValue());
            BigDecimal balance = userCapital.getBalance();
            BigDecimal occupyBalance = userCapital.getOccupyBalance();
            if (balance.compareTo(amount) == -1) {
                throwExp("余额不足");
            }
            // 添加提现订单 扣除资产到冻结余额
            String orderNo = OrderUtil.getBatchOrder32Number();
            User user = userCacheService.getUserInfoById(userId);
            int isAutoPay = managerConfigService.getInteger(Config.IS_AUTO_PAY);
            Long cashOrderId = cashRecordService.addCashOrder(openId, userId, userNo, userName, realName, orderNo,
                    amount, type, user.getPhone(),1);
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
            if (amount.compareTo(BigDecimal.ZERO) == 0) {
                throwExp("请输入大于0的数值");
            }
            int type = data.getIntValue("type");
            int sourceType;
            int targetType;
            BigDecimal sourceAmount;
            if (type == 1) {
                sourceType = UserCapitalTypeEnum.currency_1.getValue();
                targetType = UserCapitalTypeEnum.currency_2.getValue();
                sourceAmount = managerConfigService.getBigDecimal(Config.CONVERT_RATE).multiply(amount);
            } else {
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
        userCapitalService.addUserBalanceByCancelBet(betAmount, userId, UserCapitalTypeEnum.yyb.getValue(), null, null);
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.yyb.getValue());
        JSONObject pushData = new JSONObject();
        pushData.put("userId", userId);
        pushData.put("capitalType", UserCapitalTypeEnum.yyb.getValue());
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
            userCapitalService.betUpdateBalance2(data,0);
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
    @ServiceMethod(code = "720", description = "推箱子(PBX)下注扣款")
    public JSONObject pbxBet(ManagerDTS2SocketServer adminSocketServer, JSONObject data) {
        checkNull(data);
        checkNull(data.get("userId"), data.get("betAmount"));

        Long userId = data.getLong("userId");
        BigDecimal betAmount = data.getBigDecimal("betAmount");
        if (betAmount == null) {
            throwExp("betAmount 不能为空");
        }
        betAmount = betAmount.setScale(2, java.math.RoundingMode.HALF_UP);
        if (betAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throwExp("betAmount 非法");
        }

        Integer capitalType = data.getInteger("capitalType");
        if (capitalType == null || capitalType == 0) {
            capitalType = 1002;
        }

        BigDecimal feeRate = data.getBigDecimal("feeRate");
        if (feeRate == null) {
            feeRate = new BigDecimal("0.05");
        }

        String orderNo = data.getString("orderNo");
        if (orderNo == null || orderNo.trim().isEmpty()) {
            orderNo = OrderUtil.getOrder5Number();
        }
        synchronized (LockUtil.getlock(String.valueOf(userId))) {
            JSONObject one = new JSONObject();
            one.put("amount", betAmount.negate());
            one.put("capitalType", capitalType);
            one.put("em", LogCapitalTypeEnum.game_bet_pbx.getValue());
            one.put("orderNo", orderNo);

            JSONObject betObj = new JSONObject();
            betObj.put(String.valueOf(userId), one);
            // 批量更新用户资产余额
            userCapitalService.betUpdateBalance2(betObj,capitalType);

            // 推送资产变更
            UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, capitalType);
            JSONObject pushData = new JSONObject();
            pushData.put("userId", userId);
            pushData.put("capitalType", capitalType);
            pushData.put("balance", userCapital.getBalance());
            Push.push(PushCode.updateUserCapital, managerSocketService.getServerIdByUserId(userId), pushData);

            // 手续费入奖池
            String weekKey = DateUtil.getFirstDayOfWeek(new Date());
            String poolKey = RedisKeyConstant.PRIZE_POOL + "pbx:" + weekKey;

            long betCents = betAmount.multiply(new BigDecimal("100"))
                    .setScale(0, java.math.RoundingMode.HALF_UP)
                    .longValue();

            gameCacheService.incr(poolKey, betCents);
            gameCacheService.expire(poolKey, 86400 * 14);

            // 下注成功 -> 计入本周投入排行（周榜）
            pbxWeekOnBet(data.getIntValue("gameId"), userId, betCents, weekKey);

            String poolCentsStr = gameCacheService.get(poolKey);
            BigDecimal poolBalance = BigDecimal.ZERO;
            if (poolCentsStr != null) {
                poolBalance = new BigDecimal(poolCentsStr).divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
            }

            // fee单位统一为BigDecimal 两位小数
            BigDecimal fee = BigDecimal.ZERO.setScale(2, java.math.RoundingMode.HALF_UP);
            // 回包给 DTS2
            JSONObject result = new JSONObject();
            result.put("success", true);
            result.put("gameId", data.getString("gameId"));
            result.put("userId", String.valueOf(userId));
            result.put("orderNo", orderNo);
            result.put("betAmount", betAmount);
            result.put("feeRate", feeRate);
            result.put("fee", fee);
            result.put("balance", userCapital.getBalance());
            result.put("poolBalance", poolBalance);
            return result;
        }
    }

    @Transactional
    @ServiceMethod(code = "721", description = "推箱子(PBX)派奖/结算（奖池扣净额net，手续费fee为系统收益）")
    public JSONObject pbxSettle(ManagerDTS2SocketServer adminSocketServer, JSONObject data) {
        checkNull(data);
        checkNull(data.get("gameId"), data.get("periodNo"));
        String week = DateUtil.getFirstDayOfWeek(new Date());
        // ---- 基础参数 ----
        String gameId = data.getString("gameId");
        String periodNo = data.getString("periodNo");

        Integer capitalType = data.getInteger("capitalType");
        if (capitalType == null || capitalType == 0) {
            capitalType = 1002;
        }

        BigDecimal feeRate = data.getBigDecimal("feeRate");
        if (feeRate == null) {
            feeRate = new BigDecimal("0.05");
        }

        // em：允许 DTS2 指定；不指定则用通用 win 类型，避免硬依赖枚举常量导致编译失败
        int emValue;
        if (data.containsKey("em")) {
            emValue = data.getIntValue("em");
        } else {
            emValue = LogCapitalTypeEnum.game_bet_win_pbx.getValue();
        }

        // ---- 解析 winList（支持单人/多人）----
        JSONArray winList = data.getJSONArray("winList");
        if (winList == null) {
            // 兼容：单人结算模式（userId + returnAmount）
            if (data.containsKey("userId") && data.containsKey("returnAmount")) {
                winList = new JSONArray();
                JSONObject one = new JSONObject();
                one.put("userId", data.get("userId"));
                one.put("returnAmount", data.get("returnAmount"));
                winList.add(one);
            } else {
                throwExp("winList 不能为空");
            }
        }
        if (winList.size() == 0) {
            // 空结算：只返回当前奖池
            JSONObject empty = new JSONObject();
            empty.put("success", true);
            empty.put("gameId", gameId);
            empty.put("periodNo", periodNo);
            empty.put("totalReturnAmount", BigDecimal.ZERO);
            empty.put("totalFee", BigDecimal.ZERO);
            empty.put("totalNet", BigDecimal.ZERO);


            String poolKey0 = RedisKeyConstant.PRIZE_POOL + "pbx:" + week;
            String poolCentsStr0 = gameCacheService.get(poolKey0);
            BigDecimal poolBalance0 = BigDecimal.ZERO;
            if (poolCentsStr0 != null) {
                poolBalance0 = new BigDecimal(poolCentsStr0).divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
            }
            empty.put("poolBalance", poolBalance0);
            empty.put("userList", new JSONArray());
            return empty;
        }

        // ---- 奖池 key（与 200720 同口径：按周）----

        String poolKey = RedisKeyConstant.PRIZE_POOL + "pbx:" + week;

        // ---- 先核算总额（统一两位小数 HALF_UP -> cents）----
        BigDecimal totalReturn = BigDecimal.ZERO;
        BigDecimal totalFee = BigDecimal.ZERO;
        BigDecimal totalNet = BigDecimal.ZERO;

        // 先整理一个标准化列表，后面要用两次（验奖池 + 批量入账）
        List<JSONObject> normalized = new ArrayList<>();

        for (Object o : winList) {
            JSONObject w = (JSONObject) o;

            Object uidObj = w.get("userId");
            if (uidObj == null) {
                throwExp("winList.userId 不能为空");
            }
            Long uid = (uidObj instanceof Number) ? ((Number) uidObj).longValue() : Long.parseLong(uidObj.toString());

            BigDecimal returnAmount = w.getBigDecimal("returnAmount");
            if (returnAmount == null) {
                throwExp("winList.returnAmount 不能为空");
            }
            returnAmount = returnAmount.setScale(2, java.math.RoundingMode.HALF_UP);
            if (returnAmount.compareTo(BigDecimal.ZERO) < 0) {
                throwExp("returnAmount 非法");
            }
            if (returnAmount.compareTo(BigDecimal.ZERO) == 0) {
                // 允许 0：比如没中奖的人也被带进来，直接跳过入账
                continue;
            }

            BigDecimal fee = returnAmount.multiply(feeRate).setScale(2, java.math.RoundingMode.HALF_UP);
            BigDecimal net = returnAmount.subtract(fee).setScale(2, java.math.RoundingMode.HALF_UP);
            if (net.compareTo(BigDecimal.ZERO) < 0) {
                throwExp("net 非法");
            }

            JSONObject n = new JSONObject();
            n.put("userId", uid);
            n.put("returnAmount", returnAmount);
            n.put("fee", fee);
            n.put("net", net);
            normalized.add(n);

            totalReturn = totalReturn.add(returnAmount);
            totalFee = totalFee.add(fee);
            totalNet = totalNet.add(net);
        }

        // 若全是 0，直接返回当前奖池
        if (normalized.size() == 0) {
            JSONObject empty2 = new JSONObject();
            empty2.put("success", true);
            empty2.put("gameId", gameId);
            empty2.put("periodNo", periodNo);
            empty2.put("totalReturnAmount", BigDecimal.ZERO);
            empty2.put("totalFee", BigDecimal.ZERO);
            empty2.put("totalNet", BigDecimal.ZERO);

            String poolCentsStr = gameCacheService.get(poolKey);
            BigDecimal poolBalance = BigDecimal.ZERO;
            if (poolCentsStr != null) {
                poolBalance = new BigDecimal(poolCentsStr).divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
            }
            empty2.put("poolBalance", poolBalance);
            empty2.put("userList", new JSONArray());
            return empty2;
        }

        long totalNetCents = totalNet.multiply(new BigDecimal("100")).longValue();

        // ---- 关键：奖池扣净额（net），不足则直接失败（由 DTS2 决定“让其输”时不调用 200721）----
        synchronized (LockUtil.getlock("pbx_pool_" + week)) {
            String poolCentsStr = gameCacheService.get(poolKey);
            long poolCents = 0L;
            if (poolCentsStr != null && poolCentsStr.trim().length() > 0) {
                poolCents = Long.parseLong(poolCentsStr);
            }

            if (poolCents < totalNetCents) {
                throwExp("推箱子奖池不足，无法派奖！当前奖池: " +
                        new BigDecimal(poolCents).divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP) +
                        "，需要: " + totalNet);
            }

            // 1) 先扣奖池（扣净额 net）
            gameCacheService.decr(poolKey, totalNetCents);
            gameCacheService.expire(poolKey, 86400 * 14);

            // 2) 批量给用户加钱（net 入账）
            JSONObject batch = new JSONObject();
            for (JSONObject n : normalized) {
                Long uid = n.getLong("userId");
                BigDecimal net = n.getBigDecimal("net");

                JSONObject one = new JSONObject();
                one.put("amount", net); // 正数：加钱
                one.put("capitalType", capitalType);
                one.put("em", emValue);
                one.put("orderNo", periodNo); // 用 periodNo 做结算单号，便于对账/幂等（若你后续加唯一约束可继续扩展）
                batch.put(String.valueOf(uid), one);
            }
            userCapitalService.betUpdateBalance2(batch, capitalType);

            // 3) 推送每个用户资产变更 + 组装回包
            JSONArray userResult = new JSONArray();
            for (JSONObject n : normalized) {
                Long uid = n.getLong("userId");
                UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(uid, capitalType);

                JSONObject pushData = new JSONObject();
                pushData.put("userId", uid);
                pushData.put("capitalType", capitalType);
                pushData.put("balance", userCapital.getBalance());
                Push.push(PushCode.updateUserCapital, managerSocketService.getServerIdByUserId(uid), pushData);

                JSONObject ur = new JSONObject();
                ur.put("userId", String.valueOf(uid));
                ur.put("returnAmount", n.getBigDecimal("returnAmount"));
                ur.put("fee", n.getBigDecimal("fee"));
                ur.put("net", n.getBigDecimal("net"));
                ur.put("balance", userCapital.getBalance());
                userResult.add(ur);
            }

            // 4) 返回最新奖池
            String newPoolCentsStr = gameCacheService.get(poolKey);
            BigDecimal poolBalance = BigDecimal.ZERO;
            if (newPoolCentsStr != null && newPoolCentsStr.trim().length() > 0) {
                poolBalance = new BigDecimal(newPoolCentsStr).divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
            }
            BigDecimal totalReturnAmount = totalReturn.setScale(2, java.math.RoundingMode.HALF_UP);

            JSONObject result = new JSONObject();
            result.put("success", true);
            result.put("gameId", gameId);
            result.put("periodNo", periodNo);
            result.put("capitalType", capitalType);
            result.put("feeRate", feeRate);

            result.put("totalReturnAmount", totalReturn.setScale(2, java.math.RoundingMode.HALF_UP));
            result.put("totalFee", totalFee.setScale(2, java.math.RoundingMode.HALF_UP));
            result.put("totalNet", totalNet.setScale(2, java.math.RoundingMode.HALF_UP));
            result.put("poolBalance", poolBalance);
            // 周榜统计：累计本周“净返还”与个人净返还
            BigDecimal totalNetReturnAmount = totalNet.setScale(2, java.math.RoundingMode.HALF_UP);
            pbxWeekOnSettle(
                    data.getIntValue("gameId"),
                    week,
                    bdToCents(totalReturnAmount),
                    bdToCents(totalNetReturnAmount),
                    bdToCents(totalFee)
            );
            result.put("userList", userResult);
            return result;
        }
    }

    @Transactional(readOnly = true)
    @ServiceMethod(code = "722", description = "推箱子(PBX)查询（奖池/记录/榜单）")
    public JSONObject pbxQuery(ManagerDTS2SocketServer adminSocketServer, JSONObject data) {
        checkNull(data);
        String gameId = data.getString("gameId");
        if (gameId == null || gameId.trim().isEmpty()) {
            gameId = "12";
        }

        // 1) 查询本周奖池（和 pbxBet 的写法保持一致）
        String week = com.zywl.app.base.util.DateUtil.getFirstDayOfWeek(new java.util.Date());
        String poolKey = RedisKeyConstant.PRIZE_POOL + "pbx:" + week;

        String poolCentsStr = gameCacheService.get(poolKey);
        java.math.BigDecimal poolBalance = java.math.BigDecimal.ZERO;
        if (poolCentsStr != null) {
            poolBalance = new java.math.BigDecimal(poolCentsStr)
                    .divide(new java.math.BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
        }

        // 2) 这里先回最小可用字段：奖池 + serverTime
        // 后续你要加“近100期统计/周榜/上周榜”再扩展字段与数据源（DB表/Redis集合）
        JSONObject result = new JSONObject();
        result.put("success", true);
        result.put("gameId", gameId);
        result.put("poolBalance", poolBalance);
        result.put("serverTime",nowTimeMs());
        return result;
    }


// ===================== PBX 周榜/上周榜：接口 + helper（阶段1：内存版）=====================

    /**
     * 200723：PBX 周榜结算（用于结算“上周榜”）
     * - weekKey 为空：默认结算“上周”（取今天-7天所在周的周一）
     * - top10Rates / rankProfitPercent 若不传：从 l_game.game_setting 读取；再缺省则给默认值
     */
    @ServiceMethod(code = "200723", description = "PBX 周榜结算（上周榜）")
    public JSONObject pbxWeekSettle(JSONObject data) {
        int gameId = data.getIntValue("gameId");
        if (gameId <= 0) {
            gameId = 12;
        }

        String weekKey = data.getString("weekKey");
        if (StringUtils.isBlank(weekKey)) {
            Date d = new Date(System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000);
            weekKey = DateUtil.getFirstDayOfWeek(d);
        }

        // 读取 l_game.game_setting
        JSONObject setting = pbxLoadGameSettingByGameId(gameId);
        BigDecimal rankProfitPercent = null;
        JSONArray top10Rates = null;
        if (setting != null) {
            rankProfitPercent = setting.getBigDecimal("rankProfitPercent");
            top10Rates = setting.getJSONArray("top10Rates");
        }

        // 兜底：如果没有配置，就按 50% 与等分 10 份
        if (rankProfitPercent == null) {
            rankProfitPercent = new BigDecimal("0.50");
        }
        if (top10Rates == null || top10Rates.isEmpty()) {
            top10Rates = new JSONArray();
            for (int i = 0; i < 10; i++) {
                top10Rates.add(new BigDecimal("0.10"));
            }
        }

        return pbxWeekSettleInternal(gameId, weekKey, rankProfitPercent, top10Rates);
    }

    private JSONObject pbxWeekSettleInternal(int gameId, String weekKey, BigDecimal rankProfitPercent, JSONArray top10Rates) {
        JSONObject result = new JSONObject();
        result.put("gameId", gameId);
        result.put("weekKey", weekKey);

        Boolean settled = PBX_WEEK_SETTLED.getOrDefault(weekKey, Boolean.FALSE);
        if (Boolean.TRUE.equals(settled)) {
            result.put("success", false);
            result.put("msg", "本周已结算");
            result.put("settled", true);
            result.put("poolBalance", centsToBd(PBX_WEEK_RANK_POOL_CENTS.getOrDefault(weekKey, 0L)));
            return result;
        }

        long totalBetCents = PBX_WEEK_TOTAL_BET_CENTS.getOrDefault(weekKey, 0L);
        long totalNetCents = PBX_WEEK_TOTAL_NET_CENTS.getOrDefault(weekKey, 0L);
        long profitCents = totalBetCents - totalNetCents;
        if (profitCents < 0) {
            profitCents = 0;
        }

        // 计算进入周榜奖池的金额（分）
        BigDecimal profitBd = centsToBd(profitCents);
        BigDecimal rankPoolBd = profitBd.multiply(rankProfitPercent).setScale(2, RoundingMode.HALF_UP);
        long rankPoolCents = bdToCents(rankPoolBd);

        // 写入周榜奖池（累计）
        PBX_WEEK_RANK_POOL_CENTS.merge(weekKey, rankPoolCents, Long::sum);
        long poolCents = PBX_WEEK_RANK_POOL_CENTS.getOrDefault(weekKey, 0L);

        // 取 Top10（按投注额倒序）
        Map<Long, Long> userBetMap = PBX_WEEK_USER_BET_CENTS.getOrDefault(weekKey, new HashMap<>());
        List<Map.Entry<Long, Long>> entries = new ArrayList<>(userBetMap.entrySet());
        entries.sort((a, b) -> Long.compare(b.getValue(), a.getValue()));
        if (entries.size() > 10) {
            entries = entries.subList(0, 10);
        }

        // 发放（币种固定 1002）
        int capitalType = 1002;
        BigDecimal poolLeft = centsToBd(poolCents);
        JSONArray userResult = new JSONArray();

        // 发币批次：最终统一走 betUpdateBalance2
        JSONObject payBatch = new JSONObject();

        for (int i = 0; i < entries.size(); i++) {
            int rank = i + 1;
            long uid = entries.get(i).getKey();
            long betC = entries.get(i).getValue();

            BigDecimal rate = BigDecimal.ZERO;
            if (i < top10Rates.size()) {
                rate = top10Rates.getBigDecimal(i);
            }

            BigDecimal award = centsToBd(poolCents).multiply(rate).setScale(2, RoundingMode.HALF_UP);
            if (award.compareTo(BigDecimal.ZERO) < 0) {
                award = BigDecimal.ZERO;
            }
            if (award.compareTo(poolLeft) > 0) {
                award = poolLeft;
            }

            if (award.compareTo(BigDecimal.ZERO) > 0) {
                JSONObject one = new JSONObject();
                one.put("amount", award);

                // 复用工程已有 PBX 中奖枚举，保证不额外改 enum 也能编译通过
                one.put("em", LogCapitalTypeEnum.game_bet_win_pbx.getValue());

                one.put("orderNo", "PBX_WEEK_RANK_" + weekKey);
                one.put("tableName", "pbx_week_rank");
                payBatch.put(String.valueOf(uid), one);
            }

            poolLeft = poolLeft.subtract(award).setScale(2, RoundingMode.HALF_UP);

            JSONObject ur = new JSONObject();
            ur.put("rank", rank);
            ur.put("userId", uid);
            ur.put("betAmount", centsToBd(betC));
            ur.put("rate", rate);
            ur.put("award", award);
            userResult.add(ur);
        }

        // 执行批量发币（与你工程 pbxSettle 现有写法一致：走 betUpdateBalance2）
        if (!payBatch.isEmpty()) {
            userCapitalService.betUpdateBalance2(payBatch, capitalType);

            // 补充返回余额：从缓存读取最新余额（betUpdateBalance2 内部会清缓存并重建）
            for (int i = 0; i < userResult.size(); i++) {
                JSONObject ur = userResult.getJSONObject(i);
                Long uid = ur.getLong("userId");
                if (uid != null) {
                    try {
                        UserCapital uc = userCapitalCacheService.getUserCapitalCacheByType(uid, capitalType);
                        if (uc != null) {
                            ur.put("balance", uc.getBalance());
                        }
                    } catch (Exception ignore) {
                    }
                }
            }
        }

        // 回写奖池剩余（分）
        PBX_WEEK_RANK_POOL_CENTS.put(weekKey, bdToCents(poolLeft));
        PBX_WEEK_SETTLED.put(weekKey, true);

        result.put("success", true);
        result.put("settled", true);
        result.put("totalBet", centsToBd(totalBetCents));
        result.put("totalNet", centsToBd(totalNetCents));
        result.put("profit", centsToBd(profitCents));
        result.put("rankProfitPercent", rankProfitPercent);
        result.put("poolAdd", rankPoolBd);
        result.put("poolBalance", poolLeft);
        result.put("userList", userResult);
        return result;
    }

    /** 下注统计：投入累加到本周 */
    private void pbxWeekOnBet(int gameId, long userId, long betCents, String weekKey) {
        if (StringUtils.isBlank(weekKey)) {
            weekKey = DateUtil.getFirstDayOfWeek(new Date());
        }
        Map<Long, Long> userBetMap = PBX_WEEK_USER_BET_CENTS.computeIfAbsent(weekKey, k -> new ConcurrentHashMap<>());
        userBetMap.merge(userId, betCents, Long::sum);
        PBX_WEEK_TOTAL_BET_CENTS.merge(weekKey, betCents, Long::sum);
    }

    /** 结算时累计周榜返还/实返/手续费（在 pbxSettle(200721) 完成派奖后调用） */
    private void pbxWeekOnSettle(int gameId, String weekKey, long totalReturnCents, long totalNetCents, long totalFeeCents) {
        if (StringUtils.isBlank(weekKey)) {
            weekKey = DateUtil.getFirstDayOfWeek(new Date());
        }
        PBX_WEEK_TOTAL_RETURN_CENTS.merge(weekKey, totalReturnCents, Long::sum);
        PBX_WEEK_TOTAL_NET_CENTS.merge(weekKey, totalNetCents, Long::sum);
        PBX_WEEK_TOTAL_FEE_CENTS.merge(weekKey, totalFeeCents, Long::sum);
    }

    /** 从 l_game.game_setting 读取配置：rankProfitPercent/top10Rates 等 */
    private JSONObject pbxLoadGameSettingByGameId(int gameId) {
        try {
            Game g = gameDbService.findGameById((long) gameId);
            if (g == null) {
                return null;
            }
            String gs = g.getGameSetting();
            if (StringUtils.isBlank(gs)) {
                return null;
            }
            return JSONObject.parseObject(gs);
        } catch (Exception e) {
            return null;
        }
    }

    private long bdToCents(BigDecimal amount) {
        if (amount == null) {
            return 0L;
        }
        return amount.multiply(new BigDecimal("100"))
                .setScale(0, RoundingMode.HALF_UP)
                .longValue();
    }

    private BigDecimal centsToBd(long cents) {
        return new BigDecimal(cents)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }

// ===================== PBX 周榜/上周榜：接口 + helper（阶段1：内存版）=====================




    @Transactional
    @ServiceMethod(code = "801", description = "大逃杀下注修改内存")
    public JSONObject updateCacheByDts(ManagerDTS2SocketServer adminSocketServer, JSONObject data) throws InterruptedException {
        checkNull(data);
        checkNull(data.get("betArray"));
        JSONArray betArray = data.getJSONArray("betArray");
        for (Object o : betArray) {
            try {
                JSONObject orderInfo = (JSONObject) o;
                gameService.updateDtsData(null,orderInfo);
            } catch (Exception e) {
                logger.error(e);
                e.printStackTrace();
            }
        }
        return new JSONObject();
    }
    @Transactional
    @ServiceMethod(code = "901", description = "聂小倩修改内存")
    public JSONObject updateCacheByNxq(ManagerDTS2SocketServer adminSocketServer, JSONObject data) throws InterruptedException {
        checkNull(data);
        checkNull(data.get("betArray"));
        JSONArray betArray = data.getJSONArray("betArray");
        for (Object o : betArray) {
            try {
                JSONObject orderInfo = (JSONObject) o;
                gameService.updateNxqData(null,orderInfo);
            } catch (Exception e) {
                logger.error(e);
                e.printStackTrace();
            }
        }
        return new JSONObject();
    }
    @Transactional
    @ServiceMethod(code = "808", description = "打怪兽修改内存")
    public JSONObject updateCacheByDgs( JSONObject data) throws InterruptedException {
        checkNull(data);
        checkNull(data.get("betArray"));
        JSONArray betArray = data.getJSONArray("betArray");
        for (Object o : betArray) {
            try {
                JSONObject orderInfo = (JSONObject) o;
                gameService.updateDgsData(null,orderInfo);
            } catch (Exception e) {
                logger.error(e);
                e.printStackTrace();
            }
        }
        return new JSONObject();
    }



    @Transactional
    @ServiceMethod(code = "811", description = "2选1投入修改内存")
    public JSONObject updateCacheByDts(ManagerLhdSocketServer lhdSocketServer, JSONObject data) throws InterruptedException {
        checkNull(data);
        checkNull(data.get("betArray"));
        JSONArray betArray = data.getJSONArray("betArray");
        for (Object o : betArray) {
            try {
                JSONObject orderInfo = (JSONObject) o;
                gameService.updateLhdData(null,orderInfo);
            } catch (Exception e) {
                logger.error(e);
                e.printStackTrace();
            }
        }
        return new JSONObject();
    }

    @Transactional
    @ServiceMethod(code = "911", description = "2选1投入修改内存")
    public JSONObject updateCacheByNxq(JSONObject data) throws InterruptedException {
        checkNull(data);
        checkNull(data.get("betArray"));
        JSONArray betArray = data.getJSONArray("betArray");
        for (Object o : betArray) {
            try {
                JSONObject orderInfo = (JSONObject) o;
                gameService.updateNXQData(null,orderInfo);
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
        userCapitalService.betUpdateBalance2(data,UserCapitalTypeEnum.currency_2.getValue());
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
                gameService.updateSg(null,orderInfo);
            } catch (Exception e) {
                logger.error(e);
                e.printStackTrace();
            }
        }
        return new JSONObject();
    }

    @Transactional
    @ServiceMethod(code = "822", description = "大逃杀免伤修改内存")
    public JSONObject updateCacheByDtsRank(ManagerDTS2SocketServer adminSocketServer, JSONObject data) throws InterruptedException {
        checkNull(data);
        checkNull(data.get("betArray"));
        JSONArray betArray = data.getJSONArray("betArray");
        for (Object o : betArray) {
            try {
                JSONObject orderInfo = (JSONObject) o;
                String id = orderInfo.getString("userId");
                String orderNo = orderInfo.getString("orderNo");
                BigDecimal amount = orderInfo.getBigDecimal("betAmount");
                UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(Long.parseLong(id), UserCapitalTypeEnum.yyb.getValue());
                BigDecimal beforeAmount = userCapital.getBalance();
                userCapitalCacheService.add(Long.parseLong(id), UserCapitalTypeEnum.yyb.getValue(), amount, BigDecimal.ZERO);
                userCapital = userCapitalCacheService.getUserCapitalCacheByType(Long.parseLong(id), UserCapitalTypeEnum.yyb.getValue());
                userCapitalService.pushLog(1, Long.parseLong(id), UserCapitalTypeEnum.yyb.getValue(), userCapital.getBalance(), userCapital.getOccupyBalance(), amount, LogCapitalTypeEnum.dts_rank_rebate, orderNo, null, null);
                JSONObject pushData = new JSONObject();
                pushData.put("userId", id);
                pushData.put("capitalType", UserCapitalTypeEnum.yyb.getValue());
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
    @ServiceMethod(code = "710", description = "倩女幽魂结算")
    public JSONObject dtsSettle(ManagerDTS2SocketServer adminSocketServer, JSONObject data) {
        checkNull(data);
        Set<String> set = data.keySet();
        if (set.size() == 0) {
            return new JSONObject();
        }
        LogCapitalTypeEnum em = null;
        Long userId = null;
        for (String key : set) {
            JSONObject o = JSONObject.parse(data.getString(key));
            em = LogCapitalTypeEnum.getEm(o.getIntValue("em"));
            userId = Long.parseLong(key);
        }
        userCapitalService.betUpdateBalance2(data,UserCapitalTypeEnum.yyb.getValue());
        for (String key : set) {
            JSONObject o = JSONObject.parse(data.getString(key));
            em = LogCapitalTypeEnum.getEm(o.getIntValue("em"));
            userId = Long.parseLong(key);
            UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.yyb.getValue());
            JSONObject pushData = new JSONObject();
            pushData.put("userId", userId);
            if (em.getValue() == LogCapitalTypeEnum.game_bet_win_dts2.getValue()) {
                pushData.put("isDts", 1);
            }
            pushData.put("capitalType", UserCapitalTypeEnum.yyb.getValue());
            pushData.put("balance", userCapital.getBalance());
            Push.push(PushCode.updateUserCapital, managerSocketService.getServerIdByUserId(userId), pushData);
        }


        return new JSONObject();
    }

    @Transactional
    @ServiceMethod(code = "711", description = "2选1结算")
    public JSONObject nhSettle(ManagerLhdSocketServer adminSocketServer, JSONObject data) {
        checkNull(data);
        Set<String> set = data.keySet();
        LogCapitalTypeEnum em = null;
        Long userId = null;
        if (data.size()==0){
            return new JSONObject();
        }
        userCapitalService.betUpdateBalance2(data,UserCapitalTypeEnum.yyb.getValue());
        addItem(data);
        for (String key : set) {
            JSONObject o = JSONObject.parse(data.getString(key));
            em = LogCapitalTypeEnum.getEm(o.getIntValue("em"));
            userId = Long.parseLong(key);
            UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.yyb.getValue());
            JSONObject pushData = new JSONObject();
            if (em.getValue() == LogCapitalTypeEnum.game_bet_win_nh.getValue()) {
                pushData.put("isDts", 1);
            }
            pushData.put("userId", userId);
            pushData.put("capitalType", UserCapitalTypeEnum.yyb.getValue());
            pushData.put("balance", userCapital.getBalance());
            Push.push(PushCode.updateUserCapital, managerSocketService.getServerIdByUserId(userId), pushData);
        }
        return new JSONObject();
    }


    public void addItem(JSONObject data){
        Set<String> set = data.keySet();
        for (String userId : set) {
            JSONObject o = data.getJSONObject(userId);
            if (o.getBigDecimal("amount").compareTo(BigDecimal.ZERO)==0){
                BigDecimal getFz = o.getBigDecimal("getFz");
                double v = Double.parseDouble(getFz.toString());
                gameService.updateUserBackpack(userId,"47",v,LogUserBackpackTypeEnum.game);
                gameService.updateUserBackpack(userId,"48",v,LogUserBackpackTypeEnum.game);
            }
        }

    }

    @Transactional
    @ServiceMethod(code = "713", description = "打怪兽结算")
    public JSONObject DgsSettle(ManagerDgsSocketServer adminSocketServer, JSONObject data) {
        checkNull(data);
        Set<String> set = data.keySet();
        Long userId = null;
        if (data.size()==0){
            return new JSONObject();
        }
        betUpdateBalanceOrItem(data);

       /* for (String key : set) {
            JSONObject o = JSONObject.parse(data.getString(key));
            userId = Long.parseLong(key);
            UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.yyb.getValue());
            JSONObject pushData = new JSONObject();
            pushData.put("userId", userId);
            pushData.put("capitalType", UserCapitalTypeEnum.yyb.getValue());
            pushData.put("balance", userCapital.getBalance());
            pushData.put("type", o.getIntValue("type"));
            if(1==o.getIntValue("type")){
                pushData.put("amount", o.get("amount"));
                pushData.put("id", userCapital.getId());
                Push.push(PushCode.updateUserCapital, managerSocketService.getServerIdByUserId(userId), pushData);

            }
        }*/
        return new JSONObject();
    }

    @Transactional
    public void betUpdateBalanceOrItem(JSONObject obj) {
        int capitalType = UserCapitalTypeEnum.yyb.getValue();
        List<Map<String, Object>> list = new ArrayList<>();
        Set<String> set = obj.keySet();
        LogCapitalTypeEnum em = LogCapitalTypeEnum.dgs_bet;
        Map<String, BigDecimal> beforeMoney = new HashMap<>();
        for (String key : set) {
            Map<String, Object> map = new HashedMap<>();
            map.put("userId", key);
            UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(Long.parseLong(key), UserCapitalTypeEnum.yyb.getValue());
            beforeMoney.put(key, userCapital.getBalance());
            JSONObject o = JSONObject.parse(obj.getString(key));
            if(1==o.getIntValue("type")){
                /*map.put("amount", o.get("amount"));*/
                map.put("amount", 0);
                map.put("id",userCapital.getId());
                /*em = LogCapitalTypeEnum.getEm(o.getIntValue("em"));*/
                map.put("capitalType", capitalType);
                list.add(map);
            } else if(2==o.getIntValue("type")){
                Integer monsterType = o.getIntValue("monsterType");
                if(1==monsterType){
                    gameService.updateUserBackpack(o.getLong("userId"), "42", +1, LogUserBackpackTypeEnum.zs);
                }else if(10 ==  monsterType){
                    gameService.updateUserBackpack(o.getLong("userId"), "42", +10, LogUserBackpackTypeEnum.zs);
                }
                else if(100 ==  monsterType){
                    gameService.updateUserBackpack(o.getLong("userId"), "42", +100, LogUserBackpackTypeEnum.zs);
                }
            }
        }

       /* int a = userCapitalService.updateUserCapital(list);
        if (a < 1) {
            for (String key : set) {
                userCapitalCacheService.deltedUserCapitalCache(Long.parseLong(key), UserCapitalTypeEnum.yyb.getValue());
            }
            if (em.getValue() == LogCapitalTypeEnum.game_bet.getValue()) {
                throwExp("失败！");
            } else {
                throwExp("结算失败！");
            }
        }
        if (a > 0) {
            for (String userId : set) {
                BigDecimal before;
                if (!beforeMoney.containsKey(userId)) {
                    before = userCapitalCacheService.getUserCapitalCacheByType(Long.parseLong(userId), UserCapitalTypeEnum.yyb.getValue()).getBalance();
                } else {
                    before = beforeMoney.get(userId);
                }
                BigDecimal occupyBefore = BigDecimal.ZERO;
                JSONObject o = (JSONObject) obj.get(userId);
                if(null != o.getBigDecimal("amount")){
                    userCapitalCacheService.add(Long.parseLong(userId), capitalType, o.getBigDecimal("amount"), BigDecimal.ZERO);
                    pushLog(1, Long.parseLong(userId), capitalType, before, occupyBefore, o.getBigDecimal("amount"), em, (String) o.getOrDefault("orderNo", null), null, (String) o.getOrDefault("tableName", null));
                }
            }
        }*/
    }

    public void pushLog(int type, Long userId, Integer capitalType, BigDecimal balanceBefore, BigDecimal occupyBalanceBefore, BigDecimal amount, LogCapitalTypeEnum em, String orderNo, Long sourceDataId, String tableName) {
        Map<String,Object> a = new HashedMap<>();
        a.put("logType", 1);
        a.put("type", type);
        a.put("userId", userId);
        a.put("capitalType", capitalType);
        a.put("balanceBefore", balanceBefore);
        a.put("occupyBalanceBefore", occupyBalanceBefore);
        a.put("amount", amount);
        a.put("em", em);
        a.put("orderNo", orderNo);
        a.put("sourceDataId", sourceDataId);
        a.put("tableName", tableName);
        Push.push(PushCode.insertLog, null, a);
    }
    private String nowTimeMs() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
    }

}

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
import com.zywl.app.base.exp.AppException;
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
import org.springframework.data.redis.core.ZSetOperations;
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

    /**
     * 周榜结算状态缓存
     */
    private static final Map<String, Boolean> PBX_WEEK_SETTLED = new ConcurrentHashMap<>();

    /**
     * 已结算 Top10 榜单快照
     */
    private static final Map<String, JSONArray> PBX_WEEK_SETTLED_TOP10_CACHE = new ConcurrentHashMap<>();

    /**
     * 实时榜单快照
     */
    private static final Map<String, JSONArray> PBX_WEEK_TOP10_SNAPSHOT = new ConcurrentHashMap<>();

    /**
     * 结算时计算出的分红奖池金额
     */
    private static final Map<String, Long> PBX_WEEK_RANK_POOL_CENTS = new ConcurrentHashMap<>();

    /**
     * 玩家周榜分红金额
     */
    private static final Map<String, Map<Long, Long>> PBX_WEEK_USER_AWARD_CENTS = new ConcurrentHashMap<>();


    private transient Timer pbxWeekSettleTimer;

    @Autowired
    private UserCapitalService userCapitalService;

    /** PBX 周榜汇总（DB） */
    @Autowired
    private PbxWeekSummaryService pbxWeekSummaryService;

    /** PBX 周榜结算事件（DB） */
    @Autowired
    private PbxWeekSettleEventService pbxWeekSettleEventService;

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

        this.pbxWeekSettleTimer = new Timer("pbx-week-settle-timer", true);
        this.pbxWeekSettleTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    checkAndSettlePbxLastWeek();
                } catch (Exception e) {
                    logger.error("[PBX] Auto settle timer error", e);
                }
            }
        }, 60 * 1000, 10 * 60 * 1000); // 延迟1分钟启动，每10分钟执行一次
    }

    /**
     * 定时任务逻辑：检查上周是否已结算，未结算则执行
     */
    private void checkAndSettlePbxLastWeek() {
        // 1. 计算上周的 Key
        // 逻辑：当前时间 - 7天，取那天的周一
        Date lastWeekDate = new Date(System.currentTimeMillis() - 7L * 24 * 3600 * 1000);
        String lastWeekKey = DateUtil.getFirstDayOfWeek(lastWeekDate);

        // 2. 检查 DB 是否已结算 (幂等检查)
        // 注意：这里 gameId 固定 12
        PbxWeekSummary summary = pbxWeekSummaryService.findByGameWeek(12, lastWeekKey);
        if (summary != null && summary.getSettled() == 1) {
            // 已结算，跳过
            return;
        }

        // 3. 执行结算 (复用已有的 pbxWeekSettleInternal)
        logger.info("[PBX] Auto triggering last week settle: " + lastWeekKey);

        // 构造默认参数
        JSONObject gameSetting = pbxLoadGameSettingByGameId(12);
        BigDecimal rankProfitPercent = (gameSetting != null) ? gameSetting.getBigDecimal("rankProfitPercent") : new BigDecimal("0.5");
        JSONArray top10Rates = (gameSetting != null) ? gameSetting.getJSONArray("top10Rates") : new JSONArray();

        // 调用结算
        // 注意：这里需要 try-catch 避免影响定时器
        try {
            pbxWeekSettleInternal(12, lastWeekKey, rankProfitPercent, top10Rates);
            logger.info("[PBX] Auto settle success: " + lastWeekKey);
        } catch (Exception e) {
            logger.error("[PBX] Auto settle failed: " + lastWeekKey, e);
        }
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

        // 1. 参数解析
        int gameId = data.containsKey("gameId") ? data.getIntValue("gameId") : 12;
        Long userId = data.getLong("userId");
        BigDecimal betAmount = data.getBigDecimal("betAmount");
        if (betAmount == null || betAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throwExp("betAmount 非法");
        }

        String orderNo = data.getString("orderNo");
        if (StringUtils.isBlank(orderNo)) {
            orderNo = OrderUtil.getOrder5Number();
        }

        // 【修复点 1：幂等性检查】防止重复扣款
        String orderKey = "game:pbx:order:" + orderNo;
        String exist = gameCacheService.get(orderKey);
        if (StringUtils.isNotBlank(exist)) {
            JSONObject resp = new JSONObject();
            resp.put("success", true);
            resp.put("message", "order processed");
            resp.put("orderNo", orderNo);
            UserCapital uc = userCapitalCacheService.getUserCapitalCacheByType(userId, 1002);
            resp.put("balance", uc != null ? uc.getBalance() : BigDecimal.ZERO);
            return resp;
        }

        // 加锁处理
        synchronized (LockUtil.getlock(String.valueOf(userId))) {
            // 二次检查
            if (StringUtils.isNotBlank(gameCacheService.get(orderKey))) {
                return new JSONObject().fluentPut("success", true);
            }

            Integer capitalType = data.getInteger("capitalType");
            if (capitalType == null || capitalType == 0) capitalType = 1002;

            BigDecimal feeRate = data.getBigDecimal("feeRate");
            if (feeRate == null) feeRate = new BigDecimal("0.05");

            // 构造扣款对象
            JSONObject one = new JSONObject();
            one.put("amount", betAmount.negate());
            one.put("capitalType", capitalType);
            one.put("em", LogCapitalTypeEnum.game_bet_pbx.getValue());
            one.put("orderNo", orderNo);

            JSONObject betObj = new JSONObject();
            betObj.put(String.valueOf(userId), one);

            // 执行扣款
            userCapitalService.betUpdateBalance2(betObj, capitalType);

            // 【修复点 1 补充】：标记订单已处理 (24小时)
            gameCacheService.set(orderKey, "1", 24 * 3600);

            // 推送资产变更
            UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, capitalType);
            JSONObject pushData = new JSONObject();
            pushData.put("userId", userId);
            pushData.put("capitalType", capitalType);
            pushData.put("balance", userCapital.getBalance());
            Push.push(PushCode.updateUserCapital, managerSocketService.getServerIdByUserId(userId), pushData);

            // 手续费入奖池
            String weekKey = DateUtil.getFirstDayOfWeek(new Date());
            // 【修复点 3：Key 一致性】使用带 gameId 的格式
            String poolKey = RedisKeyConstant.PRIZE_POOL + "pbx:" + gameId + ":" + weekKey;

            long betCents = betAmount.multiply(new BigDecimal("100"))
                    .setScale(0, java.math.RoundingMode.HALF_UP)
                    .longValue();

            gameCacheService.incr(poolKey, betCents);
            gameCacheService.expire(poolKey, 86400 * 14);

            // 计入周榜
            pbxWeekOnBet(gameId, userId, betCents, weekKey);
            // 计入用户历史总投入 (永久累计)
            gameCacheService.incr(pbxUserTotalBetKey(userId), betCents);

            // 返回最新奖池
            String poolCentsStr = gameCacheService.get(poolKey);
            BigDecimal poolBalance = BigDecimal.ZERO;
            if (poolCentsStr != null) {
                poolBalance = new BigDecimal(poolCentsStr).divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
            }

            BigDecimal fee = BigDecimal.ZERO.setScale(2, java.math.RoundingMode.HALF_UP);

            JSONObject result = new JSONObject();
            result.put("success", true);
            result.put("gameId", String.valueOf(gameId));
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

        // 1. 参数解析
        String gameId = data.getString("gameId");
        String periodNo = data.getString("periodNo");

        // 【修复点 2：幂等性检查】防止重复派奖
        String settleKey = "game:pbx:period:" + periodNo + ":settled";
        String settled = gameCacheService.get(settleKey);
        if (StringUtils.isNotBlank(settled)) {
            JSONObject resp = new JSONObject();
            resp.put("success", true);
            resp.put("message", "period already settled");
            return resp;
        }

        String week = DateUtil.getFirstDayOfWeek(new Date());

        Integer capitalType = data.getInteger("capitalType");
        if (capitalType == null || capitalType == 0) capitalType = 1002;

        BigDecimal feeRate = data.getBigDecimal("feeRate");
        if (feeRate == null) feeRate = new BigDecimal("0.05");

        int emValue = data.containsKey("em") ? data.getIntValue("em") : LogCapitalTypeEnum.game_bet_win_pbx.getValue();

        // 解析 winList
        JSONArray winList = data.getJSONArray("winList");
        if (winList == null) {
            // 兼容单人模式
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

        // 【修复点 3：Key 一致性】使用带 gameId 的格式
        String poolKey = RedisKeyConstant.PRIZE_POOL + "pbx:" + gameId + ":" + week;

        // 计算总额
        BigDecimal totalReturn = BigDecimal.ZERO;
        BigDecimal totalFee = BigDecimal.ZERO;
        BigDecimal totalNet = BigDecimal.ZERO;
        List<JSONObject> normalized = new ArrayList<>();

        for (Object o : winList) {
            JSONObject w = (JSONObject) o;
            Object uidObj = w.get("userId");
            if (uidObj == null) throwExp("winList.userId 不能为空");
            Long uid = (uidObj instanceof Number) ? ((Number) uidObj).longValue() : Long.parseLong(uidObj.toString());

            BigDecimal returnAmount = w.getBigDecimal("returnAmount");
            if (returnAmount == null) throwExp("winList.returnAmount 不能为空");

            returnAmount = returnAmount.setScale(2, java.math.RoundingMode.HALF_UP);
            if (returnAmount.compareTo(BigDecimal.ZERO) < 0) throwExp("returnAmount 非法");
            if (returnAmount.compareTo(BigDecimal.ZERO) == 0) continue;

            BigDecimal fee = returnAmount.multiply(feeRate).setScale(2, java.math.RoundingMode.HALF_UP);
            BigDecimal net = returnAmount.subtract(fee).setScale(2, java.math.RoundingMode.HALF_UP);
            if (net.compareTo(BigDecimal.ZERO) < 0) throwExp("net 非法");

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

        // 空结算处理
        if (normalized.size() == 0) {
            JSONObject empty = new JSONObject();
            empty.put("success", true);
            empty.put("gameId", gameId);
            empty.put("periodNo", periodNo);
            empty.put("totalReturnAmount", BigDecimal.ZERO);
            empty.put("totalFee", BigDecimal.ZERO);
            empty.put("totalNet", BigDecimal.ZERO);

            String poolCentsStr = gameCacheService.get(poolKey);
            BigDecimal poolBalance = BigDecimal.ZERO;
            if (poolCentsStr != null) {
                poolBalance = new BigDecimal(poolCentsStr).divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
            }
            empty.put("poolBalance", poolBalance);
            empty.put("userList", new JSONArray());

            // 标记已结算
            gameCacheService.set(settleKey, "1", 86400 * 2);
            return empty;
        }

        long totalNetCents = totalNet.multiply(new BigDecimal("100")).longValue();

        // 奖池扣款与入账
        synchronized (LockUtil.getlock("pbx_pool_" + week)) {
            // 二次检查幂等
            if (StringUtils.isNotBlank(gameCacheService.get(settleKey))) {
                return new JSONObject().fluentPut("success", true);
            }

            String poolCentsStr = gameCacheService.get(poolKey);
            long poolCents = (poolCentsStr == null) ? 0L : Long.parseLong(poolCentsStr);

            if (poolCents < totalNetCents) {
                BigDecimal poolBalanceBd = new BigDecimal(poolCents).divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
                JSONObject fail = new JSONObject();
                fail.put("success", false);
                fail.put("message", "推箱子奖池不足，无法派奖！当前奖池: " + poolBalanceBd + "，需要: " + totalNet);
                fail.put("gameId", gameId);
                fail.put("periodNo", periodNo);
                fail.put("poolBalance", poolBalanceBd);
                fail.put("feeRate", feeRate);
                return fail;
            }

            // 1) 扣奖池
            gameCacheService.decr(poolKey, totalNetCents);
            gameCacheService.expire(poolKey, 86400 * 14);

            // 2) 批量入账
            JSONObject batch = new JSONObject();
            for (JSONObject n : normalized) {
                JSONObject one = new JSONObject();
                one.put("amount", n.getBigDecimal("net"));
                one.put("capitalType", capitalType);
                one.put("em", emValue);
                one.put("orderNo", periodNo);
                batch.put(String.valueOf(n.getLong("userId")), one);
            }
            userCapitalService.betUpdateBalance2(batch, capitalType);

            // 【修复点 2 补充】：标记已结算
            gameCacheService.set(settleKey, "1", 86400 * 2);

            // 3) 推送与回包
            JSONArray userResult = new JSONArray();
            for (JSONObject n : normalized) {
                Long uid = n.getLong("userId");
                UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(uid, capitalType);

                JSONObject pushData = new JSONObject();
                pushData.put("userId", uid);
                pushData.put("capitalType", capitalType);
                pushData.put("balance", userCapital.getBalance());
                Push.push(PushCode.updateUserCapital, managerSocketService.getServerIdByUserId(uid), pushData);

                // 【新增】计入用户历史总返还 & 总净利 (Redis 永久保存)
                long returnCents = n.getBigDecimal("returnAmount").multiply(new BigDecimal("100")).longValue();
                long netCents = n.getBigDecimal("net").multiply(new BigDecimal("100")).longValue();
                gameCacheService.incr(pbxUserTotalReturnKey(uid), returnCents);
                gameCacheService.incr(pbxUserTotalNetKey(uid), netCents);

                JSONObject ur = new JSONObject();
                ur.put("userId", String.valueOf(uid));
                ur.put("returnAmount", n.getBigDecimal("returnAmount"));
                ur.put("fee", n.getBigDecimal("fee"));
                ur.put("net", n.getBigDecimal("net"));
                ur.put("balance", userCapital.getBalance());
                userResult.add(ur);
            }

            // 4) 统计到周榜 (Redis incr)
            BigDecimal totalReturnAmount = totalReturn.setScale(2, java.math.RoundingMode.HALF_UP);
            BigDecimal totalNetReturnAmount = totalNet.setScale(2, java.math.RoundingMode.HALF_UP);

            pbxWeekOnSettle(
                    Integer.parseInt(gameId),
                    week,
                    bdToCents(totalReturnAmount),
                    bdToCents(totalNetReturnAmount),
                    bdToCents(totalFee)
            );

            // 5) 返回最新奖池
            String newPoolCentsStr = gameCacheService.get(poolKey);
            BigDecimal poolBalance = BigDecimal.ZERO;
            if (newPoolCentsStr != null) {
                poolBalance = new BigDecimal(newPoolCentsStr).divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
            }

            JSONObject result = new JSONObject();
            result.put("success", true);
            result.put("gameId", gameId);
            result.put("periodNo", periodNo);
            result.put("capitalType", capitalType);
            result.put("feeRate", feeRate);
            result.put("totalReturnAmount", totalReturnAmount);
            result.put("totalFee", totalFee.setScale(2, java.math.RoundingMode.HALF_UP));
            result.put("totalNet", totalNet.setScale(2, java.math.RoundingMode.HALF_UP));
            result.put("poolBalance", poolBalance);
            result.put("userList", userResult);
            return result;
        }
    }

    @Transactional(readOnly = true)
    @ServiceMethod(code = "722", description = "推箱子(PBX)查询（奖池/记录/榜单）")
    public JSONObject pbxQuery(ManagerDTS2SocketServer adminSocketServer, JSONObject data) {
        JSONObject result = new JSONObject();
        int gameId = data.getIntValue("gameId");
        String userIdStr = data.getString("userId");
        Long userId = null;
        if (userIdStr != null) {
            try {
                userId = Long.parseLong(userIdStr);
            } catch (Exception ignore) {
            }
        }

        long nowTimeMs = System.currentTimeMillis();

        // 1) 基础字段：服务器时间
        result.put("success", true);
        result.put("serverTime", nowTimeMs());
        result.put("serverTimeMs", nowTimeMs);

        // 2) 周维度 key（周一作为 key）
        String weekKey = DateUtil.getFirstDayOfWeek(new Date(nowTimeMs));
        String lastWeekKey = DateUtil.getFirstDayOfWeek(new Date(nowTimeMs - 7L * 24 * 60 * 60 * 1000));
        result.put("weekKey", weekKey);
        result.put("lastWeekKey", lastWeekKey);

        // 3) 本周奖池余额（按 pbxBet/pbxSettle 使用的 Redis key 读取；单位分）
        long poolCents = 0L;
        String poolKey = RedisKeyConstant.PRIZE_POOL + "pbx:" + gameId + ":" + weekKey;
        String poolStr = gameCacheService.get(poolKey);
        if (StringUtils.isNotBlank(poolStr)) {
            try {
                poolCents = Long.parseLong(poolStr);
            } catch (Exception ignore) {
                poolCents = 0L;
            }
        }
        result.put("poolBalance", centsToBd(poolCents));

        PbxWeekSummary thisSummary = pbxWeekSummaryService.findByGameWeek(gameId, weekKey);
        boolean weekSettled = (thisSummary != null && thisSummary.getSettled() == 1) || PBX_WEEK_SETTLED.containsKey(weekKey);
        PbxWeekSummary lastSummary = pbxWeekSummaryService.findByGameWeek(gameId, lastWeekKey);
        boolean lastWeekSettled = (lastSummary != null && lastSummary.getSettled() == 1);
        if (weekSettled) {
            PBX_WEEK_SETTLED.putIfAbsent(weekKey, true);
        }
        result.put("weekSettled", weekSettled);
        result.put("lastWeekSettled", lastWeekSettled);

        // 3) 读取游戏配置：榜单利润入池比例（默认 0.5）
        JSONObject gameSetting = pbxLoadGameSettingByGameId(gameId);
        BigDecimal rankProfitPercent = null;
        if (gameSetting != null) {
            rankProfitPercent = gameSetting.getBigDecimal("rankProfitPercent");
        }
        if (rankProfitPercent == null) {
            rankProfitPercent = new BigDecimal("0.5");
        }

        // 4) 周汇总（本周/上周）
        // =========================================================================
        // 【核心修复点】全部改为从 Redis 读取 (使用 getLongFromCache + 对应的 Key 方法)
        // 解决了“数据写入 Redis 但读取仍在读空 Map”导致周榜显示为 0 的问题
        // =========================================================================

        // --- 4.1 本周数据 ---
        long weekTotalBetCents       = getLongFromCache(pbxWeekTotalBetCentsKey(gameId, weekKey));
        long weekTotalNetReturnCents = getLongFromCache(pbxWeekTotalNetCentsKey(gameId, weekKey)); // ✅ 修正：读 Redis
        long weekTotalFeeCents       = getLongFromCache(pbxWeekTotalFeeCentsKey(gameId, weekKey)); // ✅ 修正：读 Redis

        long weekProfitCents = weekTotalBetCents - weekTotalNetReturnCents;
        if (weekProfitCents < 0) {
            weekProfitCents = 0;
        }
        long weekRankPoolCents = PBX_WEEK_RANK_POOL_CENTS.getOrDefault(weekKey, 0L);
        if (weekRankPoolCents <= 0 && weekSettled && thisSummary != null) {
            Long savedPool = thisSummary.getPoolAddCents();
            if (savedPool != null) {
                weekRankPoolCents = savedPool;
            }
        }
        if (weekRankPoolCents <= 0 && !weekSettled) {
            weekRankPoolCents = new BigDecimal(weekProfitCents).multiply(rankProfitPercent).setScale(0, RoundingMode.FLOOR).longValue();
        }
        result.put("weekConsume", centsToBd(weekTotalBetCents));
        result.put("weekReturn", centsToBd(weekTotalNetReturnCents));
        result.put("weekFee", centsToBd(weekTotalFeeCents));
        result.put("weekProfit", centsToBd(weekProfitCents));
        result.put("weekDividendPool", centsToBd(weekRankPoolCents));

        // --- 4.2 上周数据 ---
        long lastWeekTotalBetCents       = getLongFromCache(pbxWeekTotalBetCentsKey(gameId, lastWeekKey)); // ✅ 修正：读 Redis
        long lastWeekTotalNetReturnCents = getLongFromCache(pbxWeekTotalNetCentsKey(gameId, lastWeekKey)); // ✅ 修正：读 Redis
        long lastWeekTotalFeeCents       = getLongFromCache(pbxWeekTotalFeeCentsKey(gameId, lastWeekKey)); // ✅ 修正：读 Redis

        long lastWeekProfitCents = lastWeekTotalBetCents - lastWeekTotalNetReturnCents;
        if (lastWeekProfitCents < 0) {
            lastWeekProfitCents = 0;
        }
        long lastWeekRankPoolCents = PBX_WEEK_RANK_POOL_CENTS.getOrDefault(lastWeekKey, 0L);
        if (lastWeekRankPoolCents <= 0 && !lastWeekSettled) {
            lastWeekRankPoolCents = new BigDecimal(lastWeekProfitCents).multiply(rankProfitPercent).setScale(0, RoundingMode.FLOOR).longValue();
        }
        result.put("lastWeekConsume", centsToBd(lastWeekTotalBetCents));
        result.put("lastWeekReturn", centsToBd(lastWeekTotalNetReturnCents));
        result.put("lastWeekFee", centsToBd(lastWeekTotalFeeCents));
        result.put("lastWeekProfit", centsToBd(lastWeekProfitCents));
        result.put("lastWeekDividendPool", centsToBd(lastWeekRankPoolCents));

        // 5) Top10 榜单（本周实时快照 / 已结算周：优先读 DB 快照，避免重启丢失）
        JSONArray weekRankTop10;
        if (weekSettled) {
            weekRankTop10 = buildWeekRankTop10FromSummary(thisSummary);
            if (weekRankTop10 == null) {
                weekRankTop10 = PBX_WEEK_SETTLED_TOP10_CACHE.get(weekKey);
            }
            if (weekRankTop10 == null) {
                weekRankTop10 = new JSONArray();
            }
            PBX_WEEK_SETTLED_TOP10_CACHE.putIfAbsent(weekKey, weekRankTop10);
        } else {
            weekRankTop10 = buildWeekRankTop10FromRedis(gameId, weekKey);
            PBX_WEEK_TOP10_SNAPSHOT.put(weekKey, weekRankTop10);
            if (weekRankTop10 == null) {
                weekRankTop10 = new JSONArray();
            }
        }
        result.put("weekRankTop10", weekRankTop10);

        JSONArray lastWeekRankTop10;
        if (lastWeekSettled) {
            lastWeekRankTop10 = buildWeekRankTop10FromSummary(lastSummary);
            if (lastWeekRankTop10 == null) {
                lastWeekRankTop10 = PBX_WEEK_SETTLED_TOP10_CACHE.get(lastWeekKey);
            }
            if (lastWeekRankTop10 == null) {
                lastWeekRankTop10 = new JSONArray();
            }
            PBX_WEEK_SETTLED_TOP10_CACHE.putIfAbsent(lastWeekKey, lastWeekRankTop10);
        } else {
            lastWeekRankTop10 = buildWeekRankTop10FromRedis(gameId, lastWeekKey);
            PBX_WEEK_TOP10_SNAPSHOT.put(lastWeekKey, lastWeekRankTop10);
            if (lastWeekRankTop10 == null) {
                lastWeekRankTop10 = new JSONArray();
            }
        }
        result.put("lastWeekRankTop10", lastWeekRankTop10);

        // 6) 个人周榜信息（rank/本周投入/上周投入/奖励）
        if (userId != null) {
            // ==========================================
            // 1. 本周数据 (读取 Redis ZSet)
            // ==========================================
            String currentZsetKey = pbxWeekUserRankZsetKey(gameId, weekKey);

            // 本周投入 (zscore)
            Double currentScore = gameCacheService.zscore(currentZsetKey, String.valueOf(userId));
            long myWeekBet = (currentScore == null) ? 0L : currentScore.longValue();
            result.put("myWeekConsume", centsToBd(myWeekBet));

            // 本周排名 (zrevrank, 0-based, so +1)
            Long currentRankObj = gameCacheService.zrevrank(currentZsetKey, String.valueOf(userId));
            result.put("myWeekRank", (currentRankObj == null) ? 0 : (currentRankObj.intValue() + 1));

            // ==========================================
            // 2. 上周数据 (读取 Redis ZSet)
            // ==========================================
            String lastZsetKey = pbxWeekUserRankZsetKey(gameId, lastWeekKey);

            // 上周投入
            Double lastScore = gameCacheService.zscore(lastZsetKey, String.valueOf(userId));
            long myLastWeekBet = (lastScore == null) ? 0L : lastScore.longValue();
            result.put("myLastWeekConsume", centsToBd(myLastWeekBet));

            // 上周排名
            Long lastRankObj = gameCacheService.zrevrank(lastZsetKey, String.valueOf(userId));
            result.put("myLastWeekRank", (lastRankObj == null) ? 0 : (lastRankObj.intValue() + 1));

            // ==========================================
            // 3. 奖励数据 (目前仍读内存 Map)
            // ==========================================
            // 注意：PBX_WEEK_USER_AWARD_CENTS 是在 pbxWeekSettleInternal 时放入内存的。
            // 如果你只做了 Bet 的 Redis 持久化，Award 这里暂时维持读 Map 即可。
            Map<Long, Long> weekAwardMap = PBX_WEEK_USER_AWARD_CENTS.getOrDefault(weekKey, new ConcurrentHashMap<>());
            Map<Long, Long> lastWeekAwardMap = PBX_WEEK_USER_AWARD_CENTS.getOrDefault(lastWeekKey, new ConcurrentHashMap<>());
            // 【新增】查询用户历史总数据 (从 Redis 读取)
            long totalBet = getLongFromCache(pbxUserTotalBetKey(userId));
            long totalReturn = getLongFromCache(pbxUserTotalReturnKey(userId));
            long totalNet = getLongFromCache(pbxUserTotalNetKey(userId));

            result.put("myTotalConsume", centsToBd(totalBet)); // 历史总投入
            result.put("myTotalReturn", centsToBd(totalReturn)); // 历史总返还(Gross)
            result.put("myTotalNet", centsToBd(totalNet)); // 历史总净利(Net)
            BigDecimal myWeekAwardBd = weekSettled ? findUserAwardFromSummary(thisSummary, userId) : null;
            if (myWeekAwardBd == null) {
                myWeekAwardBd = centsToBd(weekAwardMap.getOrDefault(userId, 0L));
            }
            BigDecimal myLastWeekAwardBd = lastWeekSettled ? findUserAwardFromSummary(lastSummary, userId) : null;
            if (myLastWeekAwardBd == null) {
                myLastWeekAwardBd = centsToBd(lastWeekAwardMap.getOrDefault(userId, 0L));
            }
            result.put("myWeekAward", myWeekAwardBd);
            result.put("myLastWeekAward", myLastWeekAwardBd);
        } else {
            result.put("myWeekConsume", BigDecimal.ZERO);
            result.put("myLastWeekConsume", BigDecimal.ZERO);
            result.put("myWeekRank", 0);
            result.put("myLastWeekRank", 0);
            result.put("myWeekAward", BigDecimal.ZERO);
            result.put("myLastWeekAward", BigDecimal.ZERO);
            result.put("myTotalConsume", BigDecimal.ZERO);
            result.put("myTotalReturn", BigDecimal.ZERO);
            result.put("myTotalNet", BigDecimal.ZERO);
        }

        return result;
    }


// ===================== PBX 周榜/上周榜：接口 + helper（阶段2：Redis + DB 版）=====================

    private JSONArray buildWeekRankTop10FromRedis(int gameId, String weekKey) {
        Set<ZSetOperations.TypedTuple<String>> tuples = gameCacheService.getZset(pbxWeekUserRankZsetKey(gameId, weekKey), 10);
        if (tuples == null || tuples.isEmpty()) {
            return new JSONArray();
        }
        JSONArray arr = new JSONArray();
        int rank = 1;
        for (ZSetOperations.TypedTuple<String> t : tuples) {
            if (rank > 10) break;
            if (t == null || t.getValue() == null || t.getScore() == null) {
                continue;
            }
            JSONObject o = new JSONObject();
            o.put("rank", rank);
            o.put("userId", t.getValue());
            o.put("betAmount", centsToBd(t.getScore().longValue()));
            arr.add(o);
            rank++;
        }
        return arr;
    }

    /**
     * 已结算周榜 Top10：直接读取 DB 快照（summary.user_list_json）
     * 注意：快照字段内 bet/award 等已是“元”单位（BigDecimal），无需再做分->元转换。
     */
    private JSONArray buildWeekRankTop10FromSummary(PbxWeekSummary summary) {
        if (summary == null) {
            return null;
        }
        String userListJson = summary.getUserListJson();
        if (StringUtils.isBlank(userListJson)) {
            return null;
        }
        try {
            JSONArray arr = JSON.parseArray(userListJson);
            return (arr == null) ? null : arr;
        } catch (Exception ignore) {
            return null;
        }
    }

    /**
     * 已结算周榜：从 DB 快照中找到个人 award（元）。
     * 未上榜用户返回 null（调用方可回落到 Redis 快照/0）。
     */
    private BigDecimal findUserAwardFromSummary(PbxWeekSummary summary, Long userId) {
        if (summary == null || userId == null) {
            return null;
        }
        String userListJson = summary.getUserListJson();
        if (StringUtils.isBlank(userListJson)) {
            return null;
        }
        try {
            JSONArray arr = JSON.parseArray(userListJson);
            if (arr == null || arr.isEmpty()) {
                return null;
            }
            String uid = String.valueOf(userId);
            for (int i = 0; i < arr.size(); i++) {
                JSONObject o = arr.getJSONObject(i);
                if (o == null) {
                    continue;
                }
                String u = o.getString("userId");
                if (uid.equals(u)) {
                    BigDecimal bd = o.getBigDecimal("award");
                    if (bd != null) {
                        return bd;
                    }
                    Object v = o.get("award");
                    if (v != null) {
                        return new BigDecimal(String.valueOf(v));
                    }
                }
            }
        } catch (Exception ignore) {
            // ignore
        }
        return null;
    }

    private long getLongFromCache(String key) {
        Object v = null;
        try {
            v = gameCacheService.get(key);
        } catch (Exception ignore) {
        }
        if (v == null) {
            return 0L;
        }
        String s = String.valueOf(v);
        if (StringUtils.isBlank(s) || "null".equalsIgnoreCase(s)) {
            return 0L;
        }
        try {
            return Long.parseLong(s);
        } catch (Exception ignore) {
            return 0L;
        }
    }

    /**
     * 200723：PBX 周榜结算（用于结算“上周榜”）
     * - weekKey 为空：默认结算“上周”（取今天-7天所在周的周一）
     * - top10Rates / rankProfitPercent 若不传：从 l_game.game_setting 读取；再缺省则给默认值
     */
    @Transactional(rollbackFor = Exception.class)
    @ServiceMethod(code = "723", description = "PBX 周榜结算（上周榜）")
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
        Date now = new Date();

        JSONObject result = new JSONObject();
        result.put("gameId", gameId);
        result.put("weekKey", weekKey);

        // 1) DB 幂等：锁定周汇总行（不存在则创建）
        PbxWeekSummary summary = pbxWeekSummaryService.findByGameWeekForUpdate(gameId, weekKey);
        if (summary == null) {
            PbxWeekSummary init = new PbxWeekSummary();
            init.setGameId(gameId);
            init.setWeekKey(weekKey);
            init.setSettled(0);
            init.setRankProfitPercent(rankProfitPercent);
            init.setTop10RatesJson(top10Rates == null ? "[]" : top10Rates.toJSONString());
            init.setCreateTime(now);
            init.setUpdateTime(now);
            pbxWeekSummaryService.insertIgnore(init);
            summary = pbxWeekSummaryService.findByGameWeekForUpdate(gameId, weekKey);
        }

        if (summary != null && summary.getSettled() != null && summary.getSettled() == 1) {
            result.put("success", false);
            result.put("msg", "本周已结算");
            result.put("settled", true);

            long poolAddCents = summary.getPoolAddCents() == null ? 0L : summary.getPoolAddCents();
            long poolBalanceCents = summary.getPoolBalanceCents() == null ? 0L : summary.getPoolBalanceCents();
            long profitCents = summary.getProfitCents() == null ? 0L : summary.getProfitCents();

            result.put("profit", centsToBd(profitCents));
            result.put("poolAdd", centsToBd(poolAddCents));
            result.put("poolBalance", centsToBd(poolBalanceCents));

            JSONArray userList = new JSONArray();
            if (summary.getUserListJson() != null && summary.getUserListJson().trim().length() > 0) {
                try {
                    userList = JSON.parseArray(summary.getUserListJson());
                } catch (Exception ignore) {
                }
            }

            // 修复历史快照：若结算事件已成功，但 summary.user_list_json 里 status 仍为 0（常见于首次结算时先落快照后更新事件状态）
            // 这里以 DB 事件表为准回填，并顺带自愈 summary.user_list_json，保证幂等查询口径一致。
            try {
                if (userList != null && !userList.isEmpty()) {
                    List<PbxWeekSettleEvent> events = pbxWeekSettleEventService.findByGameWeek(gameId, weekKey);
                    if (events != null && !events.isEmpty()) {
                        Map<Long, Integer> statusMap = new HashMap<>();
                        for (PbxWeekSettleEvent e : events) {
                            if (e != null && e.getUserId() != null) {
                                statusMap.put(e.getUserId(), e.getStatus() == null ? 0 : e.getStatus());
                            }
                        }
                        boolean changed = false;
                        for (int i = 0; i < userList.size(); i++) {
                            JSONObject one = userList.getJSONObject(i);
                            if (one == null) {
                                continue;
                            }
                            Long uid = null;
                            try {
                                uid = Long.parseLong(one.getString("userId"));
                            } catch (Exception ignore) {
                            }
                            if (uid == null) {
                                continue;
                            }
                            Integer st = statusMap.get(uid);
                            if (st == null) {
                                continue;
                            }
                            Integer old = one.getInteger("status");
                            if (old == null || old.intValue() != st.intValue()) {
                                one.put("status", st);
                                changed = true;
                            }
                        }
                        if (changed) {
                            summary.setUserListJson(userList.toJSONString());
                            summary.setUpdateTime(new Date());
                            pbxWeekSummaryService.update(summary);
                        }
                    }
                }
            } catch (Exception ignore) {
            }

            result.put("userList", userList);
            return result;
        }

        if (summary == null || summary.getId() == null) {
            throw new AppException("周榜汇总数据异常：无法锁定 weekSummary 行");
        }

        // 2) 本周（weekKey）统计数据：当前版本仍从运行期内存统计获取，结算结果写入 DB 做幂等与快照
        long totalBetCents = getLongFromCache(pbxWeekTotalBetCentsKey(gameId, weekKey));
        long totalReturnCents = getLongFromCache(pbxWeekTotalReturnCentsKey(gameId, weekKey));
        long totalNetCents    = getLongFromCache(pbxWeekTotalNetCentsKey(gameId, weekKey));
        long totalFeeCents    = getLongFromCache(pbxWeekTotalFeeCentsKey(gameId, weekKey));

        long profitCents = totalBetCents - totalNetCents;
        if (profitCents < 0) {
            profitCents = 0;
        }

        BigDecimal percent = rankProfitPercent == null ? BigDecimal.ZERO : rankProfitPercent;
        long poolAddCents = bdToCents(centsToBd(profitCents).multiply(percent).setScale(2, RoundingMode.HALF_UP));

        // 本周周榜奖池：按 Top10 分润比例分配（默认分完，剩余留作 poolBalance）
        long poolLeftCents = poolAddCents;

        // Top10：按周投注额排序
        Set<ZSetOperations.TypedTuple<String>> tuples = gameCacheService.getZset(pbxWeekUserRankZsetKey(gameId, weekKey), 10);
        List<Map.Entry<Long, Long>> top10 = new ArrayList<>();
        if (tuples != null) {
            for (ZSetOperations.TypedTuple<String> t : tuples) {
                if (t.getValue() == null) continue;
                long uid = Long.parseLong(t.getValue());
                long score = t.getScore() == null ? 0L : t.getScore().longValue();
                top10.add(new AbstractMap.SimpleEntry<>(uid, score));
            }
        }
        JSONArray userList = new JSONArray();
        JSONObject payBatch = new JSONObject();
        Map<Long, PbxWeekSettleEvent> eventByUser = new HashMap<>();
        Map<Long, JSONObject> jsonByUser = new HashMap<>();

        for (int i = 0; i < top10.size(); i++) {
            long uid = top10.get(i).getKey();
            long betCents = top10.get(i).getValue();

            BigDecimal rate = BigDecimal.ZERO;
            if (top10Rates != null && i < top10Rates.size()) {
                try {
                    rate = top10Rates.getBigDecimal(i);
                } catch (Exception ignore) {
                }
            }

            long awardCents = 0;
            if (poolAddCents > 0 && rate != null && rate.compareTo(BigDecimal.ZERO) > 0) {
                awardCents = bdToCents(centsToBd(poolAddCents).multiply(rate).setScale(2, RoundingMode.HALF_UP));
                if (awardCents < 0) {
                    awardCents = 0;
                }
                if (awardCents > poolLeftCents) {
                    awardCents = poolLeftCents;
                }
            }
            poolLeftCents -= awardCents;

            // 3) DB 幂等：按 (gameId, weekKey, userId) 幂等写入结算事件
            PbxWeekSettleEvent event = pbxWeekSettleEventService.findByGameWeekUser(gameId, weekKey, uid);
            if (event == null) {
                event = new PbxWeekSettleEvent();
                event.setGameId(gameId);
                event.setWeekKey(weekKey);
                event.setUserId(uid);
                event.setRank(i + 1);
                event.setBetCents(betCents);
                event.setRate(rate);
                event.setAwardCents(awardCents);
                event.setCapitalType(1002);
                event.setOrderNo("PBX_WEEK_RANK_" + weekKey + "_" + uid);
                event.setEm(LogCapitalTypeEnum.game_bet_win_pbx.getValue());
                event.setTableName("pbx_week_rank");
                event.setStatus(0);
                event.setFailMsg(null);
                event.setCreateTime(now);
                event.setUpdateTime(now);
                pbxWeekSettleEventService.insertIgnore(event);
                event = pbxWeekSettleEventService.findByGameWeekUser(gameId, weekKey, uid);
            } else {
                // 重新结算时刷新确定性字段，但不会回滚成功状态
                event.setRank(i + 1);
                event.setBetCents(betCents);
                event.setRate(rate);
                event.setAwardCents(awardCents);
                if (event.getOrderNo() == null) {
                    event.setOrderNo("PBX_WEEK_RANK_" + weekKey + "_" + uid);
                }
                if (event.getTableName() == null) {
                    event.setTableName("pbx_week_rank");
                }
                if (event.getCapitalType() == null) {
                    event.setCapitalType(1002);
                }
                event.setEm(LogCapitalTypeEnum.game_bet_win_pbx.getValue());
                event.setUpdateTime(now);
                pbxWeekSettleEventService.update(event);
            }

            if (event == null || event.getId() == null) {
                throw new AppException("周榜结算事件写入失败：userId=" + uid);
            }

            eventByUser.put(uid, event);

            JSONObject one = new JSONObject();
            one.put("rank", i + 1);
            one.put("userId", String.valueOf(uid));
            one.put("bet", centsToBd(betCents));
            one.put("rate", rate == null ? BigDecimal.ZERO : rate);
            one.put("award", centsToBd(awardCents));
            one.put("status", event.getStatus() == null ? 0 : event.getStatus());
            userList.add(one);
            jsonByUser.put(uid, one);

            // 未成功发奖且有金额才进入 payout batch
            if (awardCents > 0 && (event.getStatus() == null || event.getStatus() != 1)) {
                JSONObject payOne = new JSONObject();
                payOne.put("amount", centsToBd(awardCents));
                payOne.put("em", LogCapitalTypeEnum.game_bet_win_pbx.getValue());
                payOne.put("orderNo", event.getOrderNo());
                payOne.put("tableName", event.getTableName());
                payBatch.put(String.valueOf(uid), payOne);
            }

            PBX_WEEK_USER_AWARD_CENTS.computeIfAbsent(weekKey, k -> new ConcurrentHashMap<>()).put(uid, awardCents);
        }

        // 4) 发奖：与事件状态更新放在同一事务内，避免发奖成功但事件未落库导致重复发奖
        if (!payBatch.isEmpty()) {
            userCapitalService.betUpdateBalance2(payBatch, 1002);
            for (String uidStr : payBatch.keySet()) {
                long uid = Long.parseLong(uidStr);
                PbxWeekSettleEvent event = eventByUser.get(uid);
                if (event != null) {
                    event.setStatus(1);
                    event.setFailMsg(null);
                    event.setUpdateTime(new Date());
                    pbxWeekSettleEventService.update(event);
                }

                // 同步修正返回/快照里的 status，避免“DB 已成功但 userList.status 仍是 0”
                JSONObject one = jsonByUser.get(uid);
                if (one != null) {
                    one.put("status", 1);
                }
            }
        }

        // 5) 补全余额快照（用于 summary.user_list_json）
        for (int i = 0; i < userList.size(); i++) {
            JSONObject one = userList.getJSONObject(i);
            long uid = Long.parseLong(one.getString("userId"));
            UserCapital uc = userCapitalService.findUserCapitalByUserIdAndCapitalType(uid, 1002);
            one.put("balance", uc == null ? "0" : uc.getBalance());
        }

        // 6) 写入周汇总快照并置 settled=1
        summary.setTotalBetCents(totalBetCents);
        summary.setTotalReturnCents(totalReturnCents);
        summary.setTotalNetCents(totalNetCents);
        summary.setTotalFeeCents(totalFeeCents);
        summary.setProfitCents(profitCents);
        summary.setRankProfitPercent(percent);
        summary.setPoolAddCents(poolAddCents);
        summary.setPoolBalanceCents(poolLeftCents);
        summary.setTop10RatesJson(top10Rates == null ? "[]" : top10Rates.toJSONString());
        summary.setUserListJson(userList.toJSONString());
        summary.setSettled(1);
        summary.setSettleTime(now);
        summary.setUpdateTime(now);
        pbxWeekSummaryService.update(summary);

        // 7) 运行期缓存（非幂等关键路径）
        PBX_WEEK_SETTLED.put(weekKey, true);
        PBX_WEEK_RANK_POOL_CENTS.put(weekKey, poolLeftCents);
        PBX_WEEK_SETTLED_TOP10_CACHE.put(weekKey, userList);

        result.put("success", true);
        result.put("settled", true);
        result.put("totalBet", centsToBd(totalBetCents));
        result.put("totalReturn", centsToBd(totalReturnCents));
        result.put("totalNet", centsToBd(totalNetCents));
        result.put("totalFee", centsToBd(totalFeeCents));
        result.put("profit", centsToBd(profitCents));
        result.put("poolAdd", centsToBd(poolAddCents));
        result.put("poolBalance", centsToBd(poolLeftCents));
        result.put("userList", userList);
        return result;
    }

    /** 下注统计：投入累加到本周 */
    private void pbxWeekOnBet(int gameId, long userId, long betCents, String weekKey) {
        if (StringUtils.isBlank(weekKey)) {
            weekKey = DateUtil.getFirstDayOfWeek(new Date());
        }

        // 1. 个人周榜积分 (ZSet): 累加分数
        String zsetKey = pbxWeekUserRankZsetKey(gameId, weekKey);
        gameCacheService.zincrby(zsetKey, String.valueOf(userId), (double) betCents);
        gameCacheService.expire(zsetKey, 86400 * 14); // 保留2周

        // 2. 周总投入 (String): 累加数值
        String totalBetKey = pbxWeekTotalBetCentsKey(gameId, weekKey);
        gameCacheService.incr(totalBetKey, betCents);
        gameCacheService.expire(totalBetKey, 86400 * 14);
    }

    /** 结算时累计周榜返还/实返/手续费（在 pbxSettle(200721) 完成派奖后调用） */
    private void pbxWeekOnSettle(int gameId, String weekKey, long totalReturnCents, long totalNetCents, long totalFeeCents) {
        if (StringUtils.isBlank(weekKey)) {
            weekKey = DateUtil.getFirstDayOfWeek(new Date());
        }
        // 改为 Redis incr 累加
        gameCacheService.incr(pbxWeekTotalReturnCentsKey(gameId, weekKey), totalReturnCents);
        gameCacheService.incr(pbxWeekTotalNetCentsKey(gameId, weekKey), totalNetCents);
        gameCacheService.incr(pbxWeekTotalFeeCentsKey(gameId, weekKey), totalFeeCents);

        // 设置过期时间
        gameCacheService.expire(pbxWeekTotalReturnCentsKey(gameId, weekKey), 86400 * 14);
        gameCacheService.expire(pbxWeekTotalNetCentsKey(gameId, weekKey), 86400 * 14);
        gameCacheService.expire(pbxWeekTotalFeeCentsKey(gameId, weekKey), 86400 * 14);
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
// -------------------------------------------------------------------------
    //  Redis Key 规范化管理 (使用冒号 : 分隔，便于工具折叠查看)
    //  结构：game:pbx:{gameId}:{weekKey}:{type}
    // -------------------------------------------------------------------------

    /** 周榜 ZSet: game:pbx:12:2025-12-29:rank */
    private String pbxWeekUserRankZsetKey(int gameId, String weekKey) {
        return "game:pbx:" + gameId + ":" + weekKey + ":rank";
    }

    /** 总投注: game:pbx:12:2025-12-29:stat:bet */
    private String pbxWeekTotalBetCentsKey(int gameId, String weekKey) {
        return "game:pbx:" + gameId + ":" + weekKey + ":stat:bet";
    }

    /** 总返还: game:pbx:12:2025-12-29:stat:return */
    private String pbxWeekTotalReturnCentsKey(int gameId, String weekKey) {
        return "game:pbx:" + gameId + ":" + weekKey + ":stat:return";
    }

    /** 总手续费: game:pbx:12:2025-12-29:stat:fee */
    private String pbxWeekTotalFeeCentsKey(int gameId, String weekKey) {
        return "game:pbx:" + gameId + ":" + weekKey + ":stat:fee";
    }

    /** 总净利: game:pbx:12:2025-12-29:stat:net */
    private String pbxWeekTotalNetCentsKey(int gameId, String weekKey) {
        return "game:pbx:" + gameId + ":" + weekKey + ":stat:net";
    }
    /** 用户历史总投入: game:pbx:user:{userId}:total:bet */
    private String pbxUserTotalBetKey(long userId) {
        return "game:pbx:user:" + userId + ":total:bet";
    }

    /** 用户历史总返还(含本金): game:pbx:user:{userId}:total:return */
    private String pbxUserTotalReturnKey(long userId) {
        return "game:pbx:user:" + userId + ":total:return";
    }

    /** 用户历史总净收益(Net): game:pbx:user:{userId}:total:net */
    private String pbxUserTotalNetKey(long userId) {
        return "game:pbx:user:" + userId + ":total:net";
    }
}

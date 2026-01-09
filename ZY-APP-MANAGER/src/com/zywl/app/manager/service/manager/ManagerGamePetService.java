package com.zywl.app.manager.service.manager;
import com.zywl.app.base.bean.*;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.OrderUtil;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.cache.UserCapitalCacheService;
import com.zywl.app.defaultx.enmus.LogCapitalTypeEnum;
import com.zywl.app.defaultx.enmus.LogUserBackpackTypeEnum;
import com.zywl.app.defaultx.service.*;
import com.zywl.app.manager.context.MessageCodeContext;
import com.zywl.app.manager.service.PlayGameService;
import com.zywl.app.manager.socket.ManagerSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.util.LockUtil;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.service.UserPetRecordService;
import com.zywl.app.defaultx.service.UserPetService;
import com.zywl.app.defaultx.service.UserPetUserService;
import com.zywl.app.defaultx.service.UserService;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Author: lzx
 * @Create: 2025/12/30
 * @Version: V1.0
 * @Description: 用户养宠管理  Manager
 * @Task: 038 (MessageCodeContext.USER_PET)
 */
@Service
@ServiceClass(code = MessageCodeContext.USER_PET)
public class ManagerGamePetService  extends BaseService {
    private static final long HOUR_MS = 60L * 60L * 1000L;

    /**
     * t_user_pet_record.record_type
     * 0=STATE快照 1=SETTLE结算 2=DIVIDEND分润 3=BUY购买 4=FEED喂养 5=CLAIM领取 6=UNLOCK解锁
     */
    private static final int PET_RECORD_TYPE_BUY = 3;
    private static final int PET_RECORD_TYPE_FEED = 4;
    private static final int PET_RECORD_TYPE_CLAIM = 5;
    private static final int PET_RECORD_TYPE_UNLOCK = 6;

    // 用户服务
    @Autowired
    private UserService userService;

    // VIP 用户服务
    @Autowired
    private UserVipService userVipService;

    // 游戏核心服务
    @Autowired
    private PlayGameService gameService;

    // 配置中心服务
    @Autowired
    private ManagerConfigService managerConfigService;

    // 用户资产服务
    @Autowired
    private UserCapitalService userCapitalService;

    // 用户缓存服务
    @Autowired
    private UserCacheService userCacheService;

    // 用户资产缓存服务
    @Autowired
    private UserCapitalCacheService userCapitalCacheService;

    // 养宠用户流水服务
    @Autowired
    private UserPetRecordService userPetRecordService;

    // 养宠用户状态服务
    @Autowired
    private UserPetUserService userPetUserService;

    // 养宠用户明细服务
    @Autowired
    private UserPetService userPetService;

    //游戏服
    @Autowired
    private ManagerGameBaseService managerGameBaseService;

    /**
     * 获取养宠信息
     */
    @ServiceMethod(code = "001", description = "获取养宠信息（含追结算）")
    @Transactional(rollbackFor = Exception.class)
    public JSONObject getPetInfo(ManagerSocketServer socket, JSONObject data) {
        Long userId = data.getLong("userId");
        if (userId == null || userId <= 0) {
            throwExp("userId不能为空");
        }
        // 用户校验
        loadAndCheckUser(userId);
        // 取 dic_pet
        DicPet dicPet = getDicPet();

        initUserPetUserIfAbsent(userId);
        UserPetUser petUser = userPetUserService.lockByUserId(userId);
        if (petUser == null) {
            initUserPetUserIfAbsent(userId);
            petUser = userPetUserService.lockByUserId(userId);
        }
        if (petUser == null) {
            throwExp("用户养宠状态初始化失败");
        }

        // last_settle_time 追到当前整点
        settleToCurrentHour(userId, petUser, dicPet);

        // 基础统计
        int petCount = userPetService.countByUserId(userId);

        // 1~5代人数统计
        Map<Integer, Integer> levelPeople = calcLevelPeople(userId, 5);

        // 看 1+2 代贡献分润与贡献
        BigDecimal todayDividend = safeDecimal(userPetRecordService.sumTodayDividend(userId));
        BigDecimal contribLevel12 = safeDecimal(userPetRecordService.sumDividendLevel12(userId));

        // hungerMaxHours
        int hungerMaxHours = safeInt(dicPet.getHungerMaxDays()) * 24;

        JSONObject resp = new JSONObject();
        resp.put("serverTime", System.currentTimeMillis());
        resp.put("petCount", petCount);
        resp.put("hungerHours", safeInt(petUser.getHungerHours()));
        resp.put("hungerMaxHours", hungerMaxHours);
        resp.put("pendingYieldAmount", format6(safeDecimal(petUser.getPendingYieldAmount())));
        resp.put("totalYieldAmount", format6(safeDecimal(petUser.getTotalYieldAmount())));
        resp.put("todayDividendAmount", format6(todayDividend));
        resp.put("totalDividendAmount", format6(safeDecimal(petUser.getTotalDividendAmount())));

        JSONObject unlock = new JSONObject();
        unlock.put("lv3", safeInt(petUser.getUnlockLv3()));
        unlock.put("lv4", safeInt(petUser.getUnlockLv4()));
        unlock.put("lv5", safeInt(petUser.getUnlockLv5()));
        resp.put("unlock", unlock);

        JSONObject levelPeopleJson = new JSONObject();
        for (int i = 1; i <= 5; i++) {
            levelPeopleJson.put("lv" + i, levelPeople.getOrDefault(i, 0));
        }
        resp.put("levelPeople", levelPeopleJson);

        // 直推 + 1/2代贡献 解锁差额
        resp.put("unlockNeed", buildUnlockNeed(dicPet, petUser, levelPeople.getOrDefault(1, 0), contribLevel12));

        // 各代分润
        JSONObject unlockNeed = resp.getJSONObject("unlockNeed");
        JSONObject levelDividend = new JSONObject();
        for (int lv = 1; lv <= 5; lv++) {
            JSONObject one = new JSONObject();
            one.put("people", levelPeople.getOrDefault(lv, 0));

            BigDecimal todayLv = safeDecimal(userPetRecordService.sumTodayDividendByLevel(userId, lv));
            BigDecimal totalLv = safeDecimal(userPetRecordService.sumTotalDividendByLevel(userId, lv));
            one.put("todayAmount", format6(todayLv));
            one.put("totalAmount", format6(totalLv));

            int enabled = 1;
            if (lv == 3) {
                enabled = safeInt(petUser.getUnlockLv3());
            } else if (lv == 4) {
                enabled = safeInt(petUser.getUnlockLv4());
            } else if (lv == 5) {
                enabled = safeInt(petUser.getUnlockLv5());
            }
            one.put("enabled", enabled);

            // 3-5代若未解锁，返回还差多少
            if (lv >= 3) {
                one.put("needDirect", unlockNeed == null ? 0 : unlockNeed.getInteger("lv" + lv + "NeedDirect"));
                one.put("needContrib", unlockNeed == null ? format6(BigDecimal.ZERO) : unlockNeed.getString("lv" + lv + "NeedContrib"));
            }

            levelDividend.put("lv" + lv, one);
        }
        resp.put("levelDividend", levelDividend);

        return resp;
    }

    /**
     * 购买狮子
     */
    @Transactional
    @ServiceMethod(code = "002")
    public JSONObject buyLion(ManagerSocketServer managerSocketServer, JSONObject params) {
        checkNull(params);
        Long userId = params.getLong("userId");
        Integer buyCount = params.getInteger("buyCount");
        String  orderNo = "BUY_LION_" + OrderUtil.getOrder5Number();
        if (userId == null || userId <= 0) {
            throwExp("参数错误：userId");
        }
        if (buyCount == null || buyCount <= 0) {
            throwExp("buyCount参数错误");
        }
        synchronized (LockUtil.getlock(userId)) {
            loadAndCheckUser(userId);
            DicPet dicPet = getDicPet();
            if (dicPet.getStatus() != null && dicPet.getStatus() != 1) {
                throwExp("该功能暂未开放");
            }
            Date now = new Date();
            Date nowHourBegin = floorToHourBegin(now);
            // 初始化用户宠物主表
            UserPetUser userPetUser = lockOrCreateUserPetUser(userId, nowHourBegin);
            // 扣资产
            Integer costCapitalType = dicPet.getBuyCostCapitalType();
            BigDecimal costOne = dicPet.getBuyCostAmount();
            BigDecimal totalCost = costOne.multiply(BigDecimal.valueOf(buyCount));

            UserCapital capital = userCapitalCacheService.getUserCapitalCacheByType(userId, costCapitalType);
            if (capital == null || capital.getBalance() == null || capital.getBalance().compareTo(totalCost) < 0) {
                throwExp("资产不足");
            }
            //扣除资产 交易
            userCapitalService.subUserBalanceByBuyLion(userId, totalCost, costCapitalType, orderNo, null, LogCapitalTypeEnum.pet_lion_buy);
            for (int i = 0; i < buyCount; i++) {
                UserPet userPet = new UserPet();
                userPet.setUserId(userId);
                userPet.setStatus(1);
                userPet.setBuyTime(now);
                userPet.setTotalYieldAmount(BigDecimal.ZERO);
                userPet.setCreateTime(now);
                userPet.setUpdateTime(now);
                userPetService.insert(userPet);
            }

            JSONObject result = buildPetInfoResult(userId, dicPet, userPetUser);
            result.put("orderNo", orderNo);
            result.put("buyCount", buyCount);
            result.put("costCapitalType", costCapitalType);
            result.put("costAmount", toPlain6(totalCost));
            // 写订单成功记录
            UserPetRecord record = new UserPetRecord();
            record.setUserId(userId);
            record.setRecordType(PET_RECORD_TYPE_BUY);
            record.setRecordKey(orderNo);
            record.setPetId(0L);
            record.setFromUserId(0L);
            record.setLevel(0);
            record.setHungerBefore(safeInt(userPetUser.getHungerHours()));
            record.setHungerAfter(safeInt(userPetUser.getHungerHours()));
            record.setCapitalType(costCapitalType);
            record.setAmount(totalCost);
            record.setPayloadJson(result.toJSONString());
            record.setStatus(1);
            record.setCreateTime(now);
            record.setUpdateTime(now);
            try {
                userPetRecordService.insert(record);
            } catch (DuplicateKeyException e) {
            }
            return result;
        }
    }

    /**
     * 038003 喂养狮子
     * **/
    @ServiceMethod(code = "003")
    @Transactional(rollbackFor = Exception.class)
    public JSONObject feedLion(ManagerSocketServer socket, JSONObject params) {
        checkNull(params);

        Long userId = params.getLong("userId");
        Integer feedTimes = params.getInteger("feedTimes");
        String orderNo = "FEED_LION_" + OrderUtil.getOrder5Number();
        if (userId == null || userId <= 0) {
            throwExp("参数错误：userId");
        }
        if (feedTimes == null || feedTimes <= 0) {
            throwExp("参数错误：feedTimes");
        }
        synchronized (LockUtil.getlock(userId)) {
            loadAndCheckUser(userId);

            DicPet dicPet = getDicPet();
            if (safeInt(dicPet.getStatus()) != 1) {
                throwExp("养兽暂未开放");
            }
            // 锁定
            initUserPetUserIfAbsent(userId);
            UserPetUser petUserUser = userPetUserService.lockByUserId(userId);
            if (petUserUser == null) {
                throwExp("用户养兽数据初始化失败");
            }
            // 追结算到当前小时
            settleToCurrentHour(userId, petUserUser, dicPet);
            int hungerMaxHours = safeInt(dicPet.getHungerMaxDays()) * 24;
            int hungerHours = safeInt(petUserUser.getHungerHours());
            int addHoursPerTime = safeInt(dicPet.getFeedAddHours());

            if (addHoursPerTime <= 0) {
                throwExp("喂养增加饱腹配置错误");
            }
            if (hungerMaxHours <= 0) {
                throwExp("饱腹上限配置错误");
            }

            int remainHours = hungerMaxHours - hungerHours;
            if (remainHours <= 0) {
                throwExp("当前饱腹已满，无需喂养");
            }
            int maxTimes = remainHours / addHoursPerTime;
            if (maxTimes <= 0) {
                throwExp("当前剩余饱腹不足 1 次喂养增加量");
            }
            if (feedTimes > maxTimes) {
                throwExp("本次最多可喂养 " + maxTimes + " 次");
            }

            Integer costCapitalType = dicPet.getFeedCostCapitalType();
            BigDecimal costAmount = safeDecimal(dicPet.getFeedCostAmount());
            if (costCapitalType == null) {
                throwExp("喂养消耗资产类型未配置");
            }
            if (costAmount.compareTo(BigDecimal.ZERO) <= 0) {
                throwExp("喂养消耗金额未配置");
            }

            BigDecimal totalCost = costAmount.multiply(new BigDecimal(feedTimes)).setScale(6, RoundingMode.DOWN);
            // 扣款
            userCapitalService.subUserBalanceByBuyLion(userId, totalCost, costCapitalType, orderNo, 0L, LogCapitalTypeEnum.pet_feed);
            // 推送资产变更
            managerGameBaseService.pushCapitalUpdate(userId, costCapitalType);

            int hungerBefore = hungerHours;
            int hungerAfter = hungerHours + feedTimes * addHoursPerTime;

            Date now = new Date();
            petUserUser.setHungerHours(hungerAfter);
            petUserUser.setUpdateTime(now);
            userPetUserService.saveOrUpdate(petUserUser);

            JSONObject result = buildPetInfoResult(userId, dicPet, petUserUser);
            result.put("orderNo", orderNo);
            result.put("feedTimes", feedTimes);
            result.put("hungerBefore", hungerBefore);
            result.put("hungerAfter", hungerAfter);

            UserPetRecord record = new UserPetRecord();
            record.setUserId(userId);
            record.setRecordType(PET_RECORD_TYPE_FEED);
            record.setRecordKey(orderNo);
            record.setPetId(0L);
            record.setFromUserId(0L);
            record.setLevel(0);
            record.setCapitalType(costCapitalType);
            record.setAmount(totalCost);
            record.setHungerBefore(hungerBefore);
            record.setHungerAfter(hungerAfter);
            record.setStatus(1);
            record.setPayloadJson(result.toJSONString());
            record.setCreateTime(now);
            record.setUpdateTime(now);
            try {
                userPetRecordService.insert(record);
            } catch (DuplicateKeyException e) {
            }

            return result;
        }
    }


    /**
     * 领取产出
     */
    @ServiceMethod(code = "004")
    @Transactional(rollbackFor = Exception.class)
    public JSONObject claimYield(ManagerSocketServer socket, JSONObject params) {
        checkNull(params);

        Long userId = params.getLong("userId");
        if (userId == null || userId <= 0) {
            throwExp("参数错误：userId");
        }
        String orderNo = "CLAIM_LION_" + OrderUtil.getOrder5Number();
        synchronized (LockUtil.getlock(userId)) {
            loadAndCheckUser(userId);

            DicPet dicPet = getDicPet();
            if (safeInt(dicPet.getStatus()) != 1) {
                throwExp("养兽暂未开放");
            }
            initUserPetUserIfAbsent(userId);
            UserPetUser petUserUser = userPetUserService.lockByUserId(userId);
            if (petUserUser == null) {
                throwExp("用户养兽数据初始化失败");
            }
            // 追结算到当前小时
            settleToCurrentHour(userId, petUserUser, dicPet);

            BigDecimal pending = safeDecimal(petUserUser.getPendingYieldAmount());
            if (pending.compareTo(BigDecimal.ZERO) <= 0) {
                throwExp("暂无可领取产出");
            }

            Integer yieldCapitalType = dicPet.getYieldCapitalType();
            if (yieldCapitalType == null) {
                throwExp("产出资产类型未配置");
            }

            // 发放资产
            JSONArray rewards = new JSONArray();
            JSONObject reward = new JSONObject();
            reward.put("type", 1);
            reward.put("id", yieldCapitalType);
            reward.put("number", pending);
            rewards.add(reward);

            gameService.addReward(userId, rewards, LogCapitalTypeEnum.pet_claim_yield, LogUserBackpackTypeEnum.game);

            // 清空待领取
            petUserUser.setPendingYieldAmount(BigDecimal.ZERO);
            petUserUser.setUpdateTime(new Date());
            userPetUserService.saveOrUpdate(petUserUser);

            // 最新状态
            JSONObject result = buildPetInfoResult(userId, dicPet, petUserUser);
            result.put("orderNo", orderNo);
            result.put("claimAmount", format6(pending));

            UserPetRecord record = new UserPetRecord();
            record.setUserId(userId);
            record.setRecordType(PET_RECORD_TYPE_CLAIM);
            record.setRecordKey(orderNo);
            record.setPetId(0L);
            record.setFromUserId(0L);
            record.setLevel(0);
            record.setHungerBefore(safeInt(petUserUser.getHungerHours()));
            record.setHungerAfter(safeInt(petUserUser.getHungerHours()));
            record.setCapitalType(yieldCapitalType);
            record.setAmount(pending);
            record.setStatus(1);
            record.setPayloadJson(result.toJSONString());
            record.setCreateTime(new Date());
            record.setUpdateTime(new Date());
            try {
                userPetRecordService.insert(record);
            } catch (DuplicateKeyException e) {
            }

            return result;
        }
    }

    /**
     * 解锁分润等级（3/4/5 代）
     */
    @ServiceMethod(code = "005")
    @Transactional(rollbackFor = Exception.class)
    public JSONObject unlockDividendLevel(ManagerSocketServer socket, JSONObject params) {
        checkNull(params);

        Long userId = params.getLong("userId");
        Integer unlockLevel = params.getInteger("unlockLevel");

        if (userId == null || userId <= 0) {
            throwExp("参数错误：userId");
        }
        if (unlockLevel == null || (unlockLevel != 3 && unlockLevel != 4 && unlockLevel != 5)) {
            throwExp("参数错误：unlockLevel");
        }

        String orderNo = "UNLOCK_LION_" + OrderUtil.getOrder5Number();
        synchronized (LockUtil.getlock(userId)) {
            loadAndCheckUser(userId);

            DicPet dicPet = getDicPet();
            if (safeInt(dicPet.getStatus()) != 1) {
                throwExp("养兽暂未开放");
            }

            initUserPetUserIfAbsent(userId);
            UserPetUser petUserUser = userPetUserService.lockByUserId(userId);
            if (petUserUser == null) {
                throwExp("用户养兽数据初始化失败");
            }

            // 追结算到当前小时
            settleToCurrentHour(userId, petUserUser, dicPet);

            // 已解锁直接返回
            if (unlockLevel == 3 && safeInt(petUserUser.getUnlockLv3()) == 1) {
                JSONObject result = buildPetInfoResult(userId, dicPet, petUserUser);
                result.put("orderNo", orderNo);
                result.put("unlockLevel", unlockLevel);
                result.put("alreadyUnlocked", 1);
                return result;
            }
            if (unlockLevel == 4 && safeInt(petUserUser.getUnlockLv4()) == 1) {
                JSONObject result = buildPetInfoResult(userId, dicPet, petUserUser);
                result.put("orderNo", orderNo);
                result.put("unlockLevel", unlockLevel);
                result.put("alreadyUnlocked", 1);
                return result;
            }
            if (unlockLevel == 5 && safeInt(petUserUser.getUnlockLv5()) == 1) {
                JSONObject result = buildPetInfoResult(userId, dicPet, petUserUser);
                result.put("orderNo", orderNo);
                result.put("unlockLevel", unlockLevel);
                result.put("alreadyUnlocked", 1);
                return result;
            }

            // 直推人数 + 1/2 代累计贡献狮毛
            Map<Integer, Integer> levelPeople = calcLevelPeople(userId, 5);
            int directCount = levelPeople.getOrDefault(1, 0);

            BigDecimal contribLevel12 = safeDecimal(userPetRecordService.sumDividendLevel12(userId));

            int needDirect;
            BigDecimal needContrib;

            if (unlockLevel == 3) {
                needDirect = safeInt(dicPet.getUnlockDirectLv3());
                needContrib = safeDecimal(dicPet.getUnlockContribLv3());
            } else if (unlockLevel == 4) {
                needDirect = safeInt(dicPet.getUnlockDirectLv4());
                needContrib = safeDecimal(dicPet.getUnlockContribLv4());
            } else {
                needDirect = safeInt(dicPet.getUnlockDirectLv5());
                needContrib = safeDecimal(dicPet.getUnlockContribLv5());
            }

            if (directCount < needDirect || contribLevel12.compareTo(needContrib) < 0) {
                String msg = "解锁条件不足：直推人数 " + directCount + "/" + needDirect
                        + "，团队贡献狮毛 " + format6(contribLevel12) + "/" + format6(needContrib);
                throwExp(msg);
            }

            if (unlockLevel == 3) {
                petUserUser.setUnlockLv3(1);
            } else if (unlockLevel == 4) {
                petUserUser.setUnlockLv4(1);
            } else {
                petUserUser.setUnlockLv5(1);
            }
            petUserUser.setUpdateTime(new Date());
            userPetUserService.saveOrUpdate(petUserUser);

            JSONObject result = buildPetInfoResult(userId, dicPet, petUserUser);
            result.put("orderNo", orderNo);
            result.put("unlockLevel", unlockLevel);
            result.put("directCount", directCount);
            result.put("directNeed", needDirect);
            result.put("teamContrib", format6(contribLevel12));
            result.put("teamContribNeed", format6(needContrib));

            // 记录
            UserPetRecord record = new UserPetRecord();
            record.setUserId(userId);
            record.setRecordType(PET_RECORD_TYPE_UNLOCK);
            record.setRecordKey(orderNo);
            record.setPetId(0L);
            record.setFromUserId(0L);
            record.setLevel(unlockLevel);
            record.setHungerBefore(safeInt(petUserUser.getHungerHours()));
            record.setHungerAfter(safeInt(petUserUser.getHungerHours()));
            record.setCapitalType(dicPet.getUnlockContribCapitalType());
            record.setAmount(BigDecimal.ZERO);
            record.setStatus(1);
            record.setPayloadJson(result.toJSONString());
            record.setCreateTime(new Date());
            record.setUpdateTime(new Date());

            try {
                userPetRecordService.insert(record);
            } catch (DuplicateKeyException e) {
            }

            return result;
        }
    }

    /**
     * 038006 邀请主页：邀请码/邀请人信息/邀请统计
     */
    @ServiceMethod(code = "006", description = "养宠-邀请主页")
    public JSONObject inviteHome(ManagerSocketServer socket, JSONObject params) {
        checkNull(params);
        Long userId = params.getLong("userId");
        loadAndCheckUser(userId);

        User user = userCacheService.getUserInfoById(userId);
        UserPetUser petUser = getOrCreateUserPetUser(userId);

        JSONObject result = new JSONObject();
        result.put("serverTime", System.currentTimeMillis());

        // 邀请码
        result.put("inviteCode", user.getInviteCode());

        // 今日/累计金币
        JSONObject stat = new JSONObject();
        stat.put("todayCoin", format6(safeDecimal(petUser.getTodayDividendAmount())));
        stat.put("totalCoin", format6(safeDecimal(petUser.getTotalDividendAmount())));
        result.put("stat", stat);

        // 直推总人数
        int totalDirect = userService.countInviteUsersByParentId(userId);
        result.put("directTotal", totalDirect);

        // 有效人数=认证+买过宠物
        int validDirect = userService.countInviteUsersValidByParentId(userId);
        result.put("directValid", validDirect);

        // 上级邀请人信息
        result.put("inviter", buildInviterInfo(user));
        return result;
    }

    /**
     * 038007 邀请列表（直推）
     * type: 0全部 / 1未达标 / 2有效
     */
    @ServiceMethod(code = "007", description = "养宠-邀请列表")
    public JSONObject inviteList(ManagerSocketServer socket, JSONObject params) {
        checkNull(params);
        Long userId = params.getLong("userId");
        loadAndCheckUser(userId);

        Integer page = params.getInteger("page");
        Integer pageSize = params.getInteger("pageSize");
        Integer type = params.getInteger("type");
        if (page == null || page <= 0) page = 1;
        if (pageSize == null || pageSize <= 0) pageSize = 10;
        if (type == null) type = 0;
        if (type < 0 || type > 2) {
            throwExp("type参数非法");
        }

        // tabTotal
        int total = userService.countInviteUsersByParentId(userId);
        int validTotal = userService.countInviteUsersValidByParentId(userId);
        int unqualifiedTotal = userService.countInviteUsersUnqualifiedByParentId(userId);

        JSONObject tabTotal = new JSONObject();
        tabTotal.put("all", total);
        tabTotal.put("unqualified", unqualifiedTotal);
        tabTotal.put("valid", validTotal);

        List<User> users = userService.findInviteUsersByParentIdWithType(userId, type, page, pageSize);

        List<Long> userIds = new ArrayList<>();
        if (users != null) {
            for (User u : users) {
                if (u != null && u.getId() != null) userIds.add(u.getId());
            }
        }

        Map<Long, Integer> petCountMap = userIds.isEmpty() ? new HashMap<>() : userPetService.countByUserIds(userIds);
        Map<Long, BigDecimal> coinMap = userIds.isEmpty() ? new HashMap<>() : userPetRecordService.sumTotalDividendByFromUserIdsAndLevel(userId, userIds, 1);

        JSONArray list = new JSONArray();
        if (users != null) {
            for (User u : users) {
                JSONObject it = new JSONObject();
                it.put("userId", u.getId());
                it.put("nickName", u.getName());

                it.put("inviteTime", formatDate(u.getRegistTime()));

                int petCount = petCountMap.getOrDefault(u.getId(), 0);
                BigDecimal coinAmount = coinMap.getOrDefault(u.getId(), BigDecimal.ZERO);

                int status;
                String statusText;
                if (u.getAuthentication() != null && u.getAuthentication() == 1) {
                    if (petCount > 0) {
                        status = 0;
                        statusText = "进行中";
                    } else {
                        status = 1;
                        statusText = "未达标";
                    }
                } else {
                    status = 2;
                    statusText = "未通过认证";
                }

                it.put("status", status);
                it.put("statusText", statusText);
                it.put("petCount", petCount);
                it.put("coinAmount", format6(coinAmount));
                it.put("serverTime", System.currentTimeMillis());
                list.add(it);
            }
        }

        JSONObject result = new JSONObject();
        result.put("serverTime", System.currentTimeMillis());
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("type", type);

        result.put("total", total);
        result.put("validTotal", validTotal);
        result.put("unqualifiedTotal", unqualifiedTotal);
        result.put("tabTotal", tabTotal);

        result.put("list", list);
        return result;
    }

    /**
     * 038008 邀请人信息（本人/上级/统计/分润）
     */
    @ServiceMethod(code = "008", description = "养宠-邀请人信息")
    public JSONObject inviterInfo(ManagerSocketServer socket, JSONObject params) {
        checkNull(params);
        Long userId = params.getLong("userId");
        loadAndCheckUser(userId);

        User user = userCacheService.getUserInfoById(userId);

        JSONObject result = new JSONObject();
        result.put("serverTime", System.currentTimeMillis());
        result.put("userId", user.getId());
        result.put("name", user.getName());
        result.put("authentication", user.getAuthentication() == null ? 0 : user.getAuthentication());

        // 上级
        result.put("inviter", buildInviterInfo(user));

        // 统计
        int total = userService.countInviteUsersByParentId(userId);
        int validTotal = userService.countInviteUsersValidByParentId(userId);
        int unqualifiedTotal = userService.countInviteUsersUnqualifiedByParentId(userId);
        result.put("total", total);
        result.put("validTotal", validTotal);
        result.put("unqualifiedTotal", unqualifiedTotal);

        // 分润
        UserPetUser petUser = getOrCreateUserPetUser(userId);
        result.put("todayDividendAmount", format6(safeDecimal(petUser.getTodayDividendAmount())));
        result.put("totalDividendAmount", format6(safeDecimal(petUser.getTotalDividendAmount())));
        return result;
    }

    private JSONObject buildInviterInfo(User user) {
        if (user == null || user.getParentId() == null || user.getParentId() <= 0) {
            return new JSONObject();
        }
        User parent = userCacheService.getUserInfoById(user.getParentId());
        if (parent == null) {
            return new JSONObject();
        }
        JSONObject inviter = new JSONObject();
        inviter.put("userId", parent.getId());
        inviter.put("name", parent.getName());
        inviter.put("headImageUrl", parent.getHeadImageUrl());
        inviter.put("userNo", parent.getUserNo());
        inviter.put("wechatId", parent.getWechatId());
        inviter.put("qq", parent.getQq());
        return inviter;
    }

    private UserPetUser getOrCreateUserPetUser(Long userId) {
        Date now = new Date();
        Date nowHourBegin = truncateToHour(now);
        return lockOrCreateUserPetUser(userId, nowHourBegin);
    }

    /**
     *  dic_pet 仅一行配置；优先取 id=1
     */
    private DicPet getDicPet() {
        if (PlayGameService.DIC_PET.isEmpty()) {
            throwExp("养兽配置暂未初始化");
        }
        DicPet dicPet = PlayGameService.DIC_PET.get("1");
        if (dicPet != null) {
            return dicPet;
        }
        return PlayGameService.DIC_PET.values().iterator().next();
    }


    /**
     * 行级锁 t_user_pet_user
     */
    private UserPetUser lockOrCreateUserPetUser(Long userId, Date nowHourBegin) {
        UserPetUser userPetUser = userPetUserService.lockByUserId(userId);
        if (userPetUser != null) {
            if (userPetUser.getLastSettleTime() == null) {
                userPetUser.setLastSettleTime(nowHourBegin);
                userPetUser.setUpdateTime(new Date());
                userPetUserService.saveOrUpdate(userPetUser);
            }
            return userPetUser;
        }
        Date now = new Date();
        UserPetUser create = new UserPetUser();
        create.setUserId(userId);
        create.setHungerHours(0);
        create.setPendingYieldAmount(BigDecimal.ZERO);
        create.setTotalYieldAmount(BigDecimal.ZERO);
        create.setTodayDividendAmount(BigDecimal.ZERO);
        create.setTotalDividendAmount(BigDecimal.ZERO);
        create.setUnlockLv3(0);
        create.setUnlockLv4(0);
        create.setUnlockLv5(0);
        create.setLastSettleTime(nowHourBegin);
        create.setCreateTime(now);
        create.setUpdateTime(now);
        userPetUserService.saveOrUpdate(create);
        return create;
    }


    /**
     * 统一构造养宠返回结构
     */
    private JSONObject buildPetInfoResult(Long userId, DicPet dicPet, UserPetUser petUser) {
        // 追结算
        settleToCurrentHour(userId, petUser, dicPet);
        int petCount = userPetService.countByUserId(userId);
        // 1~5代人数统计
        Map<Integer, Integer> levelPeople = calcLevelPeople(userId, 5);
        // 今日分润 / 1+2代贡献
        BigDecimal todayDividend = safeDecimal(userPetRecordService.sumTodayDividend(userId));
        BigDecimal contribLevel12 = safeDecimal(userPetRecordService.sumDividendLevel12(userId));

        int hungerMaxHours = safeInt(dicPet.getHungerMaxDays()) * 24;

        JSONObject resp = new JSONObject();
        resp.put("serverTime", System.currentTimeMillis());
        resp.put("petCount", petCount);
        resp.put("hungerHours", safeInt(petUser.getHungerHours()));
        resp.put("hungerMaxHours", hungerMaxHours);
        resp.put("pendingYieldAmount", format6(safeDecimal(petUser.getPendingYieldAmount())));
        resp.put("totalYieldAmount", format6(safeDecimal(petUser.getTotalYieldAmount())));
        resp.put("todayDividendAmount", format6(todayDividend));
        resp.put("totalDividendAmount", format6(safeDecimal(petUser.getTotalDividendAmount())));
        JSONObject unlock = new JSONObject();
        unlock.put("lv3", safeInt(petUser.getUnlockLv3()));
        unlock.put("lv4", safeInt(petUser.getUnlockLv4()));
        unlock.put("lv5", safeInt(petUser.getUnlockLv5()));
        resp.put("unlock", unlock);
        JSONObject levelPeopleJson = new JSONObject();
        for (int i = 1; i <= 5; i++) {
            levelPeopleJson.put("lv" + i, levelPeople.getOrDefault(i, 0));
        }
        resp.put("levelPeople", levelPeopleJson);
        // 解锁差额..直推 + 1/2代贡献
        resp.put("unlockNeed", buildUnlockNeed(dicPet, petUser, levelPeople.getOrDefault(1, 0), contribLevel12));
        // 各代分润
        JSONObject unlockNeed = resp.getJSONObject("unlockNeed");
        JSONObject levelDividend = new JSONObject();
        for (int lv = 1; lv <= 5; lv++) {
            JSONObject one = new JSONObject();
            one.put("people", levelPeople.getOrDefault(lv, 0));
            BigDecimal todayLv = safeDecimal(userPetRecordService.sumTodayDividendByLevel(userId, lv));
            BigDecimal totalLv = safeDecimal(userPetRecordService.sumTotalDividendByLevel(userId, lv));
            one.put("todayAmount", format6(todayLv));
            one.put("totalAmount", format6(totalLv));

            int enabled = 1;
            if (lv == 3) {
                enabled = safeInt(petUser.getUnlockLv3());
            } else if (lv == 4) {
                enabled = safeInt(petUser.getUnlockLv4());
            } else if (lv == 5) {
                enabled = safeInt(petUser.getUnlockLv5());
            }
            one.put("enabled", enabled);

            // 3-5代若未解锁，返回还差多少
            if (lv >= 3) {
                one.put("needDirect", unlockNeed == null ? 0 : unlockNeed.getInteger("lv" + lv + "NeedDirect"));
                one.put("needContrib", unlockNeed == null ? format6(BigDecimal.ZERO) : unlockNeed.getString("lv" + lv + "NeedContrib"));
            }

            levelDividend.put("lv" + lv, one);
        }
        resp.put("levelDividend", levelDividend);

        return resp;
    }

    private Date floorToHourBegin(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private String toPlain6(BigDecimal amount) {
        if (amount == null) {
            return "0.000000";
        }
        return amount.setScale(6, RoundingMode.DOWN).toPlainString();
    }

    /**
     * 追结算到当前整点：
     */
    private void settleToCurrentHour(Long userId, UserPetUser petUser, DicPet dicPet) {
        Date now = new Date();
        Integer yieldCapitalType = dicPet.getYieldCapitalType();
        Integer dividendCapitalType = dicPet.getDividendCapitalType();
        if (yieldCapitalType == null) {
            throwExp("产出资产类型未配置(yield_capital_type)");
        }
        if (dividendCapitalType == null) {
            throwExp("分润资产类型未配置(dividend_capital_type)");
        }

        Date currentHour = truncateToHour(now);

        Date lastSettle = petUser.getLastSettleTime();
        if (lastSettle == null) {
            petUser.setLastSettleTime(currentHour);
            userPetUserService.saveOrUpdate(petUser);
            return;
        }
        lastSettle = truncateToHour(lastSettle);
        if (!lastSettle.equals(petUser.getLastSettleTime())) {
            petUser.setLastSettleTime(lastSettle);
        }

        if (!lastSettle.before(currentHour)) {
            return;
        }

        int hungerHours = safeInt(petUser.getHungerHours());
        if (hungerHours <= 0) {
            petUser.setLastSettleTime(currentHour);
            userPetUserService.saveOrUpdate(petUser);
            return;
        }

        // 若当前无狮子：无需结算，直接推进结算时间
        int totalPetsNow = userPetService.countByUserId(userId);
        if (totalPetsNow <= 0) {
            petUser.setLastSettleTime(currentHour);
            userPetUserService.saveOrUpdate(petUser);
            return;
        }

        // 将 lastSettle 快速推进到“可能产生产出”的第一个整点
        Date firstBuyTime = userPetService.findFirstBuyTime(userId);
        if (firstBuyTime != null) {
            Date firstEffectiveHour = truncateToHour(firstBuyTime);
            if (firstBuyTime.after(firstEffectiveHour)) {
                firstEffectiveHour = new Date(firstEffectiveHour.getTime() + HOUR_MS);
            }
            if (lastSettle.before(firstEffectiveHour)) {
                lastSettle = firstEffectiveHour;
                petUser.setLastSettleTime(lastSettle);
                if (!lastSettle.before(currentHour)) {
                    userPetUserService.saveOrUpdate(petUser);
                    return;
                }
            }
        }

        // 解析收益曲线 & 分润固定额
        List<YieldSegment> yieldSegments = parseYieldCurve(dicPet.getYieldCurveJson());
        if (yieldSegments.isEmpty()) {
            // 没曲线无法结算
            throwExp("dic_pet.yield_curve_json 为空或格式错误");
        }
        Map<Integer, BigDecimal> profitFixed = parseProfitFixed(dicPet.getProfitFixedJson());

        // 结算循环
        long diffHours = (currentHour.getTime() - lastSettle.getTime()) / HOUR_MS;
        int settleHours = (int) Math.min(diffHours, (long) hungerHours);

        BigDecimal pending = safeDecimal(petUser.getPendingYieldAmount());
        BigDecimal totalYield = safeDecimal(petUser.getTotalYieldAmount());

        Date hourStart = lastSettle;
        for (int i = 0; i < settleHours; i++) {
            String recordKey = formatDate(hourStart);
            UserPetRecord existSettle = userPetRecordService.findOneByUk(
                    userId, 1, recordKey, 0L, 0L, 0
            );
            if (existSettle != null) {
                hourStart = new Date(hourStart.getTime() + HOUR_MS);
                continue;
            }
            // 计算本小时狮子数量与产出
            HourYield hourYield = calcHourYield(userId, hourStart, yieldSegments);
            if (hourYield.petCountAtHour <= 0) {
                hourStart = new Date(hourStart.getTime() + HOUR_MS);
                continue;
            }

            int hungerBefore = hungerHours;
            hungerHours = hungerHours - 1;
            int hungerAfter = hungerHours;

            UserPetRecord settleRecord = new UserPetRecord();
            settleRecord.setUserId(userId);
            settleRecord.setPetId(0L);
            settleRecord.setRecordType(1);
            settleRecord.setRecordKey(recordKey);
            settleRecord.setFromUserId(0L);
            settleRecord.setLevel(0);
            settleRecord.setHungerBefore(hungerBefore);
            settleRecord.setHungerAfter(hungerAfter);
            settleRecord.setCapitalType(yieldCapitalType);
            settleRecord.setAmount(hourYield.yieldAmount);
            settleRecord.setChangesJson(null);
            settleRecord.setPayloadJson(buildSettlePayload(hourYield).toJSONString());
            settleRecord.setStatus(1);
            settleRecord.setFailMsg(null);

            userPetRecordService.insert(settleRecord);

            // 更新用户累计与待领取
            pending = pending.add(hourYield.yieldAmount);
            totalYield = totalYield.add(hourYield.yieldAmount);

            // 分润
            settleDividendForHour(userId, recordKey, profitFixed, dividendCapitalType, hungerBefore, hungerAfter);

            hourStart = new Date(hourStart.getTime() + HOUR_MS);
            if (hungerHours <= 0) {
                break;
            }
        }

        petUser.setPendingYieldAmount(pending);
        petUser.setTotalYieldAmount(totalYield);
        petUser.setHungerHours(Math.max(hungerHours, 0));
        petUser.setLastSettleTime(currentHour);
        userPetUserService.saveOrUpdate(petUser);
    }

    private void settleDividendForHour(Long fromUserId, String recordKey,
                                       Map<Integer, BigDecimal> profitFixed,
                                       Integer dividendCapitalType,
                                       int hungerBefore, int hungerAfter) {

        if (profitFixed == null || profitFixed.isEmpty()) {
            return;
        }

        if (dividendCapitalType == null) {
            throwExp("分润资产类型未配置(dividend_capital_type)");
        }

        List<Long> uplines = getUplineChain(fromUserId, 5);
        if (uplines.isEmpty()) {
            return;
        }

        for (int level = 1; level <= uplines.size(); level++) {
            Long upUserId = uplines.get(level - 1);
            if (upUserId == null || upUserId <= 0) {
                continue;
            }

            BigDecimal amountPerHour = safeDecimal(profitFixed.get(level));
            if (amountPerHour.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            UserPetRecord existDividend = userPetRecordService.findOneByUk(
                    upUserId, 2, recordKey, 0L, fromUserId, level
            );
            if (existDividend != null) {
                continue;
            }

            initUserPetUserIfAbsent(upUserId);
            UserPetUser upState = userPetUserService.lockByUserId(upUserId);
            if (upState == null) {
                continue;
            }

            // 3/4/5 代解锁判定
            if (level == 3 && safeInt(upState.getUnlockLv3()) != 1) {
                continue;
            }
            if (level == 4 && safeInt(upState.getUnlockLv4()) != 1) {
                continue;
            }
            if (level == 5 && safeInt(upState.getUnlockLv5()) != 1) {
                continue;
            }

            // 分润
            UserPetRecord dividendRecord = new UserPetRecord();
            dividendRecord.setUserId(upUserId);
            dividendRecord.setPetId(0L);
            dividendRecord.setRecordType(2);
            dividendRecord.setRecordKey(recordKey);
            dividendRecord.setFromUserId(fromUserId);
            dividendRecord.setLevel(level);
            dividendRecord.setHungerBefore(hungerBefore);
            dividendRecord.setHungerAfter(hungerAfter);
            dividendRecord.setCapitalType(dividendCapitalType);
            dividendRecord.setAmount(amountPerHour);
            dividendRecord.setChangesJson(null);

            JSONObject payload = new JSONObject();
            payload.put("fromUserId", fromUserId);
            payload.put("level", level);
            payload.put("amount", format6(amountPerHour));
            dividendRecord.setPayloadJson(payload.toJSONString());

            dividendRecord.setStatus(1);
            dividendRecord.setFailMsg(null);

            userPetRecordService.insert(dividendRecord);

            // 上级累计与待领取
            BigDecimal upPending = safeDecimal(upState.getPendingYieldAmount()).add(amountPerHour);
            BigDecimal upTotalDividend = safeDecimal(upState.getTotalDividendAmount()).add(amountPerHour);
            BigDecimal upTodayDividend = safeDecimal(upState.getTodayDividendAmount()).add(amountPerHour);

            upState.setPendingYieldAmount(upPending);
            upState.setTotalDividendAmount(upTotalDividend);
            upState.setTodayDividendAmount(upTodayDividend);

            userPetUserService.saveOrUpdate(upState);
        }
    }

    /**
     * 本小时产出计算：
     */
    private HourYield calcHourYield(Long userId, Date hourStart, List<YieldSegment> segments) {

        Date endInclusive = new Date(hourStart.getTime() + 1);

        int totalAtHour = userPetService.countByUserIdAndBuyTimeRange(userId, null, endInclusive);
        if (totalAtHour <= 0) {
            return new HourYield(0, BigDecimal.ZERO, Collections.emptyList());
        }

        segments.sort(Comparator.comparingInt(a -> a.dayStart));

        BigDecimal yield = BigDecimal.ZERO;
        int counted = 0;
        List<JSONObject> segDebug = new ArrayList<>();

        for (int i = 0; i < segments.size(); i++) {
            YieldSegment seg = segments.get(i);

            int segCount;
            if (i == segments.size() - 1) {
                segCount = Math.max(totalAtHour - counted, 0);
            } else {
                Date start = subDays(hourStart, seg.dayEnd);
                Date end = subDays(hourStart, Math.max(seg.dayStart - 1, 0));
                Date endSeg = new Date(end.getTime() + 1);

                segCount = userPetService.countByUserIdAndBuyTimeRange(userId, start, endSeg);
                counted += segCount;
            }

            BigDecimal segAmount = seg.amountPerHour.multiply(BigDecimal.valueOf(segCount));
            yield = yield.add(segAmount);

            JSONObject dbg = new JSONObject();
            dbg.put("dayStart", seg.dayStart);
            dbg.put("dayEnd", seg.dayEnd);
            dbg.put("amountPerHour", format6(seg.amountPerHour));
            dbg.put("petCount", segCount);
            dbg.put("amount", format6(segAmount));
            segDebug.add(dbg);
        }

        return new HourYield(totalAtHour, yield.setScale(6, RoundingMode.DOWN), segDebug);
    }

    private JSONObject buildSettlePayload(HourYield hourYield) {
        JSONObject payload = new JSONObject();
        payload.put("petCountAtHour", hourYield.petCountAtHour);
        payload.put("yieldAmount", format6(hourYield.yieldAmount));
        payload.put("segments", new JSONArray(hourYield.segDebug));
        return payload;
    }

    private JSONObject buildUnlockNeed(DicPet dicPet, UserPetUser petUser, int directCount, BigDecimal contribLevel12) {
        JSONObject need = new JSONObject();

        // lv3
        if (safeInt(petUser.getUnlockLv3()) == 1) {
            need.put("lv3NeedDirect", 0);
            need.put("lv3NeedContrib", format6(BigDecimal.ZERO));
        } else {
            int d = Math.max(safeInt(dicPet.getUnlockDirectLv3()) - directCount, 0);
            BigDecimal c = safeDecimal(dicPet.getUnlockContribLv3()).subtract(contribLevel12);
            if (c.compareTo(BigDecimal.ZERO) < 0) c = BigDecimal.ZERO;
            need.put("lv3NeedDirect", d);
            need.put("lv3NeedContrib", format6(c));
        }

        // lv4
        if (safeInt(petUser.getUnlockLv4()) == 1) {
            need.put("lv4NeedDirect", 0);
            need.put("lv4NeedContrib", format6(BigDecimal.ZERO));
        } else {
            int d = Math.max(safeInt(dicPet.getUnlockDirectLv4()) - directCount, 0);
            BigDecimal c = safeDecimal(dicPet.getUnlockContribLv4()).subtract(contribLevel12);
            if (c.compareTo(BigDecimal.ZERO) < 0) c = BigDecimal.ZERO;
            need.put("lv4NeedDirect", d);
            need.put("lv4NeedContrib", format6(c));
        }

        // lv5
        if (safeInt(petUser.getUnlockLv5()) == 1) {
            need.put("lv5NeedDirect", 0);
            need.put("lv5NeedContrib", format6(BigDecimal.ZERO));
        } else {
            int d = Math.max(safeInt(dicPet.getUnlockDirectLv5()) - directCount, 0);
            BigDecimal c = safeDecimal(dicPet.getUnlockContribLv5()).subtract(contribLevel12);
            if (c.compareTo(BigDecimal.ZERO) < 0) c = BigDecimal.ZERO;
            need.put("lv5NeedDirect", d);
            need.put("lv5NeedContrib", format6(c));
        }

        return need;
    }

    private Map<Integer, Integer> calcLevelPeople(Long userId, int maxLevel) {
        Map<Integer, Integer> res = new HashMap<>();
        if (maxLevel <= 0) return res;

        List<Long> parents = Collections.singletonList(userId);
        for (int level = 1; level <= maxLevel; level++) {
            if (parents.isEmpty()) {
                res.put(level, 0);
                parents = Collections.emptyList();
                continue;
            }
            List<Long> children = userService.findIdByParentId(parents);
            res.put(level, children == null ? 0 : children.size());
            parents = children == null ? Collections.emptyList() : children;
        }
        return res;
    }

    private List<Long> getUplineChain(Long userId, int maxLevel) {
        List<Long> res = new ArrayList<>();
        Long cur = userId;
        for (int i = 0; i < maxLevel; i++) {
            User u = userCacheService.getUserInfoById(cur);
            if (u == null || u.getParentId() == null || u.getParentId() <= 0) {
                break;
            }
            Long parentId = u.getParentId();
            res.add(parentId);
            cur = parentId;
        }
        return res;
    }


    private void initUserPetUserIfAbsent(Long userId) {
        UserPetUser exists = userPetUserService.findByUserId(userId);
        if (exists != null) {
            return;
        }
        UserPetUser init = new UserPetUser();
        init.setUserId(userId);
        init.setHungerHours(0);
        init.setPendingYieldAmount(BigDecimal.ZERO);
        init.setTotalYieldAmount(BigDecimal.ZERO);
        init.setTodayDividendAmount(BigDecimal.ZERO);
        init.setTotalDividendAmount(BigDecimal.ZERO);
        init.setUnlockLv3(0);
        init.setUnlockLv4(0);
        init.setUnlockLv5(0);
        init.setLastSettleTime(truncateToHour(new Date()));

        userPetUserService.saveOrUpdate(init);
    }

    private User loadAndCheckUser(Long userId) {
        Map<Long, User> users = userCacheService.loadUsers(userId);
        User user = (users != null) ? users.get(userId) : null;
        if (user == null) {
            throwExp("用户不存在");
        }
        if (user.getStatus() != null && user.getStatus() != 1) {
            throwExp("用户状态异常");
        }
        return user;
    }

    private Date truncateToHour(Date d) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    private Date subDays(Date base, int days) {
        Calendar c = Calendar.getInstance();
        c.setTime(base);
        c.add(Calendar.DATE, -days);
        return c.getTime();
    }

    private String format6(BigDecimal v) {
        if (v == null) v = BigDecimal.ZERO;
        return v.setScale(6, RoundingMode.DOWN).toPlainString();
    }

    private int safeInt(Integer v) {
        return v == null ? 0 : v;
    }

    private BigDecimal safeDecimal(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private Map<Integer, BigDecimal> parseProfitFixed(String profitFixedJson) {
        Map<Integer, BigDecimal> map = new HashMap<>();
        if (profitFixedJson == null || profitFixedJson.trim().isEmpty()) {
            return map;
        }
        try {
            JSONArray arr = JSONArray.parseArray(profitFixedJson);
            for (int i = 0; i < arr.size(); i++) {
                JSONObject o = arr.getJSONObject(i);
                int level = o.getIntValue("level");
                BigDecimal a = o.getBigDecimal("amountPerHour");
                if (level > 0 && a != null) {
                    map.put(level, a);
                }
            }
        } catch (Exception e) {
        }
        return map;
    }

    private List<YieldSegment> parseYieldCurve(String yieldCurveJson) {
        List<YieldSegment> list = new ArrayList<>();
        if (yieldCurveJson == null || yieldCurveJson.trim().isEmpty()) {
            return list;
        }
        try {
            JSONArray arr = JSONArray.parseArray(yieldCurveJson);
            for (int i = 0; i < arr.size(); i++) {
                JSONObject o = arr.getJSONObject(i);
                int dayStart = o.getIntValue("dayStart");
                int dayEnd = o.getIntValue("dayEnd");
                BigDecimal a = o.getBigDecimal("amountPerHour");
                if (dayStart <= 0 || dayEnd <= 0 || a == null) {
                    continue;
                }
                list.add(new YieldSegment(dayStart, dayEnd, a));
            }
        } catch (Exception e) {
            return Collections.emptyList();
        }
        return list;
    }

    private static class YieldSegment {
        int dayStart;
        int dayEnd;
        BigDecimal amountPerHour;

        YieldSegment(int dayStart, int dayEnd, BigDecimal amountPerHour) {
            this.dayStart = dayStart;
            this.dayEnd = dayEnd;
            this.amountPerHour = amountPerHour == null ? BigDecimal.ZERO : amountPerHour;
        }
    }

    private static class HourYield {
        int petCountAtHour;
        BigDecimal yieldAmount;
        List<JSONObject> segDebug;

        HourYield(int petCountAtHour, BigDecimal yieldAmount, List<JSONObject> segDebug) {
            this.petCountAtHour = petCountAtHour;
            this.yieldAmount = yieldAmount == null ? BigDecimal.ZERO : yieldAmount;
            this.segDebug = segDebug == null ? Collections.emptyList() : segDebug;
        }
    }
    private static String formatDate(Date date){
        if (date==null)return "";
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }
}

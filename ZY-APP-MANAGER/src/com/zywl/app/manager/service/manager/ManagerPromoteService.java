package com.zywl.app.manager.service.manager;

import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.enums.PushCode;
import com.live.app.ws.util.Push;
import com.zywl.app.base.bean.*;
import com.zywl.app.base.constant.RedisKeyConstant;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.LockUtil;
import com.zywl.app.base.util.OrderUtil;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.cache.AppConfigCacheService;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.cache.UserCapitalCacheService;
import com.zywl.app.defaultx.enmus.LogUserBackpackTypeEnum;
import com.zywl.app.defaultx.enmus.RedReminderIndexEnum;
import com.zywl.app.defaultx.enmus.UserCapitalTypeEnum;
import com.zywl.app.defaultx.service.*;
import com.zywl.app.manager.context.MessageCodeContext;
import com.zywl.app.manager.service.PlayGameService;
import com.zywl.app.manager.socket.ManagerSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@ServiceClass(code = MessageCodeContext.PROMOTE_SERVER)
public class ManagerPromoteService extends BaseService {

    public static Map<String, UserInviteInfo> userInviteInfos = new ConcurrentHashMap<>();


    @Autowired
    private UserInviteInfoService userInviteInfoService;

    @Autowired
    private UserCacheService userCacheService;

    @Autowired
    private InviteRewardService inviteRewardService;

    @Autowired
    private UserReceiveInviteRecordService userReceiveInviteRecordService;

    @Autowired
    private LogUserBackpackService logUserBackpackService;

    @Autowired
    private LogUserCapitalService logUserCapitalService;

    @Autowired
    private UserCapitalService userCapitalService;

    @Autowired
    private UserAnimaTreeService userAnimaTreeService;


    @Autowired
    private UserCapitalCacheService userCapitalCacheService;

    @Autowired
    private PlayGameService gameService;

    @Autowired
    private AppConfigCacheService appConfigCacheService;

    @Autowired
    private UserStatisticService userStatisticService;

    @Autowired
    private ManagerSocketService managerSocketService;

    @Autowired
    private UserService userService;


    @Autowired
    private ManagerConfigService managerConfigService;


    public static Map<String, InviteReward> INVITE_REWARDS = new ConcurrentHashMap<>();

    public static Map<String, List<UserReceiveInviteRecord>> userInviteRecords = new ConcurrentHashMap<>();


    public static Object lock = new Object();

    @PostConstruct
    public void _construct() {
        initUserInviteInfo();
        initInviteReward();
    }


    public void initUserInviteRecord(Long userId) {
        UserInviteInfo userInviteInfo = getUserInviteInfo(userId.toString());
        Integer issue = userInviteInfo.getIssue();
        List<UserReceiveInviteRecord> byUserId = userCacheService.findUserReceiveInviteRecord(userId, issue);
        if (byUserId != null && byUserId.size() > 0) {
            userInviteRecords.put(userId.toString(), byUserId);
        } else {
            userInviteRecords.put(userId.toString(), new ArrayList<>());
        }
    }

    public void initUserInviteInfo() {
        logger.info("初始化邀请好友活动信息");
        List<UserInviteInfo> allInviteInfo = userInviteInfoService.findAllInviteInfo();
        for (UserInviteInfo userInviteInfo : allInviteInfo) {
            userInviteInfos.put(userInviteInfo.getUserId().toString(), userInviteInfo);
        }
        logger.info("初始化邀请好友活动信息完成");
    }

    public void initInviteReward() {
        logger.info("初始化邀请好友活动奖励信息");
        List<InviteReward> allInviteReward = inviteRewardService.findAllInviteReward();
        if (allInviteReward != null) {
            for (InviteReward inviteReward : allInviteReward) {
                INVITE_REWARDS.put(inviteReward.getId().toString(), inviteReward);
            }

        }
        logger.info("初始化邀请好友活动奖励信息完成");
    }

    public UserInviteInfo getUserInviteInfo(String userId) {
        UserInviteInfo userInviteInfo;
        if (userInviteInfos.containsKey(userId)) {
            userInviteInfo = userInviteInfos.get(userId);
            if (userInviteInfo.getEndTime().getTime() <= System.currentTimeMillis()) {
                //内存中的数据已过期  需要清理并重新插入
                UserInviteInfo newUserInviteInfo = userInviteInfoService.addUserInviteInfo(Long.parseLong(userId), userInviteInfo.getEndTime(), userInviteInfo.getIssue() + 1);
                userInviteInfos.remove(userId);
                userInviteInfos.put(userId, newUserInviteInfo);
                userInviteInfo = newUserInviteInfo;
            }
        } else {
            userInviteInfo = userInviteInfoService.findByUserId(Long.parseLong(userId));
            if (userInviteInfo != null) {
                userInviteInfos.put(userInviteInfo.getUserId().toString(), userInviteInfo);
            } else {
                //数据库中也没有 内存也没有 插入  需要先查询上期活动的结束时间
                userInviteInfo = userInviteInfoService.findLastByUserId(Long.parseLong(userId));
                if (userInviteInfo == null) {
                    //还是查不到 按照现在的时间插入
                    userInviteInfo = userInviteInfoService.addUserInviteInfo(Long.parseLong(userId));
                }
                userInviteInfos.put(userId, userInviteInfo);
            }
        }
        return userInviteInfo;
    }

    public void parentAddFriendNumber(Long myId) {
        User user = userCacheService.getUserInfoById(myId);
        if (user.getParentId() != null) {
            String parentId = user.getParentId().toString();
            getUserInviteInfo(parentId);
            int res = userInviteInfoService.addFriendNumber(user.getParentId());
            if (res > 0) {
                userInviteInfos.get(parentId).setNumber(userInviteInfos.get(parentId).getNumber() + 1);
            }
        }
    }

    public void parentAddEffectiveFriendNumber(Long myId) {
        User user = userCacheService.getUserInfoById(myId);
        if (user.getParentId() != null) {
            String parentId = user.getParentId().toString();
            getUserInviteInfo(parentId);
            int res = userInviteInfoService.addEffectiveFriendNumber(user.getParentId());
            if (res > 0) {
                userInviteInfos.get(parentId).setEffectiveNumber((userInviteInfos.get(parentId).getEffectiveNumber() + 1));
            }
        }
    }

    public void parentAddAdNumber(Long myId, Long parentId) {
        User my = userCacheService.getUserInfoById(myId);
        UserInviteInfo userInviteInfo = getUserInviteInfo(parentId.toString());
        if (my.getRegistTime().getTime() < userInviteInfo.getCreateTime().getTime()) {
            //非邀请时间内注册  跳过
            return;
        }
        Long allAdCount = userCacheService.getUserAdAllLookNum(myId);
        if (allAdCount > 25) {
            return;
        }
        Random r = new Random();
        int i = r.nextInt(100) + 1;
        if (i > managerConfigService.getInteger(Config.GOOD_AD)) {
            return;
        }
        int res = userInviteInfoService.addAdNumber(parentId);
        if (res > 0) {
            userInviteInfos.get(parentId.toString()).setAdNumber((userInviteInfos.get(parentId.toString()).getAdNumber() + 1));
        }
    }


    @Transactional
    @ServiceMethod(code = "001", description = "获取邀请活动的信息")
    public Object getInviteInfo(ManagerSocketServer adminSocketServer, JSONObject data) {
        checkNull(data);
        checkNull(data.get("userId"));
        String userId = data.getString("userId");
        UserInviteInfo userInviteInfo = getUserInviteInfo(userId);
        List<UserReceiveInviteRecord> receiveRecord = userCacheService.findUserReceiveInviteRecord(Long.parseLong(userId), userInviteInfo.getIssue());
        JSONObject result = new JSONObject();
        result.put("userInviteInfo", userInviteInfo);
        result.put("inviteReward", INVITE_REWARDS.values());
        Set<Long> list = new HashSet<>();
        int issue = userInviteInfo.getIssue();
        if (receiveRecord != null) {
            for (UserReceiveInviteRecord record : receiveRecord) {
                if (record.getIssue() == issue) {
                    list.add(record.getRewardId());
                }
            }
        }
        result.put("receiveRecord", list);
        return result;
    }


    @Transactional
    @ServiceMethod(code = "002", description = "领取邀请活动的奖励")
    public Object receiveInviteReward(ManagerSocketServer adminSocketServer, JSONObject data) {
        checkNull(data);
        checkNull(data.get("userId"), data.get("rewardId"));
        String userId = data.getString("userId");
        synchronized (LockUtil.getlock(userId)) {
            Long rewardId = data.getLong("rewardId");
            JSONObject result = new JSONObject();
            if (!INVITE_REWARDS.containsKey(rewardId.toString())) {
                throwExp("没有该奖励信息");
            }
            InviteReward inviteReward = INVITE_REWARDS.get(rewardId.toString());
            UserInviteInfo userInviteInfo = getUserInviteInfo(userId);
            synchronized (LockUtil.getlock(userId)) {
                if (userInviteInfo.getAdNumber() >= inviteReward.getAdNum() && userInviteInfo.getEffectiveNumber() >= inviteReward.getFriendNum()) {
                    //满足条件
                    //验证是否已领取
                    if (userReceiveInviteRecordService.findByUserId(Long.parseLong(userId), userInviteInfo.getIssue(), rewardId) != null) {
                        throwExp("该奖励已领取");
                    }
                    BigDecimal amount = inviteReward.getReward();
                    UserReceiveInviteRecord record = userReceiveInviteRecordService.addRecord(Long.parseLong(userId), userInviteInfo.getIssue(), rewardId, amount);
                    userInviteRecords.get(userId).add(record);
                    if (amount != null) {
                        userCapitalService.addUserBalanceByReceiveInvite(amount, Long.parseLong(userId), UserCapitalTypeEnum.rmb.getValue(), record.getId(), OrderUtil.getOrder5Number());
                        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(Long.parseLong(userId), UserCapitalTypeEnum.rmb.getValue());
                        JSONObject pushData = new JSONObject();
                        pushData.put("userId", userId);
                        pushData.put("capitalType", UserCapitalTypeEnum.rmb.getValue());
                        pushData.put("balance", userCapital.getBalance());
                        Push.push(PushCode.updateUserCapital, managerSocketService.getServerIdByUserId(Long.parseLong(userId)), pushData);
                    }
                } else {
                    throwExp("未满足领取条件");
                }
            }
            userCacheService.removeUserReceiveInviteRecord(Long.parseLong(userId), userInviteInfo.getIssue());
            return data;
        }
    }


    @Transactional
    @ServiceMethod(code = "006", description = "获取渠道信息")
    public Object getChannelInfo(ManagerSocketServer adminSocketServer, JSONObject data) {
        checkNull(data);
        checkNull(data.get("userId"));
        Long userId = data.getLong("userId");
        User user = userCacheService.getUserInfoById(userId);
        BigDecimal cashSill = new BigDecimal(appConfigCacheService.getConfigByKey(RedisKeyConstant.APP_CONFIG_CHANNEL_CASH_SILL, Config.CHANNEL_CASH_SILL));
        long todayChannelNum = userCacheService.getTodayChannelAddUserNum(userId);
        long allChannelNum = userService.getChannelNum(userId, user.getChannelNo());
        UserStatistic userStatistic = gameService.getUserStatistic(userId.toString());
        BigDecimal nowIncome = userStatistic.getNowChannelIncome();
        Double todayIncome = userCacheService.getTodayChannelIncome(userId);
        BigDecimal allIncome = userStatistic.getChannelIncome();
        JSONObject result = new JSONObject();
        result.put("todayIncome", todayIncome);
        result.put("allIncome", allIncome);
        result.put("todayAddNum", todayChannelNum);
        result.put("allAddNum", allChannelNum);
        result.put("nowIncome", nowIncome);
        result.put("cashSill", cashSill);
        return result;
    }


}

package com.zywl.app.defaultx.cache;

import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.enums.PushCode;
import com.live.app.ws.util.Push;
import com.zywl.app.base.bean.*;
import com.zywl.app.base.constant.KafkaEventContext;
import com.zywl.app.base.constant.RedisKeyConstant;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.defaultx.cache.impl.RedisService;
import com.zywl.app.defaultx.enmus.AdvertIndexEnum;
import com.zywl.app.defaultx.enmus.RedReminderIndexEnum;
import com.zywl.app.defaultx.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class UserCacheService extends RedisService {

    @Autowired
    private UserService userService;

    @Autowired
    private UserReceiveInviteRecordService userReceiveInviteRecordService;

    @Autowired
    private UserConfigService userConfigService;

    @Autowired
    private WsidCaCheService wsidCaCheService;


    @Autowired
    private UserSignCacheService userSignCacheService;

    @Autowired
    private UserCapitalCacheService userCapitalCacheService;


    @Autowired
    private UserStatisticService userStatisticService;

    @Autowired
    private UserPowerService userPowerService;

    @Autowired
    private VersionService versionService;


    public User getUserInfoById(Long userId) {
        String key = RedisKeyConstant.APP_USER_INFO + userId + "-";
        User user = JSONObject.parseObject(get(key), User.class);
        if (user == null) {
            user = userService.findById(userId);
            if (user != null) {
                set(key, user, 600L);

            }
        }
        return user;
    }

    public void addIpUser(String loginIp,Long userId){
        String key = RedisKeyConstant.IP_USER+loginIp;
        hset(key,String.valueOf(userId),loginIp);
    }



    public boolean canLogin(String loginIp,Long userId){
        String key = RedisKeyConstant.IP_USER+loginIp;
        Map<String, Object> hmget = hmget(key);
        if (hmget.size()<=3){
            return true;
        }
        Object hget = hget(key, String.valueOf(userId));
        if (hget!=null){
            return true;
        }
        return false;
    }

    public User getUserInfoByGameToken(String gameToken) {
        User user = userService.findByUserGameToken(gameToken);
        return user;
    }

    public User getUserInfoById(String userId) {
        return getUserInfoById(Long.parseLong(userId));
    }

    public User getUserInfoByUserNo(String userNo) {
        Long userId = get(RedisKeyConstant.APP_USER_CODE_ID + userNo + "-", Long.class);
        if (userId != null && userId != 0) {
            return getUserInfoById(userId);
        }
        User user = userService.findByUserNo(userNo);
        if (user != null) {
            set(RedisKeyConstant.APP_USER_CODE_ID + user.getUserNo() + "-", user.getId(), 6000L);
            set(RedisKeyConstant.APP_USER_INFO + userId + "-", user, 600L);
        }
        return user;
    }

    public void removeUserCodeToIdCache(String userNo) {
        String key = get(RedisKeyConstant.APP_USER_CODE_ID + userNo + "-");
        del(RedisKeyConstant.APP_USER_CODE_ID + userNo + "-");
        if (key != null) {
            del(key);
        }
    }

    public void removeUserInfoCache(Long userId) {
        del(RedisKeyConstant.APP_USER_INFO + userId + "-");
    }

    public UserConfig getUserSetting(Long userId) {
        String key = RedisKeyConstant.APP_USER_SETTING + userId + "-";
        UserConfig userConfig = get(key, UserConfig.class);
        if (userConfig == null) {
            userConfig = userConfigService.findUserConfigByUserId(userId);
            if (userConfig != null) {
                set(key, userConfig, 1000L);
            }
        }
        return userConfig;
    }

    public void removeUserSetting(Long userId) {
        String key = RedisKeyConstant.APP_USER_SETTING + userId + "-";
        del(key);
    }

    // 用户红点提醒集合
    public List<String> getPlayerRedReminderList(Long userId) {
        String key = RedisKeyConstant.APP_USER_REDREMINDER_LIST + userId + "-";
        List<String> list = getList(key, String.class);
        if (list == null) {
            list = new ArrayList<String>();
        }
        return list;
    }

    // 更新用户红点集合
    public void setPlayerReminderList(Long userId, List<String> list) {
        String key = RedisKeyConstant.APP_USER_REDREMINDER_LIST + userId + "-";
        set(key, list, 1440 * 60L * 2);
    }


    public Long getUserAdAllLookNum(Long userId) {
        long allCount = 0L;
        for (AdvertIndexEnum value : AdvertIndexEnum.values()) {
            allCount += getUserAdvertLookNum(userId, value.getIndex());
        }
        return allCount;
    }

    // 用户广告位观看次数
    public Long getUserAdvertLookNum(Long userId, int adIndex) {
        String key = RedisKeyConstant.APP_USER_ADVERT + DateUtil.format2(new Date()) + ":" + userId;
        Long a = (Long) hget(key, String.valueOf(adIndex));
        if (a == null) {
            Long allCount = 0L;
            hset(key, String.valueOf(adIndex), allCount, 86400);
            return allCount;
        }
        return a;
    }


    // 增加用户广告位观看次数
    public void addUserAdvertLookNum(Long userId, int adIndex, Long parentId, boolean isVip) {
        String key = RedisKeyConstant.APP_USER_ADVERT + DateUtil.format2(new Date()) + ":" + userId;
        if (!isVip) {
            String key2 = RedisKeyConstant.APP_PLATFORM_ADVERT + DateUtil.format2(new Date()) + "-";
            incr(key2, 1L);
            expire(key2, 86400L * 7);
        }
        Long userAdvertLookNum = getUserAdvertLookNum(userId, adIndex);
        hset(key, String.valueOf(adIndex), userAdvertLookNum + 1);
    }


    public Long getPlatformAdvertLookNum() {
        String key = RedisKeyConstant.APP_PLATFORM_ADVERT + DateUtil.format2(new Date()) + "-";
        String a = get(key);
        if (a == null) {
            Long allCount = 0L;
            incr(key, allCount);
            expire(key, 86400L * 7);
            return allCount;
        }
        return Long.parseLong(a);
    }


    // 离线 用户待领取铜钱
    public BigDecimal getUserNoCompleteCoin(Long userId) {
        String key = RedisKeyConstant.APP_USER_NO_COMPLETE_COIN + userId + "-";
        BigDecimal amount = get(key, BigDecimal.class);
        return amount;
    }

    // 离线 存放用户待领取铜钱
    public void saveUserNoCompleteCoin(Long userId, BigDecimal amount) {
        String key = RedisKeyConstant.APP_USER_NO_COMPLETE_COIN + userId + "-";
        set(key, amount);
    }

    // 离线 移除用户待领取铜钱
    public void removeUserNoCompleteCoin(Long userId) {
        String key = RedisKeyConstant.APP_USER_NO_COMPLETE_COIN + userId + "-";
        del(key);
    }

    // 用户注销，移除相关缓存
    public void deleteUser(Long userId, String wsId, String userNo) {
        wsidCaCheService.removeUserWs(userId);
        wsidCaCheService.removeWsid(wsId);
        removeUserInfoCache(userId);
        removeUserSetting(userId);
        removeUserCodeToIdCache(userNo);
        userCapitalCacheService.deletedUserAllCapitalCache(userId);
        userSignCacheService.removeCache(userId);
    }

    // 获取下级人数
    public Long getMySonCount(Long userId, int type) {
        return userService.findMySonCount(userId, type);
    }

    // 下级人数增加
    public void addSonCount(Long userId, int type) {
        String key = RedisKeyConstant.APP_USER_SON_COUNT + userId + ":" + type + "-";
        incr(key, 1L);
        expire(key, 86400);
    }

    // 下级人数减少
    public void subSonCount(Long userId, int type) {
        String key = RedisKeyConstant.APP_USER_SON_COUNT + userId + ":" + type + "-";
        decr(key, 1L);
        expire(key, 86400);
    }

    // 获取今日偷蛋刷新列表次数
    public Long todayRerfeshStolenCount(Long userId) {
        String key = RedisKeyConstant.APP_USER_REFRESH_STOLEN_COUNT + DateUtil.format2(new Date()) + ":" + userId + "-";
        return getaLong(key);
    }

    // 今日偷蛋刷新列表次数增加
    public void addTodayRerfeshStolenCount(Long userId) {
        String key = RedisKeyConstant.APP_USER_REFRESH_STOLEN_COUNT + DateUtil.format2(new Date()) + ":" + userId + "-";
        incr(key, 1L);
        expire(key, 86400);
    }

    // 获取今日偷蛋次数
    public Long todayStolenEggCount(Long userId) {
        String key = RedisKeyConstant.APP_USER_STOLEN_COUNT + DateUtil.format2(new Date()) + ":" + userId + "-";
        return getaLong(key);
    }

    private Long getaLong(String key) {
        String count = get(key);
        if (count == null) {
            return 0L;
        }
        return Long.parseLong(count);
    }

    private Double getaDouble(String key) {
        String count = get(key);
        if (count == null) {
            return 0.0;
        }
        return Double.parseDouble(count);
    }

    // 今日偷蛋次数增加
    public void addTodayStolenEggCount(Long userId) {
        String key = RedisKeyConstant.APP_USER_STOLEN_COUNT + DateUtil.format2(new Date()) + ":" + userId + "-";
        incr(key, 1L);
        expire(key, 86400);
    }

    //获取今日用户技能释放次数
    public Long getSkillUseCount(Long userId, int type) {
        String key = RedisKeyConstant.APP_USER_SKILL_COUNT + DateUtil.format2(new Date()) + ":" + userId + ":" + type + "-";
        return getaLong(key);
    }

    //获取完成每日任务的数量


    public void removeUserDailyTask(Long userId) {
        String key = RedisKeyConstant.APP_USER_TASK + DateUtil.format2(new Date()) + ":" + userId + "-";
        del(key);
    }

    //完成每日任务数量+1

    //获取今日用户抽奖次数
    public Long getUserPrizeDrawCount(Long userId) {
        String key = RedisKeyConstant.APP_USER_PRIZE_COUNT + DateUtil.format2(new Date()) + ":" + userId + "-";
        return getaLong(key);
    }

    //今日释放技能次数增加

    public void addTodayPrizeDrawCount(Long userId, Long count) {
        String key = RedisKeyConstant.APP_USER_PRIZE_COUNT + DateUtil.format2(new Date()) + ":" + userId + "-";
        incr(key, count);
        expire(key, 86400);
    }

    public Long getUserAddCoinCount(Long userId) {
        String key = RedisKeyConstant.APP_USER_ADD_COIN_COUNT + DateUtil.format2(new Date()) + ":" + userId + "-";
        return getaLong(key);
    }


    public void addTodayAddCoinCount(Long userId, Long count) {
        String key = RedisKeyConstant.APP_USER_ADD_COIN_COUNT + DateUtil.format2(new Date()) + ":" + userId + "-";
        incr(key, count);
        expire(key, 86400);
    }

    public Long getUserAddMpCount(Long userId) {
        String key = RedisKeyConstant.APP_USER_ADD_MP_COUNT + DateUtil.format2(new Date()) + ":" + userId + "-";
        return getaLong(key);
    }


    public void addTodayAddMpCount(Long userId, Long count) {
        String key = RedisKeyConstant.APP_USER_ADD_MP_COUNT + DateUtil.format2(new Date()) + ":" + userId + "-";
        incr(key, count);
        expire(key, 86400);
    }

    public Long getUserElixirSpeedCount(Long userId) {
        String key = RedisKeyConstant.APP_USER_ELIXIR_SPEED_COUNT + DateUtil.format2(new Date()) + ":" + userId + "-";
        return getaLong(key);
    }


    public void addTodayElixirSpeedCount(Long userId, Long count) {
        String key = RedisKeyConstant.APP_USER_ELIXIR_SPEED_COUNT + DateUtil.format2(new Date()) + ":" + userId + "-";
        incr(key, count);
        expire(key, 86400);
    }

    public Long getTodayUserPlayCount(Long userId) {
        String key = RedisKeyConstant.APP_USER_PLAYER_AREA + DateUtil.format2(new Date()) + ":" + userId + "-";
        return getaLong(key);
    }


    public void addTodayUserPlayCount(Long userId) {
        String key = RedisKeyConstant.APP_USER_PLAYER_AREA + DateUtil.format2(new Date()) + ":" + userId + "-";
        incr(key, 1L);
        expire(key, 86400);
    }


    public Long getUserAreaCount(Long userId) {
        String key = RedisKeyConstant.APP_USER_AREA_COUNT + DateUtil.format2(new Date()) + ":" + userId + "-";
        return getaLong(key);
    }


    public void addTodayAreaCount(Long userId, Long count) {
        String key = RedisKeyConstant.APP_USER_AREA_COUNT + DateUtil.format2(new Date()) + ":" + userId + "-";
        incr(key, count);
        expire(key, 86400);
    }

    public Long getUserAddPlCount(Long userId) {
        String key = RedisKeyConstant.APP_USER_ADD_PL_COUNT + DateUtil.format2(new Date()) + ":" + userId + "-";
        return getaLong(key);
    }


    public void addTodayAddPlCount(Long userId, Long count) {
        String key = RedisKeyConstant.APP_USER_ADD_PL_COUNT + DateUtil.format2(new Date()) + ":" + userId + "-";
        incr(key, count);
        expire(key, 86400);
    }


    public void addTodaySkillUseCount(Long userId, int type) {
        String key = RedisKeyConstant.APP_USER_SKILL_COUNT + DateUtil.format2(new Date()) + ":" + userId + ":" + type + "-";
        incr(key, 1L);
        expire(key, 86400);
    }


    @Transactional
    public void addParentAnima(Long myId, String parentId, BigDecimal number) {
        User user = getUserInfoById(myId);
        addTodayMyCreateAnima(myId, number);
        userStatisticService.updateUserCreateAnima(myId, number);
        userStatisticService.updateUserGetAnima(Long.parseLong(parentId), number);
    }

    @Transactional
    public void addParentAnima2(Long myId, String parentId, BigDecimal number) {
        User user = getUserInfoById(myId);
        addTodayMyCreateAnima2(myId, number);
        userStatisticService.updateUserCreateAnima2(myId, number);
        userStatisticService.updateUserGetAnima2(Long.parseLong(parentId), number);
    }

    @Transactional
    public void addGrandfaAnima(Long myId, String grandfaId, BigDecimal number) {
        User user = getUserInfoById(myId);
        addTodayMyCreateGrandfaAnima(myId, number);
        userStatisticService.updateUserCreateGrandfaAnima(myId, number);
        userStatisticService.updateUserGetAnima(Long.parseLong(grandfaId), number);
    }

    public List<UserReceiveInviteRecord> findUserReceiveInviteRecord(Long userId, int issue) {
        String key = RedisKeyConstant.APP_USER_RECEIVE_INVITE_RECORD + userId + ":" + issue + "-";
        List<UserReceiveInviteRecord> records;
        records = getList(key, UserReceiveInviteRecord.class);
        if (records == null) {
            records = userReceiveInviteRecordService.findByUserId(userId, issue);
            set(key, records, 1440 * 60L * 2);
        }
        return records;
    }

    public void removeUserReceiveInviteRecord(Long userId, int issue) {
        String key = RedisKeyConstant.APP_USER_RECEIVE_INVITE_RECORD + userId + ":" + issue + "-";
        del(key);
    }

    public void addTodayMyCreateAnima(Long userId, BigDecimal anima) {
        String key = RedisKeyConstant.APP_USER_CREATE_ANIMA + DateUtil.format2(new Date()) + ":" + userId + "-";
        User user = getUserInfoById(userId);
        if (user.getParentId() != null) {
            addParentTodayAnima(user.getParentId(), anima);
        }
        incrDouble(key, anima.doubleValue());
        expire(key, 86400);
    }

    public void addTodayMyCreateAnima2(Long userId, BigDecimal anima) {
        String key = RedisKeyConstant.APP_USER_CREATE_ANIMA2 + DateUtil.format2(new Date()) + ":" + userId + "-";
        User user = getUserInfoById(userId);
        if (user.getParentId() != null) {
            addParentTodayAnima2(user.getParentId(), anima);
        }
        incrDouble(key, anima.doubleValue());
        expire(key, 86400);
    }

    public void addTodayMyCreateGrandfaAnima(Long userId, BigDecimal anima) {
        String key = RedisKeyConstant.APP_USER_CREATE_GRANDFA_ANIMA + DateUtil.format2(new Date()) + ":" + userId + "-";
        User user = getUserInfoById(userId);
        if (user.getGrandfaId() != null) {
            addParentTodayAnima(user.getGrandfaId(), anima);
        }
        incrDouble(key, anima.doubleValue());
        expire(key, 86400);
    }

    public void addParentTodayAnima(Long parentId, BigDecimal anima) {
        String key = RedisKeyConstant.APP_USER_GET_ANIMA + DateUtil.format2(new Date()) + ":" + parentId + "-";
        incrDouble(key, anima.doubleValue());
        expire(key, 86400);
    }

    public void addParentTodayAnima2(Long parentId, BigDecimal anima) {
        String key = RedisKeyConstant.APP_USER_GET_ANIMA2 + DateUtil.format2(new Date()) + ":" + parentId + "-";
        incrDouble(key, anima.doubleValue());
        expire(key, 86400);
    }

    public double getTodayMyCreateAnima(Long userId) {
        String key = RedisKeyConstant.APP_USER_CREATE_ANIMA + DateUtil.format2(new Date()) + ":" + userId + "-";
        return getaDouble(key);
    }

    public double getTodayMyCreateAnima2(Long userId) {
        String key = RedisKeyConstant.APP_USER_CREATE_ANIMA2 + DateUtil.format2(new Date()) + ":" + userId + "-";
        return getaDouble(key);
    }

    public double getTodayMyCreateGrandfaAnima(Long userId) {
        String key = RedisKeyConstant.APP_USER_CREATE_GRANDFA_ANIMA + DateUtil.format2(new Date()) + ":" + userId + "-";
        return getaDouble(key);
    }

    public double getTodayMyCreateGrandfaAnima2(Long userId) {
        String key = RedisKeyConstant.APP_USER_CREATE_GRANDFA_ANIMA2 + DateUtil.format2(new Date()) + ":" + userId + "-";
        return getaDouble(key);
    }

    public double getTodayMyGetAnima(Long userId) {
        String key = RedisKeyConstant.APP_USER_GET_ANIMA + DateUtil.format2(new Date()) + ":" + userId + "-";
        return getaDouble(key);
    }

    public double getTodayMyGetAnima2(Long userId) {
        String key = RedisKeyConstant.APP_USER_GET_ANIMA2 + DateUtil.format2(new Date()) + ":" + userId + "-";
        return getaDouble(key);
    }


    public double getTodayCreateParentIncome(Long userId) {
        String key = RedisKeyConstant.APP_USER_CREATE_INCOME + DateUtil.format2(new Date()) + ":" + userId + "-";
        return getaDouble(key);
    }

    public void addParentTodayIncome(Long userId, BigDecimal income) {
        String key = RedisKeyConstant.APP_USER_CREATE_INCOME + DateUtil.format2(new Date()) + ":" + userId + "-";
        incrDouble(key, income.doubleValue());
        expire(key, 86400);
    }

    public void addGrandfaTodayIncome(Long userId, BigDecimal income) {
        String key = RedisKeyConstant.APP_USER_CREATE_GRANDFA_INCOME + DateUtil.format2(new Date()) + ":" + userId + "-";
        incrDouble(key, income.doubleValue());
        expire(key, 86400);
    }

    public double getGrandfaTodayIncome(Long userId) {
        String key = RedisKeyConstant.APP_USER_CREATE_GRANDFA_INCOME + DateUtil.format2(new Date()) + ":" + userId + "-";
        return getaDouble(key);
    }

    public void checkAddRedminder(Long userId, RedReminderIndexEnum em) {
        JSONObject obj = new JSONObject();
        List<String> indexList = getPlayerRedReminderList(userId);
        if (indexList.contains(em.getValue())) {
            return;
        }
        indexList.add(em.getValue());
        obj.put("indexList", indexList);
        obj.put("userId", userId);
        setPlayerReminderList(userId, indexList);
        Push.push(PushCode.redReminder, null, obj);
    }

    public void beginLookAd(Long userId, int index) {
        String key = RedisKeyConstant.APP_USER_BEGIN_LOOK_AD + userId + ":" + AdvertIndexEnum.getAdName(index) + "-";
        set(key, System.currentTimeMillis(), 10);
    }

    public String getUserServerId(String userId) {
        String key = RedisKeyConstant.APP_USER_SERVER_ID + userId + "-";
        return get(key);
    }

    public void setUserServerId(String userId, String serverId) {
        String key = RedisKeyConstant.APP_USER_SERVER_ID + userId + "-";
        set(key, serverId);
    }

    public void removeUserServerId(String userId) {
        String key = RedisKeyConstant.APP_USER_SERVER_ID + userId + "-";
        del(key);
    }

    public boolean isLookAd(Long userId, int index) {
        String key = RedisKeyConstant.APP_USER_BEGIN_LOOK_AD + userId + ":" + AdvertIndexEnum.getAdName(index) + "-";
        return hasKey(key);
    }

    public void addTodayChannelIncome(Long userId, Double income) {
        String key = RedisKeyConstant.APP_USER_CHANNEL_INCOME + DateUtil.format2(new Date()) + ":" + userId + "-";
        incrDouble(key, income);
        expire(key, 86400);
    }

    public Double getTodayChannelIncome(Long userId) {
        String key = RedisKeyConstant.APP_USER_CHANNEL_INCOME + DateUtil.format2(new Date()) + ":" + userId + "-";
        return getaDouble(key);
    }

    public void addTodayLogin() {
        String key = RedisKeyConstant.APP_PLATFORM_TODAY_LOGIN + DateUtil.format2(new Date());
        incr(key, 1L);
        expire(key, 86400);
    }

    public Long getTodayLogin() {
        String key = RedisKeyConstant.APP_PLATFORM_TODAY_LOGIN + DateUtil.format2(new Date());
        return getaLong(key);
    }

    public void addTodayRegister() {
        String key = RedisKeyConstant.APP_PLATFORM_TODAY_REGISTER + DateUtil.format2(new Date());
        addTodayLogin();
        incr(key, 1L);
        expire(key, 86400);
    }

    public Long getTodayRegister() {
        String key = RedisKeyConstant.APP_PLATFORM_TODAY_REGISTER + DateUtil.format2(new Date());
        return getaLong(key);
    }

    public String getCdTime(Long userId) {
        String key = RedisKeyConstant.APP_USER_MAIL_CD + userId + "-";
        if (hasKey(key)) {
            return get(key);
        } else {
            return null;
        }
    }

    public void setUserCd(Long userId, int s) {
        String key = RedisKeyConstant.APP_USER_MAIL_CD + userId + "-";
        set(key, DateUtil.format10(DateUtil.getDateByM(s * 60)), s * 60);
    }

    public void addTodayChannelAddUserNum(Long userId) {
        String key = RedisKeyConstant.APP_USER_CHANNEL_ADD + DateUtil.format2(new Date()) + ":" + userId + "-";
        incr(key, 1L);
    }

    public long getTodayChannelAddUserNum(Long userId) {
        String key = RedisKeyConstant.APP_USER_CHANNEL_ADD + DateUtil.format2(new Date()) + ":" + userId + "-";
        return getaLong(key);
    }


    public void setUserInviteRisk(Long userId) {
        String key = RedisKeyConstant.APP_USER_INVITE_RISK + userId + "-";
        set(key, 1, 1);
    }

    public boolean isRisk(Long userId) {
        String key = RedisKeyConstant.APP_USER_INVITE_RISK + userId + "-";
        return hasKey(key);
    }

    public void addRiskTime(Long userId) {
        String key = RedisKeyConstant.APP_USER_INVITE_RISK + userId + "-";
        expire(key, 600);
    }

    public boolean isReceiveMagic(Long userId) {
        String key = RedisKeyConstant.APP_USER_IS_RECEIVE_MAGIC + DateUtil.format2(new Date()) + ":" + userId + "-";
        return hasKey(key);
    }

    public void userReceiveMagic(Long userId) {
        String key = RedisKeyConstant.APP_USER_IS_RECEIVE_MAGIC + DateUtil.format2(new Date()) + ":" + userId + "-";
        set(key, 1, 86400L);
    }

    public Long getUserPower(Long userId) {
        return getUserPower(userId.toString());
    }

    public Long getUserPower(String userId) {
        String key = RedisKeyConstant.APP_USER_POWER + userId + "-";
        String result = get(key);
        Long power = null;
        if (isNull(result)) {
            UserPower userPower = userPowerService.findByUserId(userId);
            if (userPower != null) {
                insertUserPowerCache(userId, userPower.getPower());
                power = userPower.getPower();
            }
            return power;
        }
        power = Long.parseLong(result);
        return power;
    }

    public void removeAllUserPower() {
        deleteByLikeKey(RedisKeyConstant.APP_USER_POWER + "*");
    }

    public void insertUserPowerCache(String userId, Long power) {
        String key = RedisKeyConstant.APP_USER_POWER + userId + "-";
        set(key, power, 600L);
    }

    public void removeUserPower(String userId) {
        String key = RedisKeyConstant.APP_USER_POWER + userId + "-";
        del(key);
    }


    public void userReceiveMagicTime(String userId) {
        String key = RedisKeyConstant.RECEIVE_MAGIC + userId + "-";
        set(key, 1, 1L);
    }

    public boolean canReceive(String userId) {
        String key = RedisKeyConstant.RECEIVE_MAGIC + userId + "-";
        return hasKey(key);
    }

    public void userReceiveMailTime(String userId) {
        String key = RedisKeyConstant.RECEIVE_MAIL + userId + "-";
        set(key, 1, 2L);
    }

    public boolean canReceiveMail(String userId) {
        String key = RedisKeyConstant.RECEIVE_MAIL + userId + "-";
        return hasKey(key);
    }

    public List<Integer> getUserPetTypes(String userId) {
        String key = RedisKeyConstant.USER_PET_TYPES + userId + "-";
        return getList(key, Integer.class);
    }

    public void removeUserPetTypes(String userId) {
        String key = RedisKeyConstant.USER_PET_TYPES + userId + "-";
        del(key);
    }

    public void addPetTypeCache(String userId, List<Integer> list) {
        String key = RedisKeyConstant.USER_PET_TYPES + userId + "-";
        set(key, list, 60 * 60);
    }


    public long getUserJoinCount(Long userId) {
        String key = RedisKeyConstant.USER_ANCIENT_JION_COUNT + DateUtil.format2(new Date()) + ":" + userId + "-";
        return getaLong(key);
    }

    public void userJoinAncient(Long userId) {
        String key = RedisKeyConstant.USER_ANCIENT_JION_COUNT + DateUtil.format2(new Date()) + ":" + userId + "-";
        incr(key, 1L);
        expire(key, 86400);
    }

    public void addUserConvertCount(long userId) {
        String key = RedisKeyConstant.USER_CONVERT_COUNT + DateUtil.format2(new Date()) + ":" + userId + "-";
        set(key, 1, 86400);
    }

    public boolean canConvert(long userId) {
        String key = RedisKeyConstant.USER_CONVERT_COUNT + DateUtil.format2(new Date()) + ":" + userId + "-";
        return hasKey(key);
    }

    public void addTicketPrize(Long userId, List<Long> supplyIds) {
        String key = RedisKeyConstant.USER_TICKET_PRIZE + userId + "-";
        set(key, supplyIds, 5);
    }

    public void addPrizeIds(Long userId, List<Long> ids) {
        String key = RedisKeyConstant.USER_TICKET_PRIZE + userId + "-";
        set(key, ids, 10);
    }

    public List<Long> getPrizeIds(Long userId) {
        String key = RedisKeyConstant.USER_TICKET_PRIZE + userId + "-";
        List<Long> ids = getList(key, Long.class);
        del(key);
        return ids;
    }


    public List<Long> getTicketPrize(Long userId) {
        String key = RedisKeyConstant.USER_TICKET_PRIZE + userId + "-";
        List<Long> supplyIds = getList(key, Long.class);
        return supplyIds;
    }

    public void removeTicketPrize(Long userId) {
        String key = RedisKeyConstant.USER_TICKET_PRIZE + userId + "-";
        del(key);
    }


    public void addUserConvertCount2(long userId) {
        String key = "t:app:user:convertCount:2" + DateUtil.format2(new Date()) + ":" + userId + "-";
        set(key, 1, 86400);
    }

    public boolean canConvert2(long userId) {
        String key = "t:app:user:convertCount:2" + DateUtil.format2(new Date()) + ":" + userId + "-";
        return hasKey(key);
    }

    public boolean userTodayIsLogin(Long userId) {
        String key = RedisKeyConstant.APP_USER_TODAY_IS_LOGIN + DateUtil.format2(new Date()) + ":" + userId;
        if (hasKey(key)) {
            return true;
        }
        set(key, 1, 86400);
        return false;
    }

    public Map getUserTopLike(Long userId) {
        String key = RedisKeyConstant.TOP_LIKE + DateUtil.format2(new Date()) + ":" + userId;
        Map<String, Object> topLike = hmget(key);
        return topLike;
    }

    public int getUserTopLikeByType(Long userId, int type) {
        String key = RedisKeyConstant.TOP_LIKE + DateUtil.format2(new Date()) + ":" + userId;
        Object hget = hget(key, String.valueOf(type));
        if (hget != null) {
            return Integer.parseInt(hget.toString());
        }
        return 0;
    }

    public void userTopLike(Long userId, int type) {
        String key = RedisKeyConstant.TOP_LIKE + DateUtil.format2(new Date()) + ":" + userId;
        hset(key, String.valueOf(type), 1, 86400);
    }

    public Version getVersion() {
        String key = RedisKeyConstant.GAME_VERSION;
        Version version = get(key, Version.class);
        if (version == null) {
            List<Version> versions = versionService.getReleaseVersions(1);
            if (versions != null && !versions.isEmpty()) {
                version = versions.get(0);    //当前最新版本
            } else {
                throwExp("版本维护中");
            }
        }
        return version;
    }

    public void removeVersionCache() {
        String key = RedisKeyConstant.GAME_VERSION;
        del(key);
    }

    public void addUserTodayCreateSw(Long userId, BigDecimal sw) {
        String key = RedisKeyConstant.TODAY_CREATE_SW + DateUtil.format2(new Date()) + ":" + userId;
        BigDecimal bigDecimal = get(key, BigDecimal.class);
        if (bigDecimal == null) {
            set(key, sw, 86400L);
        } else {
            set(key, sw.add(bigDecimal), 86400);
        }
    }

    public BigDecimal getUserTodayCreateSw(Long userId) {
        String key = RedisKeyConstant.TODAY_CREATE_SW + DateUtil.format2(new Date()) + ":" + userId;
        String bigDecimal = get(key, String.class);
        if (bigDecimal == null) {
            return BigDecimal.ZERO;
        } else {
            return new BigDecimal(bigDecimal);
        }
    }
}

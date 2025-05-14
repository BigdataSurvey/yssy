package com.zywl.app.manager.service.manager;

import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.enums.PushCode;
import com.live.app.ws.util.Push;
import com.zywl.app.base.bean.Config;
import com.zywl.app.base.bean.UserCapital;
import com.zywl.app.base.bean.UserDzPeriods;
import com.zywl.app.base.bean.UserDzRecord;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.base.util.LockUtil;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.cache.DzCacheService;
import com.zywl.app.defaultx.cache.impl.RedisService;
import com.zywl.app.defaultx.enmus.DzStatus;
import com.zywl.app.defaultx.enmus.UserCapitalTypeEnum;
import com.zywl.app.defaultx.service.DzPeriodsService;
import com.zywl.app.defaultx.service.DzService;
import com.zywl.app.defaultx.service.UserCapitalService;
import com.zywl.app.manager.context.MessageCodeContext;
import com.zywl.app.manager.socket.ManagerSocketServer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/***
 * 打坐游戏service
 */
@Service
@ServiceClass(code = MessageCodeContext.DZ_SERVER)
public class ManagerDzService extends BaseService {

    private static final Log logger = LogFactory.getLog(ManagerDzService.class);

    @Autowired
    private DzPeriodsService dzPeriodsService;

    @Autowired
    private DzService dzService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private DzCacheService dzCacheService;
    @Autowired
    private UserCapitalService userCapitalService;
    @Autowired
    private ManagerSocketService managerSocketService;

    @Autowired
    private ManagerConfigService managerConfigService;

    public static Integer periods = null;


    public void initPeriods() {
        if (null == periods) {
            UserDzPeriods userDzPeriods = dzPeriodsService.findOne();
            if (null != userDzPeriods)  {
                periods = userDzPeriods.getPeriods() + 1;
            } else {
                periods = 1;
            }
        }
    }

    @PostConstruct
    public void _construct() {

        new Timer("重置打坐期数").schedule(new TimerTask() {
            public void run() {
                try {
                    periods = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, DateUtil.getDzPeriodsTaskDate() - System.currentTimeMillis(), 1000 * 60 * 60 * 24);
    }


//    @ServiceMethod(code = "001", description = "获取打坐初始信息")
//    public JSONObject getDzInfo(ManagerSocketServer adminSocketServer, JSONObject data) {
//        checkNull(data);
//        long userId = data.getLongValue("userId");
//        JSONObject result = getRedisData();
//        UserDzPeriods userDzPeriods = dzCacheService.getDzInitInfo();
//        //当前期数，上一期不展示
//        result.put("periods",userDzPeriods.getPeriods()+1);
//        //幸运儿userId
//        result.put("luckUserId",userDzPeriods.getUserId());
//        result.put("luckUserName",userDzPeriods.getUserName());
//        result.put("luckImage",userDzPeriods.getUserImage());
//        //幸运儿瓜分的灵石数量
//        result.put("cuMoney",userDzPeriods.getCuMoney());
//        //报名总人数
//        result.put("userDzBmNum",userDzPeriods.getUserDZNum());
//        //未打卡人数
//        result.put("userNoDkNum",userDzPeriods.getUserDkNum());
//        //玩家是否报名，签到，领取，true签到 空值 或者 0 就是没签到  没报名  没领取
//        result.put("bmStatus",dzCacheService.getUserBmInfo(userId)); // false  就是用户没报名 显示报名 true 是已报名
//        result.put("dkStatus",dzCacheService.getUserDkInfo(userId)); // false  用户没签到 true 是已签到
//        result.put("lqStatus",dzCacheService.getUserLqInfo(userId+"")); // false 不可以领取，true 可以领取
//        //这一期报名人数 灵石数量
//        result.put("nowPeriodsNum",dzCacheService.getUsersNum());
//        result.put("totalMoney",dzCacheService.getUsersMoney());
//        //用户头像 抽取10人头像 给的是数组 List<String>
//        result.put("userTenImageUrl",dzCacheService.getUserImageUrl());
//        logger.info("执行结束========" + result.toJSONString());
//        return result;
//    }

    @Transactional
    @ServiceMethod(code = "002", description = "玩家报名参加打坐游戏")
    public JSONObject addUserDzBmInfo(ManagerSocketServer adminSocketServer, JSONObject data) {
        checkNull(data);
        if (1 != managerConfigService.getDouble(Config.DZ_GAME_ON)) {
            throwExp("活动已结束");
        }
        long userId = data.getLongValue("userId");
        synchronized (LockUtil.getlock(userId + "")) {
            BigDecimal bmMoney = data.getBigDecimal("bmMoney");
            initPeriods();
            JSONObject result = getRedisData();
            UserDzPeriods userDzPeriods = dzCacheService.getDzInitInfo();
            if ((userDzPeriods.getPeriods() + 1) != periods) {
                throwExp("网络异常，刷新后重试");
            }
            //用户资产记录 锁这里 锁全部

            UserCapital userCapital = userCapitalService.findUserCapitalByUserIdAndCapitalType(userId, UserCapitalTypeEnum.currency_2.getValue());
            // 必须有足够的钱
            if (userCapital.getBalance().compareTo(bmMoney) < 0) {
                throwExp("您的金币不足");
            }
            userCapitalService.subUserBalanceByUserId(userId, bmMoney, UserCapitalTypeEnum.currency_2.getValue(), periods, userCapital);
            dzCacheService.setUserIdBmIntoCache(userId + "");
            dzCacheService.setUsersMoney(bmMoney);
            UserDzRecord record = dzService.selectByUserIdAndPeriods(userId, periods);
            if (null == record) {
                result.put("joinNum", BigDecimal.ZERO);
            } else {
                result.put("joinNum", record.getDzMoney());
            }
            JSONObject pushData = new JSONObject();
            pushData.put("userId", userId);
            pushData.put("capitalType", UserCapitalTypeEnum.currency_2.getValue());
            pushData.put("balance", userCapital.getBalance().subtract(bmMoney));
            Push.push(PushCode.updateUserCapital, managerSocketService.getServerIdByUserId(userId), pushData);
            result.put("isSuccess", "SUCCESS");
            logger.info("执行结束========" + result.toJSONString());
            return result;
        }
    }

    @Transactional
    @ServiceMethod(code = "003", description = "玩家打卡签到本次打坐游戏")
    public JSONObject updateUserDzInfo(ManagerSocketServer adminSocketServer, JSONObject data) {
        checkNull(data);
        if (1 != managerConfigService.getDouble(Config.DZ_GAME_ON)) {
            throwExp("活动已停止");
        }
        long userId = data.getLongValue("userId");
        initPeriods();
        JSONObject result = getRedisData();
        if (dzCacheService.getUserDkInfo(userId)) {
            throwExp("您已点卯成功");
        }
        //只更改记录状态、打卡时间
        UserDzRecord userDzRecord = dzService.selectByUserIdAndPeriods(userId, periods);
        if (null == userDzRecord || 1 != userDzRecord.getStatus()) {
            throwExp("您未报名上期点卯游戏或已点卯！");
        }
        dzService.updateByUserIdAndPeriods(userId, periods);
        dzCacheService.setUserIdDkIntoCache(userId);
        result.put("isSuccess", "SUCCESS");
        logger.info("执行结束========" + result.toJSONString());
        return result;
    }

    @Transactional
    @ServiceMethod(code = "004", description = "玩家领取瓜分灵石")
    public JSONObject addUserMoneyInfo(ManagerSocketServer adminSocketServer, JSONObject data) {
        if (1 != managerConfigService.getDouble(Config.DZ_GAME_ON)) {
            throwExp("活动已结束");
        }
        checkNull(data);
        long userId = data.getLongValue("userId");
        JSONObject result = getRedisData();
        synchronized (LockUtil.getlock(userId + "")) {
            if (!dzCacheService.getUserLqInfo(userId + "")) {
                throwExp("您没有可领取的奖励");
            }
            UserCapital userCapital = userCapitalService.findUserCapitalByUserIdAndCapitalType(userId, UserCapitalTypeEnum.currency_2.getValue());
            //增加用户资产记录
            //根据record记录，给用户增加资产
            JSONObject pushData = new JSONObject();
            UserDzRecord userDzRecord;
            userDzRecord = dzService.selectByUserIdAndStatus(userId, DzStatus.DK_STATUS.getValue(), null);
            //如果用户userID 存在redis中，但是记录又显示领取，这个时候清除掉用户的userId,返回其已经领取过
            if (null == userDzRecord) {
                dzCacheService.removeLqUserId(userId + "");
                throwExp("您已经领取过奖励");
            }
            userDzRecord.setStatus(DzStatus.LQ_STATUS.getValue());
            dzService.updateStatusTo3(userDzRecord);
            //将userId 从缓存中删除
            dzCacheService.removeLqUserId(userId + "");
            userCapitalService.addUserBalanceByUserId(userId, userDzRecord.getCuMoney().add(userDzRecord.getReturnMoney()), UserCapitalTypeEnum.currency_2.getValue(), userDzRecord.getId(), userCapital);
            pushData.put("userId", userId);
            pushData.put("capitalType", UserCapitalTypeEnum.currency_2.getValue());
            pushData.put("balance", userCapital.getBalance().add(userDzRecord.getCuMoney().add(userDzRecord.getReturnMoney())));
            Push.push(PushCode.updateUserCapital, managerSocketService.getServerIdByUserId(userId), pushData);
            result.put("isSuccess", "SUCCESS");
            result.put("rewardNum", userDzRecord.getCuMoney().add(userDzRecord.getReturnMoney()));
        }
        logger.info("执行结束========" + result.toJSONString());
        return result;
    }

//    @ServiceMethod(code = "006", description = "")
//    public JSONObject test(ManagerSocketServer adminSocketServer, JSONObject data) {
//        UserDzPeriods userDzPeriods = dzCacheService.getDzInitInfo();
//        List<UserDzRecord> userDzRecords = dzService.findAllRecord(userDzPeriods);
//        dzPeriodsService.yesTodayTask(userDzRecords);
//        return null;
//    }

    public JSONObject getRedisData() {
        return new JSONObject();
    }
}

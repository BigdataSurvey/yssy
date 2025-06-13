package com.zywl.app.manager.service.manager;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.Activity;
import com.zywl.app.base.bean.CashRecord;
import com.zywl.app.base.bean.User;
import com.zywl.app.base.bean.UserAchievement;
import com.zywl.app.base.constant.RedisKeyConstant;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.base.util.JSONUtil;
import com.zywl.app.base.util.LockUtil;
import com.zywl.app.base.util.OrderUtil;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.cache.GameCacheService;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.cache.impl.RedisService;
import com.zywl.app.defaultx.enmus.GameTypeEnum;
import com.zywl.app.defaultx.service.ActivityService;
import com.zywl.app.defaultx.service.CashRecordService;
import com.zywl.app.defaultx.service.UserService;
import com.zywl.app.manager.context.MessageCodeContext;
import com.zywl.app.manager.socket.ManagerSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.Key;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@ServiceClass(code = MessageCodeContext.Activity)
public class ManagerActivityService  extends BaseService {

    @Autowired
    private ActivityService activityService;
    @Autowired
    private UserService userService;
    @Autowired
    private UserCacheService userCacheService;
    @Autowired
    private RedisTemplate<String,Object> redisTemplate;
    @Autowired
    private RedisService redisService;
    @Autowired
    private CashRecordService cashRecordService;
    @Autowired
    private GameCacheService gameCacheService;


    @Transactional
    @ServiceMethod(code = "001", description = "获取活动信息")
    public JSONObject getAchievementInfo(ManagerSocketServer adminSocketServer, JSONObject data) {
        checkNull(data);
        checkNull(data.get("userId"));
        String userId = data.getString("userId");
        synchronized (LockUtil.getlock(userId)){
            JSONObject result = new JSONObject();
            List<Activity> activityList = activityService.findAllActivity();
            result.put("activityList", activityList);
            return result;
        }
    }


    @Transactional
    @ServiceMethod(code = "003", description = "获取领奖记录")
    public JSONObject getRawrdsRecord(ManagerSocketServer adminSocketServer, JSONObject data) {
        JSONObject result = new JSONObject();
        checkNull(data);
        checkNull(data.get("page"), data.get("num"));
        Long userId = data.getLong("userId");
        User user = userService.findById(userId);
        if(null == user.getAlipayId()){
           result.put("bingState",0);
           return result;
        }
        List<CashRecord> cashRecord= cashRecordService.findCashRecordByUserId(userId, data.getIntValue("page"), data.getIntValue("num"));
        result.put("cashRecord",cashRecord);
        result.put("bingState",1);
        return result;
    }
    @Transactional
    @ServiceMethod(code = "002", description = "获取榜单信息")
    public JSONObject getTopListInfo1(ManagerSocketServer adminSocketServer, JSONObject data) {
        checkNull(data);
        Long userId = data.getLong("userId");
        String key = RedisKeyConstant.APP_TOP_lIST+  DateUtil.format2(new Date());
        String rankKey =RedisKeyConstant.POINT_RANK_LIST+  DateUtil.format2(new Date());
        User user = userCacheService.getUserInfoById(userId);
        if (user == null) {
            throwExp("用户信息异常");
        }
        JSONObject result = new JSONObject();
        result.put("remainingTime", DateUtil.thisWeekRemainingTime());
        result.put("rankList",gameCacheService.getTopList(key,rankKey));
        Double userRankScore = gameCacheService.getUserTopScore(String.valueOf(userId));
        result.put("myScore", userRankScore ==null?0.0:userRankScore);
        Long thisWeekUserRank = gameCacheService.getTopRank(String.valueOf(userId));
        result.put("myRank",thisWeekUserRank==null?-1:thisWeekUserRank+1);
        return result;
    }
    @Transactional
    @ServiceMethod(code = "004", description = "上期榜单")
    public JSONObject getLastTopList(ManagerSocketServer adminSocketServer, JSONObject data) {
        JSONObject result = new JSONObject();
        Long userId = data.getLong("userId");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DATE, -1); // 减去一天
        String yesterday =  DateUtil.format2(calendar.getTime());
        String key = RedisKeyConstant.APP_TOP_lIST+yesterday;
        String rankKey =RedisKeyConstant.POINT_RANK_LIST+ yesterday;
        result.put("rankList",gameCacheService.getLastTopList(key,rankKey));
        Double userRankScore = gameCacheService.getLastUserTopScore(String.valueOf(userId),key);
        result.put("remainingTime", DateUtil.thisWeekRemainingTime());
        result.put("myScore", userRankScore ==null?0.0:userRankScore);
        Long thisWeekUserRank = gameCacheService.getTopRank(String.valueOf(userId));
        result.put("myRank",thisWeekUserRank==null?-1:thisWeekUserRank+1);
        return result;
    }






}

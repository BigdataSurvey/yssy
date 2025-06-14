package com.zywl.app.server.service;

import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.bean.Command;
import com.live.app.ws.enums.TargetSocketType;
import com.live.app.ws.util.CommandBuilder;
import com.live.app.ws.util.Executer;
import com.zywl.app.base.bean.Activity;
import com.zywl.app.base.bean.CashRecord;
import com.zywl.app.base.bean.User;
import com.zywl.app.base.constant.RedisKeyConstant;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.cache.GameCacheService;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.service.ActivityService;
import com.zywl.app.defaultx.service.CashRecordService;
import com.zywl.app.defaultx.service.UserService;
import com.zywl.app.server.context.MessageCodeContext;
import com.zywl.app.server.socket.AppSocket;
import com.zywl.app.server.util.RequestManagerListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
@ServiceClass(code = MessageCodeContext.Activity)
public class ServerActivityService extends BaseService {


    @Autowired
    private UserService userService;
    @Autowired
    private ActivityService activityService;
    @Autowired
    private UserCacheService userCacheService;
    @Autowired
    private GameCacheService gameCacheService;
    @Autowired
    private CashRecordService cashRecordService;



    @ServiceMethod(code = "002", description = "获取榜单信息")
    public Object getTopListInfo(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        long userId = appSocket.getWsidBean().getUserId();
        String date = DateUtil.format2(new Date());
        List<Activity> activityList = activityService.findActivityByTime(date);
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
        result.put("activityList",activityList);
        return result;
    }
    @ServiceMethod(code = "003", description = "获取领奖记录")
    public Object getRawrdsRecord(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        long userId = appSocket.getWsidBean().getUserId();
        checkNull(params.get("page"),params.get("num"));
        JSONObject result = new JSONObject();
        User user = userService.findById(userId);
        if(null == user.getAlipayId()){
            result.put("bingState",0);
            return result;
        }
        List<CashRecord> cashRecord= cashRecordService.findCashRecordByUserId(userId, params.getIntValue("page"), params.getIntValue("num"));
        result.put("cashRecord",cashRecord);
        result.put("bingState",1);
        return result;
    }
    @ServiceMethod(code = "004", description = "上期榜单")
    public Object getLastTopList(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        long userId = appSocket.getWsidBean().getUserId();
        JSONObject result = new JSONObject();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DATE, -1); // 减去一天
        String yesterday =  DateUtil.format2(calendar.getTime());
        String key = RedisKeyConstant.APP_TOP_lIST+"lastActivity";
        String rankKey =RedisKeyConstant.POINT_RANK_LIST+ yesterday;
        result.put("rankList",gameCacheService.getLastTopList(key,rankKey));
        Double userRankScore = gameCacheService.getLastUserTopScore(String.valueOf(userId),key);
        result.put("remainingTime", DateUtil.thisWeekRemainingTime());
        result.put("myScore", userRankScore ==null?0.0:userRankScore);
        Long thisWeekUserRank = gameCacheService.getTopRank(String.valueOf(userId));
        result.put("myRank",thisWeekUserRank==null?-1:thisWeekUserRank+1);
        return result;
    }
    @ServiceMethod(code = "005", description = "绑定支付宝账号")
    public Object bingZfb(final AppSocket appSocket, Command appCommand, JSONObject params) {
        long userId = appSocket.getWsidBean().getUserId();
        params.put("userId",userId);
        String alipayId = params.getString("alipayId");
        checkNull(params);
        checkNull(params.get("alipayId"));
        userService.addAliPayUserId(userId,alipayId);
        return async();
    }




}

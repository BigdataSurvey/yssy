package com.zywl.app.server.service;

import com.alibaba.fastjson2.JSONObject;
import com.alipay.service.schema.util.StringUtil;
import com.live.app.ws.bean.Command;
import com.zywl.app.base.bean.Activity;
import com.zywl.app.base.bean.Activity2;
import com.zywl.app.base.bean.CashRecord;
import com.zywl.app.base.bean.User;
import com.zywl.app.base.constant.RedisKeyConstant;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.cache.GameCacheService;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.service.Activity2Service;
import com.zywl.app.defaultx.service.ActivityService;
import com.zywl.app.defaultx.service.CashRecordService;
import com.zywl.app.defaultx.service.UserService;
import com.zywl.app.server.context.MessageCodeContext;
import com.zywl.app.server.socket.AppSocket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@ServiceClass(code = MessageCodeContext.Activity)
public class ServerActivityService extends BaseService {


    @Autowired
    private UserService userService;
    @Autowired
    private ActivityService activityService;

    @Autowired
    private Activity2Service activity2Service;
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
        Activity activity = activityService.findActivityByTime();
        if (activity == null) {
            throwExp("未查询到活动信息");
        }
        String key = RedisKeyConstant.APP_TOP_lIST + activity.getId();
        User user = userCacheService.getUserInfoById(userId);
        if (user == null) {
            throwExp("用户信息异常");
        }
        JSONObject result = new JSONObject();
        List<JSONObject> topList = gameCacheService.getActiveTopList();
        result.put("rankList", topList);
        Double userRankScore = gameCacheService.getUserTopScore(String.valueOf(userId),activity.getId());
        result.put("myScore", userRankScore == null ? 0.0 : userRankScore);
        Long myRank = gameCacheService.getTopRankByKey(topList, String.valueOf(userId));
        result.put("myRank", myRank == null ? "未上榜" : myRank);

        if (myRank == null || userRankScore == null) {
            result.put("myMoney", BigDecimal.ZERO);
        } else {
            result.put("myMoney", gameCacheService.getRankMoney(userId,userRankScore, myRank + 1,topList));
        }
        if (result.getDoubleValue("myScore")<3){
            result.put("myRank", "未上榜");
            result.put("myMoney", BigDecimal.ZERO);
        }
        result.put("activeInfo", activity);
        return result;
    }

    @ServiceMethod(code = "003", description = "获取领奖记录")
    public Object getRewardRecord(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        long userId = appSocket.getWsidBean().getUserId();
        checkNull(params.get("page"), params.get("num"));
        JSONObject result = new JSONObject();
        User user = userService.findById(userId);
        if (StringUtil.isEmpty(user.getAlipayId())) {
            result.put("bingState", 0);
            return result;
        }
        List<CashRecord> cashRecords = cashRecordService.findCashRecordByUserId(userId, params.getIntValue("page"), params.getIntValue("num"));
        List<JSONObject> list = new ArrayList<>();
        for (CashRecord record : cashRecords) {
            JSONObject obj = new JSONObject();
            obj.put("amount", record.getAmount());
            //0 未到账  2已到账  3 失败
            obj.put("status", record.getStatus());
            obj.put("createTime", record.getCreateTime());
            list.add(obj);
        }
        result.put("cashRecord", list);
        result.put("bingState", 1);
        return result;
    }

    @ServiceMethod(code = "004", description = "上期榜单")
    public Object getLastTopList(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        long userId = appSocket.getWsidBean().getUserId();
        Activity activity = activityService.findActivityByTime();
        if (activity == null) {
            throwExp("未查询到上期榜单信息");
        }
        Activity byId = activityService.findById(activity.getId() - 1);
        if (byId==null){
            throwExp("未查询到上期榜单信息");
        }
        JSONObject result = new JSONObject();
        String key = RedisKeyConstant.APP_TOP_lIST + (activity.getId() - 1);
        List<JSONObject> lastTopList = gameCacheService.getLastTopList();
        result.put("rankList", lastTopList);
        Double userRankScore = gameCacheService.getLastUserTopScore(String.valueOf(userId), key);
        result.put("myScore", userRankScore == null ? 0.0 : userRankScore);
        Long lastRank = gameCacheService.getTopRankByKey(lastTopList, String.valueOf(userId));
        result.put("myRank", lastRank == null ? "未上榜" : lastRank );
        if (lastRank == null || userRankScore == null) {
            result.put("myMoney", BigDecimal.ZERO);
        } else {
            result.put("myMoney", gameCacheService.getRankMoney(userId,userRankScore, lastRank + 1,lastTopList));
        }
        result.put("activeInfo",byId);
        return result;
    }

    @ServiceMethod(code = "005", description = "绑定支付宝账号")
    public Object bingZfb(final AppSocket appSocket, Command appCommand, JSONObject params) {
        long userId = appSocket.getWsidBean().getUserId();
        params.put("userId", userId);
        String alipayId = params.getString("alipayId");
        checkNull(params);
        checkNull(params.get("alipayId"));
        userService.addAliPayUserId(userId, alipayId);
        return new JSONObject();
    }


    @ServiceMethod(code = "006", description = "获取间推榜单信息")
    public Object getTopListInfo2(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        long userId = appSocket.getWsidBean().getUserId();
        Activity activity = activity2Service.findActivity2ByTime();
        if (activity == null) {
            throwExp("未查询到活动信息");
        }
        String key = RedisKeyConstant.APP_TOP_lIST_2 + activity.getId();
        User user = userCacheService.getUserInfoById(userId);
        if (user == null) {
            throwExp("用户信息异常");
        }
        JSONObject result = new JSONObject();
        List<JSONObject> topList = gameCacheService.getActiveTopList2();
        result.put("rankList", topList);
        Double userRankScore = gameCacheService.getUserTopScore2(String.valueOf(userId),activity.getId());
        result.put("myScore", userRankScore == null ? 0.0 : userRankScore);
        Long myRank = gameCacheService.getTopRankByKey(key, String.valueOf(userId));
        result.put("myRank", myRank == null ? "未上榜" : myRank + 1);

        if (myRank == null || userRankScore == null) {
            result.put("myMoney", BigDecimal.ZERO);
        } else {
            result.put("myMoney", gameCacheService.getRankMoney2(userId,userRankScore, myRank + 1,topList));
        }
        if (result.getDoubleValue("myScore")<1){
            result.put("myRank", "未上榜");
            result.put("myMoney", BigDecimal.ZERO);
        }
        result.put("activeInfo", activity);
        return result;
    }

    @ServiceMethod(code = "007", description = "上期间推榜单")
    public Object getLastTopList2(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        long userId = appSocket.getWsidBean().getUserId();
        Activity activity = activity2Service.findActivity2ByTime();
        if (activity == null) {
            throwExp("未查询到上期榜单信息");
        }
        Activity byId = activity2Service.findById(activity.getId() - 1);
        if (byId==null){
            throwExp("未查询到上期榜单信息");
        }
        JSONObject result = new JSONObject();
        String key = RedisKeyConstant.APP_TOP_lIST_2 + (activity.getId() - 1);
        List<JSONObject> lastTopList = gameCacheService.getLastTopList2();
        result.put("rankList", lastTopList);
        Double userRankScore = gameCacheService.getLastUserTopScore(String.valueOf(userId), key);
        result.put("myScore", userRankScore == null ? 0.0 : userRankScore);
        Long lastRank = gameCacheService.getTopRankByKey(key, String.valueOf(userId));
        result.put("myRank", lastRank == null ? "未上榜" : lastRank + 1);
        if (lastRank == null || userRankScore == null) {
            result.put("myMoney", BigDecimal.ZERO);
        } else {
            result.put("myMoney", gameCacheService.getRankMoney2(userId,userRankScore, lastRank + 1,lastTopList));
        }
        result.put("activeInfo",byId);
        return result;
    }



}

package com.zywl.app.server.service;

import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.bean.Command;
import com.live.app.ws.enums.TargetSocketType;
import com.live.app.ws.util.CommandBuilder;
import com.live.app.ws.util.Executer;
import com.zywl.app.base.bean.User;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.Async;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.cache.GameCacheService;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.enmus.GameTypeEnum;
import com.zywl.app.server.context.MessageCodeContext;
import com.zywl.app.server.socket.AppSocket;
import com.zywl.app.server.util.RequestManagerListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@ServiceClass(code = MessageCodeContext.NXQ)
public class ServerNxqGameService extends BaseService {

    @Autowired
    private UserCacheService userCacheService;
    @Autowired
    private GameCacheService gameCacheService;

    @ServiceMethod(code = "001", description = "投入信物")
    public Async investSeal(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        long userId = appSocket.getWsidBean().getUserId();
        params.put("userId", userId);
        User user = userCacheService.getUserInfoById(userId);
        if (user == null) {
            throwExp("用户信息异常");
        }
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("9021001", params).build(),
                new RequestManagerListener(appCommand));
        return async();
    }

    @ServiceMethod(code = "002", description = "宁采臣心动值查询")
    public Async findNccHeart(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        long userId = appSocket.getWsidBean().getUserId();
        params.put("userId", userId);
        User user = userCacheService.getUserInfoById(userId);
        if (user == null) {
            throwExp("用户信息异常");
        }
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("9021002", params).build(),
                new RequestManagerListener(appCommand));
        return async();
    }

    @ServiceMethod(code = "003", description = "领取收益")
    public Async getReceive(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        long userId = appSocket.getWsidBean().getUserId();
        params.put("userId", userId);
        User user = userCacheService.getUserInfoById(userId);
        if (user == null) {
            throwExp("用户信息异常");
        }
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("9021003", params).build(),
                new RequestManagerListener(appCommand));
        return async();
    }
    @ServiceMethod(code = "004", description = "排行榜")
    public JSONObject dgsRankList(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("type"));
        long userId = appSocket.getWsidBean().getUserId();
        params.put("userId", userId);
        User user = userCacheService.getUserInfoById(userId);
        if (user == null) {
            throwExp("用户信息异常");
        }
        JSONObject result = new JSONObject();
        int type= params.getInteger("type");
        if (type==2){
            result.put("rankList",gameCacheService.getNccLastWeekList());
            Double userLastWeekRankScore = gameCacheService.getUserLastWeekRankScore(GameTypeEnum.nxq.getValue(), String.valueOf(userId));
            result.put("myScore",userLastWeekRankScore==null?0.0:userLastWeekRankScore);
            Long rank = gameCacheService.getLastWeekUserRankDgs(String.valueOf(userId));
            result.put("myRank",rank==null?-1:rank+1);
        } else if (type==1) {
            result.put("remainingTime", DateUtil.thisWeekRemainingTime());
            result.put("rankList",gameCacheService.getThisWeekListDgs());
            Double userRankScore = gameCacheService.getUserRankScore(GameTypeEnum.nxq.getValue(), String.valueOf(userId));
            result.put("myScore", userRankScore ==null?0.0:userRankScore);
            Long thisWeekUserRank = gameCacheService.getThisWeekUserRankDgs(String.valueOf(userId));
            result.put("myRank",thisWeekUserRank==null?-1:thisWeekUserRank+1);
        }
        return result;
    }
    @ServiceMethod(code = "005", description = "领取记录")
    public Async findInvestRecord(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        long userId = appSocket.getWsidBean().getUserId();
        params.put("userId", userId);
        User user = userCacheService.getUserInfoById(userId);
        if (user == null) {
            throwExp("用户信息异常");
        }
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("9021006", params).build(),
                new RequestManagerListener(appCommand));
        return async();
    }





}

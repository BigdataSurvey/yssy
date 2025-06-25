package com.zywl.app.server.service;

import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.bean.Command;
import com.live.app.ws.enums.TargetSocketType;
import com.live.app.ws.util.CommandBuilder;
import com.live.app.ws.util.Executer;
import com.zywl.app.base.bean.Config;
import com.zywl.app.base.bean.card.GameEscortRecord;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.Async;
import com.zywl.app.base.util.LockUtil;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.enmus.GameEscortEventTypeEnum;
import com.zywl.app.defaultx.service.card.GameEscortRecordService;
import com.zywl.app.server.context.MessageCodeContext;
import com.zywl.app.server.socket.AppSocket;
import com.zywl.app.server.util.RequestManagerListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.Lock;

@Service
@ServiceClass(code = MessageCodeContext.GAME_ESCORT)
public class ServerGameEscortService extends BaseService {


    @Autowired
    private GameEscortRecordService gameEscortRecordService;

    @Autowired
    private ServerConfigService serverConfigService;

    @Autowired
    private UserCacheService userCacheService;

    /*@Transactional
    @ServiceMethod(code = "getGameInfo", description = "进入游戏界面")
    public JSONObject getMailInfo(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        long userId = appSocket.getWsidBean().getUserId();
        GameEscortRecord byUserId = gameEscortRecordService.findByUserId(userId);
        JSONObject result = new JSONObject();
        result.put("inGame", 0);
        if (byUserId != null && byUserId.getGameStatus() == 1) {
            result.put("inGame", 1);
            result.put("nowNumber", byUserId.getNowNumber());
            result.put("nowCheckpoint", byUserId.getNowCheckpoint());
            result.put("amount", byUserId.getAmount());
        }
        return result;
    }

    @Transactional
    @ServiceMethod(code = "beginGame", description = "开始游戏")
    public Async beginGame(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("type"));
        Long userId = appSocket.getWsidBean().getUserId();
        params.put("userId", userId);
        synchronized (LockUtil.getlock(userId)) {
            if (serverConfigService.getInteger(Config.PJZL_STATUS)==0){
                throwExp("游戏暂未开放，开放时间请留意公告");
            }
            if (userCacheService.canReceiveMail(String.valueOf(userId))) {
                throwExp("点击过快");
            }
            userCacheService.userReceiveMailTime(String.valueOf(userId));
            int type = params.getIntValue("type");
            if (type != 1 && type != 2 && type != 3 && type != 4 && type != 5) {
                throwExp("参数异常");
            }
            Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("9003001", params).build(), new RequestManagerListener(appCommand));
            return async();

        }
    }

    @Transactional
    @ServiceMethod(code = "choice", description = "选择事件")
    public Async check(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("eventType"));
        int type = params.getIntValue("eventType");
        Long userId = appSocket.getWsidBean().getUserId();
        synchronized (LockUtil.getlock(userId)) {
            params.put("userId", userId);
            if (userCacheService.canReceiveMail(String.valueOf(userId))) {
                throwExp("点击过快");
            }
            userCacheService.userReceiveMailTime(String.valueOf(userId));
            if (type != GameEscortEventTypeEnum.EVENT1.getCount() &&
                    type != GameEscortEventTypeEnum.EVENT2.getCount() &&
                    type != GameEscortEventTypeEnum.EVENT3.getCount()) {
                throwExp("参数异常");
            }
            Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("9003002", params).build(), new RequestManagerListener(appCommand));
            return async();
        }
    }

    @ServiceMethod(code = "record", description = "游戏记录")
    public Object record(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        Long userId = appSocket.getWsidBean().getUserId();
        List<GameEscortRecord> recordByUserId = gameEscortRecordService.findRecordByUserId(userId);
        return recordByUserId;
    }

    public static void main(String[] args) {
        int all = 0;
        int all2 = 0;
        for (int k = 0; k < 1000; k++) {
            int begin = 100;
            for (int i = 0; i < 6; i++) {
                Random random = new Random();
                if (random.nextInt(100) + 1 > 50) {
                    begin += begin / 2;
                } else {
                    begin -= begin / 2;
                }
            }
            all += begin;
            all2 += 100;
        }
        System.out.println(all);
        System.out.println(all2);
    }*/
}

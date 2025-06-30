package com.zywl.app.server.service;


import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.bean.Command;
import com.live.app.ws.enums.PushCode;
import com.live.app.ws.enums.TargetSocketType;
import com.live.app.ws.socket.BaseClientSocket;
import com.live.app.ws.socket.manager.SocketManager;
import com.live.app.ws.util.CommandBuilder;
import com.live.app.ws.util.DefaultPushHandler;
import com.live.app.ws.util.Executer;
import com.live.app.ws.util.Push;
import com.zywl.app.base.bean.User;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.Async;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.cache.GameCacheService;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.server.context.MessageCodeContext;
import com.zywl.app.server.socket.AppSocket;
import com.zywl.app.server.util.RequestManagerListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@ServiceClass(code = MessageCodeContext.Dgs)
public class ServerDgsService extends BaseService {

    @Autowired
    private UserCacheService userCacheService;
    @Autowired
    private GameCacheService gameCacheService;
    public static List<BigDecimal> betList = new ArrayList<>();

    @PostConstruct
    public void _Construct() {
        betList.add(new BigDecimal("1"));
        betList.add(new BigDecimal("10"));
        betList.add(new BigDecimal("100"));
    }



    public boolean isOnline(int gameId) {

        Set<BaseClientSocket> clients = SocketManager.getServers(TargetSocketType.getServerEnum(gameId));
        if (clients != null && !clients.isEmpty()) {
            return true;
        }
        return false;
    }

    @ServiceMethod(code = "002", description = "投入")
    public Async bet(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("betAmount"), params.get("bet"));
        int gameId = params.getIntValue("gameId");
        if (!isOnline(gameId)) {
            throwExp("小游戏正在维护");
        }
        long userId = appSocket.getWsidBean().getUserId();
        User user = userCacheService.getUserInfoById(userId);
        if (user == null) {
            throwExp("用户信息异常");
        }
        BigDecimal amount = params.getBigDecimal("betAmount");
        if (gameId != 5) {
            if (!betList.contains(amount)) {
                throwExp("非法请求");
            }
        }
        params.put("userId", userId);
        params.put("headImgUrl",user.getHeadImageUrl());
        params.put("name",user.getName());
        Executer.request(TargetSocketType.getServerEnum(gameId), CommandBuilder.builder().request("111", params).build(),
                new RequestManagerListener(appCommand));
        return async();
    }




}

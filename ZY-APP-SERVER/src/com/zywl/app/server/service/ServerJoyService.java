package com.zywl.app.server.service;

import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.bean.Command;
import com.live.app.ws.enums.TargetSocketType;
import com.live.app.ws.util.CommandBuilder;
import com.live.app.ws.util.Executer;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.server.context.MessageCodeContext;
import com.zywl.app.server.socket.AppSocket;
import com.zywl.app.server.util.RequestManagerListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author: lzx
 * @Create: 2025/12/15
 * @Version: V1.0
 * @Description: 欢乐值（气球树）核心业务服务  USER_JOR "086"
 */
@Service
@ServiceClass(code = MessageCodeContext.USER_JOR)
public class ServerJoyService  extends BaseService {
    @Autowired
    private RequestManagerService requestManagerService;


    /**
     * 查询我的欢乐值与可兑气球数量
     */
    @ServiceMethod(code = "001", description = "查询我的欢乐值与可兑气球数量")
    public Object getFarmInfo(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        long userId = appSocket.getWsidBean().getUserId();
        params.put("userId",userId);
        Command managerCmd = CommandBuilder.builder()
                .request("037001", params)
                .build();
        Executer.request(
                TargetSocketType.manager,
                managerCmd,
                new RequestManagerListener(appCommand)
        );
        return async();
    }

    /**
     * 兑换气球
     */
    @ServiceMethod(code = "002", description = "兑换气球")
    public Object exchangeJoyToBalloon(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        long userId = appSocket.getWsidBean().getUserId();
        params.put("userId",userId);
        Command managerCmd = CommandBuilder.builder()
                .request("037002", params)
                .build();
        Executer.request(
                TargetSocketType.manager,
                managerCmd,
                new RequestManagerListener(appCommand)
        );
        return async();
    }

    /**
     * 查看某个好友对我的欢乐值贡献
     */
    @ServiceMethod(code = "003", description = "查看某个好友对我的欢乐值贡献")
    public Object getFriendJoyContrib(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        long userId = appSocket.getWsidBean().getUserId();
        params.put("userId",userId);
        Command managerCmd = CommandBuilder.builder()
                .request("037003", params)
                .build();
        Executer.request(
                TargetSocketType.manager,
                managerCmd,
                new RequestManagerListener(appCommand)
        );
        return async();
    }


}

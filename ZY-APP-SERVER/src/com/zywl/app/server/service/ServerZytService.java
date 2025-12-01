package com.zywl.app.server.service;

import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.bean.Command;
import com.live.app.ws.enums.TargetSocketType;
import com.live.app.ws.util.CommandBuilder;
import com.live.app.ws.util.Executer;
import com.zywl.app.base.bean.User;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.Async;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.server.context.MessageCodeContext;
import com.zywl.app.server.socket.AppSocket;
import com.zywl.app.server.util.RequestManagerListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ServiceClass(code = MessageCodeContext.ZYT)
public class ServerZytService  extends BaseService {


    @Transactional
    @ServiceMethod(code = "001", description = "激活")
    public Object open(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        params.put("userId", appSocket.getWsidBean().getUserId());
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("9022001", params).build(),
                new RequestManagerListener(appCommand));
        return async();
    }

    @Transactional
    @ServiceMethod(code = "002", description = "抽奖")
    public Object draw(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        params.put("userId", appSocket.getWsidBean().getUserId());
        throwExp("即将开放");
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("9022002", params).build(),
                new RequestManagerListener(appCommand));
        return async();
    }
    @Transactional
    @ServiceMethod(code = "003", description = "进入页面")
    public Object see(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        params.put("userId", appSocket.getWsidBean().getUserId());
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("9022003", params).build(),
                new RequestManagerListener(appCommand));
        return async();
    }

    @Transactional
    @ServiceMethod(code = "004", description = "领取收益")
    public Object receiveIncome(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        params.put("userId", appSocket.getWsidBean().getUserId());
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("9022004", params).build(),
                new RequestManagerListener(appCommand));
        return async();
    }
    @Transactional
    @ServiceMethod(code = "005", description = "爆炸")
    public Object boom(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        params.put("userId", appSocket.getWsidBean().getUserId());
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("9022005", params).build(),
                new RequestManagerListener(appCommand));
        return async();
    }
    @ServiceMethod(code = "006", description = "领取记录")
    public Async findRecord(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        long userId = appSocket.getWsidBean().getUserId();
        params.put("userId", userId);
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("9022006", params).build(),
                new RequestManagerListener(appCommand));
        return async();
    }


}

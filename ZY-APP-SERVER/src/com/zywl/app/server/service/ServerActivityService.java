package com.zywl.app.server.service;

import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.bean.Command;
import com.live.app.ws.enums.TargetSocketType;
import com.live.app.ws.util.CommandBuilder;
import com.live.app.ws.util.Executer;
import com.zywl.app.base.bean.User;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.server.context.MessageCodeContext;
import com.zywl.app.server.socket.AppSocket;
import com.zywl.app.server.util.RequestManagerListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ServiceClass(code = MessageCodeContext.Activity)
public class ServerActivityService extends BaseService {

    @ServiceMethod(code = "001", description = "获取活动信息")
    public Object getAchievementInfo(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        long userId = appSocket.getWsidBean().getUserId();
        params.put("userId", userId);
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("9009001", params).build(), new RequestManagerListener(appCommand));
        return async();
    }

    @ServiceMethod(code = "002", description = "获取榜单信息")
    public Object getTopListInfo(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        long userId = appSocket.getWsidBean().getUserId();
        params.put("userId", userId);
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("9009002", params).build(), new RequestManagerListener(appCommand));
        return async();
    }
    @ServiceMethod(code = "003", description = "获取领奖记录")
    public Object getRawrdsRecord(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        long userId = appSocket.getWsidBean().getUserId();
        params.put("userId",userId);
        checkNull(params.get("page"),params.get("num"));
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("9009003", params).build(), new RequestManagerListener(appCommand));
        return async();
    }
    @ServiceMethod(code = "004", description = "上期榜单")
    public Object getLastTopList(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        long userId = appSocket.getWsidBean().getUserId();
        params.put("userId",userId);
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("9009004", params).build(), new RequestManagerListener(appCommand));
        return async();
    }
    @ServiceMethod(code = "005", description = "获取领奖记录")
    public Object judgeBind(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        long userId = appSocket.getWsidBean().getUserId();
        params.put("userId",userId);
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("9009005", params).build(), new RequestManagerListener(appCommand));
        return async();
    }



}

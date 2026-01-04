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
 * @Create: 2026/1/3
 * @Version: V1.0
 * @Description: 悬赏任务 Server Service
 * @Task: 088 (MessageCodeContext.BOUNTY_TASK)
 */
@Service
@ServiceClass(code = MessageCodeContext.BOUNTY_TASK)
public class ServerBountyService extends BaseService {
    @Autowired
    private RequestManagerService requestManagerService;

    /**
     * 获取全局任务列表
     */
    @ServiceMethod(code = "001", description = "任务列表")
    public Object getFarmInfo(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        long userId = appSocket.getWsidBean().getUserId();
        params.put("userId",userId);
        Command managerCmd = CommandBuilder.builder()
                .request("036001", params)
                .build();
        Executer.request(
                TargetSocketType.manager,
                managerCmd,
                new RequestManagerListener(appCommand)
        );
        return async();
    }
}

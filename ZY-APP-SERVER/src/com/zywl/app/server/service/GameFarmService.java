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
 * @Create: 2025/12/9
 * @Version: V1.0
 * @Description: 用户种地管理 Service
 * @Task: 085 (MessageCodeContext.USER_FARM)
 */

@Service
@ServiceClass(code = MessageCodeContext.USER_FARM)
public class GameFarmService extends BaseService {
    @Autowired
    private RequestManagerService requestManagerService;


    /**
     * 获取农场信息
     */
    @ServiceMethod(code = "001", description = "获取农场信息")
    public Object getInfo(final AppSocket appSocket, Command appCommand, JSONObject params) {
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


    /**
     * 播种
     * {"landIndex": 1, "seedId": 1101}
     */
    @ServiceMethod(code = "002", description = "播种")
    public Object plant(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        long userId = appSocket.getWsidBean().getUserId();
        params.put("userId",userId);
        Command managerCmd = CommandBuilder.builder()
                .request("036002", params)
                .build();
        Executer.request(
                TargetSocketType.manager,
                managerCmd,
                new RequestManagerListener(appCommand)
        );
        return async();
    }

    /**
     * 收割 / 铲除
     * {"landIndex": 1} (如果是 -1 代表一键收割)
     */
    @ServiceMethod(code = "003", description = "收割 / 铲除土地")
    public Object harvest(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        long userId = appSocket.getWsidBean().getUserId();
        params.put("userId",userId);
        Command managerCmd = CommandBuilder.builder()
                .request("036003", params)
                .build();
        Executer.request(
                TargetSocketType.manager,
                managerCmd,
                new RequestManagerListener(appCommand)
        );
        return async();
    }


    /**
     * 购买/解锁土地
     * {"landIndex": 7}
     */
    @ServiceMethod(code = "004", description = "购买/解锁土地")
    public Object unlock(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        long userId = appSocket.getWsidBean().getUserId();
        params.put("userId",userId);
        Command managerCmd = CommandBuilder.builder()
                .request("036004", params)
                .build();
        Executer.request(
                TargetSocketType.manager,
                managerCmd,
                new RequestManagerListener(appCommand)
        );
        return async();
    }
}

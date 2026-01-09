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
import com.zywl.app.base.bean.Config;
import com.zywl.app.base.util.StringUtils;

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
    @Autowired
    private ServerConfigService serverConfigService;

    /**
     * 获取农场信息
     */
    @ServiceMethod(code = "001", description = "获取农场信息")
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

    /**
     * 一阶种子兑换（核心积分）
     * {"seedItemId":1101,"number":1}
     */
    @ServiceMethod(code = "005", description = "一阶种子兑换")
    public Object exchangeSeed(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        long userId = appSocket.getWsidBean().getUserId();
        params.put("userId", userId);
        Command managerCmd = CommandBuilder.builder()
                .request("036005", params)
                .build();
        Executer.request(
                TargetSocketType.manager,
                managerCmd,
                new RequestManagerListener(appCommand)
        );
        return async();
    }

    /**
     * 获取种子兑换配置
     */
    @ServiceMethod(code = "006", description = "获取种子兑换配置")
    public Object getExchangeConfig(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        String cfgStr = serverConfigService.getString(Config.SEED_EXCHANGE_CONFIG);
        if (StringUtils.isBlank(cfgStr)) {
            throwExp("兑换配置缺失：" + Config.SEED_EXCHANGE_CONFIG);
        }
        return JSONObject.parseObject(cfgStr);
    }
}

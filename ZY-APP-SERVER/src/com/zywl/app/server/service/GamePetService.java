package com.zywl.app.server.service;

import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.bean.AppCommand;
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
import org.springframework.stereotype.Service;

/**
 * @Author: lzx
 * @Create: 2025/12/30
 * @Version: V1.0
 * @Description: 养宠管理 Service
 * @Task: 087 (MessageCodeContext.USER_PET)
 */
@Service
@ServiceClass(code = MessageCodeContext.USER_PET)
public class GamePetService  extends BaseService {
    /**
     * 获取养宠信息
     */
    @ServiceMethod(code = "001", description = "获取养宠信息")
    public Object getPetInfo(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        long userId = appSocket.getWsidBean().getUserId();
        params.put("userId",userId);
        Command managerCmd = CommandBuilder.builder()
                .request("038001", params)
                .build();
        Executer.request(
                TargetSocketType.manager,
                managerCmd,
                new RequestManagerListener(appCommand)
        );
        return async();
    }

    /**
     * 养宠-购买狮子
     */
    @ServiceMethod(code = "002", description = "养宠-购买狮子")
    public Object buy(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        long userId = appSocket.getWsidBean().getUserId();
        params.put("userId",userId);
        Command managerCmd = CommandBuilder.builder()
                .request("038002", params)
                .build();
        Executer.request(
                TargetSocketType.manager,
                managerCmd,
                new RequestManagerListener(appCommand)
        );
        return async();
    }

    /**
     * 养宠-喂养
     */
    @ServiceMethod(code = "003", description = "养宠-喂养")
    public Object feed(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        long userId = appSocket.getWsidBean().getUserId();
        params.put("userId",userId);
        Command managerCmd = CommandBuilder.builder()
                .request("038003", params)
                .build();
        Executer.request(
                TargetSocketType.manager,
                managerCmd,
                new RequestManagerListener(appCommand)
        );
        return async();
    }

    /**
     * 养宠-领取产出
     */
    @ServiceMethod(code = "004", description = "养宠-领取产出")
    public Object claim(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        long userId = appSocket.getWsidBean().getUserId();
        params.put("userId",userId);
        Command managerCmd = CommandBuilder.builder()
                .request("038004", params)
                .build();
        Executer.request(
                TargetSocketType.manager,
                managerCmd,
                new RequestManagerListener(appCommand)
        );
        return async();
    }


    /**
     * 养宠-解锁分润等级
     */
    @ServiceMethod(code = "005", description = "养宠-解锁分润等级")
    public Object unlock(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        long userId = appSocket.getWsidBean().getUserId();
        params.put("userId",userId);
        Command managerCmd = CommandBuilder.builder()
                .request("038005", params)
                .build();
        Executer.request(
                TargetSocketType.manager,
                managerCmd,
                new RequestManagerListener(appCommand)
        );
        return async();
    }

    /**
     * 邀请-主页信息（邀请码/邀请人/统计）
     */
    @ServiceMethod(code = "006", description = "养宠-邀请主页")
    public Object inviteHome(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        long userId = appSocket.getWsidBean().getUserId();
        params.put("userId", userId);
        Command managerCmd = CommandBuilder.builder()
                .request("038006", params)
                .build();
        Executer.request(TargetSocketType.manager, managerCmd, new RequestManagerListener(appCommand));
        return async();
    }

    /**
     * 邀请-直推列表（全部/未达标/有效）
     * type: 0全部 / 1未达标 / 2有效
     * page/pageSize: 可选
     */
    @ServiceMethod(code = "007", description = "养宠-邀请列表")
    public Object inviteList(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        long userId = appSocket.getWsidBean().getUserId();
        params.put("userId", userId);
        Command managerCmd = CommandBuilder.builder()
                .request("038007", params)
                .build();
        Executer.request(TargetSocketType.manager, managerCmd, new RequestManagerListener(appCommand));
        return async();
    }

    /**
     * 邀请人信息弹窗（上级信息）
     */
    @ServiceMethod(code = "008", description = "养宠-邀请人信息")
    public Object inviterInfo(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        long userId = appSocket.getWsidBean().getUserId();
        params.put("userId", userId);
        Command managerCmd = CommandBuilder.builder()
                .request("038008", params)
                .build();
        Executer.request(TargetSocketType.manager, managerCmd, new RequestManagerListener(appCommand));
        return async();
    }

    /**
     * 部落-主页信息（部落总人数/部落1~5卡片/解锁进度）
     */
    @ServiceMethod(code = "009", description = "养宠-部落主页")
    public Object tribeHome(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        long userId = appSocket.getWsidBean().getUserId();
        params.put("userId", userId);
        Command managerCmd = CommandBuilder.builder()
                .request("038009", params)
                .build();
        Executer.request(TargetSocketType.manager, managerCmd, new RequestManagerListener(appCommand));
        return async();
    }

}

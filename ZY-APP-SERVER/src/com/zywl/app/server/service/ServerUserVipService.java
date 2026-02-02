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
import org.springframework.stereotype.Service;

/**
 * @Author: lzx
 * @Create: 2026-01-19
 * @Version: V1.0
 * @Description: VIP Service（
 * @Task: 078 (MessageCodeContext.USER_VIP)
 */
@Service
@ServiceClass(code = MessageCodeContext.USER_VIP)
public class ServerUserVipService extends BaseService {

    /**
     * 078001
     * VIP面板信息
     */
    @ServiceMethod(code = "001", description = "VIP面板信息")
    public Object getVipPanelInfo(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        long userId = appSocket.getWsidBean().getUserId();
        params.put("userId", userId);

        Executer.request(
                TargetSocketType.manager,
                CommandBuilder.builder().request("9008001", params).build(),
                new RequestManagerListener(appCommand)
        );
        return async();
    }


    /**
     * 078002
     * 自购开通/续期VIP（VIP1/VIP2）
     */
    @ServiceMethod(code = "002", description = "自购开通/续期VIP")
    public Object buyOrRenewVip(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        long userId = appSocket.getWsidBean().getUserId();
        params.put("userId", userId);

        Executer.request(
                TargetSocketType.manager,
                CommandBuilder.builder().request("9008002", params).build(),
                new RequestManagerListener(appCommand)
        );
        return async();
    }


    /**
     * 078003
     * VIP1每日领取
     */
    @ServiceMethod(code = "003", description = "VIP1每日领取")
    public Object receiveVip1DailyReward(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        long userId = appSocket.getWsidBean().getUserId();
        params.put("userId", userId);

        Executer.request(
                TargetSocketType.manager,
                CommandBuilder.builder().request("9008003", params).build(),
                new RequestManagerListener(appCommand)
        );
        return async();
    }


    /**
     * 078004
     * VIP卡转赠
     */
    @ServiceMethod(code = "004", description = "VIP卡转赠")
    public Object giftVipCard(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        long userId = appSocket.getWsidBean().getUserId();
        params.put("userId", userId);

        Executer.request(
                TargetSocketType.manager,
                CommandBuilder.builder().request("9008004", params).build(),
                new RequestManagerListener(appCommand)
        );
        return async();
    }


    /**
     * 078005
     * VIP卡确认激活
     */
    @ServiceMethod(code = "005", description = "VIP卡确认激活")
    public Object activateVipCard(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        long userId = appSocket.getWsidBean().getUserId();
        params.put("userId", userId);

        Executer.request(
                TargetSocketType.manager,
                CommandBuilder.builder().request("9008005", params).build(),
                new RequestManagerListener(appCommand)
        );
        return async();
    }


    /**
     * 078006
     * VIP1每日领取记录列表
     */
    @ServiceMethod(code = "006", description = "VIP1每日领取记录")
    public Object getVip1ReceiveRecordList(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        long userId = appSocket.getWsidBean().getUserId();
        params.put("userId", userId);

        Executer.request(
                TargetSocketType.manager,
                CommandBuilder.builder().request("9008006", params).build(),
                new RequestManagerListener(appCommand)
        );
        return async();
    }


    /**
     * 078007
     * VIP卡转赠记录列表
     * direction: 0 全部；1 我送出的；2 我收到的
     */
    @ServiceMethod(code = "007", description = "VIP卡转赠记录")
    public Object getVipGiftRecordList(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        long userId = appSocket.getWsidBean().getUserId();
        params.put("userId", userId);

        Executer.request(
                TargetSocketType.manager,
                CommandBuilder.builder().request("9008007", params).build(),
                new RequestManagerListener(appCommand)
        );
        return async();
    }

}

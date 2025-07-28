package com.zywl.app.server.service;

import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.bean.Command;
import com.live.app.ws.bean.PushBean;
import com.live.app.ws.enums.PushCode;
import com.live.app.ws.enums.TargetSocketType;
import com.live.app.ws.util.CommandBuilder;
import com.live.app.ws.util.Executer;
import com.live.app.ws.util.Push;
import com.zywl.app.base.bean.hongbao.RedEnvelope;
import com.zywl.app.base.bean.hongbao.RedPosition;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.service.RecordSheetService;
import com.zywl.app.defaultx.service.RedEnvelopeService;
import com.zywl.app.defaultx.service.RedPositionService;
import com.zywl.app.server.context.MessageCodeContext;
import com.zywl.app.server.socket.AppSocket;
import com.zywl.app.server.util.RequestManagerListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


@Service
@ServiceClass(code = MessageCodeContext.RED_RECORD)
public class ServerRecordService extends BaseService {

    @Autowired
    private RedEnvelopeService redEnvelopeService;

    @Autowired
    private RedPositionService redPositionService;




    @ServiceMethod(code = "001", description = "加入房间")
    public Object joinRoom(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        Push.doAddPush(appSocket, new PushBean(PushCode.pushRed, "hongbaoyu"));
        return new Object();
    }

    @ServiceMethod(code = "002", description = "离开房间")
    public Object romeUser(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        Push.doRemovePush(appSocket, new PushBean(PushCode.pushRed, "hongbaoyu"));
        return new Object();
    }



    @ServiceMethod(code = "004", description = "发红包记录")
    // 查询用户发出的红包记录
    public List<RedEnvelope> queryRedRecord(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        int page = params.getIntValue("page");
        int num = params.getIntValue("num");
        Long userId = appSocket.getWsidBean().getUserId();
        return redEnvelopeService.findQueryRedRecord(userId,page,num);
    }



    @ServiceMethod(code = "005", description = "抢红包记录")
    // 查询用户发出的红包记录
    public List<RedEnvelope> redRecordList(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        int page = params.getIntValue("page");
        int num = params.getIntValue("num");
        Long userId = appSocket.getWsidBean().getUserId();
        return redEnvelopeService.findQueryRedPacket(userId,page,num);
    }

    @ServiceMethod(code = "008", description = "领取红包记录")
    // 查询用户发出的红包记录
    public List<RedEnvelope> LuckyMoneyRcord(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("redId"));
        Long userId = appSocket.getWsidBean().getUserId();
        params.put("userId", userId);
        return redEnvelopeService.findQueryLuckyMoneyRcord(userId);
    }


    @ServiceMethod(code = "003", description = "发包")
    // 查询用户发出的红包记录
    public Object redEnvelopes(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("redId"));
        checkNull(params.get("totalNumber"));
        checkNull(params.get("amount"));
        checkNull(appSocket.getWsidBean().getUserId());
        Long userId = appSocket.getWsidBean().getUserId();
        params.put("userId", userId);
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("9020001", params).build(),
                new RequestManagerListener(appCommand));
        return async();
    }

    @ServiceMethod(code = "006", description = "抢包")
    // 查询用户发出的红包记录
    public Object bagSnatchingRed(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("redId"));
        Long userId = appSocket.getWsidBean().getUserId();
        params.put("userId", userId);
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("9020002", params).build(),
                new RequestManagerListener(appCommand));
        return async();
    }


    @ServiceMethod(code = "007 ", description = "发红包次数")
    // 查询用户发出的红包记录
    public Object redPacketCount(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("redId"));
        Long userId = appSocket.getWsidBean().getUserId();
        return redPositionService.findByUserId(userId);
    }







}

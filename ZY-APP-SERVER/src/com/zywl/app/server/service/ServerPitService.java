package com.zywl.app.server.service;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.bean.Command;
import com.live.app.ws.enums.PushCode;
import com.live.app.ws.enums.TargetSocketType;
import com.live.app.ws.util.CommandBuilder;
import com.live.app.ws.util.Executer;
import com.live.app.ws.util.Push;
import com.zywl.app.base.bean.PitUserParent;
import com.zywl.app.base.bean.User;
import com.zywl.app.base.bean.UserPit;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.service.PitService;
import com.zywl.app.defaultx.service.PitUserParentService;
import com.zywl.app.defaultx.service.UserPitService;
import com.zywl.app.server.context.MessageCodeContext;
import com.zywl.app.server.socket.AppSocket;
import com.zywl.app.server.util.RequestManagerListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.*;

@Service
@ServiceClass(code = MessageCodeContext.PIT)
public class ServerPitService extends BaseService {

    private static final Log logger = LogFactory.getLog(ServerPitService.class);


    @PostConstruct
    public void _ServerMineService() {
        init();
    }

    public void init() {
    }

    @Transactional
    @ServiceMethod(code = "001", description = "开通")
    public Object open(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        params.put("userId", appSocket.getWsidBean().getUserId());
        params.put("pitId", params.getString("pitId"));
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("9010001", params).build(),
                new RequestManagerListener(appCommand));
        return async();
    }


    @Transactional
    @ServiceMethod(code = "002", description = "进入矿洞页面")
    public Object bindingParent(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        params.put("userId", appSocket.getWsidBean().getUserId());
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("9010002", params).build(),
                new RequestManagerListener(appCommand));
        return async();
    }

    @Transactional
    @ServiceMethod(code = "003", description = "领取收益")
    public Object receiveNumber(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        params.put("userId", appSocket.getWsidBean().getUserId());
        params.put("pitId", params.getString("pitId"));
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("9010003", params).build(),
                new RequestManagerListener(appCommand));
        return async();
    }

    @Transactional
    @ServiceMethod(code = "004", description = "绑定")
    public Object binding(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        params.put("userId", appSocket.getWsidBean().getUserId());
        params.put("parentId", params.getString("parentId"));
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("9010004", params).build(),
                new RequestManagerListener(appCommand));
        return async();
    }

    @Transactional
    @ServiceMethod(code = "005", description = "退款")
    public Object refund(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        params.put("userId", appSocket.getWsidBean().getUserId());
        throwExp("不支持取消开通");
        return new JSONObject();
    }
    @ServiceMethod(code = "006", description = "领取记录")
    public Object receiveRecord(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        params.put("userId", appSocket.getWsidBean().getUserId());
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("9010006", params).build(),
                new RequestManagerListener(appCommand));
        return async();
    }
    @ServiceMethod(code = "007", description = "直接矿工")
    public Object getSuborList(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        params.put("userId", appSocket.getWsidBean().getUserId());
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("9010007", params).build(),
                new RequestManagerListener(appCommand));
        return async();
    }

    @ServiceMethod(code = "008", description = "直接矿工")
    public Object getIndirSuborList(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        params.put("userId", appSocket.getWsidBean().getUserId());
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("9010008", params).build(),
                new RequestManagerListener(appCommand));
        return async();
    }
    @ServiceMethod(code = "009", description = "搜索上级")
    public Object selectParent(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        params.put("userId", appSocket.getWsidBean().getUserId());
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("9010009", params).build(),
                new RequestManagerListener(appCommand));
        return async();
    }
}

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
 * @Create: 2026/1/3
 * @Version: V1.0
 * @Description: OSS 直传/资源服务
 * @Task: 089 (MessageCodeContext.OSS_SERVER)
 */
@Service
@ServiceClass(code = MessageCodeContext.OSS_SERVER)
public class ServerOssService extends BaseService {

    /**
     * OSS-获取直传签名
     * 089001
     */
    @ServiceMethod(code = "001", description = "OSS-直传签名")
    public Object getOssDirectUploadPolicy(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        long userId = appSocket.getWsidBean().getUserId();
        params.put("userId", userId);

        Command managerCmd = CommandBuilder.builder().request("040001", params).build();
        Executer.request(TargetSocketType.manager, managerCmd, new RequestManagerListener(appCommand));
        return async();
    }

    /**
     * OSS-规范化并校验资源 URL
     * 089002
     */
    @ServiceMethod(code = "002", description = "OSS-规范化资源URL")
    public Object canonicalizeOssUrl(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        long userId = appSocket.getWsidBean().getUserId();
        params.put("userId", userId);

        Command managerCmd = CommandBuilder.builder().request("040002", params).build();
        Executer.request(TargetSocketType.manager, managerCmd, new RequestManagerListener(appCommand));
        return async();
    }

    /**
     * OSS-获取可展示URL
     * 089003
     */
    @ServiceMethod(code = "003", description = "OSS-获取可展示URL")
    public Object getReadableUrl(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        long userId = appSocket.getWsidBean().getUserId();
        params.put("userId", userId);

        Command managerCmd = CommandBuilder.builder().request("040003", params).build();
        Executer.request(TargetSocketType.manager, managerCmd, new RequestManagerListener(appCommand));
        return async();
    }
}

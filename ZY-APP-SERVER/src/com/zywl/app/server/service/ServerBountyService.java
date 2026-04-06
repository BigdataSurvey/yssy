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
 * @Description: 悬赏任务 Manager Service
 * @Task: 088 (MessageCodeContext.BOUNTY_TASK)
 */

@Service
@ServiceClass(code = MessageCodeContext.BOUNTY_TASK)
public class ServerBountyService extends BaseService {

    @ServiceMethod(code = "001", description = "悬赏任务-大厅列表")
    public Object listTasks(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        long userId = appSocket.getWsidBean().getUserId();
        params.put("userId", userId);

        Command managerCmd = CommandBuilder.builder().request("039001", params).build();
        Executer.request(TargetSocketType.manager, managerCmd, new RequestManagerListener(appCommand));
        return async();
    }

    @ServiceMethod(code = "002", description = "悬赏任务-任务详情")
    public Object getTaskDetail(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("taskId"));
        long userId = appSocket.getWsidBean().getUserId();
        params.put("userId", userId);

        Command managerCmd = CommandBuilder.builder().request("039002", params).build();
        Executer.request(TargetSocketType.manager, managerCmd, new RequestManagerListener(appCommand));
        return async();
    }

    @ServiceMethod(code = "003", description = "悬赏任务-发布")
    public Object publishTask(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("taskName"), params.get("taskTitle"), params.get("taskDesc"),
                params.get("unitPrice"), params.get("quotaTotal"), params.get("takeLimitHours"), params.get("downloadImgs"));
        long userId = appSocket.getWsidBean().getUserId();
        params.put("userId", userId);

        Command managerCmd = CommandBuilder.builder().request("039003", params).build();
        Executer.request(TargetSocketType.manager, managerCmd, new RequestManagerListener(appCommand));
        return async();
    }

    @ServiceMethod(code = "004", description = "悬赏任务-取消任务")
    public Object cancelTask(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("taskId"));
        long userId = appSocket.getWsidBean().getUserId();
        params.put("userId", userId);

        Command managerCmd = CommandBuilder.builder().request("039004", params).build();
        Executer.request(TargetSocketType.manager, managerCmd, new RequestManagerListener(appCommand));
        return async();
    }

    @ServiceMethod(code = "005", description = "悬赏任务-接单")
    public Object takeTask(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("taskId"));
        long userId = appSocket.getWsidBean().getUserId();
        params.put("userId", userId);

        Command managerCmd = CommandBuilder.builder().request("039005", params).build();
        Executer.request(TargetSocketType.manager, managerCmd, new RequestManagerListener(appCommand));
        return async();
    }

    @ServiceMethod(code = "006", description = "悬赏任务-取消接单")
    public Object cancelOrder(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("taskId"));
        long userId = appSocket.getWsidBean().getUserId();
        params.put("userId", userId);

        Command managerCmd = CommandBuilder.builder().request("039006", params).build();
        Executer.request(TargetSocketType.manager, managerCmd, new RequestManagerListener(appCommand));
        return async();
    }

    @ServiceMethod(code = "007", description = "悬赏任务-提交材料")
    public Object submitOrder(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("taskId"), params.get("submitUserId"), params.get("submitImgs"));
        long userId = appSocket.getWsidBean().getUserId();
        params.put("userId", userId);

        Command managerCmd = CommandBuilder.builder().request("039007", params).build();
        Executer.request(TargetSocketType.manager, managerCmd, new RequestManagerListener(appCommand));
        return async();
    }

    @ServiceMethod(code = "008", description = "悬赏任务-重新提交")
    public Object resubmitOrder(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("taskId"), params.get("submitUserId"), params.get("submitImgs"));
        long userId = appSocket.getWsidBean().getUserId();
        params.put("userId", userId);

        Command managerCmd = CommandBuilder.builder().request("039008", params).build();
        Executer.request(TargetSocketType.manager, managerCmd, new RequestManagerListener(appCommand));
        return async();
    }

    @ServiceMethod(code = "009", description = "悬赏任务-申诉")
    public Object appealOrder(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("taskId"), params.get("appealReason"), params.get("appealImgs"));
        long userId = appSocket.getWsidBean().getUserId();
        params.put("userId", userId);

        Command managerCmd = CommandBuilder.builder().request("039009", params).build();
        Executer.request(TargetSocketType.manager, managerCmd, new RequestManagerListener(appCommand));
        return async();
    }

    @ServiceMethod(code = "010", description = "悬赏任务-我的接单列表")
    public Object myOrders(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        long userId = appSocket.getWsidBean().getUserId();
        params.put("userId", userId);

        Command managerCmd = CommandBuilder.builder().request("039010", params).build();
        Executer.request(TargetSocketType.manager, managerCmd, new RequestManagerListener(appCommand));
        return async();
    }

    @ServiceMethod(code = "011", description = "悬赏任务-我的发布列表")
    public Object myPublish(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        long userId = appSocket.getWsidBean().getUserId();
        params.put("userId", userId);

        Command managerCmd = CommandBuilder.builder().request("039011", params).build();
        Executer.request(TargetSocketType.manager, managerCmd, new RequestManagerListener(appCommand));
        return async();
    }

    @ServiceMethod(code = "012", description = "悬赏任务-我发布的待审核列表")
    public Object pendingAudit(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        long userId = appSocket.getWsidBean().getUserId();
        params.put("userId", userId);

        Command managerCmd = CommandBuilder.builder().request("039012", params).build();
        Executer.request(TargetSocketType.manager, managerCmd, new RequestManagerListener(appCommand));
        return async();
    }

    @ServiceMethod(code = "013", description = "悬赏任务-审核通过")
    public Object auditApprove(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("orderId"));
        long userId = appSocket.getWsidBean().getUserId();
        params.put("userId", userId);

        Command managerCmd = CommandBuilder.builder().request("039013", params).build();
        Executer.request(TargetSocketType.manager, managerCmd, new RequestManagerListener(appCommand));
        return async();
    }

    @ServiceMethod(code = "014", description = "悬赏任务-审核驳回")
    public Object auditReject(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("orderId"), params.get("rejectReason"));
        long userId = appSocket.getWsidBean().getUserId();
        params.put("userId", userId);

        Command managerCmd = CommandBuilder.builder().request("039014", params).build();
        Executer.request(TargetSocketType.manager, managerCmd, new RequestManagerListener(appCommand));
        return async();
    }

    @ServiceMethod(code = "015", description = "悬赏任务-OSS直传签名")
    public Object getOssDirectUploadPolicy(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        long userId = appSocket.getWsidBean().getUserId();
        params.put("userId", userId);

        Command managerCmd = CommandBuilder.builder().request("039015", params).build();
        Executer.request(TargetSocketType.manager, managerCmd, new RequestManagerListener(appCommand));
        return async();
    }
}

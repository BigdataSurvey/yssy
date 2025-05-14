package com.zywl.app.server.service;


import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.bean.Command;
import com.live.app.ws.enums.TargetSocketType;
import com.live.app.ws.util.CommandBuilder;
import com.live.app.ws.util.Executer;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.Async;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.server.context.MessageCodeContext;
import com.zywl.app.server.socket.AppSocket;
import com.zywl.app.server.util.RequestManagerListener;
import org.springframework.stereotype.Service;

@Service
@ServiceClass(code = MessageCodeContext.WX_PAY_SERVER)
public class ServerPayService extends BaseService {




    @ServiceMethod(code = "001", description = "发起微信支付")
    public Async wxPay(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("productId"));
        int productId = params.getIntValue("productId");
        if (productId<1 || productId>4){
            throwExp("非法请求");
        }
        params.put("userId", appSocket.getWsidBean().getUserId());
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("030001", params).build(),
                new RequestManagerListener(appCommand));
        return async();
    }

    @ServiceMethod(code = "002", description = "发起IOS支付")
    public Async IOSPay(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("receiptData"),params.get("transactionId"),params.get("aa"));
        params.put("userId", appSocket.getWsidBean().getUserId());
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("031001", params).build(),
                new RequestManagerListener(appCommand));
        return async();
    }

    @ServiceMethod(code = "003", description = "获取支付订单以及通道")
    public Async getPayOrder(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("productId"));
        params.put("userId", appSocket.getWsidBean().getUserId());
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("034001", params).build(),
                new RequestManagerListener(appCommand));
        return async();
    }

}

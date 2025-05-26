package com.zywl.app.server.service;


import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.bean.Command;
import com.live.app.ws.enums.TargetSocketType;
import com.live.app.ws.util.CommandBuilder;
import com.live.app.ws.util.Executer;
import com.zywl.app.base.bean.Config;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.server.context.MessageCodeContext;
import com.zywl.app.server.socket.AppSocket;
import com.zywl.app.server.util.RequestManagerListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@ServiceClass(code = MessageCodeContext.BUY_GIFT)
public class ServerBuyGiftService extends BaseService {

    @Autowired
    private ServerConfigService serverConfigService;


    @ServiceMethod(code = "001", description = "打开购买礼包界面，获取礼包信息")
    public Object getGiftInfo(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        BigDecimal price1 = serverConfigService.getBigDecimal(Config.GIFT_PRICE_1);
        BigDecimal price2 = serverConfigService.getBigDecimal(Config.GIFT_PRICE_2);
        JSONObject result = new JSONObject();
        result.put("gift1",price1);
        result.put("gift2",price2);
        return result;
    }

    @ServiceMethod(code = "002", description = "购买礼包")
    public Object buy(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("giftId"));
        //todo 验证giftId合理性
        Long userId = appSocket.getWsidBean().getUserId();
        //用户Id 插入到参数中 传到manager服务器   code 035011
        params.put("userId", userId);
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("035011", params).build(), new RequestManagerListener(appCommand));
        return async();
    }



}

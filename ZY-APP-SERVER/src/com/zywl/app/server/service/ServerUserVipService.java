package com.zywl.app.server.service;

import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.bean.Command;
import com.zywl.app.base.bean.Config;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.server.context.MessageCodeContext;
import com.zywl.app.server.socket.AppSocket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@ServiceClass(code = MessageCodeContext.USER_VIP)
public class ServerUserVipService extends BaseService {

    @Autowired
    private ServerConfigService serverConfigService;

    @ServiceMethod(code = "001", description = "获取vip信息")
    public Object getVipInfo(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        BigDecimal price1 = serverConfigService.getBigDecimal(Config.GIFT_PRICE_1);
        BigDecimal price2 = serverConfigService.getBigDecimal(Config.GIFT_PRICE_2);
        JSONObject result = new JSONObject();
        result.put("gift1",price1);
        result.put("gift2",price2);
        return result;
    }

}

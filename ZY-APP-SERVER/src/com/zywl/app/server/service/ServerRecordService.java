package com.zywl.app.server.service;

import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.bean.Command;
import com.live.app.ws.bean.PushBean;
import com.live.app.ws.enums.PushCode;
import com.live.app.ws.enums.TargetSocketType;
import com.live.app.ws.util.CommandBuilder;
import com.live.app.ws.util.Executer;
import com.live.app.ws.util.Push;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.server.context.MessageCodeContext;
import com.zywl.app.server.socket.AppSocket;
import com.zywl.app.server.util.RequestManagerListener;
import org.springframework.stereotype.Service;


@Service
@ServiceClass(code = MessageCodeContext.RED_RECORD)
public class ServerRecordService extends BaseService {




    @ServiceMethod(code = "001", description = "加入房间")
    public Object getGuilds(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        Long userId = appSocket.getWsidBean().getUserId();
        params.put("userId",userId  );
        Push.doAddPush(appSocket, new PushBean(PushCode.pushRed, "hongbaoyu"));
        return new Object();
    }
}

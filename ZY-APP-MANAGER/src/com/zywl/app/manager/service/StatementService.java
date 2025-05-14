package com.zywl.app.manager.service;


import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.bean.Command;
import com.live.app.ws.enums.TargetSocketType;
import com.live.app.ws.util.CommandBuilder;
import com.live.app.ws.util.Executer;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.manager.context.MessageCodeContext;
import com.zywl.app.manager.socket.AdminSocketServer;
import com.zywl.app.manager.util.RequestManagerListener;
import org.springframework.stereotype.Service;

@Service
@ServiceClass(code = MessageCodeContext.STATEMENT_SERVER)
public class StatementService extends BaseService {


    @ServiceMethod(code = "001", description = "获取平台报表")
    public Object login(AdminSocketServer adminSocketServer, Command webCommand, JSONObject params){
        String startDate = params.getString("startDate");
        String endDate = params.getString("endDate");
        Executer.request(TargetSocketType.logServer, CommandBuilder.builder().request("109002", params).build(),
                new RequestManagerListener(webCommand));
        return async();
    }

    @ServiceMethod(code = "002", description = "获取玩家报表")
    public Object getPlayerStatement(AdminSocketServer adminSocketServer, Command webCommand, JSONObject params){
        String startDate = params.getString("startDate");
        String endDate = params.getString("endDate");
        Executer.request(TargetSocketType.logServer, CommandBuilder.builder().request("109003", params).build(),
                new RequestManagerListener(webCommand));
        return async();
    }
}

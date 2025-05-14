package com.zywl.app.manager.service;

import com.zywl.app.base.service.BaseService;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.manager.context.MessageCodeContext;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
@ServiceClass(code = MessageCodeContext.ADMIN_CASH_SERVER)
public class AdminCashService extends BaseService {
//    @Autowired
//    private CashRecordService cashRecordService;
//    @Autowired
//    private RechargeOrderService rechargeOrderService;

    @PostConstruct
    public void init() {

    }
/*
    @ServiceMethod(code = "001", description = "获取提现数据列表")
    public Object getCashData(AdminSocketServer adminSocketServer, Command webCommand, JSONObject params) {
        int page = params.getIntValue("page",0);
        int limit = params.getIntValue("limit",10);
        int status = params.getIntValue("status", -1);

        Integer start = (page - 1) * limit;
        Integer end = page * limit;
        JSONObject condition = new JSONObject();
        condition.put("start",start);
        condition.put("limit",end);
        if(status >= 0) {
            condition.put("status",status);
        }
        Long count = cashRecordService.count("countByConditions",condition);
        List<CashRecord> recrods = cashRecordService.findByConditions(condition);

        JSONObject data = new JSONObject();
        data.put("list",recrods);
        data.put("count",count);
        return data;
    }

    @ServiceMethod(code = "002", description = "修改提现数据")
    public Object modifyData(AdminSocketServer adminSocketServer, Command webCommand, JSONObject params) {
        int id = params.getIntValue("id",-1);
        int action = params.getIntValue("action",-1);
        if(id < 0 || action < 0) {
            throwExp("参数错误!");
        }

        int status = action == 1 ? 1 : 4;
        String mark = "后台审核";
        cashRecordService.updateStatus(id,status,mark);

        return new Object();
    }

    @ServiceMethod(code = "003", description = "获取充值数据")
    public Object getOrderList(AdminSocketServer adminSocketServer, Command webCommand, JSONObject params) {
        int page = params.getIntValue("page",0);
        int limit = params.getIntValue("limit",10);
        int status = params.getIntValue("status", -1);

        Integer start = (page - 1) * limit;
        Integer end = page * limit;
        JSONObject condition = new JSONObject();
        condition.put("start",start);
        condition.put("limit",end);
        if(status >= 0) {
            condition.put("status",status);
        }
        Long count = rechargeOrderService.count("countByConditions",condition);
        List<RechargeOrder> recrods = rechargeOrderService.findByConditions(condition);

        JSONObject data = new JSONObject();
        data.put("list",recrods);
        data.put("count",count);
        return data;
    }
 */
}

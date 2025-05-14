package com.zywl.app.server.service;

import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.bean.Command;
import com.live.app.ws.enums.TargetSocketType;
import com.live.app.ws.interfacex.Listener;
import com.live.app.ws.socket.BaseClientSocket;
import com.live.app.ws.util.CommandBuilder;
import com.live.app.ws.util.Executer;
import com.zywl.app.base.bean.CashRecord;
import com.zywl.app.base.bean.Config;
import com.zywl.app.base.bean.User;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.Async;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.enmus.UserCapitalTypeEnum;
import com.zywl.app.defaultx.enmus.UserGroupEnum;
import com.zywl.app.defaultx.service.CashRecordService;
import com.zywl.app.server.context.MessageCodeContext;
import com.zywl.app.server.socket.AppSocket;
import com.zywl.app.server.util.RequestManagerListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@ServiceClass(code = MessageCodeContext.CAPITAL_SERVER)
public class ServerCapitalService extends BaseService {


    @Autowired
    private UserCacheService userCacheService;

    @Autowired
    private RequestManagerService requestManagerService;




    public static List<BigDecimal> canCash = new ArrayList<>();

    public static List<BigDecimal> canC3ToRmb = new ArrayList<>();

    @Autowired
    private CashRecordService cashRecordService;

    @Autowired
    private ServerConfigService serverConfigService;

    public static final Object object = new Object();

    @PostConstruct
    public void _ManagerCapitalService() {
        canCash.add(new BigDecimal("0.3"));
        canCash.add(new BigDecimal("20"));
        canCash.add(new BigDecimal("50"));
        canCash.add(new BigDecimal("100"));
        canCash.add(new BigDecimal("200"));
        canCash.add(new BigDecimal("500"));

        canC3ToRmb.add(new BigDecimal("10"));
        canC3ToRmb.add(new BigDecimal("100"));
        canC3ToRmb.add(new BigDecimal("1000"));
        canC3ToRmb.add(new BigDecimal("6000"));


    }


    @ServiceMethod(code = "cash", description = "提交提现申请")
    public synchronized Async cash(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("amount"));
        long userId = appSocket.getWsidBean().getUserId();
        synchronized (object) {
            BigDecimal amount = params.getBigDecimal("amount");
            User user = userCacheService.getUserInfoById(userId);
            long cashOrderCount = cashRecordService.findCashOrderCount(userId);
            if (!canCash.contains(amount)) {
                throwExp("金额不正确");
            }

            if (user == null) {
                throwExp("查询用户信息失败，请稍后重试");
            }
            if (user.getRealName() == null) {
                throwExp("未填写真实姓名不能提现");
            }
            if (user.getAuthentication() != null && user.getAuthentication() == 1 && amount.compareTo(new BigDecimal("0.3")) == 0 && user.getIsCash() == 1) {
                throwExp("仅首次提现可提现0.3元");
            }
            if (user.getGroup() != UserGroupEnum.NORMAL_USER.getValue()) {
                throwExp("禁止提现");
            }
            /*if (type == CashTypeEnum.ZFB.getValue() && user.getPhone() == null) {
                throwExp("未绑定手机号不能提现到支付宝");
            }*/
            if (cashOrderCount > 0) {
                throwExp("今天已经提现过了，请明天再来");
            }
            if (user.getRiskPlus()!=null &&  user.getRiskPlus()==1){
                throwExp("请求异常");
            }
            JSONObject data = new JSONObject();
            data.put("userId", userId);
            data.put("userNo", user.getUserNo());
            data.put("userName", user.getName());
            data.put("realName", user.getRealName());
            data.put("tel", user.getPhone());
            data.put("amount", amount);
            data.put("openId", user.getOpenId());
            requestManagerService.requestManagerUserCash(data, new Listener() {
                public void handle(BaseClientSocket clientSocket, Command command) {
                    if (command.isSuccess()) {
                        JSONObject result = JSONObject.from(command.getData());
                        Executer.response(CommandBuilder.builder(appCommand).success(result).build());
                    } else {
                        Executer.response(CommandBuilder.builder(appCommand)
                                .error(command.getMessage(), command.getData()).build());
                    }
                }
            });
            return async();
        }
    }

    @ServiceMethod(code = "002", description = "余额兑换金币")
    public Async assetConversion(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("amount"),params.get("type"));
        BigDecimal amount = params.getBigDecimal("amount");
        Long userId = appSocket.getWsidBean().getUserId();
        params.put("userId",userId);
        if (amount.toString().contains(".") || amount.toString().length()>9){
            throwExp("非法请求");
        }
        int type = params.getIntValue("type");
        if (type!=1 && type!=2){
            throwExp("非法请求");
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throwExp("非法请求");
        }
        synchronized (object) {
            User user = userCacheService.getUserInfoById(userId);
            if (user == null) {
                throwExp("查询用户信息失败，请稍后重试");
            }
            if (user.getAuthentication() == 0) {
                throwExp("未实名认证不能兑换");
            }
            if (user.getGroup() != UserGroupEnum.NORMAL_USER.getValue()) {
                throwExp("禁止兑换");
            }
            if (user.getRiskPlus()!=null && user.getRiskPlus() == 1) {
                throwExp("未知错误");
            }
            requestManagerService.requestManagerAssetConversion(params, new Listener() {
                public void handle(BaseClientSocket clientSocket, Command command) {
                    if (command.isSuccess()) {
                        JSONObject result = JSONObject.from(command.getData());
                        Executer.response(CommandBuilder.builder(appCommand).success(result).build());
                    } else {
                        Executer.response(CommandBuilder.builder(appCommand)
                                .error(command.getMessage(), command.getData()).build());
                    }
                }
            });
            return async();
        }
    }

    @ServiceMethod(code = "003", description = "获取个人资产信息")
    public Object getIncomeInfo(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        long userId = appSocket.getWsidBean().getUserId();
        params.put("userId", userId);
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("200500", params).build(), new RequestManagerListener(appCommand));
        return async();
    }



    @ServiceMethod(code = "006", description = "查询资产日志")
    public Object getCapitalLog(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("capitalType"),params.get("type"));
        long userId = appSocket.getWsidBean().getUserId();
        User user = userCacheService.getUserInfoById(userId);
        if (user == null) {
            throwExp("查询角色信息异常");
        }
        params.put("userId", userId);
        Executer.request(TargetSocketType.logServer, CommandBuilder.builder().request("109001", params).build(),
                new RequestManagerListener(appCommand));
        return async();
    }


    @ServiceMethod(code = "cashOrder", description = "查询提现订单")
    public JSONObject getCashOrder(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("page"), params.get("num"));
        long userId = appSocket.getWsidBean().getUserId();
        List<CashRecord> vos = cashRecordService.findCashRecordByUserId(userId, params.getInteger("page"), params.getInteger("num"));
        List<JSONObject> list = new ArrayList<JSONObject>();
        for (CashRecord record : vos) {
            JSONObject obj = new JSONObject();
            obj.put("orderNo", record.getOrderNo());
            obj.put("type", record.getType());
            obj.put("amount", record.getAmount());
            //0 未推送 1已推送  2提现成功  3 提现失败
            obj.put("status", record.getStatus());
            obj.put("remark", record.getRemark());
            obj.put("createTime", record.getCreateTime());
            list.add(obj);
        }
        JSONObject result = new JSONObject();
        result.put("cashList", list);
        return result;
    }

    @ServiceMethod(code = "008", description = "获取收益信息")
    public Object getIncomeStatementInfo(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        long userId = appSocket.getWsidBean().getUserId();
        params.put("userId", userId);
        Executer.request(TargetSocketType.logServer, CommandBuilder.builder().request("119001", params).build(),
                new RequestManagerListener(appCommand));
        return async();
    }


    @ServiceMethod(code = "009", description = "余额兑换金币")
    public Async conversion(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("amount"),params.get("sourceType"),params.get("targetType"));
        BigDecimal amount = params.getBigDecimal("amount");
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throwExp("非法请求");
        }
        synchronized (object) {
            long userId = appSocket.getWsidBean().getUserId();
            User user = userCacheService.getUserInfoById(userId);
            if (user == null) {
                throwExp("查询用户信息失败，请稍后重试");
            }
            if (user.getAuthentication() == 0) {
                throwExp("未实名认证不能兑换");
            }
            if (user.getGroup() != UserGroupEnum.NORMAL_USER.getValue()) {
                throwExp("禁止兑换");
            }
            if (user.getRiskPlus()!=null && user.getRiskPlus() == 1) {
                throwExp("未知错误");
            }
            JSONObject data = new JSONObject();
            data.put("userId", userId);
            data.put("amount", params.get("amount"));
            requestManagerService.requestManagerAssetConversion2(data, new Listener() {
                public void handle(BaseClientSocket clientSocket, Command command) {
                    if (command.isSuccess()) {
                        JSONObject result = JSONObject.from(command.getData());
                        Executer.response(CommandBuilder.builder(appCommand).success(result).build());
                    } else {
                        Executer.response(CommandBuilder.builder(appCommand)
                                .error(command.getMessage(), command.getData()).build());
                    }
                }
            });
            return async();
        }
    }



    @ServiceMethod(code = "010", description = "兑换汇率")
    public Object rate(final AppSocket appSocket, Command appCommand, JSONObject params) {
        JSONObject result = new JSONObject();
        result.put("rate",serverConfigService.getBigDecimal(Config.CONVERT_RATE));
        return result;
    }
}





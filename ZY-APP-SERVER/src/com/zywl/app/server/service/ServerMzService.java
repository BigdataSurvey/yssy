package com.zywl.app.server.service;

import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.bean.Command;
import com.live.app.ws.enums.TargetSocketType;
import com.live.app.ws.util.CommandBuilder;
import com.live.app.ws.util.Executer;
import com.zywl.app.base.bean.DicMzItem;
import com.zywl.app.base.bean.MzBuyRecord;
import com.zywl.app.base.bean.MzTrad;
import com.zywl.app.base.bean.MzUserItem;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.service.DicMzItemService;
import com.zywl.app.defaultx.service.MzBuyRecordService;
import com.zywl.app.defaultx.service.MzTradService;
import com.zywl.app.defaultx.service.MzUserItemService;
import com.zywl.app.server.context.MessageCodeContext;
import com.zywl.app.server.socket.AppSocket;
import com.zywl.app.server.util.RequestManagerListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Service
@ServiceClass(code = MessageCodeContext.MZ)
public class ServerMzService extends BaseService {

    @Autowired
    private MzTradService mzTradService;

    @Autowired
    private MzUserItemService mzUserItemService;

    @Autowired
    private DicMzItemService dicMzItemService;

    @Autowired
    private MzBuyRecordService mzBuyRecordService;


    @ServiceMethod(code = "001", description = "获取商店信息")
    public Object addBook(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("5001001", params).build(), new RequestManagerListener(appCommand));
        return async();
    }


    @Transactional
    @ServiceMethod(code = "002", description = "商店购买慢涨道具")
    public Object buy(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        Long userId = appSocket.getWsidBean().getUserId();
        params.put("userId", userId);
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("5001002", params).build(), new RequestManagerListener(appCommand));
        return async();
    }

    @Transactional
    @ServiceMethod(code = "003", description = "升级慢涨道具")
    public Object up(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("id"));
        Long userId = appSocket.getWsidBean().getUserId();
        params.put("userId", userId);
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("5001003", params).build(), new RequestManagerListener(appCommand));
        return async();
    }

    @Transactional
    @ServiceMethod(code = "004", description = "领取升级完成的道具")
    public Object receive(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("id"));
        Long userId = appSocket.getWsidBean().getUserId();
        params.put("userId", userId);
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("5001004", params).build(), new RequestManagerListener(appCommand));
        return async();
    }

    @Transactional
    @ServiceMethod(code = "005", description = "上架慢涨道具")
    public Object sell(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("id"));
        Long userId = appSocket.getWsidBean().getUserId();
        params.put("userId", userId);
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("5001005", params).build(), new RequestManagerListener(appCommand));
        return async();
    }

    @Transactional
    @ServiceMethod(code = "006", description = "获取交易行信息")
    public Object find(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        Long userId = appSocket.getWsidBean().getUserId();
        String type = params.getString("type");
        Integer lv = params.getInteger("lv");
        int page = params.getIntValue("page");
        int num = params.getIntValue("num");
        List<MzTrad> allTrad = new ArrayList<>();
        if (type == null && lv == null) {
            allTrad = mzTradService.findAllTrad(page, num,userId);
        }
        if (lv == null && type != null) {
            allTrad = mzTradService.findByItemType(type, page, num,userId);
        }
        if (lv != null && type != null) {
            allTrad = mzTradService.findByTypeAndLv(type, lv, page, num,userId);
        }
        return allTrad;
    }

    @Transactional
    @ServiceMethod(code = "007", description = "交易行购买慢涨道具")
    public Object tradBuy(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("id"));
        Long userId = appSocket.getWsidBean().getUserId();
        params.put("userId", userId);
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("5001007", params).build(), new RequestManagerListener(appCommand));
        return async();
    }

    @Transactional
    @ServiceMethod(code = "008", description = "交易行下架道具")
    public Object cancelTrad(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("id"));
        Long userId = appSocket.getWsidBean().getUserId();
        params.put("userId", userId);
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("5001008", params).build(), new RequestManagerListener(appCommand));
        return async();
    }

    @Transactional
    @ServiceMethod(code = "009", description = "获取我的慢涨道具信息")
    public Object getMyInfo(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        Long userId = appSocket.getWsidBean().getUserId();
        List<MzUserItem> byUserId = mzUserItemService.findByUserId(userId);
        return byUserId;
    }

    @Transactional
    @ServiceMethod(code = "010", description = "交易行可以选择的道具")
    public Object tradCanCheck(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        List<DicMzItem> allMzItem = dicMzItemService.findAllMzItem();
        Set<String> check = new HashSet<>();
        allMzItem.forEach(e -> check.add(e.getType()));
        return check;
    }

    @Transactional
    @ServiceMethod(code = "011", description = "我的寄售")
    public Object mySell(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        Long userId = appSocket.getWsidBean().getUserId();
        List<MzTrad> mySell = mzTradService.findMySell(userId);
        return mySell;
    }


    @Transactional
    @ServiceMethod(code = "012", description = "我的交易记录")
    public Object tradRecord(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        Long userId = appSocket.getWsidBean().getUserId();
        List<MzBuyRecord> mzBuyRecords = mzBuyRecordService.finUserTradRecord(userId);
        return mzBuyRecords;
    }
}

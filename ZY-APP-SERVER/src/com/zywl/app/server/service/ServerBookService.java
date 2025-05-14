package com.zywl.app.server.service;

import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.bean.Command;
import com.live.app.ws.enums.TargetSocketType;
import com.live.app.ws.util.CommandBuilder;
import com.live.app.ws.util.Executer;
import com.zywl.app.base.bean.UserBook;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.service.UserBookService;
import com.zywl.app.server.context.MessageCodeContext;
import com.zywl.app.server.socket.AppSocket;
import com.zywl.app.server.util.RequestManagerListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;


@Service
@ServiceClass(code = MessageCodeContext.BOOK_SERVER)
public class ServerBookService extends BaseService {


    @Autowired
    private UserBookService userBookService;

    @Autowired
    private GameBaseService gameBaseService;


    @ServiceMethod(code = "001", description = "放入藏书阁")
    public Object addBook(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("itemId"),params.get("number"));
        Long userId = appSocket.getWsidBean().getUserId();
        int number = params.getIntValue("number");
        if (number<1 || number>9999){
            throwExp("参数异常");
        }
        params.put("userId", userId);
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("700100", params).build(), new RequestManagerListener(appCommand));
        return async();
    }
    @ServiceMethod(code = "002", description = "进入藏书阁")
    public Object findBook(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        Long userId = appSocket.getWsidBean().getUserId();
        List<UserBook> byUserId = userBookService.findByUserId(userId);
        JSONObject result = new JSONObject();
        result.put("bookList",byUserId);
        BigDecimal canReceive = BigDecimal.ZERO;
        BigDecimal todayMoney = BigDecimal.ZERO;
        Boolean b = false;
        for (UserBook userBook : byUserId) {
            if (userBook.getSettleTime().getTime()< DateUtil.getToDayBegin()){
                //今日还没计算收益 需要计算收益
                if (userBook.getTodayNumber()>0 && userBook.getAddTime().getTime()<DateUtil.getToDayBegin()){
                    b = true;
                    BigDecimal bookReward = GameBaseService.itemMap.get(userBook.getItemId().toString()).getBookReward().multiply(BigDecimal.valueOf(userBook.getNumber()));
                    userBook.setCanReceive(bookReward);
                    canReceive = canReceive.add(bookReward);
                    //昨天添加过书籍
                    userBook.setNumber(userBook.getTodayNumber()+userBook.getNumber());
                    userBook.setTodayNumber(0);
                    userBook.setSettleTime(new Date());
                }else if(userBook.getSettleTime().getTime()<DateUtil.getToDayBegin()){
                    b = true;
                    BigDecimal bookReward = GameBaseService.itemMap.get(userBook.getItemId().toString()).getBookReward().multiply(BigDecimal.valueOf(userBook.getNumber()));
                    userBook.setCanReceive(bookReward);
                    canReceive = canReceive.add(userBook.getCanReceive());
                    userBook.setSettleTime(new Date());
                }
            }else{
                canReceive =canReceive.add( userBook.getCanReceive());
            }
            BigDecimal oneBookReward = GameBaseService.itemMap.get(userBook.getItemId().toString()).getBookReward().multiply(BigDecimal.valueOf(userBook.getNumber()));
            todayMoney = todayMoney.add(oneBookReward);
        }
        if (b){
            userBookService.batchUpdate(byUserId);
        }
        result.put("canReceive",canReceive);
        result.put("tomorrowMoney",todayMoney);
        return result;
    }

    @ServiceMethod(code = "003", description = "取出藏书")
    public Object getBook(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("itemId"));
        Long userId = appSocket.getWsidBean().getUserId();
        params.put("userId", userId);
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("700300", params).build(), new RequestManagerListener(appCommand));
        return async();
    }

    @ServiceMethod(code = "004", description = "领取收益")
    public Object receiveReward(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        Long userId = appSocket.getWsidBean().getUserId();
        params.put("userId", userId);
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("700400", params).build(), new RequestManagerListener(appCommand));
        return async();
    }


}

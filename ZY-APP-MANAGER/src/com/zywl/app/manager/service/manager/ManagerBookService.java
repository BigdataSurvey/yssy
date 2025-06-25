package com.zywl.app.manager.service.manager;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.Config;
import com.zywl.app.base.bean.Item;
import com.zywl.app.base.bean.User;
import com.zywl.app.base.bean.UserBook;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.base.util.LockUtil;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.enmus.ItemIdEnum;
import com.zywl.app.defaultx.enmus.LogCapitalTypeEnum;
import com.zywl.app.defaultx.enmus.LogUserBackpackTypeEnum;
import com.zywl.app.defaultx.service.UserBookService;
import com.zywl.app.manager.context.MessageCodeContext;
import com.zywl.app.manager.service.PlayGameService;
import com.zywl.app.manager.socket.ManagerSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;


@Service
@ServiceClass(code = MessageCodeContext.BOOK_SERVER)
public class ManagerBookService extends BaseService {


    @Autowired
    private ManagerGameBaseService managerGameBaseService;

    @Autowired
    private UserBookService userBookService;

    @Autowired
    private PlayGameService gameService;

    @Transactional
    @ServiceMethod(code = "100", description = "放入藏书阁")
    public JSONObject addBook(ManagerSocketServer adminSocketServer, JSONObject data) {
        checkNull(data);
        checkNull(data.get("userId"), data.get("itemId"), data.get("number"));
        long userId = data.getLongValue("userId");
        synchronized (LockUtil.getlock(userId)){
            long itemId = data.getLongValue("itemId");
            int number = data.getIntValue("number");
            gameService.checkUserItemNumber(userId, String.valueOf(itemId),number);
            UserBook userBook = userBookService.findByUserIdAndItemId(userId, itemId);
            if (null==userBook){
                userBookService.addUserBook(userId,itemId,number);
            }else {
                userBook.setUnlockTime(DateUtil.getDateByDay(1));
                userBook.setTodayNumber(userBook.getTodayNumber()+number);
                userBook.setAddTime(new Date());
                userBook.setAllNumber(userBook.getAllNumber()+number);
                userBookService.updateUserBook(userBook);
            }
            gameService.updateUserBackpack(userId, String.valueOf(itemId),-number, LogUserBackpackTypeEnum.use);
            return null;
        }

    }

    @Transactional
    @ServiceMethod(code = "300", description = "取出藏书")
    public Object getBook(ManagerSocketServer adminSocketServer, JSONObject data) {
        checkNull(data);
        checkNull(data.get("userId"), data.get("itemId"));
        long userId = data.getLongValue("userId");
        synchronized (LockUtil.getlock(userId)){
            long itemId = data.getLongValue("itemId");
            UserBook byUserIdAndItemId = userBookService.findByUserIdAndItemId(userId, itemId);
            if (byUserIdAndItemId.getUnlockTime().getTime()>System.currentTimeMillis()){
                throwExp("未到取回时间");
            }
            if (byUserIdAndItemId.getAllNumber()==0){
                userBookService.deleteBook(userId,itemId);
            }
            gameService.updateUserBackpack(userId, String.valueOf(itemId),byUserIdAndItemId.getNumber(), LogUserBackpackTypeEnum.use);
            JSONObject result = new JSONObject();
            result.put("type",1);
            result.put("id",itemId);
            result.put("number",byUserIdAndItemId.getNumber());
            JSONArray array = new JSONArray();
            array.add(result);
            return array;
        }
    }
    @Transactional
    @ServiceMethod(code = "400", description = "领取收益")
    public Object receiveReward(ManagerSocketServer adminSocketServer, JSONObject data) {
        checkNull(data);
        checkNull(data.get("userId"));
        long userId = data.getLongValue("userId");
        synchronized (LockUtil.getlock(userId)){
            List<UserBook> byUserId = userBookService.findByUserId(userId);
            JSONObject reward = new JSONObject();
            BigDecimal number = BigDecimal.ZERO;
            for (UserBook userBook : byUserId) {
                number=number.add(userBook.getCanReceive());
                userBook.setCanReceive(BigDecimal.ZERO);
                userBook.setUnlockTime(DateUtil.getTimeByDay(1));
            }
            if (number.compareTo(BigDecimal.ZERO)==0){
                throwExp("当前没有可以领取的收益");
            }
            userBookService.batchUpdate(byUserId);
            reward.put("type",1);
            reward.put("id", 2);
            reward.put("number",number);
            JSONArray array = new JSONArray();
            array.add(reward);
            gameService.addReward(userId,array, LogCapitalTypeEnum.book);
            return array;
        }
    }

}

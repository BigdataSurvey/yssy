package com.zywl.app.manager.service.manager;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.Backpack;
import com.zywl.app.base.bean.User;
import com.zywl.app.base.bean.UserCapital;
import com.zywl.app.base.bean.card.DicMine;
import com.zywl.app.base.bean.card.UserMine;
import com.zywl.app.base.bean.vo.card.UserMineVo;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.BeanUtils;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.base.util.LockUtil;
import com.zywl.app.base.util.OrderUtil;
import com.zywl.app.defaultx.annotation.KafkaProducer;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.cache.UserCapitalCacheService;
import com.zywl.app.defaultx.enmus.LogUserBackpackTypeEnum;
import com.zywl.app.defaultx.enmus.UserCapitalTypeEnum;
import com.zywl.app.defaultx.service.UserCapitalService;
import com.zywl.app.defaultx.service.card.UserMineService;
import com.zywl.app.defaultx.service.card.UserOpenMineRecordService;
import com.zywl.app.manager.context.KafkaEventContext;
import com.zywl.app.manager.context.KafkaTopicContext;
import com.zywl.app.manager.context.MessageCodeContext;
import com.zywl.app.manager.service.PlayGameService;
import com.zywl.app.manager.socket.ManagerSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


@Service
@ServiceClass(code = MessageCodeContext.MINE)
public class ManagerMineService extends BaseService {


    @Autowired
    private UserMineService userMineService;
    @Autowired
    private UserCacheService userCacheService;
    @Autowired
    private UserCapitalCacheService userCapitalCacheService;

    @Autowired
    private UserCapitalService userCapitalService;

    @Autowired
    private ManagerGameBaseService managerGameBaseService;

    @Autowired
    private ManagerUserService managerUserService;

    @Autowired
    private PlayGameService gameService;

    @Autowired
    private UserOpenMineRecordService userOpenMineRecordService;

    @Transactional
    @ServiceMethod(code = "001", description = "开通矿场")
    @KafkaProducer(topic = KafkaTopicContext.RED_POINT,event = KafkaEventContext.OPEN_MINE,sendParams = true)
    public Object openMine(ManagerSocketServer adminSocketServer, JSONObject params) {
        checkNull(params);
        checkNull(params.get("userId"), params.get("index"));
        Long userId = params.getLong("userId");
        synchronized (LockUtil.getlock(userId)) {
            int index = params.getIntValue("index");
            UserMine mine = userMineService.findByUserIdAndIndex(userId, index);
            if (mine != null && mine.getStatus() == 1) {
                throwExp("您已解锁，无需重复解锁");
            }
            DicMine dicMine = PlayGameService.DIC_MINE.get(String.valueOf(index));
            JSONArray costItem = dicMine.getCostItem();
            BigDecimal needMoney = dicMine.getCostMoney();
            UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.currency_2.getValue());
            if (userCapital.getBalance().compareTo(needMoney) < 0) {
                throwExp(UserCapitalTypeEnum.currency_2.getName() + "不足");
            }
            Map<String, Backpack> userBackpack = gameService.getUserBackpack(userId);
            for (Object o : costItem) {
                JSONObject item = (JSONObject) o;
                String id = item.getString("id");
                int number = item.getIntValue("number");
                if (!userBackpack.containsKey(id) || userBackpack.get(id).getItemNumber() < number) {
                    throwExp(PlayGameService.itemMap.get(id).getName() + "数量不足");
                }
            }
            //开通矿产 增加开通记录 更改资产 道具
            String orderNo = OrderUtil.getOrder5Number();
            Long dataId = userOpenMineRecordService.addRecord(userId, orderNo, needMoney);
            userCapitalService.subUserBalanceByOpenMine(userId, needMoney, orderNo, dataId);
            for (Object o : costItem) {
                JSONObject item = (JSONObject) o;
                String id = item.getString("id");
                int number = item.getIntValue("number");
                gameService.updateUserBackpack(userId, id, -number, LogUserBackpackTypeEnum.open_mine);
            }
            UserMine userMine = userMineService.addUserMine(userId, dicMine.getId(), dicMine.getCount(), index, dicMine.getReward().getJSONObject(0).getIntValue("number"));
            managerGameBaseService.pushCapitalUpdate(userId, UserCapitalTypeEnum.currency_2.getValue());
            User user = userCacheService.getUserInfoById(userId);
            UserMineVo vo = new UserMineVo();
            BeanUtils.copy(userMine, vo);
            int hour = 24;
            vo.setHour(hour);
            vo.setUseItem(Long.parseLong(PlayGameService.DIC_MINE.get(userMine.getMineId().toString()).getMiningItem()));
            int useNumber = 10;
            vo.setUseNumber(useNumber);
            BigDecimal anima = dicMine.getCostMoney();
            if (user.getVip2ExpireTime().getTime() < System.currentTimeMillis()) {
                anima = anima.divide(new BigDecimal("2"));
            }
            managerUserService.addAnimaToInviter(userId,anima,new BigDecimal("0.1"));
            return vo;
        }
    }

    @Transactional
    @ServiceMethod(code = "002", description = "探索书境")
    public Object mining(ManagerSocketServer adminSocketServer, JSONObject params) {
        checkNull(params);
        checkNull(params.get("index"), params.get("userId"));
        int index = params.getIntValue("index");
        DicMine dicMine = PlayGameService.DIC_MINE.get(String.valueOf(index));
        String itemId = dicMine.getMiningItem();
        Long userId = params.getLong("userId");
        synchronized (LockUtil.getlock(userId)) {
            User user = userCacheService.getUserInfoById(userId);
            UserMine userMine = userMineService.findByUserIdAndIndex(userId, index);
            if (userMine == null) {
                throwExp("您未解锁该书境");
            }
            if (userMine.getCount()==0){
                throwExp("当前书境探索次数已用完，请重新解锁");
            }
            if (userMine.getIsMining()==1){
                throwExp("当前正在探索，无需重复探索");
            }
            String useItemId = PlayGameService.DIC_MINE.get(userMine.getMineId().toString()).getMiningItem();
            int number = 10;
            gameService.checkUserItemNumber(userId,useItemId,10);
            gameService.updateUserBackpack(userId,useItemId,-number,LogUserBackpackTypeEnum.use);
            userMine.setIsMining(1);
            userMine.setLastMineTime(new Date());
            userMine.setLastOutputTime(new Date());
            userMine.setMinEndTime(DateUtil.getDateByHour(new Date(), 24));
            userMine.setCount(userMine.getCount()-1);
            userMineService.updateUserMine(userMine);
            UserMineVo userMineVo = new UserMineVo();
            BeanUtils.copy(userMine, userMineVo);
            userMineVo.setMinEndTime(userMine.getMinEndTime().getTime());
            userMineVo.setLastMineTime(userMine.getLastMineTime().getTime());
            return userMineVo;
        }
    }

    @Transactional
    @ServiceMethod(code = "003", description = "采集矿场")
    public Object gather(ManagerSocketServer adminSocketServer, JSONObject params) {
        checkNull(params);
        checkNull(params.get("index"), params.get("userId"));
        int index = params.getIntValue("index");
        Long userId = params.getLong("userId");
        synchronized (LockUtil.getlock(userId)) {
            UserMine userMine = userMineService.findByUserIdAndIndex(userId, index);
            if (userMine == null) {
                throwExp("您未激活该矿场");
            }
            if (userMine.getOutput() == 0) {
                throwExp("当前没有可采集的物品");
            }
            //收集道具
            JSONArray array = new JSONArray();
            JSONObject object = new JSONObject();
            object.put("type",1);
            object.put("id",9);
            object.put("number",userMine.getOutput());
            array.add(object);
            gameService.addReward(userId, array, null);
            userMine.setOutput(0);
            userMineService.updateUserMine(userMine);
            return array;
        }
    }


    @Transactional
    @ServiceMethod(code = "004", description = "一键采集矿场")
    public Object gatherAll(ManagerSocketServer adminSocketServer, JSONObject params) {
        checkNull(params);
        checkNull(params.get("userId"));
        Long userId = params.getLong("userId");
        synchronized (LockUtil.getlock(userId)) {
            List<UserMine> userMineByUserId = userMineService.findUserMineByUserId(userId);
            if (userMineByUserId == null || userMineByUserId.size() == 0) {
                throwExp("没有激活的矿场");
            }
            int allOutput = 0;
            List<UserMine> needUpdate = new ArrayList<>();
            for (UserMine userMine : userMineByUserId) {
                if (userMine.getOutput() > 0) {
                    allOutput+=userMine.getOutput();
                    userMine.setAllOutput(userMine.getAllOutput()+userMine.getOutput());
                    userMine.setOutput(0);
                    needUpdate.add(userMine);
                }
            }
            userMineService.batchUpdateUserMine(needUpdate);
            if (allOutput == 0) {
                throwExp("当前没有可领取的收益");
            }
            JSONArray array = new JSONArray();
            JSONObject object = new JSONObject();
            object.put("type",1);
            object.put("id",9);
            object.put("number",allOutput);
            array.add(object);
            gameService.addReward(userId, array,null);
            //收集道具
            return array;
        }
    }
}

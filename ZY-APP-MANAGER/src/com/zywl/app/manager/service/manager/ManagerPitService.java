package com.zywl.app.manager.service.manager;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.bean.Command;
import com.zywl.app.base.bean.*;
import com.zywl.app.base.bean.card.DicMine;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.base.util.LockUtil;
import com.zywl.app.base.util.OrderUtil;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.cache.GameCacheService;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.enmus.LogCapitalTypeEnum;
import com.zywl.app.defaultx.enmus.LogUserBackpackTypeEnum;
import com.zywl.app.defaultx.enmus.UserCapitalTypeEnum;
import com.zywl.app.defaultx.service.*;
import com.zywl.app.manager.context.MessageCodeContext;
import com.zywl.app.manager.service.PlayGameService;
import com.zywl.app.manager.socket.ManagerSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@ServiceClass(code = MessageCodeContext.USER_PIT)
public class ManagerPitService extends BaseService {


    @Autowired
    private PitService pitService;

    @Autowired
    private UserPitService userPitService;

    @Autowired
    private UserCacheService userCacheService;

    @Autowired
    private UserCapitalService userCapitalService;

    @Autowired
    private PitUserParentService pitUserParentService;

    @Autowired
    private ManagerGameBaseService managerGameBaseService;

    @Autowired
    private PlayGameService gameService;
    @Autowired
    private PitRecordService pitRecordService;

    @Autowired
    private GameCacheService gameCacheService;


    @PostConstruct
    public void _ServerMineService() {
        init();
    }

    public void init() {
    }

    @Transactional
    @ServiceMethod(code = "001", description = "开通")
    public Object open(ManagerSocketServer adminSocketServer, JSONObject params) {
        checkNull(params);
        Long userId = params.getLong("userId");
        synchronized (LockUtil.getlock(userId)) {
            JSONObject jsonObject = new JSONObject();
            String pitId = params.getString("pitId");
            //初始化上次领取时间 上次查看时间 未领取
            jsonObject.put("userId", userId);
            jsonObject.put("pitId", pitId);
            jsonObject.put("id", PlayGameService.DIC_PIT.get(pitId).getRewardItem());
            //扣取通宝 开通金矿洞需要“1000通宝”，开通银矿洞需要“500通宝”
            UserPit userPits = userPitService.findInitInfo(jsonObject);
            if (userPits != null) {
                throwExp("已经开通过此矿洞");
            }
            managerGameBaseService.checkBalance(userId, PlayGameService.DIC_PIT.get(pitId).getPrice(), UserCapitalTypeEnum.currency_2);
            userCapitalService.subUserBalanceByOpenPit(userId, PlayGameService.DIC_PIT.get(pitId).getPrice());
            managerGameBaseService.pushCapitalUpdate(userId, UserCapitalTypeEnum.currency_2.getValue());
            int i = userPitService.insertUserPit(jsonObject);
            addActiveScore(userId, pitId);
            return i;
        }
    }

    public void addActiveScore(Long userId, String pitId) {
        Activity activity = gameCacheService.getActivity();
        if (activity == null) {
            return;
        }
        double score = 5.0;
        if (pitId.equals("1")) {
            score = 10.0;
        }
        if (activity.getAddPointEvent() == 4) {
            User user = userCacheService.getUserInfoById(userId);
            PitUserParent pitUserParent = pitUserParentService.findParentByUserId(userId);
            //  gameCacheService.addPointMySelf(userId,getScore(dicMine));
            if (pitUserParent.getPitParentId() != null) {
                gameCacheService.addPointMySelf(pitUserParent.getPitParentId(), score);
            }
        }
    }


    @Transactional
    @ServiceMethod(code = "002", description = "进入矿洞页面")
    public Object bindingParent(ManagerSocketServer adminSocketServer, JSONObject params) {
        checkNull(params);
        Long userId = params.getLong("userId");
        synchronized (LockUtil.getlock(userId)) {

            User user = userCacheService.getUserInfoById(userId);
            PitUserParent parentId = pitUserParentService.findParentByUserId(userId);
            JSONObject jsonObject1 = new JSONObject();
            if (parentId == null) {
                jsonObject1.put("bingingStatus", 0);
                return jsonObject1;
            }
            if (user == null) {
                throwExp("用户信息异常");
            }
            //查询当前用户是否有数据，是否已开通，是否已查看
            List<UserPit> userPitList = userPitService.findPitList(params);
            JSONObject jsonObject = new JSONObject();
                /*
                  1.是否开通
                  2.收益几天未领取
                  3.上次查看时
                 */
            ArrayList<UserPit> newUserPitList = new ArrayList<>();
            jsonObject.put("openStatus", 1);
            for (UserPit userPit : userPitList) {
                long targetDate = System.currentTimeMillis();
                if ((System.currentTimeMillis() - userPit.getEndTime().getTime()) / 1000 / 84600 >= 0) {
                    targetDate = userPit.getEndTime().getTime();
                }
                //根据上一次计算时间去计算未领取收益,相差天数
                //获取上次领取时间去计算收益递增数量值，加到多少了
                long diffLastReceiveTime = (targetDate - userPit.getLastReceiveTime().getTime()) / 1000 / 86400;
                long diffLastLookTime = (targetDate - userPit.getLastLookTime().getTime()) / 1000 / 86400;
                userPit.setDays((int) diffLastReceiveTime);
                int start = PlayGameService.DIC_PIT.get(userPit.getPitId().toString()).getMinCount();      // 起始值
                int increment = 1;   // 每次递增的值
                int steps = (int) diffLastReceiveTime;// 递增次数
                Map<String, Integer> numberMap = new HashMap<>();
                for (int i = 0; i < steps; i++) {
                    int currentValue = start + (i * increment);
                    Integer a = i + 1;
                    numberMap.put(a + "", currentValue);
                }
                //获取未领取收益
                if (userPit.getUnReceive() != null) {
                    for (Object o : userPit.getUnReceive()) {
                        JSONObject reward = (JSONObject) o;
                        Integer number = reward.getIntValue("number");
                        long needCount = diffLastReceiveTime - diffLastLookTime;
                        Integer b = Math.toIntExact(needCount + 1);
                        Integer startNumber = numberMap.get(b + ""); //继续新增的起始值
                        Integer sumNumber = 0;    // 累加总和
                        for (int i = 0; i < diffLastLookTime; i++) {
                            sumNumber += startNumber + i; // 依次累加 95, 96, 97, 98, 99
                        }
                        //赋值未领取收益
                        reward.put("number", number + sumNumber);
                    }
                }
                userPit.setLastLookTime(DateUtil.getDateByDay(userPit.getLastLookTime(), (int) diffLastLookTime));

                newUserPitList.add(userPit);
                if ((System.currentTimeMillis() - userPit.getOpenTime().getTime()) / 1000 / 84600 < 3) {
                    userPit.setIstk(1);
                } else {
                    userPit.setIstk(0);
                }
            }

            jsonObject.put("userPit", userPitList);
            if (newUserPitList.size() > 0) {
                userPitService.batchUpdateNumber(newUserPitList);
            }
            return jsonObject;
        }
    }


    @Transactional
    @ServiceMethod(code = "003", description = "领取收益")
    public Object receiveNumber(ManagerSocketServer adminSocketServer, JSONObject params) {
        checkNull(params);
        Long userId = params.getLong("userId");
        synchronized (LockUtil.getlock(userId)) {
            JSONObject jsonObject = new JSONObject();
            //初始化上次领取时间 上次查看时间 未领取
            jsonObject.put("userId", userId);
            jsonObject.put("pitId", params.getString("pitId"));
            UserPit userPit = userPitService.findInitInfo(params);
            List<PitRecord> pitRecordList = new ArrayList<>();
            PitRecord pitRecord = null;
            String orderNo = OrderUtil.getOrder5Number();
            pitRecord = new PitRecord();
            pitRecord.setUserId(Math.toIntExact(userId));
            pitRecord.setCrteTime(new Date());
            pitRecord.setOrderNo(orderNo);
            pitRecord.setReceiveTime(new Date());
            pitRecord.setPitId(params.getIntValue("pitId"));
            JSONArray reward = new JSONArray();
            for (Object o : userPit.getUnReceive()) {
                JSONObject info = new JSONObject();
                JSONObject newUserPit = (JSONObject) o;
                info.put("type", newUserPit.getIntValue("type"));
                info.put("id", newUserPit.getIntValue("id"));
                info.put("number", newUserPit.getIntValue("number"));
                reward.add(info);
                String id = newUserPit.getString("id");
                int number = newUserPit.getIntValue("number");
                pitRecord.setNumber(number);
                newUserPit.put("number", 0);
            }
            long day = (System.currentTimeMillis() - userPit.getLastReceiveTime().getTime()) / 1000 / 60 / 60 / 24;
            userPit.setLastReceiveTime(DateUtil.getDateByDay(userPit.getLastReceiveTime(), (int) day));
            //领取矿石到背包
            gameService.addReward(userId, reward, null);
            pitRecordList.add(pitRecord);
            //批量新增領取记录
            pitRecordService.batchAddRecord(pitRecordList);
            //添加上级收益
            addParentOrGrandfaReward(userPit.getUnReceive(), 1, userId);
            addParentOrGrandfaReward(userPit.getUnReceive(), 2, userId);
            //数据库更改数据
            userPitService.update(userPit);
            JSONObject result = new JSONObject();
            result.put("rewardInfo", reward);
            return result;
        }
    }

    public void addParentOrGrandfaReward(JSONArray reward, int type, Long userId) {
        int count;
        Long addUserId = null;
        PitUserParent pitUserParent = pitUserParentService.findParentByUserId(userId);
        if (type == 1) {
            count = 5;
            if (pitUserParent.getPitParentId() != null) {
                addUserId = pitUserParent.getPitParentId();
            }
        } else {
            count = 20;
            if (pitUserParent.getPitGrandfaId() != null) {
                addUserId = pitUserParent.getPitGrandfaId();
            }
        }
        if (addUserId != null) {
            for (Object o : reward) {
                JSONObject info = (JSONObject) o;
                String id = info.getString("id");
                int number = info.getIntValue("number") / count;
                gameService.updateUserBackpack(addUserId, id, number, LogUserBackpackTypeEnum.receive_number);
                if (type == 1) {
                    //修改上级收益
                    pitUserParentService.addParentIncome(userId, number);
                } else {
                    //修改上上级收益
                    pitUserParentService.addGrandfaIncome(userId, number);
                }
            }
        }
    }

    @Transactional
    @ServiceMethod(code = "004", description = "绑定")
    public Object binding(ManagerSocketServer adminSocketServer, Command appCommand, JSONObject params) {
        checkNull(params);
        /**
         * 绑定成功之后赠送上级和上上级收益
         * 赋值上级id 查询父级的父级 赋值上上级id
         */
        Long userId = params.getLong("userId");
        synchronized (LockUtil.getlock(userId)){
            User parentUser = userCacheService.getUserInfoByUserNo(params.getString("userNo"));
            if (parentUser == null) {
                throwExp("玩家不存在");
            }
            if (Objects.equals(parentUser.getId(), userId)) {
                throwExp("不能绑定自己的id");
            }
            //上级的绑定信息 判断是否互相绑定
            PitUserParent pitUserParent = pitUserParentService.findParentByUserId(parentUser.getId());
            if (Objects.equals(parentUser.getParentId(), userId) || Objects.equals(parentUser.getGrandfaId(), userId)) {
                throwExp("该玩家是您的好友，不能绑定");
            }
            long parentId = parentUser.getId();
            params.put("parentId", parentId);
            JSONObject jsonObject = new JSONObject();
            //初始化上次领取时间 上次查看时间 未领取
            jsonObject.put("userId", userId);
            //查询上上级id
            if (null != pitUserParent) {
                params.put("pitGrandfaId", pitUserParent.getPitParentId());
            }
            pitUserParentService.insertPitUserParent(params);
            return new JSONObject();
        }

    }

    @Transactional
    @ServiceMethod(code = "005", description = "退款")
    public Object refund(ManagerSocketServer adminSocketServer, JSONObject params) {
        JSONObject jsonObject = new JSONObject();
        checkNull(params);
        Long userId = params.getLong("userId");
        synchronized (LockUtil.getlock(userId)) {
            Long pitId = params.getLong("pitId");
            //初始化上次领取时间 上次查看时间 未领取
            UserPit userPit = userPitService.findInitInfo(params);
            if (userPit == null) {
                throwExp("无需重复退款");

            }
            //如果上次领取时间-开通时间 =0 未领取过
            long diffLastReceiveTime = (userPit.getLastReceiveTime().getTime() - userPit.getOpenTime().getTime()) / 1000 / 86400;
            if (diffLastReceiveTime > 0) {
                throwExp("已领取过奖励，不可退款");
            }
            if (((System.currentTimeMillis() - userPit.getOpenTime().getTime()) / 1000 / 60 / 60 / 24) > 3) {
                throwExp("超过3天不可以退款");
            }
            BigDecimal amount = PlayGameService.DIC_PIT.get(pitId.toString()).getPrice();
            userCapitalService.addUserBalanceByCancelPit(amount, userId, UserCapitalTypeEnum.currency_2.getValue(), LogCapitalTypeEnum.pit_refund);
            managerGameBaseService.pushCapitalUpdate(userId, UserCapitalTypeEnum.currency_2.getValue());
            jsonObject.put("refundStatus", 1);
            userPitService.deleteUserPit(userId, pitId);
            subActiveScore(userId, String.valueOf(pitId));
            return jsonObject;
        }

    }

    public void subActiveScore(Long userId, String pitId) {
        Activity activity = gameCacheService.getActivity();
        if (activity == null) {
            return;
        }
        double score = -5.0;
        if (pitId.equals("1")) {
            score = -10.0;
        }
        if (activity.getAddPointEvent() == 4) {
            User user = userCacheService.getUserInfoById(userId);
            PitUserParent pitUserParent = pitUserParentService.findParentByUserId(userId);
            //  gameCacheService.addPointMySelf(userId,getScore(dicMine));
            if (pitUserParent.getPitParentId() != null) {
                gameCacheService.addPointMySelf(pitUserParent.getPitParentId(), score);
            }
        }
    }

    @Transactional
    @ServiceMethod(code = "006", description = "领取记录")
    public JSONObject receiveRecord(ManagerSocketServer adminSocketServer, JSONObject params) {
        checkNull(params);
        //初始化上次领取时间 上次查看时间 未领取
        List<PitRecord> userPitList = pitRecordService.findPitRecord(params);
        JSONObject result = new JSONObject();
        result.put("myRecord", userPitList);
        return result;
    }

    @Transactional
    @ServiceMethod(code = "007", description = "直接矿工")
    public JSONObject getSuborList(ManagerSocketServer adminSocketServer, JSONObject params) {
        checkNull(params);
        JSONObject result = new JSONObject();
        //根据userId查询我的下级矿工
        List<PitUserParent> suborList = pitUserParentService.findSubor(params);
        if (suborList.size() == 0) {
            return result;
        }
        JSONArray array = new JSONArray();
        for (PitUserParent pitUserParent : suborList) {
            JSONObject jsonObject = new JSONObject();
            User user = userCacheService.getUserInfoById(pitUserParent.getUserId().longValue());
            jsonObject.put("userId", user.getId());
            jsonObject.put("userNo", user.getUserNo());
            jsonObject.put("name", user.getName());
            jsonObject.put("headImageUrl", user.getHeadImageUrl());
            //根据两个集合查询出我下级员工的所有收益
            jsonObject.put("number", pitUserParent.getCreateParentAmount());
            array.add(jsonObject);
        }
        result.put("suborList", array);
        return result;
    }

    @Transactional
    @ServiceMethod(code = "008", description = "间接矿工")
    public JSONObject getIndirSuborList(ManagerSocketServer adminSocketServer, JSONObject params) {
        checkNull(params);
        Long userId = params.getLong("userId");
        //根据userId查询我的下下级矿工
        List<PitUserParent> indirSuborList = pitUserParentService.findIndirSubor(params);
        JSONArray array = new JSONArray();
        for (PitUserParent pitUserParent : indirSuborList) {
            JSONObject jsonObject = new JSONObject();
            User user = userCacheService.getUserInfoById(pitUserParent.getUserId().longValue());
            jsonObject.put("userId", user.getId());
            jsonObject.put("userNo", user.getUserNo());
            jsonObject.put("name", user.getName());
            jsonObject.put("headImageUrl", user.getHeadImageUrl());
            //根据两个集合查询出我下级员工的所有收益
            jsonObject.put("number", pitUserParent.getCreateGrandfaAmount());
            array.add(jsonObject);
        }
        JSONObject result = new JSONObject();
        result.put("indirSuborList", array);
        return result;
    }
}

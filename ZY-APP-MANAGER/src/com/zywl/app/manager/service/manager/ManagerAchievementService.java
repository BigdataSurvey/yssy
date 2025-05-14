package com.zywl.app.manager.service.manager;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.enums.PushCode;
import com.live.app.ws.util.Push;
import com.zywl.app.base.bean.*;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.JSONUtil;
import com.zywl.app.base.util.LockUtil;
import com.zywl.app.base.util.OrderUtil;
import com.zywl.app.defaultx.annotation.KafkaProducer;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.cache.ItemCacheService;
import com.zywl.app.defaultx.cache.UserCapitalCacheService;
import com.zywl.app.defaultx.enmus.AchievementGroupEnum;
import com.zywl.app.defaultx.enmus.LogCapitalTypeEnum;
import com.zywl.app.defaultx.enmus.LogUserBackpackTypeEnum;
import com.zywl.app.defaultx.enmus.UserCapitalTypeEnum;
import com.zywl.app.defaultx.service.CompleteAchievementRecordService;
import com.zywl.app.defaultx.service.UserAchievementService;
import com.zywl.app.defaultx.service.UserCapitalService;
import com.zywl.app.manager.context.KafkaEventContext;
import com.zywl.app.manager.context.KafkaTopicContext;
import com.zywl.app.manager.context.MessageCodeContext;
import com.zywl.app.manager.service.PlayGameService;
import com.zywl.app.manager.socket.ManagerSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@ServiceClass(code = MessageCodeContext.ACHIEVEMENT_SETVER)
public class ManagerAchievementService extends BaseService {


    @Autowired
    private UserCapitalService userCapitalService;

    @Autowired
    private PlayGameService gameService;

    @Autowired
    private UserAchievementService userAchievementService;

    @Autowired
    private ItemCacheService itemCacheService;

    @Autowired
    private UserCapitalCacheService userCapitalCacheService;

    @Autowired
    private ManagerSocketService managerSocketService;


    public static Object lock = new Object();
    @Autowired
    private CompleteAchievementRecordService completeAchievementRecordService;


    @Transactional
    @ServiceMethod(code = "001", description = "领取成就奖励")
    @KafkaProducer(topic = KafkaTopicContext.RED_POINT,event = KafkaEventContext.RECEIVE_ACHIEVEMENT,sendParams = true)
    public JSONObject receiveReward(ManagerSocketServer adminSocketServer, JSONObject data) {
        checkNull(data);
        checkNull(data.get("userId"), data.get("achievementId"));
        String id = data.getString("achievementId");
        Long userId = data.getLong("userId");
        JSONArray reward = null;
        synchronized (LockUtil.getlock(userId.toString())) {
            UserAchievement userAchievement = gameService.getUserAchievement(userId.toString());
            JSONArray array = userAchievement.getAchievementList();
            boolean b = false;
            JSONArray newArray = new JSONArray();
            for (Object o : array) {
                JSONObject achievementVo = JSONObject.from(o);
                if (achievementVo.getString("id").equals(id)) {
                    b = true;
                    if (achievementVo.getIntValue("status") == 0) {
                        throwExp("不满足领取条件");
                    }
                    if (achievementVo.getIntValue("status") == 1) {
                        throwExp("已领取过该成就");
                    }
                    reward = achievementVo.getJSONArray("reward");
                    gameService.addReward(userId, reward, LogCapitalTypeEnum.receive_achievement);
                    int group = achievementVo.getIntValue("group");
                    int groupId = achievementVo.getIntValue("groupId");
                    Map<String, Achievement> stringAchievementMap = PlayGameService.achievementMap.get(String.valueOf(group));
                    if (stringAchievementMap.containsKey(String.valueOf(groupId + 1))) {
                        Achievement achievement = stringAchievementMap.get(String.valueOf(groupId + 1));
                        JSONObject newVo = new JSONObject();
                        newVo.put("id", achievement.getId());
                        newVo.put("condition", achievement.getCondition());
                        newVo.put("context", achievement.getContext());
                        newVo.put("reward", achievement.getReward());
                        newVo.put("group", group);
                        newVo.put("groupId", groupId + 1);
                        newVo.put("schedule", achievementVo.getIntValue("schedule"));
                        if (achievementVo.getIntValue("schedule") >= Integer.parseInt(achievement.getCondition()) &&group!= AchievementGroupEnum.PVP_RANK.getValue()) {
                            newVo.put("status", 2);
                        } else {
                            if (group== AchievementGroupEnum.PVP_RANK.getValue() &&   (achievementVo.getIntValue("schedule") <= Integer.parseInt(achievement.getCondition()))){
                                newVo.put("status", 2);
                            }else {
                                newVo.put("status", 0);
                            }

                        }
                        newArray.add(newVo);
                    }
                } else {
                    newArray.add(achievementVo);
                }
            }
            if (!b) {
                throwExp("未查询到该成就");
            }
            //领取完成 更新成就信息
            userAchievementService.updateList(userId,newArray);
            PlayGameService.userAchievementMap.get(userId.toString()).setAchievementList(newArray);
            JSONObject result = new JSONObject();
            result.put("userId", data.get("userId"));
            result.put("rewardInfo", reward);
            return result;
        }
    }

    @Transactional
    @ServiceMethod(code = "002", description = "获取成就信息")
    public JSONObject getAchievementInfo(ManagerSocketServer adminSocketServer, JSONObject data) {
        checkNull(data);
        checkNull(data.get("userId"));
        String userId = data.getString("userId");
        synchronized (LockUtil.getlock(userId)){
            UserAchievement userAchievement = gameService.getUserAchievement(userId);
            JSONObject result = new JSONObject();
            result.put("userId", userId);
            JSONArray achievementList = userAchievement.getAchievementList();
            achievementList = JSONUtil.sortArray(achievementList,"status");
            result.put("achievementList", achievementList);
            return result;
        }
    }


}

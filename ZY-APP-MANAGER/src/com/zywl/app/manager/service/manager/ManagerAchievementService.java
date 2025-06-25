package com.zywl.app.manager.service.manager;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.CompleteAchievementRecord;
import com.zywl.app.base.bean.UserAchievement;
import com.zywl.app.base.bean.UserStatistic;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.LockUtil;
import com.zywl.app.base.util.OrderUtil;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.cache.AchievementCacheService;
import com.zywl.app.defaultx.cache.ItemCacheService;
import com.zywl.app.defaultx.cache.UserBackpackCacheService;
import com.zywl.app.defaultx.cache.UserCapitalCacheService;
import com.zywl.app.defaultx.enmus.LogCapitalTypeEnum;
import com.zywl.app.defaultx.service.*;
import com.zywl.app.manager.context.MessageCodeContext;
import com.zywl.app.manager.service.PlayGameService;
import com.zywl.app.manager.socket.ManagerSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@ServiceClass(code = MessageCodeContext.ACHIEVEMENT_SETVER)
public class ManagerAchievementService extends BaseService {


    @Autowired
    private UserAchievementService userAchievementService;

    @Autowired
    private PlayGameService gameService;

    @Autowired
    private UserStatisticService userStatisticService;

    @Autowired
    private UserService userService;



    @Transactional
    @ServiceMethod(code = "001", description = "领取成就奖励")
    public JSONObject receiveReward(ManagerSocketServer adminSocketServer, JSONObject data) {
        checkNull(data);
        checkNull(data.get("userId"), data.get("achievementId"));
        String id = data.getString("achievementId");
        Long userId = data.getLong("userId");
        synchronized (LockUtil.getlock(userId)) {
            UserAchievement userAchievement = gameService.getUserAchievement(userId.toString());
            JSONArray array = userAchievement.getAchievementList();
            boolean b = false;
            JSONArray reward = null;
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
                    achievementVo.put("status", 1);
                    newArray.add(achievementVo);
                } else {
                    newArray.add(achievementVo);
                }
            }
            if (!b) {
                throwExp("未查询到该成就");
            }
            userAchievementService.updateList(userId,array);
            //领取完成 更新成就信息
            gameService.addReward(userId, reward, LogCapitalTypeEnum.receive_achievement);

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
        UserAchievement userAchievement = gameService.getUserAchievement(userId);
        JSONArray achievementList = userAchievement.getAchievementList();
        UserStatistic userStatistic = userStatisticService.findByUserId(Long.valueOf(userId));
        boolean b = false;
        long oneJuniorCount = userService.getOneJuniorCount(Long.valueOf(userId));
        for (Object o : achievementList) {
            JSONObject info = (JSONObject) o;
            if (info.getInteger("group") == 2) {
               boolean c = checkAchievementByInviteUser(info, oneJuniorCount);
               if (!b && c){
                    b= true;
               }
            }
        }
        if (b){
            userAchievementService.updateList(Long.valueOf(userId),achievementList);
        }
        JSONObject result = new JSONObject();
        result.put("userId", userId);
        result.put("achievementList", achievementList);
        return result;
    }

    public boolean checkAchievementByInviteUser(JSONObject info, Long number) {
        int status = info.getInteger("status");
        if (status==1){
            return false;
        }
        info.put("schedule",number);
        // 0 未完成  2 完成未领取
        if (status==0 || status==2){
            if (number>=info.getInteger("condition")){
                info.put("schedule",info.getIntValue("condition"));
                info.put("status",2);
                return true;
            }
        }
        return false;
    }

}

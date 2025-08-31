package com.zywl.app.defaultx.service;

import com.alibaba.fastjson2.JSONArray;
import com.zywl.app.base.bean.Achievement;
import com.zywl.app.base.bean.UserAchievement;
import com.zywl.app.base.bean.vo.AchievementVo;
import com.zywl.app.base.util.BeanUtils;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.apache.commons.collections4.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Service
public class UserAchievementService extends DaoService {


    @Autowired
    private AchievementService achievementService;

    public UserAchievementService() {
        super("UserAchievementMapper");
    }

    @Transactional
    public void addUserAchievement(Long userId, JSONArray list) {
        UserAchievement userAchievement = new UserAchievement();
        userAchievement.setUserId(userId);
        userAchievement.setAchievementList(list);
        save(userAchievement);
    }

    public UserAchievement findUserAchievement(Long userId) {
        Map<String, Object> params = new HashedMap<String, Object>();
        params.put("userId", userId);
        UserAchievement one = findOne(params);
        if (one == null) {
            List<Achievement> achievements = achievementService.findAll();
            JSONArray vos = new JSONArray();
            for (Achievement achievement : achievements) {
                    AchievementVo vo = new AchievementVo();
                    BeanUtils.copy(achievement, vo);
                    vos.add(vo);
            }
            addUserAchievement(userId, vos);
            return findOne(params);
        }
        return one;
    }

    public void batchUpdateStatic(List<UserAchievement> userAchievements) {
        if (userAchievements != null) {
            List<UserAchievement> newList = new ArrayList<>();
            for (int i = 0; i < userAchievements.size(); i++) {
                newList.add(userAchievements.get(i));
                if (i % 5000 == 0) {
                    execute("updateList", newList);
                    newList.clear();
                }
            }
            if (!newList.isEmpty()) {
                execute("updateList", newList);
            }
        }

    }



    @Transactional
    public int updateList(Long userId, JSONArray achievementList) {
        Map<String, Object> params = new HashedMap<>();
        params.put("userId", userId);
        params.put("achievementList", achievementList);
        return execute("updateUserAchievement", params);
    }


}

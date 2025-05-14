package com.zywl.app.defaultx.service.card;

import com.alibaba.fastjson2.JSONArray;
import com.zywl.app.base.bean.UserTimeTask;
import com.zywl.app.base.bean.card.DicTimeTask;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserTimeTaskService extends DaoService {

    public UserTimeTaskService() {
        super("UserTimeTaskMapper");
    }

    public List<UserTimeTask> findAllTimeTask() {
        return findAll();
    }

    @Transactional
    public void addTimeTask(Long userId, DicTimeTask dicTimeTask) {
        UserTimeTask userTimeTask = new UserTimeTask();
        userTimeTask.setTaskId(dicTimeTask.getId());
        userTimeTask.setUserId(userId);
        userTimeTask.setCondition(dicTimeTask.getCondition());
        userTimeTask.setContext(dicTimeTask.getContext());
        userTimeTask.setGroup(dicTimeTask.getGroup());
        userTimeTask.setSchedule(0);
        userTimeTask.setStatus(0);
        userTimeTask.setReward(dicTimeTask.getReward());
        save(userTimeTask);
    }


    public List<UserTimeTask> findByGroupAndUserId(int group,Long userId) {
        Map<String, Object> map = new HashMap<>();
        map.put("group", group);
        map.put("userId",userId);
        return findList("findByGroupAndUserId", map);
    }

    public UserTimeTask findByIdAndUserId(Long taskId,Long userId) {
        Map<String, Object> map = new HashMap<>();
        map.put("taskId", taskId);
        map.put("userId",userId);
        return (UserTimeTask) findOne("findByIdAndUserId", map);
    }

    @Transactional
    public void updateTaskCanReceive(Long userId,Long taskId) {
        Map<String, Object> map = new HashMap<>();
        map.put("taskId", taskId);
        map.put("userId",userId);
        map.put("status",2);
        execute("updateTaskStatus",map);
    }
    @Transactional
    public void updateTaskReceiving(Long userId,Long taskId) {
        Map<String, Object> map = new HashMap<>();
        map.put("taskId", taskId);
        map.put("userId",userId);
        map.put("status",1);
        execute("updateTaskStatus",map);
    }

    @Transactional
    public void updateTaskSchedule(Long userId,Long taskId,int schedule) {
        Map<String, Object> map = new HashMap<>();
        map.put("taskId", taskId);
        map.put("userId",userId);
        map.put("schedule",schedule);
        execute("updateTaskSchedule",map);
    }


}

package com.zywl.app.manager.service.manager;


import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.BaseBean;
import com.zywl.app.base.bean.UserInviteInfo;
import com.zywl.app.base.bean.UserReceiveInviteRecord;
import com.zywl.app.base.bean.UserTimeTask;
import com.zywl.app.base.bean.card.DicTimeTaskGroup;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.enmus.LogCapitalTypeEnum;
import com.zywl.app.defaultx.service.card.DicTimeTaskGroupService;
import com.zywl.app.defaultx.service.card.UserTimeTaskService;
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
@ServiceClass(code = MessageCodeContext.TIME_TASK)
public class ManagerTimeTaskService extends BaseService {

    @Autowired
    private UserTimeTaskService userTimeTaskService;

    @Autowired
    private DicTimeTaskGroupService dicTimeTaskGroupService;

    @Autowired
    private PlayGameService gameService;



    @Transactional
    @ServiceMethod(code = "002", description = "领取限时活动奖励")
    public Object receiveReward(ManagerSocketServer adminSocketServer, JSONObject data) {
        checkNull(data);
        checkNull(data.get("userId"),data.get("taskId"));
        Long userId = data.getLong("userId");
        Long taskId = data.getLong("taskId");
        UserTimeTask byIdAndUserId = userTimeTaskService.findByIdAndUserId(taskId, userId);
        DicTimeTaskGroup dicTimeTaskGroup = dicTimeTaskGroupService.findGroup();
        if (byIdAndUserId.getGroup()!=dicTimeTaskGroup.getGroup()){
            throwExp("任务已过期");
        }
        if (byIdAndUserId.getSchedule()< byIdAndUserId.getCondition() || byIdAndUserId.getStatus()==0){
            throwExp("未达成任务条件");
        }
        if (byIdAndUserId.getStatus()==1){
            throwExp("奖励已领取");
        }
        JSONArray reward = byIdAndUserId.getReward();
        userTimeTaskService.updateTaskReceiving(userId,taskId);
        gameService.addReward(userId,reward, LogCapitalTypeEnum.receive_invite);
        JSONObject result = new JSONObject();
        result.put("rewardInfo",reward);
        return result;
    }
}

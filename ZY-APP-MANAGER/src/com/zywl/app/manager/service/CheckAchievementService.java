package com.zywl.app.manager.service;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.enums.PushCode;
import com.live.app.ws.util.DefaultPushHandler;
import com.live.app.ws.util.Push;
import com.zywl.app.base.bean.UserAchievement;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.enmus.AchievementTypeEnum;
import com.zywl.app.defaultx.enmus.DailyTaskTypeEnum;
import com.zywl.app.defaultx.enmus.RedReminderIndexEnum;
import com.zywl.app.manager.service.manager.ManagerGameBaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;


@Service
public class CheckAchievementService extends BaseService {




    @Autowired
    private PlayGameService gameService;


    @Autowired
    private UserCacheService userCacheService;

    @Autowired
    private ManagerGameBaseService managerGameBaseService;


    @PostConstruct
    public void _CheckAchievementService() {
        Push.addPushSuport(PushCode.redReminder, new DefaultPushHandler());
    }















}

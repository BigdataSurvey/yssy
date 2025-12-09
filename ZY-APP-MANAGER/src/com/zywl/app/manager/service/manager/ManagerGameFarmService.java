package com.zywl.app.manager.service.manager;

import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.User;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.cache.UserCapitalCacheService;
import com.zywl.app.defaultx.service.UserFarmLandService;
import com.zywl.app.defaultx.service.UserService;
import com.zywl.app.defaultx.service.card.DicFarmService;
import com.zywl.app.manager.context.MessageCodeContext;
import com.zywl.app.manager.socket.ManagerSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * @Author: lzx
 * @Create: 2025/12/9
 * @Version: V1.0
 * @Description: 用户种地管理  Manager
 * @Task: 036 (MessageCodeContext.USER_FARM)
 */
@Service
@ServiceClass(code = MessageCodeContext.USER_FARM)
public class ManagerGameFarmService {
    //农场种地配置服务
    @Autowired
    private DicFarmService dicFarmService;
    //用户缓存服务
    @Autowired
    private UserCacheService userCacheService;
    //用户资产缓存服务
    @Autowired
    private UserCapitalCacheService userCapitalCacheService;
    //用户种地服务
    @Autowired
    private UserFarmLandService userFarmLandService;
    //用户服务
    @Autowired
    private UserService userService;


    @ServiceMethod(code = "001", description = "获取农场信息")
    @Transactional(readOnly = true)
    public JSONObject getMyFarmInfo(ManagerSocketServer socket, JSONObject data) {
        Long userId = data.getLong("userId");
        //校验用户
        Map<Long, User> users = userCacheService.loadUsers(userId);
        User user = users.get(userId);

        return null;
    }


}

package com.zywl.app.server.service;


import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.bean.Command;
import com.live.app.ws.enums.TargetSocketType;
import com.live.app.ws.util.CommandBuilder;
import com.live.app.ws.util.Executer;
import com.zywl.app.base.bean.User;
import com.zywl.app.base.bean.UserStatistic;
import com.zywl.app.base.bean.vo.UserVo;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.BeanUtils;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.cache.AppConfigCacheService;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.service.UserService;
import com.zywl.app.defaultx.service.UserStatisticService;
import com.zywl.app.server.context.MessageCodeContext;
import com.zywl.app.server.socket.AppSocket;
import com.zywl.app.server.util.RequestManagerListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


//推广相关
@Service
@ServiceClass(code = MessageCodeContext.PROMOTE_SERVER)
public class ServerPromoteService extends BaseService {

    @Autowired
    private UserCacheService userCacheService;


  /*  @ServiceMethod(code = "info", description = "获取限时邀请好友活动")
    public Object getUserInviteInfo(final AppSocket appSocket, Command appCommand, JSONObject params) {
        long userId =  appSocket.getWsidBean().getUserId();
        User user = userCacheService.getUserInfoById(userId);
        params.put("userId",userId);
        if (user==null) {
            throwExp("查询用户信息失败，请稍后重试");
        }
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("017001", params).build(), new RequestManagerListener(appCommand));
        return async();
    }

    @ServiceMethod(code = "receive", description = "领取奖励")
    public Object receive(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("rewardId"));
        long userId =  appSocket.getWsidBean().getUserId();
        User user = userCacheService.getUserInfoById(userId);
        params.put("userId",userId);
        if (user==null) {
            throwExp("查询用户信息失败，请稍后重试");
        }
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("017002", params).build(), new RequestManagerListener(appCommand));
        return async();
    }*/


}

package com.zywl.app.server.service;

import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.bean.Command;
import com.live.app.ws.enums.TargetSocketType;
import com.live.app.ws.util.CommandBuilder;
import com.live.app.ws.util.Executer;
import com.zywl.app.base.bean.User;
import com.zywl.app.base.bean.vo.UserSonVo;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.Async;
import com.zywl.app.base.util.BeanUtils;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.service.UserService;
import com.zywl.app.server.context.MessageCodeContext;
import com.zywl.app.server.socket.AppSocket;
import com.zywl.app.server.util.RequestManagerListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@ServiceClass(code = MessageCodeContext.USER_SERVER)
public class ServerUserService extends BaseService {


    @Autowired
    private UserCacheService userCacheService;

    @Autowired
    private UserService userService;

    @ServiceMethod(code = "updateIdCardMoney", description = "是否可以实名认证")
    public Object isUpdateIdCard(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        Long userId = appSocket.getWsidBean().getUserId();
        User user = userCacheService.getUserInfoById(userId);
        JSONObject result = new JSONObject();
        if (user.getIsUpdateIdCard()==0){
            result.put("money",0);
        }else {
            result.put("money",100);
        }
        return result;
    }


    @ServiceMethod(code = "001", description = "实名认证")
    public Async authentication(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("realName"));
        params.put("userId", appSocket.getWsidBean().getUserId());
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("010008", params).build(),
                new RequestManagerListener(appCommand));
        return async();
    }

    @ServiceMethod(code = "002", description = "设置社交信息")
    public Async settingQQWX(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        params.put("userId", appSocket.getWsidBean().getUserId());
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("010009", params).build(),
                new RequestManagerListener(appCommand));
        return async();
    }


    @ServiceMethod(code = "003", description = "用户注销账号")
    public Async delete(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        params.put("userId", appSocket.getWsidBean().getUserId());
        params.put("wsId", appSocket.getWsidBean().getWsid());
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("010010", params).build(),
                new RequestManagerListener(appCommand));
        return async();
    }

    @ServiceMethod(code = "004", description = "获取好友列表")
    public Object getSon(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        int vip = params.getIntValue("vip");
        Long userId =appSocket.getWsidBean().getUserId();
        int type = 1;
        if (params.containsKey("type")) {
            type = params.getIntValue("type");
        }
        List<User> users = null;
        if (type == 1) {
            users = userService.findUsersByParentId(userId, params.getInteger("page"), params.getInteger("num"), vip);
        } else if (type == 2) {
            users = userService.findUsersByGrandfaId(userId, params.getInteger("page"), params.getInteger("num"), vip);
        } else if (type == 3) {
            users = userService.findMySonNoAuthicatino(userId, params.getInteger("page"), params.getInteger("num"), vip);
        }
        long count = userCacheService.getMySonCount(userId, type);
        List<UserSonVo> list = new ArrayList<>();
        for (User user1 : users) {
            UserSonVo userSonVo = new UserSonVo();
            BeanUtils.copy(user1, userSonVo);
            double todayCreateParentIncome = 0.0;
            double todayMyCreateAnima = 0.0;
            if (type==1){
                todayCreateParentIncome= userCacheService.getTodayCreateParentIncome(user1.getId());
                todayMyCreateAnima = userCacheService.getTodayMyCreateAnima(user1.getId());
            }else if(type==2){
                todayCreateParentIncome= userCacheService.getGrandfaTodayIncome(user1.getId());
                todayMyCreateAnima = userCacheService.getTodayMyCreateGrandfaAnima(user1.getId());
            }
            userSonVo.setTodayCreateIncome(Math.round(todayCreateParentIncome * 100.0) / 100.0);
            userSonVo.setTodayCreatAnima(Math.round(todayMyCreateAnima * 100.0) / 100.0);
            userSonVo.setLastLoginTime(user1.getLastLoginTime().getTime());
            list.add(userSonVo);
        }
        long oneJuniorCount = userService.getOneJuniorCount(userId);
        long twoJuniorCount = userService.getTwoJuniorCount(userId);
        long noAuthCount = userService.countNoAuthenticationJunior(userId);
        JSONObject result = new JSONObject();
        result.put("userList", list);
        result.put("count", count < 0 ? 0 : count);
        result.put("oneFriend", oneJuniorCount);
        result.put("twoFriend", twoJuniorCount);
        result.put("threeFriend",noAuthCount);
        return result;
    }

    @ServiceMethod(code = "005", description = "获取好友信息")
    public Object getSonInfo(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("userNo"));
        long myId = appSocket.getWsidBean().getUserId();//412
        params.put("userId",myId);
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("010021", params).build(),
                new RequestManagerListener(appCommand));
        return async();
    }


    @ServiceMethod(code = "006", description = "手动绑定邀请码")
    public Object addCode(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("inviteCode"));
        Long userId = appSocket.getWsidBean().getUserId();
        params.put("userId", userId);
        String inviteCode = params.getString("inviteCode");
        User my = userCacheService.getUserInfoById(userId);
        User user = userService.findUserByInviteCode(inviteCode);
        if (user == null || my == null) {
            throwExp("未查询到该玩家");
        }
        if (my.getInviteCode()==null){
            throwExp("账号信息异常，请联系客服");
        }
        if (my.getInviteCode().equals(inviteCode)) {
            throwExp("不能设置自己为邀请人");
        }
        if (user.getParentId()!=null && user.getParentId().toString().equals(my.getId().toString())) {
            throwExp("不能绑定自己下级为邀请人");
        }
        if (user.getGrandfaId()!=null && user.getGrandfaId().toString().equals(my.getId().toString())) {
            throwExp("不能绑定自己下级为邀请人");
        }
        if (user.getChannelNo()!=null && my.getIsChannel()==1 && user.getChannelNo().equals(my.getChannelNo())){
            throwExp("不能绑定团队好友为邀请人");
        }
        if (my.getParentId() != null) {
            throwExp("已有邀请人");
        }
        if (user.getChannelNo() != null) {
            params.put("channelNo", user.getChannelNo());
        }
        params.put("parentId", user.getId());
        params.put("grandfaId", user.getParentId());
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("010012", params).build(),
                new RequestManagerListener(appCommand));
        return async();
    }
    @ServiceMethod(code = "007", description = "获取上级信息")
    public Object getParentInfo(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        Long userId = appSocket.getWsidBean().getUserId();
        User user = userCacheService.getUserInfoById(userId);
        if (user == null) {
            throwExp("获取玩家信息失败");
        }
        JSONObject result = new JSONObject();
        if (user.getParentId() == null) {
            return result;
        }
        User parentUser = userCacheService.getUserInfoById(user.getParentId());
        if (parentUser==null || parentUser.getStatus()!=1){
            throwExp("邀请人状态异常");
        }
        result.put("name", parentUser.getName());
        result.put("headImgUrl", parentUser.getHeadImageUrl());
        result.put("userNo", parentUser.getUserNo());
        result.put("wx", parentUser.getWechatId());
        result.put("qq", parentUser.getQq());
        return result;
    }


    @ServiceMethod(code = "008", description = "获取收益统计")
    public Object getIncomeStatement(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        Long userId = appSocket.getWsidBean().getUserId();
        params.put("userId",userId);
        User user = userCacheService.getUserInfoById(userId);
        if (user == null) {
            throwExp("获取玩家信息失败");
        }
        JSONObject result = new JSONObject();
        if (user.getParentId() == null) {
            result.put("inviterNo",user.getParentId());
        }
        User parentUser = userCacheService.getUserInfoById(user.getParentId());
        result.put("name", parentUser.getName());
        result.put("headImgUrl", parentUser.getHeadImageUrl());
        result.put("userNo", parentUser.getUserNo());
        result.put("wx", parentUser.getWechatId());
        result.put("qq", parentUser.getQq());
        return result;
    }

    @ServiceMethod(code = "013", description = "申请开通渠道")
    public Object applyForChannel(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        Long userId = appSocket.getWsidBean().getUserId();
        params.put("userId",userId);
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("010013", params).build(),
                new RequestManagerListener(appCommand));
        return async();
    }

    @ServiceMethod(code = "014", description = "绑定手机号")
    public Object addTel(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("phone"));
        Long userId = appSocket.getWsidBean().getUserId();
        params.put("userId",userId);
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("010014", params).build(),
                new RequestManagerListener(appCommand));
        return async();
    }

    @ServiceMethod(code = "week", description = "周卡")
    public Object week(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        Long userId = appSocket.getWsidBean().getUserId();
        params.put("userId",userId);
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("010015", params).build(),
                new RequestManagerListener(appCommand));
        return async();
    }

    @ServiceMethod(code = "month", description = "月卡")
    public Object month(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        Long userId = appSocket.getWsidBean().getUserId();
        params.put("userId",userId);
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("010016", params).build(),
                new RequestManagerListener(appCommand));
        return async();
    }

    @ServiceMethod(code = "015", description = "我的信息")
    public Object getMyInfo(final AppSocket appSocket, Command appCommand, JSONObject params){
        Long userId = appSocket.getWsidBean().getUserId();
        params.put("userId",userId);
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("100044", params).build(),
                new RequestManagerListener(appCommand));
        return async();
    }

    @ServiceMethod(code = "getTopUserInfo", description = "排行榜成员信息")
    public Object getTopUserInfo(final AppSocket appSocket, Command appCommand, JSONObject params){
        Long userId = params.getLong("userId");
        User user = userCacheService.getUserInfoById(userId);
        if (user==null){
            throwExp("查询用户信息失败");
        }
       JSONObject result= new JSONObject();
        result.put("userId",userId);
        result.put("userNo",user.getUserNo());
        result.put("name",user.getName());
        result.put("headImg",user.getHeadImageUrl());
        result.put("qq",user.getQq()==null?"暂无":user.getQq());
        result.put("wx",user.getWechatId()==null?"暂无":user.getWechatId());
        return result;
    }


}

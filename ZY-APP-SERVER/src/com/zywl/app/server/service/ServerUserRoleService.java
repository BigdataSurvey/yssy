package com.zywl.app.server.service;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.bean.Command;
import com.live.app.ws.enums.TargetSocketType;
import com.live.app.ws.util.CommandBuilder;
import com.live.app.ws.util.Executer;
import com.zywl.app.base.bean.*;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.base.util.JSONUtil;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.service.DicRoleService;
import com.zywl.app.defaultx.service.RoleService;
import com.zywl.app.defaultx.service.UserGiftService;
import com.zywl.app.defaultx.service.UserRoleService;
import com.zywl.app.server.context.MessageCodeContext;
import com.zywl.app.server.socket.AppSocket;
import com.zywl.app.server.util.RequestManagerListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@ServiceClass(code = MessageCodeContext.USER_ROLE)
public class ServerUserRoleService extends BaseService {

    @Autowired
    private UserGiftService userGiftService;

    @Autowired
    private UserCacheService userCacheService;

    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private DicRoleService dicRoleService;

    public static final Map<String, DicRole> DIC_ROLE = new ConcurrentHashMap<>();

    @PostConstruct
    public void _serverUserRoleService() {
        initRole();
    }

    public void initRole() {
        List<DicRole> allRole = dicRoleService.findAllRole();
        allRole.forEach(e -> DIC_ROLE.put(e.getId().toString(), e));
    }


    @ServiceMethod(code = "001", description = "获取角色礼包信息")
    public JSONObject getUserConfig(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("type"));
        Long userId = appSocket.getWsidBean().getUserId();
        int type = params.getIntValue("type");
        if (type != 1 && type != 2) {
            throwExp("非法请求");
        }
        UserGift userGift = userGiftService.findUserGift(userId, type);
        params.put("number", 0);
        if (userGift != null) {
            params.put("number", userGift);
        } 
        return params;
    }

    @ServiceMethod(code = "002", description = "激活角色礼包")
    public JSONObject useGift(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("userNo"), params.get("type"));
        String userNo = params.getString("userNo");
        Long myId = appSocket.getWsidBean().getUserId();
        int type = params.getIntValue("type");
        if (type != 1 && type != 2) {
            throwExp("非法请求");
        }
        User user = userCacheService.getUserInfoByUserNo(userNo);
        if (user == null) {
            throwExp("玩家不存在");
        }
        UserGift userGift = userGiftService.findUserGift(user.getId(), type);
        if (userGift == null || userGift.getGiftNum() < 1) {
            throwExp("礼包数量不足");
        }
        userGiftService.useGift(myId, type);
        if (type == 1) {
            useSmallGift(user.getId());
        } else {
            useBigGift(user.getId());
        }
        return params;
    }


    public void useSmallGift(Long userId) {
        userRoleService.addUserRole(userId, 1L, 30);
    }

    public void useBigGift(Long userId) {
        for (int i = 1; i <= 5; i++) {
            userRoleService.addUserRole(userId, (long) i, 30);
        }
    }


    @Transactional
    @ServiceMethod(code = "003", description = "进入场景查看角色工作信息")
    public Object findWorkingRoles(final AppSocket appSocket, Command appCommand, JSONObject params) {
        Long userId = appSocket.getWsidBean().getUserId();
        List<UserRole> roles = userRoleService.findWorkingRoles(userId);
        return settleRoleReceive(roles);
    }

    @Transactional
    @ServiceMethod(code = "004", description = "查看我的角色")
    public Object myRoles(final AppSocket appSocket, Command appCommand, JSONObject params) {
        Long userId = appSocket.getWsidBean().getUserId();
        List<UserRole> roles = userRoleService.findByUserId(userId);
        return settleRoleReceive(roles);
    }

    public List<UserRole> settleRoleReceive(List<UserRole> roles) {
        List<UserRole> needUpdate = new ArrayList<>();
        for (UserRole userRole : roles) {
            if (userRole.getStatus() == 2) {
                continue;
            }
            Date lastLookTime = userRole.getLastLookTime();
            Date lastReceiveTime = userRole.getLastReceiveTime();
            long hour = (System.currentTimeMillis() - lastReceiveTime.getTime()) / 1000 / 60 / 60;
            if (userRole.getEndTime().getTime() < System.currentTimeMillis()) {
                //角色已到期  则要从到期时间和上次产出时间比较 判断中间隔了几个小时
                hour = (userRole.getEndTime().getTime() - lastReceiveTime.getTime()) / 1000 / 60 / 60;
                userRole.setStatus(2);
            }
            //判断体力是否足够这几个小时的消耗 如果不够 产出的小时数要更少
            DicRole dicRole = DIC_ROLE.get(userRole.getRoleId().toString());
            int oneHourCostHp = dicRole.getCost();
            if (userRole.getHp() < oneHourCostHp * hour) {
                //剩余体力不够
                hour = userRole.getHp() / oneHourCostHp;
            }
            //实际消耗体力
            if (hour > 1) {
                long useHp = hour * oneHourCostHp;
                userRole.setHp((int) (userRole.getHp() - useHp));
                JSONArray reward = dicRole.getReward();
                for (Object o : reward) {
                    JSONObject info = (JSONObject) o;
                    Integer oneHourNumber = info.getInteger("number");
                    info.put("number", oneHourNumber * hour);
                }
                JSONArray nowReward = JSONUtil.mergeJSONArray(userRole.getUnReceive(), reward);
                userRole.setUnReceive(nowReward);
                userRole.setLastReceiveTime(DateUtil.getDateByHour(userRole.getLastReceiveTime(), (int) hour));
                needUpdate.add(userRole);
            }
        }
        userRoleService.batchUpdateUserRole(needUpdate);
        return roles;
    }


    @ServiceMethod(code = "005", description = "设置角色为工作状态")
    public JSONObject working(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("index"),params.get("userRoleId"));
        Long userId = appSocket.getWsidBean().getUserId();
        int index = params.getIntValue("index");
        Long userRoleId = params.getLong("userRoleId");
        UserRole userRole = userRoleService.findByUserRoleId(userRoleId);
        if (userRole==null){
            throwExp("未查询到角色信息");
        }
        if (!Objects.equals(userRole.getUserId(), userId)){
            throwExp("非法请求");
        }
        if (userRole.getStatus()==2){
            throwExp("角色已到期");
        }
        userRole.setIndex(index);
        userRole.setStatus(1);
        userRoleService.updateUserRole(userRole);
        return null;
    }


    @ServiceMethod(code = "006", description = "查询可选择的角色")
    public Object getNoWorkingRoles(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        //checkNull(params.get("index"));
        Long userId = appSocket.getWsidBean().getUserId();
        return userRoleService.findNoWorkingRoles(userId);
    }

    @ServiceMethod(code = "007", description = "补充体力")
    public Object addHp(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        Long userId = appSocket.getWsidBean().getUserId();
        params.put("userId",userId);
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("400001", params).build(), new RequestManagerListener(appCommand));
        return async();
    }

    @ServiceMethod(code = "008", description = "领取产出道具")
    public Object receiveItem(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("userRoleId"));
        Long userId = appSocket.getWsidBean().getUserId();
        params.put("userId",userId);
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("400002", params).build(), new RequestManagerListener(appCommand));
        return async();
    }

}

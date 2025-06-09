package com.zywl.app.manager.service.manager;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.Config;
import com.zywl.app.base.bean.DicRole;
import com.zywl.app.base.bean.UserRole;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.enmus.LogUserBackpackTypeEnum;
import com.zywl.app.defaultx.service.UserRoleService;
import com.zywl.app.manager.context.MessageCodeContext;
import com.zywl.app.manager.service.PlayGameService;
import com.zywl.app.manager.socket.AdminSocketServer;
import com.zywl.app.manager.socket.ManagerSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;


@Service
@ServiceClass(code = MessageCodeContext.USER_ROLE)
public class ManagerUserRoleService extends BaseService {

    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private PlayGameService gameService;

    @Autowired
    private ManagerGameBaseService managerGameBaseService;

    @Autowired
    private ManagerConfigService managerConfigService;

    @Transactional
    @ServiceMethod(code = "001", description = "恢复角色体力")
    public  JSONObject addHp(ManagerSocketServer adminSocketServer,  JSONObject data) {
        checkNull(data);
        checkNull(data.get("userId"),data.get("userRoleId"),data.get("number"));
        Long userId = data.getLong("userId");
        Long userRoleId = data.getLong("userRoleId");
        int number = data.getIntValue("number");
        gameService.checkUserItemNumber(userId,"30",number);
        gameService.updateUserBackpack(userId,"30",-number, LogUserBackpackTypeEnum.use);
        int oneItemAddHp = managerConfigService.getInteger(Config.ADD_HP_WFSB);
        int allHp = oneItemAddHp*number;
        UserRole userRole = userRoleService.findByUserRoleId(userRoleId);
        DicRole dicRole = PlayGameService.DIC_ROLE.get(userRole.getRoleId().toString());
        int maxHp = dicRole.getHp();
        if (allHp+userRole.getHp()>maxHp){
            userRole.setHp(maxHp);
        }else {
            userRole.setHp(allHp);
        }
        JSONObject result = new JSONObject();
        result.put("userRole",userRole);
        return result;
    }

    @Transactional
    @ServiceMethod(code = "002", description = "领取产出道具")
    public  JSONObject receiveItem(ManagerSocketServer adminSocketServer, JSONObject data) {
        checkNull(data);
        checkNull(data.get("userId"),data.get("userRoleId"));
        Long userRoleId = data.getLong("userRoleId");
        Long userId = data.getLong("userId");
        UserRole userRole = userRoleService.findByUserRoleId(userRoleId);
        if (!Objects.equals(userRole.getUserId(), userId)){
            throwExp("非法请求");
        }
        gameService.addReward(userId,userRole.getUnReceive(),null);
        userRole.setUnReceive(new JSONArray());
        userRoleService.updateUserRole(userRole);
        JSONObject result = new JSONObject();
        result.put("userRole",userRole);
        return result;
    }
}

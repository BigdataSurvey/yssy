package com.zywl.app.manager.service.manager;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.Config;
import com.zywl.app.base.bean.DicRole;
import com.zywl.app.base.bean.User;
import com.zywl.app.base.bean.UserRole;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.LockUtil;
import com.zywl.app.defaultx.annotation.KafkaProducer;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.enmus.LogUserBackpackTypeEnum;
import com.zywl.app.defaultx.service.UserRoleService;
import com.zywl.app.manager.context.KafkaEventContext;
import com.zywl.app.manager.context.KafkaTopicContext;
import com.zywl.app.manager.context.MessageCodeContext;
import com.zywl.app.manager.service.PlayGameService;
import com.zywl.app.manager.socket.AdminSocketServer;
import com.zywl.app.manager.socket.ManagerSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
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
    private UserCacheService userCacheService;

    @Autowired
    private ManagerConfigService managerConfigService;

    @Transactional
    @ServiceMethod(code = "001", description = "恢复角色体力")
    @KafkaProducer(topic = KafkaTopicContext.RED_POINT, event = KafkaEventContext.ADD_HP, sendParams = true)
    public  JSONObject addHp(ManagerSocketServer adminSocketServer,  JSONObject data) {
        checkNull(data);
        checkNull(data.get("userId"),data.get("userRoleId"),data.get("number"));
        Long userId = data.getLong("userId");
        synchronized (LockUtil.getlock(userId)){
            Long userRoleId = data.getLong("userRoleId");
            int number = data.getIntValue("number");
            gameService.checkUserItemNumber(userId,"5",number);
            int oneItemAddHp = managerConfigService.getInteger(Config.ADD_HP_WFSB);
            int allHp = oneItemAddHp*number;
            UserRole userRole = userRoleService.findByUserIdAndRoleId(userId,userRoleId);
            if (userRole==null){
                throwExp("未查询到角色信息");
            }
            if (userRole.getStatus()==0){
                throwExp("角色尚未开始工作，无需补充体力");
            }
            if (userRole.getEndTime().getTime()<System.currentTimeMillis()){
                throwExp("角色已到期，请重新激活礼包");
            }
            gameService.updateUserBackpack(userId,"5",-number, LogUserBackpackTypeEnum.addHp);
            if (userRole.getHp()==0){
                userRole.setLastReceiveTime(new Date());
            }
            DicRole dicRole = PlayGameService.DIC_ROLE.get(userRole.getRoleId().toString());
            int maxHp = dicRole.getHp();
            if (allHp+userRole.getHp()>maxHp){
                userRole.setHp(maxHp);
            }else {
                userRole.setHp(userRole.getHp()+allHp);
            }
            User user = userCacheService.getUserInfoById(userId);
            if (user.getParentId()!=null){
                gameService.addParentGetAnima(userId,user.getParentId().toString(),new BigDecimal("0.25").multiply(new BigDecimal(number)));
            }
            if (user.getGrandfaId()!=null){
                gameService.addGrandfaGetAnima(userId,user.getGrandfaId().toString(),new BigDecimal("0.15").multiply(new BigDecimal(number)));
            }
            userRoleService.updateUserRole(userRole);
            JSONObject result = new JSONObject();
            result.put("userRole",userRole);
            return result;
        }

    }

    @Transactional
    @ServiceMethod(code = "002", description = "领取产出道具")
    public  JSONObject receiveItem(ManagerSocketServer adminSocketServer, JSONObject data) {
        checkNull(data);
        checkNull(data.get("userId"),data.get("userRoleId"));
        Long userRoleId = data.getLong("userRoleId");
        Long userId = data.getLong("userId");
        synchronized (LockUtil.getlock(userId)){
            UserRole userRole = userRoleService.findByUserIdAndRoleId(userId,userRoleId);
            if (userRole==null){
                throwExp("未查询到角色信息");
            }
            JSONArray reward = userRole.getUnReceive();
            gameService.addReward(userId,reward,null);
            userRole.setUnReceive(new JSONArray());
            userRoleService.updateUserRole(userRole);
            JSONObject result = new JSONObject();
            result.put("rewards",reward);
            return result;
        }
    }

    @Transactional
    @ServiceMethod(code = "003", description = "免费角色角色体力")
    public  JSONObject addHpFree(ManagerSocketServer adminSocketServer,  JSONObject data) {
        checkNull(data);
        checkNull(data.get("userId"));
        Long userId = data.getLong("userId");
        synchronized (LockUtil.getlock(userId)){
            Long userRoleId = 6L;
            int allHp = 1;
            UserRole userRole = userRoleService.findByUserIdAndRoleId(userId,userRoleId);
            if (userRole==null){
                throwExp("未查询到角色信息");
            }
            if (userRole.getStatus()==0){
                throwExp("角色尚未开始工作，无需补充体力");
            }
            if (userRole.getEndTime().getTime()<System.currentTimeMillis()){
                throwExp("角色已到期，请重新领取角色");
            }
            if (userRole.getHp()==0){
                userRole.setLastReceiveTime(new Date());
            }
            DicRole dicRole = PlayGameService.DIC_ROLE.get(userRole.getRoleId().toString());
            int maxHp = dicRole.getHp();
            if (allHp+userRole.getHp()>maxHp){
                userRole.setHp(maxHp);
            }else {
                userRole.setHp(userRole.getHp()+allHp);
            }
            User user = userCacheService.getUserInfoById(userId);
            if (user.getParentId()!=null){
                gameService.addParentGetAnima(userId,user.getParentId().toString(),new BigDecimal("0.01"));
            }
            if (user.getGrandfaId()!=null){
                gameService.addGrandfaGetAnima(userId,user.getGrandfaId().toString(),new BigDecimal("0.005"));
            }
            userRoleService.updateUserRole(userRole);
            JSONObject result = new JSONObject();
            result.put("userRole",userRole);
            return result;
        }
    }

    @Transactional
    @ServiceMethod(code = "004", description = "领取免费角色")
    @KafkaProducer(topic = KafkaTopicContext.RED_POINT, event = KafkaEventContext.ADD_HP, sendParams = true)
    public  JSONObject receiveFreeRole(ManagerSocketServer adminSocketServer,  JSONObject data) {
        Long userId = data.getLong("userId");
        User user = userCacheService.getUserInfoById(userId);
        synchronized (LockUtil.getlock(userId)){
            UserRole freeUserRole = userRoleService.findByUserIdAndRoleId(userId, 6L);
            if (freeUserRole!=null){
                throwExp("已经领取过该角色了");
            }
            userRoleService.addUserRoleFree(userId,6L,30);
        }
        if (user.getParentId()!=null){
            gameService.addParentGetAnima(userId,user.getParentId().toString(),BigDecimal.ONE);
        }
        if (user.getGrandfaId()!=null){
            gameService.addGrandfaGetAnima(userId,user.getGrandfaId().toString(),new BigDecimal("0.5"));
        }
        return data;
    }


}

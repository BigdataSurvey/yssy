package com.zywl.app.manager.service.manager;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.*;
import com.zywl.app.base.bean.shoop.ShopManager;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.base.util.LockUtil;
import com.zywl.app.defaultx.annotation.KafkaProducer;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.cache.GameCacheService;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.cache.UserCapitalCacheService;
import com.zywl.app.defaultx.enmus.LogUserBackpackTypeEnum;
import com.zywl.app.defaultx.enmus.UserCapitalTypeEnum;
import com.zywl.app.defaultx.service.*;
import com.zywl.app.manager.context.KafkaEventContext;
import com.zywl.app.manager.context.KafkaTopicContext;
import com.zywl.app.manager.context.MessageCodeContext;
import com.zywl.app.manager.service.PlayGameService;
import com.zywl.app.manager.socket.ManagerSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@ServiceClass(code = MessageCodeContext.USER_ROLE)
public class ManagerUserRoleService extends BaseService {

    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private UserRoleAdService userRoleAdService;

    @Autowired
    private PlayGameService gameService;

    @Autowired
    private ManagerGameBaseService managerGameBaseService;

    @Autowired
    private UserCacheService userCacheService;

    @Autowired
    private UserCapitalCacheService userCapitalCacheService;

    @Autowired
    private ManagerConfigService managerConfigService;

    @Autowired
    private UserCapitalService userCapitalService;

    @Autowired
    private GameCacheService gameCacheService;

    @Autowired
    private ConfigService configService;

    @Autowired
    private CashRecordService cashRecordService;

    @Autowired
    private UserService userService;

    @Autowired
    private ShopManagerService shopManagerService;





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
            UserRoleAd byUserIdAndYmd = userRoleAdService.findByUserIdAndYmd(userId);
            User user = userCacheService.getUserInfoById(userId);
            if (byUserIdAndYmd.getCanLook()<1){
                throwExp("请等待广告刷新");
            }
            if (byUserIdAndYmd.getLook()==10){
                throwExp("今日次数已用完。");
            }
            if (userRole==null){
                throwExp("未查询到角色信息");
            }
            if (userRole.getStatus()==0){
                throwExp("角色尚未开始工作，无需补充体力");
            }
            if (userRole.getEndTime().getTime()<System.currentTimeMillis()){
                throwExp("角色已到期，请重新领取角色");
            }
            if (userRole.getHp()<10){
                userRole.setLastReceiveTime(new Date());
            }
            DicRole dicRole = PlayGameService.DIC_ROLE.get(userRole.getRoleId().toString());
            int maxHp = dicRole.getHp();
            if (allHp+userRole.getHp()>maxHp){
                userRole.setHp(maxHp);
            }else {
                userRole.setHp(userRole.getHp()+allHp);
            }
            if (user.getVip2()==0){
                if (user.getParentId()!=null){
                    gameService.addParentGetAnima(userId,user.getParentId().toString(),new BigDecimal("0.01"));
                }
                if (user.getGrandfaId()!=null){
                    gameService.addGrandfaGetAnima(userId,user.getGrandfaId().toString(),new BigDecimal("0.005"));
                }
            }

            byUserIdAndYmd.setCanLook(byUserIdAndYmd.getCanLook()-1);
            byUserIdAndYmd.setLook(byUserIdAndYmd.getLook()+1);
            userRoleAdService.update(byUserIdAndYmd);
            if (byUserIdAndYmd.getLook()==10 && user.getVip2()==0){
                //看到第10次的时候 判断  玩家是不是第一天  如果是 并且没激活礼包的给上级返1个通宝
                List<UserRoleAd> byUserId = userRoleAdService.findByUserId(userId);
                if (byUserId.size()==1){
                    if (user.getParentId()!=null){
                        gameService.addParentGetAnima(userId,user.getParentId().toString(),BigDecimal.ONE);
                    }
                    if (user.getGrandfaId()!=null){
                        gameService.addGrandfaGetAnima(userId,user.getGrandfaId().toString(),new BigDecimal("0.5"));
                    }
                }
            }
            userRoleService.updateUserRole(userRole);
            JSONObject result = new JSONObject();
            result.put("userRole",userRole);
            return result;
        }
    }

    @Transactional
    @ServiceMethod(code = "004", description = "领取免费角色")
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

        return data;
    }




    @Transactional
    @ServiceMethod(code = "005", description = "购买角色")
    public  JSONObject buyRole(ManagerSocketServer adminSocketServer,  JSONObject data) {
        Long userId = data.getLong("userId");
        synchronized (LockUtil.getlock(userId)){
            Long roleId = data.getLong("roleId");
            BigDecimal price = managerConfigService.getBigDecimal(Config.GIFT_PRICE_1_GAME);
            managerGameBaseService.checkBalance(userId,price, UserCapitalTypeEnum.currency_2);
            UserRole byUserIdAndRoleId = userRoleService.findByUserIdAndRoleId(userId, roleId);
            if (byUserIdAndRoleId!=null){
                if (byUserIdAndRoleId.getEndTime().getTime()<System.currentTimeMillis()){
                    //早就结束了 直接从现在 猛续一个月
                    byUserIdAndRoleId.setEndTime(DateUtil.getDateByDay(30));
                    byUserIdAndRoleId.setStatus(1);
                }else {
                    byUserIdAndRoleId.setEndTime(DateUtil.getDateByDay(byUserIdAndRoleId.getEndTime(),30));
                }
                userRoleService.updateUserRole(byUserIdAndRoleId);
            }else{
                UserRole userRole = userRoleService.addUserRole(userId, roleId, 30);
            }
            userCapitalService.subBalanceByGift(price, userId, null, null);
            managerGameBaseService.pushCapitalUpdate(userId,UserCapitalTypeEnum.currency_2.getValue());
            gameCacheService.addPoint(userId,1);
        }
        return data;
    }

    @Transactional
    @ServiceMethod(code = "006", description = "申请成为店长")
    public  JSONObject buyShoopManager(ManagerSocketServer adminSocketServer,  JSONObject data) {
        checkNull(data.get("userId"));
        Long userId = data.getLong("userId");
        User user = userCacheService.getUserInfoById(userId);
        synchronized (LockUtil.getlock(userId)){
            String url = data.getString("");
            ShopManager userEntity = shopManagerService.findByUserId(userId);
            if (userEntity!=null){
                throwExp("申请成为店长,请耐心等待审核！");
            }
            BigDecimal price = managerConfigService.getBigDecimal(Config.SHOP_MANAGER);
            //获取账户余额
            managerGameBaseService.checkBalance(userId,price,UserCapitalTypeEnum.currency_2);
            //扣除金额
            userCapitalService.subShopManager(price, userId, null, null);
            //推送用户余额变动
            managerGameBaseService.pushCapitalUpdate(userId,UserCapitalTypeEnum.currency_2.getValue());
            ShopManager shopManager = new ShopManager();
            shopManager.setUserId(userId);
            shopManager.setNickName(user.getName());
            shopManager.setHeadImageUrl(user.getHeadImageUrl());
            shopManager.setUserNo(user.getUserNo());
            shopManager.setWechat(user.getWechatId());
            shopManager.setQq(user.getQq());
            shopManager.setUserAddress(url);
            shopManager.setStatus(1);
            shopManagerService.addShopManager(shopManager);
            JSONObject result = new JSONObject();
            result.put("price",price);
            }
        return new JSONObject();
    }






}

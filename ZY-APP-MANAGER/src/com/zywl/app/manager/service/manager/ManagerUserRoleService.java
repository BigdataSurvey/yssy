package com.zywl.app.manager.service.manager;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.*;
import com.zywl.app.base.bean.hongbao.DicPrizeCard;
import com.zywl.app.base.bean.jingang.BellRecord;
import com.zywl.app.base.bean.shoop.ShopManager;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.base.util.LockUtil;
import com.zywl.app.defaultx.annotation.KafkaProducer;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.cache.GameCacheService;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.enmus.ItemIdEnum;
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

import java.math.BigDecimal;
import java.util.*;


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
    private ManagerConfigService managerConfigService;

    @Autowired
    private UserCapitalService userCapitalService;

    @Autowired
    private GameCacheService gameCacheService;

    @Autowired
    private DicPrizeCardService  dicPrizeCardService;

    @Autowired
    private ShopManagerService shopManagerService;


    @Autowired
    private KongKimBellService kongkimBellService;





    @Transactional
    @ServiceMethod(code = "001", description = "恢复角色体力")
    @KafkaProducer(topic = KafkaTopicContext.RED_POINT, event = KafkaEventContext.ADD_HP, sendParams = true)
    public  JSONObject addHp(ManagerSocketServer adminSocketServer,  JSONObject data) {
        checkNull(data);
        checkNull(data.get("userId"),data.get("userRoleId"),data.get("number"),data.get("itemId"));
        Long userId = data.getLong("userId");
        synchronized (LockUtil.getlock(userId)){
            Long userRoleId = data.getLong("userRoleId");
            int number = data.getIntValue("number");
            String itemId = data.getString("itemId");
            if (!itemId.equals("50") && !itemId.equals("51") && !itemId.equals("52")  ){
                throwExp("道具id有误");
            }
            gameService.checkUserItemNumber(userId,itemId,number);
            double oneItemAddHp = managerConfigService.getDouble(Config.ADD_HP_WFSB);
            double allHp = oneItemAddHp*number;
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
            gameService.updateUserBackpack(userId,itemId,-number, LogUserBackpackTypeEnum.addHp);
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
            User user = userCacheService.getUserInfoById(userId);
            if (user.getParentId()!=null){
                gameService.addParentGetAnima(userId,user.getParentId().toString(),new BigDecimal("0.011").multiply(new BigDecimal(number)));
            }
            if (user.getGrandfaId()!=null){
                gameService.addGrandfaGetAnima(userId,user.getGrandfaId().toString(),new BigDecimal("0.006").multiply(new BigDecimal(number)));
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
            String url = data.getString("url");
            ShopManager userEntity = shopManagerService.findByUserId(userId);
            if (userEntity!=null && userEntity.getStatus()==2){
                throwExp("已申请成为店长,请耐心等待审核！");
            }
            if (userEntity!=null && userEntity.getStatus()==1){
                throwExp("已经是店长啦，无需重复申请");
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
            shopManager.setStatus(2);
            shopManagerService.addShopManager(shopManager);
            }
        return new JSONObject();
    }



    @Transactional
    @ServiceMethod(code = "007", description = "金刚铃兑换")
    public  JSONObject buyJingGangLing(ManagerSocketServer adminSocketServer,  JSONObject data) {
        checkNull(data.get("userId"));
        Long userId = data.getLong("userId");
        int number = data.getInteger("number");
        synchronized (LockUtil.getlock(userId)){

            BellRecord bellRecord =new BellRecord();
            BigDecimal price = managerConfigService.getBigDecimal(Config.CONVERT_TOTAL);
            //判断积分余额够不够
            managerGameBaseService.checkBalance(userId,price.multiply(BigDecimal.valueOf(number)),UserCapitalTypeEnum.score);
            //扣除金额
            userCapitalService.subJingGangLing(price.multiply(BigDecimal.valueOf(number)), userId, null, bellRecord.getId());
            //增加玩家道具
            gameService.updateUserBackpack(userId, ItemIdEnum.GOLD.getValue(), number, LogUserBackpackTypeEnum.use);
            //积分推送
            managerGameBaseService.pushCapitalUpdate(userId,UserCapitalTypeEnum.score.getValue());
            //插兑换记录
            bellRecord.setUserId(userId);
            bellRecord.setConverCount(BigDecimal.valueOf(number));
            bellRecord.setConsumeTotal(price.multiply(BigDecimal.valueOf(number)));
            bellRecord.setCreateTime(new Date());
            kongkimBellService.addKongkimBell(bellRecord);
        }
        return new JSONObject();
    }






    @Transactional
    @ServiceMethod(code = "008", description = "翻拍游戏")
    @KafkaProducer(topic = KafkaTopicContext.RED_POINT,event = KafkaEventContext.QSHJ,sendParams = true)
    public  JSONObject buyFanPai(ManagerSocketServer adminSocketServer,  JSONObject data) {
        checkNull(data.get("userId"));
        Long userId = data.getLong("userId");
        int number = data.getInteger("number");
        if (PlayGameService.PRIZE_IDS.size()<number){
            throwExp("奖池奖品数量不足");
        }
        //游园卷兑换比例
        BigDecimal price = managerConfigService.getBigDecimal(Config.YYB_CONVERT_RATE);
        //判断余额够不够
        managerGameBaseService.checkBalance(userId,price,UserCapitalTypeEnum.yyb);
        //扣除金额
        userCapitalService.subFanPai(price.multiply(BigDecimal.valueOf(number)), userId, null, null);
        JSONObject object = new JSONObject();
        List<DicPrizeCard> prizeList = new ArrayList<>();
        for (int i = 0; i < number; i++) {
          Long num = PlayGameService.PRIZE_IDS.remove(0);
          prizeList.add(PlayGameService.DIC_PRIZE.get(num.toString()));
            //更改数据库的当前数量
            dicPrizeCardService.updatePrizeTotal(num);
            //获取完奖品id之后  要扣除奖池数量
            PlayGameService.DIC_PRIZE.get(num.toString()).setNumTotal(PlayGameService.DIC_PRIZE.get(num.toString()).getNumTotal()-1);
        }
        BigDecimal priceNum =  BigDecimal.ZERO;
        for (DicPrizeCard prize : prizeList) {
            priceNum=priceNum.add(prize.getType());
        }
        userCapitalService.addUseeBalancePrize(priceNum,userId,null,null,UserCapitalTypeEnum.yyb.getValue());
        //推送用户余额变动
        managerGameBaseService.pushCapitalUpdate(userId,UserCapitalTypeEnum.yyb.getValue());
        object.put("prizeList", prizeList);
        return object;
    }

    @Transactional
    @ServiceMethod(code = "009", description = "当前总数量")
    public  Object buyFanPaiToal(ManagerSocketServer adminSocketServer,  JSONObject data) {
        checkNull(data.get("userId"));
        return PlayGameService.DIC_PRIZE.values();
    }



}

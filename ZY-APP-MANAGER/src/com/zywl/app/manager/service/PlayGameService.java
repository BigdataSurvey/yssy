package com.zywl.app.manager.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.bean.PushBean;
import com.live.app.ws.enums.PushCode;
import com.live.app.ws.socket.BaseSocket;
import com.live.app.ws.util.DefaultPushHandler;
import com.live.app.ws.util.Push;
import com.zywl.app.base.bean.*;
import com.zywl.app.base.bean.card.*;
import com.zywl.app.base.bean.vo.AchievementVo;
import com.zywl.app.base.constant.RedisKeyConstant;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.BeanUtils;
import com.zywl.app.base.util.LockUtil;
import com.zywl.app.defaultx.annotation.KafkaProducer;
import com.zywl.app.defaultx.cache.AppConfigCacheService;
import com.zywl.app.defaultx.cache.GameCacheService;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.cache.UserCapitalCacheService;
import com.zywl.app.defaultx.cache.card.CardGameCacheService;
import com.zywl.app.defaultx.enmus.*;
import com.zywl.app.defaultx.service.*;
import com.zywl.app.defaultx.service.card.DicDrawProbabilityService;
import com.zywl.app.defaultx.service.card.DicMineService;
import com.zywl.app.defaultx.service.card.DicShopService;
import com.zywl.app.manager.context.KafkaEventContext;
import com.zywl.app.manager.context.KafkaTopicContext;
import com.zywl.app.manager.service.manager.ManagerGameBaseService;
import com.zywl.app.manager.socket.ManagerSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 初始化游戏数据
 *
 * @author 1
 */
@Service
public class PlayGameService extends BaseService {


    public static Map<String, Card> CARD_INFO = new ConcurrentHashMap<>();

    public static Set<String> ARTIFACT_ID = new HashSet<>();


    public static Map<String, DicMine> DIC_MINE = new ConcurrentHashMap<>();

    public static final Map<String, DicRole> DIC_ROLE = new ConcurrentHashMap<>();


    public static Map<String, UserCapital> playercoins = new ConcurrentHashMap<>();
    public static Map<String, User> channelUser = new ConcurrentHashMap<>();


    //玩家背包信息
    public static Map<String, Map<String, Backpack>> playerItems = new ConcurrentHashMap<>();


    //道具信息
    public static Map<String, Item> itemMap = new ConcurrentHashMap<>();
    public final static Map<String, DicVip> DIC_VIP_MAP = new ConcurrentHashMap<>();


    public static Map<String, PrizeDrawReward> prizeDrawRewardInfo = new ConcurrentHashMap<>();

    public static Map<String, DailyTask> dailyTaskInfo = new ConcurrentHashMap<>();

    public static Map<String, ChannelIncome> channelIncomeMap = new ConcurrentHashMap<>();

    public static Map<String, GiveParentIncome> parentIncomeMap = new ConcurrentHashMap<>();

    public static Map<String, GiveGrandfaIncome> grandfaIncomeMap = new ConcurrentHashMap<>();

    public static Map<String, UserStatistic> userStatisticMap = new ConcurrentHashMap<>();

    public static Map<String, Product> productMap = new ConcurrentHashMap<>();

    public static Map<String, Achievement> achievementMap = new ConcurrentHashMap<>();

    public static Map<String, Map<String, DicShop>> DIC_SHOP_MAP = new ConcurrentHashMap<>();
    public static Map<String, List<DicShop>> DIC_SHOP_LIST = new ConcurrentHashMap<>();
    public static Map<String, UserAchievement> userAchievementMap = new ConcurrentHashMap<>();


    @Autowired
    private UserAchievementService userAchievementService;


    @Autowired
    private UserCapitalService userCapitalService;

    @Autowired
    private GameCacheService gameCacheService;


    @Autowired
    private ItemService itemService;

    @Autowired
    private AchievementService achievementService;

    @Autowired
    private DicDrawProbabilityService dicDrawProbabilityService;

    @Autowired
    private DicShopService dicShopService;
    @Autowired
    private DicVipService dicVipService;

    @Autowired
    private AppConfigCacheService appConfigCacheService;


    @Autowired
    private BackpackService backpackService;


    @Autowired
    private UserCacheService userCacheService;

    @Autowired
    private DailyTaskService dailyTaskService;
    @Autowired
    private UserCapitalCacheService userCapitalCacheService;


    @Autowired
    private UserStatisticService userStatisticService;

    @Autowired
    private ManagerGameBaseService managerGameBaseService;


    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;


    @Autowired
    private GiveParentIncomeService giveParentIncomeService;

    @Autowired
    private ChannelIncomeService channelIncomeService;

    @Autowired
    private GiveGrandfaIncomeService giveGrandfaIncomeService;


    @Autowired
    private DicMineService dicMineService;

    @Autowired
    private DicRoleService dicRoleService;


    @Autowired
    private JDCardService jdCardService;

    @PostConstruct
    public void _InitGameInfoService() {
        Push.addPushSuport(PushCode.updateAdCount, new DefaultPushHandler() {
            public void onRegist(BaseSocket baseSocket, PushBean pushBean) {
                if (baseSocket instanceof ManagerSocketServer) {
                    ManagerSocketServer managerSocketServer = (ManagerSocketServer) baseSocket;
                    pushBean.setCondition(managerSocketServer.getId());
                }
            }
        });
        Push.addPushSuport(PushCode.updatePlayerPl, new DefaultPushHandler() {
            public void onRegist(BaseSocket baseSocket, PushBean pushBean) {
                if (baseSocket instanceof ManagerSocketServer) {
                    ManagerSocketServer managerSocketServer = (ManagerSocketServer) baseSocket;
                    pushBean.setCondition(managerSocketServer.getId());
                }
            }
        });
        Push.addPushSuport(PushCode.updateUserInfo, new DefaultPushHandler() {
            public void onRegist(BaseSocket baseSocket, PushBean pushBean) {
                if (baseSocket instanceof ManagerSocketServer) {
                    ManagerSocketServer managerSocketServer = (ManagerSocketServer) baseSocket;
                    pushBean.setCondition(managerSocketServer.getId());
                }
            }
        });

        Push.addPushSuport(PushCode.updatePlayerMp, new DefaultPushHandler() {
            public void onRegist(BaseSocket baseSocket, PushBean pushBean) {
                if (baseSocket instanceof ManagerSocketServer) {
                    ManagerSocketServer managerSocketServer = (ManagerSocketServer) baseSocket;
                    pushBean.setCondition(managerSocketServer.getId());
                }
            }
        });
        Push.addPushSuport(PushCode.updatePlayer, new DefaultPushHandler() {
            public void onRegist(BaseSocket baseSocket, PushBean pushBean) {
                if (baseSocket instanceof ManagerSocketServer) {
                    ManagerSocketServer managerSocketServer = (ManagerSocketServer) baseSocket;
                    pushBean.setCondition(managerSocketServer.getId());
                }
            }
        });

        Push.addPushSuport(PushCode.redPackageInfo, new DefaultPushHandler() {
            public void onRegist(BaseSocket baseSocket, PushBean pushBean) {
                if (baseSocket instanceof ManagerSocketServer) {
                    ManagerSocketServer managerSocketServer = (ManagerSocketServer) baseSocket;
                    pushBean.setCondition(managerSocketServer.getId());
                }
            }
        });
        Push.addPushSuport(PushCode.updateUserPower, new DefaultPushHandler() {
            public void onRegist(BaseSocket baseSocket, PushBean pushBean) {
                if (baseSocket instanceof ManagerSocketServer) {
                    ManagerSocketServer managerSocketServer = (ManagerSocketServer) baseSocket;
                    pushBean.setCondition(managerSocketServer.getId());
                }
            }
        });

        Push.addPushSuport(PushCode.redPointShow, new DefaultPushHandler() {
            public void onRegist(BaseSocket baseSocket, PushBean pushBean) {
                if (baseSocket instanceof ManagerSocketServer) {
                    ManagerSocketServer managerSocketServer = (ManagerSocketServer) baseSocket;
                    pushBean.setCondition(managerSocketServer.getId());
                }
            }
        });

        Push.addPushSuport(PushCode.updateRoleCard, new DefaultPushHandler() {
            public void onRegist(BaseSocket baseSocket, PushBean pushBean) {
                if (baseSocket instanceof ManagerSocketServer) {
                    ManagerSocketServer managerSocketServer = (ManagerSocketServer) baseSocket;
                    pushBean.setCondition(managerSocketServer.getId());
                }
            }
        });
        Push.addPushSuport(PushCode.updateRoleCardAll, new DefaultPushHandler() {
            public void onRegist(BaseSocket baseSocket, PushBean pushBean) {
                if (baseSocket instanceof ManagerSocketServer) {
                    ManagerSocketServer managerSocketServer = (ManagerSocketServer) baseSocket;
                    pushBean.setCondition(managerSocketServer.getId());
                }
            }
        });

        Push.addPushSuport(PushCode.redPointHide, new DefaultPushHandler() {
            public void onRegist(BaseSocket baseSocket, PushBean pushBean) {
                if (baseSocket instanceof ManagerSocketServer) {
                    ManagerSocketServer managerSocketServer = (ManagerSocketServer) baseSocket;
                    pushBean.setCondition(managerSocketServer.getId());
                }
            }
        });

        initItem();
        initProduct();
        initDailyTask();
        initIncome();
        initAchievement();
        initMine();
        initShop();
        initRole();
        initDicVip();
    }


    public void initShop() {
        List<DicShop> allShop = dicShopService.findAllShop();
        for (DicShop dicShop : allShop) {
            Map<String, DicShop> orDefault = DIC_SHOP_MAP.getOrDefault(String.valueOf(dicShop.getShopType()), new ConcurrentHashMap<>());
            orDefault.put(dicShop.getId().toString(), dicShop);
            if (!DIC_SHOP_MAP.containsKey(String.valueOf(dicShop.getShopType()))) {
                DIC_SHOP_MAP.put(String.valueOf(dicShop.getShopType()), orDefault);
            }

            List<DicShop> shops = DIC_SHOP_LIST.getOrDefault(String.valueOf(dicShop.getShopType()), new ArrayList<>());
            shops.add(dicShop);
            if (!DIC_SHOP_LIST.containsKey(String.valueOf(dicShop.getShopType()))) {
                DIC_SHOP_LIST.put(String.valueOf(dicShop.getShopType()), shops);
            }
        }
    }

    public void initAchievement() {
        List<Achievement> achievements = achievementService.findAll();
        achievements.forEach(e -> achievementMap.put(e.getId().toString(), e));
    }

    public void initIncome() {
        List<GiveParentIncome> allIncome = giveParentIncomeService.findAllIncome();
        allIncome.forEach(e -> parentIncomeMap.put(e.getId().toString(), e));
        List<GiveGrandfaIncome> allIncome2 = giveGrandfaIncomeService.findAllIncome();
        allIncome2.forEach(e -> grandfaIncomeMap.put(e.getId().toString(), e));
        List<ChannelIncome> allIncome3 = channelIncomeService.findAllChannelIncome();
        allIncome3.forEach(e -> channelIncomeMap.put(String.valueOf(e.getTier()), e));
    }

    public void initDicVip() {
        List<DicVip> allVip = dicVipService.findAllVip();
        allVip.forEach(e -> DIC_VIP_MAP.put(String.valueOf(e.getLv()), e));
    }

    public void initProduct() {
        List<Product> allProduct = productService.findAllProduct();
        allProduct.forEach(e -> productMap.put(e.getId().toString(), e));
    }

    public void initDailyTask() {
        dailyTaskInfo.clear();
        List<DailyTask> allDailyTask = dailyTaskService.findAllDailyTask();
        allDailyTask.forEach(e -> dailyTaskInfo.put(e.getId().toString(), e));
    }

    public void initItem() {
        itemMap.clear();
        List<Item> items = itemService.findAll();
        items.forEach(e -> itemMap.put(e.getId().toString(), e));
    }


    public void initMine() {
        DIC_MINE.clear();
        logger.info("初始化矿产相关信息");
        List<DicMine> list = dicMineService.findAllMine();
        list.forEach(e -> DIC_MINE.put(String.valueOf(e.getIndex()), e));
        logger.info("初始化矿产信息完成,加载数据数量：" + DIC_MINE.size());
    }

    public void initRole() {
        DIC_ROLE.clear();
        logger.info("初始化角色相关信息");
        List<DicRole> allRole = dicRoleService.findAllRole();
        allRole.forEach(e -> DIC_ROLE.put(e.getId().toString(), e));
        logger.info("初始化角色信息完成,加载数据数量：" + DIC_MINE.size());
    }


    public void updateStatic() {
        try {
            long time = System.currentTimeMillis();
            logger.info("=========用户统计数据更新至数据库==========");
            List<UserStatistic> list = new ArrayList<>();
            userStatisticMap.values().stream().forEach(e -> list.add(e));
            userStatisticService.batchUpdateStatic(list);
            logger.info("用户统计数据更新至数据库完成，用时：" + (System.currentTimeMillis() - time) + ",条数：" + list.size());
        } catch (Exception e) {
            logger.info(e);
        }
    }

    public void updateUserAchievement() {
        try {
            long time = System.currentTimeMillis();
            logger.info("=========用户成就数据更新至数据库==========");
            List<UserAchievement> list = new ArrayList<>();
            userAchievementMap.values().stream().forEach(e -> list.add(e));
            userAchievementService.batchUpdateStatic(list);
            userAchievementMap.clear();
            logger.info("用户成就数据更新至数据库完成，用时：" + (System.currentTimeMillis() - time) + ",条数：" + list.size());
        } catch (Exception e) {
            logger.info(e);
        }
    }

    public void updateUserStatic() {
        new Timer("更新用户统计数据，成就信息到数据库").schedule(new TimerTask() {
            public void run() {
                logger.info("更新用户统计数据，成就信息到数据库");
                try {
                    updateUserAchievement();
                } catch (Exception e) {

                }


            }
        }, 1000, 1000 * 60 * 30);
    }


    public User getUserByChannelNo(String channelNo) {
        User user;
        if (channelUser.containsKey(channelNo)) {
            user = channelUser.get(channelNo);
        } else {
            user = userService.findUserByChannelNo(channelNo);
            if (user == null) {
                return null;
            }
            channelUser.put(channelNo, user);
        }
        return user;
    }

    public JSONObject getUserAdCountInfo(Long userId) {
        JSONObject result = new JSONObject();
        AdvertIndexEnum[] values = AdvertIndexEnum.values();
        for (AdvertIndexEnum value : values) {
            result.put(value.toString(), value.getCount() - userCacheService.getUserAdvertLookNum(userId, value.getIndex()));
        }
        return result;
    }


    public void checkAddRedminder(Long userId, RedReminderIndexEnum em) {
        JSONObject obj = new JSONObject();
        List<String> indexList = userCacheService.getPlayerRedReminderList(userId);
        if (indexList.contains(em.getValue())) {
            return;
        }
        indexList.add(em.getValue());
        obj.put("indexList", indexList);
        obj.put("userId", userId);
        userCacheService.setPlayerReminderList(userId, indexList);
        Push.push(PushCode.redReminder, null, obj);
    }

    public Map<String, Backpack> getUserBackpack(Long userId) {
        return getUserBackpack(userId.toString());
    }

    public Map<String, Backpack> getUserBackpack(String userId) {
        if (!playerItems.containsKey(userId)) {
            List<Backpack> list = backpackService.getBackpackByUserId(Long.parseLong(userId));
            Map<String, Backpack> map = new HashMap<>();
            for (Backpack backpack : list) {
                map.put(backpack.getItemId().toString(), backpack);
            }
            playerItems.put(userId, map);
        }
        return playerItems.get(userId);
    }

    public int getUserItemNumber(Long userId, String itemId) {
        if (!playerItems.containsKey(userId)) {
            List<Backpack> list = backpackService.getBackpackByUserId(userId);
            Map<String, Backpack> map = new HashMap<>();
            for (Backpack backpack : list) {
                map.put(backpack.getItemId().toString(), backpack);
            }
            playerItems.put(String.valueOf(userId), map);
        }
        Map<String, Backpack> stringBackpackMap = playerItems.get(userId.toString());
        if (stringBackpackMap.containsKey(itemId)) {
            Backpack backpack = stringBackpackMap.get(itemId);
            return backpack.getItemNumber();
        }
        return 0;
    }

    public void updateUserBackpack(Long userId, String itemId, int number, LogUserBackpackTypeEnum em) {
        updateUserBackpack(userId.toString(), itemId, number, em);
    }

    public void updateUserBackpack(Long userId, String itemId, int number, LogUserBackpackTypeEnum em,String otherUserId) {
        updateUserBackpack(userId.toString(), itemId, number, em,otherUserId);
    }


    public void updateUserBackpackCache(Long userId, String itemId, int number) {
        synchronized (LockUtil.getlock(userId)) {
            if (playerItems.containsKey(userId)) {
                if (playerItems.get(userId).containsKey(itemId)) {
                    playerItems.get(userId).get(itemId).setItemNumber(playerItems.get(userId).get(itemId).getItemNumber() + number);
                }
            }
            managerGameBaseService.pushBackpackUpdate(userId, itemId, number, 1);
        }
    }

    public void updateUserBackpack(String userId, String itemId, int number, LogUserBackpackTypeEnum em) {
        synchronized (LockUtil.getlock(userId)) {
            Map<String, Backpack> map = getUserBackpack(userId);
            int type = 0;
            int beforeNumber = 0;
            Long id = null;
            if (map != null && map.size() > 0) {
                if (map.containsKey(itemId)) {
                    type = 1;
                    beforeNumber = map.get(itemId).getItemNumber();
                    id = map.get(itemId).getId();
                }
            }
            int result;
            if (number < 0) {
                result = backpackService.subItemNumber(Long.parseLong(userId), Long.parseLong(itemId), -number, em, beforeNumber, id);
            } else {
                result = backpackService.addItemNumber(Long.parseLong(userId), Long.parseLong(itemId), number, em, type, beforeNumber, id);
            }
            if (result < 1) {
                userCapitalCacheService.deltedUserCapitalCache(Long.parseLong(userId), UserCapitalTypeEnum.currency_2.getValue());
                playerItems.remove(userId);
                throwExp("道具操作失败,请重试");
            }
            if (playerItems.containsKey(userId)) {
                //TODO
                if (playerItems.get(userId).containsKey(itemId)) {
                    playerItems.get(userId).get(itemId).setItemNumber(playerItems.get(userId).get(itemId).getItemNumber() + number);
                } else {
                    Backpack backpack = new Backpack();
                    backpack.setItemId(Long.parseLong(itemId));
                    backpack.setItemNumber(number);
                    backpack.setCreateTime(new Date());
                    backpack.setUpdateTime(new Date());
                    backpack.setUserId(Long.parseLong(userId));
                    playerItems.get(userId).put(itemId, backpack);
                }
            }
            managerGameBaseService.pushBackpackUpdate(Long.parseLong(userId), itemId, number, 1);
        }
    }

    public void updateUserBackpack(String userId, String itemId, int number, LogUserBackpackTypeEnum em,String otherUserId) {
        synchronized (LockUtil.getlock(userId)) {
            Map<String, Backpack> map = getUserBackpack(userId);
            int type = 0;
            int beforeNumber = 0;
            Long id = null;
            if (map != null && map.size() > 0) {
                if (map.containsKey(itemId)) {
                    type = 1;
                    beforeNumber = map.get(itemId).getItemNumber();
                    id = map.get(itemId).getId();
                }
            }
            int result;
            if (number < 0) {
                result = backpackService.subItemNumber(Long.parseLong(userId), Long.parseLong(itemId), -number, em, beforeNumber, id,otherUserId);
            } else {
                result = backpackService.addItemNumber(Long.parseLong(userId), Long.parseLong(itemId), number, em, type, beforeNumber, id,otherUserId);
            }
            if (result < 1) {
                userCapitalCacheService.deltedUserCapitalCache(Long.parseLong(userId), UserCapitalTypeEnum.currency_2.getValue());
                playerItems.remove(userId);
                throwExp("道具操作失败,请重试");
            }
            if (playerItems.containsKey(userId)) {
                //TODO
                if (playerItems.get(userId).containsKey(itemId)) {
                    playerItems.get(userId).get(itemId).setItemNumber(playerItems.get(userId).get(itemId).getItemNumber() + number);
                } else {
                    Backpack backpack = new Backpack();
                    backpack.setItemId(Long.parseLong(itemId));
                    backpack.setItemNumber(number);
                    backpack.setCreateTime(new Date());
                    backpack.setUpdateTime(new Date());
                    backpack.setUserId(Long.parseLong(userId));
                    playerItems.get(userId).put(itemId, backpack);
                }
            }
            managerGameBaseService.pushBackpackUpdate(Long.parseLong(userId), itemId, number, 1);
        }
    }

    public List<JSONObject> getReturnPack(Long userId) {
        Map<String, Backpack> map = getUserBackpack(userId.toString());
        Set<String> ids = map.keySet();
        JSONArray array = new JSONArray();
        for (String id : ids) {
            if (map.get(id).getItemNumber() == 0) {
                continue;
            }
            JSONObject obj = new JSONObject();
            Long itemId = map.get(id).getItemId();
            obj.put("id", itemId);
            obj.put("number", map.get(id).getItemNumber());
            obj.put("type", PlayGameService.itemMap.get(itemId.toString()).getType());
            array.add(obj);
        }
        List<JSONObject> list = JSON.parseArray(array.toJSONString(), JSONObject.class);
        list.stream().sorted(Comparator.comparingLong(a -> a.getLongValue("itemId")));
        return list;
    }


    public UserStatistic getUserStatistic(String userId) {
        if (userId == null) {
            return null;
        }
        if (userStatisticMap.containsKey(userId)) {
            return userStatisticMap.get(userId);
        } else {
            UserStatistic userStatistic = userStatisticService.findByUserId(Long.parseLong(userId));
            if (userStatistic == null) {
                userStatisticService.addUserStatistic(Long.parseLong(userId));
                userStatistic = userStatisticService.findByUserId(Long.parseLong(userId));
            }
            userStatisticMap.put(userId, userStatistic);
            return userStatistic;
        }
    }

    public void addCreateParentIncome(String userId, BigDecimal amount, String parentId) {
        getUserStatistic(userId);
        userStatisticMap.get(userId).setCreateIncome(userStatisticMap.get(userId).getCreateIncome().add(amount));
        userCacheService.addParentTodayIncome(Long.parseLong(userId), amount);
        addUserGetIncome(parentId, amount);
    }


    public void addCreateGrandfaIncome(String userId, BigDecimal amount, String grandfaId) {
        getUserStatistic(userId);
        userStatisticMap.get(userId).setCreateGrandfaIncome(userStatisticMap.get(userId).getCreateGrandfaIncome().add(amount));
        userCacheService.addGrandfaTodayIncome(Long.parseLong(userId), amount);
        addUserGetIncome(grandfaId, amount);
    }

    public void addParentGetAnima(Long userId, String parentId, BigDecimal amount) {
        getUserStatistic(parentId);
        getUserStatistic(userId.toString());
        userStatisticMap.get(parentId).setGetAnima(userStatisticMap.get(parentId).getGetAnima().add(amount));
        userStatisticMap.get(userId.toString()).setCreateAnima(userStatisticMap.get(userId.toString()).getCreateAnima().add(amount));
        userCacheService.addParentAnima(userId, parentId, amount);
    }

    public void addGrandfaGetAnima(Long userId, String grandfaId, BigDecimal amount) {
        getUserStatistic(grandfaId);
        userStatisticMap.get(grandfaId).setGetAnima(userStatisticMap.get(grandfaId).getGetAnima().add(amount));
        userStatisticMap.get(userId.toString()).setCreateGrandfaAnima(userStatisticMap.get(userId.toString()).getCreateGrandfaAnima().add(amount));
        userCacheService.addGrandfaAnima(userId, grandfaId, amount);
    }

    public void addChannelNoIncome(Long myId, String channelNo) {
        User my = userCacheService.getUserInfoById(myId);
        if (my.getIsChannel() == 1) {
            return;
        }
        User userByChannelNo = getUserByChannelNo(channelNo);
        if (userByChannelNo == null) {
            return;
        }
        getUserStatistic(userByChannelNo.getId().toString());
        BigDecimal income = BigDecimal.ZERO;
        Long count = userCacheService.getUserAdAllLookNum(myId);
        User parentUser = userCacheService.getUserInfoById(my.getParentId());
        if (parentUser == null || userByChannelNo == null || userByChannelNo.getUserNo() == null) {
            return;
        }
        if (userByChannelNo.getUserNo().equals(parentUser.getUserNo())) {
            if (channelIncomeMap.containsKey("1")) {
                income = channelIncomeMap.get("1").getIncomeByNum(Integer.parseInt(count.toString()));
            }
        } else {
            if (channelIncomeMap.containsKey("2")) {
                income = channelIncomeMap.get("2").getIncomeByNum(Integer.parseInt(count.toString()));
            }
        }
        if (income.compareTo(BigDecimal.ZERO) > 0) {
            userStatisticMap.get(userByChannelNo.getId().toString()).setChannelIncome(userStatisticMap.get(userByChannelNo.getId().toString()).getChannelIncome().add(income));
            userStatisticMap.get(userByChannelNo.getId().toString()).setNowChannelIncome(userStatisticMap.get(userByChannelNo.getId().toString()).getNowChannelIncome().add(income));
            BigDecimal sill = new BigDecimal(appConfigCacheService.getConfigByKey(RedisKeyConstant.APP_CONFIG_CHANNEL_CASH_SILL, Config.CHANNEL_CASH_SILL));
            if (userStatisticMap.get(userByChannelNo.getId().toString()).getNowChannelIncome().compareTo(sill) > 0) {
                userStatisticMap.get(userByChannelNo.getId().toString()).setNowChannelIncome(sill);
            }
        }
        userCacheService.addTodayChannelIncome(userByChannelNo.getId(), Double.parseDouble(income.toString()));
    }

    public void addUserGetIncome(String userId, BigDecimal income) {
        getUserStatistic(userId);
        userStatisticMap.get(userId).setGetIncome(userStatisticMap.get(userId).getGetIncome().add(income));
        userStatisticMap.get(userId).setGetAllIncome(userStatisticMap.get(userId).getGetAllIncome().add(income));
    }

    public UserAchievement getUserAchievement(String userId) {
        UserAchievement userAchievement= userAchievementService.findUserAchievement(Long.parseLong(userId));
        return userAchievement;
    }


    @KafkaProducer(topic = KafkaTopicContext.RED_POINT, event = KafkaEventContext.ADD_REWARD, sendParams = true)
    public void addReward(Long userId, JSONArray array, LogCapitalTypeEnum em) {
        for (Object o : array) {
            JSONObject reward = (JSONObject) o;
            int type = reward.getIntValue("type");
            String id = reward.getString("id");
            int number = reward.getIntValue("number");
            if (type == 1) {
                if (id.equals(ItemIdEnum.GOLD.getValue()) || id.equals(ItemIdEnum.YYQ.getValue())) {
                    BigDecimal amount = reward.getBigDecimal("number");
                    if (reward.containsKey("channel")) {
                        int channel = reward.getIntValue("channel");
                        if (channel == MailGoldTypeEnum.PLAY_GAME.getValue()) {
                            em = LogCapitalTypeEnum.play_game;
                        } else if (channel == MailGoldTypeEnum.FRIEND_PLAY_GAME.getValue()) {
                            em = LogCapitalTypeEnum.friend_play_game;
                        } else if (channel == MailGoldTypeEnum.TRADING.getValue()) {
                            em = LogCapitalTypeEnum.sell;
                        }
                    }
                    userCapitalService.addUserBalanceByAddReward(amount, userId, Integer.parseInt(id), em);
                    managerGameBaseService.pushCapitalUpdate(userId, Integer.parseInt(id));
                } else {
                    //正常道具
                    if (!id.equals("1001")) {
                        if ( em!=null && em.getValue()==LogCapitalTypeEnum.mail.getValue()){
                            //邮件
                            updateUserBackpack(userId, id, number, LogUserBackpackTypeEnum.zs,reward.getString("fromUserId"));
                        }else{
                            updateUserBackpack(userId, id, number, LogUserBackpackTypeEnum.game);
                        }

                    } else {
                        JDCard noExchange = jdCardService.findNoExchange();
                        if (noExchange == null) {
                            throwExp("当前已无库存，请联系官方QQ群客服进行库存补充~");
                        }
                        Long jdCardId = noExchange.getId();
                        jdCardService.userExchange(userId, jdCardId);
                    }
                }
            }
        }
    }

    public void addRankCache(String id, int number,int gameType) {
        gameCacheService.addGameRankCache(gameType, id, number);
    }

    @KafkaProducer(topic = KafkaTopicContext.RED_POINT, event = KafkaEventContext.DTS, sendParams = true)
    public void updateDtsData(String a,JSONObject orderInfo){
        String id = orderInfo.getString("userId");
        String orderNo = orderInfo.getString("orderNo");
        Long dataId = orderInfo.getLong("dataId");
        BigDecimal amount = orderInfo.getBigDecimal("betAmount");
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(Long.parseLong(id), UserCapitalTypeEnum.currency_2.getValue());
        userCacheService.addTodayUserPlayCount(Long.valueOf(id));
        userCapitalCacheService.sub(Long.parseLong(id), UserCapitalTypeEnum.yyb.getValue(), amount, BigDecimal.ZERO);
        userCapital = userCapitalCacheService.getUserCapitalCacheByType(Long.parseLong(id), UserCapitalTypeEnum.yyb.getValue());
        userCapitalService.pushLog(1, Long.parseLong(id), UserCapitalTypeEnum.yyb.getValue(), userCapital.getBalance(), userCapital.getOccupyBalance(), amount.negate(), LogCapitalTypeEnum.game_bet_dts2, orderNo, dataId, null);
        managerGameBaseService.pushCapitalUpdate(Long.valueOf(id),UserCapitalTypeEnum.yyb.getValue());
        addRankCache(id, Integer.parseInt(amount.setScale(0).toString()),GameTypeEnum.battleRoyale.getValue());
    }

    public void updateDgsData(String a,JSONObject orderInfo){
        String id = orderInfo.getString("userId");
        String orderNo = orderInfo.getString("orderNo");
        Long dataId = orderInfo.getLong("dataId");
        BigDecimal amount = orderInfo.getBigDecimal("betAmount");
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(Long.parseLong(id), UserCapitalTypeEnum.currency_2.getValue());
        userCacheService.addTodayUserPlayCount(Long.valueOf(id));
        userCapitalCacheService.sub(Long.parseLong(id), UserCapitalTypeEnum.yyb.getValue(), amount, BigDecimal.ZERO);
        userCapital = userCapitalCacheService.getUserCapitalCacheByType(Long.parseLong(id), UserCapitalTypeEnum.yyb.getValue());
        userCapitalService.pushLog(1, Long.parseLong(id), UserCapitalTypeEnum.yyb.getValue(), userCapital.getBalance(), userCapital.getOccupyBalance(), amount.negate(), LogCapitalTypeEnum.dgs_join, orderNo, dataId, null);
        managerGameBaseService.pushCapitalUpdate(Long.valueOf(id),UserCapitalTypeEnum.yyb.getValue());
        addRankCache(id, Integer.parseInt(amount.setScale(0).toString()),GameTypeEnum.battleRoyale.getValue());
    }

    @KafkaProducer(topic = KafkaTopicContext.RED_POINT, event = KafkaEventContext.LHD, sendParams = true)
    public  void updateLhdData(String a,JSONObject orderInfo){
        String id = orderInfo.getString("userId");
        String orderNo = orderInfo.getString("orderNo");
        Long dataId = orderInfo.getLong("dataId");
        BigDecimal amount = orderInfo.getBigDecimal("betAmount");
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(Long.parseLong(id), UserCapitalTypeEnum.currency_2.getValue());
        userCacheService.addTodayUserPlayCount(Long.valueOf(id));
        userCapitalCacheService.sub(Long.parseLong(id), UserCapitalTypeEnum.yyb.getValue(), amount, BigDecimal.ZERO);
        userCapital = userCapitalCacheService.getUserCapitalCacheByType(Long.parseLong(id), UserCapitalTypeEnum.yyb.getValue());
        userCapitalService.pushLog(1, Long.parseLong(id), UserCapitalTypeEnum.yyb.getValue(), userCapital.getBalance(), userCapital.getOccupyBalance(), amount.negate(), LogCapitalTypeEnum.game_bet_nh, orderNo, dataId, null);
        managerGameBaseService.pushCapitalUpdate(Long.valueOf(id),UserCapitalTypeEnum.yyb.getValue());
        addRankCache(id, Integer.parseInt(amount.setScale(0).toString()),GameTypeEnum.nh.getValue());
    }

    public void checkUserItemNumber(String userId, String itemId, int number) {
        Map<String, Backpack> userBackpack = getUserBackpack(userId);
        if (!userBackpack.containsKey(itemId) || userBackpack.get(itemId).getItemNumber() < number) {
            throwExp(itemMap.get(itemId).getName() + "数量不足");
        }
    }

    public void checkUserItemNumber(long userId, String itemId, int number) {
        checkUserItemNumber(String.valueOf(userId), itemId, number);
    }

}

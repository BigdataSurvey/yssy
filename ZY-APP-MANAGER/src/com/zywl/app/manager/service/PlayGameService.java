package com.zywl.app.manager.service;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.bean.PushBean;
import com.live.app.ws.enums.PushCode;
import com.live.app.ws.socket.BaseSocket;
import com.live.app.ws.util.DefaultPushHandler;
import com.live.app.ws.util.Push;
import com.zywl.app.base.bean.*;
import com.zywl.app.base.bean.card.Card;
import com.zywl.app.base.bean.card.DicMine;
import com.zywl.app.base.bean.card.DicShop;
import com.zywl.app.base.bean.card.JDCard;
import com.zywl.app.base.bean.hongbao.DicPrizeCard;
import com.zywl.app.base.constant.RedisKeyConstant;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.LockUtil;
import com.zywl.app.defaultx.annotation.KafkaProducer;
import com.zywl.app.defaultx.cache.AppConfigCacheService;
import com.zywl.app.defaultx.cache.GameCacheService;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.cache.UserCapitalCacheService;
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
 * 加载静态配置，维护玩家缓存，给玩家发奖励，做上下级分层
 * 负责把数据库的静态配置表加载进内存Map
 * @author 1
 */
@Service
public class PlayGameService extends BaseService {
    @Autowired
    private UserAchievementService userAchievementService;
    @Autowired
    private UserCapitalService userCapitalService;
    @Autowired
    private DicPrizeDrawService dicPrizeDrawService;
    @Autowired
    private DicHandBookService dicHandBookService;
    @Autowired
    private DicHandBookRewardService dicHandBookRewardService;
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
    private DicPrizeCardService dicPrizeCardService;
    @Autowired
    private DicPitService dicPitService;
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
    /**
     * 系统初始化缓存配置
     * 系统启动通过@PostConstruct配置的_InitGameInfoService方法一次性把各种表加载进静态 Map：
     * **/
    //道具信息配置缓存
    public static Map<String, Item> itemMap = new ConcurrentHashMap<>();
    //增值产品缓存
    public static Map<String, Product> productMap = new ConcurrentHashMap<>();
    //奖品配置缓存
    public static Map<String, DicPrizeCard> DIC_PRIZE = new ConcurrentHashMap<>();
    //矿场配置缓存
    public static Map<String, DicPit> DIC_PIT = new ConcurrentHashMap<>();
    //每日任务配置缓存
    public static Map<String, DailyTask> dailyTaskInfo = new ConcurrentHashMap<>();
    //分成配置缓存
    public static Map<String, GiveParentIncome> parentIncomeMap = new ConcurrentHashMap<>();
    public static Map<String, GiveGrandfaIncome> grandfaIncomeMap = new ConcurrentHashMap<>();
    public static Map<String, ChannelIncome> channelIncomeMap = new ConcurrentHashMap<>();
    //成就配置缓存
    public static Map<String, Achievement> achievementMap = new ConcurrentHashMap<>();
    //矿场配置缓存
    public static Map<String, DicMine> DIC_MINE = new ConcurrentHashMap<>();
    //商店配置缓存
    public static Map<String, Map<String, DicShop>> DIC_SHOP_MAP = new ConcurrentHashMap<>();
    public static Map<String, List<DicShop>> DIC_SHOP_LIST = new ConcurrentHashMap<>();

    //角色配置缓存
    public static final Map<String, DicRole> DIC_ROLE = new ConcurrentHashMap<>();
    //抽奖规则缓存
    public static Map<String, DicPrizeDraw> DIC_PRIZE_DRAW_MAP = new ConcurrentHashMap<>();
    //VIP配置缓存
    public final static Map<String, DicVip> DIC_VIP_MAP = new ConcurrentHashMap<>();
    //手册基础信息配置缓存
    public final static Map<String, DicHandBook> DIC_HAND_BOOK_MAP = new ConcurrentHashMap<>();
    //手册每日奖励信息配置缓存
    public final static Map<String, Map<String, DicHandBookReward>> DIC_HAND_BOOK_REWARD_MAP = new ConcurrentHashMap<>();


    /**
     * 非系统初始化的 业务缓存型Map
     *
     */

    //玩家背包缓存
    public static Map<String, Map<String, Backpack>> playerItems = new ConcurrentHashMap<>();

    //玩家资产缓存
    public static Map<String, UserCapital> playercoins = new ConcurrentHashMap<>();

    //渠道号缓存
    public static Map<String, User> channelUser = new ConcurrentHashMap<>();

    //玩家统计数据缓存
    public static Map<String, UserStatistic> userStatisticMap = new ConcurrentHashMap<>();

    //用户成就缓存
    public static Map<String, UserAchievement> userAchievementMap = new ConcurrentHashMap<>();

    //抽奖奖励缓存
    public static Map<String, PrizeDrawReward> prizeDrawRewardInfo = new ConcurrentHashMap<>();

    //卡牌配置缓存
    public static Map<String, Card> CARD_INFO = new ConcurrentHashMap<>();


    public static Set<String> ARTIFACT_ID = new HashSet<>();
    public static final LinkedList<Long> PRIZE_IDS = new LinkedList<>();

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
        Push.addPushSuport(PushCode.pushRed, new DefaultPushHandler());

        initItem();
        initProduct();
        initPrize();
        initPit();
        initDailyTask();
        initIncome();
        initAchievement();
        initMine();
        initShop();
        initRole();
        initPrizeDraw();
        initDicVip();
        initDicHandBook();
        initDicHandBookReward();
    }

    /**
     * 初始化商店配置表
     * **/
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

    /**分成配置**/
    public void initIncome() {
        //一代(上级)分成配置
        List<GiveParentIncome> allIncome = giveParentIncomeService.findAllIncome();
        allIncome.forEach(e -> parentIncomeMap.put(e.getId().toString(), e));
        //二代(上上级)分成配置
        List<GiveGrandfaIncome> allIncome2 = giveGrandfaIncomeService.findAllIncome();
        allIncome2.forEach(e -> grandfaIncomeMap.put(e.getId().toString(), e));
        //按照渠道的层级配置分层
        List<ChannelIncome> allIncome3 = channelIncomeService.findAllChannelIncome();
        allIncome3.forEach(e -> channelIncomeMap.put(String.valueOf(e.getTier()), e));
    }

    /**
     * 初始化VIP配置
     * **/
    public void initDicVip() {
        List<DicVip> allVip = dicVipService.findAllVip();
        allVip.forEach(e -> DIC_VIP_MAP.put(String.valueOf(e.getLv()), e));
    }

    /**
     * 初始化手册基础信息
     * **/
    public void initDicHandBook() {
        List<DicHandBook> allHandBook = dicHandBookService.findAllHandBook();
        allHandBook.forEach(e -> DIC_HAND_BOOK_MAP.put(String.valueOf(e.getId()), e));
    }

    /**
     * 初始化手册每日奖励信息
     * **/
    public void initDicHandBookReward() {
        List<DicHandBookReward> allHandBookReward = dicHandBookRewardService.findAllHandBookReward();
        for (DicHandBookReward dicHandBookReward : allHandBookReward) {
            Map<String, DicHandBookReward> map = DIC_HAND_BOOK_REWARD_MAP.getOrDefault(dicHandBookReward.getHandbookId().toString(), new HashMap<>());
            map.put(String.valueOf(dicHandBookReward.getDayNum()), dicHandBookReward);
            if (!DIC_HAND_BOOK_REWARD_MAP.containsKey(dicHandBookReward.getHandbookId().toString())) {
                DIC_HAND_BOOK_REWARD_MAP.put(dicHandBookReward.getHandbookId().toString(), map);
            }
        }
    }
    /**
     * 初始化增值产品配置
     * **/
    public void initProduct() {
        List<Product> allProduct = productService.findAllProduct();
        allProduct.forEach(e -> productMap.put(e.getId().toString(), e));
    }

    /**
     * 初始化奖品配置
     * **/
    public void initPrize() {
        DIC_PRIZE.clear();
        List<DicPrizeCard> allPrizeRecord = dicPrizeCardService.findAllPrize();
        allPrizeRecord.forEach(e -> DIC_PRIZE.put(e.getId().toString(), e));
        for (DicPrizeCard dicPrizeCard : allPrizeRecord) {
            int total = dicPrizeCard.getTotal();
            for (int i = 0; i < total; i++) {
                PRIZE_IDS.add(dicPrizeCard.getId());
            }
        }
        Collections.shuffle(PRIZE_IDS);
    }


    public void initPirzeCard(){
        DIC_PRIZE.clear();
        List<DicPrizeCard> allPrizeRecord = dicPrizeCardService.findAllPrize();
        allPrizeRecord.forEach(e -> DIC_PRIZE.put(e.getId().toString(), e));
    }

    /**
     * 初始化矿场配置
     * **/
    public void initPit() {
        List<DicPit> allPit = dicPitService.findAllPit();
        allPit.forEach(e -> DIC_PIT.put(e.getId().toString(), e));
    }
    /**
     * 初始化矿场规则配置
     * **/
    public void initPrizeDraw() {
        List<DicPrizeDraw> allPrizeDraw = dicPrizeDrawService.findAllPrizeDraw();
        allPrizeDraw.forEach(e -> DIC_PRIZE_DRAW_MAP.put(e.getId().toString(), e));
    }

    /**初始化每日任务配置**/
    public void initDailyTask() {
        dailyTaskInfo.clear();
        List<DailyTask> allDailyTask = dailyTaskService.findAllDailyTask();
        allDailyTask.forEach(e -> dailyTaskInfo.put(e.getId().toString(), e));
    }
    /**
     * 初始化道具
     * **/
    public void initItem() {
        itemMap.clear();
        List<Item> items = itemService.findAll();
        items.forEach(e -> itemMap.put(e.getId().toString(), e));
    }

    /**初始化矿产信息**/
    public void initMine() {
        DIC_MINE.clear();
        logger.info("初始化矿产相关信息");
        List<DicMine> list = dicMineService.findAllMine();
        list.forEach(e -> DIC_MINE.put(String.valueOf(e.getIndex()), e));
        logger.info("初始化矿产信息完成,加载数据数量：" + DIC_MINE.size());
    }

    /**
     * 初始化角色信息
     * **/
    public void initRole() {
        DIC_ROLE.clear();
        logger.info("初始化角色相关信息");
        List<DicRole> allRole = dicRoleService.findAllRole();
        allRole.forEach(e -> DIC_ROLE.put(e.getId().toString(), e));
        logger.info("初始化角色信息完成,加载数据数量：" + DIC_MINE.size());
    }

    /**
     * 用户统计数据更新至数据库
     * **/
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

    public void updateUserPrize() {
        try {
            long time = System.currentTimeMillis();
            logger.info("=========更新奖池的数据==========");
            List<DicPrizeCard> list = new ArrayList<>();
            DIC_PRIZE.values().stream().forEach(e -> list.add(e));
            DIC_PRIZE.clear();
            logger.info("数据更新完成，用时：" + (System.currentTimeMillis() - time) + ",条数：" + list.size());
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

    /**
     * 根据渠道号获取用户
     * **/
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
    /**
     * 把玩家背包从 DB 拉进内存缓存
     * **/
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

    public double getUserItemNumber(Long userId, String itemId) {
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

    public void updateUserBackpack(Long userId, String itemId, double number, LogUserBackpackTypeEnum em) {
        updateUserBackpack(userId.toString(), itemId, number, em);
    }

    public void updateUserBackpack(Long userId, String itemId, double number, LogUserBackpackTypeEnum em, String otherUserId) {
        updateUserBackpack(userId.toString(), itemId, number, em, otherUserId);
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

    public void updateUserBackpack(String userId, String itemId, double number, LogUserBackpackTypeEnum em) {
        synchronized (LockUtil.getlock(userId)) {
            Map<String, Backpack> map = getUserBackpack(userId);
            int type = 0;
            double beforeNumber = 0;
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

    public void updateUserBackpack(String userId, String itemId, double number, LogUserBackpackTypeEnum em, String otherUserId) {
        synchronized (LockUtil.getlock(userId)) {
            Map<String, Backpack> map = getUserBackpack(userId);
            int type = 0;
            double beforeNumber = 0;
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
                result = backpackService.subItemNumber(Long.parseLong(userId), Long.parseLong(itemId), -number, em, beforeNumber, id, otherUserId);
            } else {
                result = backpackService.addItemNumber(Long.parseLong(userId), Long.parseLong(itemId), number, em, type, beforeNumber, id, otherUserId);
            }
            if (result < 1) {
                userCapitalCacheService.deltedUserCapitalCache(Long.parseLong(userId), UserCapitalTypeEnum.currency_2.getValue());
                playerItems.remove(userId);
                throwExp("道具操作失败,请重试");
            }
            if (playerItems.containsKey(userId)) {

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

    /**
     * 得到用户背包中道具信息
     */
    public List<JSONObject> getReturnPack(Long userId) {
        // 通过userId拿到放入playerItems缓存中得背包信息 itemId -> Backpack
        Map<String, Backpack> map = getUserBackpack(userId.toString());
        List<JSONObject> list = new ArrayList<>(map.size());

        for (Map.Entry<String, Backpack> entry : map.entrySet()) {
            Backpack backpack = entry.getValue();
            if (backpack.getItemNumber() == 0) {
                continue;
            }
            Long itemId = backpack.getItemId();

            // 从物品配置缓存中拿到 Item
            Item item = PlayGameService.itemMap.get(String.valueOf(itemId));
            if (item == null) {
                continue;
            }

            JSONObject obj = new JSONObject();
            obj.put("id", itemId);                   // 道具ID
            obj.put("number", backpack.getItemNumber()); // 道具数量
            obj.put("type", item.getType());         // 道具大类

            list.add(obj);
        }

        // 按道具ID升序排序
        list.sort(Comparator.comparingLong(o -> o.getLongValue("id")));

        return list;
    }


    /**
     * 获取用户统计数据
     * **/
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

    //得到玩家成就
    public UserAchievement getUserAchievement(String userId) {
        UserAchievement userAchievement = userAchievementService.findUserAchievement(Long.parseLong(userId));
        return userAchievement;
    }



    /**
     * 统一发奖入口（资产 + 道具）
     * 使用场景为各种玩法结算奖励和邮件领取附件；
     *
     * 奖励格式约定（JSONArray array）中的单条 JSON：
     *  {
     *      "type": 1,          // 暂定只支持 type = 1（都视为“道具类奖励”，其中部分是资产）
     *      "id":   "1001",     // 道具ID or 资产ID（与 ItemIdEnum / dic_item.id 一致）
     *      "number": 100,      // 数量；资产用 BigDecimal，普通道具用整数即可
     *      "channel": 1,       // 预留字段：收益渠道（目前不在此方法里使用）
     *      "fromUserId": "xxx" // 可选：来源玩家ID（比如好友赠送邮件）
     *  }
     *
     * 资产定义
     *  - 核心积分：       ItemIdEnum.CORE_POINT      -> UserCapitalTypeEnum.hxjf      (value = 1001)
     *  - 游戏消耗货币：   ItemIdEnum.GAME_CONSUME_COIN -> UserCapitalTypeEnum.xxxhhb (value = 1002)
     *
     * 规则：
     *  1）判断 itemId 是否是资产：
     *      - 是资产：写 UserCapital & pushCapitalUpdate(userId, capitalType)
     *      - 否则：当普通背包道具处理
     *
     *  2）普通背包道具：
     *      - 若 source == LogCapitalTypeEnum.mail：视为“邮件/好友赠送”
     *          -> updateUserBackpack(userId, itemId, qty, LogUserBackpackTypeEnum.zs, fromUserId)
     *      - 否则：视为“玩法/运营奖励”
     *          -> updateUserBackpack(userId, itemId, qty, LogUserBackpackTypeEnum.game)
     */
    @KafkaProducer(topic = KafkaTopicContext.RED_POINT, event = KafkaEventContext.ADD_REWARD, sendParams = true)
    public void addReward(Long userId, JSONArray array, LogCapitalTypeEnum source) {
        if (array == null || array.isEmpty()) {
            return;
        }
        for (Object o : array) {
            if (!(o instanceof JSONObject)) {
                continue;
            }
            JSONObject reward = (JSONObject) o;
            int type = reward.getIntValue("type");
            // 道具ID
            String itemId = reward.getString("id");
            if (itemId == null || itemId.trim().isEmpty()) {
                continue;
            }
            // 数量
            BigDecimal amount = reward.getBigDecimal("number");
            if (amount == null) {amount = BigDecimal.ZERO; }
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {continue;}

            if (type == 1) {
            // 判断如果是资产型道具就走资产奖励,写 UserCapital & 推资产更新
            boolean isCapital = ItemIdEnum.CORE_POINT.getValue().equals(itemId) || ItemIdEnum.GAME_CONSUME_COIN.getValue().equals(itemId);
            if (isCapital) {
                int capitalType = Integer.parseInt(itemId);
                // 如果调用方没传source则给一个兜底类型
                LogCapitalTypeEnum finalSource = (source != null)
                        ? source
                        : LogCapitalTypeEnum.friend_transfer;

                //添加资产
                userCapitalService.addUserBalanceByAddReward(
                        amount,
                        userId,
                        capitalType,
                        finalSource
                );
                // 推送最新资产给客户端
                managerGameBaseService.pushCapitalUpdate(userId, capitalType);
                continue;
            }
            //不是资产类型奖励就是正常道具走背包奖励
            double qty = amount.doubleValue();
            if (qty == 0D) {
                continue;
            }

            // 邮件类型;
            if (source != null && source == LogCapitalTypeEnum.mail) {
                String fromUserId = reward.getString("fromUserId");
                updateUserBackpack(
                        userId,
                        itemId,
                        qty,
                        LogUserBackpackTypeEnum.zs,
                        fromUserId
                );
            } else {
                //其他玩法奖励
                updateUserBackpack(
                        userId,
                        itemId,
                        qty,
                        LogUserBackpackTypeEnum.game
                );
                List<JSONObject> packList = getReturnPack(userId);
                JSONObject packUpdate = new JSONObject();
                packUpdate.put("backpackInfo", packList);
                Push.push(PushCode.updateUserBackpack, null, packUpdate);
            }
        }
        }
    }


    public void addRankCache(String id, int number, int gameType) {
        gameCacheService.addGameRankCache(gameType, id, number);
    }

    //TODO 参与五次倩女幽魂
    @KafkaProducer(topic = KafkaTopicContext.RED_POINT, event = KafkaEventContext.DTS, sendParams = true)
    public void updateDtsData(String a, JSONObject orderInfo) {
        String id = orderInfo.getString("userId");
        String orderNo = orderInfo.getString("orderNo");
        Long dataId = orderInfo.getLong("dataId");
        BigDecimal amount = orderInfo.getBigDecimal("betAmount");
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(Long.parseLong(id), UserCapitalTypeEnum.currency_2.getValue());
        userCacheService.addTodayUserPlayCount(Long.valueOf(id));
        userCapitalCacheService.sub(Long.parseLong(id), UserCapitalTypeEnum.yyb.getValue(), amount, BigDecimal.ZERO);
        userCapital = userCapitalCacheService.getUserCapitalCacheByType(Long.parseLong(id), UserCapitalTypeEnum.yyb.getValue());
        userCapitalService.pushLog(1, Long.parseLong(id), UserCapitalTypeEnum.yyb.getValue(), userCapital.getBalance(), userCapital.getOccupyBalance(), amount.negate(), LogCapitalTypeEnum.game_bet_dts2, orderNo, dataId, null);
        managerGameBaseService.pushCapitalUpdate(Long.valueOf(id), UserCapitalTypeEnum.yyb.getValue());
        addRankCache(id, Integer.parseInt(amount.setScale(0).toString()), GameTypeEnum.battleRoyale.getValue());
    }

    @KafkaProducer(topic = KafkaTopicContext.RED_POINT, event = KafkaEventContext.NXQ, sendParams = true)
    public void updateNxqData(String a, JSONObject orderInfo) {
        String id = orderInfo.getString("userId");
        Integer number = orderInfo.getIntValue("number");
        addRankCache(id, number, GameTypeEnum.nxq.getValue());
    }

    @KafkaProducer(topic = KafkaTopicContext.RED_POINT, event = KafkaEventContext.DGS, sendParams = true)
    public void updateDgsData(String a, JSONObject orderInfo) {
        String id = orderInfo.getString("userId");
        String orderNo = orderInfo.getString("orderNo");
        Long dataId = orderInfo.getLong("dataId");
        BigDecimal amount = orderInfo.getBigDecimal("betAmount");
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(Long.parseLong(id), UserCapitalTypeEnum.currency_2.getValue());
        userCacheService.addTodayUserPlayCount(Long.valueOf(id));
        userCapitalCacheService.sub(Long.parseLong(id), UserCapitalTypeEnum.yyb.getValue(), amount, BigDecimal.ZERO);
        userCapital = userCapitalCacheService.getUserCapitalCacheByType(Long.parseLong(id), UserCapitalTypeEnum.yyb.getValue());
        userCapitalService.pushLog(1, Long.parseLong(id), UserCapitalTypeEnum.yyb.getValue(), userCapital.getBalance(), userCapital.getOccupyBalance(), amount.negate(), LogCapitalTypeEnum.dgs_join, orderNo, dataId, null);
        managerGameBaseService.pushCapitalUpdate(Long.valueOf(id), UserCapitalTypeEnum.yyb.getValue());
        addRankCache(id, Integer.parseInt(amount.setScale(0).toString()), GameTypeEnum.battleRoyale.getValue());
    }


    @KafkaProducer(topic = KafkaTopicContext.RED_POINT, event = KafkaEventContext.NXQ, sendParams = true)
    public void updateNXQData(String a, JSONObject orderInfo) {
        String id = orderInfo.getString("userId");
        String orderNo = orderInfo.getString("orderNo");
        Long dataId = orderInfo.getLong("dataId");
        BigDecimal amount = orderInfo.getBigDecimal("betAmount");
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(Long.parseLong(id), UserCapitalTypeEnum.currency_2.getValue());
        userCacheService.addNxqUserPlayCount(Long.valueOf(id));
        userCapitalCacheService.sub(Long.parseLong(id), UserCapitalTypeEnum.yyb.getValue(), amount, BigDecimal.ZERO);
        userCapital = userCapitalCacheService.getUserCapitalCacheByType(Long.parseLong(id), UserCapitalTypeEnum.yyb.getValue());
        userCapitalService.pushLog(1, Long.parseLong(id), UserCapitalTypeEnum.yyb.getValue(), userCapital.getBalance(), userCapital.getOccupyBalance(), amount.negate(), LogCapitalTypeEnum.game_bet_nh, orderNo, dataId, null);
        managerGameBaseService.pushCapitalUpdate(Long.valueOf(id), UserCapitalTypeEnum.yyb.getValue());
        addRankCache(id, Integer.parseInt(amount.setScale(0).toString()), GameTypeEnum.nxq.getValue());
    }

    @KafkaProducer(topic = KafkaTopicContext.RED_POINT, event = KafkaEventContext.SDMZ, sendParams = true)
    public void updateSg(String a, JSONObject orderInfo) {
        String id = orderInfo.getString("userId");
        String orderNo = orderInfo.getString("orderNo");
        Long dataId = orderInfo.getLong("dataId");
        BigDecimal amount = orderInfo.getBigDecimal("betAmount");
        userCacheService.addTodayUserPlayCount(Long.valueOf(id));
        userCapitalCacheService.sub(Long.parseLong(id), UserCapitalTypeEnum.yyb.getValue(), amount, BigDecimal.ZERO);
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(Long.parseLong(id), UserCapitalTypeEnum.yyb.getValue());
        userCapitalService.pushLog(1, Long.parseLong(id), UserCapitalTypeEnum.yyb.getValue(), userCapital.getBalance(), userCapital.getOccupyBalance(), amount.negate(), LogCapitalTypeEnum.game_bet_sg, orderNo, dataId, null);
        managerGameBaseService.pushCapitalUpdate(Long.parseLong(id), UserCapitalTypeEnum.yyb.getValue());
    }

    @KafkaProducer(topic = KafkaTopicContext.RED_POINT, event = KafkaEventContext.LHD, sendParams = true)
    public void updateLhdData(String a, JSONObject orderInfo) {
        String id = orderInfo.getString("userId");
        String orderNo = orderInfo.getString("orderNo");
        Long dataId = orderInfo.getLong("dataId");
        BigDecimal amount = orderInfo.getBigDecimal("betAmount");
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(Long.parseLong(id), UserCapitalTypeEnum.currency_2.getValue());
        userCacheService.addTodayUserPlayCount(Long.valueOf(id));
        userCapitalCacheService.sub(Long.parseLong(id), UserCapitalTypeEnum.yyb.getValue(), amount, BigDecimal.ZERO);
        userCapital = userCapitalCacheService.getUserCapitalCacheByType(Long.parseLong(id), UserCapitalTypeEnum.yyb.getValue());
        userCapitalService.pushLog(1, Long.parseLong(id), UserCapitalTypeEnum.yyb.getValue(), userCapital.getBalance(), userCapital.getOccupyBalance(), amount.negate(), LogCapitalTypeEnum.game_bet_nh, orderNo, dataId, null);
        managerGameBaseService.pushCapitalUpdate(Long.valueOf(id), UserCapitalTypeEnum.yyb.getValue());
        addRankCache(id, Integer.parseInt(amount.setScale(0).toString()), GameTypeEnum.nh.getValue());
    }



    @KafkaProducer(topic = KafkaTopicContext.RED_POINT, event = KafkaEventContext.SEA_HUNT, sendParams = true)
    public void updateSeaMononokeData(String a, JSONObject orderInfo) {
        String id = orderInfo.getString("userId");
        String orderNo = orderInfo.getString("orderNo");
        Long dataId = orderInfo.getLong("dataId");
        BigDecimal amount = orderInfo.getBigDecimal("betAmount");
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(Long.parseLong(id), UserCapitalTypeEnum.currency_2.getValue());
        userCacheService.addTodayUserPlayCount(Long.valueOf(id));
        userCapitalCacheService.sub(Long.parseLong(id), UserCapitalTypeEnum.yyb.getValue(), amount, BigDecimal.ZERO);
        userCapital = userCapitalCacheService.getUserCapitalCacheByType(Long.parseLong(id), UserCapitalTypeEnum.yyb.getValue());
        userCapitalService.pushLog(1, Long.parseLong(id), UserCapitalTypeEnum.yyb.getValue(), userCapital.getBalance(), userCapital.getOccupyBalance(), amount.negate(), LogCapitalTypeEnum.dgs_join, orderNo, dataId, null);
        managerGameBaseService.pushCapitalUpdate(Long.valueOf(id), UserCapitalTypeEnum.yyb.getValue());
        addRankCache(id, Integer.parseInt(amount.setScale(0).toString()), GameTypeEnum.dgs.getValue());
    }

    /**
     * 检查背包中道具数量是否充足
     * **/
    public void checkUserItemNumber(String userId, String itemId, double number) {
        Map<String, Backpack> userBackpack = getUserBackpack(userId);
        if (!userBackpack.containsKey(itemId) || userBackpack.get(itemId).getItemNumber() < number) {
            throwExp(itemMap.get(itemId).getName() + "数量不足");
        }
    }

    public void checkUserItemNumber(long userId, String itemId, int number) {
        checkUserItemNumber(String.valueOf(userId), itemId, number);
    }

}

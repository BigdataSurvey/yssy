package com.zywl.app.manager.service;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.*;
import com.zywl.app.base.bean.hongbao.DicPrizeCard;
import com.zywl.app.base.bean.hongbao.RedPosition;
import com.zywl.app.base.bean.shoop.ShopManager;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.base.util.OrderUtil;
import com.zywl.app.defaultx.cache.DzCacheService;
import com.zywl.app.defaultx.cache.GameCacheService;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.cache.card.CardGameCacheService;
import com.zywl.app.defaultx.service.*;
import com.zywl.app.manager.service.manager.ManagerConfigService;
import com.zywl.app.manager.service.manager.ManagerUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.zywl.app.base.util.Constant.key;

@Service
public class TaskService extends BaseService {


    @Autowired
    private TopService topService;

    @Autowired
    private AdminSocketService adminSocketService;

    @Autowired
    private UserStatisticService userStatisticService;

    @Autowired
    private CardGameCacheService cardGameCacheService;

    @Autowired
    private GameCacheService gameCacheService;

    @Autowired
    private DicPrizeCardService dicPrizeService;

    @Autowired
    private UserCacheService userCacheService;

    @Autowired
    private CashRecordService cashRecordService;

    @Autowired
    private ActivityService activityService;

    @Autowired
    private Activity2Service activityService2;

    @Autowired
    private Activity3Service activity3Service;
    @Autowired
    private DzCacheService dzCacheService;

    @Autowired
    private DzService dzService;

    @Autowired
    private ShopManagerService shopManagerService;

    @Autowired
    private DzPeriodsService dzPeriodsService;

    @Autowired
    private RechargeOrderService rechargeOrderService;

    @Autowired
    private ManagerUserService managerUserService;

    @Autowired
    private ManagerConfigService managerConfigService;

    @Autowired
    private GuildDailyStaticsService guildDailyStaticsService;

    @Autowired
    private GuildMemberService guildMemberService;

    @Autowired
    private TsgPayOrderCheckService tsgPayOrderCheckService;

    @Autowired
    private AdminMailService adminMailService;

    @Autowired
    private PlayGameService gameService;

    public static double ALL_JUNIOR_NUM = 0;

    public static Map<String, BigDecimal> USER_RECEIVE_PRIZE_MAP = new ConcurrentHashMap<>();

    public static Map<String, Integer> USER_JUNIOR_NUM = new ConcurrentHashMap<>();

    public static Map<String, DicPrizeCard> DIC_PRIZE = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<DayOfWeek, Integer> DIC_PRICE_MAP = new ConcurrentHashMap<>();
    @Autowired
    private UserAchievementService userAchievementService;
    @Autowired
    private PlayGameService playGameService;

    //店长每日奖励
    public void shopManagerReward(Long userId) {
        //添加余额资产  改为发邮件
        JSONArray userIdArr = new JSONArray();
        User user = userCacheService.getUserInfoById(userId);
        userIdArr.add(user.getUserNo());
        JSONObject params = new JSONObject();
        params.put("userArr", userIdArr);
        params.put("title", "店长每日奖励");
        params.put("context", "店长每日奖励发放");
        params.put("mailType", 1);
        JSONArray itemArr = new JSONArray();
        JSONObject itemInfo = new JSONObject();
        itemInfo.put("itemId", 53);
        itemInfo.put("itemNum", 66);
        itemArr.add(itemInfo);
        params.put("itemArr", itemArr);
        adminMailService.sendMail(null, params, null);
    }


    @PostConstruct
    public void _construct() {
        adminSocketService.initAllBalance();
        topService.updateTop1Info();
        adminSocketService.initKeepAlive();
        adminSocketService.initGiftInfo();
        adminSocketService.initWfsbNumber();
        initPrizePool();

        new Timer("每周日24点重置奖品").schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    if (!gameCacheService.hasPrizeKey()) {
                        //新的一周 处理数据
                        dicPrizeService.initPrize();
                        playGameService.initPrize();
                        gameCacheService.setPrizeKey();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, DateUtil.getActivityNeed(), 1000L * 60 * 60 * 24);


        new Timer("每晚给店长赠送66个金刚铃").schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    List<ShopManager> allShopManager = shopManagerService.findAllShopManager();
                    for (ShopManager shopManager : allShopManager) {
                        shopManagerReward(shopManager.getUserId());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, DateUtil.getActivityNeed(), 1000L * 60 * 60 * 24);

        new Timer("排行榜数据更新,后台数据更新").schedule(new TimerTask() {
            public void run() {
                try {
                    adminSocketService.initAllBalance();
                    topService.updateTop1Info();
                    adminSocketService.initAllBalance();
                    adminSocketService.initWfsbNumber();
                    Thread.sleep(1000);
                    gameService.updateStatic();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, DateUtil.getTopNeed(), 60000 * 5);

        new Timer("检查支付超时订单").schedule(new TimerTask() {
            public void run() {
                try {
                    tsgPayOrderCheckService.checkOrder();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 1000, 60000);


        new Timer("每晚0点执行店长推送金刚铃价格增值币66").schedule(new TimerTask() {
            public void run() {
                try {
                    shopManagerService.queryShopList();
                    logger.info("每晚0点执行成为店长推送金刚铃价格增值币66");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, DateUtil.getActivityNeed(), 1000 * 60 * 60 * 24);


        new Timer("计算奖池信息").schedule(new TimerTask() {
            public void run() {
                try {
                    initPrizePool();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, DateUtil.getTomorrowBegin() - System.currentTimeMillis() + 1000 * 60, 1000 * 60 * 60 * 24);


        new Timer("检测支付订单超时").schedule(new TimerTask() {
            public void run() {
                try {
                    rechargeOrderService.updateOrderExpire();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }, 1000, 1000 * 3);


        new Timer("同步用户离线时间").schedule(new TimerTask() {
            public void run() {
                try {
                    managerUserService.updateUserOfflineTime();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }, 1000, 1000);
        new Timer("定时处理打坐游戏每日瓜分灵石任务").schedule(new TimerTask() {
            public void run() {
                dzTask();
            }
        }, DateUtil.getDzTaskDate() - System.currentTimeMillis(), 1000 * 60 * 60 * 24);


        new Timer("定时判断是否需要操作限时活动").schedule(new TimerTask() {
            public void run() {
                try {
                    logger.info("判断限时活动是否需要插入提现订单开始");
                    long time = System.currentTimeMillis();
                    Activity activityByTime = activityService.findActivityByTime();
                    Activity lastActive = activityService.findById(activityByTime.getId() - 1);
                    long activeTime = activityByTime.getBeginTime().getTime();
                    logger.info("本期活动开启时间" + activeTime);
                    if ((System.currentTimeMillis() - activeTime) / 1000 < 10) {
                        //本期活动刚开启还不到10秒  证明上一期刚结束
                        List<JSONObject> lastActiveTopList = gameCacheService.getLastActiveTopList(lastActive);
                        for (JSONObject info : lastActiveTopList) {
                            Long userId = info.getLong("userId");
                            User user = userCacheService.getUserInfoById(userId);
                            BigDecimal rewardAmount = info.getBigDecimal("rewardAmount");
                            int isAutoPay = managerConfigService.getInteger(Config.IS_AUTO_PAY);
                            BigDecimal chunk = managerConfigService.getBigDecimal(Config.ALIPAY_ONE_MONEY);
                            BigDecimal remaining = rewardAmount;
                            while (remaining.compareTo(BigDecimal.ZERO) > 0) {
                                String orderNo = OrderUtil.getOrder5Number();
                                BigDecimal current = remaining.min(chunk);
                                cashRecordService.addCashOrder(user.getOpenId(), userId, user.getUserNo(), user.getName(), user.getRealName(), orderNo,
                                        current, 2, user.getPhone(), isAutoPay);
                                System.out.println("取出: " + current);
                                remaining = remaining.subtract(current);

                            }

                        }
                    }
                    Activity activityByTime2 = activityService2.findActivity2ByTime();
                    Activity lastActive2 = activityService2.findById(activityByTime2.getId() - 1);
                    long activeTime2 = activityByTime2.getBeginTime().getTime();
                    logger.info("本期活动2开启时间" + activeTime2);
                    if ((System.currentTimeMillis() - activeTime2) / 1000 < 10) {
                        //本期活动刚开启还不到10秒  证明上一期刚结束
                        List<JSONObject> lastActiveTopList = gameCacheService.getLastActiveTopList2(lastActive2);
                        for (JSONObject info : lastActiveTopList) {
                            Long userId = info.getLong("userId");
                            User user = userCacheService.getUserInfoById(userId);
                            BigDecimal rewardAmount = info.getBigDecimal("rewardAmount");
                            int isAutoPay = managerConfigService.getInteger(Config.IS_AUTO_PAY);
                            BigDecimal chunk = managerConfigService.getBigDecimal(Config.ALIPAY_ONE_MONEY);
                            BigDecimal remaining = rewardAmount;
                            while (remaining.compareTo(BigDecimal.ZERO) > 0) {
                                String orderNo = OrderUtil.getOrder5Number();
                                BigDecimal current = remaining.min(chunk);
                                if (current.compareTo(BigDecimal.ZERO) > 0) {
                                    cashRecordService.addCashOrder(user.getOpenId(), userId, user.getUserNo(), user.getName(), user.getRealName(), orderNo,
                                            current, 2, user.getPhone(), isAutoPay);
                                }
                                System.out.println("取出: " + current);
                                remaining = remaining.subtract(current);

                            }
                        }
                    }

                    logger.info("判断限时活动用时【" + (System.currentTimeMillis() - time) + "】ms");
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }, DateUtil.getActivityNeed(), 1000 * 60 * 60 * 24);

        new Timer("定时判断是否需要操作限时活动3").schedule(new TimerTask() {
            public void run() {
                try {
                    logger.info("判断限时活动是否需要插入提现订单开始");
                    Activity activity3ByTime = activity3Service.findActivity3ByTime();
                    Activity activity = activity3Service.findById(activity3ByTime.getId() - 1);
                    long activeTime2 = activity3ByTime.getBeginTime().getTime();
                    logger.info("本期活动3开启时间" + activeTime2);
                    if ((System.currentTimeMillis() - activeTime2) / 1000 < 10) {
                        //本期活动刚开启还不到10秒  证明上一期刚结束
                        List<JSONObject> lastActiveTopList = gameCacheService.getLastActiveTopList3(activity);
                        for (JSONObject info : lastActiveTopList) {
                            Long userId = info.getLong("userId");
                            User user = userCacheService.getUserInfoById(userId);
                            BigDecimal rewardAmount = info.getBigDecimal("rewardAmount");
                            int isAutoPay = managerConfigService.getInteger(Config.IS_AUTO_PAY);
                            BigDecimal chunk = managerConfigService.getBigDecimal(Config.ALIPAY_ONE_MONEY);
                            BigDecimal remaining = rewardAmount;
                            while (remaining.compareTo(BigDecimal.ZERO) > 0) {
                                String orderNo = OrderUtil.getOrder5Number();
                                BigDecimal current = remaining.min(chunk);
                                cashRecordService.addCashOrder(user.getOpenId(), userId, user.getUserNo(), user.getName(), user.getRealName(), orderNo,
                                        current, 2, user.getPhone(), isAutoPay);
                                System.out.println("取出: " + current);
                                remaining = remaining.subtract(current);

                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }, DateUtil.getActivityNeed(), 1000 * 60 * 60 * 24);


    }


    public void initPrizePool() {
        logger.info("==============计算奖池信息=================");
        ALL_JUNIOR_NUM = 0.0;
        USER_RECEIVE_PRIZE_MAP.clear();
        USER_JUNIOR_NUM.clear();
        BigDecimal yesterdayPrizePool = cardGameCacheService.getYesterdayPrizePool();
        List<UserStatistic> sonNumberByPrizePool = userStatisticService.findSonNumberByPrizePool();
        for (UserStatistic userStatistic : sonNumberByPrizePool) {
            ALL_JUNIOR_NUM += userStatistic.getOneJuniorNum() + userStatistic.getTwoJuniorNum();
            USER_JUNIOR_NUM.put(userStatistic.getUserId().toString(), (userStatistic.getOneJuniorNum() + userStatistic.getTwoJuniorNum()));
        }
        for (UserStatistic userStatistic : sonNumberByPrizePool) {
            double rate = userStatistic.getOneJuniorNum() / ALL_JUNIOR_NUM;
            BigDecimal canReceive = yesterdayPrizePool.multiply(BigDecimal.valueOf(rate)).setScale(2, BigDecimal.ROUND_DOWN);
            USER_RECEIVE_PRIZE_MAP.put(userStatistic.getUserId().toString(), canReceive);
        }
    }


    public void dzTask() {
        try {
            logger.info("定时处理打坐游戏每日瓜分灵石任务开始");
            long time = System.currentTimeMillis();
            UserDzPeriods userDzPeriods = dzCacheService.getDzInitInfo();
            List<UserDzRecord> userDzRecords = dzService.findAllRecord(userDzPeriods);
            dzPeriodsService.yesTodayTask(userDzRecords);
            logger.info("定时处理打坐游戏每日瓜分灵石任务用时【" + (System.currentTimeMillis() - time) + "】ms");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        BigDecimal total = new BigDecimal("888");
        BigDecimal chunk = new BigDecimal("200");
        BigDecimal remaining = total;

        while (remaining.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal current = remaining.min(chunk);
            System.out.println("取出: " + current);
            remaining = remaining.subtract(current);
        }
    }
}


package com.zywl.app.manager.service;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.*;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.base.util.LockUtil;
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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
    private UserCacheService userCacheService;

    @Autowired
    private CashRecordService cashRecordService;

    @Autowired
    private ActivityService activityService;


    @Autowired
    private DzCacheService dzCacheService;

    @Autowired
    private DzService dzService;

    @Autowired
    private DzPeriodsService dzPeriodsService;

    @Autowired
    private RechargeOrderService rechargeOrderService;

    @Autowired
    private ManagerUserService managerUserService;


    @Autowired
    private GuildDailyStaticsService guildDailyStaticsService;


    @Autowired
    private GuildMemberService guildMemberService;

    @Autowired
    private TsgPayOrderCheckService tsgPayOrderCheckService;



    @Autowired
    private PlayGameService gameService;



    public static double ALL_JUNIOR_NUM = 0;

    public static Map<String, BigDecimal> USER_RECEIVE_PRIZE_MAP = new ConcurrentHashMap<>();

    public static Map<String, Integer> USER_JUNIOR_NUM = new ConcurrentHashMap<>();






    @PostConstruct
    public void _construct() {
        adminSocketService.initAllBalance();
        topService.updateTop1Info();
        adminSocketService.initKeepAlive();
        adminSocketService.initGiftInfo();
        initPrizePool();

        //=========
     /*   new Timer("检查工作室账号").schedule(new TimerTask() {
            Admin admin = new Admin("系统风控","系统风控");

            public void run() {
                try {
                    List<User> byGZSFK1 = userService.findByGZSFK1();
                    List<User> byGZSFK2 = userService.findByGZSFK2();
                    for (User user : byGZSFK1) {
                        logger.info("检测到工作室账号，userId"+user.getId());
                        adminMailService.banUser(user.getId(),0,"系统风控",admin);
                    }
                    for (User user : byGZSFK2) {
                        logger.info("检测到工作室账号，userId"+user.getId());
                        adminMailService.banUser(user.getId(),0,"系统风控",admin);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 1000, 5000);*/

/*
        new Timer("清空30天未登录数据，LOG记录,计算留存").schedule(new TimerTask() {
            public void run() {
                try {
                    adminSocketService.initKeepAlive();

                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    gameService.updateStatic();
                    PlayGameService.userStatisticMap.clear();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    logger.info("移除前 key map size{}" + LockUtil.lock.size());
                    Set<String> keys = LockUtil.lock.keySet();
                    for (String key : keys) {
                        String value = (String) LockUtil.lock.get(key);
                        String[] split = value.split("---");
                        if (split != null) {
                            Long timer = Long.parseLong(split[1]);
                            if (System.currentTimeMillis() - timer > 1000 * 60) {
                                LockUtil.lock.remove(key);
                            }
                        }
                    }
                    logger.info("移除后key map size{}" + LockUtil.lock.size());


                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }, DateUtil.getTaskNeed(), 1000 * 60 * 60 * 24);
*/


    /*    new Timer("定时增加每日报表数据").schedule(new TimerTask() {
            public void run() {
                try {
                    logger.info("增加每日报表数据开始");
                    long time = System.currentTimeMillis();
                    List<GuildMember> allGuildMember = guildMemberService.findAllGuildMember();
                    JSONArray array = new JSONArray();
                    for (GuildMember member : allGuildMember) {
                        JSONObject object = new JSONObject();
                        object.put("userId", member.getUserId());
                        object.put("guildId", member.getGuildId());
                        object.put("ymd", DateUtil.format9(DateUtil.getDateByM(60 * 10)));
                        object.put("number", 0);
                        object.put("revenue", 0);
                        object.put("expend", 0);
                        array.add(object);
                    }
                    if (array.size() > 0) {
                        guildDailyStaticsService.batchInsertStatics(array);
                    }
                    logger.info("增加每日报表数据用时【" + (System.currentTimeMillis() - time) + "】ms");
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }, DateUtil.getAddStaticsDate(), 1000 * 60 * 60 * 24);*/


        new Timer("排行榜数据更新,后台数据更新").schedule(new TimerTask() {
            public void run() {
                try {
                    adminSocketService.initAllBalance();
                    topService.updateTop1Info();
                    adminSocketService.initAllBalance();
                    Thread.sleep(1000);
                    gameService.updateStatic();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, DateUtil.getTopNeed(), 60000 * 30);

        new Timer("检查支付超时订单").schedule(new TimerTask() {
            public void run() {
                try {
                    tsgPayOrderCheckService.checkOrder();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 1000, 60000 );


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
                    logger.info("本期活动开启时间"+activeTime);
                    if ((System.currentTimeMillis() - activeTime)/1000 <10 ){
                        //本期活动刚开启还不到10秒  证明上一期刚结束
                        List<JSONObject> lastActiveTopList = gameCacheService.getLastActiveTopList(lastActive);
                        for (JSONObject info : lastActiveTopList) {
                            Long userId = info.getLong("userId");
                            User user = userCacheService.getUserInfoById(userId);
                            BigDecimal rewardAmount = info.getBigDecimal("rewardAmount");
                            String orderNo = OrderUtil.getOrder5Number();
                            cashRecordService.addCashOrder(user.getOpenId(), userId, user.getUserNo(), user.getName(), user.getRealName(), orderNo,
                                    rewardAmount, 2, user.getPhone());
                        }
                    }


                    logger.info("判断限时活动用时【" + (System.currentTimeMillis() - time) + "】ms");
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

}

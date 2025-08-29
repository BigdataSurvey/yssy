package com.zywl.app.server.service;

import cn.hutool.dfa.WordTree;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.bean.Command;
import com.live.app.ws.bean.PushBean;
import com.live.app.ws.enums.PushCode;
import com.live.app.ws.enums.TargetSocketType;
import com.live.app.ws.util.CommandBuilder;
import com.live.app.ws.util.DefaultPushHandler;
import com.live.app.ws.util.Executer;
import com.live.app.ws.util.Push;
import com.zywl.app.base.bean.*;
import com.zywl.app.base.bean.vo.UserDailyTaskVo;
import com.zywl.app.base.constant.RedisKeyConstant;
import com.zywl.app.base.exp.AppException;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.Async;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.base.util.OrderUtil;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.cache.*;
import com.zywl.app.defaultx.cache.card.CardGameCacheService;
import com.zywl.app.defaultx.enmus.GameTypeEnum;
import com.zywl.app.defaultx.service.*;
import com.zywl.app.server.context.MessageCodeContext;
import com.zywl.app.server.socket.AppSocket;
import com.zywl.app.server.util.RequestManagerListener;
import org.apache.commons.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@ServiceClass(code = MessageCodeContext.GAME_BASE_SERVER)
public class GameBaseService extends BaseService {


    /**
     * 手机号登录
     */
    private static final String PHONE_LOGIN = "1";

    /**
     * 验证码登录
     */
    private static final String VERIFY_LOGIN = "2";

    @Autowired
    private UserCacheService userCacheService;


    @Autowired
    private UserSignCacheService userSignCacheService;

    @Autowired
    private GoodNoService goodNoService;
    @Autowired
    private GuildService guildService;

    @Autowired
    private SignRewardCacheService signRewardCacheService;


    @Autowired
    private TotalSignRecordService totalSignRecordService;

    @Autowired
    private AppConfigCacheService appConfigCacheService;

    @Autowired
    private ServerConfigService serverConfigService;

    @Autowired
    private JDCardService jdCardService;

    @Autowired
    private DailyTaskCacheService dailyTaskCacheService;

    @Autowired
    private CardGameCacheService cardGameCacheService;

    @Autowired
    private GameCacheService gameCacheService;

    @Autowired
    private Activity3Service activity3Service;
    @Autowired
    private Activity2Service activity2Service;
    @Autowired
    private ActivityService activity1Service;

    @Autowired
    private CashRecordService cashRecordService;


    @Autowired
    private ItemService itemService;
    public static Map<String, Item> itemMap = new ConcurrentHashMap<String, Item>();

    private static AtomicInteger i = new AtomicInteger(3);

    private static WordTree tree = new WordTree();


    public void checkAdUser(Long userId) {
        User user = userCacheService.getUserInfoById(userId);
        if (user.getVip1() >= 1 || user.getVip2() >= 1) {
            return;
        }
        if (user.getRisk() == 1 || user.getRiskPlus() == 1) {
            throwExp("广告异常");
        }
    }

    public void aa() {
        try {
            logger.info("判断限时活动是否需要插入提现订单开始");
            Activity activityByTime = activity1Service.findActivityByTime();
            Activity activity = activity1Service.findById(activityByTime.getId() - 1);
            List<JSONObject> lastActiveTopList = gameCacheService.getLastActiveTopList(activity);
            for (JSONObject info : lastActiveTopList) {
                Long userId = info.getLong("userId");
                User user = userCacheService.getUserInfoById(userId);
                BigDecimal rewardAmount = info.getBigDecimal("rewardAmount");
                int isAutoPay = serverConfigService.getInteger(Config.IS_AUTO_PAY);
                BigDecimal chunk = serverConfigService.getBigDecimal(Config.ALIPAY_ONE_MONEY);
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
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void bb() {
        try {
            logger.info("判断限时活动是否需要插入提现订单开始");
            Activity activityByTime = activity2Service.findActivity2ByTime();
            Activity activity = activity2Service.findById(activityByTime.getId() - 1);
            List<JSONObject> lastActiveTopList = gameCacheService.getLastActiveTopList2(activity);
            for (JSONObject info : lastActiveTopList) {
                Long userId = info.getLong("userId");
                User user = userCacheService.getUserInfoById(userId);
                BigDecimal rewardAmount = info.getBigDecimal("rewardAmount");
                int isAutoPay = serverConfigService.getInteger(Config.IS_AUTO_PAY);
                BigDecimal chunk = serverConfigService.getBigDecimal(Config.ALIPAY_ONE_MONEY);
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
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @PostConstruct
    public void _ManagerCapitalService() {
        aa();
        bb();
        initItemMap();
        loadDict();
        Push.addPushSuport(PushCode.updateUserCapital, new DefaultPushHandler());
        Push.addPushSuport(PushCode.userLeaveAncient, new DefaultPushHandler());
        Push.addPushSuport(PushCode.userAncientNotice, new DefaultPushHandler());
        Push.addPushSuport(PushCode.userJoinAncient, new DefaultPushHandler());
        Push.addPushSuport(PushCode.redReminder, new DefaultPushHandler());
        Push.addPushSuport(PushCode.redPointShow, new DefaultPushHandler());
        Push.addPushSuport(PushCode.updateRoleCard, new DefaultPushHandler());
        Push.addPushSuport(PushCode.caidengmi, new DefaultPushHandler());
        Push.addPushSuport(PushCode.updateRoleCardAll, new DefaultPushHandler());
        Push.addPushSuport(PushCode.redPointHide, new DefaultPushHandler());
        Push.addPushSuport(PushCode.updatePlayerPl, new DefaultPushHandler());
        Push.addPushSuport(PushCode.updateAdCount, new DefaultPushHandler());
        Push.addPushSuport(PushCode.updateUserInfo, new DefaultPushHandler());
        Push.addPushSuport(PushCode.updateUserPower, new DefaultPushHandler());
        Push.addPushSuport(PushCode.updateUserBackpack, new DefaultPushHandler());
        Push.addPushSuport(PushCode.pushRed, new DefaultPushHandler());
        new Timer().schedule(new TimerTask() {
            public void run() {
                try {
                    i.set(3);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 1000, 1000);


    }

    public void initItemMap() {
        List<Item> items = itemService.findAll();
        for (Item item : items) {
            itemMap.put(item.getId().toString(), item);
        }
    }

    public Map<String, Item> getItemMap() {
        if (itemMap.size() == 0) {
            initItemMap();
        }
        return itemMap;
    }

    public void registLoginPush(AppSocket appSocket) {
        User user = appSocket.getUser();
        Push.doAddPush(appSocket, new PushBean(PushCode.userUpdate, user.getId().toString()));
        Push.doAddPush(appSocket, new PushBean(PushCode.redReminder, user.getId().toString()));
        Push.doAddPush(appSocket, new PushBean(PushCode.updateUserCapital, user.getId().toString()));
        Push.doAddPush(appSocket, new PushBean(PushCode.updatePlayerPl, user.getId().toString()));
        Push.doAddPush(appSocket, new PushBean(PushCode.updateAdCount, user.getId().toString()));
        Push.doAddPush(appSocket, new PushBean(PushCode.updateUserInfo, user.getId().toString()));
        Push.doAddPush(appSocket, new PushBean(PushCode.sendNotice, user.getId().toString()));
        Push.doAddPush(appSocket, new PushBean(PushCode.updateUserPower, user.getId().toString()));
        Push.doAddPush(appSocket, new PushBean(PushCode.userLeaveAncient, user.getId().toString()));
        Push.doAddPush(appSocket, new PushBean(PushCode.userAncientNotice, user.getId().toString()));
        Push.doAddPush(appSocket, new PushBean(PushCode.updateUserBackpack, user.getId().toString()));
        Push.doAddPush(appSocket, new PushBean(PushCode.redPointShow, user.getId().toString()));
        Push.doAddPush(appSocket, new PushBean(PushCode.redPointHide, user.getId().toString()));
        Push.doAddPush(appSocket, new PushBean(PushCode.updateRoleCard, user.getId().toString()));
        Push.doAddPush(appSocket, new PushBean(PushCode.updateRoleCardAll, user.getId().toString()));
        Push.doAddPush(appSocket, new PushBean(PushCode.caidengmi, user.getId().toString()));

    }

    public void unregistLoginPush(AppSocket appSocket) {
        User user = appSocket.getUser();
        Push.doRemovePush(appSocket, new PushBean(PushCode.userUpdate, user.getId().toString()));
        Push.doRemovePush(appSocket, new PushBean(PushCode.redReminder, user.getId().toString()));
        Push.doRemovePush(appSocket, new PushBean(PushCode.updateUserCapital, user.getId().toString()));
        Push.doRemovePush(appSocket, new PushBean(PushCode.updateRoomDate, String.valueOf(GameTypeEnum.battleRoyale.getValue())));
        Push.doRemovePush(appSocket, new PushBean(PushCode.updatePlayerPl, user.getId().toString()));
        Push.doRemovePush(appSocket, new PushBean(PushCode.updateAdCount, user.getId().toString()));
        Push.doRemovePush(appSocket, new PushBean(PushCode.updateUserInfo, user.getId().toString()));
        Push.doRemovePush(appSocket, new PushBean(PushCode.sendNotice, user.getId().toString()));
        Push.doRemovePush(appSocket, new PushBean(PushCode.updateUserPower, user.getId().toString()));
        Push.doRemovePush(appSocket, new PushBean(PushCode.userLeaveAncient, user.getId().toString()));
        Push.doRemovePush(appSocket, new PushBean(PushCode.userAncientNotice, user.getId().toString()));
        Push.doRemovePush(appSocket, new PushBean(PushCode.updateUserBackpack, user.getId().toString()));
        Push.doRemovePush(appSocket, new PushBean(PushCode.redPointShow, user.getId().toString()));
        Push.doRemovePush(appSocket, new PushBean(PushCode.redPointHide, user.getId().toString()));
        Push.doRemovePush(appSocket, new PushBean(PushCode.updateRoleCard, user.getId().toString()));
        Push.doRemovePush(appSocket, new PushBean(PushCode.updateRoleCardAll, user.getId().toString()));
        Push.doRemovePush(appSocket, new PushBean(PushCode.caidengmi, user.getId().toString()));
        //离线时清除
        Push.doRemovePush(appSocket, new PushBean(PushCode.updateAncientInfo, "1"));
        Push.doRemovePush(appSocket, new PushBean(PushCode.updateAncientInfo, "2"));
        Push.doRemovePush(appSocket, new PushBean(PushCode.updateAncientInfo, "3"));
    }


    @ServiceMethod(code = "001", description = "登录成功获取用户信息")
    public Object getLoginInfo(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        WsidBean wsBean = appSocket.getWsidBean();
        Long userId = wsBean.getUserId();
        params.put("userId", userId);
        User user = userCacheService.getUserInfoById(userId);
        if (user == null) {
            throwExp("用户信息异常");
        }

        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("100100", params).build(), new RequestManagerListener(appCommand));
        return async();

    }


    @ServiceMethod(code = "003", description = "获取排行榜")
    public Async getTop(AppSocket appSocket, Command command, JSONObject params) {
        checkNull(params);
        checkNull(params.get("type"));
        Long userId = appSocket.getWsidBean().getUserId();
        params.put("userId", userId);
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("100400", params).build(),
                new RequestManagerListener(command));
        return async();
    }

    /**
     * 登录方法
     *
     * @param appSocket
     * @param
     * @param
     */
    private void doLogin(AppSocket appSocket, Long userId, Command appCommand) {
        User newUser = new User();
        newUser.setId(userId);
        newUser.setLastLoginIp(appSocket.getIp());
        newUser.setLastLoginTime(new Date());
        if (appSocket.getDevice() != null) {
            // newUser.setLastDeviceKey(appSocket.getDevice().getId());
        }

    }

    private User createUser(String phone, String deviceKey, AppSocket appSocket) {
        User user = new User();
        user.setPhone(phone);
        return user;
    }

    /**
     * 返回用户登录成功的握手数据
     *
     * @param user
     * @return
     * @author DOE
     */
    public JSONObject getLoginData(AppSocket appSocket, final User user) throws AppException {
        JSONObject result = new JSONObject();
        if (user != null) {

            JSONObject userInfo = (JSONObject) JSON.toJSON(user);
            userInfo.remove("password");

            result.put("userInfo", userInfo);

            /* APP支付 */
            registLoginPush(appSocket);

        }
        return result;
    }

/*    @ServiceMethod(code = "012", description = "更改基础设置")
    public Object updateBaseSetting(AppSocket appSocket, Command command, JSONObject data) {
        checkNull(data);
        checkNull(data.get("userId"), data.get("audio"), data.get("music"));
        JSONObject result = new JSONObject();
        long userId = data.getLongValue("userId");
        User user = userCacheService.getUserInfoById(userId);

        if (user == null) {
            throwExp("用户信息异常");
        }
        if (data.containsKey("language")) {
            appSocket.setLocale(data.getString("language"));
        }
        int audio = data.getIntValue("audio");
        int music = data.getIntValue("music");
        UserConfig userConfig = userCacheService.getUserSetting(userId);

        if (userConfig.getAudioSetting() == audio && userConfig.getMusicSetting() == music) {
            // 未修改设置 直接返回
            return result;
        }
        // 需要修改设置
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("100600", data).build(),
                new RequestManagerListener(command));
        return async();
    }*/

    @ServiceMethod(code = "013", description = "用户获取背包")
    public Object getBackPackInfo(AppSocket appSocket, Command command, JSONObject data) {
        checkNull(data);
        JSONObject result = new JSONObject();
        long userId = appSocket.getWsidBean().getUserId();
        data.put("userId", userId);
        User user = userCacheService.getUserInfoById(userId);
        if (user == null) {
            throwExp("用户信息异常");
        }
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("100110", data).build(),
                new RequestManagerListener(command));
        return async();
    }


  /*  @ServiceMethod(code = "014", description = "用户广告位观看次数")
    public Object getUserAdvertNumber(AppSocket appSocket, Command command, JSONObject data) {
        checkNull(data);
        checkNull(data.get("index"));
        int index = data.getIntValue("index");
        if (index < 0 || index > 8) {
            throwExp("非法请求");
        }
        long userId = appSocket.getWsidBean().getUserId();
        Long number = userCacheService.getUserAdvertLookNum(userId, data.getIntValue("index"));
        return number;
    }*/


    @ServiceMethod(code = "016", description = "获取每日任务详情")
    public JSONObject getDailyTask(AppSocket appSocket, Command command, JSONObject data) {
        checkNull(data);
        long userId = appSocket.getWsidBean().getUserId();
        JSONObject result = new JSONObject();
        Map userTask = cardGameCacheService.getUserTask(userId);
        Collection values = userTask.values();
        List<UserDailyTaskVo> list0 = new ArrayList<>();
        List<UserDailyTaskVo> list1 = new ArrayList<>();
        List<UserDailyTaskVo> list2 = new ArrayList<>();
        for (Object value : values) {
            UserDailyTaskVo vo = (UserDailyTaskVo) value;
            if (vo.getStatus() == 0) {
                list0.add(vo);
            } else if (vo.getStatus() == 1) {
                list1.add(vo);
            } else {
                list2.add(vo);
            }
        }
        safeSortByScoreDesc(list0);
        safeSortByScoreDesc(list2);
        list1.addAll(list0);
        list1.addAll(list2);
        Long ap = cardGameCacheService.getUserDtAp(userId);
        Long userTodaySign = cardGameCacheService.getUserTodaySign(userId);
        result.put("taskList", list1);
        result.put("isSign", userTodaySign);
        result.put("signReward", JSONArray.parseArray(serverConfigService.getString(Config.SIGN_REWARD)));
        result.put("signNow", list2.size());
        result.put("signAll", 5);
        return result;
    }

    public static void safeSortByScoreDesc(List<UserDailyTaskVo> list) {
        Collections.sort(list, (o1, o2) -> {
            double score1 = o1.getId(); // 默认值0
            double score2 = o2.getId();
            return Double.compare(score2, score1);
        });
    }

    @ServiceMethod(code = "017", description = "每日任务完成签到")
    public Async userSign(AppSocket appSocket, Command command, JSONObject data) {
        checkNull(data);
        long userId = appSocket.getWsidBean().getUserId();
        data.put("userId", userId);
        JSONObject result = new JSONObject();
        Map userTask = cardGameCacheService.getUserTask(userId);
        Collection values = userTask.values();
        for (Object value : values) {
            UserDailyTaskVo vo = (UserDailyTaskVo) value;
            if (vo.getId() <= 5 && vo.getStatus() != 2) {
                throwExp("请完成全部每日任务并领取奖励后进行签到");
            }
        }
        Long userTodaySign = cardGameCacheService.getUserTodaySign(userId);
        if (userTodaySign == 1) {
            throwExp("今天已经签到过啦");
        }
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("1001151", data).build(),
                new RequestManagerListener(command));
        return async();
    }

    @ServiceMethod(code = "020", description = "背包物品出售")
    public Object sellItemToSystem(AppSocket appSocket, Command command, JSONObject data) {
        checkNull(data);
        checkNull(data.get("itemId"), data.get("num"));
        long userId = appSocket.getWsidBean().getUserId();
        data.put("userId", userId);
        int num = data.getIntValue("num");
        if (num < 1) {
            throwExp("请填写正确的出售数量");
        }
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("100111", data).build(),
                new RequestManagerListener(command));

        return async();
    }


    @ServiceMethod(code = "023", description = "获取每日任务信息")
    public Object getUserDailyTaskInfo(AppSocket appSocket, Command command, JSONObject data) {
        checkNull(data);
        long userId = appSocket.getWsidBean().getUserId();
        data.put("userId", userId);
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("100114", data).build(),
                new RequestManagerListener(command));
        return async();
    }


    @ServiceMethod(code = "024", description = "每日任务完成领奖")
    public Object receiveUserDailyTask(AppSocket appSocket, Command command, JSONObject data) {
        checkNull(data);
        checkNull(data.get("taskId"));
        if (i.getAndAdd(-1) < 0) {
            throwExp("当前领取的人数太多啦~");
        }
        long userId = appSocket.getWsidBean().getUserId();
        int isLook = data.getIntValue("isLook");
        if (isLook == 1) {
            checkAdUser(userId);
        }

        data.put("userId", userId);
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("100115", data).build(),
                new RequestManagerListener(command));
        return async();
    }

    /*@ServiceMethod(code = "033", description = "每日任务活跃度领奖")
    public Object receiveUserDailyTaskAp(AppSocket appSocket, Command command, JSONObject data) {
        checkNull(data);
        checkNull(data.get("ap"));
        if (i.getAndAdd(-1) < 0) {
            throwExp("当前领取的人数太多啦~");
        }
        long userId = appSocket.getWsidBean().getUserId();
        data.put("userId", userId);
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("100116", data).build(),
                new RequestManagerListener(command));
        return async();
    }*/

   /* @ServiceMethod(code = "025", description = "购买铜钱")
    public Object buyCoin(AppSocket appSocket, Command command, JSONObject data) {
        checkNull(data);
        long userId = appSocket.getWsidBean().getUserId();
        data.put("userId", userId);
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("100116", data).build(),
                new RequestManagerListener(command));
        return async();
    }*/


   /* @ServiceMethod(code = "026", description = "开始看广告")
    public Object beginLook(AppSocket appSocket, Command command, JSONObject data) {
        checkNull(data);
        checkNull(data.get("adIndex"));
        int adIndex = data.getIntValue("adIndex");
        long userId = appSocket.getWsidBean().getUserId();
        userCacheService.beginLookAd(userId, adIndex);
        return data;
    }*/

    @ServiceMethod(code = "027", description = "获取某种道具的数量")
    public Object getItemNumber(AppSocket appSocket, Command command, JSONObject data) {
        checkNull(data);
        long userId = appSocket.getWsidBean().getUserId();
        data.put("userId", userId);
        User user = userCacheService.getUserInfoById(userId);
        if (user == null) {
            throwExp("获取玩家信息失败");
        }
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("100027", data).build(),
                new RequestManagerListener(command));
        return async();
    }

 /*   @ServiceMethod(code = "028", description = "购买靓号")
    public Object buyGoodNo(AppSocket appSocket, Command command, JSONObject data) {
        checkNull(data);
        checkNull(data.get("goodNoId"));
        long userId = appSocket.getWsidBean().getUserId();
        data.put("userId", userId);
        User user = userCacheService.getUserInfoById(userId);
        if (user == null) {
            throwExp("获取玩家信息失败");
        }
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("100028", data).build(),
                new RequestManagerListener(command));
        return async();
    }*/


/*    @ServiceMethod(code = "031", description = "使用道具")
    public Object useItem(AppSocket appSocket, Command command, JSONObject data) {
        checkNull(data);
        long userId = appSocket.getWsidBean().getUserId();
        data.put("userId", userId);
        int number = data.getIntValue("useNumber");
        if (number<1){
            throwExp("非法请求");
        }
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("100031", data).build(),
                new RequestManagerListener(command));
        return async();
    }*/

    //TODO
    @ServiceMethod(code = "035", description = "商城购买")
    public Object buy(AppSocket appSocket, Command command, JSONObject data) {
        checkNull(data);
        checkNull(data.get("type"), data.get("id"), data.get("number"));
        long userId = appSocket.getWsidBean().getUserId();
        data.put("userId", userId);
        int number = data.getIntValue("number");
        if (number < 1 || number > 9999) {
            throwExp("非法请求");
        }
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("100035", data).build(),
                new RequestManagerListener(command));
        return async();
    }

    @ServiceMethod(code = "037", description = "商店信息")
    public Object shopInfo(AppSocket appSocket, Command command, JSONObject data) {
        checkNull(data);
        checkNull(data.get("type"));
        long userId = appSocket.getWsidBean().getUserId();
        data.put("userId", userId);
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("100037", data).build(),
                new RequestManagerListener(command));
        return async();
    }

    public void syncOffline(String userId) {
    }

    @Override
    protected Log logger() {
        return logger;
    }


    @ServiceMethod(code = "038", description = "一键领取收益")
    public Object receiveAdIncome(AppSocket appSocket, Command command, JSONObject data) {
        Long userId = appSocket.getWsidBean().getUserId();
        data.put("userId", userId);
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("100040", data).build(),
                new RequestManagerListener(command));
        return async();
    }




   /* @ServiceMethod(code = "vipPrice", description = "vip价格")
    public Object vipPrice(AppSocket appSocket, Command command, JSONObject data) {
        Long userId = appSocket.getWsidBean().getUserId();
        data.put("userId", userId);
        String monthPrice = appConfigCacheService.getConfigByKey(RedisKeyConstant.VIP_MONTH_PRICE, Config.VIP_MONTH_PRICE);
        String weekPrice = appConfigCacheService.getConfigByKey(RedisKeyConstant.VIP_WEEK_PRICE, Config.VIP_WEEK_PRICE);
        JSONObject result = new JSONObject();
        result.put("monthPrice", monthPrice);
        result.put("weekPrice", weekPrice);
        return result;
    }*/


    @ServiceMethod(code = "051", description = "修改昵称")
    public Object updateName(AppSocket appSocket, Command command, JSONObject data) {
        Long userId = appSocket.getWsidBean().getUserId();
        data.put("userId", userId);
        String name = data.getString("name");
        if (tree.isMatch(name)) {
            throwExp("包含敏感字符");
        }
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("100045", data).build(),
                new RequestManagerListener(command));
        return async();
    }


    @ServiceMethod(code = "052", description = "QQQUN")
    public Object QQQUN(AppSocket appSocket, Command command, JSONObject data) {
        Long userId = appSocket.getWsidBean().getUserId();
        String QQ = serverConfigService.getString(Config.QQ);
        String[] split = QQ.split(",");
        return split;
    }


    public void loadDict() {
        try {

            URL resource = this.getClass().getResource("/dict.txt");
            InputStream inputStream = new FileInputStream(resource.getPath());
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                tree.addWord(line.trim());
            }
        } catch (Exception e) {
            logger.error("加载敏感词数据库异常：" + e, e);
        }
    }

    @ServiceMethod(code = "syn", description = "syn")
    public Object syn(AppSocket appSocket, Command command, JSONObject data) {
        checkNull(data.get("itemId"), data.get("number"));
        Long userId = appSocket.getWsidBean().getUserId();
        data.put("userId", userId);
        int number = data.getIntValue("number");
        if (number < 1 || number > 999) {
            throwExp("请输入合理的数量");
        }
        String itemId = data.getString("itemId");
        if (!itemMap.containsKey(itemId)) {
            throwExp("道具不存在");
        }
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("100046", data).build(),
                new RequestManagerListener(command));
        return async();
    }

    @ServiceMethod(code = "053", description = "捐赠")
    public Object donateItem(AppSocket appSocket, Command command, JSONObject data) {
        checkNull(data.get("num"));
        Long userId = appSocket.getWsidBean().getUserId();
        data.put("userId", userId);
        int number = data.getIntValue("num");
        if (number < 1 || number > 9999) {
            throwExp("请输入合理的数量");
        }
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("100047", data).build(),
                new RequestManagerListener(command));
        return async();
    }

    @ServiceMethod(code = "054", description = "使用道具(靓号)")
    public Object selectItem(AppSocket appSocket, Command command, JSONObject data) {
        Long userId = appSocket.getWsidBean().getUserId();
        data.put("userId", userId);
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("100048", data).build(),
                new RequestManagerListener(command));
        return async();
    }

    @ServiceMethod(code = "055", description = "选择靓号")
    public Object useGoodNoItem(AppSocket appSocket, Command command, JSONObject data) {
        Long userId = appSocket.getWsidBean().getUserId();
        data.put("userId", userId);
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("100049", data).build(),
                new RequestManagerListener(command));
        return async();
    }


    @ServiceMethod(code = "056", description = "世界聊天")
    public Object chat(AppSocket appSocket, Command command, JSONObject data) {
        Long userId = appSocket.getWsidBean().getUserId();
        data.put("userId", userId);
        int type = data.getIntValue("type");
        if (type != 1 && type != 2) {
            throwExp("非法请求");
        }
        String text = data.getString("text");
        if (tree.isMatch(text)) {
            throwExp("包含敏感字符");
        }
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("100056", data).build(),
                new RequestManagerListener(command));
        return async();
    }


    @ServiceMethod(code = "057", description = "获取抽奖详情")
    public Object getPrizeInfo(AppSocket appSocket, Command command, JSONObject data) {
        Long userId = appSocket.getWsidBean().getUserId();
        data.put("userId", userId);
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("100057", data).build(),
                new RequestManagerListener(command));
        return async();
    }

    @ServiceMethod(code = "058", description = "获取抽奖详情")
    public Object prize(AppSocket appSocket, Command command, JSONObject data) {
        Long userId = appSocket.getWsidBean().getUserId();
        data.put("userId", userId);
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("100058", data).build(),
                new RequestManagerListener(command));
        return async();
    }

}

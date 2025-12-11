package com.zywl.app.manager.service.manager;
import com.zywl.app.manager.service.manager.ManagerGameFarmService;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipaySystemOauthTokenRequest;
import com.alipay.api.response.AlipaySystemOauthTokenResponse;
import com.live.app.ws.bean.Command;
import com.live.app.ws.enums.PushCode;
import com.live.app.ws.util.Push;
import com.zywl.app.base.bean.UserYyScore;
import com.zywl.app.base.bean.*;
import com.zywl.app.base.bean.card.*;
import com.zywl.app.base.bean.vo.*;
import com.zywl.app.base.bean.vo.card.UserShopVo;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.*;
import com.zywl.app.defaultx.annotation.KafkaProducer;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.cache.*;
import com.zywl.app.defaultx.cache.card.*;
import com.zywl.app.defaultx.enmus.*;
import com.zywl.app.defaultx.service.*;
import com.zywl.app.manager.context.KafkaTopicContext;
import com.zywl.app.manager.context.MessageCodeContext;
import com.zywl.app.manager.context.KafkaEventContext;
import com.zywl.app.manager.service.*;
import com.zywl.app.manager.socket.AdminSocketServer;
import com.zywl.app.manager.socket.ManagerSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@ServiceClass(code = MessageCodeContext.GAME_BASE_SERVER)
public class ManagerGameBaseService extends BaseService {


    @Autowired
    private UserCapitalService userCapitalService;


    @Autowired
    private UserIncomeStatementService userIncomeStatementService;


    @Autowired
    private AuthService authService;


    @Autowired
    private UserPickGoodsService userPickGoodsService;

    @Autowired
    private ConvertIncomeRecordService convertIncomeRecordService;

    @Autowired
    private UserCapitalCacheService userCapitalCacheService;

    @Autowired
    private ManagerConfigService managerConfigService;


    @Autowired
    private UserConfigService userConfigService;

    @Autowired
    private UserCacheService userCacheService;
    @Autowired
    private UserVipService userVipService;
    @Autowired
    private GoodNoService goodNoService;
    @Autowired
    private GameCacheService gameCacheService;


    @Autowired
    private ShoppingRecordService shoppingRecordService;

    @Autowired
    private UserStatisticService userStatisticService;

    @Autowired
    private PlayGameService gameService;


    @Autowired
    private CardGameCacheService cardGameCacheService;


    @Autowired
    private ManagerSocketService managerSocketService;


    @Autowired
    private SellSysRecordService sellSysRecordService;


    @Autowired
    private UserService userService;

    @Autowired
    private UserYyScoreService userYyScoreService;


    @Autowired
    private ManagerUserService managerUserService;


    @Autowired
    private UserDailyTaskService userDailyTaskService;
    @Autowired
    private ItemService itemService;

    @Autowired
    private BackpackService backpackService;
    @Autowired
    private UserDonateItemRecordService userDonateItemRecordService;

    @Autowired
    private UserHandbookService userHandbookService;

    @Autowired
    private HandBookRewardRecordService handBookRewardRecordService;

    @Autowired
    private ManagerGameFarmService managerGameFarmService;


    public static final LinkedList<JSONObject> CHAT_LIST = new LinkedList<>();

    public static final JSONObject SERVER_CHAT = new JSONObject();

    @PostConstruct
    public void _ManagerGameBaseService() {
        Chat lastType2 = chatService.findLastType2();
        if (lastType2 != null) {
            SERVER_CHAT.put("name", lastType2.getUserName());
            SERVER_CHAT.put("headImg", lastType2.getUserHeadImg());
            SERVER_CHAT.put("userNo", lastType2.getUserNo());
            SERVER_CHAT.put("text", lastType2.getText());
            SERVER_CHAT.put("type", lastType2.getType());
            SERVER_CHAT.put("lv", lastType2.getVipLv());
        }

        List<Chat> last10 = chatService.findLast10();
        for (Chat chat : last10) {
            JSONObject info = new JSONObject();
            info.put("name", chat.getUserName());
            info.put("headImg", chat.getUserHeadImg());
            info.put("userNo", chat.getUserNo());
            info.put("text", chat.getText());
            info.put("type", chat.getType());
            info.put("lv", chat.getVipLv());
            CHAT_LIST.add(info);
        }
    }

    private static final int MAX_SIZE = 100;

    public void addChat(JSONObject json) {
        if (CHAT_LIST.size() >= MAX_SIZE) {
            CHAT_LIST.removeFirst(); // 移除最早的数据
        }
        CHAT_LIST.addLast(json); // 添加新数据到末尾
    }

    public LinkedList<JSONObject> getRecent(int count) {
        if (count <= 0 || count > CHAT_LIST.size()) {
            count = CHAT_LIST.size();
        }
        return new LinkedList<>(CHAT_LIST.subList(
                Math.max(0, CHAT_LIST.size() - count),
                CHAT_LIST.size()
        ));
    }

    @Autowired
    private ChatService chatService;


    private int LJY_NUMBER = 0;

    private void addLjyNumber(int number) {
        LJY_NUMBER += number;
        if (LJY_NUMBER >= 10000) {
            BigDecimal amount = BigDecimal.valueOf(LJY_NUMBER / 10000);
            LJY_NUMBER = LJY_NUMBER % 10000;
            cardGameCacheService.addTodayPrizePool(amount);
        }
    }

    List<String> GET_CARD_ONE_GAME;

    public ManagerGameBaseService() {
        GET_CARD_ONE_GAME = new ArrayList<>();
        GET_CARD_ONE_GAME.add("7");
        GET_CARD_ONE_GAME.add("23");
        GET_CARD_ONE_GAME.add("24");
        GET_CARD_ONE_GAME.add("39");
        GET_CARD_ONE_GAME.add("40");
        GET_CARD_ONE_GAME.add("53");
    }


    public static final Object obj = new Object();

    /**
     * todo:写下来别再忘了..
     * 同步表信息
     * 客户端在请求参数里带上自己当前持有的表版本,服务端对比当前配置中心里的版本号
     * 如果客户端没带某个表或者版本号不一致；就把对应的那张表的完整数据塞进 tableInfos 返回。
     * 这样这个方法就不维护任何缓存，也不用读数据库，只读两个现成的内存源；
     * 一个是 版本号源：ManagerConfigService.CONFIG
     * 一个是 数据源：PlayGameService 里对应的静态 Map
     * **/
    public JSONObject syncTableInfo(JSONObject params) {
        JSONObject tableInfos = new JSONObject();
        //从配置中心拿当前版本号
        String itemV = managerConfigService.getString(Config.ITEM_VERSION);
        String farmV = managerConfigService.getString(Config.FARM_TABLE_VERSION);
        String mineV = managerConfigService.getString(Config.MINE_VERSION);
        String roleV = managerConfigService.getString(Config.ROLE_VERSION);
        // 客户端传上来的 tableInfo
        JSONObject tableInfo = params.getJSONObject("tableInfo");

        // 道具表
        if (tableInfo != null  && (!tableInfo.containsKey("itemTable") || !itemV.equals(tableInfo.getString("itemTable")))) {
            // 需要同步物品表
            List<Item> items = new ArrayList<>(PlayGameService.itemMap.values());
            List<ItemVo> vos = new ArrayList<>();
            for (Item item : items) {
                ItemVo vo = new ItemVo();
                BeanUtils.copy(item, vo);
                vos.add(vo);
            }
            JSONObject obj = new JSONObject();
            obj.put("version", itemV);
            obj.put("data", vos);
            tableInfos.put("itemTable", obj);
        }

        // 矿表
        if (tableInfo != null  && (!tableInfo.containsKey("mineTable") || !mineV.equals(tableInfo.getString("mineTable")))) {
            // 挖矿相关配置
            List<DicMine> dicMines = new ArrayList<>(PlayGameService.DIC_MINE.values());
            JSONObject obj = new JSONObject();
            obj.put("version", mineV);
            obj.put("data", dicMines);
            tableInfos.put("mineTable", obj);
        }

        // 角色表
        if (tableInfo != null && (!tableInfo.containsKey("roleTable") || !roleV.equals(tableInfo.getString("roleTable")))) {
            // 角色/装备配置
            List<DicRole> dicRoles = new ArrayList<>(PlayGameService.DIC_ROLE.values());
            JSONObject obj = new JSONObject();
            obj.put("version", roleV);
            obj.put("data", dicRoles);
            tableInfos.put("roleTable", obj);
        }

        // 农场种地配置表
        if (tableInfo != null && (!tableInfo.containsKey("farmTable") || !farmV.equals(tableInfo.getString("farmTable")))) {
            List<DicFarm> dicFarms = new ArrayList<>(PlayGameService.DIC_FARM.values());
            JSONObject obj = new JSONObject();
            obj.put("version", farmV);
            obj.put("data", dicFarms);
            tableInfos.put("farmTable", obj);
        }

        return tableInfos;
    }


    @Transactional
    @ServiceMethod(code = "100", description = "获取用户信息")
    @KafkaProducer(topic = KafkaTopicContext.RED_POINT, event = KafkaEventContext.LOGIN, sendParams = true)
    public JSONObject getInfo(ManagerSocketServer managerSocketServer, JSONObject params) {
        checkNull(params);
        checkNull(params.get("userId"));
        Long userId = params.getLong("userId");
        synchronized (LockUtil.getlock(userId)) {
            JSONObject result = new JSONObject();
            //用户基础信息
            User user = userCacheService.getUserInfoById(userId);
            UserVo vo = new UserVo();
            BeanUtils.copy(user, vo);
            result.put("userInfo", vo);

            //货币资产
            List<UserCapitalVo> userCapitals = userCapitalCacheService.getAllUserCapitalCache(userId);
            if (managerConfigService.getInteger(Config.IP_LOGIN_RISK) == 1) {
                //如果开启了IP风控就对当前登录IP做一次风控校验，然后把IP和用户记录下来
                String lastLoginIp = user.getLastLoginIp();
                userCacheService.canLogin(lastLoginIp, userId);
                userCacheService.addIpUser(lastLoginIp, userId);
            }
            result.put("userCapitals", userCapitals);

            //用户VIP信息,如果用户没有VIP就默认创建一条；
            UserVip userVipByUserId = userVipService.findUserVipByUserId(userId);
            result.put("vipLv", userVipByUserId == null ? 0 : userVipByUserId.getVipLevel());

            result.put("achievement",gameService.getUserAchievement(String.valueOf(userId)));
            //用户上级
            result.put("parentId", user.getParentId() == null ? "" : user.getParentId());
            //用户绑定信息
            result.put("alipayAuth", user.getAlipayId() == null ? 0 : 1);
            //公告
            result.put("notice", managerConfigService.getString(Config.HOME_POPUP));
            //交易限制 下浮比例 上浮比例
            result.put("exLim", managerConfigService.getDouble(Config.TRAD_MIN));
            result.put("exMax", managerConfigService.getDouble(Config.TRAD_MAX));
            //排行榜 活动展示
            result.put("isShowTopList", managerConfigService.getInteger(Config.SHOW_TOP_LIST));
            result.put("isShowActive1", managerConfigService.getInteger(Config.ACTIVE1));
            result.put("isShowActive2", managerConfigService.getInteger(Config.ACTIVE2));
            result.put("isShowActive3", managerConfigService.getInteger(Config.ACTIVE3));
            //玩法
            result.put("sy1", managerConfigService.getInteger(Config.PLAYGAME_1_STATUS));
            result.put("sy2", managerConfigService.getInteger(Config.PLAYGAME_2_STATUS));
            //服务器时间
            result.put("serverTime", System.currentTimeMillis());
            //配置表版本与静态表同步
            result.put("tableInfo", syncTableInfo(params));
            //版本号
            result.put("version", authService.getVersion().getVersionName());
            //背包信息
            result.put("backpackInfo", gameService.getReturnPack(userId));

            // 农场信息
            JSONObject farmParams = new JSONObject();
            farmParams.put("userId", userId);
            JSONObject farmInfo = managerGameFarmService.getMyFarmInfo(managerSocketServer, farmParams);
            result.put("farmInfo", farmInfo);

            //聊天信息
            result.put("chatInfo", getRecent(10));
            result.put("serverChat", SERVER_CHAT);
            return result;
        }
    }


    @Transactional
    @ServiceMethod(code = "400", description = "查看排行榜信息")
    public JSONObject getTop(ManagerSocketServer managerSocketServer, JSONObject params) {
        checkNull(params);
        checkNull(params.get("type"));
        Long userId = params.getLong("userId");
        User user = userCacheService.getUserInfoById(userId);
        //开服活动
        JSONObject result = new JSONObject();
        int type = params.getIntValue("type");
        if (type == TopTypeEnum.VIP.getValue()) {
            result.put("topList", TopService.TOP_VIP);
            UserVip userVip = userVipService.findRechargeAmountByUserId(userId);
            VipTopVo vo = new VipTopVo();
            vo.setUserId(userId);
            vo.setNum((int) userVip.getVipLevel());
            vo.setUserNo(user.getUserNo());
            vo.setUserHeadImg(user.getHeadImageUrl());
            vo.setUserName(user.getName());
            result.put("my", vo);
        } else if (type == TopTypeEnum.INVITE.getValue()) {
            result.put("topList", TopService.TOP_5);
            OneJuniorNumTopVo myJuniorNum = userStatisticService.findMyJuniorNum(userId);
            myJuniorNum.setNum(myJuniorNum.getNum() + myJuniorNum.getNum2());
            result.put("my", myJuniorNum);
        }
        result.put("isOpen", managerConfigService.getInteger(Config.RANK_IS_OPEN));

        return result;
    }


    @Transactional
    @ServiceMethod(code = "600", description = "用户修改基础设置")
    public JSONObject userUpdateSetting(ManagerSocketServer managerSocketServer, JSONObject params) {
        checkNull(params);
        checkNull(params.get("userId"), params.get("audio"), params.get("music"));
        userConfigService.updateUserSoundsSetting(params.getLong("userId"), params.getIntValue("audio"),
                params.getIntValue("music"));
        UserConfig userConfig = userCacheService.getUserSetting(params.getLong("userId"));
        JSONObject result = new JSONObject();
        result.put("userSetting", userConfig);
        return result;

    }


    @Transactional
    @ServiceMethod(code = "018", description = "抽奖详情")
    public JSONObject prizeDrawInfo(ManagerSocketServer managerSocketServer, JSONObject params) {
        checkNull(params);
        checkNull(params.get("userId"));
        String userId = params.getString("userId");
        return getRewardList(userId);

    }


    public JSONArray getNewRewardList(String userId) {
        List<String> idList = new ArrayList<>();
        JSONArray allRewardInfo = new JSONArray();
        allRewardInfo.addAll(PlayGameService.prizeDrawRewardInfo.values());
        if (PlayGameService.prizeDrawRewardInfo.size() >= 12) {
            int i = 0;
            while (true) {
                JSONObject oneReward = DrawUtil.draw(allRewardInfo, idList);
                idList.add(oneReward.getString("id"));
                if (idList.size() == 12) {
                    break;
                }
                if (i == 1000) {
                    break;
                }
                i++;
            }
        }
        JSONArray arr = new JSONArray();
        arr.addAll(idList);
        return arr;
    }

    public JSONObject getRewardList(String userId) {
        return null;
    }


    @Transactional
    @ServiceMethod(code = "110", description = "背包")
    public JSONObject backpack(ManagerSocketServer managerSocketServer, JSONObject params) {
        checkNull(params);
        checkNull(params.get("userId"));
        Long userId = params.getLong("userId");
        return getBackpack(userId);
    }

    public JSONObject getBackpack(Long userId) {
        List<JSONObject> packs = gameService.getReturnPack(userId);
        JSONObject result = new JSONObject();
        result.put("backPackInfo", packs);
        return result;
    }

    @Transactional
    @ServiceMethod(code = "111", description = "出售给系统")
    public Object sellItemToSys(ManagerSocketServer managerSocketServer, JSONObject params) {
        checkNull(params);
        checkNull(params.get("userId"), params.get("itemId"), params.get("num"));
        String userId = params.getString("userId");
        String itemId = params.getString("itemId");
        int number = params.getIntValue("num");
        //检查道具数量
        gameService.checkUserItemNumber(userId, itemId, number);
        //道具的出售价格
        BigDecimal onePrice = PlayGameService.itemMap.get(itemId).getPrice();
        //订单号
        String orderNo = OrderUtil.getOrder5Number();
        //总价格  单价x数量
        BigDecimal totalAmount = onePrice.multiply(new BigDecimal(String.valueOf(number)));
        //添加出售记录 拿到recordId
        Long dataId = sellSysRecordService.addRecord(Long.parseLong(userId), Long.parseLong(itemId), number, totalAmount, orderNo);
        //更改背包信息 不用推送背包是因为这个updateUserBackpack 里面 推送了
        gameService.updateUserBackpack(userId, itemId, -number, LogUserBackpackTypeEnum.sell_sys);
        // 更改资产信息 卖了东西要加钱
        userCapitalService.addUserBalanceBySellToSys(totalAmount, Long.parseLong(userId), orderNo, dataId, UserCapitalTypeEnum.currency_2.getValue());
        //推送资产变动
        pushCapitalUpdate(Long.valueOf(userId), UserCapitalTypeEnum.currency_2.getValue());
        return params;
    }


    @Transactional
    @ServiceMethod(code = "112", description = "从系统购买")
    public JSONObject buyItemBySys(ManagerSocketServer managerSocketServer, JSONObject params) {
        checkNull(params);
        checkNull(params.get("userId"), params.get("itemId"), params.get("num"));
       /* String userId = params.getString("userId");
        String itemId = params.getString("itemId");
        int number = params.getIntValue("num");
        int type = params.getIntValue("type");
        int capitalType = type == 0 ? UserCapitalTypeEnum.currency_2.getValue() : UserCapitalTypeEnum.magic.getValue();
        LogCapitalTypeEnum em = type == 0 ? LogCapitalTypeEnum.shopping : LogCapitalTypeEnum.shopping_magic;
        if (capitalType == UserCapitalTypeEnum.currency_2.getValue() && PlayGameService.itemMap.get(itemId).getShopPrice().compareTo(BigDecimal.ZERO) == 0) {
            throwExp("不可购买");
        }
        if (capitalType == UserCapitalTypeEnum.magic.getValue() && PlayGameService.itemMap.get(itemId).getShopMagicPrice().compareTo(BigDecimal.ZERO) == 0) {
            throwExp("不可购买");
        }
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(Long.parseLong(userId), capitalType);
        BigDecimal amount = PlayGameService.itemMap.get(itemId).getShopPrice().multiply(new BigDecimal(Integer.toString(number)));
        if (type == 1) {
            amount = PlayGameService.itemMap.get(itemId).getShopMagicPrice().multiply(new BigDecimal(Integer.toString(number)));
        }
        if (userCapital.getBalance().compareTo(amount) < 0) {
            throwExp(UserCapitalTypeEnum.getName(capitalType) + "不足");
        }
        String orderNo = OrderUtil.getOrder5Number();
        //增加记录
        Long dataId = shoppingRecordService.addRecord(Long.parseLong(userId), Long.parseLong(itemId), number, orderNo, amount, 0);
        //修改资产
        userCapitalService.subUserBalanceByShopping(Long.parseLong(userId), amount, orderNo, dataId, capitalType, em);
        userCapital = userCapitalCacheService.getUserCapitalCacheByType(Long.parseLong(userId), capitalType);
        JSONObject pushData = new JSONObject();
        pushData.put("userId", userId);
        pushData.put("capitalType", capitalType);
        pushData.put("balance", userCapital.getBalance());
        Push.push(PushCode.updateUserCapital, managerSocketService.getServerIdByUserId(Long.parseLong(userId)), pushData);
        //修改道具
        gameService.updateUserBackpack(userId, itemId, number, LogUserBackpackTypeEnum.shopping);*/
        return params;
    }

    //购买靓号


    @Transactional
    @ServiceMethod(code = "113", description = "看广告增加铜钱")
    public JSONObject addCoin(ManagerSocketServer managerSocketServer, JSONObject params) {
        checkNull(params);
        checkNull(params.get("userId"));
        return null;

    }


    public void updateUserTask(Long userId, JSONArray taskList) {
        userDailyTaskService.updateUserTask(userId, taskList);
    }


    public JSONArray getUserTask(Long userId) {
        Map taskList = cardGameCacheService.getUserTask(userId);
        return JSONArray.copyOf(taskList.values());
    }


    @Transactional
    @ServiceMethod(code = "114", description = "获取每日任务信息")
    public JSONObject getUserDailyTaskInfo(ManagerSocketServer managerSocketServer, JSONObject params) {
        checkNull(params);
        checkNull(params.get("userId"));
        Long userId = params.getLong("userId");
        synchronized (LockUtil.getlock(userId.toString())) {
            JSONArray userTaskList = getUserTask(userId);
            Collection<DailyTask> values = PlayGameService.dailyTaskInfo.values();
            JSONObject result = new JSONObject();
            result.put("taskList", userTaskList);
            result.put("duoYou", managerConfigService.getInteger(Config.PLAYGAME_1_STATUS));
            result.put("xianWan", managerConfigService.getInteger(Config.PLAYGAME_2_STATUS));
            return result;
        }
    }
    //51391375


    @Transactional
    @ServiceMethod(code = "115", description = "领取每日任务奖励")
    @KafkaProducer(topic = KafkaTopicContext.RED_POINT, event = KafkaEventContext.DO_DAILY_TASK, sendParams = true)
    public JSONObject receiveUserDailyTask(ManagerSocketServer managerSocketServer, JSONObject params) {
        checkNull(params);
        checkNull(params.get("userId"), params.get("taskId"));
        Long userId = params.getLong("userId");
        synchronized (LockUtil.getlock(userId.toString())) {
            String taskId = params.getString("taskId");
            //获取这条每日任务的信息  在内存中存着  PlayGameService.dailyTaskInfo 是一个map
            DailyTask dailyTask = PlayGameService.dailyTaskInfo.get(taskId);
            if (dailyTask == null) {
                throwExp("非法请求");
            }

            int isLook = params.getIntValue("isLook");
            if (isLook == 1 && dailyTask.getCategory().equals("INVITE")) {
                throwExp("限时任务不可双倍领取奖励");
            }
            //获取玩家每日任务的信息
            UserDailyTaskVo userTaskById = cardGameCacheService.getUserTaskById(userId, taskId);
            if (userTaskById == null) {
                throwExp("请刷新后尝试领取");
            }
            if (userTaskById.getStatus() == 2) {
                throwExp("已领取过该奖励");
            }
            JSONArray rewards = dailyTask.getReward();
            JSONObject result = new JSONObject();

            if (isLook == 1) {
                JSONArray adReward = new JSONArray();
                for (Object reward : rewards) {
                    JSONObject info = (JSONObject) reward;
                    JSONObject newInfo = new JSONObject();
                    newInfo.put("type", info.getIntValue("type"));
                    newInfo.put("id", info.getIntValue("id"));
                    newInfo.put("number", info.getBigDecimal("number").multiply(new BigDecimal("2")));
                    adReward.add(newInfo);
                }
                gameService.addReward(userId, adReward, LogCapitalTypeEnum.daily_task, LogUserBackpackTypeEnum.daily_task);
                result.put("rewardInfo", adReward);
            } else {
                result.put("rewardInfo", rewards);
                gameService.addReward(userId, rewards, LogCapitalTypeEnum.daily_task, LogUserBackpackTypeEnum.daily_task);
            }

            result.put("taskId", taskId);
            updateUserTask(userId, taskId);
            return result;
        }

    }

    @Transactional
    @ServiceMethod(code = "1151", description = "每日任务签到奖励")
    @KafkaProducer(topic = KafkaTopicContext.RED_POINT, event = KafkaEventContext.DO_DAILY_TASK, sendParams = true)
    public JSONObject receiveUserDailyTaskSign(ManagerSocketServer managerSocketServer, JSONObject params) {
        checkNull(params);
        checkNull(params.get("userId"));
        Long userId = params.getLong("userId");
        JSONObject result = new JSONObject();
        synchronized (LockUtil.getlock(userId.toString())) {
            JSONArray reward = JSONArray.parseArray(managerConfigService.getString(Config.SIGN_REWARD));
            gameService.addReward(userId, reward,  LogCapitalTypeEnum.daily_task, LogUserBackpackTypeEnum.daily_task);
            result.put("reward", reward);
            cardGameCacheService.userSign(userId);
            return result;
        }
    }


    public void updateUserTask(Long userId, String taskId) {
        Map userTasks = cardGameCacheService.getUserTask(userId);
        Collection values = userTasks.values();
        for (Object value : values) {
            UserDailyTaskVo task = (UserDailyTaskVo) value;
            if (task.getId().toString().equals(taskId)) {
                task.setStatus(2);
                cardGameCacheService.updateUserDailyTaskStatus(userId, taskId, task);
                userDailyTaskService.updateUserTask(userId, JSONArray.copyOf(userTasks.values()));
                break;
            }
        }
    }

    @Transactional
    @ServiceMethod(code = "027", description = "获取某种道具的数量")
    public Object getItemNumber(ManagerSocketServer managerSocketServer, JSONObject params) {
        checkNull(params);
        checkNull(params.get("userId"));
        Long userId = params.getLong("userId");
        String itemId = params.getString("itemId");
        return getItemNumber(userId, itemId);
    }

    public double getItemNumber(Long userId, String itemId) {
        Map<String, Backpack> userBackpack = gameService.getUserBackpack(userId.toString());
        if (userBackpack.containsKey(itemId)) {
            return userBackpack.get(itemId).getItemNumber();
        }
        return 0;
    }


   /* @Transactional
    @ServiceMethod(code = "028", description = "购买靓号")
    public Object buyGoodNo(ManagerSocketServer managerSocketServer, JSONObject params) {
        checkNull(params);
        checkNull(params.get("userId"));
        Long userId = params.getLong("userId");
        Long goodNoId = params.getLong("goodNoId");
        GoodNo byId = goodNoService.findById(goodNoId);
        User user = userCacheService.getUserInfoById(userId);
        String oldNo = user.getUserNo();
        if (byId == null || byId.getStatus() == 0) {
            throwExp("靓号不存在");
        }
        if (byId.getNumber() == 0) {
            throwExp("靓号已售出");
        }
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.currency_2.getValue());
        if (userCapital.getBalance().compareTo(byId.getPrice()) < 0) {
            throwExp("金币不足");
        }
        goodNoService.buy(goodNoId);
        userService.updateUserNo(byId.getGoodNo(), userId);
        userCapitalService.subUserBalanceByBuyUserNo(userId, byId.getPrice());
        userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.currency_2.getValue());
        JSONObject pushData = new JSONObject();
        pushData.put("userId", userId);
        pushData.put("capitalType", UserCapitalTypeEnum.currency_2.getValue());
        pushData.put("balance", userCapital.getBalance());
        Push.push(PushCode.updateUserCapital, managerSocketService.getServerIdByUserId(userId), pushData);
        JSONObject pushDate = new JSONObject();
        pushDate.put("userId", userId);
        user = userCacheService.getUserInfoById(userId);
        UserVo vo = new UserVo();
        BeanUtils.copy(user, vo);
        pushDate.put("userInfo", vo);
        Push.push(PushCode.updateUserInfo, managerSocketService.getServerIdByUserId(userId), pushDate);
        userCacheService.removeUserCodeToIdCache(oldNo);
        JSONObject result = new JSONObject();
        result.put("goodNo", byId.getGoodNo());
        return result;
    }*/

    @Transactional
    @ServiceMethod(code = "029", description = "离线同步")
    public void syncOffline(ManagerSocketServer managerSocketServer, JSONObject params) {
        checkNull(params);
        checkNull(params.get("pl"), params.get("userId"));

    }

    @Transactional
    @ServiceMethod(code = "030", description = "支付宝绑定用户信息")
    public JSONObject getAlipayUserInfo(ManagerSocketServer managerSocketServer, JSONObject params) throws AlipayApiException {
        checkNull(params);
        checkNull(params.get("authCode"));
        Long userId = params.getLong("userId");
        synchronized (LockUtil.getlock(userId)) {
            String authCode = params.getString("authCode");
            long time = System.currentTimeMillis();
            AlipayClient alipayClient = AliPayCashService.alipayClient;
            AlipaySystemOauthTokenRequest request = new AlipaySystemOauthTokenRequest();
            request.setGrantType("authorization_code");
            request.setCode(authCode);
            AlipaySystemOauthTokenResponse returnInfo = alipayClient.certificateExecute(request);

            if (!returnInfo.isSuccess()) {
                throwExp("获取支付宝用户信息失败，请稍后重试");
            }
            String aliPayUserId = returnInfo.getUserId();
            List<User> byAliUserId = userService.findByAliUserId(aliPayUserId);
            if (byAliUserId.size() >= managerConfigService.getInteger(Config.ALIPAY_MAX_NUMBER)) {
                throwExp("该支付宝已绑定其他账号");
            }
            if (StrUtil.isEmpty(aliPayUserId)) {
                throwExp("获取支付宝信息失败，请清理缓存后重新尝试");
            }
            userService.addAliPayUserId(userId, aliPayUserId);
            return new JSONObject();
        }
    }

    @Transactional
    @ServiceMethod(code = "048", description = "使用道具")
    public JSONObject selectItem(ManagerSocketServer managerSocketServer, JSONObject params) {
        JSONObject result = new JSONObject();
        int random = 9;
        Long userId = params.getLong("userId");
        synchronized (LockUtil.getlock(userId)) {
            String itemId = params.getString("itemId");
            //大小靓号选择的道具
            //v4小靓号 v5大靓号
            if (itemId.equals("37") || itemId.equals("38")) {
                result.put("type", "1");
                List<GoodNo> allGoodNoList = goodNoService.findCanBuyGoodNo();
                List<GoodNo> smallGoodNoList = allGoodNoList.stream().filter(e -> 0 == e.getType()).collect(Collectors.toList());
                List<GoodNo> bigGoodNoList = allGoodNoList.stream().filter(e -> 1 == e.getType()).collect(Collectors.toList());
                List<GoodNo> randomSamllList = getRandomElements(smallGoodNoList, random);
                List<GoodNo> randomBigList = getRandomElements(bigGoodNoList, random);
                if (itemId.equals("37")) {
                    //随机返回9个靓号的list
                    result.put("randomGoodNoList", randomSamllList);
                    gameCacheService.set("randomGoodNoList", randomSamllList);
                } else {
                    result.put("randomGoodNoList", randomBigList);
                    gameCacheService.set("randomGoodNoList", randomBigList);
                }
            } else {
                result.put("randomGoodNoList", new ArrayList<>());
            }
            return result;
        }
    }

    @Transactional
    @ServiceMethod(code = "049", description = "选择靓号")
    public JSONObject useItem(ManagerSocketServer managerSocketServer, JSONObject params) {
        JSONObject result = new JSONObject();
        Long goodNoId = params.getLong("data");
        Long userId = params.getLong("userId");
        synchronized (LockUtil.getlock(userId)) {
            String itemId = params.getString("itemId");
            User old = userCacheService.getUserInfoById(userId);
            String oldNo = old.getUserNo();
            int number = 1;
            UserVip uservip = userVipService.findUserVipByUserId(userId);
            //修改user表中userno 并删掉redis中缓存数据
            GoodNo goodNo = goodNoService.findById(goodNoId);
            List<GoodNo> list = gameCacheService.getList("randomGoodNoList", GoodNo.class);
            if (list != null && list.size() > 0) {
                List<String> goodNoList = list.stream().map(GoodNo::getGoodNo).collect(Collectors.toList());
                if (!goodNoList.contains(goodNo.getGoodNo())) {
                    throwExp("当前靓号不属于选择范围内");
                }
            }
            goodNoService.updateStatus(goodNoId, 0);
            userService.updateUserNo(goodNo.getGoodNo(), userId);
            //指定itemid为37、38 减掉背包大小靓号道具
            gameService.updateUserBackpack(userId, itemId, -number, LogUserBackpackTypeEnum.use);
            gameCacheService.deleteByKey("randomGoodNoList");
            //变更用户信息推送
            JSONObject pushDate = new JSONObject();
            pushDate.put("userId", userId);
            User user = userCacheService.getUserInfoById(userId);
            UserVo vo = new UserVo();
            BeanUtils.copy(user, vo);
            pushDate.put("userInfo", vo);
            Push.push(PushCode.updateUserInfo, managerSocketService.getServerIdByUserId(userId), pushDate);
            userCacheService.removeUserCodeToIdCache(oldNo);
            return result;
        }
    }


    public static <T> List<com.zywl.app.base.bean.GoodNo> getRandomElements(List<GoodNo> allGoodNoList, int count) {
        // 创建一个ArrayList的拷贝，因为我们需要打乱原始列表而不影响它
        List<GoodNo> tempList = new ArrayList<>(allGoodNoList);
        Collections.shuffle(tempList); // 打乱列表顺序
        // 取前count个元素作为结果，如果count大于列表大小，则返回整个列表
        return new ArrayList<>(tempList.subList(0, Math.min(count, tempList.size())));
    }

    @Transactional
    @ServiceMethod(code = "031", description = "使用道具")
    public Object useItem1(ManagerSocketServer managerSocketServer, JSONObject params) {
        Long userId = params.getLong("userId");
        synchronized (LockUtil.getlock(userId)) {
            String itemId = params.getString("itemId");
            int number = params.getIntValue("number");
            gameService.checkUserItemNumber(userId, itemId, number);
            JSONArray reward = new JSONArray();
            JSONObject obj = new JSONObject();
            obj.put("type", 1);
            gameService.updateUserBackpack(userId,itemId,-number,LogUserBackpackTypeEnum.use);
            if (itemId.equals("55")) {
                obj.put("id", 2);
                BigDecimal all = BigDecimal.ZERO;
                for (int i = 0; i < number; i++) {
                    all = all.add(new BigDecimal("1.5"));
                }
                obj.put("number", all);
                userCapitalService.addUserBalanceByAddBox(all, userId, UserCapitalTypeEnum.currency_2.getValue(), LogCapitalTypeEnum.box_reward);
                pushCapitalUpdate(userId, UserCapitalTypeEnum.currency_2.getValue());
            } else if (itemId.equals("56")) {
                double all = 0.0;
                obj.put("id", 2);
                for (int i = 0; i < number; i++) {
                    Random random = new Random();
                    double v = random.nextInt(50) + 500;
                    v = v / 100;
                    all += v;
                }
                BigDecimal allMoney = BigDecimal.valueOf(all);
                obj.put("number", allMoney);
                userCapitalService.addUserBalanceByAddBox(allMoney, userId, UserCapitalTypeEnum.currency_2.getValue(), LogCapitalTypeEnum.box_reward);
                pushCapitalUpdate(userId, UserCapitalTypeEnum.currency_2.getValue());
            }

            reward.add(obj);
            JSONObject result = new JSONObject();
            result.put("rewardInfo", reward);
            return result;
        }


    }

    @Transactional
    @ServiceMethod(code = "035", description = "商店购买")
    @KafkaProducer(topic = KafkaTopicContext.RED_POINT, event = KafkaEventContext.SHOP_BUY, sendParams = true)
    public JSONArray buy(ManagerSocketServer managerSocketServer, JSONObject params) {
        checkNull(params);
        checkNull(params.get("userId"), params.get("id"), params.get("type"), params.get("number"));
        Long userId = params.getLong("userId");
        synchronized (LockUtil.getlock(userId)) {
            String id = params.getString("id");
            String type = params.getString("type");
            int number = params.getIntValue("number");
            if (!PlayGameService.DIC_SHOP_MAP.containsKey(type) || !PlayGameService.DIC_SHOP_MAP.get(type).containsKey(id)) {
                throwExp("异常请求");
            }
            DicShop dicShop = PlayGameService.DIC_SHOP_MAP.get(type).get(id);
            BigDecimal amount = dicShop.getPrice().multiply(BigDecimal.valueOf(number));
            if (dicShop.getUseItemId() == 1 || dicShop.getUseItemId() == 2 || dicShop.getUseItemId() == 3) {
                UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, dicShop.getUseItemId().intValue());
                if (userCapital.getBalance().compareTo(amount) < 0) {
                    throwExp("余额不足");
                } else {
                    String orderNo = OrderUtil.getOrder5Number();
                    Long dataId = shoppingRecordService.addRecord(userId, dicShop.getItemId(), number, orderNo, amount, Math.toIntExact(dicShop.getUseItemId()));
                    userCapitalService.subUserBalanceByShopping(userId, amount, orderNo, dataId, dicShop.getUseItemId().intValue(), LogCapitalTypeEnum.shopping);
                    pushCapitalUpdate(userId, dicShop.getUseItemId().intValue());
                }
            } else {
                double userItemNumber = gameService.getUserItemNumber(userId, dicShop.getUseItemId().toString());
                if (userItemNumber < number) {
                    throwExp(PlayGameService.itemMap.get(dicShop.getUseItemId().toString()).getName() + "不足");
                } else {
                    gameService.updateUserBackpack(userId, dicShop.getUseItemId().toString(), -number, LogUserBackpackTypeEnum.use);
                }
            }
            JSONObject result = new JSONObject();
            JSONArray array = new JSONArray();
            result.put("type", 1);
            result.put("id", dicShop.getItemId());
            result.put("number", number);
            array.add(result);
            gameService.addReward(userId, array, LogCapitalTypeEnum.SHOPPING_GET,LogUserBackpackTypeEnum.shopping);
            return array;
        }
    }

    /**
     * 捐赠道具
     *
     * @param managerSocketServer
     * @param params
     * @return
     */
    @Transactional
    @ServiceMethod(code = "047", description = "捐赠道具")
    public JSONArray donateItem(ManagerSocketServer managerSocketServer, JSONObject params) throws Exception {
        checkNull(params);
        checkNull(params.get("userId"), params.get("num"));
        long userId = params.getLong("userId");
        String itemId = "5";
        int number = params.getIntValue("num");
        gameService.checkUserItemNumber(userId, itemId, number);
        //每次捐赠可以获得一个道具 和一些数额（待定）的金币
        UserCapital userCapitalCache = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.currency_2.getValue());
        //生成捐赠道具订单
        String orderNo = OrderUtil.getOrder5Number();
        Long recordId = userDonateItemRecordService.addDonateItemRecord(userId, orderNo, UserCapitalTypeEnum.currency_2.getValue(), number, BigDecimal.valueOf(20L * number));
        String getItemId = managerConfigService.getString(Config.JZ_ITEM);
        //修改该用户的道具
        gameService.updateUserBackpack(userId, itemId, -number, LogUserBackpackTypeEnum.jz);
        gameService.updateUserBackpack(userId, getItemId, +number, LogUserBackpackTypeEnum.use);
        userCapitalService.addUserBalanceByDonate(userId, BigDecimal.valueOf(20L * number), UserCapitalTypeEnum.currency_2.getValue(), recordId, userCapitalCache);
        pushCapitalUpdate(userId, UserCapitalTypeEnum.currency_2.getValue());
        JSONObject result = new JSONObject();
        JSONObject itemResult = new JSONObject();
        JSONArray array = new JSONArray();
        result.put("type", 1);
        result.put("id", 2);
        result.put("number", BigDecimal.valueOf(20L * number));
        itemResult.put("type", 1);
        itemResult.put("id", getItemId);
        itemResult.put("number", number);
        array.add(result);
        array.add(itemResult);
        return array;
    }


    private void deleteItem(JSONObject params) {
        Map<String, Object> mapParams = new HashMap<>();
        mapParams.put("userId", params.getLong("userId"));
        mapParams.put("itemId", 30);
        backpackService.delete(mapParams);
    }


    public void userBuy(Long userId, UserShopVo userShopVo, int shopType, int number) {
        int capitalType = 0;
        if (shopType == ShopTypeEnum.YUANBAO.getValue()) {
            //元宝商店
            capitalType = UserCapitalTypeEnum.currency_2.getValue();
            BigDecimal price = BigDecimal.valueOf(userShopVo.getPrice());
            UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, capitalType);
            if (userCapital.getBalance().compareTo(price) < 0) {
                throwExp(UserCapitalTypeEnum.getName(capitalType) + "不足");
            }
            String orderNo = OrderUtil.getOrder5Number();
            //增加记录
            Long dataId = shoppingRecordService.addRecord(userId, userShopVo.getItemEquId(), number, orderNo, price, shopType);
            //修改资产
            userCapitalService.subUserBalanceByShopping(userId, price, orderNo, dataId, capitalType, LogCapitalTypeEnum.shopping);
            pushCapitalUpdate(userId, capitalType);
        }
        Long itemEquId = userShopVo.getItemEquId();
        if (userShopVo.getType() == 1) {
            if (itemEquId.toString().equals(ItemIdEnum.GOLD.getValue())) {
                LogCapitalTypeEnum em = null;
                if (shopType == ShopTypeEnum.FRIEND.getValue()) {
                    em = LogCapitalTypeEnum.friend_buy;
                } else if (shopType == ShopTypeEnum.PET.getValue()) {
                    em = LogCapitalTypeEnum.pet_buy;
                }
                BigDecimal amount = BigDecimal.valueOf(number);
                userCapitalService.addUserBalanceByAddReward(amount, userId, UserCapitalTypeEnum.currency_2.getValue(), em);
                pushCapitalUpdate(userId, UserCapitalTypeEnum.currency_2.getValue());
            } else {
                gameService.updateUserBackpack(userId, itemEquId.toString(), number, LogUserBackpackTypeEnum.shopping);
            }
        }

    }


    public void pushCapitalUpdate(Long userId, int capitalType) {
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, capitalType);
        JSONObject pushData = new JSONObject();
        pushData.put("userId", userId);
        pushData.put("capitalType", capitalType);
        pushData.put("balance", userCapital.getBalance());
        Push.push(PushCode.updateUserCapital, managerSocketService.getServerIdByUserId(userId), pushData);
    }


    public void pushBackpackUpdate(Long userId, String id, double number, int type) {
        JSONObject pushData = new JSONObject();
        pushData.put("userId", userId);
        JSONArray array = new JSONArray();
        JSONObject object = new JSONObject();
        object.put("type", type);
        object.put("number", number);
        object.put("id", Long.parseLong(id));
        if (PlayGameService.ARTIFACT_ID.contains(id)) {
            object.put("type", 4);
        }
        array.add(object);
        pushData.put("backPackInfo", array);
        Push.push(PushCode.updateUserBackpack, managerSocketService.getServerIdByUserId(userId), pushData);
    }

    public void pushBackpackUpdate(Long userId, Object array) {
        JSONObject pushData = new JSONObject();
        pushData.put("userId", userId);
        pushData.put("backPackInfo", array);
        Push.push(PushCode.updateUserBackpack, managerSocketService.getServerIdByUserId(userId), pushData);
    }

    @Transactional
    @ServiceMethod(code = "037", description = "商店信息")
    public Object shopInfo(ManagerSocketServer managerSocketServer, JSONObject params) {
        Long userId = params.getLong("userId");
        int shopType = params.getIntValue("type");
        List<DicShop> shopInfo = PlayGameService.DIC_SHOP_LIST.get(String.valueOf(shopType));
        if (shopInfo == null) {
            return new ArrayList<>();
        }
        return shopInfo;
    }

    @Transactional
    @ServiceMethod(code = "124", description = "领取渠道收益")
    public Object queryChannelIncome(ManagerSocketServer managerSocketServer, Command webCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("userId"));
        String userId = params.getString("userId");
        synchronized (LockUtil.getlock(userId)) {
            //User user = userCacheService.getUserInfoById(userId);
            //查询当前渠道收益信息
            UserStatistic userStatistic = userStatisticService.findByUserId(Long.valueOf(userId));
            BigDecimal nowChannelIncome = userStatistic.getNowChannelIncome();
            //检查是否有可领取的收益
            if (nowChannelIncome != null && nowChannelIncome.compareTo(BigDecimal.ZERO) > 0) {
                //保存更新
                userCapitalService.addUserBalanceByReceiveFriend(nowChannelIncome, Long.parseLong(userId), null, null);
                pushCapitalUpdate(Long.valueOf(userId), UserCapitalTypeEnum.currency_2.getValue());
                userStatisticService.updateStaticChannel(userStatistic);
                //return true;
            } else {
                throwExp("没有可以领取的收益");
            }

            return new JSONObject();
        }
    }


    @Transactional
    @ServiceMethod(code = "040", description = "一键领取友情值和广告收益")
    public Object receiveAdIncome(ManagerSocketServer managerSocketServer, JSONObject params) {
        String userId = params.getString("userId");
        synchronized (LockUtil.getlock(userId)) {
            UserStatistic userStatistic = gameService.getUserStatistic(userId);
            BigDecimal getAnima = userStatistic.getGetAnima();
            if (getAnima.compareTo(new BigDecimal("20")) > 0) {
                String orderNo = OrderUtil.getOrder5Number();
                Long dataId = convertIncomeRecordService.addRecord(Long.parseLong(userId), orderNo, getAnima, userStatistic.getGetAnima(), BigDecimal.ZERO);
                PlayGameService.userStatisticMap.get(userId).setGetAnima(BigDecimal.ZERO);
                PlayGameService.userStatisticMap.get(userId).setGetAnima2(PlayGameService.userStatisticMap.get(userId).getGetAnima2().add(getAnima));
                userCapitalService.addUserBalanceByReceiveFriend(getAnima, Long.parseLong(userId), orderNo, dataId);
                pushCapitalUpdate(Long.valueOf(userId), UserCapitalTypeEnum.currency_2.getValue());
            } else {
                throwExp("累积达到20通宝后可领取");
            }
            userStatisticService.updateStatic(PlayGameService.userStatisticMap.get(userId));
            JSONObject result = new JSONObject();
            result.put("amount", getAnima);
            return result;
        }
    }


    @Transactional
    @ServiceMethod(code = "044", description = "我的信息")
    public Object getMyInfo(ManagerSocketServer managerSocketServer, JSONObject params) {
        Long userId = params.getLong("userId");
        double todayMyGetAnima = userCacheService.getTodayMyGetAnima(userId);
        UserStatistic userStatistic = userStatisticService.findByUserId(userId);
        JSONObject result = new JSONObject();
        result.put("today", todayMyGetAnima);
        result.put("all", userStatistic.getGetAnima2().setScale(2, BigDecimal.ROUND_DOWN));
        User user = userCacheService.getUserInfoById(userId);
        JSONObject parentInfo = new JSONObject();
        parentInfo.put("userNo", "");
        parentInfo.put("headImageUrl", "");
        parentInfo.put("wx", "");
        parentInfo.put("qq", "");
        parentInfo.put("name", "暂无");
        if (user != null && user.getParentId() != null) {
            User parent = userCacheService.getUserInfoById(user.getParentId());
            if (parent != null) {
                parentInfo.put("userNo", parent.getUserNo());
                parentInfo.put("headImageUrl", parent.getHeadImageUrl());
                parentInfo.put("wx", parent.getWechatId());
                parentInfo.put("qq", parent.getQq());
                parentInfo.put("name", parent.getName());
            }
        }
        result.put("inviterInfo", parentInfo);
        result.put("isChannel", 0);
        if (user != null && user.getIsChannel() == 1) {
            result.put("isChannel", 1);
        }
        Long aLong = userService.countAllSon(userId);
        result.put("number", aLong);
        result.put("canReceive", userStatistic.getGetAnima());
        //TODO 查询收益字段
        result.put("channelIncome", userStatistic.getChannelIncome());
        result.put("nowChannelIncome", userStatistic.getNowChannelIncome());
        return result;
    }

    @Transactional
    @ServiceMethod(code = "045", description = "修改昵称")
    public Object updateName(ManagerSocketServer managerSocketServer, JSONObject params) {
        String name = params.getString("name");
        if (name.length() > 16) {
            throwExp("长度超出限制");
        }
        Long userId = params.getLong("userId");
        userService.updateUserName(name, userId);
        return name;
    }


    /**
     * 046 合成道具
     * 种子合成：3合1 + 主/副 + 暗概率 + 奖池
     * 配表 dic_item：
     *  syn_use  = [主种子ID, 副种子ID1, 副种子ID2]
     *  syn_rate = 明概率; 策划说只展示不计算，
     *  price    = 当前目标种子的价值，单位=核心积分 用于奖池计算；
     *
     *主/副消耗规则（单次尝试）：
     * - 成功：主种子1 + 两个副种子共2  ==> 共消耗3颗当前等级种子，获得1颗高一级种子
     * - 失败：主种子不消耗，两个副种子各1  ==> 共消耗2颗当前等级种子，保底留1颗主种子
     *
     * 奖池规则（按本次总成功/失败次数集中结算）：
     * SeedValue = 目标种子的 price
     * failCount = 失败次数
     * successCount = 成功次数
     *
     *  奖池增加值 =  SeedValue * FailRate * failCount
     *  奖池减少值 =  SeedValue * successCount
     *  newPool   =  oldPool + 奖池增加值 - 奖池减少值
     *
     *  假设：
     *  maxNeedPerItem 是主材料大消耗值
     *  subCountForMain 是主材料在副材料中的数量；
     *  number 合成次数
     *
     *  syn_use = 1101、1101、1101; number =10;
     *  10次都合成的最大消耗值: maxNeedPerItem = [（1+ subCountForMain=2 ）* number=10 ] = 30; 这是最大的消耗数 30也是主+副的全部;
     *
     *  syn_use = 1101、1102、1102; number =10;
     *  10次都合成的最大消耗值: maxNeedPerItem = [（1+ subCountForMain=0 ）* number=10 ] = 10;
     **/
    @Transactional
    @ServiceMethod(code = "046", description = "合成道具")
    @KafkaProducer(topic = KafkaTopicContext.RED_POINT, event = KafkaEventContext.SYN, sendParams = true)
    public Object syn(ManagerSocketServer managerSocketServer, JSONObject params) {
        String resultId = params.getString("itemId");
        int    number   = params.getIntValue("number");
        Long   userId   = params.getLong("userId");
        checkNull(resultId, number, userId);
        Map<Long, User> users = userCacheService.loadUsers(userId);

        synchronized (LockUtil.getlock(userId)) {

            if (number < 1 || number > 999) {
                throwExp("数量区间不合理");
            }

            Item resultItem = PlayGameService.itemMap.get(resultId);
            if (resultItem == null || resultItem.getCanSyn() == null || resultItem.getCanSyn() == 0) {
                throwExp("道具ID有误");
            }

            JSONArray synUse = resultItem.getSynUse();
            if (synUse == null || synUse.isEmpty()) {
                throwExp("该道具未配置合成材料");
            }
            if (synUse.size() != 3) {
                throwExp("合成配方配置错误，必须为3个材料（1主2副）");
            }

            //主种子ID
            String mainItemId = synUse.get(0).toString();

            // 统计副材料列表中每个道具出现的次数 syn_use = [1101,1101,1101] -> subUseCount = {"1101": 2}
            Map<String, Integer> subUseCount = new HashMap<>();
            for (int i = 1; i < synUse.size(); i++) {
                String sid = synUse.get(i).toString();
                subUseCount.merge(sid, 1, Integer::sum);
            }

            // 主种子在“副材料列表”里出现了几次 [1101,1101,1101] -> subCountForMain = 2；[1101,1102,1102] -> subCountForMain = 0
            int subCountForMain = subUseCount.getOrDefault(mainItemId, 0);

            /**
             * 计算最大消耗(合成)的情况下的消耗量 背包里抗不抗得住；
             * **/
            Map<String, Integer> maxNeedPerItem = new HashMap<>();

            // 主种子最大需求量 = 1个主种子 + (主种子在副种子中的数量) * N
            maxNeedPerItem.put(mainItemId, (1 + subCountForMain) * number);

            // 其他副材料最大需求量
            for (Map.Entry<String, Integer> entry : subUseCount.entrySet()) {
                String sid = entry.getKey();
                //如果主材料和辅材料一样就跳过；因为前面算过了；
                if (sid.equals(mainItemId)) {
                    continue;
                }
                maxNeedPerItem.put(sid, entry.getValue() * number);
            }

            // 背包中检查每种材料必须 >= 最大需求量
            for (Map.Entry<String, Integer> entry : maxNeedPerItem.entrySet()) {
                gameService.checkUserItemNumber(userId, entry.getKey(), entry.getValue());
            }

            //明
            int realRate = resultItem.getSynRate() == null ? 0 : resultItem.getSynRate();

            //暗的开关,如果策划想搞可以设置为1让所有合成失败
            String darkSwitch = managerConfigService.getString(Config.SEED_SYN_DARK_SWITCH);

            // 根据不同种子的品质，映射不同的暗滤
            String darkKey = null;
            Integer quality = resultItem.getQuality();
            if (quality != null) {
                switch (quality) {
                    case 2: darkKey = Config.SEED_SYN_DARK_RATE_LV2; break;
                    case 3: darkKey = Config.SEED_SYN_DARK_RATE_LV3; break;
                    case 4: darkKey = Config.SEED_SYN_DARK_RATE_LV4; break;
                    case 5: darkKey = Config.SEED_SYN_DARK_RATE_LV5; break;
                    default: break;
                }
            }

            // 如果开关开启且有配置暗概率，则用暗概率覆盖真实成功率
            if (!"0".equals(darkSwitch) && darkKey != null) {
                Integer darkRate = managerConfigService.getInteger(darkKey);
                if (darkRate != null && darkRate >= 0 && darkRate <= 100) {
                    realRate = darkRate;
                }
            }

            //根据真是成功率和合并次数来计算出 成功次数和失败次数
            int successCount = 0;
            Random random = new Random();
            for (int i = 0; i < number; i++) {
                int k = random.nextInt(100) + 1;
                if (k <= realRate) {
                    successCount++;
                }
            }
            int failCount = number - successCount;


            /*
             * 单次规则：
             *   成功：主1 + 副全部各1  => 共3颗当前等级
             *   失败：主0 + 副全部各1  => 共2颗当前等级（保底主不掉）
             */

            //主种子扣除
            int mainUse = subCountForMain * number + successCount;
            if (mainUse > 0) {
                gameService.updateUserBackpack(
                        userId,
                        mainItemId,
                        -mainUse,
                        LogUserBackpackTypeEnum.use
                );
            }

            //副材料扣除
            for (Map.Entry<String, Integer> entry : subUseCount.entrySet()) {
                String sid = entry.getKey();
                //主种子已经处理过
                if (sid.equals(mainItemId)) {
                    continue;
                }
                // 这个sid在副列表出现次数
                int countInSubList = entry.getValue();
                // 总共要扣多少
                int subUse = countInSubList * number;
                if (subUse > 0) {
                    gameService.updateUserBackpack(
                            userId,
                            sid,
                            -subUse,
                            LogUserBackpackTypeEnum.use
                    );
                }
            }

            // 合成结果
            if (successCount > 0) {
                gameService.updateUserBackpack(
                        userId,
                        resultId,
                        successCount,
                        LogUserBackpackTypeEnum.syn
                );
            }

            try {
                // 种子价值
                BigDecimal seedValue = resultItem.getPrice();
                if (seedValue == null) {
                    seedValue = BigDecimal.ZERO;
                }

                // 当前奖池值
                String poolStr = managerConfigService.getString(Config.SEED_SYN_POOL);
                if (poolStr == null || poolStr.trim().isEmpty()) {
                    poolStr = "0";
                }
                BigDecimal pool = new BigDecimal(poolStr);

                // 失败注入比例
                String failRateStr = managerConfigService.getString(Config.SEED_SYN_FAIL_POOL_RATE);
                if (failRateStr == null || failRateStr.trim().isEmpty()) {
                    failRateStr = "0.2";
                }
                BigDecimal failRate = new BigDecimal(failRateStr);

                // 失败累计增加的奖池：种子价值 * 失败次数 * 失败注入比例
                BigDecimal failAdd = seedValue
                        .multiply(failRate)
                        .multiply(BigDecimal.valueOf(failCount));

                // 成功累计扣减的奖池：种子价值 * 成功次数
                BigDecimal successSub = seedValue
                        .multiply(BigDecimal.valueOf(successCount));

                // 新奖池 = 旧奖池 + 失败注入 - 成功扣减
                BigDecimal newPool = pool.add(failAdd).subtract(successSub);

                // 回写到奖池
                managerConfigService.updateConfigData(
                        Config.SEED_SYN_POOL,
                        newPool.toPlainString()
                );
            } catch (Exception e) {
                logger.error("更新种子合成奖池失败", e);
            }

            JSONObject result = new JSONObject();
            result.put("itemId", resultId);
            result.put("successCount", successCount);
            result.put("failCount", failCount);

            return result;
        }
    }



    public void checkBalance(String userId, BigDecimal price, UserCapitalTypeEnum em) {
        checkBalance(Long.parseLong(userId), price, em);
    }

    public void checkBalance(Long userId, BigDecimal price, UserCapitalTypeEnum em) {
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, em.getValue());
        if (userCapital.getBalance().compareTo(price) < 0) {
            throwExp(em.getName() + "不足");
        }
    }


    @Transactional
    @ServiceMethod(code = "056", description = "世界聊天")
    public Object chat(ManagerSocketServer managerSocketServer, JSONObject params) {
        checkNull(params);
        checkNull(params.get("userId"), params.get("text"), params.get("type"));
        Long userId = params.getLong("userId");
        synchronized (LockUtil.getlock(userId)) {
            String text = params.getString("text");
            if (text.length() > 20) {
                throwExp("最多不超过20个字");
            }
            int type = params.getIntValue("type");
            String itemId;
            if (type == 1) {
                itemId = ItemIdEnum.XLB.getValue();
            } else {
                itemId = ItemIdEnum.DLB.getValue();
            }
            gameService.checkUserItemNumber(userId, itemId, 1);
            String orderNo = OrderUtil.getOrder5Number();
            User user = userCacheService.getUserInfoById(userId);
            Long dataId = chatService.addChat(userId, user.getName(), user.getHeadImageUrl(), text, type, orderNo, user.getUserNo(), user.getVip1());
            gameService.updateUserBackpack(userId, itemId, -1, LogUserBackpackTypeEnum.use);
            JSONObject obj = new JSONObject();
            obj.put("name", user.getName());
            obj.put("headImg", user.getHeadImageUrl());
            obj.put("userNo", user.getUserNo());
            obj.put("text", text);
            obj.put("type", type);
            obj.put("lv", user.getVip1());
            addChat(obj);
            if (type != 1) {
                SERVER_CHAT.put("name", user.getName());
                SERVER_CHAT.put("headImg", user.getHeadImageUrl());
                SERVER_CHAT.put("userNo", user.getUserNo());
                SERVER_CHAT.put("text", text);
                SERVER_CHAT.put("type", type);
                SERVER_CHAT.put("lv", user.getVip1());
            }
            Push.push(PushCode.chat, null, obj);
            return new JSONObject();
        }
    }

    @Transactional
    @ServiceMethod(code = "057", description = "获取抽奖详情")
    public Object getPrizeInfo(ManagerSocketServer managerSocketServer, JSONObject params) {
        Long userId = params.getLongValue("userId");
        UserYyScore byUserId = userYyScoreService.findByUserId(userId);
        JSONObject result = new JSONObject();
        Collection<DicPrizeDraw> values = PlayGameService.DIC_PRIZE_DRAW_MAP.values();
        List<DicPirzeDrawVo> vos = new ArrayList<>();
        for (DicPrizeDraw value : values) {
            DicPirzeDrawVo vo = new DicPirzeDrawVo();
            BeanUtils.copy(value, vo);
            vos.add(vo);
        }
        result.put("rewardList", vos);
        result.put("score", byUserId.getScore());
        return result;
    }

    @Transactional
    @ServiceMethod(code = "058", description = "抽奖")
    public Object prize(ManagerSocketServer managerSocketServer, JSONObject params) {
        Long userId = params.getLongValue("userId");
        UserYyScore byUserId = userYyScoreService.findByUserId(userId);
        if (byUserId.getScore().compareTo(new BigDecimal("100")) < 1) {
            throwExp("积分不足，不能抽奖");
        }
        //扣掉100积分
        userYyScoreService.subScore(userId, new BigDecimal("100"));
        JSONObject result = new JSONObject();
        Random random = new Random();
        int i = random.nextInt(1000) + 1;
        JSONArray rewards = new JSONArray();
        Collection<DicPrizeDraw> values = PlayGameService.DIC_PRIZE_DRAW_MAP.values();
        Long id = null;
        for (DicPrizeDraw value : values) {
            if (i < value.getRate()) {
                rewards.add(value.getReward());
                id = value.getId();
                break;
            }
        }
        if (rewards.size() > 0) {
            gameService.addReward(userId, rewards, LogCapitalTypeEnum.cave_prize_draw,LogUserBackpackTypeEnum.prize_draw);
        }
        Activity activity = gameCacheService.getActivity();
        if (activity!=null){
            if (activity.getAddPointEvent()==6){
                gameCacheService.addPointMySelf(userId,10);
            }
        }
        result.put("rewardInfo", rewards);
        byUserId = userYyScoreService.findByUserId(userId);
        result.put("score", byUserId.getScore());
        result.put("id", id);
        return result;
    }


    @ServiceMethod(code = "061", description = "获取手册信息")
    public Object getHandBookInfo(ManagerSocketServer managerSocketServer, JSONObject params) {
        checkNull(params);
        checkNull(params.get("type"), params.get("lv"));
        int type = params.getIntValue("type");
        Long userId = params.getLong("userId");
        UserHandbook userHandbook = userHandbookService.findByUserIdAndHandbookType(userId, type);
        int lv = params.getIntValue("lv");
        DicHandBook dicHandBook = getHandBook(type, lv);
        JSONObject result = new JSONObject();
        Collection<DicHandBookReward> values;
        if (userHandbook!=null){
            values = PlayGameService.DIC_HAND_BOOK_REWARD_MAP.get(userHandbook.getHandbookId().toString()).values();
        }else {
            values = PlayGameService.DIC_HAND_BOOK_REWARD_MAP.get(dicHandBook.getId().toString()).values();
        }
        List<DicHandBookReward> list = new ArrayList<>(values);
        list.sort(Comparator.comparingInt(DicHandBookReward::getDayNum));
        result.put("handBookReward", list);
        result.put("myInfo", userHandbook);
        if (type == 1) {
            result.put("useItem", 57);
        } else {
            result.put("useItem", 2);
        }
        if (userHandbook == null) {
            result.put("myLv", 0);
        } else {
            result.put("myLv", PlayGameService.DIC_HAND_BOOK_MAP.get(userHandbook.getHandbookId().toString()).getLv());
        }
        result.put("price", dicHandBook.getPrice());
        return result;
    }


    public DicHandBook getHandBook(int type, int lv) {
        DicHandBook dicHandBook = null;
        Collection<DicHandBook> values = PlayGameService.DIC_HAND_BOOK_MAP.values();
        for (DicHandBook value : values) {
            if (value.getType() == type && value.getLv() == lv) {
                dicHandBook = value;
                break;
            }
        }
        return dicHandBook;
    }


    @Transactional
    @ServiceMethod(code = "062", description = "购买手册")
    public Object buyHandBook(ManagerSocketServer managerSocketServer, JSONObject params) {
        checkNull(params);
        checkNull(params.get("type"), params.get("lv"));
        int type = params.getIntValue("type");
        Long userId = params.getLong("userId");
        int lv = params.getIntValue("lv");
        DicHandBook dicHandBook = getHandBook(type, lv);
        userHandbookService.addUserHandbook(userId, type, dicHandBook.getId());
        Activity activity3 = gameCacheService.getActivity3();
        User user = userCacheService.getUserInfoById(userId);
        if (type == 1) {
            String itemId = "57";
            gameService.checkUserItemNumber(userId, itemId, dicHandBook.getPrice());
            gameService.updateUserBackpack(userId, itemId, -dicHandBook.getPrice(), LogUserBackpackTypeEnum.use);
            userPickGoodsService.addPickGoods(userId, "定制笔", dicHandBook.getPrice());
            if (activity3!=null){
                if (activity3.getAddPointEvent()==6){
                    double score = 0.0;
                    if (lv==1){
                        score = 1;
                    } else if (lv==2) {
                        score = 5;
                    } else if (lv==3) {
                        score=10;
                    }else {
                        score=20;
                    }
                    gameCacheService.addPointMySelf3(user.getParentId(),score);
                }
            }

        } else {
            //通宝
            checkBalance(userId, BigDecimal.valueOf(dicHandBook.getPrice()), UserCapitalTypeEnum.currency_2);
            userCapitalService.subUserBalanceByBuyHandbook(userId, BigDecimal.valueOf(dicHandBook.getPrice()), UserCapitalTypeEnum.currency_2.getValue());
            pushCapitalUpdate(userId, UserCapitalTypeEnum.currency_2.getValue());
            if (activity3!=null){
                if (activity3.getAddPointEvent()==6){
                    double score = 0.0;
                    if (lv==1){
                        score = 2;
                    } else if (lv==2) {
                        score = 10;
                    } else if (lv==3) {
                        score=20;
                    }else {
                        score=40;
                    }
                    gameCacheService.addPointMySelf3(user.getParentId(),score);
                }
            }
        }
        JSONObject result = new JSONObject();
        return result;
    }

    @Transactional
    @ServiceMethod(code = "063", description = "领取手册奖励")
    public Object receiveHandBookReward(ManagerSocketServer managerSocketServer, JSONObject params) {
        checkNull(params);
        checkNull(params.get("userId"), params.get("type"));
        Long userId = params.getLong("userId");
        synchronized (LockUtil.getlock(userId)) {
            User user = userCacheService.getUserInfoById(userId);
            int type = params.getIntValue("type");
            UserHandbook userHandbook = userHandbookService.findByUserIdAndHandbookType(userId, type);
            if (userHandbook == null) {
                throwExp("手册不存在");
            }
            HandBookRewardRecord userIdOneRecord = handBookRewardRecordService.findUserIdOneRecord(userId, userHandbook.getHandbookId());
            if (userIdOneRecord != null && userIdOneRecord.getCreateTime().after(DateUtil.getToDayDateBegin())) {
                throwExp("今日已经领取过奖励了");
            }
            if (userHandbook.getDays() == 35) {
                throwExp("奖励已全部领取。");
            }
            JSONArray reward = PlayGameService.DIC_HAND_BOOK_REWARD_MAP.get(userHandbook.getHandbookId().toString()).get(String.valueOf(userHandbook.getDays() + 1)).getReward();
            userHandbook.setDays(userHandbook.getDays() + 1);
            userHandbookService.updateUserHandbook(userHandbook);
            handBookRewardRecordService.addRecord(userId, userHandbook.getHandbookId(), userHandbook.getDays(), reward);
            gameService.addReward(userId, reward, LogCapitalTypeEnum.handbook_reward, null);
            JSONObject result = new JSONObject();
            result.put("rewardInfo", reward);
            BigDecimal parentMoney = BigDecimal.ZERO;
            if (userHandbook.getHandbookId()==1){
                parentMoney = new BigDecimal("0.3");
            } else if (userHandbook.getHandbookId()==2) {
                parentMoney = new BigDecimal("1.5");
            } else if (userHandbook.getHandbookId()==3) {
                parentMoney = new BigDecimal("3");
            } else if (userHandbook.getHandbookId()==4) {
                parentMoney = new BigDecimal("6");
            } else if (userHandbook.getHandbookId()==5) {
                parentMoney = new BigDecimal("0.56");
            }else if (userHandbook.getHandbookId()==6) {
                parentMoney = new BigDecimal("2.8");
            }else if (userHandbook.getHandbookId()==7) {
                parentMoney = new BigDecimal("5.6");
            }else if (userHandbook.getHandbookId()==8) {
                parentMoney = new BigDecimal("11.2");
            }
            if (user.getParentId()!=null){
                gameService.addParentGetAnima(userId,user.getParentId().toString(),parentMoney);
            }
            if (user.getGrandfaId()!=null){
                gameService.addGrandfaGetAnima(userId,user.getGrandfaId().toString(),parentMoney.divide(new BigDecimal("4"),2, RoundingMode.DOWN));
            }
            return result;
        }
    }
}




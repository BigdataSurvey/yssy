package com.zywl.app.manager.service.manager;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipaySystemOauthTokenRequest;
import com.alipay.api.response.AlipaySystemOauthTokenResponse;
import com.live.app.ws.enums.PushCode;
import com.live.app.ws.util.Push;
import com.zywl.app.base.bean.*;
import com.zywl.app.base.bean.card.*;
import com.zywl.app.base.bean.vo.*;
import com.zywl.app.base.bean.vo.card.CardVo;
import com.zywl.app.base.bean.vo.card.PlayerCardCodexVo;
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
import com.zywl.app.defaultx.service.card.DicShopService;
import com.zywl.app.manager.context.KafkaTopicContext;
import com.zywl.app.manager.context.MessageCodeContext;
import com.zywl.app.manager.context.KafkaEventContext;
import com.zywl.app.manager.service.*;
import com.zywl.app.manager.socket.ManagerSocketServer;
import org.apache.commons.collections4.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
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
    private ManagerUserService managerUserService;


    @Autowired
    private UserDailyTaskService userDailyTaskService;
    @Autowired
    private ItemService itemService;

    @Autowired
    private BackpackService backpackService;
    @Autowired
    private UserDonateItemRecordService userDonateItemRecordService;

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


    public JSONObject syncTableInfo(JSONObject params) {
        JSONObject tableInfos = new JSONObject();
        String itemV = managerConfigService.getString(Config.ITEM_VERSION);
        String mineV = managerConfigService.getString(Config.MINE_VERSION);
        String roleV = managerConfigService.getString(Config.ROLE_VERSION);
        JSONObject tableInfo = params.getJSONObject("tableInfo");
        if (tableInfo != null
                && (!tableInfo.containsKey("itemTable") || !itemV.equals(tableInfo.getString("itemTable")))) {
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


        if (tableInfo != null
                && (!tableInfo.containsKey("mineTable") || !mineV.equals(tableInfo.getString("mineTable")))) {
            // 需要同步装备表
            List<DicMine> dicMines = new ArrayList<>(PlayGameService.DIC_MINE.values());
            JSONObject obj = new JSONObject();
            obj.put("version", mineV);
            obj.put("data", dicMines);
            tableInfos.put("mineTable", obj);
        }

        if (tableInfo != null
                && (!tableInfo.containsKey("roleTable") || !roleV.equals(tableInfo.getString("roleTable")))) {
            // 需要同步装备表
            List<DicRole> dicRoles = new ArrayList<>(PlayGameService.DIC_ROLE.values());
            JSONObject obj = new JSONObject();
            obj.put("version", roleV);
            obj.put("data", dicRoles);
            tableInfos.put("roleTable", obj);
        }

        return tableInfos;
    }


    @Transactional
    @ServiceMethod(code = "100", description = "获取用户信息")
    @KafkaProducer(topic = KafkaTopicContext.RED_POINT, event = KafkaEventContext.LOGIN, sendParams = true)
    public JSONObject getInfo(ManagerSocketServer adminSocketServer, JSONObject params) {
        checkNull(params);
        checkNull(params.get("userId"));
        Long userId = params.getLong("userId");
        synchronized (LockUtil.getlock(userId)) {
            JSONObject result = new JSONObject();
            User user = userCacheService.getUserInfoById(userId);
            UserVo vo = new UserVo();
            BeanUtils.copy(user, vo);
            List<UserCapitalVo> userCapitals = userCapitalCacheService.getAllUserCapitalCache(userId);
            if (managerConfigService.getInteger(Config.IP_LOGIN_RISK) == 1) {
                String lastLoginIp = user.getLastLoginIp();
                userCacheService.canLogin(lastLoginIp, userId);
                userCacheService.addIpUser(lastLoginIp, userId);
            }
            UserVip userVipByUserId = userVipService.findUserVipByUserId(userId);
            gameService.getUserAchievement(String.valueOf(userId));
            result.put("userInfo", vo);
            result.put("vipLv", userVipByUserId == null ? 0 : userVipByUserId.getVipLevel());
            result.put("parentId", user.getParentId() == null ? "" : user.getParentId());
            result.put("userCapitals", userCapitals);
            result.put("alipayAuth", user.getAlipayId() == null ? 0 : 1);
            result.put("notice", managerConfigService.getString(Config.HOME_POPUP));
            result.put("exLim", managerConfigService.getDouble(Config.TRAD_MIN));
            result.put("exMax", managerConfigService.getDouble(Config.TRAD_MAX));
            result.put("serverTime", System.currentTimeMillis());
            result.put("tableInfo", syncTableInfo(params));
            result.put("version", authService.getVersion().getVersionName());
            result.put("sy1", managerConfigService.getInteger(Config.PLAYGAME_1_STATUS));
            result.put("sy2", managerConfigService.getInteger(Config.PLAYGAME_2_STATUS));
            result.put("backpackInfo", gameService.getReturnPack(userId));
            result.put("chatInfo", getRecent(10));
            result.put("serverChat", SERVER_CHAT);
            return result;
        }
    }


    @Transactional
    @ServiceMethod(code = "400", description = "查看排行榜信息")
    public JSONObject getTop(ManagerSocketServer adminSocketServer, JSONObject params) {
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
    public JSONObject userUpdateSetting(ManagerSocketServer adminSocketServer, JSONObject params) {
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
    public JSONObject prizeDrawInfo(ManagerSocketServer adminSocketServer, JSONObject params) {
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
    public JSONObject backpack(ManagerSocketServer adminSocketServer, JSONObject params) {
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
    public Object sellItemToSys(ManagerSocketServer adminSocketServer, JSONObject params) {
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
    public JSONObject buyItemBySys(ManagerSocketServer adminSocketServer, JSONObject params) {
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
    public JSONObject addCoin(ManagerSocketServer adminSocketServer, JSONObject params) {
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
    public JSONObject getUserDailyTaskInfo(ManagerSocketServer adminSocketServer, JSONObject params) {
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
    public JSONObject receiveUserDailyTask(ManagerSocketServer adminSocketServer, JSONObject params) {
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
            if (isLook==1 && dailyTask.getCategory().equals("INVITE")){
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
                gameService.addReward(userId, adReward, LogCapitalTypeEnum.daily_task);
                result.put("rewardInfo", adReward);
            } else {
                result.put("rewardInfo", rewards);
                gameService.addReward(userId, rewards, LogCapitalTypeEnum.daily_task);
            }

            result.put("taskId", taskId);
            updateUserTask(userId, taskId);
            return result;
        }

    }

    @Transactional
    @ServiceMethod(code = "1151", description = "每日任务签到奖励")
    @KafkaProducer(topic = KafkaTopicContext.RED_POINT, event = KafkaEventContext.DO_DAILY_TASK, sendParams = true)
    public JSONObject receiveUserDailyTaskSign(ManagerSocketServer adminSocketServer, JSONObject params) {
        checkNull(params);
        checkNull(params.get("userId"));
        Long userId = params.getLong("userId");
        JSONObject result = new JSONObject();
        synchronized (LockUtil.getlock(userId.toString())) {
            JSONArray reward = JSONArray.parseArray(managerConfigService.getString(Config.SIGN_REWARD));
            gameService.addReward(userId, reward, null);
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
    public Object getItemNumber(ManagerSocketServer adminSocketServer, JSONObject params) {
        checkNull(params);
        checkNull(params.get("userId"));
        Long userId = params.getLong("userId");
        String itemId = params.getString("itemId");
        return getItemNumber(userId, itemId);
    }

    public int getItemNumber(Long userId, String itemId) {
        Map<String, Backpack> userBackpack = gameService.getUserBackpack(userId.toString());
        if (userBackpack.containsKey(itemId)) {
            return userBackpack.get(itemId).getItemNumber();
        }
        return 0;
    }


   /* @Transactional
    @ServiceMethod(code = "028", description = "购买靓号")
    public Object buyGoodNo(ManagerSocketServer adminSocketServer, JSONObject params) {
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
    public void syncOffline(ManagerSocketServer adminSocketServer, JSONObject params) {
        checkNull(params);
        checkNull(params.get("pl"), params.get("userId"));

    }

    @Transactional
    @ServiceMethod(code = "030", description = "支付宝绑定用户信息")
    public JSONObject getAlipayUserInfo(ManagerSocketServer adminSocketServer, JSONObject params) throws AlipayApiException {
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
    public JSONObject selectItem(ManagerSocketServer adminSocketServer, JSONObject params) {
        JSONObject result = new JSONObject();
        int random = 9;
        Long userId = params.getLong("userId");
        synchronized (LockUtil.getlock(userId)){
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
    public JSONObject useItem(ManagerSocketServer adminSocketServer, JSONObject params) {
        JSONObject result = new JSONObject();
        Long goodNoId = params.getLong("data");
        Long userId = params.getLong("userId");
        synchronized (LockUtil.getlock(userId)){
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
            goodNoService.updateStatus(goodNoId,0);
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
    @ServiceMethod(code = "035", description = "商店购买")
    @KafkaProducer(topic = KafkaTopicContext.RED_POINT, event = KafkaEventContext.SHOP_BUY, sendParams = true)
    public JSONArray buy(ManagerSocketServer adminSocketServer, JSONObject params) {
        checkNull(params);
        checkNull(params.get("userId"), params.get("id"), params.get("type"), params.get("number"));
        Long userId = params.getLong("userId");
        synchronized (LockUtil.getlock(userId)){
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
                int userItemNumber = gameService.getUserItemNumber(userId, dicShop.getUseItemId().toString());
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
            gameService.addReward(userId, array, LogCapitalTypeEnum.SHOPPING_GET);
            return array;
        }
    }

    /**
     * 捐赠道具
     *
     * @param adminSocketServer
     * @param params
     * @return
     */
    @Transactional
    @ServiceMethod(code = "047", description = "捐赠道具")
    public JSONArray donateItem(ManagerSocketServer adminSocketServer, JSONObject params) throws Exception {
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


    public void pushBackpackUpdate(Long userId, String id, int number, int type) {
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
    public Object shopInfo(ManagerSocketServer adminSocketServer, JSONObject params) {
        Long userId = params.getLong("userId");
        int shopType = params.getIntValue("type");
        List<DicShop> shopInfo = PlayGameService.DIC_SHOP_LIST.get(String.valueOf(shopType));
        if (shopInfo == null) {
            return new ArrayList<>();
        }
        return shopInfo;
    }


    @Transactional
    @ServiceMethod(code = "040", description = "一键领取友情值和广告收益")
    public Object receiveAdIncome(ManagerSocketServer adminSocketServer, JSONObject params) {
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
    public Object getMyInfo(ManagerSocketServer adminSocketServer, JSONObject params) {
        Long userId = params.getLong("userId");
        double todayMyGetAnima = userCacheService.getTodayMyGetAnima(userId);
        UserStatistic userStatistic = gameService.getUserStatistic(String.valueOf(userId));
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
        return result;
    }

    @Transactional
    @ServiceMethod(code = "045", description = "修改昵称")
    public Object updateName(ManagerSocketServer adminSocketServer, JSONObject params) {
        String name = params.getString("name");
        if (name.length()>16){
            throwExp("长度超出限制");
        }
        Long userId = params.getLong("userId");
        userService.updateUserName(name, userId);
        return name;
    }


    @Transactional
    @ServiceMethod(code = "046", description = "合成道具")
    @KafkaProducer(topic = KafkaTopicContext.RED_POINT, event = KafkaEventContext.SYN, sendParams = true)
    public Object syn(ManagerSocketServer adminSocketServer, JSONObject params) {
        String resultId = params.getString("itemId");
        int number = params.getIntValue("number");
        Long userId = params.getLong("userId");
        synchronized (LockUtil.getlock(userId)){
            if (number < 0 || number > 99) {
                throwExp("数量区间不合理");
            }
            if (!PlayGameService.itemMap.containsKey(resultId) || PlayGameService.itemMap.get(resultId).getCanSyn() == 0) {
                throwExp("道具ID有误");
            }
            JSONArray synUse = PlayGameService.itemMap.get(resultId).getSynUse();
            int finalNumber = 0;
            Random random = new Random();
            for (int i = 0; i < number; i++) {
                int k = random.nextInt(100) + 1;
                if (k <= PlayGameService.itemMap.get(resultId).getSynRate()) {
                    finalNumber++;
                }
            }
            for (Object o : synUse) {
                Long useId = Long.parseLong(o.toString());
                gameService.checkUserItemNumber(userId, String.valueOf(useId), number);
                gameService.updateUserBackpack(userId, String.valueOf(useId), -number, LogUserBackpackTypeEnum.use);
            }
            gameService.updateUserBackpack(userId, resultId, finalNumber, LogUserBackpackTypeEnum.syn);
            JSONObject result = new JSONObject();
            result.put("number", finalNumber);
            result.put("itemId", resultId);
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
    public Object chat(ManagerSocketServer adminSocketServer, JSONObject params) {
        checkNull(params);
        checkNull(params.get("userId"), params.get("text"), params.get("type"));
        Long userId = params.getLong("userId");
        synchronized (LockUtil.getlock(userId)){
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
            Push.push(PushCode.chat, null, obj);
            return new JSONObject();
        }
    }


}




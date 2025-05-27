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

import java.math.BigDecimal;
import java.util.*;

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
    private GoodNoService goodNoService;

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
        String cardV = managerConfigService.getString(Config.CARD_VERSION);
        String equipmentV = managerConfigService.getString(Config.EQUIPMENT_VERSION);
        //   String checkpointV = managerConfigService.getString(Config.CHECKPOINT_VERSION);
        String equSynV = managerConfigService.getString(Config.EQU_SYN_VERSION);
        String mineV = managerConfigService.getString(Config.MINE_VERSION);
        String petV = managerConfigService.getString(Config.PET_VERSION);
        String artifactV = managerConfigService.getString(Config.ARTIFACT_VERSION);
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
            if (userId != null) {
                User user = userCacheService.getUserInfoById(userId);
                UserVo vo = new UserVo();
                BeanUtils.copy(user, vo);
                List<String> redMinderList = userCacheService.getPlayerRedReminderList(userId);
                List<UserCapitalVo> userCapitals = userCapitalCacheService.getAllUserCapitalCache(userId);
                if (managerConfigService.getInteger(Config.IP_LOGIN_RISK) == 1) {
                    String lastLoginIp = user.getLastLoginIp();
                    userCacheService.canLogin(lastLoginIp, userId);
                    userCacheService.addIpUser(lastLoginIp, userId);
                }
                gameService.getUserAchievement(String.valueOf(userId));
                JSONObject lvInfo = new JSONObject();
                result.put("userInfo", vo);
                result.put("parentId", user.getParentId() == null ? "" : user.getParentId());
                result.put("lvInfo", lvInfo);
                result.put("userCapitals", userCapitals);
                result.put("alipayAuth", user.getAlipayId() == null ? 0 : 1);
                result.put("AdLookInfo", gameService.getUserAdCountInfo(userId));
                result.put("redMinderList", redMinderList);
                result.put("notice", managerConfigService.getString(Config.HOME_POPUP));
                result.put("exLim", managerConfigService.getDouble(Config.TRAD_MIN));
                result.put("exMax", managerConfigService.getDouble(Config.TRAD_MAX));
                result.put("serverTime", System.currentTimeMillis());
                result.put("tableInfo", syncTableInfo(params));
                result.put("version", authService.getVersion().getVersionName());
                result.put("sy1", managerConfigService.getInteger(Config.PLAYGAME_1_STATUS));
                result.put("sy2", managerConfigService.getInteger(Config.PLAYGAME_2_STATUS));
                if (user.getAuthentication() == 0) {
                    result.put("realNameReward", JSONArray.parseArray(managerConfigService.getString(Config.REAL_NAME_REWARD)));
                }
                result.put("backpackInfo", getBackpack(userId).get("backPackInfo"));
            } else {
                throwExp("获取用户信息失败！");
            }
            return result;
        }
    }


    @Transactional
    @ServiceMethod(code = "400", description = "查看排行榜信息")
    public JSONObject getTop(ManagerSocketServer adminSocketServer, JSONObject params) {
        checkNull(params);
        checkNull(params.get("type"));
        Long userId = params.getLong("userId");
        //开服活动
        JSONObject result = new JSONObject();
        int type = params.getIntValue("type");
        if (type == TopTypeEnum.POPULAR.getValue()) {
            result.put("topList", TopService.TOP_DS);
        }  else if (type == TopTypeEnum.INVITE.getValue()) {
            result.put("topList", TopService.TOP_5);
            OneJuniorNumTopVo myJuniorNum = userStatisticService.findMyJuniorNum(userId);
            myJuniorNum.setNum(myJuniorNum.getNum()+myJuniorNum.getNum2());
            result.put("my", myJuniorNum);
        } else if (type == TopTypeEnum.TOWER_TOP.getValue()) {
            result.put("topList", TopService.TOWER_TOP);
        } else if (type == TopTypeEnum.POWER.getValue()) {
            result.put("topList", TopService.TOP_4);
        } else if (type == TopTypeEnum.CHECK_POINT.getValue()) {
            result.put("topList", TopService.TOP_FRIEND);
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
       /* String userId = params.getString("userId");
        String itemId = params.getString("itemId");
        int number = params.getIntValue("num");
        int type = params.getIntValue("type");
        Map<String, Backpack> myBack = gameService.getUserBackpack(userId);
        if (!myBack.containsKey(itemId)) {
            throwExp("没有该道具");
        }
        if (myBack.get(itemId).getItemNumber() < number) {
            throwExp(PlayGameService.itemMap.get(itemId).getName() + "数量不足");
        }

        BigDecimal onePrice = type == 0 ? PlayGameService.itemMap.get(itemId).getPrice() : PlayGameService.itemMap.get(itemId).getMagicPrice();
        String orderNo = OrderUtil.getOrder5Number();
        BigDecimal totalAmount = onePrice.multiply(new BigDecimal(String.valueOf(number)));
        Long dataId = sellSysRecordService.addRecord(Long.parseLong(userId), Long.parseLong(itemId), number, totalAmount, orderNo);
        gameService.updateUserBackpack(userId, itemId, -number, LogUserBackpackTypeEnum.sell_sys);
        int capitalType = type == 0 ? UserCapitalTypeEnum.currency_2.getValue() : UserCapitalTypeEnum.magic.getValue();
        if (itemId.equals("47")) {
            userCapitalService.addUserBalanceBySellToSys2(totalAmount, Long.parseLong(userId), orderNo, dataId, capitalType);
        } else if (itemId.equals("48")) {
            userCapitalService.addUserBalanceBySellToSys3(totalAmount, Long.parseLong(userId), orderNo, dataId, capitalType);
        } else {
            userCapitalService.addUserBalanceBySellToSys(totalAmount, Long.parseLong(userId), orderNo, dataId, capitalType);
        }

        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(Long.parseLong(userId), capitalType);
        JSONObject pushData = new JSONObject();
        pushData.put("userId", userId);
        pushData.put("capitalType", capitalType);
        pushData.put("balance", userCapital.getBalance());
        Push.push(PushCode.updateUserCapital, managerSocketService.getServerIdByUserId(Long.parseLong(userId)), pushData);*/
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


    @Transactional
    @ServiceMethod(code = "115", description = "领取每日任务奖励")
    @KafkaProducer(topic = KafkaTopicContext.RED_POINT, event = KafkaEventContext.DO_DAILY_TASK, sendParams = true)
    public JSONObject receiveUserDailyTask(ManagerSocketServer adminSocketServer, JSONObject params) {
        checkNull(params);
        checkNull(params.get("userId"), params.get("taskId"));
        Long userId = params.getLong("userId");
        synchronized (LockUtil.getlock(userId.toString())) {
            String taskId = params.getString("taskId");
            boolean doubleReceive = params.containsKey("isLook") && params.getIntValue("isLook") == 1;
            DailyTask dailyTask = PlayGameService.dailyTaskInfo.get(taskId);
            if (dailyTask == null) {
                throwExp("非法请求");
            }
            UserDailyTaskVo userTaskById = cardGameCacheService.getUserTaskById(userId, taskId);
            if (userTaskById.getStatus() == 2) {
                throwExp("已领取过该奖励");
            }
            JSONArray rewards = dailyTask.getReward();
            gameService.addReward(userId, rewards, LogCapitalTypeEnum.daily_task);
            JSONObject result = new JSONObject();
            result.put("rewardInfo", rewards);
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
            gameService.addReward(userId,reward,null);
            result.put("reward",reward);
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


    @Transactional
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
    }

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


    public JSONObject useItem(String itemId, String userId, int number) {
        JSONObject result = new JSONObject();
        JSONArray reward = new JSONArray();
        result.put("isShow", 0);
        if (PlayGameService.itemMap.get(itemId).getSynResultId() != null && PlayGameService.itemMap.get(itemId).getSynResultId().toString().equals(ItemIdEnum.MONEY_1.getValue())) {
            JSONObject data = new JSONObject();
            BigDecimal all = BigDecimal.ZERO;
            //铜钱礼包
            for (int i = 0; i < number; i++) {
                BigDecimal coin = BigDecimal.valueOf(500);
                if (itemId.equals(ItemIdEnum.CY1_PACK_1.getValue())) {
                    coin = BigDecimal.valueOf(1000L);
                } else if (itemId.equals(ItemIdEnum.CY1_PACK_2.getValue())) {
                    coin = BigDecimal.valueOf(5000L);
                } else if (itemId.equals(ItemIdEnum.CY1_PACK_3.getValue())) {
                    coin = BigDecimal.valueOf(50000L);
                }
                all = all.add(coin);
            }

            JSONObject addReward = new JSONObject();
            addReward.put("type", 1);
            addReward.put("id", ItemIdEnum.MONEY_1.getValue());
            addReward.put("number", all);
            reward.add(addReward);
            gameService.addReward(Long.valueOf(userId), reward, LogCapitalTypeEnum.from_item);
            result.put("isShow", 1);
        } else if (PlayGameService.itemMap.get(itemId).getSynResultId() != null && PlayGameService.itemMap.get(itemId).getSynResultId().toString().equals(ItemIdEnum.CARD_EXP.getValue())) {
            JSONObject data = new JSONObject();
            Long all = 0l;
            for (int i = 0; i < number; i++) {
                Long exp = 1L;
                if (itemId.equals(ItemIdEnum.CARD_EXP_1.getValue())) {
                    exp = 1000L;
                } else if (itemId.equals(ItemIdEnum.CARD_EXP_2.getValue())) {
                    exp = 5000L;
                } else if (itemId.equals(ItemIdEnum.CARD_EXP_3.getValue())) {
                    exp = 50000L;
                }
                all += exp;
            }
            JSONObject addReward = new JSONObject();
            addReward.put("type", 1);
            addReward.put("id", ItemIdEnum.CARD_EXP.getValue());
            addReward.put("number", all);
            reward.add(addReward);
            result.put("isShow", 1);
        }
        result.put("reward", reward);
        gameService.updateUserBackpack(userId, itemId, -number, LogUserBackpackTypeEnum.use);
        return result;
    }


    public void pushAddNewPlayerCard(Long userId, List<PlayerCard> cards) {
        List<PlayerCardCodexVo> vos = new ArrayList<>();
        for (PlayerCard card : cards) {
            PlayerCardCodexVo vo = new PlayerCardCodexVo();
            BeanUtils.copy(card, vo);
            vos.add(vo);
        }
        JSONObject pushDate = new JSONObject();
        pushDate.put("userId", userId);
        pushDate.put("cards", vos);
        Push.push(PushCode.updateRoleCard, managerSocketService.getServerIdByUserId(userId), pushDate);
    }






    @Transactional
    @ServiceMethod(code = "035", description = "商店购买")
    @KafkaProducer(topic = KafkaTopicContext.RED_POINT, event = KafkaEventContext.SHOP_BUY, sendParams = true)
    public JSONArray buy(ManagerSocketServer adminSocketServer, JSONObject params) {
        checkNull(params);
        checkNull(params.get("userId"), params.get("id"), params.get("type"),params.get("number"));
        Long userId = params.getLong("userId");
        String id = params.getString("id");
        String type = params.getString("type");
        int number = params.getIntValue("number");
        if (!PlayGameService.DIC_SHOP_MAP.containsKey(type) || !PlayGameService.DIC_SHOP_MAP.get(type).containsKey(id)){
            throwExp("异常请求");
        }
        DicShop dicShop = PlayGameService.DIC_SHOP_MAP.get(type).get(id);
        int price = dicShop.getPrice()*number;
        BigDecimal amount = new BigDecimal(String.valueOf(price ));
        if (dicShop.getUseItemId()==1 || dicShop.getUseItemId()==2){
            UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId,dicShop.getUseItemId().intValue());
            if (userCapital.getBalance().compareTo(amount)<0){
                throwExp("余额不足");
            }else{
                String orderNo = OrderUtil.getOrder5Number();
                userCapitalService.subUserBalanceByShopping(userId,amount,orderNo,null,dicShop.getUseItemId().intValue(),LogCapitalTypeEnum.shopping);
                pushCapitalUpdate(userId,dicShop.getUseItemId().intValue());
            }
        }else{
            int userItemNumber = gameService.getUserItemNumber(userId, dicShop.getUseItemId().toString());
            if (userItemNumber<price){
                throwExp(PlayGameService.itemMap.get(dicShop.getUseItemId().toString()).getName()+"不足");
            }else{
                gameService.updateUserBackpack(userId,dicShop.getUseItemId().toString(),-price,LogUserBackpackTypeEnum.use);
            }
        }
        gameService.updateUserBackpack(userId,dicShop.getItemId().toString(),number,LogUserBackpackTypeEnum.shopping);
        JSONObject result = new JSONObject();
        JSONArray array = new JSONArray();
        result.put("type",1);
        result.put("id",dicShop.getItemId());
        result.put("number",number);
        array.add(result);
        return array;
    }

    /**
     * 捐赠道具
     * @param adminSocketServer
     * @param params
     * @return
     */
    @Transactional
    @ServiceMethod(code = "047", description = "捐赠道具")
    public JSONArray donateItem(ManagerSocketServer adminSocketServer, JSONObject params) {
        //捐赠系统回收 id为30 文房四宝
        //每次捐赠可以获得一个道具 和一些数额（待定）的金币
        //减掉该用户的道具
        deleteItem(params);
        //加货币
        addGold(params);
        BigDecimal gold = BigDecimal.valueOf(1000);
        JSONObject result = new JSONObject();
        JSONArray array = new JSONArray();
        result.put("type",1);
        result.put("gold",gold);
        array.add(result);
        return array;
    }

    private void addGold(JSONObject params) {
        Map<String,Object> parameters = new HashedMap<>();
        parameters.put("userId", params.getLong("userId"));
        UserCapital userCapital = userCapitalService.findOne(parameters);
        parameters.put("capitalType", "2");
        parameters.put("balance",userCapital.getBalance().add(BigDecimal.valueOf(100)));
        params.put("obj",parameters);
        userCapitalService.betUpdateBalance(params);
    }

    private void deleteItem(JSONObject params) {
        Map<String, Object> mapParams = new HashMap<>();
        mapParams.put("userId",params.getLong("userId"));
        mapParams.put("itemId",30);
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
        }  else if (shopType == ShopTypeEnum.PET.getValue()) {
            //龙驹玉商城
            gameService.checkUserItemNumber(userId, ItemIdEnum.BUY_PET.getValue(), userShopVo.getPrice());
            String orderNo = OrderUtil.getOrder5Number();
            //增加记录
            Long dataId = shoppingRecordService.addRecord(userId, userShopVo.getItemEquId(), number, orderNo, BigDecimal.valueOf(userShopVo.getPrice()), shopType);
            gameService.updateUserBackpack(userId, ItemIdEnum.BUY_PET.getValue(), -userShopVo.getPrice(), LogUserBackpackTypeEnum.buy);
            addLjyNumber(userShopVo.getPrice());
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
        Map<String, DicShop> shopInfo = PlayGameService.DIC_SHOP_MAP.get(String.valueOf(shopType));
        JSONObject result = new JSONObject();
        return shopInfo.values();
    }



    @Transactional
    @ServiceMethod(code = "040", description = "一键领取友情值和广告收益")
    public Object receiveAdIncome(ManagerSocketServer adminSocketServer, JSONObject params) {
        String userId = params.getString("userId");
        synchronized (LockUtil.getlock(userId)) {
            UserStatistic userStatistic = gameService.getUserStatistic(userId);
            BigDecimal nowIncome = userStatistic.getGetIncome();
            BigDecimal getAnima = userStatistic.getGetAnima();
            if (nowIncome.compareTo(new BigDecimal("20")) > 0) {
                String orderNo = OrderUtil.getOrder5Number();
                Long dataId = convertIncomeRecordService.addRecord(Long.parseLong(userId), orderNo, nowIncome, userStatistic.getGetIncome(), BigDecimal.ZERO);
                PlayGameService.userStatisticMap.get(userId).setGetIncome(BigDecimal.ZERO);
                userCapitalService.addUserBalanceByStatic(Long.parseLong(userId), orderNo, dataId, nowIncome);
                pushCapitalUpdate(Long.valueOf(userId), UserCapitalTypeEnum.rmb.getValue());
            }

            userStatisticService.updateStatic(PlayGameService.userStatisticMap.get(userId));
            JSONObject result = new JSONObject();
            result.put("canReceiveIncome", userStatistic.getGetIncome());
            result.put("friendNumber", userStatistic.getGetAnima());
            return result;
        }
    }





    @Transactional
    @ServiceMethod(code = "044", description = "我的信息")
    public Object getMyInfo(ManagerSocketServer adminSocketServer, JSONObject params) {
        Long userId = params.getLong("userId");
        UserIncomeStatement byUserIdAndYmd = userIncomeStatementService.findByUserIdAndYmd(userId);
        UserStatistic userStatistic = gameService.getUserStatistic(String.valueOf(userId));
        JSONObject result = new JSONObject();
        result.put("today", byUserIdAndYmd == null ? BigDecimal.ZERO : byUserIdAndYmd.getOneIncome().add(byUserIdAndYmd.getTwoIncome()));
        result.put("all", userStatistic.getGetAllIncome().setScale(2, BigDecimal.ROUND_DOWN));
        User user = userCacheService.getUserInfoById(userId);
        JSONObject parentInfo = new JSONObject();
        parentInfo.put("userNo", "");
        parentInfo.put("headImageUrl", "");
        parentInfo.put("wx", "");
        parentInfo.put("qq","");
        parentInfo.put("name","暂无");
        if (user != null && user.getParentId() != null) {
            User parent = userCacheService.getUserInfoById(user.getParentId());
            if (parent != null) {
                parentInfo.put("userNo", parent.getUserNo());
                parentInfo.put("headImageUrl", parent.getHeadImageUrl());
                parentInfo.put("wx", parent.getWechatId());
                parentInfo.put("qq",parent.getQq());
                parentInfo.put("name",parent.getName());
            }
        }
        result.put("inviterInfo",parentInfo);
        result.put("isChannel", 0);
        if (user.getIsChannel() == 1) {
            result.put("isChannel", 1);
        }
        UserStatistic byUserId = userStatisticService.findByUserId(userId);
        int number = byUserId.getOneJuniorNum()+byUserId.getTwoJuniorNum();
        result.put("number",number);
        return result;
    }

    @Transactional
    @ServiceMethod(code = "045", description = "修改昵称")
    public Object updateName(ManagerSocketServer adminSocketServer, JSONObject params) {
        String name = params.getString("name");
        Long userId = params.getLong("userId");
        userService.updateUserName(name, userId);
        return name;
    }

    @Transactional
    @ServiceMethod(code = "046", description = "合成道具")
    @KafkaProducer(topic = KafkaTopicContext.RED_POINT, event = KafkaEventContext.SYN, sendParams = true)
    public Object syn(ManagerSocketServer adminSocketServer, JSONObject params) {
        String lId = params.getString("itemId");
        int number = params.getIntValue("number");
        Long userId = params.getLong("userId");
        if (number<0||number>99){
            throwExp("数量区间不合理");
        }
        if (!PlayGameService.itemMap.containsKey(lId) || PlayGameService.itemMap.get(lId).getCanSyn()==0){
            throwExp("道具ID有误");
        }
        String rId = PlayGameService.itemMap.get(lId).getSynUse().toString();
        gameService.checkUserItemNumber(userId,lId,number);
        gameService.checkUserItemNumber(userId,rId,number);
        int finalNumber = 0;
        Random random = new Random();
        for (int i = 0; i < number; i++) {
            int k = random.nextInt(100) + 1;
            if (k<PlayGameService.itemMap.get(lId).getSynRate()){
                finalNumber++;
            }
        }
        gameService.updateUserBackpack(userId,lId,-number,LogUserBackpackTypeEnum.use);
        gameService.updateUserBackpack(userId,rId,-number,LogUserBackpackTypeEnum.use);
        gameService.updateUserBackpack(userId,PlayGameService.itemMap.get(lId).getSynResultId(),finalNumber,LogUserBackpackTypeEnum.use);
        JSONObject result = new JSONObject();
        result.put("number",finalNumber);
        result.put("itemId",Integer.parseInt(PlayGameService.itemMap.get(lId).getSynResultId()));
        return result;
    }

    public void checkBalance(String userId,BigDecimal price,UserCapitalTypeEnum em){
        checkBalance(Long.parseLong(userId),price,em);
    }

    public void checkBalance(Long userId,BigDecimal price,UserCapitalTypeEnum em){
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId,em.getValue());
        if (userCapital.getBalance().compareTo(price)<0){
            throwExp(em.getName()+"不足");
        }
    }


}




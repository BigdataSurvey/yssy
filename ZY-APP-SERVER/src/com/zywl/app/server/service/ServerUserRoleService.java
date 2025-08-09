package com.zywl.app.server.service;

import cn.hutool.core.date.DateTime;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ijpay.core.utils.DateTimeZoneUtil;
import com.live.app.ws.bean.Command;
import com.live.app.ws.enums.PushCode;
import com.live.app.ws.enums.TargetSocketType;
import com.live.app.ws.util.CommandBuilder;
import com.live.app.ws.util.Executer;
import com.live.app.ws.util.Push;
import com.zywl.app.base.bean.*;
import com.zywl.app.base.bean.vo.SendGiftRecordVo;
import com.zywl.app.base.bean.vo.UserVo;
import com.zywl.app.base.constant.RedisKeyConstant;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.*;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.cache.GameCacheService;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.cache.impl.RedisService;
import com.zywl.app.defaultx.enmus.ActivityAddPointEventEnum;
import com.zywl.app.defaultx.service.*;
import com.zywl.app.server.context.MessageCodeContext;
import com.zywl.app.server.huifu.HfScanPay;
import com.zywl.app.server.socket.AppSocket;
import com.zywl.app.server.util.RequestManagerListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


@Service
@ServiceClass(code = MessageCodeContext.USER_ROLE)
public class ServerUserRoleService extends BaseService {

    @Autowired
    private UserGiftService userGiftService;

    @Autowired
    private UserCacheService userCacheService;

    @Autowired
    private UserVipService userVipService;
    @Autowired
    private UserService userService;

    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private CashRecordService cashRecordService;

    @Autowired
    private UserRoleAdService userRoleAdService;

    @Autowired
    private DicRoleService dicRoleService;

    @Autowired
    private TsgPayOrderService tsgPayOrderService;

    @Autowired
    private SendGiftRecordService sendGiftRecordService;

    @Autowired
    private ActiveGiftRecordService activeGiftRecordService;

    @Autowired
    private ServerConfigService serverConfigService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RedisService redisService;
    @Autowired
    private GameCacheService gameCacheService;

    @Autowired
    private UserStatisticService userStatisticService;

    public static final Map<String, DicRole> DIC_ROLE = new ConcurrentHashMap<>();

    public static final String VERSION = "v1.0";
    public static final String TYPE = "10006";
    public static final String USER_ID = "88162050";

    public static final String SECRET = "e7a15a9d4e6946bb97edf329035297d1";


    @PostConstruct
    public void _serverUserRoleService() {
        initRole();
    }

    public void initRole() {
        List<DicRole> allRole = dicRoleService.findAllRole();
        allRole.forEach(e -> DIC_ROLE.put(e.getId().toString(), e));
    }

    public String getPayAddress(Long userId, Long productId, BigDecimal price, String ip, BigDecimal allPrice, int number) throws Exception {
        String merchantId = serverConfigService.getString(Config.PAY_MERCHANT_ID);
        String merReqNo = OrderUtil.getOrder5Number();
        String notifyUrl = serverConfigService.getString(Config.PAY_NOTIFY_URL);
        String returnUrl = serverConfigService.getString(Config.PAY_REDIRECT_URL);
        String timeExpire = DateTimeZoneUtil.dateToTimeZone(System.currentTimeMillis() + 1000 * 60 * 3);
        DateTime dateTime = cn.hutool.core.date.DateUtil.parse(timeExpire);
        Date expireDate = new Date(dateTime.getTime());
        tsgPayOrderService.addOrder(userId, merReqNo, productId, price, expireDate, 1, allPrice, number);
        Map<String, Object> data = new HashMap<>();
        data.put("version", VERSION);
        data.put("type", TYPE);
        data.put("userId", USER_ID);
        //data.put("buyerId", String.valueOf(userId));
        data.put("requestNo", merReqNo);
        data.put("amount", String.valueOf(allPrice.multiply(new BigDecimal(100)).setScale(0, RoundingMode.HALF_UP)));
        data.put("callBackURL", notifyUrl);
        data.put("redirectUrl", returnUrl);
        data.put("ip", ip);
        TreeMap<String, Object> treeMap = new TreeMap<>(data);
        StringBuffer stringBuffer = new StringBuffer();
        treeMap.forEach((key, value) -> stringBuffer.append(key).append("=").append(value).append("&"));
        String s = stringBuffer + "key=" + SECRET;
        System.out.println(s);
        String signMd5 = MD5Util.md5(s).toLowerCase();
        data.put("sign", signMd5);
        long time = System.currentTimeMillis();
        JSONObject from = JSONObject.from(data);
        String s1 = from.toJSONString();
        String result = HTTPUtil.postJSON("https://api-kaite.jjoms.com/api/pay", s1);
        System.out.println("请求支付地址耗时:" + (System.currentTimeMillis() - time));
        System.out.println(result);
        if (result == null) {
            throwExp("当前没有可用的支付地址，请联系客服或稍后再试");
        }
        JSONObject jsonResult = JSONObject.parseObject(result);
        if (jsonResult.containsKey("message") && jsonResult.getString("message").equals("000000")) {
            JSONObject returnResult = new JSONObject();
            return jsonResult.getString("payUrl");
        } else {
            logger.error("请求支付接口错误" + result);
        }
        throwExp("当前没有可用的支付地址，请联系客服或稍后再试");
        return null;
    }

    public String getHfPayAddress(Long userId, Long productId, BigDecimal price, BigDecimal allPrice, int number) throws Exception {
        String orderNo = OrderUtil.getOrder5Number();
        String timeExpire = DateTimeZoneUtil.dateToTimeZone(System.currentTimeMillis() + 1000 * 60 * 10);
        DateTime dateTime = cn.hutool.core.date.DateUtil.parse(timeExpire);
        Date expireDate = new Date(dateTime.getTime());
        //通道Id  1 野鸡  2汇付
        tsgPayOrderService.addOrder(userId, orderNo, productId, price, expireDate, 2, allPrice, number);
        String payUrl = null;
        try {
            String sysId = serverConfigService.getString(Config.HF_SYS_ID);
            String privateKey = serverConfigService.getString(Config.HF_RSA_PRIVATE_KEY);
            String publicKey = serverConfigService.getString(Config.HF_RSA_PUBLIC_KEY);
            payUrl = HfScanPay.scanPay(allPrice, serverConfigService.getString(Config.PAY_NOTIFY_HF_URL), orderNo, privateKey, publicKey, sysId);
        } catch (Exception e) {
            e.printStackTrace();
            throwExp("当前没有可用的支付地址，请联系客服或稍后再试");
        }
        return payUrl;
    }

    public String getHfWechatPayAddress(Long userId, Long productId, BigDecimal price, BigDecimal allPrice, int number) throws Exception {
        String orderNo = OrderUtil.getOrder5Number();
        String timeExpire = DateTimeZoneUtil.dateToTimeZone(System.currentTimeMillis() + 1000 * 60 * 10);
        DateTime dateTime = cn.hutool.core.date.DateUtil.parse(timeExpire);
        Date expireDate = new Date(dateTime.getTime());
        //通道Id  1 野鸡  2汇付
        tsgPayOrderService.addOrder(userId, orderNo, productId, price, expireDate, 2, allPrice, number);
        String payUrl = null;
        try {
            String sysId = serverConfigService.getString(Config.HF_SYS_ID);
            String privateKey = serverConfigService.getString(Config.HF_RSA_PRIVATE_KEY);
            String publicKey = serverConfigService.getString(Config.HF_RSA_PUBLIC_KEY);
            payUrl = HfScanPay.scanPay(allPrice, serverConfigService.getString(Config.PAY_NOTIFY_HF_URL), orderNo, privateKey, publicKey, sysId);
        } catch (Exception e) {
            e.printStackTrace();
            throwExp("当前没有可用的支付地址，请联系客服或稍后再试");
        }
        return payUrl;
    }

    @Transactional
    @ServiceMethod(code = "100", description = "购买礼包")
    public Object buyByRmb(final AppSocket appSocket, Command appCommand, JSONObject params) throws Exception {
        checkNull(params);
        checkNull(params.get("giftType"), params.get("number"));
        Long giftType = params.getLong("giftType");
        Long userId = appSocket.getWsidBean().getUserId();
        synchronized (LockUtil.getlock(userId)) {
            if (userCacheService.canPay(userId.toString())) {
                throwExp("请勿频繁发起支付");
            }
            userCacheService.beginPay(userId.toString());
            if (giftType != 1 && giftType != 2) {
                throwExp("参数异常");
            }
            BigDecimal price;
            if (giftType == 1L) {
                price = serverConfigService.getBigDecimal(Config.GIFT_PRICE_1).setScale(2, RoundingMode.HALF_UP);
            } else {
                price = serverConfigService.getBigDecimal(Config.GIFT_PRICE_2).setScale(2, RoundingMode.HALF_UP);
            }
            Random random = new Random();
            int i = random.nextInt(200) + 1;
            BigDecimal subMoney = BigDecimal.valueOf(i).divide(new BigDecimal("100"));
            price = price.subtract(subMoney);
            UserVip userVipByUserId = userVipService.findUserVipByUserId(userId);
            if (isTestUserNo(userId.toString())) {
                if (userVipByUserId.getVipLevel() == 5L) {
                    price = price.multiply(new BigDecimal("0.95")).setScale(0, RoundingMode.HALF_UP);
                }
                if (userVipByUserId.getVipLevel() > 5L) {
                    price = price.multiply(new BigDecimal("0.9")).setScale(0, RoundingMode.HALF_UP);
                }
            }
            int number = params.getIntValue("number");
            if (number > serverConfigService.getInteger(Config.MAX_BUY_GIFT_NUMBER)) {
                throwExp("当前最多可购买" + serverConfigService.getInteger(Config.MAX_BUY_GIFT_NUMBER) + "套体验卡");
            }
            BigDecimal allPrice = price.multiply(BigDecimal.valueOf(number));
            pushOrder(giftType);
            JSONObject result = new JSONObject();
            int channel = serverConfigService.getInteger(Config.PAY_CHANNEL);
            result.put("channel", channel);
            String url = ";";
            if (channel == 1) {
                url = getPayAddress(userId, giftType, price, appSocket.getIp(), allPrice, number);
            } else if (channel == 2) {
                url = getHfPayAddress(userId, giftType, price, allPrice, number);
            } else if (channel == 3) {
                url = getHfWechatPayAddress(userId, giftType, price, allPrice, number);
            }
            result.put("payUrl", url);
            pushOrder(giftType);
            userCacheService.endPay(userId.toString());
            result.put("payType", channel);
            return result;
        }
    }

    public boolean isTestUserNo(String userNo) {
        String string = serverConfigService.getString(Config.TEST_USER_NO);
        String[] split = string.split(",");
        for (String s : split) {
            if (s.equals(userNo)) {
                return true;
            }
        }
        return false;

    }


    public void pushOrder(Long productId) {
        logger.info("推送订单");
        JSONObject pushDate = new JSONObject();
        pushDate.put("productId", productId);
        Push.push(PushCode.syncTsgOrder, null, pushDate);
    }

    @Transactional
    @ServiceMethod(code = "000", description = "购买礼包")
    public Object buy(final AppSocket appSocket, Command appCommand, JSONObject params) throws Exception {
        checkNull(params);
        checkNull(params.get("giftType"));
        int giftType = params.getIntValue("giftType");
        if (giftType != 1 && giftType != 2) {
            throwExp("参数异常");
        }

        Long userId = appSocket.getWsidBean().getUserId();
        //用户Id 插入到参数中 传到manager服务器   code 035011
        params.put("userId", userId);
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("035011", params).build(), new RequestManagerListener(appCommand));
        return async();
    }

    @ServiceMethod(code = "001", description = "获取角色礼包信息")
    public JSONObject getUserConfig(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("type"));
        Long userId = appSocket.getWsidBean().getUserId();
        int type = params.getIntValue("type");
        if (type != 1 && type != 2) {
            throwExp("非法请求");
        }
        UserGift userGift = userGiftService.findUserGift(userId, type);
        params.put("number", 0);
        if (userGift != null) {
            params.put("number", userGift.getGiftNum());
        }
        List<ActiveGiftRecord> userActiveRecords = activeGiftRecordService.findByUserId(userId, type);
        if (userActiveRecords.size() == 0) {
            params.put("status", 0);
        } else {
            params.put("status", 1);
        }
        return params;
    }

    /**
     * 检查玩家的上级和上上级是否都不是渠道主
     * @param user 玩家实体
     * @return true=都不是渠道主，false=至少有一个是渠道主
     */
    private void checkParentNotChannelMaster(User user){
        BigDecimal money = serverConfigService.getBigDecimal(Config.GIFT_PRICE_2_GAME);
        BigDecimal rate = serverConfigService.getBigDecimal(Config.CHANNEL_RATE);
        BigDecimal addMoney = money.multiply(rate);
//        // 1. 校验参数
        if (user.getId() == null || addMoney == null || addMoney.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("参数无效");
        }
        //  获取玩家信息
        User user1 = userService.findById(user.getId());
        if (user1 == null) {
            throw new RuntimeException("玩家不存在");
        }
        //检查上级是否为渠道主
        Long parentId = user.getParentId();
        boolean parentIsChannelMaster  = false;
        if(parentId != null){
            parentIsChannelMaster =  userService.isChannelMaster(parentId,user.getId()) > 0;
        }
        //检查上上级是否为渠道主
        Long grandfaId=user.getGrandfaId();
        boolean  grandfaIdIsChannelMaster = false;
        if(grandfaId != null){
            grandfaIdIsChannelMaster= userService.isChannelMaster(grandfaId,user.getId()) >0;
        }
        //只有两者都不是渠道主时，才给渠道主加收益
        if(!parentIsChannelMaster && !grandfaIdIsChannelMaster){
            // 获取该玩家所属渠道的有效渠道主
            List<User> channelMasterIds = Collections.singletonList(userService.getValidById(user.getId()));
            if(channelMasterIds.isEmpty()){
                return;//没有有效渠道主，无需处理
            }
            // 计算收益金额
            addMoney.multiply(addMoney).divide(new BigDecimal("100"));
            for(User user2 : channelMasterIds){
                UserStatistic userStatistic  =new UserStatistic();
                userStatistic.setUserId(user2.getId());
                userStatistic.setNowChannelIncome(addMoney);
                userStatistic.setCreateSw(BigDecimal.valueOf(0));
                userStatisticService.updateNowChannelIncome(userStatistic);
            }
        }
    }



    @Transactional
    @ServiceMethod(code = "002", description = "激活角色礼包")
    public JSONObject useGift(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("type"));
        Long myId = appSocket.getWsidBean().getUserId();
        synchronized (LockUtil.getlock(myId)) {
            User user = userCacheService.getUserInfoById(myId);
            int type = params.getIntValue("type");
            if (type != 1 && type != 2) {
                throwExp("非法请求");
            }
            UserGift userGift = userGiftService.findUserGift(myId, type);
            if (userGift == null || userGift.getGiftNum() < 1) {
                throwExp("礼包数量不足");
            }
            userGiftService.useGift(myId, type);
            if (type == 1) {
                useSmallGift(myId);
            } else {
                useBigGift(myId);
                if (user.getVip2() == 0) {
                    user.setVip2(1);
                    userService.updateUserVip2(myId);
                    JSONObject pushDate = new JSONObject();
                    pushDate.put("userId", user.getId());
                    user = userCacheService.getUserInfoById(myId);
                    UserVo vo = new UserVo();
                    BeanUtils.copy(user, vo);
                    pushDate.put("userInfo", vo);
                    Push.push(PushCode.updateUserInfo, myId.toString(), pushDate);
                }
                JSONObject info = new JSONObject();
                info.put("userId", myId);
                Activity activity = gameCacheService.getActivity();

                if (activity != null) {
                    if (activity.getAddPointEvent() == ActivityAddPointEventEnum.RMB_BUY_GIFT.getValue()) {
                        //已经激活大礼包的用户 给他上级加积分并存入redis
                        //用户父id的积分
                        gameCacheService.addPoint(myId,5);
                    }
                }
                Activity activity2 = gameCacheService.getActivity2();
                if (activity2 != null) {
                    if (activity2.getAddPointEvent() == ActivityAddPointEventEnum.RMB_BUY_GIFT.getValue()) {
                        gameCacheService.addPoint2(myId);
                    }
                }
                checkParentNotChannelMaster(user);
                addScoreByActive3(user.getId());
            }
            activeGiftRecordService.addRecord(myId, user.getId(), type);
            return params;
        }
    }

    public void addScoreByActive3(Long userId){
        Activity activity = gameCacheService.getActivity3();
        if (activity!=null){
            if (activity.getAddPointEvent() == ActivityAddPointEventEnum.RMB_BUY_GIFT.getValue()){
                User user = userCacheService.getUserInfoById(userId);
                if (user.getParentId()!=null){
                    gameCacheService.addPointMySelf3(user.getParentId(),10);
                }

            }
        }
    }

    public void useSmallGift(Long userId) {
        UserRole byUserIdAndRoleId = userRoleService.findByUserIdAndRoleId(userId, 1);
        if (byUserIdAndRoleId != null) {
            byUserIdAndRoleId.setEndTime(DateUtil.getDateByDay(byUserIdAndRoleId.getEndTime(), 30));
            userRoleService.updateUserRole(byUserIdAndRoleId);
        } else {
            userRoleService.addUserRole(userId, 1L, 30);
        }

    }

    public void useBigGift(Long userId) {
        for (int i = 1; i <= 5; i++) {
            UserRole byUserIdAndRoleId = userRoleService.findByUserIdAndRoleId(userId, i);
            if (byUserIdAndRoleId != null) {
                byUserIdAndRoleId.setEndTime(DateUtil.getDateByDay( 30));
                byUserIdAndRoleId.setHp(240);
                byUserIdAndRoleId.setLastReceiveTime(new Date());
                byUserIdAndRoleId.setStatus(1);
                userRoleService.updateUserRole(byUserIdAndRoleId);

            } else {
                userRoleService.addUserRole(userId, (long) i, 30);
            }
        }
    }


    @Transactional
    @ServiceMethod(code = "003", description = "进入场景查看角色工作信息")
    public Object findWorkingRoles(final AppSocket appSocket, Command appCommand, JSONObject params) {
        Long userId = appSocket.getWsidBean().getUserId();
        synchronized (LockUtil.getlock(userId)) {
            List<UserRole> roles = userRoleService.findWorkingRoles(userId);
            return settleRoleReceive(roles);
        }

    }

    @Transactional
    @ServiceMethod(code = "004", description = "查看我的角色")
    public Object myRoles(final AppSocket appSocket, Command appCommand, JSONObject params) {
        Long userId = appSocket.getWsidBean().getUserId();
        synchronized (LockUtil.getlock(userId)) {
            List<UserRole> roles = userRoleService.findByUserId(userId);
            if (roles.size()>=5){
                User user = userCacheService.getUserInfoById(userId);
                if (user.getVip2()!=1){
                    user.setVip2(1);
                    userService.updateUserVip2(userId);
                }
            }
            return settleRoleReceive(roles);
        }
    }

    public List<UserRole> settleRoleReceive(List<UserRole> roles) {
        List<UserRole> needUpdate = new ArrayList<>();
        for (UserRole userRole : roles) {
            if (userRole.getStatus() == 2) {
                continue;
            }
            Date lastLookTime = userRole.getLastLookTime();
            Date lastReceiveTime = userRole.getLastReceiveTime();
            long hour = (System.currentTimeMillis() - lastReceiveTime.getTime()) / 1000 / 60 / 60;
            if (userRole.getEndTime().getTime() < System.currentTimeMillis()) {
                //角色已到期  则要从到期时间和上次产出时间比较 判断中间隔了几个小时
                hour = (userRole.getEndTime().getTime() - lastReceiveTime.getTime()) / 1000 / 60 / 60;
                userRole.setStatus(2);
            }
            //判断体力是否足够这几个小时的消耗 如果不够 产出的小时数要更少
            DicRole dicRole = DIC_ROLE.get(userRole.getRoleId().toString());
            int oneHourCostHp = dicRole.getCost();
            if (userRole.getHp() < oneHourCostHp * hour) {
                //剩余体力不够
                hour = userRole.getHp() / oneHourCostHp;
            }
            //实际消耗体力
            if (hour >= 1) {
                long useHp = hour * oneHourCostHp;
                userRole.setHp((int) (userRole.getHp() - useHp));
                JSONArray reward = new JSONArray();
                JSONArray dicReward = dicRole.getReward();
                for (Object o : dicReward) {
                    JSONObject info = (JSONObject) o;
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("type", info.getIntValue("type"));
                    jsonObject.put("id", info.getIntValue("id"));
                    Integer oneHourNumber = info.getInteger("number");
                    jsonObject.put("number", oneHourNumber * hour);
                    reward.add(jsonObject);
                }
                JSONArray nowReward = JSONUtil.mergeJSONArray(userRole.getUnReceive(), reward);
                userRole.setUnReceive(nowReward);
                userRole.setLastReceiveTime(DateUtil.getDateByHour(userRole.getLastReceiveTime(), (int) hour));
                needUpdate.add(userRole);
            }
        }
        userRoleService.batchUpdateUserRole(needUpdate);
        return roles;
    }



    @Transactional
    @ServiceMethod(code = "005", description = "设置角色为工作状态")
    public JSONObject working(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("index"), params.get("userRoleId"));
        Long userId = appSocket.getWsidBean().getUserId();
        synchronized (LockUtil.getlock(userId)) {
            int index = params.getIntValue("index");
            Long userRoleId = params.getLong("userRoleId");
            UserRole userRole = userRoleService.findByUserIdAndRoleId(userId, userRoleId);
            UserRole byIndex = userRoleService.findByIndex(userId, index);
            if (byIndex != null) {
                throwExp("该位置已经有角色啦");
            }
            if (userRole == null) {
                throwExp("未查询到角色信息");
            }
            if (userRole.getIndex() != 0 || userRole.getStatus() == 1) {
                throwExp("角色已经在工作啦");
            }
            if (userRole.getStatus() == 2) {
                throwExp("角色已到期");
            }
            userRole.setIndex(index);
            userRole.setStatus(1);
            userRole.setLastReceiveTime(new Date());
            userRole.setLastLookTime(new Date());
            userRoleService.updateUserRole(userRole);
            return params;
        }
    }


    @ServiceMethod(code = "006", description = "查询可选择的角色")
    public Object getNoWorkingRoles(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("index"));
        int index = params.getIntValue("index");
        if (index < 1 || index > 6) {
            throwExp("参数异常");
        }
        Long userId = appSocket.getWsidBean().getUserId();
        List<UserRole> noWorkingRoles = userRoleService.findNoWorkingRolesByIndex(userId, index);
        List<UserRole> list = new ArrayList<>();
        if (noWorkingRoles.size() > 0) {
            list.add(noWorkingRoles.get(0));
        }
        JSONObject result = new JSONObject();
        result.put("roles", list);
        result.put("index", index);
        return result;
    }

    @ServiceMethod(code = "007", description = "补充体力")
    public Object addHp(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        Long userId = appSocket.getWsidBean().getUserId();
        params.put("userId", userId);
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("400001", params).build(), new RequestManagerListener(appCommand));
        return async();
    }

    @ServiceMethod(code = "008", description = "领取产出道具")
    public Object receiveItem(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("userRoleId"));
        Long userId = appSocket.getWsidBean().getUserId();
        params.put("userId", userId);
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("400002", params).build(), new RequestManagerListener(appCommand));
        return async();
    }

    @ServiceMethod(code = "009", description = "查看购买礼包信息")
    public Object buyGiftInfo(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        Long userId = appSocket.getWsidBean().getUserId();
        JSONObject result = new JSONObject();
        User user = userCacheService.getUserInfoById(userId);
        UserVip userVipByUserId = userVipService.findUserVipByUserId(userId);
        BigDecimal rmb1 = serverConfigService.getBigDecimal(Config.GIFT_PRICE_1);
        BigDecimal rmb2 = serverConfigService.getBigDecimal(Config.GIFT_PRICE_2);
        if (isTestUserNo(user.getUserNo())) {
            if (userVipByUserId.getVipLevel() == 5L) {
                rmb1 = rmb1.multiply(new BigDecimal("0.95")).setScale(0, RoundingMode.HALF_UP);
                rmb2 = rmb2.multiply(new BigDecimal("0.95")).setScale(0, RoundingMode.HALF_UP);
            }
            if (userVipByUserId.getVipLevel() > 5L) {
                rmb1 = rmb1.multiply(new BigDecimal("0.9")).setScale(0, RoundingMode.HALF_UP);
                rmb2 = rmb2.multiply(new BigDecimal("0.9")).setScale(0, RoundingMode.HALF_UP);
            }
        }
        result.put("rmbStatus", serverConfigService.getInteger(Config.GIFT_RMB_STATUS));
        result.put("rmbPrice1", rmb1);
        result.put("rmbPrice2", rmb2);


        result.put("gameMoneyStatus", serverConfigService.getInteger(Config.GIFT_GAME_STATUS));
        result.put("gamePrice1", serverConfigService.getBigDecimal(Config.GIFT_PRICE_1_GAME));
        result.put("gamePrice2", serverConfigService.getBigDecimal(Config.GIFT_PRICE_2_GAME));
        return result;
    }

    @Transactional
    @ServiceMethod(code = "010", description = "赠送礼包")
    public Object sendGift(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        int number = params.getIntValue("number");
        String userNo = params.getString("userNo");
        User toUser = userCacheService.getUserInfoByUserNo(userNo);

        if (toUser == null) {
            throwExp("用户不存在");
        }
        Long myId = appSocket.getWsidBean().getUserId();
        synchronized (LockUtil.getlock(myId)) {
            if (Objects.equals(myId, toUser.getId())) {
                throwExp("不能赠送给自己");
            }
            int type = params.getIntValue("type");
            if (type != 1 && type != 2) {
                throwExp("非法请求");
            }
            User user = userCacheService.getUserInfoByUserNo(userNo);
            if (user == null) {
                throwExp("玩家不存在");
            }
            UserGift userGift = userGiftService.findUserGift(myId, type);
            if (userGift == null || userGift.getGiftNum() < number) {
                throwExp("礼包数量不足");
            }
            sendGiftRecordService.addRecord(myId, toUser.getUserNo(), toUser.getId(), type, number);
            userGiftService.sendGift(myId, number, type);
            userGiftService.addGiftNumber(toUser.getId(), number, type);
            JSONObject result = new JSONObject();
            result.put("nowNumber", userGift.getGiftNum() - number);
            return result;
        }
    }

    @ServiceMethod(code = "011", description = "礼包记录")
    public Object GiftRecord(final AppSocket appSocket, Command appCommand, JSONObject params) {
        Long userId = appSocket.getWsidBean().getUserId();
        String toUserNo = params.getString("toUserNo");
        List<SendGiftRecord> list;
        if (toUserNo != null && !toUserNo.equals("")) {
            list = sendGiftRecordService.findByUserIdAndSendNo(userId, toUserNo, params.getIntValue("page"), params.getIntValue("num"));
        } else {
            list = sendGiftRecordService.findByUserId(userId, params.getIntValue("page"), params.getIntValue("num"));
        }
        return list;
    }

    @ServiceMethod(code = "012", description = "接收记录")
    public Object getGiftRecord(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params.get("page"), params.get("num"));
        Long userId = appSocket.getWsidBean().getUserId();
        List<SendGiftRecordVo> list = sendGiftRecordService.findByToUserId(userId, params.getIntValue("page"), params.getIntValue("num"));
        for (SendGiftRecordVo sendGiftRecordVo : list) {
            User user = userCacheService.getUserInfoById(sendGiftRecordVo.getUserId());
            sendGiftRecordVo.setFromUserNo(user.getUserNo());
        }
        return list;
    }

    @ServiceMethod(code = "013", description = "看广告领取角色")
    public Object receiveFreeRole(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        Long userId = appSocket.getWsidBean().getUserId();
        params.put("userId", userId);
        long freeRoleNumber = userRoleService.findFreeRoleNumber();
        if (freeRoleNumber>=serverConfigService.getInteger(Config.FREE_ROLE_NUM)){
            throwExp("角色已经领取完啦");
        }
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("400004", params).build(), new RequestManagerListener(appCommand));
        return async();
    }

    @ServiceMethod(code = "014", description = "看广告加角色体力")
    public Object lookAddHp(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        Long userId = appSocket.getWsidBean().getUserId();
        UserRole freeUserRole = userRoleService.findByUserIdAndRoleId(userId, 6L);
        if (freeUserRole == null) {
            throwExp("未拥有该角色");
        }
        params.put("userId", userId);
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("400003", params).build(), new RequestManagerListener(appCommand));
        return async();
    }


    @ServiceMethod(code = "015", description = "是否领取第六个角色")
    public Object canReceiveFree(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        Long userId = appSocket.getWsidBean().getUserId();
        UserRole freeUserRole = userRoleService.findByUserIdAndRoleId(userId, 6L);
        JSONObject result = new JSONObject();
        if (freeUserRole == null) {
            result.put("canReceive", 1);
        } else {
            result.put("canReceive", 0);
        }
        return result;
    }

    @ServiceMethod(code = "016", description = "查看角色广告信息")
    public Object adInfo(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        Long userId = appSocket.getWsidBean().getUserId();
        UserRoleAd userRoleAd = userRoleAdService.findByUserIdAndYmd(userId);
        if (userRoleAd.getCanLook() < 4) {
            //小于4证明可以倒计时累计广告 判断本次请求时是否已经累计了新的广告数量
            Date lastLookTime = userRoleAd.getLastTime();
            if ((System.currentTimeMillis() - lastLookTime.getTime()) / 1000 > 90 * 60) {
                //如果当前时间 比上次请求时间超过了1个半小时 并且之前的次数也不够4次 那么这会要加次数
                long count =( (System.currentTimeMillis() - lastLookTime.getTime()) / 1000) / (90 * 60);
                userRoleAd.setCanLook((int) (userRoleAd.getCanLook() + count));
                if (userRoleAd.getCanLook() == 4) {
                    userRoleAd.setLastTime(new Date());
                } else {
                    userRoleAd.setLastTime(DateUtil.getDateByM(userRoleAd.getLastTime(), (int) (90 * count*60)));
                }
            }
        } else {
            userRoleAd.setLastTime(new Date());
        }
        System.out.println(userRoleAd.getLook());
        System.out.println(userRoleAd.getCanLook());
        if ((userRoleAd.getLook()+userRoleAd.getCanLook())>10){
            userRoleAd.setCanLook(10-userRoleAd.getLook());
        }
        userRoleAdService.update(userRoleAd);
        JSONObject result = new JSONObject();
        result.put("canLook",userRoleAd.getCanLook());
        result.put("todayRemaining",10-userRoleAd.getLook());
        result.put("nextAdTime",DateUtil.getDateByM(userRoleAd.getLastTime(),90*60).getTime());
        return result;
    }

    @ServiceMethod(code = "017", description = "角色商城信息")
    public Object roleShopInfo(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        Long userId = appSocket.getWsidBean().getUserId();
        List<DicRole> allRole = dicRoleService.findAllRole();
        JSONArray array = new JSONArray();
        for (DicRole dicRole : allRole) {
            JSONObject info = new JSONObject();
            info.put("roleId",dicRole.getId());
            info.put("price",serverConfigService.getBigDecimal(Config.GIFT_PRICE_1_GAME));
            array.add(info);
        }
        return array;
    }


    @ServiceMethod(code = "018", description = "购买角色")
    public Object buyRole(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("roleId"));
        Long userId = appSocket.getWsidBean().getUserId();
        params.put("userId",userId);
        Long roleId = params.getLong("roleId");
        if (roleId<1 || roleId>5){
            throwExp("非法请求");
        }
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("400005", params).build(), new RequestManagerListener(appCommand));
        return async();
    }

    public static void main(String[] args) {
        long a = 15560/5400;
        System.out.println(a);
    }

}

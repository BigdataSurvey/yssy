package com.zywl.app.server.service;

import cn.hutool.core.date.DateTime;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ijpay.core.utils.DateTimeZoneUtil;
import com.live.app.ws.bean.Command;
import com.live.app.ws.enums.TargetSocketType;
import com.live.app.ws.util.CommandBuilder;
import com.live.app.ws.util.Executer;
import com.zywl.app.base.bean.*;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.*;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.service.*;
import com.zywl.app.server.context.MessageCodeContext;
import com.zywl.app.server.socket.AppSocket;
import com.zywl.app.server.util.RequestManagerListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
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
    private UserRoleService userRoleService;

    @Autowired
    private DicRoleService dicRoleService;

    @Autowired
    private TsgPayOrderService tsgPayOrderService;

    @Autowired
    private ServerConfigService serverConfigService;

    public static final Map<String, DicRole> DIC_ROLE = new ConcurrentHashMap<>();

    public static final String VERSION ="v1.0";
    public static final String TYPE = "10005";
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

    public Object getPayAddress(Long userId, JSONObject params,String ip) throws Exception {
        checkNull(params);
        checkNull(params.get("giftType"));
        Long productId = params.getLong("giftType");
        BigDecimal price ;
        String goodsName ;
        if (productId==1L){
            price = serverConfigService.getBigDecimal(Config.GIFT_PRICE_1).setScale(2);
            goodsName = "角色小礼包";
        } else if (productId==2L) {
            price = serverConfigService.getBigDecimal(Config.GIFT_PRICE_2).setScale(2);
            goodsName = "角色大礼包";
        }else {
            price = new BigDecimal("1").setScale(2);
            goodsName = "测试礼包";
        }
        String merchantId = serverConfigService.getString(Config.PAY_MERCHANT_ID);
        String merReqNo = OrderUtil.getOrder5Number();
        String notifyUrl = serverConfigService.getString(Config.PAY_NOTIFY_URL);
        String returnUrl = serverConfigService.getString(Config.PAY_REDIRECT_URL);

        String timeExpire = DateTimeZoneUtil.dateToTimeZone(System.currentTimeMillis() + 1000 * 60 * 3);
        DateTime dateTime = cn.hutool.core.date.DateUtil.parse(timeExpire);
        Date expireDate = new Date(dateTime.getTime());
        tsgPayOrderService.addOrder(userId,merReqNo,productId,price,expireDate);
        Map<String,Object> data = new HashMap<>();
        data.put("version",VERSION);
        data.put("type",TYPE);
        data.put("userId", USER_ID);
        data.put("buyerId", String.valueOf(userId));
        data.put("requestNo",merReqNo);
        data.put("amount", String.valueOf(price.multiply(new BigDecimal(100)).setScale(0)));
        data.put("callBackURL",notifyUrl);
        data.put("redirectUrl",returnUrl);
        data.put("ip",ip);
        TreeMap<String,Object> treeMap = new TreeMap<>(data);
        StringBuffer stringBuffer = new StringBuffer();
        treeMap.forEach((key, value) -> stringBuffer.append(key).append("=").append(value).append("&"));
        String s = stringBuffer+"key="+SECRET;
        System.out.println(s);
        String signMd5 = MD5Util.md5(s).toLowerCase();
        data.put("sign",signMd5);
        String result = HttpUtil.post("https://api-kaite.jjoms.com/api/pay",data);
        if (result==null){
            throwExp("当前没有可用的支付地址，请联系客服或稍后再试");
        }
        JSONObject jsonResult = JSONObject.parseObject(result);
        if (jsonResult.containsKey("message") && jsonResult.getString("message").equals("000000")){
            JSONObject returnResult = new JSONObject();
            returnResult.put("payUrl",jsonResult.getJSONObject("payUrl"));
            return returnResult;
        }else{
            logger.error("请求支付接口错误"+result);
        }
        throwExp("当前没有可用的支付地址，请联系客服或稍后再试");
        return null;
    }

    @Transactional
    @ServiceMethod(code = "000", description = "购买礼包")
    public Object buy(final AppSocket appSocket, Command appCommand, JSONObject params) throws Exception {
        checkNull(params);
        checkNull(params.get("giftType"),params.get("priceType"));
        //todo 验证giftId合理性
        Long userId = appSocket.getWsidBean().getUserId();
        //用户Id 插入到参数中 传到manager服务器   code 035011
        params.put("userId", userId);
        int priceType = params.getIntValue("priceType");

        if (priceType==2){
            return getPayAddress(userId,params, appSocket.getIp());
        }else{
            Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("035011", params).build(), new RequestManagerListener(appCommand));
            return async();
        }

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
        List<UserRole> byUserId = userRoleService.findByUserId(userId);
        if (byUserId.size()==0){
            params.put("status", 0);
        }
        for (UserRole userRole : byUserId) {
            if (userRole.getStatus()!=2){
                params.put("status",1);
                break;
            }
        }
        return params;
    }

    @ServiceMethod(code = "002", description = "激活角色礼包")
    public JSONObject useGift(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("userNo"), params.get("type"));
        String userNo = params.getString("userNo");
        Long myId = appSocket.getWsidBean().getUserId();
        int type = params.getIntValue("type");
        if (type != 1 && type != 2) {
            throwExp("非法请求");
        }
        User user = userCacheService.getUserInfoByUserNo(userNo);
        if (user == null) {
            throwExp("玩家不存在");
        }
        UserGift userGift = userGiftService.findUserGift(user.getId(), type);
        if (userGift == null || userGift.getGiftNum() < 1) {
            throwExp("礼包数量不足");
        }
        userGiftService.useGift(myId, type);
        if (type == 1) {
            useSmallGift(user.getId());
        } else {
            useBigGift(user.getId());
        }
        return params;
    }


    public void useSmallGift(Long userId) {
        userRoleService.addUserRole(userId, 1L, 30);
    }

    public void useBigGift(Long userId) {
        for (int i = 1; i <= 5; i++) {
            userRoleService.addUserRole(userId, (long) i, 30);
        }
    }


    @Transactional
    @ServiceMethod(code = "003", description = "进入场景查看角色工作信息")
    public Object findWorkingRoles(final AppSocket appSocket, Command appCommand, JSONObject params) {
        Long userId = appSocket.getWsidBean().getUserId();
        List<UserRole> roles = userRoleService.findWorkingRoles(userId);
        return settleRoleReceive(roles);
    }

    @Transactional
    @ServiceMethod(code = "004", description = "查看我的角色")
    public Object myRoles(final AppSocket appSocket, Command appCommand, JSONObject params) {
        Long userId = appSocket.getWsidBean().getUserId();
        List<UserRole> roles = userRoleService.findByUserId(userId);
        return settleRoleReceive(roles);
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
            if (hour > 1) {
                long useHp = hour * oneHourCostHp;
                userRole.setHp((int) (userRole.getHp() - useHp));
                JSONArray reward = dicRole.getReward();
                for (Object o : reward) {
                    JSONObject info = (JSONObject) o;
                    Integer oneHourNumber = info.getInteger("number");
                    info.put("number", oneHourNumber * hour);
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


    @ServiceMethod(code = "005", description = "设置角色为工作状态")
    public JSONObject working(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("index"),params.get("userRoleId"));
        Long userId = appSocket.getWsidBean().getUserId();
        int index = params.getIntValue("index");
        Long userRoleId = params.getLong("userRoleId");
        UserRole userRole = userRoleService.findByUserRoleId(userRoleId);
        if (userRole==null){
            throwExp("未查询到角色信息");
        }
        if (!Objects.equals(userRole.getUserId(), userId)){
            throwExp("非法请求");
        }
        if (userRole.getStatus()==2){
            throwExp("角色已到期");
        }
        userRole.setIndex(index);
        userRole.setStatus(1);
        userRoleService.updateUserRole(userRole);
        return null;
    }


    @ServiceMethod(code = "006", description = "查询可选择的角色")
    public Object getNoWorkingRoles(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        //checkNull(params.get("index"));
        Long userId = appSocket.getWsidBean().getUserId();
        return userRoleService.findNoWorkingRoles(userId);
    }

    @ServiceMethod(code = "007", description = "补充体力")
    public Object addHp(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        Long userId = appSocket.getWsidBean().getUserId();
        params.put("userId",userId);
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("400001", params).build(), new RequestManagerListener(appCommand));
        return async();
    }

    @ServiceMethod(code = "008", description = "领取产出道具")
    public Object receiveItem(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("userRoleId"));
        Long userId = appSocket.getWsidBean().getUserId();
        params.put("userId",userId);
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("400002", params).build(), new RequestManagerListener(appCommand));
        return async();
    }

    @ServiceMethod(code = "009", description = "查看购买礼包信息")
    public Object buyGiftInfo(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        JSONObject result = new JSONObject();
        result.put("rmbStatus",serverConfigService.getInteger(Config.GIFT_RMB_STATUS));
        result.put("rmbPrice1",serverConfigService.getBigDecimal(Config.GIFT_PRICE_1));
        result.put("rmbPrice2",serverConfigService.getBigDecimal(Config.GIFT_PRICE_2));
        result.put("gameMoneyStatus",serverConfigService.getInteger(Config.GIFT_GAME_STATUS));
        result.put("gamePrice1",serverConfigService.getBigDecimal(Config.GIFT_PRICE_1_GAME));
        result.put("gamePrice2",serverConfigService.getBigDecimal(Config.GIFT_PRICE_2_GAME));
        return result;
    }

    public static void main(String[] args) {
        String s = "amount=9900&buyerId=928371&callBackURL=http://8.130.102.236:8080/ZY-APP-MANAGER/tsgPayNotify&ip=172.16.3.127&redirectUrl=&requestNo=2025060609413822536&type=aliPayH5&userId=88162050&version=v1.0key=e7a15a9d4e6946bb97edf329035297d1";
        String s2 = "amount=9900&buyerId=928371&callBackURL=http://8.130.102.236:8080/ZY-APP-MANAGER/tsgPayNotify&ip=172.16.3.127&redirectUrl=&requestNo=2025060609413822536&type=aliPayH5&userId=88162050&version=v1.0&key=e7a15a9d4e6946bb97edf329035297d1";
        String s1 = MD5Util.md5(s).toLowerCase();
        String s3 = MD5Util.md5(s2).toLowerCase();
        System.out.println(s1);
        System.out.println(s3);
    }
}

package com.zywl.app.manager.service.manager;

import cn.hutool.core.date.DateTime;
import com.alibaba.fastjson2.JSONObject;
import com.ijpay.core.utils.DateTimeZoneUtil;
import com.zywl.app.base.bean.Config;
import com.zywl.app.base.bean.UserCapital;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.HTTPUtil;
import com.zywl.app.base.util.MD5Util;
import com.zywl.app.base.util.OrderUtil;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.cache.UserCapitalCacheService;
import com.zywl.app.defaultx.enmus.UserCapitalTypeEnum;
import com.zywl.app.defaultx.service.TsgPayOrderService;
import com.zywl.app.defaultx.service.UserCapitalService;
import com.zywl.app.defaultx.service.UserGiftRecordService;
import com.zywl.app.defaultx.service.UserGiftService;
import com.zywl.app.manager.context.MessageCodeContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@Service
@ServiceClass(code = MessageCodeContext.USER_GIFT_SERVER)
public class ManagerBuyGiftService extends BaseService {

    @Autowired
    private ManagerConfigService managerConfigService;
    @Autowired
    private ManagerGameBaseService managerGameBaseService;
    @Autowired
    private UserCapitalService userCapitalService;
    @Autowired
    private UserGiftService userGiftService;

    @Autowired
    private TsgPayOrderService tsgPayOrderService;
    @Autowired
    private UserGiftRecordService userGiftRecordService;

    @Autowired
    private ManagerUserVipService managerUserVipService;

    public static final String VERSION = "v1.0";
    public static final String TYPE = "10005";
    public static final String USER_ID = "88162050";

    public static final String SECRET = "e7a15a9d4e6946bb97edf329035297d1";

    public BigDecimal getGiftPriceById(int priceType) {
        if (priceType == 1) {
            return managerConfigService.getBigDecimal(Config.GIFT_PRICE_1);
        } else {
            return managerConfigService.getBigDecimal(Config.GIFT_PRICE_1_GAME);
        }
    }


    @Transactional
    @ServiceMethod(code = "010", description = "购买礼包")
    public JSONObject getPayAddress(JSONObject params) throws Exception {
        checkNull(params);
        checkNull(params.get("giftType"));
        Long productId = params.getLong("giftType");
        Long userId = params.getLong("userId");
        String ip = params.getString("ip");
        BigDecimal price;
        if (productId == 1L) {
            price = managerConfigService.getBigDecimal(Config.GIFT_PRICE_1).setScale(2);
        } else if (productId == 2L) {
            price = managerConfigService.getBigDecimal(Config.GIFT_PRICE_2).setScale(2);
        } else {
            price = new BigDecimal("1").setScale(2);
        }
        String merchantId = managerConfigService.getString(Config.PAY_MERCHANT_ID);
        String merReqNo = OrderUtil.getOrder5Number();
        String notifyUrl = managerConfigService.getString(Config.PAY_NOTIFY_URL);
        String returnUrl = managerConfigService.getString(Config.PAY_REDIRECT_URL);

        String timeExpire = DateTimeZoneUtil.dateToTimeZone(System.currentTimeMillis() + 1000 * 60 * 3);
        DateTime dateTime = cn.hutool.core.date.DateUtil.parse(timeExpire);
        Date expireDate = new Date(dateTime.getTime());
        tsgPayOrderService.addOrder(userId, merReqNo, productId, price, expireDate);
        Map<String, Object> data = new HashMap<>();
        data.put("version", VERSION);
        data.put("type", TYPE);
        data.put("userId", USER_ID);
        data.put("buyerId", String.valueOf(userId));
        data.put("requestNo", merReqNo);
        data.put("amount", String.valueOf(price.multiply(new BigDecimal(100)).setScale(0)));
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
        Long time = System.currentTimeMillis();
        JSONObject from = JSONObject.from(data);
        String s1 = from.toJSONString();
        String result = HTTPUtil.postJSON("https://api-kaite.jjoms.com/api/pay",s1);
        System.out.println("请求支付地址耗时:"+(System.currentTimeMillis()-time));
        System.out.println(result);
        if (result == null) {
            throwExp("当前没有可用的支付地址，请联系客服或稍后再试");
        }
        JSONObject jsonResult = JSONObject.parseObject(result);
        if (jsonResult.containsKey("message") && jsonResult.getString("message").equals("000000")) {
            JSONObject returnResult = new JSONObject();
            returnResult.put("payUrl", jsonResult.getString("payUrl"));
            return returnResult;
        } else {
            logger.error("请求支付接口错误" + result);
        }
        throwExp("当前没有可用的支付地址，请联系客服或稍后再试");
        return null;
    }

    @Transactional
    @ServiceMethod(code = "011", description = "购买礼包")
    public JSONObject buy(JSONObject data) throws Exception {
        checkNull(data);
        checkNull(data.get("userId"), data.get("priceType"));
        //根据礼包ID获取礼包价格
        int priceType = data.getIntValue("priceType");
        BigDecimal price = getGiftPriceById( priceType);
        if (priceType == 1) {
            throwExp("先用type2 支付还没接");
        }
        //购买礼包的用户ID
        Long userId = data.getLong("userId");
        //礼包加数量之前先判断用户余额是否足够
        managerGameBaseService.checkBalance(userId, price, UserCapitalTypeEnum.currency_2);
        //余额充足 1.插入订单 2.扣钱  3.加礼包数量
        //1.插入订单
        String orderNo = OrderUtil.getOrder5Number();
        Long recordId = userGiftRecordService.addGiftRecord(userId, orderNo, UserCapitalTypeEnum.currency_2.getValue(), 1, price);
        //2.扣钱
        userCapitalService.subBalanceByGift(price, userId, orderNo, recordId);
        managerUserVipService.addExper(userId, price);
        //3.礼包数+1
        userGiftService.addUserGiftNumber(userId, priceType);
        //推送用户余额变化
        managerGameBaseService.pushCapitalUpdate(userId, UserCapitalTypeEnum.currency_2.getValue());

        return new JSONObject();
    }


}

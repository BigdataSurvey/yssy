package com.zywl.app.manager.service.pay;


import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson2.JSONObject;
import com.ijpay.core.kit.PayKit;
import com.ijpay.core.utils.DateTimeZoneUtil;
import com.wechat.pay.java.service.payments.app.AppService;
import com.wechat.pay.java.service.payments.app.model.Amount;
import com.wechat.pay.java.service.payments.app.model.PrepayRequest;
import com.wechat.pay.java.service.payments.app.model.PrepayResponse;
import com.zywl.app.base.bean.RechargeOrder;
import com.zywl.app.base.bean.User;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.service.RechargeOrderService;
import com.zywl.app.manager.bean.WxPayV3Bean;
import com.zywl.app.manager.context.MessageCodeContext;
import com.zywl.app.manager.service.PlayGameService;
import com.zywl.app.manager.socket.ManagerSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shadow.com.wechat.pay.java.core.Config;
import shadow.com.wechat.pay.java.core.RSAAutoCertificateConfig;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;



@Service
@ServiceClass(code = MessageCodeContext.NEW_WX_PAY_SERVER)
public class WxPayService extends BaseService {

    @Autowired
    private RechargeOrderService rechargeOrderService;

    @Autowired
    private UserCacheService userCacheService;

    private String privateKey="315077466fa7acb5794c02a9dc662700";


    //易云仙舍
    public static String appid = "wx938564a42cf4f4cb";

    public static String merchantId = "1665263042";
    /** 商户API私钥路径 */
    /**
     * 商户证书序列号
     */
    public static String merchantSerialNumber = "698EBED900EF454A6A4D76A52BC6AB98928E4D67";
    /**
     * 商户APIV3密钥
     */
    public static String apiV3Key = "HENANzongyiwangluokejigs20220314";

    public WxPayV3Bean wxPayV3Bean;

    @Autowired
    private PlayGameService gameService;

    private final static int OK = 200;

    private static final String URL="http://123.56.119.157:8001/";


    @ServiceMethod(code = "001", description = "发起微信支付")
    public Object appPay(ManagerSocketServer adminSocketServer, JSONObject data) throws Exception {
        Long userId  = data.getLong("userId");
        User user = userCacheService.getUserInfoById(data.getLong("userId"));
        Long productId = data.getLong("productId");
        if (user.getFirstCharge()==1 && productId.toString().equals("1")){
            throwExp("您已购买过此产品，不能再次购买");
        }
        int price = 1000000;
        if (PlayGameService.productMap.containsKey(productId.toString())){
            price = PlayGameService.productMap.get(productId.toString()).getPrice();
        }
        Config config =
                new RSAAutoCertificateConfig.Builder()
                        .merchantId(merchantId)
                        .privateKeyFromPath(this.getClass().getClassLoader().getResource("apiclient_key2.pem").getPath())
                        .merchantSerialNumber(merchantSerialNumber)
                        .apiV3Key(apiV3Key)
                        .build();
        // 构建service
        AppService service = new AppService.Builder().config(config).build();
        // request.setXxx(val)设置所需参数，具体参数可见Request定义
        String timeExpire = DateTimeZoneUtil.dateToTimeZone(System.currentTimeMillis() + 1000 * 60 * 3);
        DateTime dateTime = DateUtil.parse(timeExpire);
        Date expireDate = new Date(dateTime.getTime());
        PrepayRequest request = new PrepayRequest();
        Amount amount = new Amount();
        amount.setTotal(100);
        request.setAmount(amount);
        request.setAppid(appid);
        request.setMchid(merchantId);
        request.setDescription("颤抖吧三国充值");
        request.setTimeExpire(timeExpire);
        request.setNotifyUrl("http://101.200.32.14:8001/ZY-APP-MANAGER/payNotify");
        request.setOutTradeNo(PayKit.generateStr());
        // 调用下单方法，得到应答
        PrepayResponse response = service.prepay(request);
        // 使用微信扫描 code_url 对应的二维码，即可体验Native支付
        String prepayId=response.getPrepayId();
        rechargeOrderService.addOrder(userId,request.getOutTradeNo(),prepayId,productId,price,expireDate);
        Map<String,String> map = new HashMap<>();
        map.put("prepayid",prepayId);
        return map;
    }


    public static void main(String[] args) {
        WxPayService wxPayService = new WxPayService();
        Config config =
                new RSAAutoCertificateConfig.Builder()
                        .merchantId(merchantId)
                        .privateKeyFromPath(wxPayService.getClass().getClassLoader().getResource("apiclient_key2.pem").getPath())
                        .merchantSerialNumber(merchantSerialNumber)
                        .apiV3Key(apiV3Key)
                        .build();
        // 构建service
        AppService service = new AppService.Builder().config(config).build();
        // request.setXxx(val)设置所需参数，具体参数可见Request定义
        PrepayRequest request = new PrepayRequest();
        Amount amount = new Amount();
        amount.setTotal(100);
        request.setAmount(amount);
        request.setAppid(appid);
        request.setMchid(merchantId);
        request.setDescription("逆天纪充值");
        request.setNotifyUrl("http://101.200.32.14:8001/ZY-APP-MANAGER/payNotify");
        request.setOutTradeNo("out_trade_no_001");
        // 调用下单方法，得到应答
        PrepayResponse response = service.prepay(request);
        // 使用微信扫描 code_url 对应的二维码，即可体验Native支付
        System.out.println(response.getPrepayId());
    }


}

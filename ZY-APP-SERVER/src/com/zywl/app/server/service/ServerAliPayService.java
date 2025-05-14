package com.zywl.app.server.service;

import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.bean.Command;
import com.live.app.ws.enums.TargetSocketType;
import com.live.app.ws.util.CommandBuilder;
import com.live.app.ws.util.Executer;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.OrderUtil;
import com.zywl.app.base.util.RSA2Util;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.server.context.MessageCodeContext;
import com.zywl.app.server.socket.AppSocket;
import com.zywl.app.server.util.RequestManagerListener;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@ServiceClass(code = MessageCodeContext.ALIPAY_SERVER)
public class ServerAliPayService extends BaseService {

    private static final String API_NAME = "com.alipay.account.auth";

    private static final String METHOD = "alipay.open.auth.sdk.code.get";

    private static final String APP_ID = "2021004135615110";

    private static final String APP_NAME = "mc";

    private static final String BIZ_TYPE=  "openservice";

    private static final String PID = "2088741772515344";

    private static final String PRODUCT_ID= "APP_FAST_LOGIN";

    private static final String SCOPE = "kuaijie";

    private static final String AUTH_TYPE="AUTHACCOUNT";

    private static final String SIGN_TYPE = "RSA2";

    private static final String PRIVATE_KEY   = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCPlaG50AVwC/yXOHK7ssP2KMV3TEoVFiH7xkKRyKUkPBzcL7LfbnrospRaigAVUOIoBqhC0uMKzS60Ji3EYxtsVu8Z9Qakslr7E6TqVQ9EOdPfSJCxokpASrdM/1yl9SqECkP+I9GvuVD5mU3cjdVMwUMvomHoHL3DgByHieWEqibtNijSXbeO6XEJzXd7k67y5ecjqXzgpYWXXsZOOjsRih2eEyytWSyqlbi4/f250PckMaHtvIjW4ASebGKncipnk3hN3u/R6eWq0x4CgvjsMMWbjLMw3av9aLV2Bt7Xkpd5n1xi4yEwcwN9T7ihI3Y58lsMwkW/E+dCoQWYv8oLAgMBAAECggEAKvRiu4nl9o0/daXnfQuP4FZ2LKhgCUrjw8SeKarS7LInGCAU7Q7KKk8yXpumRro5ziufrs4UKikT7cT2MChODe07/pH0+NR6r15DGe90b761CblVwC6C9BTmHVzPxL5Bh9riWGcy1dUkymb4iiDMTPgMN3XmwF/IzXHIFyxDw5oFe4ototWRdJq2PtDMiRBo6lyr/4PwBVTV+KTTAVejw6Ft02zYr9/t9be+d78Xh3LawI1SFE4w9U9uBdSVQv7dmF3tIiCGw0U7Cnu6fNVM1HhoIDDixWpFjmPXQuzCMO0q50lvis9DuyEYLMojPI0i0n5ACDTBAEoeGkK4qU994QKBgQD07VpPZoq/D46rpIxNzH2JwqJmy5DOEke8lUksSVa0KKm8ybnb8H1DCRo9klTHtKWZWX4cIX8MoLz+tMS1hO554G23uImcCSmwFgmsXlzSj0/jHZFVyf5NOML7l8eIYi/7iO8njXL2UupBTPHLluo/a4UVAU5PsyhYLAc17h4+KQKBgQCWE2WWc1RP0mwm9x3Clfppd76MRqcd/CJA2qrS/6hPoojgNGOaXv7hzRI8xS502DH/L9dyqSo468itAK4WEXXDScI144ZBEBHJazqPHfnJKc79EZ6sDf3OlORTssruWCU3XhPLzFb8bcDp6RiyOJ+/Y8mbguqVVMPOwBlXl4NlEwKBgQC4+H/ZsyFZhZBDxHNJVgQBBALOCzKCzn9qxnuKfKCEUqlNsDMzDP4soDU3BsoMQDtIArQg3pMqoEHbQf3E8G2BkaKKu00BkFHxb9NCX8lOI3k7llrqJTBudU2b4FaKg0yldBbZEhQePyQ2yLta+9BQsQzCfkf8HNt9K1MOwZQJcQKBgBWHGMZ5Krn8jEkWn60/CFnCtJG4vNY/ScaV13VG+STbQtkuiq8lO1i2qwwOmPhn3twlR7mJ7KWXpQS0GUTPIl5uIS7LwYFpxbNn71GCUkd5+Ngyg9lYdHUCxLIA7r075bLIivxsBnpVYBvttP4zwy6YKN5m7DGZpDDvO3NmJ5IDAoGAGzRupqjYUvo25seXwl2Fy9B3mV38pccSwr6VJPjNNEGLxqnFJIx7j4KVyvOCh2915elI/T4QsqplaXhmek7Y75HxPz2qvTVPIfizvUsu2RmL5kbjlUU6WPHy7KPsSZNIzpzagjMxxBFfydIuQMNS/UaqJlF3tNclA3RQofomrjU=";


    private static final Map<String,String>  REQUEST_ORDER=new ConcurrentHashMap<>();

    @ServiceMethod(code = "001", description = "获取支付宝唤起地址")
    public Object getAddress(AppSocket appSocket, Command command, JSONObject params) throws UnsupportedEncodingException {
        StringBuffer buffer = new StringBuffer();
        buffer.append("apiname=com.alipay.account.auth&method=alipay.open.auth.sdk.code.get&app_id=2021005111696226" +
                "&app_name=mc&biz_type=openservice&pid=2088941989029863&product_id=APP_FAST_LOGIN&scope=kuaijie&" +
                "target_id=");
        buffer.append(OrderUtil.getOrder5Number());
        buffer.append("&auth_type=AUTHACCOUNT&sign_type=RSA2&sign=");
        String sign = RSA2Util.sign256(buffer.toString(), PRIVATE_KEY);
        URLEncoder.encode(sign,"UTF-8");
        buffer.append(sign);
        return buffer;
    }
    @ServiceMethod(code = "002", description = "获取支付宝用户信息")
    public Object getAliPayUserInfo(AppSocket appSocket, Command command, JSONObject params) {
        checkNull(params);
        Long userId = appSocket.getWsidBean().getUserId();
        String authCode = params.getString("result");
        if (authCode==null){
            throwExp("未获取到支付宝用户信息");
        }
        String[] split = authCode.split("&");
        String authcode = null;
        for (String s : split) {
            if (s.contains("auth_code")){
                String[] split1 = s.split("=");
                authcode = split1[1];
                break;
            }
        }
        params.put("authCode",authcode);
        params.put("userId",userId);
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("100030", params).build(),
                new RequestManagerListener(command));
        return async();
    }

}

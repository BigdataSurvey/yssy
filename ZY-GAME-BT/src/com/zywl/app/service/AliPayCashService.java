package com.zywl.app.service;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayConfig;
import com.alipay.api.DefaultAlipayClient;
import com.zywl.app.base.service.BaseService;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.net.URL;

@Service
public class AliPayCashService extends BaseService {
    //this.getClass().getClassLoader().getResource("appCertPublicKey.crt").getPath()



    public static final String privateKey = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCPlaG50AVwC/yXOHK7ssP2KMV3TEoVFiH7xkKRyKUkPBzcL7LfbnrospRaigAVUOIoBqhC0uMKzS60Ji3EYxtsVu8Z9Qakslr7E6TqVQ9EOdPfSJCxokpASrdM/1yl9SqECkP+I9GvuVD5mU3cjdVMwUMvomHoHL3DgByHieWEqibtNijSXbeO6XEJzXd7k67y5ecjqXzgpYWXXsZOOjsRih2eEyytWSyqlbi4/f250PckMaHtvIjW4ASebGKncipnk3hN3u/R6eWq0x4CgvjsMMWbjLMw3av9aLV2Bt7Xkpd5n1xi4yEwcwN9T7ihI3Y58lsMwkW/E+dCoQWYv8oLAgMBAAECggEAKvRiu4nl9o0/daXnfQuP4FZ2LKhgCUrjw8SeKarS7LInGCAU7Q7KKk8yXpumRro5ziufrs4UKikT7cT2MChODe07/pH0+NR6r15DGe90b761CblVwC6C9BTmHVzPxL5Bh9riWGcy1dUkymb4iiDMTPgMN3XmwF/IzXHIFyxDw5oFe4ototWRdJq2PtDMiRBo6lyr/4PwBVTV+KTTAVejw6Ft02zYr9/t9be+d78Xh3LawI1SFE4w9U9uBdSVQv7dmF3tIiCGw0U7Cnu6fNVM1HhoIDDixWpFjmPXQuzCMO0q50lvis9DuyEYLMojPI0i0n5ACDTBAEoeGkK4qU994QKBgQD07VpPZoq/D46rpIxNzH2JwqJmy5DOEke8lUksSVa0KKm8ybnb8H1DCRo9klTHtKWZWX4cIX8MoLz+tMS1hO554G23uImcCSmwFgmsXlzSj0/jHZFVyf5NOML7l8eIYi/7iO8njXL2UupBTPHLluo/a4UVAU5PsyhYLAc17h4+KQKBgQCWE2WWc1RP0mwm9x3Clfppd76MRqcd/CJA2qrS/6hPoojgNGOaXv7hzRI8xS502DH/L9dyqSo468itAK4WEXXDScI144ZBEBHJazqPHfnJKc79EZ6sDf3OlORTssruWCU3XhPLzFb8bcDp6RiyOJ+/Y8mbguqVVMPOwBlXl4NlEwKBgQC4+H/ZsyFZhZBDxHNJVgQBBALOCzKCzn9qxnuKfKCEUqlNsDMzDP4soDU3BsoMQDtIArQg3pMqoEHbQf3E8G2BkaKKu00BkFHxb9NCX8lOI3k7llrqJTBudU2b4FaKg0yldBbZEhQePyQ2yLta+9BQsQzCfkf8HNt9K1MOwZQJcQKBgBWHGMZ5Krn8jEkWn60/CFnCtJG4vNY/ScaV13VG+STbQtkuiq8lO1i2qwwOmPhn3twlR7mJ7KWXpQS0GUTPIl5uIS7LwYFpxbNn71GCUkd5+Ngyg9lYdHUCxLIA7r075bLIivxsBnpVYBvttP4zwy6YKN5m7DGZpDDvO3NmJ5IDAoGAGzRupqjYUvo25seXwl2Fy9B3mV38pccSwr6VJPjNNEGLxqnFJIx7j4KVyvOCh2915elI/T4QsqplaXhmek7Y75HxPz2qvTVPIfizvUsu2RmL5kbjlUU6WPHy7KPsSZNIzpzagjMxxBFfydIuQMNS/UaqJlF3tNclA3RQofomrjU=";
    public static AlipayClient alipayClient;


    @PostConstruct
    private void getAlipayConfig() throws AlipayApiException {
        AlipayConfig alipayConfig = new AlipayConfig();
        alipayConfig.setPrivateKey(privateKey);
        alipayConfig.setServerUrl("https://openapi.alipay.com/gateway.do");
        alipayConfig.setAppId("2021005111696226");
        //alipayConfig.setCharset("UTF-8");
        //alipayConfig.setSignType("RSA2");
        //alipayConfig.setFormat("json");
        ClassLoader classLoader = this.getClass().getClassLoader();

        URL resource = classLoader.getResource("appCertPublicKey_2021005111696226.crt");
        String path = resource.getPath();
        alipayConfig.setAppCertPath(path);
        alipayConfig.setAlipayPublicCertPath(this.getClass().getClassLoader().getResource("alipayCertPublicKey_RSA2.crt").getPath());
        alipayConfig.setRootCertPath(this.getClass().getClassLoader().getResource("alipayRootCert.crt").getPath());
        alipayClient = new DefaultAlipayClient(alipayConfig);


    }
}

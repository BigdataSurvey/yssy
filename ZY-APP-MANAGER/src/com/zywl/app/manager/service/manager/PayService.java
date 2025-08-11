package com.zywl.app.manager.service.manager;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.ObjectMapper;
import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.PayBean;
import com.zywl.app.base.bean.UserPit;
import com.zywl.app.base.util.*;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.enmus.UserCapitalTypeEnum;
import com.zywl.app.manager.bean.AliPayBean;
import com.zywl.app.manager.context.MessageCodeContext;
import com.zywl.app.manager.service.PlayGameService;
import com.zywl.app.manager.socket.ManagerSocketServer;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@ServiceClass(code = MessageCodeContext.USER_VIP)
public class PayService {

    public static final String WXPAY = "WXPAY";
    public static final String DIV_JSON = "divJson";
    @Autowired
    private static HttpsUtil httpsUtil;


    /**
     * 支付
     * @param merOrderNo
     * @return
     */
    private static String makeUrl(String merOrderNo) {
        try {
            Map<String, String> bizContent = new HashMap<>();//组装业务参数
            bizContent.put("merchantNo", Constant.merchantNo);
            bizContent.put("merOrderNo", merOrderNo);
            bizContent.put("orderDesc", "充值");
            bizContent.put("orderAmount", "100");//单位分
            bizContent.put("terminalNo", Constant.terminalNo);//
            bizContent.put("notifyUrl", "http://h15.helipay.com/test/notify");//
            bizContent.put("payType", "Unified");//
          /*  bizContent.put("DIV_JSON", WXPAY);//
            bizContent.put("RequestNo", "2403181522270024f03d");//
            bizContent.put("returnUrl", " http://h5.helipay.com/notify.php");//
            bizContent.put("terminalIp", "192.168.0.1");//*/
           // bizContent.put("sign", "630a81e60ef0061ce254d447b2954abd");//
            String xmlText = Http.runJson(Constant.DOMAIN_NAME + "hxdpay/make/unifiedUrl", bizContent, Constant.key);
            return xmlText;
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    private static String makeUrl1(String merOrderNo) {
        try {
            Map<String, String> bizContent = new HashMap<>();//组装业务参数
            bizContent.put("merchantNo", Constant.merchantNo);
            bizContent.put("orderNo", merOrderNo);
          /*  bizContent.put("DIV_JSON", WXPAY);//
            bizContent.put("RequestNo", "2403181522270024f03d");//
            bizContent.put("returnUrl", " http://h5.helipay.com/notify.php");//
            bizContent.put("terminalIp", "192.168.0.1");//*/
            // bizContent.put("sign", "630a81e60ef0061ce254d447b2954abd");//
            String xmlText = Http.runJson(Constant.DOMAIN_NAME + "/hxdpay/query", bizContent, Constant.key);
            return xmlText;
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    public static void main(String[] args) {
        makeUrl1("471Un250729174423680035338");
    }

    public  String order(String name, String idCard) {
        PayBean payBean = new PayBean();
        Map<String, Object> payBeanMap = BeanUtil.beanToMap(payBean);
        String s = httpsUtil.sendJsonPost("/api/helipay/settle/order", payBeanMap);
        return s;
    }

    @Transactional
    public Object httpRequest(ManagerSocketServer adminSocketServer, JSONObject params) {

        // 创建要发送的对象
        PayBean user = new PayBean();

        // 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 创建请求实体
        HttpEntity<PayBean> request = new HttpEntity<>(user, headers);

        // 创建RestTemplate
        RestTemplate restTemplate = new RestTemplate();

        // 发送POST请求
        PayBean response = restTemplate.postForObject("https://api/merchantInfo/editHlbInfo", request, PayBean.class);
        return response;
    }

    @Transactional
    public Object httpRequest1(ManagerSocketServer adminSocketServer, JSONObject params) {

        // 创建要发送的对象
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type","1");
        jsonObject.put("merchantId","1");
        // 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 创建请求实体
        HttpEntity<Object> request = new HttpEntity<>(jsonObject, headers);

        // 创建RestTemplate
        RestTemplate restTemplate = new RestTemplate();

        // 发送POST请求
        Object response = restTemplate.postForObject("https://api/merchantInfo/queryEntryResult", request, Object.class);
        return response;
    }

    @Transactional
    public Object httpRequest2(ManagerSocketServer adminSocketServer, JSONObject params) {

        // 创建要发送的对象
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("bankName","1");
        // 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 创建请求实体
        HttpEntity<Object> request = new HttpEntity<>(jsonObject, headers);

        // 创建RestTemplate
        RestTemplate restTemplate = new RestTemplate();

        // 发送POST请求
        List<Object> response = restTemplate.postForObject("https:///api/merchantInfo/bankBranchName", request, List.class);
        return response;
    }
    @Transactional
    public Object httpRequest3(ManagerSocketServer adminSocketServer, JSONObject params) {

        // 创建要发送的对象
        JSONObject jsonObject = new JSONObject();
        // 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 创建请求实体
        HttpEntity<Object> request = new HttpEntity<>(jsonObject, headers);

        // 创建RestTemplate
        RestTemplate restTemplate = new RestTemplate();

        // 发送POST请求
        List<Object> response = restTemplate.postForObject("https://api/merchantInfo/areas", request, List.class);
        return response;
    }

    @Transactional
    public Object httpRequest4(ManagerSocketServer adminSocketServer, JSONObject params) {

        // 创建要发送的对象
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("merchantType","1");
        // 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 创建请求实体
        HttpEntity<Object> request = new HttpEntity<>(jsonObject, headers);

        // 创建RestTemplate
        RestTemplate restTemplate = new RestTemplate();

        // 发送POST请求
        List<Object> response = restTemplate.postForObject("https://api/merchantInfo/merchantCategory", request, List.class);
        return response;
    }

    @Transactional
    public Object httpRequest5(ManagerSocketServer adminSocketServer, JSONObject params) {

        // 创建要发送的对象
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type","1");
        jsonObject.put("imgFile","1");
        jsonObject.put("managerId","1");
        // 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 创建请求实体
        HttpEntity<Object> request = new HttpEntity<>(jsonObject, headers);

        // 创建RestTemplate
        RestTemplate restTemplate = new RestTemplate();

        // 发送POST请求
        List<Object> response = restTemplate.postForObject("https://api/merchantInfo/image", request, List.class);
        return response;
    }













}

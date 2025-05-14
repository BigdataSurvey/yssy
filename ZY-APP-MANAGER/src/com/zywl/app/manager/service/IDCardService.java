package com.zywl.app.manager.service;

import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.util.AES128Util;
import com.zywl.app.base.util.HTTPUtil;
import com.zywl.app.base.util.IDCardUtil;
import com.zywl.app.base.util.SHA256Util;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.manager.bean.IDCardReportDataBean;
import com.zywl.app.manager.util.RateLimit;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class IDCardService {
    private final RateLimit rateLimit = new RateLimit(90);
    private final String checkUrl = "https://api.wlc.nppa.gov.cn/idcard/authentication/check";
    private final String querykUrl = "http://api2.wlc.nppa.gov.cn/idcard/authentication/query";
    private final String reportUrl = "http://api2.wlc.nppa.gov.cn/behavior/collection/loginout";


    private final String appId = "c1e81f7b40be43cdb281e8af321ef589";
    private final String bizId = "1199019609";
    private final String secretKey = "65997b3ebe1b75346eab4048b1913dfd";

    private static final Log logger = LogFactory.getLog(IDCardService.class);

    @Autowired
    UserCacheService userCacheService;

    @PostConstruct
    public void Init() {
    }

    public int checkIDCard(String userId, String name, String idNum) {
        if(!IDCardUtil.check(idNum)) {
            return -101;
        }
        int age = IDCardUtil.getAgeForIdcard(idNum);
        if(age < 18) {
            return -102;
        }
        if(!rateLimit.check()) {
            return -1;
        }
        TreeMap<String,String> header = getCommonHeader();

        JSONObject data = new JSONObject();
        data.put("ai",makeAiStr(userId));
        data.put("name",name);
        data.put("idNum",idNum);

        String strData = AES128Util.encryptByAES128Gcm(data.toJSONString(), secretKey);
        Map<String,String> postData = new TreeMap<>();
        postData.put("data",strData);
        String strSign = getSign(header, JSONObject.toJSONString(postData));
        header.put("sign",strSign);

        String result = HTTPUtil.postJSON(checkUrl,JSONObject.toJSONString(postData),header);

        if(result == null || result.isEmpty()) {
            logger.info("check idcard return null: id = "+userId);
            return -2;
        }

        try {
            JSONObject rst = JSONObject.parseObject(result);
            int errorcode = rst.getIntValue("errcode", -1);
            if (errorcode == 0) {
                JSONObject jdata = (JSONObject) rst.get("data");
                JSONObject jrst = (JSONObject) jdata.get("result");
                int status = jrst.getIntValue("status",-1);
                if(status == 0) {
                    return 0;
                }else if(status == 1){
                    return -2;
                }else {
                    return -3;
                }
            } else {
                logger.info("check idcard fail: " + errorcode);
                return -3;
            }
        }catch (Exception e) {
            logger.info("check idcard error: " + e.toString());
            return -4;
        }
    }

    public int queryIDCard(String userId) {
        if(!rateLimit.check()) {
            return -1;
        }

        TreeMap<String,String> header = getCommonHeader();
        JSONObject data = new JSONObject();
        data.put("ai",makeAiStr(userId));
        String strData = AES128Util.encryptByAES128Gcm(data.toJSONString(), secretKey);

        TreeMap<String,String> joinparam = new TreeMap<>();
        joinparam.put("ai",userId);
        joinparam.put("appId",appId);
        joinparam.put("bizId",bizId);
        joinparam.put("timestamps", header.get("timestamps"));
        String strSign = getSign(joinparam, null);
        header.put("sign",strSign);

        String url = querykUrl + "?ai=" + userId;
        String result = HTTPUtil.get(url,header);
        if(result == null || result.isEmpty()) {
            logger.info("query idcard return null: id = "+userId);
            return -2;
        }

        try {
            JSONObject rst = JSONObject.parseObject(result);
            int errorcode = rst.getIntValue("errcode", -1);
            if (errorcode == 0) {
                JSONObject jdata = (JSONObject) rst.get("data");
                JSONObject jrst = (JSONObject) jdata.get("result");
                int status = jrst.getIntValue("status",-1);
                if(status == 0) {
                    return 0;
                }else if(status == 1){
                    return -2;
                }else {
                    return -3;
                }
            } else {
                logger.info("query idcard fail: " + errorcode);
                return -3;
            }
        }catch (Exception e) {
            logger.info("query idcard error: " + e.toString());
            return -4;
        }
    }

    public int reportInfo(List<IDCardReportDataBean> reportdata) {
        TreeMap<String,String> header = getCommonHeader();

        JSONObject data = new JSONObject();
        data.put("collections",reportdata);
        String strData = AES128Util.encryptByAES128Gcm(data.toJSONString(), secretKey);
        Map<String,String> postData = new TreeMap<>();
        postData.put("data",strData);
        String strSign = getSign(header, JSONObject.toJSONString(postData));
        header.put("sign",strSign);

        String result = HTTPUtil.postJSON(reportUrl,JSONObject.toJSONString(postData),header);
        try {
            JSONObject rst = JSONObject.parseObject(result);
            int errorcode = rst.getIntValue("errcode", -1);
            if (errorcode == 0) {
                return 0;
            } else {
                logger.info("idcard report fail: " + errorcode);
                return -3;
            }
        }catch (Exception e) {
            logger.info("idcard report error: " + e.toString());
            return -4;
        }
    }

    private TreeMap<String,String> getCommonHeader() {
        TreeMap<String,String> header = new TreeMap<>();
        header.put("appId",appId);
        header.put("bizId",bizId);
        long curtime = System.currentTimeMillis();
        header.put("timestamps", String.valueOf(curtime));
        return header;
    }

    private String makeAiStr(String userId) {
        String str = "";
        if(userId.length() < 32) {
            for (int i = 0; i < 32 - userId.length(); i++) {
                str += "0";
            }
        }

        return str + userId;
    }

    private String getSign(TreeMap<String,String > params, String data) {
        StringBuilder str = new StringBuilder();
        str.append(secretKey);
        for (Map.Entry<String,String> entry : params.entrySet()) {
            str.append(entry.getKey());
            str.append(entry.getValue());
        }
        if(data != null) {
            str.append(data);
        }
        return SHA256Util.getSHA256(str.toString());
    }

    public static void main(String[] args) throws UnknownHostException {

        IDCardService idCardService = new IDCardService();
        TreeMap<String,String> header = idCardService.getCommonHeader();
        InetAddress localHost = InetAddress.getLocalHost();
        String hostAddress = localHost.getHostAddress();
        System.out.println(hostAddress);
        JSONObject data = new JSONObject();
        data.put("ai",idCardService.makeAiStr("123"));
        data.put("name","牛帅");
        data.put("idNum","410526199608250015");

        String strData = AES128Util.encryptByAES128Gcm(data.toJSONString(), idCardService.secretKey);
        Map<String,String> postData = new TreeMap<>();
        postData.put("data",strData);
        String strSign = idCardService.getSign(header, JSONObject.toJSONString(postData));
        header.put("sign",strSign);

        String result = HTTPUtil.postJSON(idCardService.checkUrl,JSONObject.toJSONString(postData),header);
        System.out.println(result);
        String ipFetchUrl = "http://httpbin.org/ip";
        String publicIp = null;

        try {
            URL url = new URL(ipFetchUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // 解析JSON以获取IP地址
                // 假设返回的JSON格式为 {"origin": "x.x.x.x"}
                String jsonResponse = response.toString();
                int startIndex = jsonResponse.indexOf("\"origin\": \"");
                if (startIndex >= 0) {
                    int endIndex = jsonResponse.indexOf("\"", startIndex + "\"origin\": \"".length());
                    if (endIndex >= 0) {
                        publicIp = jsonResponse.substring(startIndex + "\"origin\": \"".length(), endIndex);
                    }
                }
            } else {
                System.out.println("GET request not worked");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (publicIp != null) {
            System.out.println("公网IP地址: " + publicIp);
        }
    }
}

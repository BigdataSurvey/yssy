package com.zywl.app.base.util;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DuoYouUtil {


    public static void checkSign(Map<String,String> demoMap) {
        try {

            // 1、回调是无参的，通过request来获取参数，如果通过自动装载，浮点型会存在精度损失问题,造成签名失败。如果你想用自动装载，media_income和member_income用String来接收

            // 第一步：回调的是这些参数转化成map

            // 第二步：生成签名，第二个参数只是测试用的appSeret，务必换成你的appSecret
            Map<String, String> signMap = new HashMap<String, String>();
            signMap.put("order_id", demoMap.get("order_id"));
            signMap.put("advert_id", demoMap.get("advert_id"));
            signMap.put("advert_name", demoMap.get("advert_name"));
            signMap.put("created", demoMap.get("created"));
            signMap.put("media_income", demoMap.get("media_income"));
            signMap.put("member_income", demoMap.get("member_income"));
            signMap.put("media_id", demoMap.get("media_id"));
            signMap.put("user_id", demoMap.get("user_id"));
            signMap.put("device_id", demoMap.get("device_id"));
            signMap.put("content", demoMap.get("content"));
            String localSign = generateSignature(signMap, "b36b87146f597842a47d52b0e30abce1");
            System.out.println("localSign = " + localSign);
            // 第三步：校验签名
            String sign = demoMap.get("sign");
            if (sign.equalsIgnoreCase(localSign)) {
                System.out.println("验签成功");
            } else {
                System.out.println("验签失败");
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Map<String, String> getParameter(String params) {
        Map<String, String> map = new HashMap<String, String>();
        try {
            final String charset = "utf-8";
            String[] keyValues = params.split("&");
            for (int i = 0; i < keyValues.length; i++) {
                String key = keyValues[i].substring(0, keyValues[i].indexOf("="));
                String value = keyValues[i].substring(keyValues[i].indexOf("=") + 1);
                map.put(key, value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    /**
     *
     * data : 参数map key：appSecret
     *
     */
    public static String generateSignature(final Map<String, String> data, String key) throws Exception {
        Set<String> keySet = data.keySet();
        String[] keyArray = keySet.toArray(new String[keySet.size()]);
        Arrays.sort(keyArray);
        System.out.println(keyArray);
        StringBuilder sb = new StringBuilder();
        for (String k : keyArray) {
            if (k.equals("sign")) {
                continue;
            }
            // 这里需要对value进行encode，如果value已经encode了忽略此步骤
            String value = data.get(k);
            String encodeValue = URLEncoder.encode(value,"utf-8");
            sb.append(k).append("=").append(encodeValue).append("&");
        }
        sb.append("key=").append(key);
        System.out.println("排序后对的"+ sb.toString());
        return MD5(sb.toString()).toLowerCase();
    }


    public static String generateSignature2(final Map<String, String> data, String key) throws Exception {
        Set<String> keySet = data.keySet();
        String[] keyArray = keySet.toArray(new String[keySet.size()]);
        Arrays.sort(keyArray);
        System.out.println(keyArray);
        StringBuilder sb = new StringBuilder();
        for (String k : keyArray) {
            if (k.equals("sign")) {
                continue;
            }
            // 这里需要对value进行encode，如果value已经encode了忽略此步骤
            String value = data.get(k);
            sb.append(k).append("=").append(value).append("&");
        }
        sb.append("key=").append(key);
        System.out.println("排序后对的"+ sb.toString());
        return MD5(sb.toString()).toLowerCase();
    }

    // 0.45   现在是4.2
    public static void main(String[] args) throws Exception {
        Map<String,String> map = new HashMap<>();
        map.put("order_id","1507513532");
        map.put("advert_name","超级怪手2");
        map.put("advert_id","1500027771");
        map.put("created","1736260749");
        map.put("media_income","4.3");
        map.put("member_income","4.2");
        map.put("media_id","dy_59645202");
        map.put("user_id","806764");
        map.put("device_id","fffeefde-cdff-5c58-6737-f6ddf83f9ba6");
        map.put("content","[试玩]超级怪手2-第1期流浪飞船通过第3关获得4.2");
        System.out.println( generateSignature(map,"b9644b6f8b38f4e3b588675053787a93"));

    }

    public static String MD5(String data) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] array = md.digest(data.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte item : array) {
                sb.append(Integer.toHexString((item & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString().toLowerCase();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }


}

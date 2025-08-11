package com.zywl.app.base.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.util.*;

public class Http {


    public static String runJson( String url, Map<String, String> map,  String md5Key) throws Exception {
        Map<String, String> reqs = new LinkedHashMap<>();
        reqs.putAll(map);
        String sign = MD5.sign(convertUrlParams(map)+md5Key,"utf-8");
        /**取Sign值，放入集合*/
        reqs.put("sign", sign);

        /**请求报文*/
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        HttpClient httpClient = httpClientBuilder.build();
        HttpPost request = new HttpPost(url);

        System.out.println("【请求参数】：" + JSONObject.toJSONString(map));
        System.out.println("【加密前】：" + convertUrlParams(map) + md5Key);
        System.out.println("【sign】：" + sign);
        /**取Sign值，放入集合*/
        reqs.put("sign", sign);
        System.out.println("【加密后map】：" + reqs);

        List<NameValuePair> nameValuePairList = new ArrayList<>();

        for (String key : reqs.keySet()) {
            Object value = reqs.get(key);
            if (null!=value &&StringUtils.isNotBlank(value.toString())) {
                nameValuePairList.add(new BasicNameValuePair(key, value.toString()));
            }
        }
        UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(nameValuePairList, "utf-8");
        formEntity.setContentType("application/x-www-form-urlencoded; charset=UTF-8");
        request.setEntity(formEntity);


        HttpResponse response = httpClient.execute(request);
        HttpEntity httpEntity = response.getEntity();
        String respMsg = EntityUtils.toString(httpEntity);
        return respMsg;
    }


    public static <T> String convertUrlParams(Map<String, T> map) {
        return convertUrlParams(map, "sign");
    }

    public static <T> String convertUrlParams(Map<String, T> map, String filterKey) {
        List<String> list = new ArrayList();
        Object[] keys = map.keySet().toArray();
        String[] newKeys = new String[keys.length];

        for(int i = 0; i < keys.length; ++i) {
            newKeys[i] = keys[i].toString();
        }

        Arrays.sort(newKeys, String.CASE_INSENSITIVE_ORDER);
        String[] var10 = newKeys;
        int var6 = newKeys.length;

        for(int var7 = 0; var7 < var6; ++var7) {
            String key = var10[var7];
            if (!StringUtils.isNotBlank(filterKey) || !filterKey.equalsIgnoreCase(key)) {
                Object value = map.get(key);
                if (value != null && StringUtils.isNotBlank(value.toString())) {
                    list.add(key + "=" + StringEscapeUtils.unescapeHtml4(value.toString()));
                }
            }
        }

        return StringUtils.join(list, "&");
    }

    public static String doPost(String url, Map<String, Object> map, String charset) {


        HttpClient httpClient = null;
        HttpPost httpPost = null;
        String result = null;
        try {
            JSONObject param = new JSONObject();
            Iterator iterator = map.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Object> elem = (Map.Entry<String, Object>) iterator.next();
                param.put(elem.getKey(), elem.getValue());
            }

           /* httpClient = createSSLInsecureClient();
            httpPost = new HttpPost(url);


            StringEntity requestEntity = new StringEntity(param.toString(), ContentType.APPLICATION_JSON);
            httpPost.setEntity(requestEntity);

            HttpResponse response = httpClient.execute(httpPost);*/

            httpPost = new HttpPost(url);
            httpPost.setHeader("Content-Type", "application/json");
            StringEntity se = new StringEntity(param.toString(), ContentType.APPLICATION_JSON);
            se.setContentEncoding("UTF-8");
            httpPost.setEntity(se);

            CloseableHttpClient closeableHttpClient = HttpClients.createDefault();

            HttpResponse response = closeableHttpClient.execute(httpPost);


            if (response != null) {
                HttpEntity resEntity = response.getEntity();
                if (resEntity != null) {
                    result = EntityUtils.toString(resEntity, charset);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.println("responseResult：" + result);
        return result;
    }
}

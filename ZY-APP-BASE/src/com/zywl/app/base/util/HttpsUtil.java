package com.zywl.app.base.util;

import cn.hutool.core.text.UnicodeUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.*;
import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HttpsUtil {

    public static String sendParamsGet(String url) {
        int httpTimeout = 30000;
        CloseableHttpResponse resp = null;
        CloseableHttpClient httpClient = createHttpClient();
        try {
            HttpGet get = new HttpGet(url);
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectionRequestTimeout(httpTimeout)
                    .setConnectTimeout(httpTimeout).setSocketTimeout(httpTimeout).build();
            get.setConfig(requestConfig);
            resp = httpClient.execute(get);
            HttpEntity entity = resp.getEntity();
            String response = EntityUtils.toString(entity, "UTF-8");
            response = UnicodeUtil.toString(response);
            return response;
        } catch (Exception e) {
            return "";
        } finally {
            try {
                if (resp != null) {
                    resp.close();
                }
                httpClient.close();
            } catch (IOException e) {
                return "";
            }
        }
    }

    public static String sendParamsPost(String url, Map<String, Object> map) {
        int httpTimeout = 30000;
        CloseableHttpResponse resp = null;
        CloseableHttpClient httpClient = createHttpClient();
        try {
            HttpPost post = new HttpPost(url);
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectionRequestTimeout(httpTimeout)
                    .setConnectTimeout(httpTimeout).setSocketTimeout(httpTimeout).build();
            post.setConfig(requestConfig);
            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            Set<String> keys = map.keySet();
            for (String key : keys) {
                formparams.add(new BasicNameValuePair(key, map.get(key) == null ? "" : map.get(key).toString()));
            }
            post.setEntity(new UrlEncodedFormEntity(formparams, "utf-8"));
            resp = httpClient.execute(post);
            HttpEntity entity = resp.getEntity();
            String response = EntityUtils.toString(entity, "UTF-8");
            response = UnicodeUtil.toString(response);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        } finally {
            try {
                if (resp != null) {
                    resp.close();
                }
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String sendJsonPost(String url, Map<String, Object> map) {
        JSONObject json = new JSONObject();
        json.putAll(map);
        int httpTimeout = 30000;
        CloseableHttpResponse resp = null;
        CloseableHttpClient httpClient = createHttpClient();
        try {
            HttpPost post = new HttpPost(url);
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectionRequestTimeout(httpTimeout)
                    .setConnectTimeout(httpTimeout).setSocketTimeout(httpTimeout).build();
            post.setConfig(requestConfig);
            StringEntity s = new StringEntity(json.toString(), "UTF-8");
            s.setContentEncoding("UTF-8");
            s.setContentType("application/json");//发送json数据需要设置contentType
            post.setEntity(s);
            resp = httpClient.execute(post);
            HttpEntity entity = resp.getEntity();
            String response = EntityUtils.toString(entity, "UTF-8");
            response = UnicodeUtil.toString(response);
            return response;
        } catch (Exception e) {
            return "";
        } finally {
            try {
                if (resp != null) {
                    resp.close();
                }
                httpClient.close();
            } catch (IOException e) {
                return "";
            }
        }
    }

    public static CloseableHttpClient createHttpClient() {
        SSLContext ctx = null;
        try {
            ctx = SSLContext.getInstance("TLS");
            ctx.init(new KeyManager[0], new TrustManager[]{new DefaultTrustManager()}, new SecureRandom());
            SSLContext.setDefault(ctx);
        } catch (Exception e) {
        }
        SSLConnectionSocketFactory sslConnectionFactory = new SSLConnectionSocketFactory(ctx,
                NoopHostnameVerifier.INSTANCE);

        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.INSTANCE).register("https", sslConnectionFactory).build();

        CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslConnectionFactory).build();
        return httpClient;
    }

    private static class TrustAnyHostnameVerifier implements HostnameVerifier {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }

    private static class DefaultTrustManager implements X509TrustManager {
        public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
        }

        public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
        }

        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }
}

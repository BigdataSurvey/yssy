package com.zywl.app.base.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HTTPUtil {

	protected static final String ENCODING = "UTF-8";
	
	private static RequestConfig requestConfig = RequestConfig.custom()
    		.setSocketTimeout(10000)
    		.setConnectTimeout(10000).setConnectionRequestTimeout(10000).build();

    public static String postJSON(String url, JSONObject data) {
    	return postJSON(url, JSON.toJSONString(data));
    }

	public static String postJSON(String url, String data) {
		return postJSON(url, data, null);
	}
	
    public static String postJSON(String url, String data, Map<String, String> header) {
    	String response = null;
		try {
			CloseableHttpClient httpclient = null;
			CloseableHttpResponse httpresponse = null;
			try {
				httpclient = HttpClients.createDefault();
				HttpPost httppost = new HttpPost(url);
				if(header != null){
					for (String key : header.keySet()) {
						httppost.setHeader(key, header.get(key));
					}
				}
				StringEntity stringentity = new StringEntity(data,
						ContentType.create("application/json", "UTF-8"));
				httppost.setEntity(stringentity);
				httppost.setConfig(requestConfig);
				httpresponse = httpclient.execute(httppost);
				response = EntityUtils.toString(httpresponse.getEntity());
			} finally {
				if (httpclient != null) {
					httpclient.close();
				}
				if (httpresponse != null) {
					httpresponse.close();
				}
			}
		} catch (Exception e) {
		}
		return response;
    }

    public static String post(String url){
    	return post(url, null, null);
    }
    
	public static String post(String url, Map<String, String> params, Map<String, String> header) {
		try {
			HttpPost httpPost = new HttpPost(url);
			if (params != null) {
				List<NameValuePair> formParas = new ArrayList<NameValuePair>();
				for (String key : params.keySet()) {
					formParas.add(new BasicNameValuePair(key, params.get(key)));
				}
				httpPost.setEntity(new UrlEncodedFormEntity(formParas, ENCODING));
			}
			if(header != null){
				for (String key : header.keySet()) {
					httpPost.setHeader(key, header.get(key));
				}
			}
			httpPost.setConfig(requestConfig);
			CloseableHttpClient httpClient = HttpClients.createDefault();
			CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
			try {
				HttpEntity httpEntity = httpResponse.getEntity();
				if (httpEntity != null) {
					return EntityUtils.toString(httpEntity, ENCODING);
				}
			} catch (Exception e) {
			} finally {
				httpResponse.close();
				httpClient.close();
			}
		} catch (Exception e) {
		}
		return null;
	}

	public static String get(String url) {
		return get(url, ENCODING);
	}
	
	public static String get(String url, String encoding) {
		try{
			HttpGet httpGet = new HttpGet(url);
			httpGet.setConfig(requestConfig);
			try (CloseableHttpClient httpClient = HttpClients.createDefault();
					CloseableHttpResponse httpResponse = httpClient.execute(httpGet, new BasicHttpContext());){
				HttpEntity httpEntity = httpResponse.getEntity();
				if(httpEntity != null){
					return EntityUtils.toString(httpEntity, encoding);
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String get(String url, Map<String, String> header) {
		try{
			HttpGet httpGet = new HttpGet(url);
			if(header != null){
				for (String key : header.keySet()) {
					httpGet.setHeader(key, header.get(key));
				}
			}
			httpGet.setConfig(requestConfig);
			try (CloseableHttpClient httpClient = HttpClients.createDefault();
				 CloseableHttpResponse httpResponse = httpClient.execute(httpGet, new BasicHttpContext());){
				HttpEntity httpEntity = httpResponse.getEntity();
				if(httpEntity != null){
					return EntityUtils.toString(httpEntity, ENCODING);
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	public static void main(String[] args) throws InterruptedException {
		for (int i = 0; i < 100; i++) {
			String getJSON = HTTPUtil.get("https://restapi.amap.com/v3/geocode/regeo?key=bdc7017b4653ce54e2f19c482481c2df&location=116.480881,39.989410&poitype=公安警察&radius=50&extensions=all&batch=false&roadlevel=0");
			System.out.println(getJSON);
			Thread.sleep(20);
		}
		
		//rtmp://1019.lsspublish.aodianyun.com
		//rtmp://1019.lssplay.aodianyun.com
		
//		String time = (System.currentTimeMillis() / 1000L) + "";
//		
//		System.out.println(DigestUtils.md5Hex("mitaostreamTyo8sFC1SXTnSk0BqlM9pKLpg1dMs3D2"+time));
//		System.out.println(time);
//		
//		JSONObject data = new JSONObject();
//		data.put("access_id", "386867370934");
//		data.put("access_key", "Tyo8sFC1SXTnSk0BqlM9pKLpg1dMs3D2");
//		
//		String url = "https://openapi.aodianyun.com/v2/LSS.GetApp";
//		data.put("page", 1);
//		data.put("num", 10);
		
//		String url = "http://openapi.aodianyun.com/v2/LSS.OpenApp";
//		data.put("appid", "mitao");
//		data.put("appname", "水蜜桃");
		
//		String url = "http://openapi.aodianyun.com/v2/LSS.GetPullStream";
//		data.put("appid", "mitao");
//		data.put("page", 1);
//		data.put("num", 10);
		
//		String url = "http://openapi.aodianyun.com/v2/LSS.PullStreamOp";
//		data.put("appid", "mitao");
//		data.put("type", "fromList");
//		data.put("from", "[\"http://xxx.aodianyun.com\"]");
//		data.put("stream", "test");
		
//		String url = "http://openapi.aodianyun.com/v2/LSS.PullStreamOp";
//		data.put("appid", "mitao");
//		data.put("type", "play");
//		data.put("from", "http://1009.aodianyun.com");
//		data.put("stream", "test");
		
//		String url = "https://openapi.aodianyun.com/v2/LSS.GetAppStreamLiving";
//		data.put("appid", "mitao");
		
//		String postJSON = postJSON(url, data);
//		System.out.println(postJSON);
	}
}

package com.zywl.app.base.util;

import com.alibaba.fastjson2.JSONObject;

/**
 * 数美接口
 */
public class ShuMeiUtil {


    private static final String URL = "http://api-skynet-bj.fengkongcloud.com/v4/event";

    private static final String ACCESS_KEY="DXdO0IumgET8YO7g5LmU";

    private static final String APP_ID="default";


    public static String  requestShuMei(String tokenId,String eventId,String ip,String deviceId,String type){
        JSONObject body = new JSONObject();
        body.put("accessKey",ACCESS_KEY);
        body.put("appId",APP_ID);
        body.put("eventId",eventId);
        JSONObject data = new JSONObject();
        data.put("tokenId",tokenId);
        data.put("ip",ip);
        data.put("timestamp",System.currentTimeMillis());
        data.put("deviceId",deviceId);
        body.put("data",data);
        String result=null;
        try {
            result = HTTPUtil.postJSON(URL, body);
        }catch (Exception e){
            e.printStackTrace();
        }
        System.out.println(result);
        return result;


    }
}

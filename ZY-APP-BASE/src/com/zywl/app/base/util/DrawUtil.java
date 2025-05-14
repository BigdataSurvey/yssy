package com.zywl.app.base.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DrawUtil {


    public static JSONObject draw(JSONArray array, List<String> idList) {
        if (array == null || array.size() == 0) {
            return null;
        }
        List<JSONObject> list = JSON.parseArray(array.toJSONString(), JSONObject.class);
        list.sort(((o1, o2) -> (o1.getIntValue("rate") - o2.getIntValue("rate")) > 0 ? 1 : -1));
        return get(list,idList);
    }
    public static JSONObject get(  List<JSONObject> list, List<String> idList){
        List<JSONObject> newList = new ArrayList<>();
        int maxRate = 0;
        for (JSONObject object : list) {
            if (!idList.contains(object.getString("id"))){
                newList.add(object);
                maxRate = object.getIntValue("rate");
            }
        }
        Random r = new Random();
        int i = r.nextInt(maxRate);
        for (JSONObject jsonObject : newList) {
            if (jsonObject.getIntValue("id") == 1 || jsonObject.getIntValue("id") == 2) {
                continue;
            }
            if (i > jsonObject.getIntValue("rate")) {
                continue;
            }
            return jsonObject;
        }
        return get(list, idList);
    }

}

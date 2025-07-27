package com.zywl.app.defaultx.service;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.User;
import com.zywl.app.base.bean.UserPit;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.defaultx.dbutil.DaoService;
import com.zywl.app.defaultx.enmus.LogUserBackpackTypeEnum;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.SimpleFormatter;

@Service
public class UserPitService extends DaoService {


    public UserPitService() {
        super("UserPitMapper");
    }

    private static final Log logger = LogFactory.getLog(UserPitService.class);

    public UserPit findInitInfo(JSONObject params) {
        Map<String, Object> param = new HashedMap<>();
        param.put("userId", params.get("userId"));
        param.put("pitId", params.get("pitId"));
        return (UserPit) findOne("findInitInfoList", param);
    }

    public List<UserPit> findPitList(JSONObject params) {
        Map<String, Object> param = new HashedMap<>();
        param.put("userId", params.get("userId"));
        param.put("pitId", params.get("pitId"));
        return findList("findInitInfoList", param);
    }

    @Transactional
    public int insertUserPit(JSONObject jsonObject) {

        Map<String, Object> params = new HashedMap<>();
        params.put("userId", jsonObject.getIntValue("userId"));
        params.put("pitId",  jsonObject.getIntValue("pitId"));
        params.put("openTime",  new Date());
        params.put("lastLookTime",  new Date());
        params.put("lastReceiveTime",  new Date());
        JSONArray array = new JSONArray();
        JSONObject jsonObject1 = new JSONObject();
        jsonObject1.put("type",1);
        jsonObject1.put("id",jsonObject.getLongValue("id"));
        jsonObject1.put("number",0);
        array.add(jsonObject1);
        params.put("endTime", DateUtil.getDateByDay(60));
        params.put("unReceive", array);
        return execute("insert",params);
    }

    @Transactional
    public int receiveNumber(JSONObject jsonObject) {
        Map<String, Object> params = new HashedMap<>();
        params.put("userId", jsonObject.get("userId"));
        params.put("pitId",  jsonObject.get("pitId"));
        params.put("lastReceiveTime",  new Date());
        // 格式要[{"type":1,"id":9,"number":3}]
        JSONArray array = new JSONArray();
        JSONObject jsonObject1 = new JSONObject();
        jsonObject1.put("type",1);
        jsonObject1.put("id",9);
        jsonObject1.put("number",0);
        array.add(jsonObject1);
        params.put("unReceive", array);
        return execute("update",params);
    }

    @Transactional
    public void updateUserPit(UserPit userPit){
        execute("update",userPit);
    }

    @Transactional
    public int batchUpdateNumber(List<UserPit> list) {
        return execute("batchUpdateNumber",list);
    }

    public List<UserPit> findOpenPitTypeByUserId(JSONObject o) {
        Map<String, Object> params = new HashedMap<>();
        params.put("userId", o.getIntValue("userId"));
        return findList("findOpenPitTypeByUserId", params);
    }

    public void deleteUserPit(Long userId,Long pitId){
        Map<String, Object> map = new HashedMap<>();
        map.put("userId", userId);
        map.put("pitId",pitId);
        delete(map);
    }
}

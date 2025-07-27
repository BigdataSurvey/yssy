package com.zywl.app.defaultx.service;

import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.PitUserParent;
import com.zywl.app.base.bean.UserPit;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.apache.commons.collections4.map.HashedMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class PitUserParentService extends DaoService {

    public PitUserParentService() {
        super("PitUserParentMapper");
    }
    public PitUserParent findParentByUserId(Long userId) {
        Map<String, Object> params = new HashedMap<>();
        params.put("userId", userId);
        return (PitUserParent) findOne("findParentId", params);
    }
    public List<PitUserParent> findSubor(JSONObject params) {
        Map<String, Object> map = new HashedMap<>();
        map.put("userId", params.getLong("userId"));
        map.put("start", params.getIntValue("page")*params.getIntValue("num"));
        map.put("limit",  params.getIntValue("num"));
        return  findList("findSubor", map);
    }

    @Transactional
    public int insertPitUserParent(JSONObject params) {
        Map<String, Object> map = new HashedMap<>();
        map.put("userId", params.getLongValue("userId"));
        map.put("pitParentId", params.getLongValue("parentId"));
        map.put("pitGrandfaId", params.getLongValue("pitGrandfaId"));
        map.put("createParentAmount", 0);
        map.put("createGrandfaAmount", 0);
        return execute("insertPitUserParent",map);
    }

    @Transactional
    public int updateParent(JSONObject params) {
        Map<String, Object> map = new HashedMap<>();
        map.put("userId", params.getLong("userId"));
        map.put("pitId",params.getLongValue("pitId"));
        map.put("createParentAmount", params.getBigDecimal("createParentAmount"));
        map.put("createGrandfaAmount", params.getBigDecimal("createGrandfaAmount"));
        return execute("update",map);
    }

    public List<PitUserParent> findIndirSubor(JSONObject params) {
        Map<String, Object> map = new HashedMap<>();
        map.put("userId", params.getLong("userId"));
        map.put("start", params.getIntValue("page")*params.getIntValue("num"));
        map.put("limit",  params.getIntValue("num"));
        return  findList("findIndirSubor", map);
    }

    @Transactional
    public void addParentIncome(Long userId,int number){
        Map<String, Object> map = new HashedMap<>();
        map.put("userId", userId);
        map.put("number", number);
        execute("addParentIncome",map);
    }

    @Transactional
    public void addGrandfaIncome(Long userId,int number){
        Map<String, Object> map = new HashedMap<>();
        map.put("userId", userId);
        map.put("number", number);
        execute("addGrandfaIncome",map);
    }


}

package com.zywl.app.defaultx.service;

import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.PitUserParent;
import com.zywl.app.base.bean.UserPit;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.apache.commons.collections4.map.HashedMap;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class PitUserParentService extends DaoService {

    public PitUserParentService() {
        super("PitUserParentMapper.xml");
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
    public int insertPitUserParent(JSONObject params) {
        Map<String, Object> map = new HashedMap<>();
        map.put("userId", params.getIntValue("userId"));
        map.put("pitParentId", params.getIntValue("parentId"));
        map.put("pitGrandfaId", params.getIntValue("pitGrandfaId"));
        map.put("createParentAmount", params.getBigDecimal("createParentAmount"));
        map.put("createGrandfaAmount", params.getBigDecimal("createGrandfaAmount"));
        return execute("insertPitUserParent",map);
    }
    public int updateParent(JSONObject params) {
        Map<String, Object> map = new HashedMap<>();
        map.put("userId", params.getLong("userId"));
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
}

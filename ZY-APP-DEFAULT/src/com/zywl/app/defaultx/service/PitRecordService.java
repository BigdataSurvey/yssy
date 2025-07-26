package com.zywl.app.defaultx.service;

import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.PitRecord;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.apache.commons.collections4.map.HashedMap;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class PitRecordService extends DaoService {

    public PitRecordService() {
        super("PitRecordMapper.xml");
    }

    public List<PitRecord> findPitRecord(JSONObject params) {
        Map<String, Object> param = new HashedMap<>();
        param.put("userId", params.getLong("userId"));
        param.put("start", params.getIntValue("page")*params.getIntValue("num"));
        param.put("limit",  params.getIntValue("num"));
        return findList("findPitRecord", param);
    }
    public int batchAddRecord(List<PitRecord> pitRecordList) {
        return execute("batchAddRecord",pitRecordList);
    }

    public List<PitRecord> findPitRecordByParentIds(List<Integer> suborListIds) {
        Map<String, Object> param = new HashedMap<>();
        param.put("userIds", suborListIds);
        return findList("findPitRecordByIds", param);

    }

    public List<PitRecord> findPitRecordByGrandfaIds(List<Integer> indirSuborListIds) {
        Map<String, Object> param = new HashedMap<>();
        param.put("userIds", indirSuborListIds);
        return findList("findPitRecordByIds", param);
    }

}

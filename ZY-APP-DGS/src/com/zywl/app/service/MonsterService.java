package com.zywl.app.service;

import com.zywl.app.base.bean.Monster;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.apache.commons.collections4.map.HashedMap;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class MonsterService extends DaoService {


    public MonsterService() {
        super("MonsterMapper");
    }

    public List<Monster> findMonsterByStatus(Long monsterType, Integer dieStatus) {
        Map<String, Object> params = new HashedMap<String, Object>();
        params.put("monsterType", monsterType);
        params.put("dieStatus", dieStatus);
        return findList("findMonsterByStatus", params);
    }

    public int updateMonSterStatus(Monster monster) {
        return update(monster);
    }
}

package com.zywl.app.service;

import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.Monster;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.apache.commons.collections4.map.HashedMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MonsterService extends DaoService {


    public MonsterService() {
        super("MonsterMapper");
    }

    public List<Monster> findMonsterByStatus(Integer monsterType, Integer dieStatus) {
        Map<String, Object> params = new HashedMap<String, Object>();
        params.put("monsterType", monsterType);
        params.put("dieStatus", dieStatus);
        return findList("findMonsterByStatus", params);
    }
    public Map<Integer, Integer> findMonsterInitStatus( Integer dieStatus) {
        Map<String, Object> params = new HashedMap<String, Object>();
        params.put("dieStatus", dieStatus);
        List<Monster> initStatus = findList("findMonsterInitStatus", params);
        Map<Integer, Integer> maps2 = initStatus.stream().collect(Collectors.toMap(Monster::getMonsterType, Monster::getCurrBlood));
        return maps2;
    }

    public int updateMonSterStatus(Monster monster) {
        return update(monster);
    }

    @Transactional
    public void batchUpdateBetAmount(List<Monster> newList) {
        execute("batchUpdateRecord", newList);
    }
}

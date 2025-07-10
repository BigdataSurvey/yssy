package com.zywl.app.defaultx.service;

import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.Monster;
import com.zywl.app.base.bean.PayOrder;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.apache.commons.collections4.map.HashedMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MonsterService extends DaoService {


    public MonsterService() {
        super("MonsterMapper");
    }



    @Transactional
    public Monster addMonster(Integer monsterType,Integer dieStatus,Long monsterNo) {
        Monster monster = new Monster();
        monster.setCreateTime(new Date());
        monster.setUpdateTime(new Date());
        monster.setMonsterType(monsterType);
        monster.setDieStatus(dieStatus);
        monster.setMonsterNo(monsterNo);
        monster.setCurrBlood(1000);
        save(monster);
        return monster;
    }

    public Monster findMonsterByStatus(Integer monsterType, Integer dieStatus) {
        Map<String, Object> params = new HashedMap<>();
        params.put("monsterType", monsterType);
        params.put("dieStatus", dieStatus);
        Monster monster = (Monster) findOne("findMonsterByStatus", params);
        if(null!=monster){
            return monster;
        }else {
            //插入新怪兽
            Monster newMonsterByType = findNewMonsterByType(monsterType);
            Long monsterNo = 1L;
            if(null != newMonsterByType){
                monsterNo = newMonsterByType.getMonsterNo()+1;
                newMonsterByType.setMonsterType(monsterType);
                newMonsterByType.setMonsterNo(monsterNo);
            }
            return addMonster(monsterType,0,monsterNo);
        }
    }

    public Monster findNewMonsterByType(Integer monsterType){
        Map<String, Object> params = new HashedMap<>();
        params.put("monsterType", monsterType);
        return (Monster) findOne("findNewMonsterByType",params);
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

    @Transactional
    public void updateMonster(Monster monster) {
        execute("updateMonster", monster);
    }
}

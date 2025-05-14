package com.zywl.app.defaultx.cache;


import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.User;
import com.zywl.app.base.bean.vo.KillNsRecordRandVo;
import com.zywl.app.base.constant.RedisKeyConstant;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.defaultx.cache.impl.RedisService;
import com.zywl.app.defaultx.enmus.GameTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.*;

@Service
public class GameCacheService extends RedisService {



    @Autowired
    private UserCacheService userCacheService;

    public static final List<String> LAST_WEEK_USER_IDS = new ArrayList<>();


    public void updateLastWeekList(){
        LAST_WEEK_USER_IDS.clear();
        Map<String, Double> lastWeekTopList = getLastWeekTopList(GameTypeEnum.battleRoyale.getValue(), 10);
        ArrayList<Map.Entry<String, Double>> list = new ArrayList(lastWeekTopList.entrySet());
        Collections.sort(list, (a, b)-> (int) (b.getValue() - a.getValue()));
        for (Map.Entry<String, Double> stringDoubleEntry : list) {
            LAST_WEEK_USER_IDS.add(stringDoubleEntry.getKey());
        }
    }


    public List<JSONObject> getLastWeekList(){
        String key = RedisKeyConstant.GAME_LAST_WEEK_TOP_LIST_XHMJ+DateUtil.getFirstDayOfWeek(new Date());
        List<JSONObject> array = getList(key,JSONObject.class);
        if (array==null || array.size()==0){
            Map<String, Double> lastWeekTopList = getLastWeekTopList(GameTypeEnum.dts2.getValue(), 10);
            Set<String> ids = lastWeekTopList.keySet();
            array =new ArrayList<>();
            for (String id : ids) {
                User userInfoById = userCacheService.getUserInfoById(id);
                JSONObject info = new JSONObject();
                info.put("userHeadImg",userInfoById.getHeadImageUrl());
                info.put("userId",id);
                info.put("userName",userInfoById.getName());
                info.put("userNo",userInfoById.getUserNo());
                info.put("score",lastWeekTopList.get(id));
                array.add(info);
            }
            set(key,array,86400*10);
        }
        return  array;
    }

    public List<JSONObject> getThisWeekList(){
        String key  = RedisKeyConstant.GAME_RANK_LIST;
        List<JSONObject> list = getList(key,JSONObject.class);
        if (list==null || list.size()==0){
            Map<String, Double> lastWeekTopList = getThisWeekTopList(GameTypeEnum.dts2.getValue(), 10);
            Set<String> ids = lastWeekTopList.keySet();
            list =new ArrayList<>();
            for (String id : ids) {
                User userInfoById = userCacheService.getUserInfoById(id);
                JSONObject info = new JSONObject();
                info.put("userHeadImg",userInfoById.getHeadImageUrl());
                info.put("userId",id);
                info.put("userName",userInfoById.getName());
                info.put("userNo",userInfoById.getUserNo());
                info.put("score",lastWeekTopList.get(id));
                list.add(info);
            }
            set(key,list,60);
        }
        return list;
    }


    public Double getUserRankScore(int gameId,String userId){
        String key = getRankKey(gameId);
        return getZsetScore(key,userId);
    }
    public Map<String,Double> getThisWeekTopList(int gameId,int count){
        String key = getRankKey(gameId);
        Set<ZSetOperations.TypedTuple<String>> zset = getZset(key, count);
        Map<String,Double> map = new HashMap<>();
        for (ZSetOperations.TypedTuple<String> s : zset) {
            Double score = s.getScore();
            map.put(s.getValue(), score);
        }
        return map;
    }

    public Long getUserKillRank(String userId){
        return getZsetRank(userId,RedisKeyConstant.KILL_RANK+DateUtil.format2(new Date()));
    }
    public Double getUserLastWeekRankScore(int gameId,String userId){
        String key = getLastWeekRankKey(gameId);
        return getZsetScore(key,userId);
    }

    public String getRankKey(int gameId){
        String key = null;
        if (gameId== GameTypeEnum.battleRoyale.getValue()||gameId==GameTypeEnum.dts2.getValue()){
            key = RedisKeyConstant.GAME_RANK_DTS+ DateUtil.getFirstDayOfWeek(new Date());
        } else if (gameId==GameTypeEnum.food.getValue()) {
            key=RedisKeyConstant.GAME_RANK_FOOD+DateUtil.getFirstDayOfWeek(new Date());
        } else if (gameId == GameTypeEnum.ns.getValue()) {
            key=RedisKeyConstant.GAME_RANK_NS+DateUtil.getFirstDayOfWeek(new Date());
        } else if (gameId==GameTypeEnum.nh.getValue()) {
            key=RedisKeyConstant.GAME_RANK_NH+DateUtil.getFirstDayOfWeek(new Date());
        }
        return key;
    }

    public String getLastWeekRankKey(int gameId){
        String key = null;
        if (gameId== GameTypeEnum.battleRoyale.getValue()||gameId==GameTypeEnum.dts2.getValue()){
            key = RedisKeyConstant.GAME_RANK_DTS+ DateUtil.getFirstDayOfLastWeek();
        } else if (gameId==GameTypeEnum.food.getValue()) {
            key=RedisKeyConstant.GAME_RANK_FOOD+DateUtil.getFirstDayOfLastWeek();
        } else if (gameId == GameTypeEnum.ns.getValue()) {
            key=RedisKeyConstant.GAME_RANK_NS+DateUtil.getFirstDayOfLastWeek();
        } else if (gameId==GameTypeEnum.nh.getValue()) {
            key=RedisKeyConstant.GAME_RANK_NH+DateUtil.getFirstDayOfLastWeek();
        }
        return key;
    }

    public Map<String,Double> getLastWeekTopList(int gameId,int count){
        String key = getLastWeekRankKey(gameId);
        Set<ZSetOperations.TypedTuple<String>> zset = getZset(key, count);
        Map<String,Double> map = new HashMap<>();
        for (ZSetOperations.TypedTuple<String> s : zset) {
            Double score = s.getScore();
            map.put(s.getValue(), score);
        }
        return map;
    }
    public void addGameRankCache(int gameId, String userId){
        if (LAST_WEEK_USER_IDS.contains(userId)){
            return;
        }
        String key = getRankKey(gameId);
        addZset(key,userId,1.0);
    }


    public Map<String,Double> getKillThisDayTopList(int count){
        String key = RedisKeyConstant.KILL_RANK+DateUtil.format2(new Date()) ;
        Set<ZSetOperations.TypedTuple<String>> zset = getZset(key, count);
        Map<String,Double> map = new HashMap<>();
        for (ZSetOperations.TypedTuple<String> s : zset) {
            Double score = s.getScore();
            map.put(s.getValue(), score);
        }
        return map;
    }




    public List<JSONObject> getKillThisDayList(){
        String key  = RedisKeyConstant.KILL_RANK_LIST;
        List<JSONObject> list = getList(key,JSONObject.class);
        if (list==null || list.size()==0){
            Map<String, Double> lastWeekTopList = getKillThisDayTopList( 50);
            Set<String> ids = lastWeekTopList.keySet();
            list =new ArrayList<>();
            for (String id : ids) {
                User userInfoById = userCacheService.getUserInfoById(id);
                JSONObject info = new JSONObject();
                info.put("userHeadImg",userInfoById.getHeadImageUrl());
                info.put("userId",id);
                info.put("userName",userInfoById.getName());
                info.put("userNo",userInfoById.getUserNo());
                info.put("score",lastWeekTopList.get(id));
                list.add(info);
            }
            set(key,list,60*10);
        }
        return list;
    }

    public Long getThisWeekUserRank(String userId){
        return getZsetRank(userId,RedisKeyConstant.GAME_RANK_DTS+ DateUtil.getFirstDayOfWeek(new Date()));
    }

    public Long getLastWeekUserRank(String userId){
        return getZsetRank(userId,RedisKeyConstant.GAME_RANK_DTS+DateUtil.getFirstDayOfLastWeek());
    }
}

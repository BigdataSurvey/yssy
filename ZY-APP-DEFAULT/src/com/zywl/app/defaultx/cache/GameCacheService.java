package com.zywl.app.defaultx.cache;


import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.Activity;
import com.zywl.app.base.bean.User;
import com.zywl.app.base.bean.UserCapital;
import com.zywl.app.base.constant.RedisKeyConstant;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.defaultx.cache.impl.RedisService;
import com.zywl.app.defaultx.enmus.GameTypeEnum;
import com.zywl.app.defaultx.enmus.UserCapitalTypeEnum;
import com.zywl.app.defaultx.service.Activity2Service;
import com.zywl.app.defaultx.service.ActivityService;
import com.zywl.app.defaultx.service.CashRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class GameCacheService extends RedisService {


    public static final double DOUBLE = 20.5;
    @Autowired
    private UserCacheService userCacheService;

    @Autowired
    private UserCapitalCacheService userCapitalCacheService;
    @Autowired
    private CashRecordService cashRecordService;

    @Autowired
    private ActivityService activityService;

    @Autowired
    private Activity2Service activity2Service;

    public static Activity activity;

    public static Activity activity2;

    public static final List<String> LAST_WEEK_USER_IDS = new ArrayList<>();

    @PostConstruct
    public void _construct() {
        updateLastWeekList();
        new Timer("刷新大逃杀排行榜上周前10数据").schedule(new TimerTask() {
            public void run() {
                try {
                    logger.info("刷新大逃杀排行榜上周前10数据");
                    updateLastWeekList();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, DateUtil.getTomorrowBegin() - System.currentTimeMillis(), 1000 * 60 * 60 * 24);
    }

    public void updateLastWeekList() {
        LAST_WEEK_USER_IDS.clear();
        Map<String, Double> lastWeekTopList = getLastWeekTopList(GameTypeEnum.battleRoyale.getValue(), 10);
        ArrayList<Map.Entry<String, Double>> list = new ArrayList(lastWeekTopList.entrySet());
        Collections.sort(list, (a, b) -> (int) (b.getValue() - a.getValue()));
        for (Map.Entry<String, Double> stringDoubleEntry : list) {
            LAST_WEEK_USER_IDS.add(stringDoubleEntry.getKey());
        }
    }

    public void addGameRankCache(int gameId, String userId, int number) {
        if (LAST_WEEK_USER_IDS.contains(userId)) {
            return;
        }
        String key = getRankKey(gameId);
        addZset(key, userId, (double) number);
    }


    public Double getUserRankScore(int gameId, String userId) {
        String key = getRankKey(gameId);
        return getZsetScore(key, userId);
    }


    public Long getUserKillRank(String userId) {
        return getZsetRank(userId, RedisKeyConstant.KILL_RANK + DateUtil.format2(new Date()));
    }

    public Double getUserLastWeekRankScore(int gameId, String userId) {
        String key = getLastWeekRankKey(gameId);
        return getZsetScore(key, userId);
    }

    public String getRankKey(int gameId) {
        String key = null;
        if (gameId == GameTypeEnum.battleRoyale.getValue() || gameId == GameTypeEnum.dts2.getValue()) {
            key = RedisKeyConstant.GAME_RANK_DTS + DateUtil.getFirstDayOfWeek(new Date());
        } else if (gameId == GameTypeEnum.food.getValue()) {
            key = RedisKeyConstant.GAME_RANK_FOOD + DateUtil.getFirstDayOfWeek(new Date());
        } else if (gameId == GameTypeEnum.ns.getValue()) {
            key = RedisKeyConstant.GAME_RANK_NS + DateUtil.getFirstDayOfWeek(new Date());
        } else if (gameId == GameTypeEnum.nh.getValue()) {
            key = RedisKeyConstant.GAME_RANK_NH + DateUtil.getFirstDayOfWeek(new Date());
        }else if (gameId == GameTypeEnum.dgs.getValue()) {
            key = RedisKeyConstant.GAME_LAST_WEEK_TOP_LIST_DGS + DateUtil.getFirstDayOfWeek(new Date());
        }
        return key;
    }

    public String getLastWeekRankKey(int gameId) {
        String key = null;
        if (gameId == GameTypeEnum.battleRoyale.getValue() || gameId == GameTypeEnum.dts2.getValue()) {
            key = RedisKeyConstant.GAME_RANK_DTS + DateUtil.getFirstDayOfLastWeek();
        } else if (gameId == GameTypeEnum.food.getValue()) {
            key = RedisKeyConstant.GAME_RANK_FOOD + DateUtil.getFirstDayOfLastWeek();
        } else if (gameId == GameTypeEnum.ns.getValue()) {
            key = RedisKeyConstant.GAME_RANK_NS + DateUtil.getFirstDayOfLastWeek();
        } else if (gameId == GameTypeEnum.nh.getValue()) {
            key = RedisKeyConstant.GAME_RANK_NH + DateUtil.getFirstDayOfLastWeek();
        } else if (gameId == GameTypeEnum.dgs.getValue()) {
            key = RedisKeyConstant.GAME_LAST_WEEK_TOP_LIST_DGS + DateUtil.getFirstDayOfLastWeek();
        }
        return key;
    }

    public Map<String, Double> getLastWeekTopList(int gameId, int count) {
        String key = getLastWeekRankKey(gameId);
        Set<ZSetOperations.TypedTuple<String>> zset = getZset(key, count);
        Map<String, Double> map = new HashMap<>();
        for (ZSetOperations.TypedTuple<String> s : zset) {
            Double score = s.getScore();
            map.put(s.getValue(), score);
        }
        return map;
    }


    public Map<String, Double> getThisWeekTopList(int gameId, int count) {
        String key = getRankKey(gameId);
        Set<ZSetOperations.TypedTuple<String>> zset = getZset(key, count);
        Map<String, Double> map = new HashMap<>();
        for (ZSetOperations.TypedTuple<String> s : zset) {
            Double score = s.getScore();
            map.put(s.getValue(), score);
        }
        return map;
    }

    public Map<String, Double> getKillThisDayTopList(int count) {
        String key = RedisKeyConstant.KILL_RANK + DateUtil.format2(new Date());
        Set<ZSetOperations.TypedTuple<String>> zset = getZset(key, count);
        Map<String, Double> map = new HashMap<>();
        for (ZSetOperations.TypedTuple<String> s : zset) {
            Double score = s.getScore();
            map.put(s.getValue(), score);
            ;
        }
        return map;
    }

    public List<JSONObject> getLastWeekListDts(){
        String key = RedisKeyConstant.GAME_LAST_WEEK_TOP_LIST+DateUtil.getFirstDayOfWeek(new Date());
        List<JSONObject> array = getList(key,JSONObject.class);
        if (array==null || array.size()==0){
            Map<String, Double> lastWeekTopList = getLastWeekTopList(GameTypeEnum.dts2.getValue(), 10);
            Set<String> ids = lastWeekTopList.keySet();
            array =new ArrayList<>();
            for (String id : ids) {
                User userInfoById = userCacheService.getUserInfoById(id);
                if (userInfoById==null){
                    continue;
                }
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
        safeSortByScoreDesc(array);
        return  array;
    }

    public List<JSONObject> getLhdLastWeekList() {
        String key = RedisKeyConstant.GAME_LAST_WEEK_TOP_LIST_LHD + DateUtil.getFirstDayOfWeek(new Date());
        List<JSONObject> array = getList(key, JSONObject.class);
        if (array == null || array.size() == 0) {
            Map<String, Double> lastWeekTopList = getLastWeekTopList(GameTypeEnum.nh.getValue(), 10);
            Set<String> ids = lastWeekTopList.keySet();
            array = new ArrayList<>();
            for (String id : ids) {
                User userInfoById = userCacheService.getUserInfoById(id);
                if (userInfoById==null){
                    continue;
                }
                JSONObject info = new JSONObject();
                info.put("userHeadImg", userInfoById.getHeadImageUrl());
                info.put("userId", id);
                info.put("userName", userInfoById.getName());
                info.put("userNo", userInfoById.getUserNo());
                info.put("score", lastWeekTopList.get(id));
                array.add(info);
            }
            set(key, array, 86400 * 10);
        }
        safeSortByScoreDesc(array);
        return array;
    }

    public List<JSONObject> getDGSLastWeekList() {
        String key = RedisKeyConstant.GAME_LAST_WEEK_TOP_LIST_DGS + DateUtil.getFirstDayOfWeek(new Date());
        List<JSONObject> array = getList(key, JSONObject.class);
        if (array == null || array.size() == 0) {
            Map<String, Double> lastWeekTopList = getLastWeekTopList(GameTypeEnum.dgs.getValue(), 10);
            Set<String> ids = lastWeekTopList.keySet();
            array = new ArrayList<>();
            for (String id : ids) {
                User userInfoById = userCacheService.getUserInfoById(id);
                if (userInfoById==null){
                    continue;
                }
                JSONObject info = new JSONObject();
                info.put("userHeadImg", userInfoById.getHeadImageUrl());
                info.put("userId", id);
                info.put("userName", userInfoById.getName());
                info.put("userNo", userInfoById.getUserNo());
                info.put("score", lastWeekTopList.get(id));
                array.add(info);
            }
            set(key, array, 86400 * 10);
        }
        safeSortByScoreDesc(array);
        return array;
    }
    public List<JSONObject> getActiveTopList() {
        String key = RedisKeyConstant.APP_TOP_lIST + getActivity().getId();
        String rankKey = RedisKeyConstant.POINT_RANK_LIST;
        return getTopList(key, rankKey,3);

    }
    public List<JSONObject> getActiveTopList2() {
        String key = RedisKeyConstant.APP_TOP_lIST_2 + getActivity2().getId();
        String rankKey = RedisKeyConstant.POINT_RANK_LIST_2;
        return getTopList(key, rankKey,1);

    }


    public List<JSONObject> getLastActiveTopList(Activity activity) {
        String key = RedisKeyConstant.APP_TOP_lIST + activity.getId();
        Map<String, Double> thisTopList = getActiveThisTopList(key, 3);
        Set<String> ids = thisTopList.keySet();
        List<JSONObject> list = new ArrayList<>();
        double allScore = 0.0;
        for (String id : ids) {
            Double score = thisTopList.get(id);
            long rank = getTopRankByKey(key, id) + 1;
            User userInfoById = userCacheService.getUserInfoById(id);
            JSONObject info = new JSONObject();
            info.put("userHeadImg", userInfoById.getHeadImageUrl());
            info.put("userId", id);
            info.put("userName", userInfoById.getName());
            info.put("userNo", userInfoById.getUserNo());
            info.put("score", score);
            info.put("rank", rank);
            list.add(info);
            allScore += score;
        }
        //通过判断奖励规则 来计算金额
        if (activity.getMoneyRule() == 1) {
            //1积分多少钱
            for (JSONObject jsonObject : list) {
                Double score = jsonObject.getDouble("score");
                long rank = jsonObject.getLong("rank");
                BigDecimal amount = BigDecimal.valueOf(score).multiply(activity.getOnePointMoney());
                BigDecimal rewardAmount = getMoney(rank, amount);
                jsonObject.put("rewardAmount", rewardAmount);
            }
        } else if (activity.getMoneyRule() == 2) {
            //总共多少钱 大家平分
            for (JSONObject jsonObject : list) {
                BigDecimal allMoney = activity.getAllMoney();
                Double score = jsonObject.getDouble("score");
                BigDecimal myMoney = allMoney.multiply(BigDecimal.valueOf(score / allScore));
                jsonObject.put("rewardAmount", myMoney);
            }
        }
        String lastActiveKey = RedisKeyConstant.POINT_RANK_LIST_LAST;
        set(lastActiveKey, list, 86400 * 7);
        return list;
    }

    public List<JSONObject> getLastActiveTopList2(Activity activity) {
        String key = RedisKeyConstant.APP_TOP_lIST_2 + activity.getId();
        Map<String, Double> thisTopList = getActiveThisTopList(key, 1);
        Set<String> ids = thisTopList.keySet();
        List<JSONObject> list = new ArrayList<>();
        double allScore = 0.0;
        for (String id : ids) {
            Double score = thisTopList.get(id);
            long rank = getTopRankByKey(key, id) + 1;
            User userInfoById = userCacheService.getUserInfoById(id);
            JSONObject info = new JSONObject();
            info.put("userHeadImg", userInfoById.getHeadImageUrl());
            info.put("userId", id);
            info.put("userName", userInfoById.getName());
            info.put("userNo", userInfoById.getUserNo());
            info.put("score", score);
            info.put("rank", rank);
            list.add(info);
            allScore += score;
        }
        //通过判断奖励规则 来计算金额
        if (activity.getMoneyRule() == 1) {
            //1积分多少钱
            for (JSONObject jsonObject : list) {
                Double score = jsonObject.getDouble("score");
                long rank = jsonObject.getLong("rank");
                BigDecimal amount = BigDecimal.valueOf(score).multiply(activity.getOnePointMoney());
                BigDecimal rewardAmount = getMoney(rank, amount);
                jsonObject.put("rewardAmount", rewardAmount);
            }
        } else if (activity.getMoneyRule() == 2) {
            //总共多少钱 大家平分
            for (JSONObject jsonObject : list) {
                BigDecimal allMoney = activity.getAllMoney();
                Double score = jsonObject.getDouble("score");
                BigDecimal myMoney = allMoney.multiply(BigDecimal.valueOf(score / allScore));
                jsonObject.put("rewardAmount", myMoney);
            }
        }
        String lastActiveKey = RedisKeyConstant.POINT_RANK_LIST_LAST_2;
        set(lastActiveKey, list, 86400 * 7);
        return list;
    }

    public List<JSONObject> getTopList(String pointKey, String rankKey,int minScore) {
        List<JSONObject> list = getList(rankKey, JSONObject.class);
        if (list == null || list.size() == 0) {
            Map<String, Double> thisTopList = getActiveThisTopList(pointKey, minScore);
            Set<String> ids = thisTopList.keySet();
            list = new ArrayList<>();
            double allScore = 0.0;
            for (String id : ids) {
                Double score = thisTopList.get(id);
                //long rank = getTopRankByKey(pointKey, id) + 1;
                User userInfoById = userCacheService.getUserInfoById(id);
                JSONObject info = new JSONObject();
                info.put("userHeadImg", userInfoById.getHeadImageUrl());
                info.put("userId", id);
                info.put("userName", userInfoById.getName());
                info.put("userNo", userInfoById.getUserNo());
                BigDecimal bd = new BigDecimal(score).setScale(2, RoundingMode.HALF_UP);
                info.put("score", bd.doubleValue());
                UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(Long.valueOf(id), UserCapitalTypeEnum.currency_2.getValue());
                info.put("money",userCapital.getBalance());
                //info.put("rank",rank);
                list.add(info);
                allScore += score;
            }
            //通过判断奖励规则 来计算金额
            Activity activity;
            if (minScore==3){
                 activity = getActivity();
            } else {
                activity = getActivity2();
            }

            if (activity.getMoneyRule()==1){
                //1积分多少钱
                for (JSONObject jsonObject : list) {
                    Double score = jsonObject.getDouble("score");
                    long rank = jsonObject.getLong("rank");
                    BigDecimal amount = BigDecimal.valueOf(score).multiply(activity.getOnePointMoney());
                    BigDecimal rewardAmount  = getMoney(rank, amount);
                    jsonObject.put("rewardAmount", rewardAmount);
                }
            } else if (activity.getMoneyRule()==2) {
                //总共多少钱 大家平分
                for (JSONObject jsonObject : list) {
                    BigDecimal allMoney = activity.getAllMoney();
                    Double score = jsonObject.getDouble("score");
                    BigDecimal myMoney =  allMoney.multiply(BigDecimal.valueOf(score/allScore));
                    jsonObject.put("rewardAmount", myMoney);
                }
            }
            set(rankKey, list, 60);
        }
        return list;
    }

    public BigDecimal getRankMoney(Long userId,Double score, Long rank,List<JSONObject> list) {
        if (getActivity().getMoneyRule()==1){
            BigDecimal amount = BigDecimal.valueOf(score).multiply(getActivity().getOnePointMoney());
            return getMoney(rank, amount);
        }else {
            Double allScore = 0.0;
            Double myScore = 0.0;
            for (JSONObject jsonObject : list) {
                allScore+=jsonObject.getDouble("score");
                if (jsonObject.getString("userId").equals(userId.toString())){
                    myScore = jsonObject.getDouble("score");
                }
            }
            return getActivity().getAllMoney().multiply(BigDecimal.valueOf(myScore/allScore));
        }

    }

    public BigDecimal getRankMoney2(Long userId,Double score, Long rank,List<JSONObject> list) {
        if (getActivity2().getMoneyRule()==1){
            BigDecimal amount = BigDecimal.valueOf(score).multiply(getActivity2().getOnePointMoney());
            return getMoney(rank, amount);
        }else {
            Double allScore = 0.0;
            Double myScore = 0.0;
            for (JSONObject jsonObject : list) {
                allScore+=jsonObject.getDouble("score");
                if (jsonObject.getString("userId").equals(userId.toString())){
                    myScore = jsonObject.getDouble("score");
                }
            }
            return getActivity2().getAllMoney().multiply(BigDecimal.valueOf(myScore/allScore));
        }

    }

    public List<JSONObject> getLastTopList() {
        String rankKey = RedisKeyConstant.POINT_RANK_LIST_LAST;
        List<JSONObject> list = getList(rankKey, JSONObject.class);
        if (list == null || list.size() == 0) {
            Activity activityByTime = activityService.findActivityByTime();
            if (activityByTime != null) {
                Activity byId = activityService.findById(activityByTime.getId() - 1);
                if (byId != null) {
                    list = getLastActiveTopList(byId);
                }
            }
        }
        return list;
    }

    public List<JSONObject> getLastTopList2() {
        String rankKey = RedisKeyConstant.POINT_RANK_LIST_LAST_2;
        List<JSONObject> list = getList(rankKey, JSONObject.class);
        if (list == null || list.size() == 0) {
            Activity activityByTime = activity2Service.findActivity2ByTime();
            if (activityByTime != null) {
                Activity byId = activity2Service.findById(activityByTime.getId() - 1);
                if (byId != null) {
                    list = getLastActiveTopList2(byId);
                }
            }
        }
        return list;
    }

    public BigDecimal getMoney(long rank, BigDecimal amount) {
        if (1 == rank) {
            amount = amount.add(BigDecimal.valueOf(1000));
        } else if (2 == rank) {
            amount = amount.add(BigDecimal.valueOf(500));
        } else if (3 == rank) {
            amount = amount.add(BigDecimal.valueOf(300));
        } else if (4 == rank) {
            amount = amount.add(BigDecimal.valueOf(200));
        } else if (5 == rank) {
            amount = amount.add(BigDecimal.valueOf(100));
        }
        return amount;
    }

    private Long getLastTopRank(String key, String id) {
        return getZsetRank(id, key);
    }

    private Map<String, Double> getThisTopList(String key, int count) {
        Set<ZSetOperations.TypedTuple<String>> zset = getZset(key, count);
        Map<String, Double> map = new HashMap<>();
        for (ZSetOperations.TypedTuple<String> s : zset) {
            Double score = s.getScore();
            map.put(s.getValue(), score);
        }
        return map;
    }

    private Map<String, Double> getActiveThisTopList(String key, double minScore) {
        Set<ZSetOperations.TypedTuple<String>> zset = getZset(key, minScore);
        Map<String, Double> map = new HashMap<>();
        for (ZSetOperations.TypedTuple<String> s : zset) {
            Double score = s.getScore();
            map.put(s.getValue(), score);
        }
        return map;
    }

    public List<JSONObject> getThisWeekListDts() {
        String key = RedisKeyConstant.GAME_RANK_LIST_DTS;
        List<JSONObject> list = getList(key, JSONObject.class);
        if (list == null || list.size() == 0) {
            Map<String, Double> lastWeekTopList = getThisWeekTopList(GameTypeEnum.dts2.getValue(), 10);
            Set<String> ids = lastWeekTopList.keySet();
            list = new ArrayList<>();
            for (String id : ids) {
                User userInfoById = userCacheService.getUserInfoById(id);
                if (userInfoById==null){
                    continue;
                }
                JSONObject info = new JSONObject();
                info.put("userHeadImg", userInfoById.getHeadImageUrl());
                info.put("userId", id);
                info.put("userName", userInfoById.getName());
                info.put("userNo", userInfoById.getUserNo());
                info.put("score", lastWeekTopList.get(id));
                list.add(info);
            }
            set(key, list, 60);
        }
        safeSortByScoreDesc(list);
        return list;
    }

    public static void sortByScoreDesc(List<JSONObject> list) {
        Collections.sort(list, (o1, o2) -> {
            double score1 = o1.getDoubleValue("score");
            double score2 = o2.getDoubleValue("score");
            return Double.compare(score2, score1); // 降序
        });
    }

    // 安全版本（处理字段不存在情况）
    public static void safeSortByScoreDesc(List<JSONObject> list) {
        Collections.sort(list, (o1, o2) -> {
            double score1 = o1.getDoubleValue("score"); // 默认值0
            double score2 = o2.getDoubleValue("score");
            return Double.compare(score2, score1);
        });
    }

    public List<JSONObject> getThisWeekListLhd() {
        String key = RedisKeyConstant.GAME_RANK_LIST_LHD;
        List<JSONObject> list = getList(key, JSONObject.class);
        if (list == null || list.size() == 0) {
            Map<String, Double> lastWeekTopList = getThisWeekTopList(GameTypeEnum.nh.getValue(), 10);
            Set<String> ids = lastWeekTopList.keySet();
            list = new ArrayList<>();
            for (String id : ids) {
                User userInfoById = userCacheService.getUserInfoById(id);
                if (userInfoById==null){
                    continue;
                }
                JSONObject info = new JSONObject();
                info.put("userHeadImg", userInfoById.getHeadImageUrl());
                info.put("userId", id);
                info.put("userName", userInfoById.getName());
                info.put("userNo", userInfoById.getUserNo());
                info.put("score", lastWeekTopList.get(id));
                list.add(info);
            }
            set(key, list, 60);
        }
        safeSortByScoreDesc(list);
        return list;
    }

    public List<JSONObject> getThisWeekListDgs() {
        String key = RedisKeyConstant.GAME_RANK_LIST_DGS;
        List<JSONObject> list = getList(key, JSONObject.class);
        if (list == null || list.size() == 0) {
            Map<String, Double> lastWeekTopList = getThisWeekTopList(GameTypeEnum.dgs.getValue(), 10);
            Set<String> ids = lastWeekTopList.keySet();
            list = new ArrayList<>();
            for (String id : ids) {
                User userInfoById = userCacheService.getUserInfoById(id);
                if (userInfoById==null){
                    continue;
                }
                JSONObject info = new JSONObject();
                info.put("userHeadImg", userInfoById.getHeadImageUrl());
                info.put("userId", id);
                info.put("userName", userInfoById.getName());
                info.put("userNo", userInfoById.getUserNo());
                info.put("score", lastWeekTopList.get(id));
                list.add(info);
            }
            set(key, list, 60);
        }
        safeSortByScoreDesc(list);
        return list;
    }

    public List<JSONObject> getKillThisDayList() {
        String key = RedisKeyConstant.KILL_RANK_LIST;
        List<JSONObject> list = getList(key, JSONObject.class);
        if (list == null || list.size() == 0) {
            Map<String, Double> lastWeekTopList = getKillThisDayTopList(50);
            Set<String> ids = lastWeekTopList.keySet();
            list = new ArrayList<>();
            for (String id : ids) {
                User userInfoById = userCacheService.getUserInfoById(id);
                JSONObject info = new JSONObject();
                info.put("userHeadImg", userInfoById.getHeadImageUrl());
                info.put("userId", id);
                info.put("userName", userInfoById.getName());
                info.put("userNo", userInfoById.getUserNo());
                info.put("score", lastWeekTopList.get(id));
                list.add(info);
            }
            set(key, list, 60 * 10);
        }
        return list;
    }

    public Long getThisWeekUserRank(String userId) {
        return getZsetRank(userId, RedisKeyConstant.GAME_RANK_DTS + DateUtil.getFirstDayOfWeek(new Date()));
    }

    public Long getTopRank(String userId) {
        return getZsetRank(userId, RedisKeyConstant.APP_TOP_lIST + DateUtil.format2(new Date()));
    }

    public Long getTopRankByKey(String key, String userId) {
        return getZsetRank(userId, key);
    }

    public Long getLastWeekUserRank(String userId) {
        return getZsetRank(userId, RedisKeyConstant.POINT_RANK_LIST + DateUtil.getFirstDayOfLastWeek());
    }

    public Long getThisWeekUserRankDts(String userId){
        return getZsetRank(userId,RedisKeyConstant.GAME_RANK_DTS+ DateUtil.getFirstDayOfWeek(new Date()));
    }

    public Long getLastWeekUserRankDts(String userId){
        return getZsetRank(userId,RedisKeyConstant.GAME_RANK_DTS+DateUtil.getFirstDayOfLastWeek());
    }

    public Long getThisWeekUserRankLhd(String userId){
        String key = RedisKeyConstant.GAME_RANK_NH+ DateUtil.getFirstDayOfWeek(new Date());
        return getZsetRank(userId,key);
    } public Long getThisWeekUserRankDgs(String userId){
        String key = RedisKeyConstant.GAME_LAST_WEEK_TOP_LIST_DGS+ DateUtil.getFirstDayOfWeek(new Date());
        return getZsetRank(userId,key);
    }

    public Long getLastWeekUserRankLhd(String userId){
        return getZsetRank(userId,RedisKeyConstant.GAME_RANK_NH+DateUtil.getFirstDayOfLastWeek());
    }

    public Long getLastWeekUserRankDgs(String userId){
        return getZsetRank(userId,RedisKeyConstant.GAME_LAST_WEEK_TOP_LIST_DGS+DateUtil.getFirstDayOfLastWeek());
    }



    public Double getUserTopScore(String userId,Long activeId) {
        String key = RedisKeyConstant.APP_TOP_lIST + activeId;
        return getZsetScore(key, userId);
    }

    public Double getUserTopScore2(String userId,Long activeId) {
        String key = RedisKeyConstant.APP_TOP_lIST_2 + activeId;
        return getZsetScore(key, userId);
    }

    public Double getLastUserTopScore(String userId, String key) {
        return getZsetScore(key, userId);
    }

    public Activity getActivity() {
        if (activity == null || activity.getEndTime().getTime() < System.currentTimeMillis()) {
            activity = activityService.findActivityByTime();
        }
        return activity;
    }

    public Activity getActivity2() {
        if (activity2 == null || activity2.getEndTime().getTime() < System.currentTimeMillis()) {
            activity2 = activity2Service.findActivity2ByTime();
        }
        return activity2;
    }

    public void addPoint(Long userId) {
        User user = userCacheService.getUserInfoById(userId);
        getActivity();
        String key = RedisKeyConstant.APP_TOP_lIST + activity.getId();
        //存之前要先判断父级id的积分是否大于10 大于10 加1.05分 如果大于20.5的话加1.1分
        //存放zset格式为了得出排名
        if (user.getParentId() != null) {
            Double oldPoint = getZsetScore(key, String.valueOf(user.getParentId()));
            Double point = 0.0;
            if (oldPoint == null) {
                oldPoint = 0.0;
            }
            if (oldPoint < 10) {
                point = oldPoint + 1;
            } else if (oldPoint >= 10 && oldPoint < 30.05) {
                point = oldPoint + 1.05;
            } else if (oldPoint > 30) {
                point = oldPoint + 1.1;
            }
            addForZset(key, String.valueOf(user.getParentId()), point);
        }
    }

    public void addPoint2(Long userId) {
        User user = userCacheService.getUserInfoById(userId);
        getActivity2();
        String key = RedisKeyConstant.APP_TOP_lIST_2 + activity2.getId();
        //存之前要先判断父级id的积分是否大于10 大于10 加1.05分 如果大于20.5的话加1.1分
        //存放zset格式为了得出排名
        if (user.getGrandfaId() != null) {
            Double oldPoint = getZsetScore(key, String.valueOf(user.getGrandfaId()));
            Double point = 0.0;
            if (oldPoint == null) {
                oldPoint = 0.0;
            }
            point = oldPoint + 0.2;
            addForZset(key, String.valueOf(user.getGrandfaId()), point);
        }
    }
}

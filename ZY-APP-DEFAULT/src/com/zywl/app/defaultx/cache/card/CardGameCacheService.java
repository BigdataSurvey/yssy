package com.zywl.app.defaultx.cache.card;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.DailyTask;
import com.zywl.app.base.bean.User;
import com.zywl.app.base.bean.UserDailyTask;
import com.zywl.app.base.bean.vo.UserDailyTaskVo;
import com.zywl.app.base.constant.KafkaEventContext;
import com.zywl.app.base.constant.RedisKeyConstant;
import com.zywl.app.base.util.BeanUtils;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.cache.impl.RedisService;
import com.zywl.app.defaultx.service.DailyTaskService;
import com.zywl.app.defaultx.service.UserDailyTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
/**
 *
 * **/
@Service
public class CardGameCacheService extends RedisService {
    @Autowired
    private UserDailyTaskService userDailyTaskService;

    @Autowired
    private DailyTaskService dailyTaskService;

    @Autowired
    private UserCacheService userCacheService;

    public static final List<String> LAST_WEEK_USER_IDS = new ArrayList<>();

    public long getAfkMoneyCount(Long userId) {
        String key = RedisKeyConstant.APP_USER_AFK_COUNT + DateUtil.format2(new Date()) + ":" + userId + "-";
        String count = get(key);
        if (count == null) {
            return 0l;
        }
        return Long.parseLong(count);
    }

    public void addAfkMoneyCount(Long userId) {
        String key = RedisKeyConstant.APP_USER_AFK_COUNT + DateUtil.format2(new Date()) + ":" + userId + "-";
        incr(key, 1L);
        expire(key, 86400L * 7);
    }

    public List<Long> getDispatchCardIdList(String userId) {
        String key = RedisKeyConstant.APP_USER_DISPATCH_CARD_LIST + userId + "-";
        List<Long> list = getList(key, Long.class);
        return list;
    }

    public void addDispatchCardIdList(String userId, JSONArray array) {
        String key = RedisKeyConstant.APP_USER_DISPATCH_CARD_LIST + userId + "-";
        List<Long> list = new ArrayList<>();
        for (Object o : array) {
            Long id = Long.parseLong(o.toString());
            list.add(id);
        }
        List<Long> dispatchCardIdList = getDispatchCardIdList(userId);
        if (dispatchCardIdList == null || dispatchCardIdList.size() == 0) {
            //没有武将正在派遣
            set(key, list, 86400L * 7);
        } else {
            dispatchCardIdList.addAll(list);
            set(key, dispatchCardIdList, 86400 * 7);
        }
    }

    public void userReceiveDispatch(String userId, JSONArray array) {
        String key = RedisKeyConstant.APP_USER_DISPATCH_CARD_LIST + userId + "-";
        List<Long> list = getList(key, Long.class);
        String cardId1 = String.valueOf(array.get(0));
        String cardId2 = String.valueOf(array.get(1));
        if (list != null) {
            Iterator<Long> iterator = list.iterator();
            while (iterator.hasNext()) {
                Long next = iterator.next();
                if (next.toString().equals(cardId1) || next.toString().equals(cardId2)) {
                    iterator.remove();
                }
            }
            set(key, list, 86400 * 7);
        }
    }


    public Long getUserSweepCount(Long userId, int sweepType) {
        String key = RedisKeyConstant.APP_PLAYER_SWEEP_FREE + DateUtil.format2(new Date()) + ":" + userId;
        Object hget = hget(key, String.valueOf(sweepType));
        if (hget != null) {
            return Long.parseLong(hget.toString());
        }
        return 0L;
    }

    public Long getUserMoneySweepCount(Long userId, int sweepType) {
        String key = RedisKeyConstant.APP_PLAYER_SWEEP_MONEY + DateUtil.format2(new Date()) + ":" + userId;
        Object hget = hget(key, String.valueOf(sweepType));
        if (hget != null) {
            return Long.parseLong(hget.toString());
        }
        return 0L;
    }

    public void addUserMoneySweepCount(Long userId, int sweepType) {
        String key = RedisKeyConstant.APP_PLAYER_SWEEP_MONEY + DateUtil.format2(new Date()) + ":" + userId;
        Object hget = hget(key, String.valueOf(sweepType));
        hset(key, String.valueOf(sweepType), hget != null ? Integer.parseInt(hget.toString()) + 1 : 1, 86400L);
    }

    public void addUserSweepCount(Long userId, int sweepType) {
        String key = RedisKeyConstant.APP_PLAYER_SWEEP_FREE + DateUtil.format2(new Date()) + ":" + userId;
        Object hget = hget(key, String.valueOf(sweepType));
        hset(key, String.valueOf(sweepType), hget != null ? Integer.parseInt(hget.toString()) + 1 : 1, 86400L);
    }


    public Map getUserTask(Long userId) {
        String key = RedisKeyConstant.APP_USER_DAILY_TASK + DateUtil.format2(new Date()) + ":" + userId;
        Map hmget = hmget(key);
        if (hmget == null || hmget.size() == 0) {
            JSONArray taskLists = getDailyTask();
            UserDailyTask todayByUserId = userDailyTaskService.findTodayByUserId(userId);
            if (todayByUserId != null) {
                taskLists = todayByUserId.getTaskList();
            } else {
                userDailyTaskService.addUserDailyTask(userId, taskLists);
            }
            hmget = new HashMap<>();
            for (Object taskList : taskLists) {
                UserDailyTaskVo vo;
                if (taskList instanceof JSONObject) {
                    JSONObject jsonObject = (JSONObject) taskList;
                    vo = jsonObject.toJavaObject(UserDailyTaskVo.class);
                } else {
                    vo = (UserDailyTaskVo) taskList;
                }

                hmget.put(vo.getId().toString(), vo);
            }
            hmset(key, hmget, 86400);
        }
        return hmget;

    }

    public UserDailyTaskVo getUserTaskById(Long userId, String taskId) {
        String key = RedisKeyConstant.APP_USER_DAILY_TASK + DateUtil.format2(new Date()) + ":" + userId;
        Object hget = hget(key, taskId);
        if (hget != null) {
            return (UserDailyTaskVo) hget;
        }
        return null;
    }

    public JSONArray getDailyTask() {
        String key = RedisKeyConstant.APP_DAILY_TASK;
        Map hmget = hmget(key);
        if (hmget == null || hmget.size() == 0) {
            hmget = new HashMap();
            List<DailyTask> list = dailyTaskService.findAllDailyTask();
            for (DailyTask dailyTask : list) {
                hmget.put(dailyTask.getId().toString(), dailyTask);
            }
            hmset(key, hmget, 86400 * 7);
        }

        JSONArray taskList = new JSONArray();
        for (Object taskId : hmget.keySet()) {
            String id = taskId.toString();
            DailyTask dt = (DailyTask) hmget.get(id);

            UserDailyTaskVo vo = new UserDailyTaskVo();
            BeanUtils.copy(dt, vo);

            int cond = 0;
            try {
                cond = Integer.parseInt(String.valueOf(dt.getCondition()));
            } catch (Exception ignored) {}

            if (cond <= 0) {
                vo.setCondition(1);
                vo.setSchedule(1);
                // 1=可领取
                vo.setStatus(1);
            } else {
                vo.setCondition(cond);
                vo.setSchedule(0);
                vo.setStatus(0);
            }

            taskList.add(vo);
        }
        return taskList;
    }

    public Map getUserRedPointInfo(Long userId) {
        String key = RedisKeyConstant.APP_USER_RED_POINT + userId;
        Map<String, Object> hmget = hmget(key);
        return hmget;
    }

    public Object getUserRedPointInfo(Long userId, String item) {
        String key = RedisKeyConstant.APP_USER_RED_POINT + userId;
        Object hmget = hget(key, item);
        return hmget;
    }

    public void setUserRedPointInfo(Long userId, String item, Object value) {
        String key = RedisKeyConstant.APP_USER_RED_POINT + userId;
        Long time = 86400 * 5L;
        if (item.equals(KafkaEventContext.DAILY_TASK)) {
            time = DateUtil.getTomorrowBegin() - System.currentTimeMillis();
        }
        hset(key, item, value, time);
    }

    public void removeUserRedPointInfo(Long userId, String item) {
        String key = RedisKeyConstant.APP_USER_RED_POINT + userId;
        hdel(key, item);
    }


    public void updateUserDailyTaskStatus(Long userId, String item, Object value) {
        String key = RedisKeyConstant.APP_USER_DAILY_TASK + DateUtil.format2(new Date()) + ":" + userId;
        hset(key, item, value);
    }

    public Long getUserDtAp(Long userId) {
        String key = RedisKeyConstant.APP_USER_DAILY_TASK_AP_NUMBER + DateUtil.format2(new Date()) + ":" + userId;
        String a = get(key);
        if (a == null) {
            incr(key, 0L);
            expire(key, 86400);
            return 0L;
        }
        return Long.parseLong(a);
    }

    public Long getUserTodaySign(Long userId) {
        String key = RedisKeyConstant.APP_USER_DAILY_TASK_SIGN + DateUtil.format2(new Date()) + ":" + userId;
        String a = get(key);
        if (a == null) {
            incr(key, 0L);
            expire(key, 86400);
            return 0L;
        }
        return Long.parseLong(a);
    }

    public void userSign(Long userId) {
        String key = RedisKeyConstant.APP_USER_DAILY_TASK_SIGN + DateUtil.format2(new Date()) + ":" + userId;
        set(key,1);
        expire(key, 86400);

        // 本周签到进度（周一为第一天）。
        String weekKey = RedisKeyConstant.APP_USER_DAILY_TASK_WEEK_SIGN + DateUtil.getFirstDayOfWeek(new Date()) + ":" + userId;
        long mask = 0L;
        String cur = get(weekKey);
        if (cur != null) {
            try {
                mask = Long.parseLong(cur);
            } catch (Exception ignored) {
                mask = 0L;
            }
        }

        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.setFirstDayOfWeek(java.util.Calendar.MONDAY);
        // 1=周日,2=周一...
        int dow = calendar.get(java.util.Calendar.DAY_OF_WEEK);
        // 周一=0 ... 周日=6
        int idx = (dow + 5) % 7;
        mask = mask | (1L << idx);
        set(weekKey, String.valueOf(mask));

        // 过期时间设置到下周一（含缓冲），确保每周自动重置。
        String weekStart = DateUtil.getFirstDayOfWeek(new Date());
        java.util.Date weekStartDate = DateUtil.parseDate(weekStart, "yyyy-MM-dd");
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(weekStartDate);
        cal.add(java.util.Calendar.DATE, 7);
        long ttl = (cal.getTimeInMillis() - System.currentTimeMillis()) / 1000L;
        if (ttl < 1) {
            ttl = 86400L;
        }
        // 额外 1 小时 buffer，避免边界时刻抖动
        expire(weekKey, ttl + 3600L);
    }

    /**
     * 获取本周签到掩码（周一为第一天）。
     */
    public long getUserWeekSignMask(Long userId) {
        String weekKey = RedisKeyConstant.APP_USER_DAILY_TASK_WEEK_SIGN + DateUtil.getFirstDayOfWeek(new Date()) + ":" + userId;
        String cur = get(weekKey);
        if (cur == null) {
            set(weekKey, "0");
            expire(weekKey, 86400L * 9);
            return 0L;
        }
        try {
            return Long.parseLong(cur);
        } catch (Exception e) {
            return 0L;
        }
    }

    /**
     * 本周已签到天数（包含今天，如果已签）。
     */
    public int getUserWeekSignedDays(Long userId) {
        long mask = getUserWeekSignMask(userId);
        return Long.bitCount(mask);
    }

    /**
     * 签到进度 = 本周已签到天数 +（今日未签 ? 1 : 0）。
     * 取值范围 1~7。
     */
    public int getUserDayNowByWeekSign(Long userId) {
        int signedDays = getUserWeekSignedDays(userId);
        long todaySign = getUserTodaySign(userId);
        int dayNow = signedDays + (todaySign == 1L ? 0 : 1);
        if (dayNow < 1) {
            dayNow = 1;
        }
        if (dayNow > 7) {
            dayNow = 7;
        }
        return dayNow;
    }


    public void updateUserDtApInfo(Long userId, String item, Object object) {
        String key = RedisKeyConstant.APP_USER_DAILY_TASK_AP + DateUtil.format2(new Date()) + ":" + userId;
        hset(key, item, object);
    }

    public String getUserDtApStatus(Long userId, String item) {
        String key = RedisKeyConstant.APP_USER_DAILY_TASK_AP + DateUtil.format2(new Date()) + ":" + userId;
        Object hget = hget(key, item);
        if (hget != null) {
            return hget.toString();
        }
        return null;
    }



    public void updateUserDafuwengIndex(Long userId, int type, int index) {
        String key = RedisKeyConstant.APP_PLAYER_DAFUWENG + userId;
        hset(key, String.valueOf(type), index, 6000L);
    }


    public void removeSkillCache() {
        String key = RedisKeyConstant.APP_GAME_SKILL;
        del(key);
    }

    public void removeArtifactCache() {
        String key = RedisKeyConstant.APP_GAME_ARTIFACT;
        del(key);
    }


    public Long getUserTodayDispatchNumber(Long userId) {
        String key = RedisKeyConstant.APP_USER_DISPATCH_NUMBER + DateUtil.format2(new Date()) + ":" + userId;
        Object o = get(key);
        if (o != null) {
            Long nowNumber = Long.parseLong(o.toString());
            return nowNumber;
        }
        return 0L;
    }


    //-------------------贺卡排行榜---------------------
    public List<JSONObject> getLastWeekList() {
        String key = RedisKeyConstant.GAME_LAST_WEEK_TOP_LIST + DateUtil.getFirstDayOfWeek(new Date());
        List<JSONObject> array = getList(key, JSONObject.class);
        if (array == null || array.size() == 0) {
            Map<String, Double> lastWeekTopList = getLastWeekTopList(50);
            Set<String> ids = lastWeekTopList.keySet();
            array = new ArrayList<>();
            for (String id : ids) {
                User userInfoById = userCacheService.getUserInfoById(id);
                if (userInfoById != null) {
                    JSONObject info = new JSONObject();
                    info.put("userHeadImg", userInfoById.getHeadImageUrl());
                    info.put("userId", id);
                    info.put("userName", userInfoById.getName());
                    info.put("userNo", userInfoById.getUserNo());
                    info.put("popularity", lastWeekTopList.get(id));
                    info.put("roleId", userInfoById.getRoleId());
                    array.add(info);
                }
            }
            set(key, array, 86400 * 10);
        }
        array.sort(Comparator.comparingDouble(obj -> obj.getIntValue("popularity")));
        Collections.reverse(array);
        return array;
    }

    public Map<String, Double> getThisWeekTopList(int count) {
        String key = getRankKey();
        Set<ZSetOperations.TypedTuple<String>> zset = getZset(key, count);
        Map<String, Double> map = new HashMap<>();
        for (ZSetOperations.TypedTuple<String> s : zset) {
            Double score = s.getScore();
            map.put(s.getValue(), score);
        }
        return map;
    }


    public String getLastWeekRankKey() {
        String key = RedisKeyConstant.GAME_RANK_GREETING_CARD + DateUtil.getFirstDayOfLastWeek();
        ;
        return key;
    }

    public String getRankKey() {
        String key = RedisKeyConstant.GAME_RANK_GREETING_CARD + DateUtil.getFirstDayOfWeek(new Date());
        return key;
    }

    public Map<String, Double> getLastWeekTopList(int count) {
        String key = getLastWeekRankKey();
        Set<ZSetOperations.TypedTuple<String>> zset = getZset(key, count);
        Map<String, Double> map = new HashMap<>();
        for (ZSetOperations.TypedTuple<String> s : zset) {
            map.put(s.getValue(), s.getScore());
        }
        return map;
    }


    public BigDecimal getYesterdayPrizePool() {
        String key = RedisKeyConstant.PRIZE_POOL + DateUtil.format2(DateUtil.getDateByDay(-1));
        String amount = get(key);
        if (amount != null) {
            return new BigDecimal(amount);
        }
        return BigDecimal.ZERO;
    }

    public void addTodayPrizePool(BigDecimal amount) {
        String key = RedisKeyConstant.PRIZE_POOL + DateUtil.format2(new Date());
        incr(key, Long.parseLong(amount.toString()));
    }

    public BigDecimal getNowPrizePoolInfo() {
        String key = RedisKeyConstant.PRIZE_POOL + DateUtil.format2(new Date());
        String amount = get(key);
        if (amount == null) {
            incr(key, 0L);
            expire(key, 86400 * 2);
            return BigDecimal.ZERO;
        }
        return new BigDecimal(amount);
    }

}

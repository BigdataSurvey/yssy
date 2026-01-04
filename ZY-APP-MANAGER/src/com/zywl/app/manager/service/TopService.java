package com.zywl.app.manager.service;

import com.alibaba.fastjson2.JSONArray;
import com.zywl.app.base.bean.User;
import com.zywl.app.base.bean.UserPower;
import com.zywl.app.base.bean.card.UserCheckpointTopVo;
import com.zywl.app.base.bean.vo.*;
import com.zywl.app.base.bean.vo.card.UserTowerVo;
import com.zywl.app.base.constant.RedisKeyConstant;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.cache.impl.RedisService;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.defaultx.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.data.redis.core.ZSetOperations.TypedTuple;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

//排行榜
@Service
public class TopService extends BaseService {
    //人气榜
    public static JSONArray POPULAR_TOP = new JSONArray();
    //仙晶排行榜
    public static JSONArray PVP = new JSONArray();
    //试炼之塔排行榜
    public static JSONArray TOWER_TOP = new JSONArray();

    public static JSONArray TOP_4 = new JSONArray();

    public static List<UserPower> userPowers = new ArrayList<>();

    public static JSONArray TOP_5 = new JSONArray();
    //仙门排行榜
    public static JSONArray TOP_6 = new JSONArray();

    public static JSONArray TOP_FRIEND = new JSONArray();

    public static JSONArray TOP_VIP = new JSONArray();


    @Autowired
    private UserStatisticService userStatisticService;

    @Autowired
    private UserPowerService userPowerService;


    @Autowired
    private UserCapitalService userCapitalService;


    @Autowired
    private UserService userService;


    @Autowired
    private UserVipService userVipService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private UserCacheService userCacheService;


    public void updateTop1Info() {
        long time = System.currentTimeMillis();
        TOP_5.clear();
        List<OneJuniorNumTopVo> toByJuniorNum = userStatisticService.findToByJuniorNum();
        for (OneJuniorNumTopVo oneJuniorNumTopVo : toByJuniorNum) {
            oneJuniorNumTopVo.setNum(oneJuniorNumTopVo.getNum()+oneJuniorNumTopVo.getNum2());
        }
        toByJuniorNum.sort(((o1,o2) -> {
            //从小到大
            return o2.getNum() - o1.getNum();//此处定义比较规则，o2.age-o1.age即为从大到小
        }));
        TOP_5.addAll(toByJuniorNum);
        logger.info("更新好友排行榜--用时：" + (System.currentTimeMillis() - time));



        time = System.currentTimeMillis();
        TOP_VIP.clear();
        List<VipTopVo> vipTopVos = userVipService.findTopByVip();
        TOP_VIP.addAll(vipTopVos);
        logger.info("更新vip榜--用时：" + (System.currentTimeMillis() - time));
    }

    // ============================ 消耗榜（Redis ZSet）============================
    private static final int CONSUME_TOP_LIMIT = 30;

    /**
     * 获取用户资产消耗排行榜（可选按资产类型）。
     *
     * Redis ZSet：member=userId，score=累计消耗(分)。
     */
    public JSONArray getConsumeTopList(Integer capitalType) {
        JSONArray result = new JSONArray();
        String key = getConsumeKey(capitalType);
        Set<TypedTuple<String>> tuples = redisService.getZset(key, 0, CONSUME_TOP_LIMIT - 1);
        if (tuples == null || tuples.isEmpty()) {
            return result;
        }
        for (TypedTuple<String> tuple : tuples) {
            if (tuple == null || tuple.getValue() == null) {
                continue;
            }
            Long uid;
            try {
                uid = Long.parseLong(tuple.getValue());
            } catch (Exception e) {
                continue;
            }
            ConsumeTopVo vo = buildConsumeVo(uid, capitalType, tuple.getScore());
            result.add(vo);
        }
        return result;
    }

    /**
     * 获取我的消耗信息（若无记录则 num=0）。
     */
    public ConsumeTopVo getConsumeMy(Long userId, Integer capitalType) {
        String key = getConsumeKey(capitalType);
        Double score = redisService.getZsetScore(key, String.valueOf(userId));
        return buildConsumeVo(userId, capitalType, score);
    }

    private ConsumeTopVo buildConsumeVo(Long userId, Integer capitalType, Double score) {
        ConsumeTopVo vo = new ConsumeTopVo();
        vo.setUserId(userId);
        vo.setCapitalType(capitalType);
        vo.setNum(scoreToAmount(score));
        User user = userCacheService.getUserInfoById(userId);
        if (user != null) {
            vo.setUserNo(String.valueOf(user.getUserNo()));
            vo.setUserName(user.getName());
            vo.setUserHeadImg(user.getHeadImageUrl());
        }
        return vo;
    }

    private BigDecimal scoreToAmount(Double score) {
        if (score == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        // score 以“分”为单位
        long cents = Math.round(score);
        return BigDecimal.valueOf(cents).movePointLeft(2).setScale(2, RoundingMode.HALF_UP);
    }

    private String getConsumeKey(Integer capitalType) {
        if (capitalType == null) {
            return RedisKeyConstant.APP_TOP_CONSUME_ALL;
        }
        return RedisKeyConstant.APP_TOP_CONSUME_TYPE + capitalType;
    }

}

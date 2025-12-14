package com.zywl.app.manager.service.manager;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.Config;
import com.zywl.app.base.bean.User;
import com.zywl.app.base.bean.UserJoy;
import com.zywl.app.base.bean.UserJoyContrib;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.cache.UserCapitalCacheService;
import com.zywl.app.defaultx.enmus.LogUserBackpackTypeEnum;
import com.zywl.app.defaultx.service.*;
import com.zywl.app.manager.context.MessageCodeContext;
import com.zywl.app.manager.service.PlayGameService;
import com.zywl.app.manager.socket.ManagerSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * @Author: lzx
 * @Create: 2025/12/14
 * @Version: V1.0
 * @Description: 用户气球树服务 USER_JOR = 037
 * @Task:
 */
@Service
@ServiceClass(code = MessageCodeContext.USER_JOR)
public class ManagerJoyService  extends BaseService {

    // 用户缓存服务
    @Autowired
    private UserCacheService userCacheService;

    // 用户资产缓存服务
    @Autowired
    private UserCapitalCacheService userCapitalCacheService;

    // 用户服务
    @Autowired
    private UserService userService;

    // VIP 用户服务
    @Autowired
    private UserVipService userVipService;

    // 游戏核心服务
    @Autowired
    private PlayGameService gameService;

    // 配置中心服务
    @Autowired
    private ManagerConfigService managerConfigService;

    // 用户资产服务
    @Autowired
    private UserCapitalService userCapitalService;

    // 用户欢乐值服务
    @Autowired
    private UserJoyService userJoyService;

    // 好友贡献汇总表服务
    @Autowired
    private UserJoyContribService userJoyContribService;


    /**
     * 001 - 查询我的欢乐值与可兑气球数量
     *
     * 入参：
     * {
     *   "userId": 937223
     * }
     *
     * 返回：
     * {
     *   "userId": 937223,
     *   "totalJoy": 150.5,          // 累计获得欢乐值
     *   "availableJoy": 101.5,      // 当前可用欢乐值（可用于兑换）
     *   "joyExchangeRate": 20,      // 配置：每 20 点欢乐值可兑换 1 组气球
     *   "exchangeableCount": 5      // 当前可兑换的气球组数 = floor(availableJoy / joyExchangeRate)
     * }
     */
    @ServiceMethod(code = "001", description = "查询我的欢乐值与可兑气球数量")
    @Transactional
    public JSONObject getMyJoyInfo(ManagerSocketServer socket, JSONObject data) {
        // 参数校验
        checkNull(data);
        checkNull(data.get("userId"));

        Long userId = data.getLong("userId");

        // 用户存在性校验
        User user = loadAndCheckUser(userId);

        // 查询用户欢乐值记录
        UserJoy userJoy = userJoyService.findByUserId(userId);

        BigDecimal totalJoy = BigDecimal.ZERO;
        BigDecimal availableJoy = BigDecimal.ZERO;
        if (userJoy != null) {
            if (userJoy.getTotalJoy() != null) {
                totalJoy = userJoy.getTotalJoy();
            }
            if (userJoy.getAvailableJoy() != null) {
                availableJoy = userJoy.getAvailableJoy();
            }
        }

        // 欢乐值兑换比例配置（每 joyExchangeRate 点欢乐值兑换 1 组气球）
        Integer rateCfg = managerConfigService.getInteger(Config.JOY_EXCHANGE_RATE);
        int joyExchangeRate = (rateCfg != null && rateCfg > 0) ? rateCfg : 10;

        // 可兑换气球组数 = floor(availableJoy / joyExchangeRate)
        long exchangeableCount = 0L;
        if (joyExchangeRate > 0 && availableJoy.compareTo(BigDecimal.ZERO) > 0) {
            exchangeableCount = availableJoy
                    .divide(new BigDecimal(joyExchangeRate), 0, RoundingMode.FLOOR)
                    .longValue();
        }

        JSONObject result = new JSONObject();
        result.put("userId", userId);
        result.put("totalJoy", totalJoy);
        result.put("availableJoy", availableJoy);
        result.put("joyExchangeRate", joyExchangeRate);
        result.put("exchangeableCount", exchangeableCount);

        return result;
    }

    /**
     * 002 - 领取气球（用当前可用欢乐值一次性兑换成气球道具）
     *
     * 规则：
     *   - 从 t_user_joy.available_joy 中扣减本次用于兑换的欢乐值；
     *   - 扣减公式：usedJoy = floor(availableJoy / joyExchangeRate) * joyExchangeRate；
     *   - 兑换出的气球数量：exchangeCount = floor(availableJoy / joyExchangeRate)；
     *   - totalJoy 不回退，只减少 availableJoy；
     *   - 通过 PlayGameService.addReward 发放气球道具到背包。
     *
     * 入参：
     * {
     *   "userId": 937223,
     *   "balloonItemId": 3001   // 氣球道具在 dic_item 中的 itemId，由前端传入
     * }
     *
     * 返回：
     * {
     *   "userId": 937223,
     *   "usedJoy": 100,             // 本次实际消耗的欢乐值
     *   "remainJoy": 1.5,           // 兑换后剩余的欢乐值
     *   "joyExchangeRate": 20,      // 当前兑换比例
     *   "exchangeCount": 5,         // 本次兑换出的气球数量
     *   "nextExchangeableCount": 0  // 兑换后剩余欢乐值还能兑换的气球数量
     * }
     */
    @ServiceMethod(code = "002", description = "领取气球（欢乐值兑换气球道具）")
    @Transactional
    public JSONObject exchangeJoyToBalloon(ManagerSocketServer socket, JSONObject data) {
        // 参数校验
        checkNull(data);
        checkNull(data.get("userId"));
        checkNull(data.get("balloonItemId"));

        Long userId = data.getLong("userId");
        Integer balloonItemId = data.getInteger("balloonItemId");
        if (balloonItemId == null || balloonItemId <= 0) {
            throwExp("气球道具ID非法");
        }

        // 用户存在性校验
        User user = loadAndCheckUser(userId);

        // 查询用户欢乐值
        UserJoy userJoy = userJoyService.findByUserId(userId);
        if (userJoy == null) {
            throwExp("当前没有可兑换的欢乐值");
        }

        BigDecimal availableJoy = userJoy.getAvailableJoy();
        if (availableJoy == null) {
            availableJoy = BigDecimal.ZERO;
        }
        if (availableJoy.compareTo(BigDecimal.ZERO) <= 0) {
            throwExp("当前没有可兑换的欢乐值");
        }

        int joyExchangeRate = getJoyExchangeRate();
        if (joyExchangeRate <= 0) {
            throwExp("欢乐值兑换比例配置错误，请联系管理员");
        }

        // 计算本次可兑换的气球数量
        BigDecimal[] divAndRemainder = availableJoy.divideAndRemainder(BigDecimal.valueOf(joyExchangeRate));
        BigDecimal exchangeCountDec = divAndRemainder[0].setScale(0, RoundingMode.FLOOR);
        long exchangeCount = exchangeCountDec.longValue();

        if (exchangeCount <= 0L) {
            throwExp("当前可兑换的气球数量为 0");
        }

        // 本次消耗的欢乐值
        BigDecimal usedJoy = BigDecimal.valueOf(joyExchangeRate).multiply(exchangeCountDec);

        // 扣减后剩余欢乐值，保留两位小数向下取整
        BigDecimal remainJoy = availableJoy.subtract(usedJoy);
        if (remainJoy.compareTo(BigDecimal.ZERO) < 0) {
            remainJoy = BigDecimal.ZERO;
        }
        remainJoy = remainJoy.setScale(2, RoundingMode.DOWN);

        // 更新 t_user_joy.available_joy
        userJoy.setAvailableJoy(remainJoy);
        userJoyService.updateByUserId(userJoy);

        // 组装奖励，发放气球道具到背包
        JSONArray rewards = new JSONArray();
        JSONObject reward = new JSONObject();
        reward.put("type", 1);               // 道具
        reward.put("id", balloonItemId);     // 气球道具 itemId
        reward.put("number", exchangeCount); // 数量 = 本次兑换气球数量
        rewards.add(reward);

        // 日志类型这里复用“收获”类型，避免改枚举
        gameService.addReward(userId, rewards, null, LogUserBackpackTypeEnum.harvest);

        // 计算兑换后还能再兑换多少（一般是 0，仅作为前端展示）
        long nextExchangeableCount = 0L;
        if (remainJoy.compareTo(BigDecimal.ZERO) > 0) {
            nextExchangeableCount = remainJoy
                    .divide(BigDecimal.valueOf(joyExchangeRate), 0, RoundingMode.FLOOR)
                    .longValue();
        }

        JSONObject result = new JSONObject();
        result.put("userId", userId);
        result.put("usedJoy", usedJoy);
        result.put("remainJoy", remainJoy);
        result.put("joyExchangeRate", joyExchangeRate);
        result.put("exchangeCount", exchangeCount);
        result.put("nextExchangeableCount", nextExchangeableCount);

        return result;
    }

    /**
     * 003 - 查看某个好友对我的欢乐值贡献
     *
     * 说明：
     *   - receiverUserId = 当前用户（我的 userId）
     *   - fromUserId     = 好友 userId
     *   - 数据来源：t_user_joy_contrib（UserJoyContrib）
     *
     * 入参：
     * {
     *   "userId": 937223,         // 我自己
     *   "friendUserId": 888001    // 好友ID（贡献来源）
     * }
     *
     * 返回：
     * {
     *   "userId": 937223,
     *   "friendUserId": 888001,
     *   "friendNickName": "好友昵称",
     *   "friendAvatar":   "头像URL",
     *   "todayJoy": 12.5,         // 今日该好友对我新增的欢乐值
     *   "totalJoy": 200.8         // 累计该好友对我贡献的欢乐值
     * }
     */
    @ServiceMethod(code = "003", description = "查看某个好友对我的欢乐值贡献")
    @Transactional(readOnly = true)
    public JSONObject getFriendJoyContrib(ManagerSocketServer socket, JSONObject data) {
        // 参数校验
        checkNull(data);
        checkNull(data.get("userId"));
        checkNull(data.get("friendUserId"));

        Long userId = data.getLong("userId");
        Long friendUserId = data.getLong("friendUserId");

        // 校验当前用户存在
        User self = loadAndCheckUser(userId);

        // 校验好友是否存在（走缓存，DB 兜底）
        User friend = userCacheService.getUserInfoById(friendUserId);
        if (friend == null) {
            throwExp("好友不存在");
        }

        // 查询好友对我的贡献汇总
        UserJoyContrib contrib = userJoyContribService.findOneByPair(userId, friendUserId);

        BigDecimal totalJoy = BigDecimal.ZERO;
        BigDecimal todayJoy = BigDecimal.ZERO;
        if (contrib != null) {
            if (contrib.getTotalJoy() != null) {
                totalJoy = contrib.getTotalJoy();
            }
            if (contrib.getTodayJoy() != null) {
                todayJoy = contrib.getTodayJoy();
            }
        }

        JSONObject result = new JSONObject();
        result.put("userId", userId);
        result.put("friendUserId", friendUserId);
        result.put("friendName", friend.getName());
        result.put("friendHeadImage", friend.getHeadImageUrl());
        result.put("todayJoy", todayJoy);
        result.put("totalJoy", totalJoy);

        return result;
    }


    /**
     * 读取欢乐值兑换比例配置
     * JOY_EXCHANGE_RATE：每 joyExchangeRate 点欢乐值可兑换 1 组气球
     * 如果配置缺失或非法，默认 10
     */
    private int getJoyExchangeRate() {
        Integer rateCfg = managerConfigService.getInteger(Config.JOY_EXCHANGE_RATE);
        if (rateCfg == null || rateCfg <= 0) {
            return 10;
        }
        return rateCfg;
    }


    /**
     * 加载并校验用户
     */
    private User loadAndCheckUser(Long userId) {
        Map<Long, User> users = userCacheService.loadUsers(userId);
        User user = (users != null) ? users.get(userId) : null;
        if (user == null) {
            throwExp("用户不存在");
        }
        return user;
    }

}



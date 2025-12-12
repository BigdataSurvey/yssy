package com.zywl.app.manager.service.manager;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.*;
import com.zywl.app.base.bean.card.DicFarm;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.cache.UserCapitalCacheService;
import com.zywl.app.defaultx.enmus.LogCapitalTypeEnum;
import com.zywl.app.defaultx.enmus.LogUserBackpackTypeEnum;
import com.zywl.app.defaultx.service.UserCapitalService;
import com.zywl.app.defaultx.service.UserFarmLandService;
import com.zywl.app.defaultx.service.UserService;
import com.zywl.app.defaultx.service.UserVipService;
import com.zywl.app.defaultx.service.card.DicFarmService;
import com.zywl.app.manager.context.MessageCodeContext;
import com.zywl.app.manager.service.PlayGameService;
import com.zywl.app.manager.socket.ManagerSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: lzx
 * @Create: 2025/12/9
 * @Version: V1.0
 * @Description: 用户种地管理  Manager
 * @Task: 036 (MessageCodeContext.USER_FARM)
 */
@Service
@ServiceClass(code = MessageCodeContext.USER_FARM)
public class ManagerGameFarmService extends BaseService {

    // ===================== 农场状态常量 =====================
    /** 0 = 空地 */
    private static final int LAND_STATUS_EMPTY = 0;
    /** 1 = 成长中 */
    private static final int LAND_STATUS_GROWING = 1;
    /** 2 = 成熟（可收割） */
    private static final int LAND_STATUS_FINISHED = 2;

    // 农场种地配置服务
    @Autowired
    private DicFarmService dicFarmService;

    // 用户缓存服务
    @Autowired
    private UserCacheService userCacheService;

    // 用户资产缓存服务
    @Autowired
    private UserCapitalCacheService userCapitalCacheService;

    // 用户农场土地服务
    @Autowired
    private UserFarmLandService userFarmLandService;

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

    //道具信息
    //PlayGameService.itemMap.get(id)
    /**
     * 001 - 获取农场信息
     * 1-3号地为实名制,实名制后解锁;其余地必须要在实名制后才可解锁;
     * 4-6号为VIP制,VIP制后解锁; 如果当前地块上有作物,那么无论是否是VIP都可以继续成长和收割；
     * 7-9号为资产解锁地；t_user_farm_land表记录就被视为已解锁
     * 收割不再检查实名/VIP/积分，只看该块地上是否有作物、是否到时间。
     */
    @ServiceMethod(code = "001", description = "获取农场信息")
    @Transactional
    public JSONObject getMyFarmInfo(ManagerSocketServer socket, JSONObject data) {
        checkNull(data);
        checkNull(data.get("userId"));
        Long userId = data.getLong("userId");
        // 用户、实名制、VIP校验
        User user = loadAndCheckUser(userId);
        boolean realName = isRealName(user);
        boolean isVip = isVipUser(user);

        // 用户所有土地
        Map<Integer, UserFarmLand> landMap = loadUserLandMap(userId);

        // 遍历每块土地
        JSONArray landsArr = new JSONArray();
        long now = System.currentTimeMillis() / 1000L;

        for (int index = 1; index <= 9; index++) {
            UserFarmLand land = landMap.get(index);

            // 构建单块土地的视图
            JSONObject landJson = buildLandJson(index, land, realName, isVip, now);

            // status字段反向校验..用户种植之后在刷新这个页面如果成熟要更新status..但是没啥用 前端直接根据时间计时渲染就行
            if (land != null) {
                Integer targetStatus = landJson.getInteger("status");
                if (targetStatus == null) {
                    targetStatus = LAND_STATUS_EMPTY;
                }
                Integer dbStatus = land.getStatus();
                int currentStatus = (dbStatus == null ? LAND_STATUS_EMPTY : dbStatus);
                if (currentStatus != targetStatus) {
                    land.setStatus(targetStatus);
                    userFarmLandService.plantLand(land);
                }
            }

            landsArr.add(landJson);
        }

        JSONObject result = new JSONObject();
        result.put("userId", userId);
        result.put("lands", landsArr);
        result.put("realName", realName);
        result.put("vip", isVip);

        return result;
    }


    /**
     * 种地种地
     */
    @ServiceMethod(code = "002", description = "播种")
    @Transactional
    public JSONObject plant(ManagerSocketServer socket, JSONObject data) {
        checkNull(data);
        Long userId = data.getLong("userId");
        Integer landIndex = data.getInteger("landIndex");
        Integer seedItemId = data.getInteger("seedItemId");
        if (userId == null || landIndex == null || seedItemId == null) {
            throwExp("参数不完整");
        }

        // 用户、实名制、VIP校验
        User user = loadAndCheckUser(userId);
        boolean realName = isRealName(user);
        boolean isVip = isVipUser(user);

        if (landIndex < 1 || landIndex > 9) {
            throwExp("非法土地编号");
        }

        // 计算锁类型
        String lockType = getLockTypeByIndex(landIndex);

        // 要种的这块地的信息
        UserFarmLand land = userFarmLandService.findOneByUserAndIndex(userId, landIndex);
        Integer currentSeedId = (land != null ? land.getSeedItemId() : null);

        long now = System.currentTimeMillis() / 1000L;
        Long endTimeSec = null;
        if (land != null && land.getEndTime() != null) {
            endTimeSec = land.getEndTime().getTime() / 1000L;
        }

        // 这块地是否能解锁; 这块地拥有但是不能代表现在可以播种;
        boolean unlocked;
        switch (lockType) {
            case "REALNAME":
                unlocked = realName;
                break;
            case "VIP":
                if (land != null && currentSeedId != null && currentSeedId > 0) {
                    // 曾经种植过就视为已经拥有这块土地
                    unlocked = true;
                } else {
                    unlocked = isVip;
                }
                break;
            case "CORE_POINT":
                // 有记录说明已用核心积分解锁过
                unlocked = (land != null);
                break;
            default:
                unlocked = false;
        }

        // 未解锁直接根据类型给提示
        if (!unlocked) {
            if ("REALNAME".equals(lockType)) {
                throwExp("该地块需要实名后才能播种");
            } else if ("VIP".equals(lockType)) {
                throwExp("该地块为 VIP 土地，请先开通 VIP");
            } else {
                throwExp("该地块尚未解锁");
            }
        }

        // 地块是否空闲 / 是否有未收割作物
        boolean landEmpty = (currentSeedId == null || currentSeedId == 0);
        boolean timeFinished = (endTimeSec != null && now >= endTimeSec);

        if (!landEmpty) {
            if (!timeFinished) {
                throwExp("土地正在种植中，暂不可播种");
            } else {
                throwExp("当前土地有成熟作物，请先收割后再播种");
            }
        }

        // 校验种子配置（只看growSeconds、status就行; 产出用 dic_farm.reward 控制）
        DicFarm farmCfg = PlayGameService.DIC_FARM.get(String.valueOf(seedItemId));
        if (farmCfg == null || farmCfg.getStatus() == null || farmCfg.getStatus() == 0) {
            throwExp("该种子暂不支持种植");
        }

        Integer growSeconds = farmCfg.getGrowSeconds();
        if (growSeconds == null || growSeconds <= 0) {
            throwExp("种子生长配置异常");
        }

        // 校验背包是否有足够种子、扣除种子;种地固定是 1 个种子
        gameService.checkUserItemNumber(userId.toString(), String.valueOf(seedItemId), 1);
        gameService.updateUserBackpack(
                userId,
                String.valueOf(seedItemId),
                -1,
                LogUserBackpackTypeEnum.use
        );

        // 计算开始/结束时间
        long startSec = now;
        long endSec = now + growSeconds;
        Date startDate = new Date(startSec * 1000L);
        Date endDate = new Date(endSec * 1000L);

        // 写入 / 更新土地记录
        UserFarmLand plantLand = new UserFarmLand();
        plantLand.setUserId(userId);
        plantLand.setLandIndex(landIndex);
        plantLand.setSeedItemId(seedItemId);
        plantLand.setStartTime(startDate);
        plantLand.setEndTime(endDate);
        // 成长中
        plantLand.setStatus(LAND_STATUS_GROWING);

        userFarmLandService.plantLand(plantLand);

        // 直接构建当前地块视图
        JSONObject landJson = buildLandJson(landIndex, plantLand, realName, isVip, now);
        JSONObject result = new JSONObject();
        result.put("userId", userId);
        result.put("land", landJson);
        result.put("realName", realName);
        result.put("vip", isVip);

        return result;
    }



    /**
     * 003 - 果实成熟收割 / 一键收割
     *
     * landIndex：
     *   1~9  表示单块收割；
     *   -1   表示一键收割全部已成熟地块。
     *
     * 收益发放完全改为：
     *   - 从 dic_farm.reward 取出 JSON 数组；
     *   - 汇总所有奖励后，统一走 PlayGameService.addReward；
     *     - 资产：由 dic_item.type = 4 决定，走 UserCapital；
     *     - 其他：走背包。
     */
    @ServiceMethod(code = "003", description = "收割 / 一键收割")
    @Transactional
    public JSONObject harvest(ManagerSocketServer socket, JSONObject data) {
        checkNull(data);

        Long userId = data.getLong("userId");
        Integer landIndex = data.getInteger("landIndex");
        if (userId == null || landIndex == null) {
            throwExp("参数不完整");
        }

        // 一键收割
        boolean harvestAll = (landIndex == -1);
        if (!harvestAll) {
            if (landIndex < 1 || landIndex > 9) {
                throwExp("非法的地块编号");
            }
        }

        // 用户、实名制、VIP 校验
        User user = loadAndCheckUser(userId);
        boolean realName = isRealName(user);
        boolean isVip = isVipUser(user);

        long now = System.currentTimeMillis() / 1000L;

        // 拉取用户所有土地 Map
        Map<Integer, UserFarmLand> landMap = loadUserLandMap(userId);

        // 收益列表
        Map<String, BigDecimal> gainMap = new HashMap<>();

        // 汇总所有要发放的 reward
        JSONArray allRewards = new JSONArray();

        if (harvestAll) {
            /*一键收割 */
            boolean hasAny = false;

            for (Map.Entry<Integer, UserFarmLand> entry : landMap.entrySet()) {
                Integer idx = entry.getKey();
                UserFarmLand land = entry.getValue();
                if (land == null) {
                    continue;
                }

                Integer seedItemId = land.getSeedItemId();
                if (seedItemId == null || seedItemId <= 0) {
                    // 没种东西
                    continue;
                }

                Long endTimeSec = null;
                if (land.getEndTime() != null) {
                    endTimeSec = land.getEndTime().getTime() / 1000L;
                }
                if (endTimeSec == null || now < endTimeSec) {
                    // 未成熟
                    continue;
                }

                // 农场配置
                DicFarm farmCfg = PlayGameService.DIC_FARM.get(String.valueOf(seedItemId));
                if (farmCfg == null || farmCfg.getStatus() == null || farmCfg.getStatus() == 0) {
                    continue;
                }

                String rewardStr = farmCfg.getReward();
                if (rewardStr == null || rewardStr.trim().isEmpty()) {
                    // 没配置奖励，跳过
                    continue;
                }

                JSONArray rewardArr;
                try {
                    rewardArr = JSONArray.parseArray(rewardStr);
                } catch (Exception e) {
                    continue;
                }
                if (rewardArr == null || rewardArr.isEmpty()) {
                    continue;
                }

                hasAny = true;

                // 汇总奖励到 allRewards + gainMap
                for (Object r : rewardArr) {
                    if (!(r instanceof JSONObject)) {
                        continue;
                    }
                    JSONObject reward = (JSONObject) r;

                    // 只统计 type = 1 的道具类奖励
                    int rType = reward.getIntValue("type");
                    if (rType != 1) {
                        allRewards.add(reward);
                        continue;
                    }

                    String itemId = reward.getString("id");
                    if (itemId == null || itemId.trim().isEmpty()) {
                        allRewards.add(reward);
                        continue;
                    }

                    BigDecimal num = reward.getBigDecimal("number");
                    if (num == null || num.compareTo(BigDecimal.ZERO) <= 0) {
                        allRewards.add(reward);
                        continue;
                    }

                    allRewards.add(reward);

                    BigDecimal old = gainMap.get(itemId);
                    if (old == null) {
                        old = BigDecimal.ZERO;
                    }
                    gainMap.put(itemId, old.add(num));
                }

                // 收割后清空地块上的数据
                land.setSeedItemId(0);
                land.setStartTime(null);
                land.setEndTime(null);
                land.setStatus(LAND_STATUS_EMPTY);
                userFarmLandService.plantLand(land);

                // 同步内存
                landMap.put(idx, land);
            }

            if (!hasAny) {
                throwExp("当前没有可收割的作物");
            }

            // 统一发奖
            gameService.addReward(
                    userId,
                    allRewards,
                    //发的是普通道具走背包所以资产枚举可以为Null
                    null,
                    LogUserBackpackTypeEnum.harvest
            );

            // 构建最新 9 块地的视图
            JSONArray landsArr = new JSONArray();
            for (int idx = 1; idx <= 9; idx++) {
                UserFarmLand l = landMap.get(idx);
                JSONObject landJson = buildLandJson(idx, l, realName, isVip, now);
                landsArr.add(landJson);
            }

            // 构建返回给前端的 gainList
            JSONArray gainList = new JSONArray();
            for (Map.Entry<String, BigDecimal> e : gainMap.entrySet()) {
                JSONObject gainJson = new JSONObject();
                gainJson.put("itemId", e.getKey());
                gainJson.put("number", e.getValue());
                gainList.add(gainJson);
            }

            JSONObject result = new JSONObject();
            result.put("userId", userId);
            result.put("mode", "ALL");
            result.put("lands", landsArr);
            result.put("gainList", gainList);
            result.put("realName", realName);
            result.put("vip", isVip);
            return result;

        } else {
            /*单块收割*/
            UserFarmLand land = landMap.get(landIndex);
            if (land == null || land.getSeedItemId() == null || land.getSeedItemId() <= 0) {
                throwExp("当前地块没有可收割的作物");
            }

            Long endTimeSec = null;
            if (land.getEndTime() != null) {
                endTimeSec = land.getEndTime().getTime() / 1000L;
            }
            if (endTimeSec == null || now < endTimeSec) {
                throwExp("作物尚未成熟，暂不可收割");
            }

            Integer seedItemId = land.getSeedItemId();
            DicFarm farmCfg = PlayGameService.DIC_FARM.get(String.valueOf(seedItemId));
            if (farmCfg == null || farmCfg.getStatus() == null || farmCfg.getStatus() == 0) {
                throwExp("作物产出配置异常");
            }

            String rewardStr = farmCfg.getReward();
            if (rewardStr == null || rewardStr.trim().isEmpty()) {
                throwExp("当前地块未配置收割奖励");
            }

            JSONArray rewardArr;
            try {
                rewardArr = JSONArray.parseArray(rewardStr);
            } catch (Exception e) {
                throwExp("收割奖励配置格式错误，请联系管理员");
                return null;
            }
            if (rewardArr == null || rewardArr.isEmpty()) {
                throwExp("当前地块未配置收割奖励");
            }

            // 汇总奖励到 gainMap
            for (Object r : rewardArr) {
                if (!(r instanceof JSONObject)) {
                    continue;
                }
                JSONObject reward = (JSONObject) r;

                int rType = reward.getIntValue("type");
                if (rType != 1) {
                    continue;
                }

                String itemId = reward.getString("id");
                if (itemId == null || itemId.trim().isEmpty()) {
                    continue;
                }

                BigDecimal num = reward.getBigDecimal("number");
                if (num == null || num.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }

                BigDecimal old = gainMap.get(itemId);
                if (old == null) {
                    old = BigDecimal.ZERO;
                }
                gainMap.put(itemId, old.add(num));
            }

            // 仅当前地块统一发奖
            gameService.addReward(
                    userId,
                    rewardArr,
                    //发的是普通道具走背包所以资产枚举可以为Null
                    null,
                    LogUserBackpackTypeEnum.harvest
            );

            // 收割后清空地块
            land.setSeedItemId(0);
            land.setStartTime(null);
            land.setEndTime(null);
            land.setStatus(LAND_STATUS_EMPTY);
            userFarmLandService.plantLand(land);

            // 构建当前地块视图
            JSONObject landJson = buildLandJson(landIndex, land, realName, isVip, now);

            // 构建
            JSONArray gainList = new JSONArray();
            for (Map.Entry<String, BigDecimal> e : gainMap.entrySet()) {
                JSONObject gainJson = new JSONObject();
                gainJson.put("itemId", e.getKey());
                gainJson.put("number", e.getValue());
                gainList.add(gainJson);
            }

            JSONObject result = new JSONObject();
            result.put("userId", userId);
            result.put("mode", "ONE");
            result.put("landIndex", landIndex);
            result.put("land", landJson);
            result.put("gainList", gainList);
            result.put("realName", realName);
            result.put("vip", isVip);
            return result;
        }
    }



    /**
     * 004 - 购买 / 解锁土地
     * 只有7 8 9 块土地可以购买;也就是只能购买三次;
     * 第一次购买读配表按照道具和资产类型进行扣除,并新增一条记录
     * 只要有记录无论是否正在种植或者收割 都视为已经解锁;
     */
    @ServiceMethod(code = "004", description = "购买 / 解锁土地")
    @Transactional
    public JSONObject unlockLand(ManagerSocketServer socket, JSONObject data) {
        checkNull(data);
        Long userId = data.getLong("userId");
        Integer landIndex = data.getInteger("landIndex");
        if (userId == null || landIndex == null) {
            throwExp("参数不完整");
        }

        if (landIndex < 7 || landIndex > 9) {
            throwExp("当前地块不支持购买解锁");
        }

        // 用户、实名制、VIP校验
        User user = loadAndCheckUser(userId);
        boolean realName = isRealName(user);
        boolean isVip = isVipUser(user);

        // 检查是否已经解锁.. 有记录就是已经解锁
        UserFarmLand existed = userFarmLandService.findOneByUserAndIndex(userId, landIndex);
        long nowSec = System.currentTimeMillis() / 1000L;
        if (existed != null) {
            // 已解锁：直接返回当前视图，不再重复扣费
            JSONObject landJson = buildLandJson(landIndex, existed, realName, isVip, nowSec);

            JSONObject result = new JSONObject();
            result.put("userId", userId);
            result.put("land", landJson);
            result.put("realName", realName);
            result.put("vip", isVip);
            result.put("message", "该地块已解锁，无需重复购买");
            return result;
        }

        // 读取解锁配置
        String cfgStr = managerConfigService.getString(Config.UNLOCK_FARM);
        if (cfgStr == null || cfgStr.trim().isEmpty()) {
            throwExp("土地解锁配置不存在，请联系管理员");
        }

        JSONObject cfg;
        try {
            cfg = JSONObject.parseObject(cfgStr);
        } catch (Exception e) {
            throwExp("土地解锁配置格式错误，请联系管理员");
            return null;
        }

        Integer capitalTypeId = cfg.getInteger("itemId");
        JSONObject indexCfg = cfg.getJSONObject("index");

        if (capitalTypeId == null || indexCfg == null) {
            throwExp("土地解锁配置不完整，请联系运营");
        }

        Integer needAmount = indexCfg.getInteger("index" + landIndex);
        if (needAmount == null || needAmount <= 0) {
            throwExp("当前地块暂不支持购买解锁");
        }

        // 需要扣除的金额
        BigDecimal amount = BigDecimal.valueOf(needAmount);

        // 资产检查：先查用户当前资产
        UserCapital userCapital = userCapitalService.findUserCapitalByUserIdAndCapitalType(userId, capitalTypeId);
        if (userCapital == null || userCapital.getBalance() == null) {
            throwExp("资产不足，无法解锁土地");
        }

        BigDecimal balanceBefore = userCapital.getBalance();
        if (balanceBefore == null) {
            balanceBefore = BigDecimal.ZERO;
        }
        BigDecimal occupyBalanceBefore = userCapital.getOccupyBalance();
        if (occupyBalanceBefore == null) {
            occupyBalanceBefore = BigDecimal.ZERO;
        }
        if (balanceBefore.compareTo(amount) < 0) {
            throwExp("资产不足，无法解锁土地");
        }

        //扣除资产 交易
        userCapitalService.subUserBalance(
                amount,
                userId,
                capitalTypeId,
                balanceBefore,
                occupyBalanceBefore,
                "UNLOCK_FARM_" + userId + "_" + landIndex,
                null,
                LogCapitalTypeEnum.achievement_reward,
                "t_user_farm_land"
        );

        //清理资产缓存
        userCapitalCacheService.deltedUserCapitalCache(userId, capitalTypeId);

        // 解锁之后就新增一条没有种植的土地信息 获得这块地的使用权，不自动种植
        Date nowDate = new Date();
        UserFarmLand newLand = new UserFarmLand();
        newLand.setUserId(userId);
        newLand.setLandIndex(landIndex);
        newLand.setSeedItemId(null);
        newLand.setStartTime(null);
        newLand.setEndTime(null);
        newLand.setStatus(LAND_STATUS_EMPTY);
        newLand.setCreateTime(nowDate);
        newLand.setUpdateTime(nowDate);

        userFarmLandService.plantLand(newLand);

        // 构建当前土地试图
        JSONObject landJson = buildLandJson(landIndex, newLand, realName, isVip, nowSec);

        JSONObject result = new JSONObject();
        result.put("userId", userId);
        result.put("land", landJson);
        result.put("realName", realName);
        result.put("vip", isVip);
        return result;
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

    /**
     * 判定是否实名
     */
    private boolean isRealName(User user) {
        if (user == null) {
            return false;
        }
        return user.getAuthentication() == 1;
    }

    /**
     * 判定是否 VIP 用户。
     */
    private boolean isVipUser(User user) {
        return user.getVip1() != 0;
    }

    /**
     * 根据 landIndex 计算锁类型。
     */
    private String getLockTypeByIndex(int index) {
        if (index <= 3) {
            return "REALNAME";
        } else if (index <= 6) {
            return "VIP";
        } else {
            return "CORE_POINT";
        }
    }

    /**
     * 拉取用户所有土地记录并封装为 Map
     */
    private Map<Integer, UserFarmLand> loadUserLandMap(Long userId) {
        List<UserFarmLand> landList = userFarmLandService.findListByUserId(userId);
        Map<Integer, UserFarmLand> landMap = new HashMap<>();
        if (landList != null && !landList.isEmpty()) {
            for (UserFarmLand land : landList) {
                if (land != null && land.getLandIndex() != null) {
                    landMap.put(land.getLandIndex(), land);
                }
            }
        }
        return landMap;
    }



    /**
     * 构建单块土地的视图
     */
    private JSONObject buildLandJson(int index, UserFarmLand land, boolean realName, boolean isVip, long now) {
        JSONObject landJson = new JSONObject();
        landJson.put("index", index);
        // 锁类型
        String lockType = getLockTypeByIndex(index);
        landJson.put("lockType", lockType);

        // 当前种植的种子
        Integer seedItemId = (land != null ? land.getSeedItemId() : null);

        // 开始 / 结束时间
        Long startTime = null;
        Long endTime = null;
        if (land != null && land.getStartTime() != null) {
            startTime = land.getStartTime().getTime() / 1000L;
        }
        if (land != null && land.getEndTime() != null) {
            endTime = land.getEndTime().getTime() / 1000L;
        }

        // 是否解锁
        boolean unlocked;
        switch (lockType) {
            case "REALNAME":
                unlocked = realName;
                break;
            case "VIP":
                if (land != null && seedItemId != null && seedItemId > 0) {
                    // 该地块已有东西,无论当前是否VIP都视为已解锁
                    unlocked = true;
                } else {
                    // 没有东西的时候,是否解锁取决于当前VIP状态
                    unlocked = isVip;
                }
                break;
            case "CORE_POINT":
                // 如果表中有记录,就代表之前使用资产解锁过
                unlocked = (land != null);
                break;
            default:
                unlocked = false;
                break;
        }
        landJson.put("unlocked", unlocked);

        // 土地是否可种植
        boolean canPlant = false;
        if (unlocked) {
            // 没有种子
            boolean landEmpty = (seedItemId == null || seedItemId == 0);
            // 有结束时间且已经达到结束时间
            boolean timeFinished = (endTime != null && now >= endTime);
            // 没有正在生长中的作物就是空地或者已经成熟
            boolean noGrowingPlant = landEmpty || timeFinished;

            if (noGrowingPlant) {
                if ("REALNAME".equals(lockType)) {
                    canPlant = realName;
                } else if ("VIP".equals(lockType)) {
                    canPlant = isVip;
                } else if ("CORE_POINT".equals(lockType)) {
                    // 已经积分解锁的地，只要解锁就可以播种
                    canPlant = true;
                }
            }
        }
        landJson.put("canPlant", canPlant);

        // 状态 + 剩余时间 + 预计产出
        int statusCode;
        long remainSeconds = 0L;
        int expectedOutput = 0;

        if (seedItemId == null || seedItemId == 0) {
            // 没有种子 = 空地
            statusCode = LAND_STATUS_EMPTY;
        } else {
            if (endTime != null && now >= endTime) {
                // 有种子，且到时间 = 已成熟
                statusCode = LAND_STATUS_FINISHED;
            } else {
                // 有种子，且未到时间 = 生长中
                statusCode = LAND_STATUS_GROWING;
                if (endTime != null) {
                    remainSeconds = Math.max(0L, endTime - now);
                }
            }
            // 预计产出数量：从 dic_farm.reward 中统计“果实/材料”数量
            expectedOutput = calcExpectedOutputFromReward(seedItemId);
        }

        landJson.put("seedItemId", seedItemId);
        landJson.put("startTime", startTime);
        landJson.put("endTime", endTime);
        landJson.put("remainSeconds", remainSeconds);
        landJson.put("status", statusCode);
        landJson.put("expectedOutput", expectedOutput);

        return landJson;
    }

    /**
     * 根据种子ID和 dic_farm.reward 计算预计产出数量。
     *
     * 当前规则：
     *   - 解析 dic_farm.reward 为 JSONArray；
     *   - 只统计 reward 中 type = 1 的条目；
     *   - 且该条目对应的 dic_item.type = 1（果实/材料）；
     *   - number 累加后取 intValue 作为预计产出。
     */
    private int calcExpectedOutputFromReward(Integer seedItemId) {
        if (seedItemId == null || seedItemId <= 0) {
            return 0;
        }

        DicFarm cfg = PlayGameService.DIC_FARM.get(String.valueOf(seedItemId));
        if (cfg == null) {
            return 0;
        }

        String rewardStr = cfg.getReward();
        if (rewardStr == null || rewardStr.trim().isEmpty()) {
            return 0;
        }

        JSONArray arr;
        try {
            arr = JSONArray.parseArray(rewardStr);
        } catch (Exception e) {
            // 配置格式异常时，直接视为无预计产出，避免拖垮主流程
            return 0;
        }
        if (arr == null || arr.isEmpty()) {
            return 0;
        }

        BigDecimal total = BigDecimal.ZERO;

        for (Object o : arr) {
            if (!(o instanceof JSONObject)) {
                continue;
            }
            JSONObject reward = (JSONObject) o;

            // 只统计 type = 1 的道具类奖励
            int type = reward.getIntValue("type");
            if (type != 1) {
                continue;
            }

            String itemId = reward.getString("id");
            if (itemId == null || itemId.trim().isEmpty()) {
                continue;
            }

            // 只统计“果实/材料”（dic_item.type = 1）
            Item dicItem = PlayGameService.itemMap.get(itemId);
            Integer itemType = (dicItem != null ? dicItem.getType() : null);
            if (itemType == null || itemType != 1) {
                continue;
            }

            BigDecimal num = reward.getBigDecimal("number");
            if (num == null || num.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            total = total.add(num);
        }

        return total.intValue();
    }

}

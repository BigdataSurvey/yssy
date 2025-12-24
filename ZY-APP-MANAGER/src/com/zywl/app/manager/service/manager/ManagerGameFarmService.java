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
import java.util.*;

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

    //用户气球数增值欢乐豆服务
    @Autowired
    private ManagerJoyService managerJoyService;

    static class JoyTrigger {
        int itemQuality;
        String eventId;
        String sourceType;
        JoyTrigger(int itemQuality, String eventId, String sourceType) {
            this.itemQuality = itemQuality;
            this.eventId = eventId;
            this.sourceType = sourceType;
        }
    }



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
        // 新一轮种植，从未收割过
        plantLand.setLastHarvestTime(null);
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
     * 003 - 果实成熟收割 / 一键收割（线性产出 + 多次领取）
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
        if (!harvestAll && (landIndex < 1 || landIndex > 9)) {
            throwExp("非法的地块编号");
        }

        // 用户、实名制、VIP 校验
        User user = loadAndCheckUser(userId);
        boolean realName = isRealName(user);
        boolean isVip = isVipUser(user);

        long nowSec = System.currentTimeMillis() / 1000L;

        // 拉取用户所有土地 Map
        Map<Integer, UserFarmLand> landMap = loadUserLandMap(userId);

        // 汇总所有要发放的奖励
        JSONArray allRewards = new JSONArray();
        // 触发Joy
        List<JoyTrigger> joyTriggers = new ArrayList<>();

        // 前端展示每个itemId一共领了多少
        Map<String, BigDecimal> gainMap = new HashMap<>();

        if (harvestAll) {
            boolean hasAnyReward = false;
            for (int idx = 1; idx <= 9; idx++) {
                UserFarmLand land = landMap.get(idx);
                if (land == null || land.getSeedItemId() == null || land.getSeedItemId() <= 0) {
                    // 空地或未种植
                    continue;
                }

                boolean landHasReward = appendLandHarvestRewards(land, nowSec, allRewards, gainMap);

                if (landHasReward) {
                    hasAnyReward = true;

                    // 在收割之前触发欢乐值收集统计
                    Integer seedIdBefore =land.getSeedItemId();
                    Date startTimeBefore = land.getStartTime();
                    boolean cleared = (land.getSeedItemId() == null || land.getSeedItemId() == 0);
                    if (cleared && seedIdBefore != null && seedIdBefore > 0) {
                        Item seedItem = PlayGameService.itemMap.get(String.valueOf(seedIdBefore));
                        Integer q = (seedItem != null ? seedItem.getQuality() : null);
                        if (q != null && q > 0) {
                            // eventId 要“每次种植周期唯一”，推荐带上 startTime
                            long st = (startTimeBefore != null ? startTimeBefore.getTime() : System.currentTimeMillis());
                            String joyEventId = "FARM_HARVEST_" + userId + "_" + idx + "_" + st;

                            joyTriggers.add(new JoyTrigger(q, joyEventId, "FARM_HARVEST"));
                        }
                    }

                    // 判断是否本轮全部产出已领完，若是则清空地块
                    finalizeLandAfterHarvest(land, nowSec);
                    userFarmLandService.plantLand(land);
                }
            }

            if (!hasAnyReward) {
                throwExp("当前没有可收割的作物");
            }

            // 统一发奖
            gameService.addReward(
                    userId,
                    // 本次应得的增量奖励
                    allRewards,
                    // 资产类型由 dic_item.type 决定
                    null,
                    LogUserBackpackTypeEnum.harvest
            );

            // 发奖成功后再发欢乐值
            for (JoyTrigger t : joyTriggers) {
                managerJoyService.distributeJoy(userId, t.itemQuality, t.eventId, "FARM_HARVEST");
            }

            // 构建最新 9 块地的视图
            JSONArray landsArr = new JSONArray();
            for (int idx = 1; idx <= 9; idx++) {
                UserFarmLand l = landMap.get(idx);
                JSONObject landJson = buildLandJson(idx, l, realName, isVip, nowSec);
                landsArr.add(landJson);
            }

            // 构建 gainList 返回给前端
            JSONArray gainList = buildGainList(gainMap);

            JSONObject result = new JSONObject();
            result.put("userId", userId);
            result.put("mode", "ALL");
            result.put("lands", landsArr);
            result.put("gainList", gainList);
            result.put("realName", realName);
            result.put("vip", isVip);
            return result;
        } else {
            // 单块收割
            UserFarmLand land = landMap.get(landIndex);
            if (land == null || land.getSeedItemId() == null || land.getSeedItemId() <= 0) {
                throwExp("当前地块没有可收割的作物");
            }

            boolean landHasReward = appendLandHarvestRewards(land, nowSec, allRewards, gainMap);
            if (!landHasReward) {
                // 有种子但当前时间点没有新产出的数量。。例如刚种下或刚领完
                throwExp("当前地块暂时没有可收割的果实");
            }

            // 在收割之前触发欢乐值收集统计
            Integer seedItemId = land.getSeedItemId();
            Date lastHarvestTime = land.getLastHarvestTime();
            int itemQuality = 0;
            String joyEventId = null;
            if (seedItemId != null && seedItemId > 0 && lastHarvestTime != null) {
                Item seedItem = PlayGameService.itemMap.get(String.valueOf(seedItemId));
                Integer q = (seedItem != null ? seedItem.getQuality() : null);
                itemQuality = (q == null ? 0 : q);
                if (itemQuality > 0) {
                    joyEventId = "FARM_HARVEST_" + userId + "_" + landIndex + "_" + lastHarvestTime.getTime();
                }
            }

            // 仅当前地块发奖
            gameService.addReward(
                    userId,
                    allRewards,
                    null,
                    LogUserBackpackTypeEnum.harvest
            );

            // 发奖成功后再发欢乐值
            if (itemQuality > 0) {
                managerJoyService.distributeJoy(userId, itemQuality, joyEventId, "FARM_HARVEST");
            }

            // 判断是否本轮全部产出已领完；若是则清空地块
            finalizeLandAfterHarvest(land, nowSec);
            userFarmLandService.plantLand(land);

            // 构建当前地块视图
            JSONObject landJson = buildLandJson(landIndex, land, realName, isVip, nowSec);

            // 构建 gainList 返回给前端
            JSONArray gainList = buildGainList(gainMap);

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
     * 对单块土地，在当前时间点计算“本次新增可领取的奖励”，追加到 allRewards / gainMap。
     *
     * 规则：
     *   - startSec = land.startTime
     *   - endSec   = startSec + growSeconds
     *   - curSec   = min(nowSec, endSec)
     *   - lastHarvestSec:
     *         若 lastHarvestTime 为 null，则等于 startSec（视为从未收割过）
     *         否则 clamp 到 [startSec, endSec]
     *
     *   对于 reward 中每条 {number = baseNum}：
     *       producedTotal  = floor(baseNum * (curSec - startSec) / growSeconds)
     *       producedBefore = floor(baseNum * (lastHarvestSec - startSec) / growSeconds)
     *       delta          = producedTotal - producedBefore
     *
     *   若 delta > 0，则表示本次可以新增领取 delta 个。
     */
    private boolean appendLandHarvestRewards(UserFarmLand land, long nowSec, JSONArray allRewards, Map<String, BigDecimal> gainMap) {
        Integer seedItemId = land.getSeedItemId();
        if (seedItemId == null || seedItemId <= 0) {
            return false;
        }

        DicFarm farmCfg = PlayGameService.DIC_FARM.get(String.valueOf(seedItemId));
        if (farmCfg == null || farmCfg.getStatus() == null || farmCfg.getStatus() == 0) {
            return false;
        }

        if (land.getStartTime() == null) {
            return false;
        }

        long startSec = land.getStartTime().getTime() / 1000L;
        Integer growSeconds = farmCfg.getGrowSeconds();
        if (growSeconds == null || growSeconds <= 0) {
            return false;
        }
        long grow = growSeconds.longValue();
        if (grow <= 0L) {
            return false;
        }

        long endSec = startSec + grow;
        //不会超过满产时间
        long curSec = Math.min(nowSec, endSec);
        if (curSec <= startSec) {
            // 还没到可以产出的时间
            return false;
        }

        long lastHarvestSec = startSec;
        if (land.getLastHarvestTime() != null) {
            long tmp = land.getLastHarvestTime().getTime() / 1000L;
            // 限制在 startSec, endSec 范围内
            if (tmp < startSec) {
                lastHarvestSec = startSec;
            } else if (tmp > endSec) {
                lastHarvestSec = endSec;
            } else {
                lastHarvestSec = tmp;
            }
        }

        if (curSec <= lastHarvestSec) {
            // 没有新的时间窗口可结算
            return false;
        }

        String rewardStr = farmCfg.getReward();
        if (rewardStr == null || rewardStr.trim().isEmpty()) {
            return false;
        }

        JSONArray rewardArr;
        try {
            rewardArr = JSONArray.parseArray(rewardStr);
        } catch (Exception e) {
            return false;
        }
        if (rewardArr == null || rewardArr.isEmpty()) {
            return false;
        }

        boolean hasReward = false;

        for (Object r : rewardArr) {
            if (!(r instanceof JSONObject)) {
                continue;
            }
            JSONObject reward = (JSONObject) r;

            int rType = reward.getIntValue("type");
            String itemId = reward.getString("id");
            long baseNum = reward.getLongValue("number");

            if (itemId == null || itemId.trim().isEmpty()) {
                continue;
            }
            if (baseNum <= 0L) {
                continue;
            }

            /*线性产出*/
            // 一共收取的数量
            long producedTotal = baseNum * (curSec - startSec) / grow;
            // 本次收取的数量
            long producedBefore = baseNum * (lastHarvestSec - startSec) / grow;
            long delta = producedTotal - producedBefore;
            if (delta <= 0L) {
                continue;
            }

            JSONObject scaled = new JSONObject();
            scaled.put("type", rType);
            scaled.put("id", itemId);
            scaled.put("number", delta);
            allRewards.add(scaled);
            hasReward = true;

            //gainMp按照itemId统计给前端
            BigDecimal old = gainMap.get(itemId);
            if (old == null) {
                old = BigDecimal.ZERO;
            }
            gainMap.put(itemId, old.add(BigDecimal.valueOf(delta)));
        }

        if (hasReward) {
            land.setLastHarvestTime(new Date(curSec * 1000L));
            if (curSec >= endSec) {
                land.setStatus(LAND_STATUS_FINISHED);
            } else {
                land.setStatus(LAND_STATUS_GROWING);
            }
        }

        return hasReward;
    }

    /**
     * 构建返回前端的 gainList
     */
    private JSONArray buildGainList(Map<String, BigDecimal> gainMap) {
        JSONArray gainList = new JSONArray();
        for (Map.Entry<String, BigDecimal> e : gainMap.entrySet()) {
            JSONObject gainJson = new JSONObject();
            gainJson.put("itemId", e.getKey());
            gainJson.put("number", e.getValue());
            gainList.add(gainJson);
        }
        return gainList;
    }

    /**
     * 收割后地块状态收尾：
     * - 若当前时间点已经把这一轮的线性产出全部领完，则清空地块，方便重新播种；
     * - 否则保留种子与时间信息，继续“矿机模式”线性产出。
     */
    private void finalizeLandAfterHarvest(UserFarmLand land, long nowSec) {
        if (land == null || land.getSeedItemId() == null || land.getSeedItemId() <= 0) {
            return;
        }

        DicFarm farmCfg = PlayGameService.DIC_FARM.get(String.valueOf(land.getSeedItemId()));
        if (farmCfg == null || farmCfg.getGrowSeconds() == null || farmCfg.getGrowSeconds() <= 0) {
            return;
        }
        if (land.getStartTime() == null) {
            return;
        }

        long startSec = land.getStartTime().getTime() / 1000L;
        long grow = farmCfg.getGrowSeconds().longValue();
        long endSec = startSec + grow;

        // 以 lastHarvestTime 为准判断当前结算进度
        long lastHarvestSec = (land.getLastHarvestTime() != null)
                ? (land.getLastHarvestTime().getTime() / 1000L)
                : startSec;

        if (lastHarvestSec >= endSec) {
            // 说明这一轮的线性产出已经全部结算完，清空地块
            land.setSeedItemId(0);
            land.setStartTime(null);
            land.setEndTime(null);
            land.setLastHarvestTime(null);
            land.setStatus(LAND_STATUS_EMPTY);
        } else {
            // 尚未全部产完，保持当前种子与时间信息
            long curSec = Math.min(nowSec, endSec);
            if (curSec >= endSec) {
                land.setStatus(LAND_STATUS_FINISHED);
            } else {
                land.setStatus(LAND_STATUS_GROWING);
            }
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

        // 当前时间点可领取的数量（线性产出 - 已结算）
        int availableOutput = 0;
        if (seedItemId != null && seedItemId > 0 && land != null && land.getStartTime() != null) {
            availableOutput = calcAvailableOutput(seedItemId, land, now);
        }


        landJson.put("seedItemId", seedItemId);
        landJson.put("startTime", startTime);
        landJson.put("endTime", endTime);
        landJson.put("remainSeconds", remainSeconds);
        landJson.put("status", statusCode);
        landJson.put("expectedOutput", expectedOutput);
        landJson.put("availableOutput", availableOutput);

        return landJson;
    }


    /**
     * 当前时间点“还未领取但已经产出”的数量
     */
    private int calcAvailableOutput(Integer seedItemId, UserFarmLand land, long nowSec) {
        if (seedItemId == null || seedItemId <= 0 || land == null || land.getStartTime() == null) {
            return 0;
        }

        DicFarm cfg = PlayGameService.DIC_FARM.get(String.valueOf(seedItemId));
        if (cfg == null || cfg.getGrowSeconds() == null || cfg.getGrowSeconds() <= 0) {
            return 0;
        }

        long startSec = land.getStartTime().getTime() / 1000L;
        long grow = cfg.getGrowSeconds().longValue();
        long endSec = startSec + grow;
        long curSec = Math.min(nowSec, endSec);
        if (curSec <= startSec) {
            return 0;
        }

        long lastHarvestSec = startSec;
        if (land.getLastHarvestTime() != null) {
            long tmp = land.getLastHarvestTime().getTime() / 1000L;
            if (tmp < startSec) {
                lastHarvestSec = startSec;
            } else if (tmp > endSec) {
                lastHarvestSec = endSec;
            } else {
                lastHarvestSec = tmp;
            }
        }

        String rewardStr = cfg.getReward();
        if (rewardStr == null || rewardStr.trim().isEmpty()) {
            return 0;
        }

        JSONArray arr;
        try {
            arr = JSONArray.parseArray(rewardStr);
        } catch (Exception e) {
            return 0;
        }
        if (arr == null || arr.isEmpty()) {
            return 0;
        }

        BigDecimal totalAvailable = BigDecimal.ZERO;

        for (Object o : arr) {
            if (!(o instanceof JSONObject)) {
                continue;
            }
            JSONObject reward = (JSONObject) o;

            int type = reward.getIntValue("type");
            if (type != 1) {
                continue;
            }

            String itemId = reward.getString("id");
            if (itemId == null || itemId.trim().isEmpty()) {
                continue;
            }

            // 只统计“果实/材料”
            Item dicItem = PlayGameService.itemMap.get(itemId);
            Integer itemType = (dicItem != null ? dicItem.getType() : null);
            if (itemType == null || itemType != 1) {
                continue;
            }

            long baseNum = reward.getLongValue("number");
            if (baseNum <= 0L) {
                continue;
            }

            long producedTotal = baseNum * (curSec - startSec) / grow;
            long producedBefore = baseNum * (lastHarvestSec - startSec) / grow;
            long delta = producedTotal - producedBefore;
            if (delta > 0L) {
                totalAvailable = totalAvailable.add(BigDecimal.valueOf(delta));
            }
        }

        return totalAvailable.intValue();
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

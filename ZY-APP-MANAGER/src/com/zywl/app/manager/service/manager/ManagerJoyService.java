package com.zywl.app.manager.service.manager;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.*;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.base.util.LockUtil;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.cache.UserCapitalCacheService;
import com.zywl.app.defaultx.enmus.ItemIdEnum;
import com.zywl.app.defaultx.enmus.LogCapitalTypeEnum;
import com.zywl.app.defaultx.enmus.LogUserBackpackTypeEnum;
import com.zywl.app.defaultx.service.*;
import com.zywl.app.manager.context.MessageCodeContext;
import com.zywl.app.manager.service.PlayGameService;
import com.zywl.app.manager.socket.ManagerSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.Map;

/**
 * @Author: lzx
 * @Create: 2025/12/14
 * @Version: V1.0
 * @Description: 欢乐值（气球树）核心业务服务  USER_JOR 037
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

    // 欢乐值贡献流水
    @Autowired
    private UserJoyEventService userJoyEventService;


    /**
     * 001 - 查询我的欢乐值与可兑气球数量
     *
     */
    @ServiceMethod(code = "001", description = "查询我的欢乐值与可兑气球数量")
    @Transactional
    public JSONObject getMyJoyInfo(ManagerSocketServer socket, JSONObject data) {
        // 参数校验
        checkNull(data);
        checkNull(data.get("userId"));
        Long userId = data.getLong("userId");
        loadAndCheckUser(userId);

        // 获取用户欢乐值
        UserJoy userJoy = getAndCheckUserJoy(userId);

        // 获取配置的兑换比例
        int exchangeRate = getJoyExchangeRate();

        // 计算可兑换气球组数 向下取整
        int canExchangeNum = 0;
        if (exchangeRate > 0 && userJoy.getAvailableJoy().compareTo(BigDecimal.ZERO) > 0) {
            canExchangeNum = userJoy.getAvailableJoy()
                    .divideToIntegralValue(BigDecimal.valueOf(exchangeRate))
                    .intValue();
        }

        JSONObject result = new JSONObject();
        result.put("userId", userId);
        // 累计欢乐值
        result.put("totalJoy", userJoy.getTotalJoy());
        // 当前可用
        result.put("availableJoy", userJoy.getAvailableJoy());
        // 今日获得
        result.put("todayJoy", userJoy.getTodayJoy());
        // 兑换比例（xx欢乐值换1组）
        result.put("exchangeRate", exchangeRate);
        // 当前可换多少组
        result.put("canExchangeNum", canExchangeNum);

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
     */
    @ServiceMethod(code = "002", description = "兑换气球")
    @Transactional
    public JSONObject exchangeJoyToBalloon(ManagerSocketServer socket, JSONObject data) {
        // 参数校验
        checkNull(data);
        checkNull(data.get("userId"));
        Long userId = data.getLong("userId");
        loadAndCheckUser(userId);

        synchronized (LockUtil.getlock(userId)) {
            // 用户欢乐值汇总表
            UserJoy userJoy = getAndCheckUserJoy(userId);
            //欢乐值兑换比例 单组气球的欢乐值价格
            int joyExchangeRate = getJoyExchangeRate();
            if (joyExchangeRate <= 0) {
                throwExp("兑换活动暂停中");
            }

            // 当前可用欢乐值
            BigDecimal available = userJoy.getAvailableJoy();
            //  当前可兑换气球数量  当前欢乐值/兑换比例 向下取整
            int exchangeCount = available.divideToIntegralValue(BigDecimal.valueOf(joyExchangeRate)).intValue();
            if (exchangeCount <= 0) {
                throwExp("欢乐值不足，无法兑换");
            }

            // 总消耗欢乐值 = 可兑换的气球数 * 单组气球的价格
            BigDecimal usedJoy  = BigDecimal.valueOf(exchangeCount).multiply(BigDecimal.valueOf(joyExchangeRate));
            // 执行扣除
            userJoy.setAvailableJoy(available.subtract(usedJoy));
            userJoy.setUpdateTime(new Date());
            userJoyService.updateByUserId(userJoy);

            //气球道具
            String balloonItemId = ItemIdEnum.BALLOON.getValue();

            // 构造奖励并发放
            JSONArray rewards = new JSONArray();
            JSONObject item = new JSONObject();
            item.put("type", 1);
            item.put("id", balloonItemId);
            item.put("number", exchangeCount);
            rewards.add(item);

            // 走 addReward
            gameService.addReward(userId, rewards,
                    null,
                    LogUserBackpackTypeEnum.balloon_exchange);

            //扣除之后所剩欢乐值
            BigDecimal remainJoy = userJoy.getAvailableJoy();
            int nextExchangeableCount = remainJoy
                    .divideToIntegralValue(BigDecimal.valueOf(joyExchangeRate))
                    .intValue();


            JSONObject result = new JSONObject();
            result.put("userId", userId);
            //消耗的欢乐值
            result.put("usedJoy", usedJoy);
            result.put("remainJoy", remainJoy);
            result.put("joyExchangeRate", joyExchangeRate);
            result.put("exchangeCount", exchangeCount);
            result.put("nextExchangeableCount", nextExchangeableCount);
            //获得的物品
            result.put("reward", rewards);
            return result;
        }

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
        loadAndCheckUser(userId);

        // 校验好友是否存在
        User friend = userCacheService.getUserInfoById(friendUserId);
        if (friend == null) {
            throwExp("好友信息不存在");
        }

        // 查询好友对我的贡献汇总
        UserJoyContrib contrib = userJoyContribService.findOneByPair(userId, friendUserId);

        // 权限边界：允许“有贡献记录”或“在我 1~5 代下级链路内”
        if (contrib == null && !isDownlineWithinLevels(friendUserId, userId, 5)) {
            throwExp("无权限查看该用户贡献");
        }

        BigDecimal totalJoy = BigDecimal.ZERO;
        BigDecimal todayJoy = BigDecimal.ZERO;
        if (contrib != null) {
            totalJoy = contrib.getTotalJoy();
            if (contrib.getTodayDate() != null && contrib.getTodayDate() == DateUtil.getTodayInt()) {
                todayJoy = contrib.getTodayJoy();
            }
        }

        JSONObject result = new JSONObject();
        result.put("userId", userId);
        result.put("friendUserId", friendUserId);
        result.put("friendName", friend.getName());
        result.put("friendHeadImg", friend.getHeadImageUrl());
        result.put("todayJoy", todayJoy);
        result.put("totalJoy", totalJoy);

        return result;
    }

    /**
     * 分发欢乐值（1~5 代上级分润）
     *
     * 适用场景：
     * - 任意玩法结算点（农场收割/开荒/后续玩法产出等）只需要给出：
     *   触发者 triggerUserId、触发道具品质 itemQuality、事件唯一ID eventId、来源 sourceType，
     *   即可完成上级链路 1~5 代的欢乐值分润入账。
     *
     * 配置来源（全部走 t_config）：
     * 1) Config.JOY_PER_LEVEL
     *    JSON：{"1":5.0,"2":10.0,...}
     *    含义：按 dic_item.quality（或玩法定义的等级）映射出本次事件的 baseJoy（基础欢乐值）。
     *
     * 2) Config.JOY_LEVEL_PERCENT
     *    JSON：{"1":30,"2":25,"3":20,"4":15,"5":10}
     *    含义：第 i 代上级分润比例 percent[i]（百分比，不要求和=100，<=100 即可）。
     *
     * 3) 精度规则：
     *    当前实现：actualJoy = baseJoy * percent / 100，保留 2 位小数，RoundingMode.FLOOR。
     *    说明：欢乐值允许小数；向下取整可避免“凭空多发”。
     *
     * 并发与幂等（务必理解）：
     * A. 幂等保证：t_user_joy_event 唯一键 (event_id, receiver_user_id)
     *    - 每一代上级 receiver 在入账前先插入事件明细 UserJoyEvent；
     *    - 若唯一键冲突（DuplicateKeyException），表示该 receiver 已经记过账 -> 本代直接跳过；
     *    - 只有插入成功，才允许更新总账/贡献汇总，杜绝重复触发导致的重复累加。
     *
     * B. 并发安全：对每个 receiverId 使用 LockUtil.getlock(receiverId) 做同步锁
     *    - 保证同一 receiver 的 t_user_joy / t_user_joy_contrib 的读改写是串行的；
     *    - 避免并发下 today_joy 跨天重置与累加、以及首次插入/更新产生竞态。
     *
     * today_joy 跨天重置规则：
     * - 使用 DateUtil.getTodayInt()（yyyyMMdd）判断是否跨天；
     * - 跨天仅清零 today_joy，并更新 today_date；total_joy、available_joy 不回退。
     *
     * @param triggerUserId 触发者（下级）用户ID
     * @param itemQuality   道具品质/等级（通常取 dic_item.quality）
     * @param eventId       事件唯一ID（强烈建议使用业务主键/流水号；避免时间戳碰撞）
     * @param sourceType    来源类型（FARM_HARVEST / FARM_UNLOCK / ...）
     */

    @Transactional
    public void distributeJoy(Long triggerUserId, int itemQuality, String eventId, String sourceType) {
        // 参数校验（保持你原有风格）
        if (triggerUserId == null || eventId == null || sourceType == null) {
            return;
        }
        if (itemQuality <= 0) {
            return;
        }

        // 读取配置
        String baseJson = managerConfigService.getString(Config.JOY_PER_LEVEL);
        String percentJson = managerConfigService.getString(Config.JOY_LEVEL_PERCENT);
        if (baseJson == null || percentJson == null) {
            return;
        }

        // 各等级基础欢乐值配置：JOY_PER_LEVEL（quality -> baseJoy）
        JSONObject baseMap = JSONObject.parseObject(baseJson);
        // 1~5 代分成比例：JOY_LEVEL_PERCENT（level -> percent）
        JSONObject percentMap = JSONObject.parseObject(percentJson);

        //baseJoy：按 quality 取基础欢乐值
        String qKey = String.valueOf(itemQuality);
        if (!baseMap.containsKey(qKey)) {
            // 配置没覆盖该品质
            return;
        }

        BigDecimal baseJoy = baseMap.getBigDecimal(qKey);
        if (baseJoy == null || baseJoy.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        // 与 DB decimal(18,2) 对齐，避免落库/表达式不一致
        baseJoy = baseJoy.setScale(2, RoundingMode.FLOOR);

        // 沿 parentId 链路向上最多 5 代
        Long currentUserId = triggerUserId;
        int todayInt = DateUtil.getTodayInt();

        for (int level = 1; level <= 5; level++) {

            User user = userCacheService.getUserInfoById(currentUserId);
            if (user == null || user.getParentId() == null || user.getParentId() <= 0) {
                break;
            }
            Long parentId = user.getParentId();

            // 读取该代分润比例（percent）
            // 说明：你 percentJson 是 {"1":30,"2":25,...}，fastjson 这里 getBigDecimal 会得到 30/25 之类
            BigDecimal rate = percentMap.getBigDecimal(String.valueOf(level));
            if (rate == null || rate.compareTo(BigDecimal.ZERO) <= 0) {
                currentUserId = parentId;
                continue;
            }

            // percent 字段落库建议为 int（与你表结构一致），这里安全取整
            // 注意：配置本身是整数百分比，若以后允许小数百分比，此处需要改为 decimal 存储
            int percentInt;
            try {
                percentInt = rate.intValue();
            } catch (Exception e) {
                currentUserId = parentId;
                continue;
            }
            if (percentInt <= 0) {
                currentUserId = parentId;
                continue;
            }

            // 计算该代实际获得的欢乐值：actualJoy = baseJoy * percent / 100（2位小数 FLOOR）
            BigDecimal actualJoy = baseJoy
                    .multiply(BigDecimal.valueOf(percentInt))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.FLOOR);

            if (actualJoy.compareTo(BigDecimal.ZERO) <= 0) {
                currentUserId = parentId;
                continue;
            }

            // 流水记录
            String calcExpr = baseJoy.toPlainString() + "*" + percentInt + "/100=" + actualJoy.toPlainString();
            String calcDesc = "from=" + triggerUserId
                    + " -> receiver=" + parentId
                    + ", level=" + level
                    + ", quality=" + itemQuality
                    + ", baseJoy=" + baseJoy.toPlainString()
                    + ", percent=" + percentInt + "%"
                    + ", joy=" + actualJoy.toPlainString()
                    + ", sourceType=" + sourceType
                    + ", eventId=" + eventId;

            UserJoyEvent event = new UserJoyEvent();
            event.setEventId(eventId);
            event.setReceiverUserId(parentId);
            event.setFromUserId(triggerUserId);
            event.setSourceType(sourceType);
            event.setLevel(level);
            event.setItemQuality(itemQuality);
            event.setBaseJoy(baseJoy);
            event.setPercent(percentInt);
            event.setCalcExpr(calcExpr);
            event.setCalcDesc(calcDesc);
            event.setJoyAmount(actualJoy);
            event.setCreateTime(new Date());
            try {
                userJoyEventService.insert(event);
            } catch (DuplicateKeyException dup) {
                // 已处理过该 (eventId, receiverId)：直接跳过该上级
                currentUserId = parentId;
                continue;
            }

            // 插入成功才允许记总账与贡献汇总，并对 receiverId 加锁保证并发正确
            synchronized (LockUtil.getlock(parentId)) {
                // 总账
                UserJoy parentJoy = userJoyService.findByUserId(parentId);
                if (parentJoy == null) {
                    parentJoy = new UserJoy();
                    parentJoy.setUserId(parentId);
                    parentJoy.setTotalJoy(BigDecimal.ZERO);
                    parentJoy.setAvailableJoy(BigDecimal.ZERO);
                    parentJoy.setTodayJoy(BigDecimal.ZERO);
                    parentJoy.setTodayDate(todayInt);
                    parentJoy.setCreateTime(new Date());
                    parentJoy.setUpdateTime(new Date());
                    userJoyService.insert(parentJoy);
                } else {
                    // 跨天：只重置 today_joy，不影响 total/available
                    if (parentJoy.getTodayDate() == null || parentJoy.getTodayDate() != todayInt) {
                        parentJoy.setTodayJoy(BigDecimal.ZERO);
                        parentJoy.setTodayDate(todayInt);
                    }
                    // 防御：避免历史数据为 null 导致 NPE
                    if (parentJoy.getTotalJoy() == null) parentJoy.setTotalJoy(BigDecimal.ZERO);
                    if (parentJoy.getAvailableJoy() == null) parentJoy.setAvailableJoy(BigDecimal.ZERO);
                    if (parentJoy.getTodayJoy() == null) parentJoy.setTodayJoy(BigDecimal.ZERO);
                }

                parentJoy.setTotalJoy(parentJoy.getTotalJoy().add(actualJoy));
                parentJoy.setAvailableJoy(parentJoy.getAvailableJoy().add(actualJoy));
                parentJoy.setTodayJoy(parentJoy.getTodayJoy().add(actualJoy));
                parentJoy.setUpdateTime(new Date());
                userJoyService.updateByUserId(parentJoy);

                // 贡献汇总：t_user_joy_contrib（receiver=上级, from=触发者）
                UserJoyContrib contrib = userJoyContribService.findOneByPair(parentId, triggerUserId);
                if (contrib == null) {
                    contrib = new UserJoyContrib();
                    contrib.setReceiverUserId(parentId);
                    contrib.setFromUserId(triggerUserId);
                    contrib.setTotalJoy(actualJoy);
                    contrib.setTodayJoy(actualJoy);
                    contrib.setTodayDate(todayInt);
                    contrib.setCreateTime(new Date());
                    contrib.setUpdateTime(new Date());
                    userJoyContribService.insert(contrib);
                } else {
                    if (contrib.getTodayDate() == null || contrib.getTodayDate() != todayInt) {
                        contrib.setTodayJoy(BigDecimal.ZERO);
                        contrib.setTodayDate(todayInt);
                    }
                    // 防御：避免历史数据为 null 导致 NPE
                    if (contrib.getTotalJoy() == null) contrib.setTotalJoy(BigDecimal.ZERO);
                    if (contrib.getTodayJoy() == null) contrib.setTodayJoy(BigDecimal.ZERO);

                    contrib.setTotalJoy(contrib.getTotalJoy().add(actualJoy));
                    contrib.setTodayJoy(contrib.getTodayJoy().add(actualJoy));
                    contrib.setUpdateTime(new Date());
                    userJoyContribService.updateByPair(contrib);
                }
            }

            // 进入下一代
            currentUserId = parentId;
        }
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


    /**
     * 获取用户 UserJoy 数据，如果不存在则初始化，如果跨天则重置 todayJoy
     */
    private UserJoy getAndCheckUserJoy(Long userId) {
        UserJoy userJoy = userJoyService.findByUserId(userId);
        int todayInt = DateUtil.getTodayInt();

        //用户没有欢乐值就初始化一体哦啊
        if (userJoy == null) {
            userJoy = new UserJoy();
            userJoy.setUserId(userId);
            userJoy.setTotalJoy(BigDecimal.ZERO);
            userJoy.setAvailableJoy(BigDecimal.ZERO);
            userJoy.setTodayJoy(BigDecimal.ZERO);
            userJoy.setTodayDate(todayInt);
            userJoy.setCreateTime(new Date());
            userJoy.setUpdateTime(new Date());
            userJoyService.insert(userJoy);
            return userJoy;
        } else {
            //如果是旧日期重置今日数据并更新
            if (userJoy.getTodayDate() == null || userJoy.getTodayDate() != todayInt) {
                userJoy.setTodayJoy(BigDecimal.ZERO);
                userJoy.setTodayDate(todayInt);
                userJoy.setUpdateTime(new Date());
                userJoyService.updateByUserId(userJoy);
            }
        }
        return userJoy;
    }


    /**
     * 判断 friendUserId 是否在 userId 的下级链路内（最多 levels 代）
     * 逻辑：从 friend 往上回溯 parentId，最多回溯 levels 次，能命中 userId 则允许。
     */
    private boolean isDownlineWithinLevels(Long friendUserId, Long userId, int levels) {
        if (friendUserId == null || userId == null || levels <= 0) {
            return false;
        }
        Long cur = friendUserId;
        for (int i = 0; i < levels; i++) {
            User u = userCacheService.getUserInfoById(cur);
            if (u == null || u.getParentId() == null || u.getParentId() <= 0) {
                return false;
            }
            if (u.getParentId().equals(userId)) {
                return true;
            }
            cur = u.getParentId();
        }
        return false;
    }

}



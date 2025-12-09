package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.UserFarmLand;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: lzx
 * @Create: 2025/12/8
 * @Version: V1.0
 * @Description: 用户农场土地表 Service
 * 核心职责：管理用户那 9 块地的状态（播种、收割、解锁）
 */
@Service
public class UserFarmLandService extends DaoService {

    public UserFarmLandService() {
        super("UserFarmLandMapper");
    }

    /**
     *  查询某个用户的所有土地列表
     * 业务场景：用户登录 getInfo 时，或者打开农场界面时，需要拉取全部 9 块地的状态。
     * 修正说明：原代码是 findOne，改为 findList，对应 XML 中的 findListByUserId
     */
    @Transactional(readOnly = true)
    public List<UserFarmLand> findListByUserId(Long userId) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        return findList("findListByUserId", params);
    }

    /**
     * 根据主键 ID 查询
     * 业务场景：一般用于后台管理查询，或者代码内部精确查找某条记录
     */
    @Transactional(readOnly = true)
    public UserFarmLand findOneById(Long id) {
        return (UserFarmLand) findOne("selectByPrimaryKey", id);
    }

    /**
     * 查询用户指定位置的一块地 (比如查询第 7 块地)
     * 业务场景：
     * 1. 播种前检查：这块地是不是空的？
     * 2. 收割前检查：这块地熟没熟？
     * 3. 购买前检查：这块地是不是已经买过了？
     */
    @Transactional(readOnly = true)
    public UserFarmLand findOneByUserAndIndex(Long userId, Integer landIndex) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("landIndex", landIndex);
        return (UserFarmLand) findOne("findOneByUserAndIndex", params);
    }

    /**
     * 购买/解锁土地 (对应 XML id="unlockLand")
     * 业务场景：玩家花费核心积分购买第 7、8、9 号地。
     * 逻辑：插入一条 seed_item_id = 0 的记录，代表“这块地归你了，虽然现在没种东西”。
     */
    @Transactional
    public void unlockLand(UserFarmLand cm) {
        getBaseDao().execute(mapperSpace, "unlockLand", cm);
    }

    /**
     * 普通新增 (很少直接用，一般用上面的 unlockLand 或下面的 plantLand)
     */
    @Transactional
    public void insertSelective(UserFarmLand cm) {
        getBaseDao().execute(mapperSpace, "insertSelective", cm);
    }

    /**
     * 普通更新 (不推荐用于收割，因为 MyBatis 默认不更新 NULL 值)
     */
    @Transactional
    public void updateByPrimaryKeySelective(UserFarmLand cm) {
        getBaseDao().execute(mapperSpace, "updateByPrimaryKeySelective", cm);
    }

    /**
     * 【核心】播种 (对应 XML id="plantLand")
     * 业务场景：玩家在某块地上种下种子。
     * 逻辑：使用了 Upsert (不存在则插入，存在则更新)。
     * 为什么这么做：第 1-6 号地也是第一次种才插入数据，第 7-9 号地是购买过的，直接更新。
     * 这个方法统一处理了这两种情况，非常方便。
     */
    @Transactional
    public void plantLand(UserFarmLand cm) {
        getBaseDao().execute(mapperSpace, "plantLand", cm);
    }

    /**
     * 【核心】收割/铲除 (对应 XML id="harvestLand")
     * 业务场景：作物成熟后收割，或者玩家强制铲除作物。
     * 逻辑：将 seed_item_id 置为 0，时间置空。
     * 重要：千万不能 Delete 这条记录！否则玩家花钱买的第 9 块地就“丢了”（变成未解锁）。
     */
    @Transactional
    public void harvestLand(Long userId, Integer landIndex) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("landIndex", landIndex);
        getBaseDao().execute(mapperSpace, "harvestLand", params);
    }

    /**
     * 统计数量 (可选)
     */
    @Transactional(readOnly = true)
    public int countByConditions(Map<String, Object> cond) {
        return (Integer) findOne("countByConditions", cond);
    }
}
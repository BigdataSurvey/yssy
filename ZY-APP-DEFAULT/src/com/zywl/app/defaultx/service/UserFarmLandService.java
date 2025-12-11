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
 * 管理用户  9 块地的状态（播种、收割、解锁）
 */
@Service
public class UserFarmLandService extends DaoService {

    public UserFarmLandService() {super("UserFarmLandMapper");}

    /**
     * 查询某个用户的所有土地列表
     */
    @Transactional(readOnly = true)
    public List<UserFarmLand> findListByUserId(Long userId) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        return findList("findListByUserId", params);
    }

    /**
     * 根据主键 ID 查询
     */
    @Transactional(readOnly = true)
    public UserFarmLand findOneById(Long id) {
        return (UserFarmLand) findOne("selectByPrimaryKey", id);
    }

    /**
     * 查询用户指定位置的一块地 (比如查询第 7 块地)
     * 播种前检查：这块地是不是空的？ 收割前检查：这块地熟没熟？ 购买前检查：这块地是不是已经买过了？
     */
    @Transactional(readOnly = true)
    public UserFarmLand findOneByUserAndIndex(Long userId, Integer landIndex) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("landIndex", landIndex);
        return (UserFarmLand) findOne("findOneByUserAndIndex", params);
    }

    /**
     * 播种插入、收割清空、解锁更新
     */
    @Transactional
    public void plantLand(UserFarmLand cm) {
        getBaseDao().execute(mapperSpace, "plantLand", cm);
    }

    /**
     * 统计数量
     */
    @Transactional(readOnly = true)
    public int countByConditions(Map<String, Object> cond) {
        return (Integer) findOne("countByConditions", cond);
    }
}
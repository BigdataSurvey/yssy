package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.UserFarmLand;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.zywl.app.defaultx.dbutil.DaoService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: lzx
 * @Create: 2025/12/8
 * @Version: V1.0
 * @Description: 用户农场土地表 Service
 * @Task:
 */
@Service
public class UserFarmLandService extends DaoService {
    public UserFarmLandService(String mapper) {
        super("UserFarmLandMapper");
    }


    /**
     * 根据UserId 查询用户农场土地表
     * **/
    @Transactional(readOnly = true)
    public UserFarmLand findOneByUserId(Long userId) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        return (UserFarmLand) findOne("findByUserId", params);
    }

    /**
     * 根据主键id 查询用户农场土地表
     * **/
    @Transactional(readOnly = true)
    public UserFarmLand findOneById(Long id) {
        return (UserFarmLand) findOne("selectByPrimaryKey", id);
    }


    /**
     * 新增土地状态
     */
    @Transactional
    public void insertSelective(UserFarmLand cm) {
        getBaseDao().execute(mapperSpace, "insertSelective", cm);
    }

    /**
     * 更新用户土地状态
     */
    @Transactional
    public void updateByPrimaryKeySelective(UserFarmLand cm) {
        getBaseDao().execute(mapperSpace, "updateByPrimaryKeySelective", cm);
    }

    /** 分页查询 */
    @Transactional(readOnly = true)
    public List<UserFarmLand> findByConditions(Map<String, Object> cond) {
        return findList("findByConditions", cond);
    }
    /** 统计总数 */
    @Transactional(readOnly = true)
    public int countByConditions(Map<String, Object> cond) {
        return (Integer) findOne("countByConditions", cond);
    }
}

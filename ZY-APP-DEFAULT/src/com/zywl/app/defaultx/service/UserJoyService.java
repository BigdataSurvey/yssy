package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.UserJoy;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.Map;
/**
 * @Author: lzx
 * @Create: 2025/12/14
 * @Version: V1.0
 * @Description: 用户欢乐值 Service，对应表 t_user_joy
 */

@Service
public class UserJoyService extends DaoService {
    public UserJoyService() {
        super("UserJoyMapper");
    }

    /**
     * 按 userId 查询用户欢乐值汇总
     */
    @Transactional(readOnly = true)
    public UserJoy findByUserId(Long userId) {
        return (UserJoy) findOne("findByUserId", userId);
    }

    /**
     * 新增一条记录
     */
    @Transactional
    public void insert(UserJoy userJoy) {
        getBaseDao().execute(mapperSpace, "insert", userJoy);
    }

    /**
     * 按 userId 更新
     */
    @Transactional
    public void updateByUserId(UserJoy userJoy) {
        getBaseDao().execute(mapperSpace, "updateByUserId", userJoy);
    }

    /**
     * 按条件统计数量
     */
    @Transactional(readOnly = true)
    public int countByConditions(Map<String, Object> cond) {
        return (Integer) findOne("countByConditions",
                cond != null ? cond : new HashMap<String, Object>());
    }
}

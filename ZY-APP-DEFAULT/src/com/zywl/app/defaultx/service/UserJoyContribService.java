package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.UserJoyContrib;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: lzx
 * @Create: 2025/12/xx
 * @Version: V1.0
 * @Description: 好友贡献欢乐值汇总表 Service ; t_user_joy_contrib
 * @Task:
 */
@Service
public class UserJoyContribService extends DaoService {

    public UserJoyContribService() {
        super("UserJoyContribMapper");
    }

    /**
     * 按 (receiverUserId, fromUserId) 查询
     */
    @Transactional(readOnly = true)
    public UserJoyContrib findOneByPair(Long receiverUserId, Long fromUserId) {
        Map<String, Object> cond = new HashMap<>();
        cond.put("receiverUserId", receiverUserId);
        cond.put("fromUserId", fromUserId);
        return (UserJoyContrib) findOne("findOneByPair", cond);
    }

    /**
     * 新增一条汇总记录
     */
    @Transactional
    public void insert(UserJoyContrib cm) {
        getBaseDao().execute(mapperSpace, "insert", cm);
    }

    /**
     * 按 (receiverUserId, fromUserId) 更新
     */
    @Transactional
    public void updateByPair(UserJoyContrib cm) {
        getBaseDao().execute(mapperSpace, "updateByPair", cm);
    }

    /**
     * 条件统计
     */
    @Transactional(readOnly = true)
    public int countByConditions(Map<String, Object> cond) {
        if (cond == null) {
            cond = new HashMap<>();
        }
        return (Integer) findOne("countByConditions", cond);
    }
}

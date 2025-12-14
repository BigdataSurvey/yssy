package com.zywl.app.defaultx.service;
import com.zywl.app.base.bean.UserJoyEvent;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description: 欢乐值事件明细 Service， t_user_joy_event
 */
@Service
public class UserJoyEventService extends DaoService {

    public UserJoyEventService() {
        super("UserJoyEventMapper");
    }

    /**
     * 按 (eventId, receiverUserId) 查询
     */
    @Transactional(readOnly = true)
    public UserJoyEvent findByEventAndReceiver(String eventId, Long receiverUserId) {
        Map<String, Object> cond = new HashMap<>();
        cond.put("eventId", eventId);
        cond.put("receiverUserId", receiverUserId);
        return (UserJoyEvent) findOne("findByEventAndReceiver", cond);
    }

    /**
     * 插入一条事件记录
     */
    @Transactional
    public void insert(UserJoyEvent event) {
        getBaseDao().execute(mapperSpace, "insert", event);
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

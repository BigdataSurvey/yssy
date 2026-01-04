package com.zywl.app.defaultx.service;
import com.zywl.app.base.bean.BountyTask;
import com.zywl.app.base.bean.UserFarmLand;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
/**
 * @Author: lzx
 * @Create: 2026-01-03
 * @Version: V1.0
 * @Description: 悬赏任务-任务配置表Service
 */
@Service
public class BountyTaskService extends DaoService {

    public BountyTaskService() {
        super("BountyTaskMapper");
    }

    public Long addTask(BountyTask task) {
        if (task == null) {
            return 0L;
        }
        Date now = new Date();
        if (task.getCreateTime() == null) {
            task.setCreateTime(now);
        }
        task.setUpdateTime(now);
        save(task);
        return task.getId();
    }

    public int updateTask(BountyTask task) {
        if (task == null || task.getId() == null) {
            return 0;
        }
        task.setUpdateTime(new Date());
        return update(task);
    }

    public BountyTask findById(Long id) {
        return (BountyTask) findOne("selectByPrimaryKey", id);
    }

    @SuppressWarnings("unchecked")
    public List<BountyTask> findListByConditions(Map<String, Object> params) {
        return findList("findListByConditions", params);
    }

    public int countByConditions(Map<String, Object> params) {
        Integer c = (Integer) findOne("countByConditions", params);
        return c == null ? 0 : c;
    }

    /**
     * 原子更新：剩余名额/参与人数/完成数（由MANAGER层业务保证参数合法）
     */
    public int updateCounts(Map<String, Object> params) {
        return getBaseDao().execute(mapperSpace, "updateCounts", params);
    }
}

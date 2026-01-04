package com.zywl.app.defaultx.service;
import com.zywl.app.base.bean.BountyTask;
import com.zywl.app.base.bean.BountyTaskOrder;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Author: lzx
 * @Create: 2026-01-03
 * @Version: V1.0
 * @Description: 悬赏任务-接单/提交明细Service
 */
@Service
public class BountyTaskOrderService extends DaoService {

    public BountyTaskOrderService() {
        super("BountyTaskOrderMapper");
    }

    public Long addOrder(BountyTaskOrder order) {
        if (order == null) {
            return 0L;
        }
        Date now = new Date();
        if (order.getCreateTime() == null) {
            order.setCreateTime(now);
        }
        order.setUpdateTime(now);
        save(order);
        return order.getId();
    }

    public int updateOrder(BountyTaskOrder order) {
        if (order == null || order.getId() == null) {
            return 0;
        }
        order.setUpdateTime(new Date());
        return update(order);
    }

    public BountyTaskOrder findById(Long id) {
        return (BountyTaskOrder) findOne("selectByPrimaryKey", id);
    }

    @SuppressWarnings("unchecked")
    public List<BountyTaskOrder> findListByConditions(Map<String, Object> params) {
        return findList("findListByConditions", params);

    }

    public int countByConditions(Map<String, Object> params) {
        Integer c = (Integer) findOne("countByConditions", params);
        return c == null ? 0 : c;
    }

    public int updateStatus(Map<String, Object> params) {
        return getBaseDao().execute(mapperSpace, "updateStatus", params);
    }
}

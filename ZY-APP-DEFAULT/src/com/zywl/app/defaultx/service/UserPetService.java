package com.zywl.app.defaultx.service;
import com.zywl.app.base.bean.UserPet;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * @Author: lzx
 * @Create: 2025/12/30
 * @Version: V1.0
 * @Description: 用户宠物明细Service
 */
@Service
public class UserPetService extends DaoService {

    public UserPetService() {
        super("UserPetMapper");
    }

    public int insert(UserPet userPet) {
        return getBaseDao().execute(mapperSpace,"insert", userPet);
    }

    public int batchInsert(List<UserPet> list) {
        if (list == null || list.isEmpty()) {
            return 0;
        }
        return getBaseDao().execute(mapperSpace,"batchInsert", list);
    }
    public List<UserPet> findListByUserId(Long userId) {
        return findList("findListByUserId", userId);
    }


    public int countByUserId(Long userId) {
        Object result = findOne("countByUserId", userId);
        return result == null ? 0 : (Integer) result;
    }
    public int saveOrUpdate(UserPet pet) {
        return getBaseDao().execute(mapperSpace, "saveOrUpdate", pet);
    }



    /**
     * 查询用户第一只狮子的购买时间（buy_time 最小值）。
     * 结算时用于将 last_settle_time 快速推进到“可能产生产出”的第一个整点，避免无意义循环。
     */
    public java.util.Date findFirstBuyTime(Long userId) {
        return (java.util.Date) findOne("findFirstBuyTime", userId);
    }

    /**
     * 按 buy_time 范围统计狮子数量（不拉全量）。
     * - startTime: buy_time >= startTime（可为空）
     * - endTime: buy_time < endTime（可为空）
     */
    public int countByUserIdAndBuyTimeRange(Long userId, java.util.Date startTime, java.util.Date endTime) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("startTime", startTime);
        params.put("endTime", endTime);
        Object result = findOne("countByUserIdAndBuyTimeRange", params);
        return result == null ? 0 : (Integer) result;
    }
}
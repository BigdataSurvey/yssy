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
     * 查询用户第一只狮子的购买时间
     */
    public java.util.Date findFirstBuyTime(Long userId) {
        return (java.util.Date) findOne("findFirstBuyTime", userId);
    }

    /**
     * 按 buy_time 范围统计狮子数量
     * - startTime: buy_time >= startTime
     * - endTime: buy_time < endTime
     */
    public int countByUserIdAndBuyTimeRange(Long userId, java.util.Date startTime, java.util.Date endTime) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("startTime", startTime);
        params.put("endTime", endTime);
        Object result = findOne("countByUserIdAndBuyTimeRange", params);
        return result == null ? 0 : (Integer) result;
    }

    /**
     * 批量统计用户的宠物数量（避免 N+1）
     */
    public Map<Long, Integer> countByUserIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return new HashMap<>();
        }
        Map<String, Object> params = new HashMap<>(4);
        params.put("userIds", userIds);

        List<Map<String, Object>> rows = findList("countByUserIds", params);
        Map<Long, Integer> map = new HashMap<>();
        if (rows != null) {
            for (Map<String, Object> r : rows) {
                if (r == null) continue;
                Object uidObj = r.get("userId");
                Object cntObj = r.get("cnt");
                if (uidObj == null || cntObj == null) continue;
                Long uid = Long.parseLong(String.valueOf(uidObj));
                Integer cnt = Integer.parseInt(String.valueOf(cntObj));
                map.put(uid, cnt);
            }
        }
        return map;
    }
}
package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.UserPetRecord;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: lzx
 * @Create: 2025/12/29
 * @Version: V1.0
 * @Description: 养宠用户明细Service
 * @Task:
 */
@Service
public class UserPetRecordService extends DaoService {

    public UserPetRecordService() {
        super("UserPetRecordMapper");
    }
    /** 分润记录类型 */
    public static final int RECORD_TYPE_DIVIDEND = 2;

    public int insert(UserPetRecord record) {
        return getBaseDao().execute(mapperSpace,"insert", record);
    }

    public UserPetRecord findOneByUk(UserPetRecord record) {
        return (UserPetRecord)findOne("findOneByUk", record);
    }

    public BigDecimal sumTodayDividend(Long userId) {
        BigDecimal v = (BigDecimal) findOne("sumTodayDividend", userId);
        return v == null ? BigDecimal.ZERO : v;
    }
    /**
     * 解锁贡献口径：累计分润且 level in (1,2)。
     */
    public BigDecimal sumDividendLevel12(Long userId) {
        BigDecimal v = (BigDecimal) findOne("sumDividendLevel12", userId);
        return v == null ? BigDecimal.ZERO : v;
    }

    /**
     * 按 UK 参数查询
     */
    public UserPetRecord findOneByUk(Long userId, Integer recordType, String recordKey,
                                     Long petId, Long fromUserId, Integer level) {
        UserPetRecord record = new UserPetRecord();
        record.setUserId(userId);
        record.setRecordType(recordType);
        record.setRecordKey(recordKey);
        record.setPetId(petId);
        record.setFromUserId(fromUserId);
        record.setLevel(level);
        return findOneByUk(record);
    }


    public BigDecimal sumTodayDividendByLevel(Long userId, Integer level) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("level", level);
        BigDecimal v = (BigDecimal) findOne("sumTodayDividendByLevel", params);
        return v == null ? BigDecimal.ZERO : v;
    }

    public BigDecimal sumTotalDividendByLevel(Long userId, Integer level) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("level", level);
        BigDecimal v = (BigDecimal) findOne("sumTotalDividendByLevel", params);
        return v == null ? BigDecimal.ZERO : v;
    }
    public BigDecimal sumTotalDividendByFromUserAndLevel(Long userId, Long fromUserId, Integer level) {
        Map<String, Object> params = new HashMap<>(8);
        params.put("userId", userId);
        params.put("fromUserId", fromUserId);
        params.put("level", level);
        params.put("recordType", RECORD_TYPE_DIVIDEND);
        return (BigDecimal) findOne("sumTotalDividendByFromUserAndLevel", params);
    }

    /**
     * 批量统计来自指定下级的累计分润贡献（避免 N+1）
     */
    public Map<Long, BigDecimal> sumTotalDividendByFromUserIdsAndLevel(Long userId, List<Long> fromUserIds, Integer level) {
        if (fromUserIds == null || fromUserIds.isEmpty()) {
            return new HashMap<>();
        }
        Map<String, Object> params = new HashMap<>(8);
        params.put("userId", userId);
        params.put("fromUserIds", fromUserIds);
        params.put("level", level);
        params.put("recordType", RECORD_TYPE_DIVIDEND);

        List<Map<String, Object>> rows = findList("sumTotalDividendByFromUserIdsAndLevel", params);
        Map<Long, BigDecimal> map = new HashMap<>();
        if (rows != null) {
            for (Map<String, Object> r : rows) {
                if (r == null) continue;
                Object fromIdObj = r.get("fromUserId");
                Object totalObj = r.get("totalAmount");
                if (fromIdObj == null) continue;
                Long fromId = Long.parseLong(String.valueOf(fromIdObj));
                BigDecimal total = totalObj == null ? BigDecimal.ZERO : new BigDecimal(String.valueOf(totalObj));
                map.put(fromId, total);
            }
        }
        return map;
    }
}

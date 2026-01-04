package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.UserPetRecord;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
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
     * 解锁贡献口径：累计分润（record_type=2）且 level in (1,2)。
     */
    public BigDecimal sumDividendLevel12(Long userId) {
        BigDecimal v = (BigDecimal) findOne("sumDividendLevel12", userId);
        return v == null ? BigDecimal.ZERO : v;
    }

    /**
     * 兼容调用方：按 UK 参数查询（用于 038001 结算/分润幂等）。
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
}

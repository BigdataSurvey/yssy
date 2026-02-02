package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.TVipGiftRecord;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;

/**
 * @Author: lzx
 * @Create: 2026-01-19
 * @Version: V1.0
 * @Description: VIP卡转赠记录Server (对应表: t_vip_gift_record)
 * @Task:
 */
@Service
public class TVipGiftRecordService extends DaoService {
    private static final Log logger = LogFactory.getLog(TVipGiftRecordService.class);

    public TVipGiftRecordService() {
        super("TVipGiftRecordMapper");
    }

    /**
     * 新增转赠记录
     */
    @Transactional(rollbackFor = Exception.class)
    public void addRecord(TVipGiftRecord record) {
        save(record);
    }

    /**
     * 根据主键查询
     */
    @Transactional(readOnly = true)
    public TVipGiftRecord findOneById(Long id) {
        return (TVipGiftRecord) findOne("selectByPrimaryKey", id);
    }

    /**
     * 根据流水号查询
     */
    @Transactional(readOnly = true)
    public TVipGiftRecord findByGiftNo(String giftNo) {
        return (TVipGiftRecord) findOne("findByGiftNo", giftNo);
    }

    /**
     * 查询记录列表 (支持分页、查我发出的、查我收到的)
     */
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<TVipGiftRecord> findListByConditions(Map<String, Object> params) {
        return findList("findListByConditions", params);
    }

    /**
     * 统计数量
     */
    @Transactional(readOnly = true)
    public int countByConditions(Map<String, Object> cond) {
        Integer c= (Integer) findOne("countByConditions", cond);

        return c == null ? 0 : c;
    }

}
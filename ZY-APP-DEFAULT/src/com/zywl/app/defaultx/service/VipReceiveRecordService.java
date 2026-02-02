package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.VipReceiveRecord;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Author: lzx
 * @Create: 2026-01-19
 * @Version: V1.0
 * @Description: VIP每日领取记录Server (对应表: r_vip_receive_record)
 * @Task:
 */
@Service
public class VipReceiveRecordService extends DaoService {

    public VipReceiveRecordService() {
        super("VipReceiveRecordMapper");
    }

    /**
     * 新增领取记录
     */
    @Transactional(rollbackFor = Exception.class)
    public void addVipReceiveRecord(VipReceiveRecord vb) {
        save(vb);
    }

    /**
     * 更新领取记录
     */
    public int updateOrder(VipReceiveRecord vb) {
        vb.setUpdateTime(new Date());
        return update(vb);
    }

    /**
     * 根据ID查询
     * **/
    public VipReceiveRecord findById(Long id) {
        return (VipReceiveRecord) findOne("selectByPrimaryKey", id);
    }

    /**
     * 列表查询
     * **/
    @SuppressWarnings("unchecked")
    public List<VipReceiveRecord> findListByConditions(Map<String, Object> params) {
        return findList("findListByConditions", params);
    }

    /**
     * 统计
     * **/
    public int countByConditions(Map<String, Object> params) {
        Integer c = (Integer) findOne("countByConditions", params);
        return c == null ? 0 : c;
    }
}

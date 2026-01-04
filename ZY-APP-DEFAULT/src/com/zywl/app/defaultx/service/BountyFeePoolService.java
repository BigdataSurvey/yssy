package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.BountyFeePool;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: lzx
 * @Create: 2026-01-03
 * @Version: V1.0
 * @Description: 悬赏任务-手续费奖池 Service
 */
@Service
public class BountyFeePoolService extends DaoService {

    public static final Long DEFAULT_ID = 1L;

    public BountyFeePoolService() {
        super("BountyFeePoolMapper");
    }

    @Transactional(readOnly = true)
    public BountyFeePool findOne() {
        return (BountyFeePool) findOne("selectByPrimaryKey", DEFAULT_ID);
    }

    /**
     * 初始化奖池行（若已存在则忽略）
     */
    @Transactional
    public void initIfAbsent() {
        getBaseDao().execute(mapperSpace, "insertIgnore", DEFAULT_ID);
    }

    /**
     * 奖池累加（deltaCents 可为正负；业务层保证不出现非法扣减）
     */
    @Transactional
    public int addPoolCents(long deltaCents) {
        Map<String, Object> params = new HashMap<>();
        params.put("id", DEFAULT_ID);
        params.put("deltaCents", deltaCents);
        return (Integer) getBaseDao().execute(mapperSpace, "addPoolCents", params);
    }
}

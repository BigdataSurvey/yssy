package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.UserIncome;
import com.zywl.app.base.bean.VipReceiveRecord;
import com.zywl.app.base.bean.ZytRecord;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.apache.commons.collections4.map.HashedMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class ZytRecordService extends DaoService {
    public ZytRecordService() {
        super("ZytRecordMapper");
    }

    @Transactional
    public int batchUpdate(List<ZytRecord> zytRecords){
        int a = execute("batchUpdate",zytRecords);
        if(a<1){
            throwExp("更新失败");
        }
        return a;
    }

    @Transactional
    public int batchInsert(List<ZytRecord> zytRecords){
        int a = execute("batchInsert",zytRecords);
        if(a<1){
            throwExp("新增失败");
        }
        return a;
    }

    public List<ZytRecord> findzytRecordByUserId(Long userId) {
        Map<String,Object> params = new HashedMap<>();
        params.put("userId", userId);
        return findByConditions(params);
    }

}

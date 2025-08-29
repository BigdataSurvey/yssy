package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.jingang.BellRecord;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service
public class KongKimBellService extends DaoService {


    public KongKimBellService() {super("KongKimBellMapper");}


    private static final Log logger = LogFactory.getLog(KongKimBellService.class);

    public List<BellRecord> findByUserId(Long userId){
        Map<String, Object> params = new HashedMap<String, Object>();
        params.put("userId", userId);
        List<BellRecord> findByUserId = findList("findByUserId", params);
        return findByUserId;
    }

    public BellRecord addKongkimBell(BellRecord bellRecord) {
        save(bellRecord);
        return bellRecord;
    }
}

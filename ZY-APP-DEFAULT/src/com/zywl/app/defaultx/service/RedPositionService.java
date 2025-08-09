package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.hongbao.RecordSheet;
import com.zywl.app.base.bean.hongbao.RedPosition;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class RedPositionService extends DaoService {
    public RedPositionService() {
        super("RedPositionMapper");
    }
    private static final Log logger = LogFactory.getLog(RedEnvelopeService.class);
    @Override
    protected Log logger() {
        return logger;
    }





    public RedPosition findByUserId(Long userId){
        Map<String,Object> map = new HashMap<>();
        map.put("userId",userId);
        RedPosition findByUserId = (RedPosition) findOne("findByUserId", map);
        if(findByUserId==null){
            findByUserId = new RedPosition();
            findByUserId.setUserId(userId);
            findByUserId.setCount1(0L);
            findByUserId.setCount2(0L);
            findByUserId.setCount3(0L);
            findByUserId.setCount4(0L);
            save(findByUserId);
        }
        return findByUserId;
    }

    @Transactional
    public void addCountByUserId(Long userId, BigDecimal amount,int count){
        Map<String,Object> map = new HashMap<>();
        map.put("userId",userId);
        if (amount.compareTo(BigDecimal.TEN)==0){
            map.put("count1",count);
        }
        if (amount.compareTo(new BigDecimal("20"))==0){
            map.put("count2",count);
        }
        if (amount.compareTo(new BigDecimal("50"))==0){
            map.put("count3",count);
        }
        if (amount.compareTo(new BigDecimal("100"))==0){
            map.put("count4",count);
        }
        execute("addCountByUserId",map);
    }

    @Transactional
    public void updatePosition(RedPosition redPosition){
        execute("updatePosition",redPosition);
    }
}

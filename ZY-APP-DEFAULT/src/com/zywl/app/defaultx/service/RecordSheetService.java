package com.zywl.app.defaultx.service;


import com.zywl.app.base.bean.hongbao.RecordSheet;
import com.zywl.app.base.bean.hongbao.RedEnvelope;
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
public class RecordSheetService extends DaoService {


    private static final Log logger = LogFactory.getLog(RecordSheetService.class);





    public RecordSheetService() {
        super("RecordSheetMapper");
    }



    @Transactional
    public Long addRecord(Long userId, String orderNo, BigDecimal amount,String name,Long redId,int isBoom){
        RecordSheet recordSheet = new RecordSheet();
        recordSheet.setAmount(amount);
        recordSheet.setCreateTime(new Date());
        recordSheet.setOrderNo(orderNo);
        recordSheet.setUserId(userId);
        recordSheet.setIsBoom(isBoom);
        recordSheet.setRedId(redId);
        recordSheet.setName(name);
        save(recordSheet);
        return recordSheet.getId();
    }


    public List<RecordSheet> findByRedId(Long redId){
        Map<String,Object> map = new HashMap<>();
        map.put("redId",redId);
        return findList("findByRedId",map);
    }

    public List<RecordSheet> findAllRecordSheet() {
        return findAll();
    }


    @Override
    protected Log logger() {
        return logger;
    }




}

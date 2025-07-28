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
    public RecordSheet addRecord(Long userId, String orderNo, BigDecimal amount,String name,Long redId,int isBoom,String headImg,BigDecimal redAmount){
        RecordSheet recordSheet = new RecordSheet();
        recordSheet.setAmount(amount);
        recordSheet.setCreateTime(new Date());
        recordSheet.setOrderNo(orderNo);
        recordSheet.setUserId(userId);
        recordSheet.setIsBoom(isBoom);
        recordSheet.setUpdateTime(new Date());
        recordSheet.setRemark("领取红包");
        recordSheet.setState("成功");
        recordSheet.setRedId(redId);
        recordSheet.setHeadImg(headImg);
        recordSheet.setName(name);
        recordSheet.setRedAmount(redAmount);
        save(recordSheet);
        return recordSheet;
    }


    public List<RecordSheet> findByRedId(Long redId){
        Map<String,Object> map = new HashMap<>();
        map.put("redId",redId);
        return findList("findByRedId",map);
    }

    public List<RedEnvelope> findQueryRedPacket(Long userId, int page, int num) {
        Map<String,Object> map = new HashMap<>();
        map.put("userId",userId);
        map.put("start",page*num);
        map.put("limit",num);
        return findList("findByRedUserId",map);
    }
    public List<RecordSheet> findAllRecordSheet() {
        return findAll();
    }


    @Override
    protected Log logger() {
        return logger;
    }






}

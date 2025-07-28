package com.zywl.app.defaultx.service;


import com.zywl.app.base.bean.hongbao.RedEnvelope;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RedEnvelopeService extends DaoService {


    private static final Log logger = LogFactory.getLog(RedEnvelopeService.class);
    private Long userId;


    public RedEnvelopeService() {
        super("RedEnvenlopeMapper");
    }

    @Transactional
    public void updateRed(RedEnvelope redEnvelope){
        execute("updateRed",redEnvelope);
    }

    @Transactional
    public Long saveRedEnvelope(RedEnvelope redEnvelope){
        save(redEnvelope);
        return redEnvelope.getId();
    }


    @Override
    protected Log logger() {
        return logger;
    }


    public List<RedEnvelope> findAllRedEnvelope() {
        return findAll();
    }


    public List<RedEnvelope> findQueryRedRecord(Long userId,int page,int num) {
        Map<String,Object> map = new HashMap<>();
        map.put("userId",userId);
        map.put("start",page*num);
        map.put("limit",num);
        return findList("findByUserId",map);
    }






}

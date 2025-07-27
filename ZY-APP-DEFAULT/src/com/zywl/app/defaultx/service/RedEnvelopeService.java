package com.zywl.app.defaultx.service;


import com.zywl.app.base.bean.hongbao.RedEnvelope;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class RedEnvelopeService extends DaoService {


    private static final Log logger = LogFactory.getLog(RedEnvelopeService.class);





    public RedEnvelopeService() {
        super("RedEnvenlopeMapper");
    }


    @Override
    protected Log logger() {
        return logger;
    }


    public List<RedEnvelope> findAllRedEnvelope() {
        return findAll();
    }

    public <T> Optional<T> findById(Long redId) {
        return Optional.empty();
    }

    public <T> Set<String> findById(Long redId, Set<T> ts) {
        return java.util.Collections.emptySet();
    }

    public void findAllRedEnvelope(Long userId, BigDecimal amount) {
    }
}

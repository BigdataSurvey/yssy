package com.zywl.app.defaultx.service;

import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.InvestDetail;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.apache.commons.collections4.map.HashedMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;


@Service
public class InvestDetailService extends DaoService {

    public InvestDetailService() {
        super("InvestDetailMapper");
    }
    @Transactional
    public long addInvestDetail(Long userId, Integer betAmount ) {
        InvestDetail investDetail = new InvestDetail();
        investDetail.setUserId(userId);
        investDetail.setInvestNumber(betAmount);
        investDetail.setInvestDate(new Date());
        investDetail.setEndDate(DateUtil.getDateByDay(60));
        investDetail.setInvestSealStatus(0);
        investDetail.setUnReceive(BigDecimal.ZERO);
        save(investDetail);
        return investDetail.getId();
    }

    public List<InvestDetail> findInvestDetail(JSONObject params) {
        Map<String, Object> param = new HashedMap<>();
        param.put("userId", params.getLong("userId"));
        param.put("investSealStatus", 0);
        return findList("findInvestDetail", param);
    }

    public long updateInvestDetail(InvestDetail investDetail) {
        return update(investDetail);
    }

    public long batchUpdateInvestDetail(List<InvestDetail> investDetailList) {

        return execute("batchUpdateInvestDetail",investDetailList);
    }

    public List<InvestDetail> getReceiveRecord(JSONObject params) {
        Map<String, Object> param = new HashedMap<>();
        param.put("userId", params.getLong("userId"));
        return findList("getReceiveRecord", param);

    }
}

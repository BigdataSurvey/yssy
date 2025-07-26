package com.zywl.app.base.bean;

import com.zywl.app.base.BaseBean;

import java.math.BigDecimal;

public class PitUserParent extends BaseBean {

    private Integer userId;

    private Integer pitParentId;

    private Integer pitGrandfaId;

    private BigDecimal createParentAmount;

    private BigDecimal createGrandfaAmount;

    public PitUserParent(Integer userId, Integer pitParentId, Integer pitGrandfaId, BigDecimal createParentAmount, BigDecimal createGrandfaAmount) {
        this.userId = userId;
        this.pitParentId = pitParentId;
        this.pitGrandfaId = pitGrandfaId;
        this.createParentAmount = createParentAmount;
        this.createGrandfaAmount = createGrandfaAmount;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getPitParentId() {
        return pitParentId;
    }

    public void setPitParentId(Integer pitParentId) {
        this.pitParentId = pitParentId;
    }

    public Integer getPitGrandfaId() {
        return pitGrandfaId;
    }

    public void setPitGrandfaId(Integer pitGrandfaId) {
        this.pitGrandfaId = pitGrandfaId;
    }

    public BigDecimal getCreateParentAmount() {
        return createParentAmount;
    }

    public void setCreateParentAmount(BigDecimal createParentAmount) {
        this.createParentAmount = createParentAmount;
    }

    public BigDecimal getCreateGrandfaAmount() {
        return createGrandfaAmount;
    }

    public void setCreateGrandfaAmount(BigDecimal createGrandfaAmount) {
        this.createGrandfaAmount = createGrandfaAmount;
    }
}

package com.zywl.app.base.bean;

import com.zywl.app.base.BaseBean;

import java.math.BigDecimal;

public class PitUserParent extends BaseBean {

    private Long userId;

    private Long pitParentId;

    private Long pitGrandfaId;

    private int createParentAmount;

    private int createGrandfaAmount;


    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getPitParentId() {
        return pitParentId;
    }

    public void setPitParentId(Long pitParentId) {
        this.pitParentId = pitParentId;
    }

    public Long getPitGrandfaId() {
        return pitGrandfaId;
    }

    public void setPitGrandfaId(Long pitGrandfaId) {
        this.pitGrandfaId = pitGrandfaId;
    }

    public int getCreateParentAmount() {
        return createParentAmount;
    }

    public void setCreateParentAmount(int createParentAmount) {
        this.createParentAmount = createParentAmount;
    }

    public int getCreateGrandfaAmount() {
        return createGrandfaAmount;
    }

    public void setCreateGrandfaAmount(int createGrandfaAmount) {
        this.createGrandfaAmount = createGrandfaAmount;
    }
}

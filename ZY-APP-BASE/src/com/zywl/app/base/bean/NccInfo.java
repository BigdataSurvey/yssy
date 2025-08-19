package com.zywl.app.base.bean;

import com.zywl.app.base.BaseBean;

import java.math.BigDecimal;

public class NccInfo extends BaseBean {

    private Integer id;
    private Integer userId;
    private BigDecimal nccHeartAmount;
    private BigDecimal xqSealNumber;
    private Integer cycle;
    private BigDecimal getYyq;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public BigDecimal getNccHeartAmount() {
        return nccHeartAmount;
    }

    public void setNccHeartAmount(BigDecimal nccHeartAmount) {
        this.nccHeartAmount = nccHeartAmount;
    }

    public BigDecimal getXqSealNumber() {
        return xqSealNumber;
    }

    public void setXqSealNumber(BigDecimal xqSealNumber) {
        this.xqSealNumber = xqSealNumber;
    }

    public Integer getCycle() {
        return cycle;
    }

    public void setCycle(Integer cycle) {
        this.cycle = cycle;
    }

    public BigDecimal getGetYyq() {
        return getYyq;
    }

    public void setGetYyq(BigDecimal getYyq) {
        this.getYyq = getYyq;
    }

    public NccInfo() {
    }

    public NccInfo(Integer id, Integer userId, BigDecimal nccHeartAmount, BigDecimal xqSealNumber, Integer cycle, BigDecimal getYyq) {
        this.id = id;
        this.userId = userId;
        this.nccHeartAmount = nccHeartAmount;
        this.xqSealNumber = xqSealNumber;
        this.cycle = cycle;
        this.getYyq = getYyq;
    }
}

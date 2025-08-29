package com.zywl.app.base.bean;

import com.zywl.app.base.BaseBean;

import java.math.BigDecimal;

public class UserIncome extends BaseBean {

    private Integer userId;
    private BigDecimal receiveIncome;
    private BigDecimal unreceiveIncome;
    private BigDecimal beginTime;
    private BigDecimal endTime;

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public BigDecimal getReceiveIncome() {
        return receiveIncome;
    }

    public void setReceiveIncome(BigDecimal receiveIncome) {
        this.receiveIncome = receiveIncome;
    }

    public BigDecimal getUnreceiveIncome() {
        return unreceiveIncome;
    }

    public void setUnreceiveIncome(BigDecimal unreceiveIncome) {
        this.unreceiveIncome = unreceiveIncome;
    }

    public BigDecimal getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(BigDecimal beginTime) {
        this.beginTime = beginTime;
    }

    public BigDecimal getEndTime() {
        return endTime;
    }

    public void setEndTime(BigDecimal endTime) {
        this.endTime = endTime;
    }
}

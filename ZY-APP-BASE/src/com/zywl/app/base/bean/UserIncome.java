package com.zywl.app.base.bean;

import com.zywl.app.base.BaseBean;

import java.math.BigDecimal;
import java.util.Date;

public class UserIncome extends BaseBean {

    private Integer userId;
    private BigDecimal receiveIncome;
    private BigDecimal unreceiveIncome;
    private Integer status;
    private Date beginTime;
    private Date endTime;
    private Date crteTime;
    private Date updtTime;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getBeginTime() {
        return beginTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setBeginTime(Date beginTime) {
        this.beginTime = beginTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Date getCrteTime() {
        return crteTime;
    }

    public void setCrteTime(Date crteTime) {
        this.crteTime = crteTime;
    }

    public Date getUpdtTime() {
        return updtTime;
    }

    public void setUpdtTime(Date updtTime) {
        this.updtTime = updtTime;
    }

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
}

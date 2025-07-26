package com.zywl.app.base.bean;

import com.zywl.app.base.BaseBean;

import java.math.BigDecimal;
import java.util.Date;

public class PitRecord extends BaseBean {
    private Integer id;

    private Integer userId;

    private String orderNo;

    private Date receiveTime;

    private Integer pitId;

    private Integer number;

    private Date crteTime;

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

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public Date getReceiveTime() {
        return receiveTime;
    }

    public void setReceiveTime(Date receiveTime) {
        this.receiveTime = receiveTime;
    }

    public Integer getPitId() {
        return pitId;
    }

    public void setPitId(Integer pitId) {
        this.pitId = pitId;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public Date getCrteTime() {
        return crteTime;
    }

    public void setCrteTime(Date crteTime) {
        this.crteTime = crteTime;
    }

    public PitRecord() {
    }

    public PitRecord(Integer id, Integer userId, String orderNo, Date receiveTime, Integer pitId, Integer number, Date crteTime) {
        this.id = id;
        this.userId = userId;
        this.orderNo = orderNo;
        this.receiveTime = receiveTime;
        this.pitId = pitId;
        this.number = number;
        this.crteTime = crteTime;
    }
}

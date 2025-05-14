package com.zywl.app.base.bean.card;

import com.zywl.app.base.BaseBean;

import java.math.BigDecimal;
import java.util.Date;

public class ReceivePoolRecord extends BaseBean {

    private Long id;

    private Long userId;

    private String orderNo;

    private BigDecimal receiveAmount;

    private String day;

    private BigDecimal todayPool;

    private int myNumber;

    private int allNumber;
    private Date receiveTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public BigDecimal getReceiveAmount() {
        return receiveAmount;
    }

    public void setReceiveAmount(BigDecimal receiveAmount) {
        this.receiveAmount = receiveAmount;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public BigDecimal getTodayPool() {
        return todayPool;
    }

    public void setTodayPool(BigDecimal todayPool) {
        this.todayPool = todayPool;
    }


    public Date getReceiveTime() {
        return receiveTime;
    }

    public void setReceiveTime(Date receiveTime) {
        this.receiveTime = receiveTime;
    }

    public int getMyNumber() {
        return myNumber;
    }

    public void setMyNumber(int myNumber) {
        this.myNumber = myNumber;
    }

    public int getAllNumber() {
        return allNumber;
    }

    public void setAllNumber(int allNumber) {
        this.allNumber = allNumber;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }
}

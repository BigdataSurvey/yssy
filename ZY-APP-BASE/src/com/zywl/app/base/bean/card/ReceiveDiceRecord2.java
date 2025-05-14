package com.zywl.app.base.bean.card;

import com.zywl.app.base.BaseBean;

import java.math.BigDecimal;
import java.util.Date;

public class ReceiveDiceRecord2 extends BaseBean {

    private Long id;

    private Long userId;

    private BigDecimal amount;

    private Date receiveTime;

    private String orderNo;

    private String weekDiceOrder;

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

    public Date getReceiveTime() {
        return receiveTime;
    }

    public void setReceiveTime(Date receiveTime) {
        this.receiveTime = receiveTime;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getWeekDiceOrder() {
        return weekDiceOrder;
    }

    public void setWeekDiceOrder(String weekDiceOrder) {
        this.weekDiceOrder = weekDiceOrder;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}

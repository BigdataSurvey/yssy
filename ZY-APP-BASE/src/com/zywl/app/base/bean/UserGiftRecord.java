package com.zywl.app.base.bean;

import com.zywl.app.base.BaseBean;

import java.math.BigDecimal;
import java.util.Date;

public class UserGiftRecord extends BaseBean {

    private long id;
    private long userId;
    private BigDecimal price;
    private String  orderNo;
    private long number;
    private long type;
    private Date createTime;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public long getNumber() {
        return number;
    }

    public void setNumber(long number) {
        this.number = number;
    }

    public long getType() {
        return type;
    }

    public void setType(long type) {
        this.type = type;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public UserGiftRecord() {
    }

    public UserGiftRecord(long id, long userId, BigDecimal price, String orderNo, long number, long type, Date createTime) {
        this.id = id;
        this.userId = userId;
        this.price = price;
        this.orderNo = orderNo;
        this.number = number;
        this.type = type;
        this.createTime = createTime;
    }
}

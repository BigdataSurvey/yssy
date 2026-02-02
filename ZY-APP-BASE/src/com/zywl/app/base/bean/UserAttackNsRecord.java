package com.zywl.app.base.bean;

import com.zywl.app.base.BaseBean;

import java.math.BigDecimal;
import java.util.Date;

public class UserAttackNsRecord extends BaseBean {

    private Long id;

    private Long userId;

    private BigDecimal amount;

    private String orderNo;

    private Long nsId;

    private Integer round;

    private Date createTime;

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

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Long getNsId() {
        return nsId;
    }

    public void setNsId(Long nsId) {
        this.nsId = nsId;
    }

    public Integer getRound() {
        return round;
    }

    public void setRound(Integer round) {
        this.round = round;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }
}

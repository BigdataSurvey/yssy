package com.zywl.app.base.bean;

import cn.hutool.core.date.DateTime;

import java.math.BigDecimal;
import java.util.Date;

public class DgsBetRecord {

    private long id;

    private long userId;

    private Integer monsterId;

    private String orderNo;

    private BigDecimal betAmount;

    private BigDecimal profit;

    private long status;

    private Date createTime;

    private Date updateTime;

    public Integer getMonsterId() {
        return monsterId;
    }

    public void setMonsterId(Integer monsterId) {
        this.monsterId = monsterId;
    }

    public DgsBetRecord() {
    }

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

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public BigDecimal getBetAmount() {
        return betAmount;
    }

    public void setBetAmount(BigDecimal betAmount) {
        this.betAmount = betAmount;
    }

    public BigDecimal getProfit() {
        return profit;
    }

    public void setProfit(BigDecimal profit) {
        this.profit = profit;
    }

    public long getStatus() {
        return status;
    }

    public void setStatus(long status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public DgsBetRecord(long id, long userId, Integer monsterId, String orderNo, BigDecimal betAmount, BigDecimal profit, long status, Date createTime, Date updateTime) {
        this.id = id;
        this.userId = userId;
        this.monsterId = monsterId;
        this.orderNo = orderNo;
        this.betAmount = betAmount;
        this.profit = profit;
        this.status = status;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }
}

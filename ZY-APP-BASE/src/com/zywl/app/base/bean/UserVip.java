package com.zywl.app.base.bean;

import java.math.BigDecimal;
import java.util.Date;

public class UserVip {

    private long id;
    private long userId;
    private long vipLevel;

    private long rank;
    private BigDecimal rechargeAmount;
    private Date createTime;
    private Date updateTime;


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

    public long getVipLevel() {
        return vipLevel;
    }

    public void setVipLevel(long vipLevel) {
        this.vipLevel = vipLevel;
    }

    public BigDecimal getRechargeAmount() {
        return rechargeAmount;
    }

    public void setRechargeAmount(BigDecimal rechargeAmount) {
        this.rechargeAmount = rechargeAmount;
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

    public UserVip(long id, long userId, long vipLevel, BigDecimal rechargeAmount, Date createTime, Date updateTime) {
        this.id = id;
        this.userId = userId;
        this.vipLevel = vipLevel;
        this.rechargeAmount = rechargeAmount;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    public long getRank() {
        return rank;
    }

    public void setRank(long rank) {
        this.rank = rank;
    }

    public UserVip() {
    }
}

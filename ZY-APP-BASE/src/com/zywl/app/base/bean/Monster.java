package com.zywl.app.base.bean;

import java.math.BigDecimal;
import java.util.Date;

public class Monster {

    private long id;

    private long userId;

    private long monsterType;

    private long currBlood;

    private BigDecimal profit;

    private long dieStatus;

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

    public long getMonsterType() {
        return monsterType;
    }

    public void setMonsterType(long monsterType) {
        this.monsterType = monsterType;
    }

    public long getCurrBlood() {
        return currBlood;
    }

    public void setCurrBlood(long currBlood) {
        this.currBlood = currBlood;
    }

    public BigDecimal getProfit() {
        return profit;
    }

    public void setProfit(BigDecimal profit) {
        this.profit = profit;
    }

    public long getDieStatus() {
        return dieStatus;
    }

    public void setDieStatus(long dieStatus) {
        this.dieStatus = dieStatus;
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

    public Monster() {
    }

    public Monster(long id, long userId, long monsterType, long currBlood, BigDecimal profit, long dieStatus, Date createTime, Date updateTime) {
        this.id = id;
        this.userId = userId;
        this.monsterType = monsterType;
        this.currBlood = currBlood;
        this.profit = profit;
        this.dieStatus = dieStatus;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }
}

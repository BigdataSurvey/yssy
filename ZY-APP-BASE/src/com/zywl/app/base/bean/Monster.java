package com.zywl.app.base.bean;

import java.math.BigDecimal;
import java.util.Date;

public class Monster {

    private Integer id;

    private Integer userId;

    private Integer monsterType;

    private Integer currBlood;

    private BigDecimal profit;
    private BigDecimal betAmount;

    private long dieStatus;

    private Date createTime;

    private Date updateTime;

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

    public Integer getMonsterType() {
        return monsterType;
    }

    public void setMonsterType(Integer monsterType) {
        this.monsterType = monsterType;
    }

    public Integer getCurrBlood() {
        return currBlood;
    }

    public void setCurrBlood(Integer currBlood) {
        this.currBlood = currBlood;
    }

    public BigDecimal getProfit() {
        return profit;
    }

    public void setProfit(BigDecimal profit) {
        this.profit = profit;
    }

    public BigDecimal getBetAmount() {
        return betAmount;
    }

    public void setBetAmount(BigDecimal betAmount) {
        this.betAmount = betAmount;
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

    public Monster(Integer id, Integer userId, Integer monsterType, Integer currBlood, BigDecimal profit, BigDecimal betAmount, long dieStatus, Date createTime, Date updateTime) {
        this.id = id;
        this.userId = userId;
        this.monsterType = monsterType;
        this.currBlood = currBlood;
        this.profit = profit;
        this.betAmount = betAmount;
        this.dieStatus = dieStatus;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }
}

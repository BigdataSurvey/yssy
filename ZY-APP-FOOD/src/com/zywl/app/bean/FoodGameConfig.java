package com.zywl.app.bean;

import java.math.BigDecimal;

public class FoodGameConfig {
    private Integer readyDelayTime;
    private Integer startDelayTime;
    private Integer endDelayTime;
    private Integer minPlayer;
    private Integer maxPlayer;
    private Integer maxRound;
    private Integer costType;
    private BigDecimal costSit;
    private BigDecimal commission;

    public Integer getReadyDelayTime() {
        return readyDelayTime;
    }

    public void setReadyDelayTime(Integer readyDelyTime) {
        this.readyDelayTime = readyDelyTime;
    }

    public Integer getStartDelayTime() {
        return startDelayTime;
    }

    public void setStartDelayTime(Integer startDelyTime) {
        this.startDelayTime = startDelyTime;
    }

    public Integer getEndDelayTime() {
        return endDelayTime;
    }

    public void setEndDelayTime(Integer endDelayTime) {
        this.endDelayTime = endDelayTime;
    }

    public Integer getMinPlayer() {
        return minPlayer;
    }

    public void setMinPlayer(Integer minPlayer) {
        this.minPlayer = minPlayer;
    }

    public Integer getMaxPlayer() {
        return maxPlayer;
    }

    public void setMaxPlayer(Integer maxPlayer) {
        this.maxPlayer = maxPlayer;
    }

    public Integer getMaxRound() {
        return maxRound;
    }

    public void setMaxRound(Integer maxRound) {
        this.maxRound = maxRound;
    }

    public Integer getCostType() {
        return costType;
    }

    public void setCostType(Integer costType) {
        this.costType = costType;
    }

    public BigDecimal getCostSit() {
        return costSit;
    }

    public void setCostSit(BigDecimal costSit) {
        this.costSit = costSit;
    }

    public BigDecimal getCommission() {
        return commission;
    }

    public void setCommission(BigDecimal commission) {
        this.commission = commission;
    }
}

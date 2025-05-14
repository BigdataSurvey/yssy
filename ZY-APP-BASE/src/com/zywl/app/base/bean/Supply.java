package com.zywl.app.base.bean;

import com.zywl.app.base.BaseBean;

import java.math.BigDecimal;

public class Supply extends BaseBean {

    private Long id;

    private int type;

    private String reward;

    private int rate;

    private int quality;

    public int getQuality() {
        return quality;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }

    private BigDecimal recycleNumber;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getReward() {
        return reward;
    }

    public void setReward(String reward) {
        this.reward = reward;
    }

    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    public BigDecimal getRecycleNumber() {
        return recycleNumber;
    }

    public void setRecycleNumber(BigDecimal recycleNumber) {
        this.recycleNumber = recycleNumber;
    }
}

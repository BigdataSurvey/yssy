package com.zywl.app.base.bean;

import com.zywl.app.base.BaseBean;

import java.math.BigDecimal;

public class DicPit extends BaseBean {

    private Integer id;

    private String name;

    private BigDecimal price;

    private Long rewardItem;

    private Integer days;

    private int minCount;
    private double dayAddCount;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getRewardItem() {
        return rewardItem;
    }

    public void setRewardItem(Long rewardItem) {
        this.rewardItem = rewardItem;
    }

    public Integer getDays() {
        return days;
    }

    public void setDays(Integer days) {
        this.days = days;
    }

    public double getDayAddCount() {
        return dayAddCount;
    }

    public void setDayAddCount(double dayAddCount) {
        this.dayAddCount = dayAddCount;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getMinCount() {
        return minCount;
    }

    public void setMinCount(int minCount) {
        this.minCount = minCount;
    }
}

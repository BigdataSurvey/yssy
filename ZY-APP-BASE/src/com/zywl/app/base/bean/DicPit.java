package com.zywl.app.base.bean;

import com.zywl.app.base.BaseBean;

public class DicPit extends BaseBean {

    private Integer id;

    private String name;

    private Integer rewardItem;

    private Integer days;

    private Integer dayAddCount;

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

    public Integer getRewardItem() {
        return rewardItem;
    }

    public void setRewardItem(Integer rewardItem) {
        this.rewardItem = rewardItem;
    }

    public Integer getDays() {
        return days;
    }

    public void setDays(Integer days) {
        this.days = days;
    }

    public Integer getDayAddCount() {
        return dayAddCount;
    }

    public void setDayAddCount(Integer dayAddCount) {
        this.dayAddCount = dayAddCount;
    }

    public DicPit(Integer id, String name, Integer rewardItem, Integer days, Integer dayAddCount) {
        this.id = id;
        this.name = name;
        this.rewardItem = rewardItem;
        this.days = days;
        this.dayAddCount = dayAddCount;
    }
}

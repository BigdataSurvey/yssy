package com.zywl.app.base.bean;

import com.zywl.app.base.BaseBean;

import java.math.BigDecimal;
import java.util.Date;

public class Activity2 extends BaseBean {

    private long id;
    /**
     * 活动名称
     */
    private String activityName;
    /**
     * 活动规则
     */
    private String activityInfo;

    private String rule;

    private int moneyRule;

    private BigDecimal allMoney;

    private int addPointEvent;

    private BigDecimal onePointMoney;
    /**
     * 活动开始时间
     */
    private Date beginTime;
    /**
     * 活动结束时间
     */
    private Date endTime;

    private String date;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public String getActivityInfo() {
        return activityInfo;
    }

    public void setActivityInfo(String activityInfo) {
        this.activityInfo = activityInfo;
    }


    public Date getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(Date beginTime) {
        this.beginTime = beginTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Activity2() {
    }

    public Activity2(long id, String activityName, String activityInfo, Date beginTime, Date endTime) {
        this.id = id;
        this.activityName = activityName;
        this.activityInfo = activityInfo;
        this.beginTime = beginTime;
        this.endTime = endTime;
    }

    public String getRule() {
        return rule;
    }

    public void setRule(String rule) {
        this.rule = rule;
    }

    public int getMoneyRule() {
        return moneyRule;
    }

    public void setMoneyRule(int moneyRule) {
        this.moneyRule = moneyRule;
    }

    public BigDecimal getAllMoney() {
        return allMoney;
    }

    public void setAllMoney(BigDecimal allMoney) {
        this.allMoney = allMoney;
    }

    public int getAddPointEvent() {
        return addPointEvent;
    }

    public void setAddPointEvent(int addPointEvent) {
        this.addPointEvent = addPointEvent;
    }

    public BigDecimal getOnePointMoney() {
        return onePointMoney;
    }

    public void setOnePointMoney(BigDecimal onePointMoney) {
        this.onePointMoney = onePointMoney;
    }
}

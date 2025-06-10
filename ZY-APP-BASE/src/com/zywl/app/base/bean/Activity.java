package com.zywl.app.base.bean;

import com.zywl.app.base.BaseBean;

import java.util.Date;

public class Activity extends BaseBean {

    private long id;
    /**
     * 活动名称
     */
    private String activityName;
    /**
     * 活动规则
     */
    private String activityInfo;
    /**
     * 活动周期
     */
    private long cycle;
    /**
     * 活动开始时间
     */
    private Date beginTime;
    /**
     * 活动结束时间
     */
    private Date endTime;

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

    public long getCycle() {
        return cycle;
    }

    public void setCycle(long cycle) {
        this.cycle = cycle;
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

    public Activity() {
    }

    public Activity(long id, String activityName, String activityInfo, long cycle, Date beginTime, Date endTime) {
        this.id = id;
        this.activityName = activityName;
        this.activityInfo = activityInfo;
        this.cycle = cycle;
        this.beginTime = beginTime;
        this.endTime = endTime;
    }
}

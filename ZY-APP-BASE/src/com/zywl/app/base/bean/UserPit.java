package com.zywl.app.base.bean;

import com.alibaba.fastjson2.JSONArray;
import com.zywl.app.base.BaseBean;

import java.util.Date;

public class UserPit extends BaseBean {

    private Integer id;

    private Integer userId;

    private Integer pitId;

    private Date openTime;

    private Date endTime;

    private Date lastLookTime;

    private Date lastReceiveTime;

    private JSONArray unReceive;

    private Integer days;
    private Integer istk;

    public Integer getIstk() {
        return istk;
    }

    public void setIstk(Integer istk) {
        this.istk = istk;
    }

    public Integer getDays() {
        return days;
    }

    public void setDays(Integer days) {
        this.days = days;
    }

    public UserPit() {
    }

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

    public Integer getPitId() {
        return pitId;
    }

    public void setPitId(Integer pitId) {
        this.pitId = pitId;
    }

    public Date getOpenTime() {
        return openTime;
    }

    public void setOpenTime(Date openTime) {
        this.openTime = openTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Date getLastLookTime() {
        return lastLookTime;
    }

    public void setLastLookTime(Date lastLookTime) {
        this.lastLookTime = lastLookTime;
    }

    public Date getLastReceiveTime() {
        return lastReceiveTime;
    }

    public void setLastReceiveTime(Date lastReceiveTime) {
        this.lastReceiveTime = lastReceiveTime;
    }

    public JSONArray getUnReceive() {
        return unReceive;
    }

    public void setUnReceive(JSONArray unReceive) {
        this.unReceive = unReceive;
    }
}

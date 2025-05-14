package com.zywl.app.base.bean.card;

import com.alibaba.fastjson2.JSONArray;
import com.zywl.app.base.BaseBean;

import java.math.BigDecimal;
import java.util.Date;

public class CheckpointAwaitRecord extends BaseBean {

    private Long id;

    private Long userId;

    private Date beginTime;
    private Date receiveTime;

    private Date lastLookTime;

    private long exp;


    private JSONArray itemInfo;


    private int status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Date getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(Date beginTime) {
        this.beginTime = beginTime;
    }

    public Date getReceiveTime() {
        return receiveTime;
    }

    public void setReceiveTime(Date receiveTime) {
        this.receiveTime = receiveTime;
    }

    public Date getLastLookTime() {
        return lastLookTime;
    }

    public void setLastLookTime(Date lastLookTime) {
        this.lastLookTime = lastLookTime;
    }

    public long getExp() {
        return exp;
    }

    public void setExp(long exp) {
        this.exp = exp;
    }

    public JSONArray getItemInfo() {
        return itemInfo;
    }

    public void setItemInfo(JSONArray itemInfo) {
        this.itemInfo = itemInfo;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}

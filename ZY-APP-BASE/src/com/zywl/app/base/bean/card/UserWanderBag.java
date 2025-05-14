package com.zywl.app.base.bean.card;

import com.alibaba.fastjson2.JSONArray;
import com.zywl.app.base.BaseBean;

import java.util.Date;

public class UserWanderBag extends BaseBean {

    private Long Id;

    private Long userId;

    private int wanderType;

    private JSONArray unReceive;

    private Date updateTime;

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public int getWanderType() {
        return wanderType;
    }

    public void setWanderType(int wanderType) {
        this.wanderType = wanderType;
    }

    public JSONArray getUnReceive() {
        return unReceive;
    }

    public void setUnReceive(JSONArray unReceive) {
        this.unReceive = unReceive;
    }
}

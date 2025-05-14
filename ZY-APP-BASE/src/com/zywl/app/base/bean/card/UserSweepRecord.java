package com.zywl.app.base.bean.card;

import com.alibaba.fastjson2.JSONArray;
import com.zywl.app.base.BaseBean;

import java.util.Date;

public class UserSweepRecord extends BaseBean {

    private Long id;

    private Long userId;

    private int sweepType;

    private String sweepName;

    private JSONArray reward;

    private Date createTime;

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

    public int getSweepType() {
        return sweepType;
    }

    public void setSweepType(int sweepType) {
        this.sweepType = sweepType;
    }

    public String getSweepName() {
        return sweepName;
    }

    public void setSweepName(String sweepName) {
        this.sweepName = sweepName;
    }

    public JSONArray getReward() {
        return reward;
    }

    public void setReward(JSONArray reward) {
        this.reward = reward;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}

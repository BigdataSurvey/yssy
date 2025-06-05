package com.zywl.app.base.bean;

import com.zywl.app.base.BaseBean;

public class VipReceiveRecord extends BaseBean {

    private long id;
    private long userId;
    private long orderNo;
    private long vipLevel;
    private long reward;
    private long createTime;
    private long updateTime;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(long orderNo) {
        this.orderNo = orderNo;
    }

    public long getVipLevel() {
        return vipLevel;
    }

    public void setVipLevel(long vipLevel) {
        this.vipLevel = vipLevel;
    }

    public long getReward() {
        return reward;
    }

    public void setReward(long reward) {
        this.reward = reward;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public VipReceiveRecord() {
    }

    public VipReceiveRecord(long id, long userId, long orderNo, long vipLevel, long reward, long createTime, long updateTime) {
        this.id = id;
        this.userId = userId;
        this.orderNo = orderNo;
        this.vipLevel = vipLevel;
        this.reward = reward;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }
}

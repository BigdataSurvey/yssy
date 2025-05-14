package com.zywl.app.base.bean.card;

import com.alibaba.fastjson2.JSONArray;
import com.zywl.app.base.BaseBean;

import java.util.Date;

public class UserMine extends BaseBean {

    private Long id;

    private Long userId;

    private Long mineId;

    private Date lastMineTime;

    private Date lastOutputTime;

    private int allOutput;

    private int oneReward;

    private int output;

    private int isMining;

    private Date minEndTime;

    private int index;

    private double count;
    private Date createTime;


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

    public Long getMineId() {
        return mineId;
    }

    public void setMineId(Long mineId) {
        this.mineId = mineId;
    }

    public Date getLastMineTime() {
        return lastMineTime;
    }

    public void setLastMineTime(Date lastMineTime) {
        this.lastMineTime = lastMineTime;
    }

    public Date getLastOutputTime() {
        return lastOutputTime;
    }

    public void setLastOutputTime(Date lastOutputTime) {
        this.lastOutputTime = lastOutputTime;
    }



    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }



    public int getIsMining() {
        return isMining;
    }

    public void setIsMining(int isMining) {
        this.isMining = isMining;
    }

    public Date getMinEndTime() {
        return minEndTime;
    }

    public void setMinEndTime(Date minEndTime) {
        this.minEndTime = minEndTime;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public double getCount() {
        return count;
    }

    public void setCount(double count) {
        this.count = count;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getAllOutput() {
        return allOutput;
    }

    public void setAllOutput(int allOutput) {
        this.allOutput = allOutput;
    }

    public int getOutput() {
        return output;
    }

    public void setOutput(int output) {
        this.output = output;
    }

    public int getOneReward() {
        return oneReward;
    }

    public void setOneReward(int oneReward) {
        this.oneReward = oneReward;
    }
}

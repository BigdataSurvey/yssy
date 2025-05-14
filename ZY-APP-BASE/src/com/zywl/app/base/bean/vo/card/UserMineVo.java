package com.zywl.app.base.bean.vo.card;

import com.alibaba.fastjson2.JSONArray;
import com.zywl.app.base.BaseBean;
import com.zywl.app.base.bean.card.DicMine;

import java.math.BigDecimal;
import java.util.Date;

public class UserMineVo extends BaseBean {


    private Long userId;

    private Long mineId;

    private int output;

    private int allOutput;

    private long lastMineTime;

    private int isMining;

    private int oneReward;

    private Long minEndTime;

    private int index;

    private double count;
    private Date createTime;


    private int status;
    private BigDecimal costMoney;

    private int hour;


    private Long useItem;

    private int useNumber;

    public int getUseNumber() {
        return useNumber;
    }

    public void setUseNumber(int useNumber) {
        this.useNumber = useNumber;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }


    public Long getUseItem() {
        return useItem;
    }

    public void setUseItem(Long useItem) {
        this.useItem = useItem;
    }

    public BigDecimal getCostMoney() {
        return costMoney;
    }

    public void setCostMoney(BigDecimal costMoney) {
        this.costMoney = costMoney;
    }


    public Long getMinEndTime() {
        return minEndTime;
    }

    public void setMinEndTime(Long minEndTime) {
        this.minEndTime = minEndTime;
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

    public long getLastMineTime() {
        return lastMineTime;
    }

    public void setLastMineTime(long lastMineTime) {
        this.lastMineTime = lastMineTime;
    }

    public int getOutput() {
        return output;
    }

    public void setOutput(int output) {
        this.output = output;
    }

    public int getAllOutput() {
        return allOutput;
    }

    public void setAllOutput(int allOutput) {
        this.allOutput = allOutput;
    }

    public int getOneReward() {
        return oneReward;
    }

    public void setOneReward(int oneReward) {
        this.oneReward = oneReward;
    }
}

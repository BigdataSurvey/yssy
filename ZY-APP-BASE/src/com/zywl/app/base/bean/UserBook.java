package com.zywl.app.base.bean;

import com.zywl.app.base.BaseBean;

import java.math.BigDecimal;
import java.util.Date;

public class UserBook extends BaseBean {

    private Long id;

    private Long userId;

    private Long itemId;

    private int allNumber;

    private int number;

    private int todayNumber;

    private BigDecimal canReceive;

    private Date addTime;

    private Date settleTime;

    private Date unlockTime;

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

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public int getAllNumber() {
        return allNumber;
    }

    public void setAllNumber(int allNumber) {
        this.allNumber = allNumber;
    }

    public int getTodayNumber() {
        return todayNumber;
    }

    public void setTodayNumber(int todayNumber) {
        this.todayNumber = todayNumber;
    }

    public BigDecimal getCanReceive() {
        return canReceive;
    }

    public void setCanReceive(BigDecimal canReceive) {
        this.canReceive = canReceive;
    }

    public Date getAddTime() {
        return addTime;
    }

    public void setAddTime(Date addTime) {
        this.addTime = addTime;
    }

    public Date getSettleTime() {
        return settleTime;
    }

    public void setSettleTime(Date settleTime) {
        this.settleTime = settleTime;
    }

    public Date getUnlockTime() {
        return unlockTime;
    }

    public void setUnlockTime(Date unlockTime) {
        this.unlockTime = unlockTime;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}

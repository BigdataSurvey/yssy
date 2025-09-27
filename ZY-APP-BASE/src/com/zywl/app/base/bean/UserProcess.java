package com.zywl.app.base.bean;

import com.zywl.app.base.BaseBean;

import java.util.Date;

public class UserProcess extends BaseBean {

    private Integer userId;
    private Integer currProcessNumber;
    private Integer highNum;
    private Integer activaStatus;
    private Date updtTime;

    public Date getUpdtTime() {
        return updtTime;
    }


    public Integer getActivaStatus() {
        return activaStatus;
    }

    public void setActivaStatus(Integer activaStatus) {
        this.activaStatus = activaStatus;
    }

    public void setUpdtTime(Date updtTime) {
        this.updtTime = updtTime;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getCurrProcessNumber() {
        return currProcessNumber;
    }

    public void setCurrProcessNumber(Integer currProcessNumber) {
        this.currProcessNumber = currProcessNumber;
    }

    public Integer getHighNum() {
        return highNum;
    }

    public void setHighNum(Integer highNum) {
        this.highNum = highNum;
    }
}

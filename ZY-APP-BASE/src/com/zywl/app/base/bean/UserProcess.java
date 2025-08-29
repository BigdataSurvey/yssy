package com.zywl.app.base.bean;

import com.zywl.app.base.BaseBean;

public class UserProcess extends BaseBean {

    private Integer userId;
    private Integer currProcessNumber;
    private Integer highNum;


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

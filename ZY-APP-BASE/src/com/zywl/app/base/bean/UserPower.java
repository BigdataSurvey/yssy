package com.zywl.app.base.bean;

import com.zywl.app.base.BaseBean;

public class UserPower extends BaseBean {

    private Long userId;

    private Long power;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getPower() {
        return power;
    }

    public void setPower(Long power) {
        this.power = power;
    }
}

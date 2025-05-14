package com.zywl.app.base.bean;

import com.zywl.app.base.BaseBean;

public class TeamAttribute extends BaseBean {

    private Long power;

    private Long attribute;

    public Long getPower() {
        return power;
    }

    public void setPower(Long power) {
        this.power = power;
    }

    public Long getAttribute() {
        return attribute;
    }

    public void setAttribute(Long attribute) {
        this.attribute = attribute;
    }
}

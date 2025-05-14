package com.zywl.app.base.bean.card;

import com.zywl.app.base.BaseBean;

public class UserTower extends BaseBean {

    private Long userId;

    private int floor;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }
}

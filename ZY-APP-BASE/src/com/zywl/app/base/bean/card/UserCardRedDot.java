package com.zywl.app.base.bean.card;

import com.zywl.app.base.BaseBean;

public class UserCardRedDot extends BaseBean {

    private Long userId;

    private RedDotState redDotState;




    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public RedDotState getRedDotState() {
        return redDotState;
    }

    public void setRedDotState(RedDotState redDotState) {
        this.redDotState = redDotState;
    }
}

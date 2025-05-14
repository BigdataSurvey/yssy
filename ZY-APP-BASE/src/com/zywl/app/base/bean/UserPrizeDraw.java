package com.zywl.app.base.bean;

import com.zywl.app.base.BaseBean;

public class UserPrizeDraw extends BaseBean {

    private Long id;

    private Long userId;

    private String prizeDrawInfo;

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

    public String getPrizeDrawInfo() {
        return prizeDrawInfo;
    }

    public void setPrizeDrawInfo(String prizeDrawInfo) {
        this.prizeDrawInfo = prizeDrawInfo;
    }
}

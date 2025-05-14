package com.zywl.app.base.bean.card;

import com.zywl.app.base.BaseBean;

public class UserFb extends BaseBean {

    private Long id;

    private Long userId;

    private String fbType;

    private Long fbId;

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

    public String getFbType() {
        return fbType;
    }

    public void setFbType(String fbType) {
        this.fbType = fbType;
    }

    public Long getFbId() {
        return fbId;
    }

    public void setFbId(Long fbId) {
        this.fbId = fbId;
    }
}

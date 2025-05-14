package com.zywl.app.base.bean.card;

import com.zywl.app.base.BaseBean;

public class UserCardExp extends BaseBean {


    private Long id;

    private Long userId;

    private Long exp;

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

    public Long getExp() {
        return exp;
    }

    public void setExp(Long exp) {
        this.exp = exp;
    }
}

package com.zywl.app.base.bean.card;

import com.zywl.app.base.BaseBean;

public class UserDispatchList extends BaseBean {

    private Long userId;

    private String dispatchList;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getDispatchList() {
        return dispatchList;
    }

    public void setDispatchList(String dispatchList) {
        this.dispatchList = dispatchList;
    }
}

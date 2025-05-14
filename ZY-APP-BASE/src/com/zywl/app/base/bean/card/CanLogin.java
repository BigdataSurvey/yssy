package com.zywl.app.base.bean.card;

import com.zywl.app.base.BaseBean;

public class CanLogin extends BaseBean {

    private Long id;

    private String idCard;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }
}

package com.zywl.app.base.bean;

import com.zywl.app.base.BaseBean;

import java.math.BigDecimal;

public class Ns extends BaseBean {

    private Long id;

    private BigDecimal hp;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getHp() {
        return hp;
    }

    public void setHp(BigDecimal hp) {
        this.hp = hp;
    }
}

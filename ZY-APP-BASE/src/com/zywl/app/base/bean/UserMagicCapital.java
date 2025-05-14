package com.zywl.app.base.bean;

import com.zywl.app.base.BaseBean;

import java.math.BigDecimal;

public class UserMagicCapital extends BaseBean {

    private Long userId;

    private BigDecimal magicBalance;


    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public BigDecimal getMagicBalance() {
        return magicBalance;
    }

    public void setMagicBalance(BigDecimal magicBalance) {
        this.magicBalance = magicBalance;
    }
}

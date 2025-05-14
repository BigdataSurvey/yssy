package com.zywl.app.base.bean;

import com.zywl.app.base.BaseBean;

import java.math.BigDecimal;

public class UserIncomeStatement extends BaseBean {

    private Long id;

    private Long userId;

    private String ymd;

    private BigDecimal oneIncome;

    private BigDecimal twoIncome;

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

    public String getYmd() {
        return ymd;
    }

    public void setYmd(String ymd) {
        this.ymd = ymd;
    }

    public BigDecimal getOneIncome() {
        return oneIncome;
    }

    public void setOneIncome(BigDecimal oneIncome) {
        this.oneIncome = oneIncome;
    }

    public BigDecimal getTwoIncome() {
        return twoIncome;
    }

    public void setTwoIncome(BigDecimal twoIncome) {
        this.twoIncome = twoIncome;
    }
}

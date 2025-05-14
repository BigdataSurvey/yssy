package com.zywl.app.base.bean.vo;

import com.zywl.app.base.BaseBean;

import java.math.BigDecimal;

public class CaveVo extends BaseBean {

    private Long itemId;

    private String cost;

    private BigDecimal initialSpeed;

    private Integer rate;

    public String getCost() {
        return cost;
    }

    public void setCost(String cost) {
        this.cost = cost;
    }

    public Integer getRate() {
        return rate;
    }

    public void setRate(Integer rate) {
        this.rate = rate;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public BigDecimal getInitialSpeed() {

        return initialSpeed;
    }

    public void setInitialSpeed(BigDecimal initialSpeed) {
        this.initialSpeed = initialSpeed;
    }
}

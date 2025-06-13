package com.zywl.app.base.bean;

import com.zywl.app.base.BaseBean;

import java.math.BigDecimal;

public class GoodNo extends BaseBean {

    private Long id;

    private String goodNo;

    private BigDecimal price;

    private Integer number;

    private Integer status;
    private Integer type;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGoodNo() {
        return goodNo;
    }

    public void setGoodNo(String goodNo) {
        this.goodNo = goodNo;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public GoodNo() {
    }

    public GoodNo(Long id, String goodNo, BigDecimal price, Integer number, Integer status, Integer type) {
        this.id = id;
        this.goodNo = goodNo;
        this.price = price;
        this.number = number;
        this.status = status;
        this.type = type;
    }
}

package com.zywl.app.base.bean.vo;

import java.math.BigDecimal;

public class ImmortalGateImageVo {

    private Long id;

    private String imageUrl;

    private BigDecimal imagePrice;

    private Integer priceType;

    private Integer status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public BigDecimal getImagePrice() {
        return imagePrice;
    }

    public void setImagePrice(BigDecimal imagePrice) {
        this.imagePrice = imagePrice;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getPriceType() {
        return priceType;
    }

    public void setPriceType(Integer priceType) {
        this.priceType = priceType;
    }
}

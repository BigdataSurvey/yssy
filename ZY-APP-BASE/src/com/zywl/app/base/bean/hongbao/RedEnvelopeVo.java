package com.zywl.app.base.bean.hongbao;


import lombok.Data;

import java.math.BigDecimal;


public class RedEnvelopeVo {


    private Long id;

    private BigDecimal totalAmount;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public RedEnvelopeVo(Long id, BigDecimal totalAmount) {
        this.id = id;
        this.totalAmount = totalAmount;
    }

    public RedEnvelopeVo() {
    }
}

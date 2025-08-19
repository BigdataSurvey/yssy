package com.zywl.app.base.bean;

import com.zywl.app.base.BaseBean;

import java.math.BigDecimal;

public class DicJackpot extends BaseBean {


    private BigDecimal amonut;
    private BigDecimal production;

    public BigDecimal getAmonut() {
        return amonut;
    }

    public void setAmonut(BigDecimal amonut) {
        this.amonut = amonut;
    }

    public BigDecimal getProduction() {
        return production;
    }

    public void setProduction(BigDecimal production) {
        this.production = production;
    }
}

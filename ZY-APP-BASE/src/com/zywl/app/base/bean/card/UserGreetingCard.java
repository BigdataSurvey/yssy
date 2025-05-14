package com.zywl.app.base.bean.card;

import com.zywl.app.base.BaseBean;

import java.math.BigDecimal;

public class UserGreetingCard extends BaseBean {
    private Long userId;

    private BigDecimal popularity;

    private BigDecimal unReceivePop;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public BigDecimal getPopularity() {
        return popularity;
    }

    public void setPopularity(BigDecimal popularity) {
        this.popularity = popularity;
    }

    public BigDecimal getUnReceivePop() {
        return unReceivePop;
    }

    public void setUnReceivePop(BigDecimal unReceivePop) {
        this.unReceivePop = unReceivePop;
    }
}

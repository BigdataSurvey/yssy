package com.zywl.app.base.bean;

import java.math.BigDecimal;
import java.util.Date;

public class UserGift {
    private Long userId;

    private BigDecimal giftNum;

    private Date createTime;

    public UserGift(Long userId, BigDecimal giftNum, Date createTime) {
        this.userId = userId;
        this.giftNum = giftNum;
        this.createTime = createTime;
    }

    public UserGift() {
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public BigDecimal getGiftNum() {
        return giftNum;
    }

    public void setGiftNum(BigDecimal giftNum) {
        this.giftNum = giftNum;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}

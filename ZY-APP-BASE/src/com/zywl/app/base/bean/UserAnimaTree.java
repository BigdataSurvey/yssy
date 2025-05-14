package com.zywl.app.base.bean;

import com.zywl.app.base.BaseBean;

import java.math.BigDecimal;
import java.util.Date;

public class UserAnimaTree extends BaseBean {


    private Long userId;

    private BigDecimal animaNumber;

    private Date createTime;


    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public BigDecimal getAnimaNumber() {
        return animaNumber;
    }

    public void setAnimaNumber(BigDecimal animaNumber) {
        this.animaNumber = animaNumber;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}

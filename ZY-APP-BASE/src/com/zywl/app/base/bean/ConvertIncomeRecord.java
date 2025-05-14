package com.zywl.app.base.bean;

import com.zywl.app.base.BaseBean;

import java.math.BigDecimal;
import java.util.Date;

public class ConvertIncomeRecord extends BaseBean {

    private Long id;

    private Long userId;

    private String orderNo;

    private BigDecimal beforeIncome;

    private BigDecimal afterIncome;

    private BigDecimal amount;

    private Date createTime;

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

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public BigDecimal getBeforeIncome() {
        return beforeIncome;
    }

    public void setBeforeIncome(BigDecimal beforeIncome) {
        this.beforeIncome = beforeIncome;
    }

    public BigDecimal getAfterIncome() {
        return afterIncome;
    }

    public void setAfterIncome(BigDecimal afterIncome) {
        this.afterIncome = afterIncome;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}

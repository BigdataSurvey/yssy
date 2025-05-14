package com.zywl.app.base.bean;

import com.zywl.app.base.BaseBean;

import java.math.BigDecimal;
import java.util.Date;

public class ApplyFor extends BaseBean {

    private Long id;

    private Long userId;

    private BigDecimal income;

    private BigDecimal sill;

    private int status;

    private Date createTime;

    private Date updateTime;

    private String createAdmin;

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

    public BigDecimal getIncome() {
        return income;
    }

    public void setIncome(BigDecimal income) {
        this.income = income;
    }

    public BigDecimal getSill() {
        return sill;
    }

    public void setSill(BigDecimal sill) {
        this.sill = sill;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getCreateAdmin() {
        return createAdmin;
    }

    public void setCreateAdmin(String createAdmin) {
        this.createAdmin = createAdmin;
    }
}

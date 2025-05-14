package com.zywl.app.base.bean;

import com.zywl.app.base.BaseBean;

import java.math.BigDecimal;
import java.util.Date;

public class UserDzRecord extends BaseBean {

    private    Long id;

    private Long userId;

    private BigDecimal dzMoney;

    private Integer status;

    private Integer luckUserStatus;

    private BigDecimal cuMoney;

    private BigDecimal returnMoney;

    private Integer periods;

    private Date clockTime;

    private Date createTime;

    private Date updateTime;

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

    public BigDecimal getDzMoney() {
        return dzMoney;
    }

    public void setDzMoney(BigDecimal dzMoney) {
        this.dzMoney = dzMoney;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getLuckUserStatus() {
        return luckUserStatus;
    }

    public void setLuckUserStatus(Integer luckUserStatus) {
        this.luckUserStatus = luckUserStatus;
    }

    public BigDecimal getCuMoney() {
        return cuMoney;
    }

    public void setCuMoney(BigDecimal cuMoney) {
        this.cuMoney = cuMoney;
    }

    public BigDecimal getReturnMoney() {
        return returnMoney;
    }

    public void setReturnMoney(BigDecimal returnMoney) {
        this.returnMoney = returnMoney;
    }

    public Integer getPeriods() {
        return periods;
    }

    public void setPeriods(Integer periods) {
        this.periods = periods;
    }

    public Date getClockTime() {
        return clockTime;
    }

    public void setClockTime(Date clockTime) {
        this.clockTime = clockTime;
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
}

package com.zywl.app.base.bean;

import com.alibaba.fastjson2.JSONArray;
import com.zywl.app.base.BaseBean;

import java.math.BigDecimal;
import java.util.Date;

public class InvestDetail extends BaseBean {

    private Integer id;

    private Long userId;

    private Integer investNumber;

    private Date investDate;

    private Date endDate;

    private Integer investSealStatus;

    private BigDecimal generYyq;

    private BigDecimal unReceive;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getInvestNumber() {
        return investNumber;
    }

    public void setInvestNumber(Integer investNumber) {
        this.investNumber = investNumber;
    }

    public Date getInvestDate() {
        return investDate;
    }

    public void setInvestDate(Date investDate) {
        this.investDate = investDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Integer getInvestSealStatus() {
        return investSealStatus;
    }

    public void setInvestSealStatus(Integer investSealStatus) {
        this.investSealStatus = investSealStatus;
    }

    public BigDecimal getGenerYyq() {
        return generYyq;
    }

    public void setGenerYyq(BigDecimal generYyq) {
        this.generYyq = generYyq;
    }

    public BigDecimal getUnReceive() {
        return unReceive;
    }

    public void setUnReceive(BigDecimal unReceive) {
        this.unReceive = unReceive;
    }
}

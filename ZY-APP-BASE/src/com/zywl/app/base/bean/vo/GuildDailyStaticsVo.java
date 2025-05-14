package com.zywl.app.base.bean.vo;

import java.math.BigDecimal;

public class GuildDailyStaticsVo {

    private Long userId;
    private int allRevenueNumber;
    private int allExpendNumber;
    private BigDecimal allRevenue;

    private BigDecimal allExpend;

    public int getAllRevenueNumber() {
        return allRevenueNumber;
    }

    public void setAllRevenueNumber(int allRevenueNumber) {
        this.allRevenueNumber = allRevenueNumber;
    }

    public int getAllExpendNumber() {
        return allExpendNumber;
    }

    public void setAllExpendNumber(int allExpendNumber) {
        this.allExpendNumber = allExpendNumber;
    }

    public BigDecimal getAllRevenue() {
        return allRevenue;
    }

    public void setAllRevenue(BigDecimal allRevenue) {
        this.allRevenue = allRevenue;
    }

    public BigDecimal getAllExpend() {
        return allExpend;
    }

    public void setAllExpend(BigDecimal allExpend) {
        this.allExpend = allExpend;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}

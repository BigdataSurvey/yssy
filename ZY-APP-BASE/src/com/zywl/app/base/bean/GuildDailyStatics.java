package com.zywl.app.base.bean;

import com.zywl.app.base.BaseBean;

import java.math.BigDecimal;

public class GuildDailyStatics extends BaseBean {

    private Long id;

    private String ymd;

    private Long userId;

    private Long guildId;

    private Integer expendNumber;

    private Integer revenueNumber;

    private BigDecimal revenue;

    private BigDecimal expend;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getYmd() {
        return ymd;
    }

    public void setYmd(String ymd) {
        this.ymd = ymd;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getGuildId() {
        return guildId;
    }

    public void setGuildId(Long guildId) {
        this.guildId = guildId;
    }

    public Integer getExpendNumber() {
        return expendNumber;
    }

    public void setExpendNumber(Integer expendNumber) {
        this.expendNumber = expendNumber;
    }

    public Integer getRevenueNumber() {
        return revenueNumber;
    }

    public void setRevenueNumber(Integer revenueNumber) {
        this.revenueNumber = revenueNumber;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }

    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue;
    }

    public BigDecimal getExpend() {
        return expend;
    }

    public void setExpend(BigDecimal expend) {
        this.expend = expend;
    }
}

package com.zywl.app.base.bean;

import com.zywl.app.base.BaseBean;

import java.math.BigDecimal;
import java.util.Date;

public class GuildMember extends BaseBean {

    private Long id;

    private Long userId;

    private Long guildId;

    private BigDecimal profitRate;

    private BigDecimal profitBalance;

    private Integer roleId;

    private Long createUserId;

    private BigDecimal bailAmount;

    private String remark;

    private Integer status;

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

    public Long getGuildId() {
        return guildId;
    }

    public void setGuildId(Long guildId) {
        this.guildId = guildId;
    }

    public BigDecimal getProfitRate() {
        return profitRate;
    }

    public void setProfitRate(BigDecimal profitRate) {
        this.profitRate = profitRate;
    }

    public BigDecimal getProfitBalance() {
        return profitBalance;
    }

    public void setProfitBalance(BigDecimal profitBalance) {
        this.profitBalance = profitBalance;
    }

    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }

    public Long getCreateUserId() {
        return createUserId;
    }

    public void setCreateUserId(Long createUserId) {
        this.createUserId = createUserId;
    }

    public BigDecimal getBailAmount() {
        return bailAmount;
    }

    public void setBailAmount(BigDecimal bailAmount) {
        this.bailAmount = bailAmount;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}

package com.zywl.app.base.bean;

import com.zywl.app.base.BaseBean;

import java.math.BigDecimal;
import java.util.Date;

public class GameNs extends BaseBean {

    private Long id;

    private int round;

    private Long nsId;

    private BigDecimal nowHp;

    private BigDecimal nowPrize;

    private Long lastUserId;

    private String betInfo;

    private String lastIds;

    private int status;

    private Long runTime;

    private Date pauseTime;

    private Date createTime;

    private Date updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getRound() {
        return round;
    }

    public void setRound(int round) {
        this.round = round;
    }

    public Long getNsId() {
        return nsId;
    }

    public void setNsId(Long nsId) {
        this.nsId = nsId;
    }

    public BigDecimal getNowHp() {
        return nowHp;
    }

    public void setNowHp(BigDecimal nowHp) {
        this.nowHp = nowHp;
    }

    public BigDecimal getNowPrize() {
        return nowPrize;
    }

    public void setNowPrize(BigDecimal nowPrize) {
        this.nowPrize = nowPrize;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Long getRunTime() {
        return runTime;
    }

    public void setRunTime(Long runTime) {
        this.runTime = runTime;
    }

    public Date getPauseTime() {
        return pauseTime;
    }

    public void setPauseTime(Date pauseTime) {
        this.pauseTime = pauseTime;
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

    public Long getLastUserId() {
        return lastUserId;
    }

    public void setLastUserId(Long lastUserId) {
        this.lastUserId = lastUserId;
    }

    public String getBetInfo() {
        return betInfo;
    }

    public void setBetInfo(String betInfo) {
        this.betInfo = betInfo;
    }

    public String getLastIds() {
        return lastIds;
    }

    public void setLastIds(String lastIds) {
        this.lastIds = lastIds;
    }
}

package com.zywl.app.base.bean.card;

import com.alibaba.fastjson2.JSONArray;
import com.zywl.app.base.BaseBean;

import java.math.BigDecimal;
import java.util.Date;

public class GameCards extends BaseBean {

    private  Long id;

    private  Long userId;

    private int type;

    private int totalTurns;

    private int trapCount;

    private JSONArray cardsInfo;

    private int status;

    private BigDecimal getAmount;

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

    public int getTotalTurns() {
        return totalTurns;
    }

    public void setTotalTurns(int totalTurns) {
        this.totalTurns = totalTurns;
    }

    public int getTrapCount() {
        return trapCount;
    }

    public void setTrapCount(int trapCount) {
        this.trapCount = trapCount;
    }

    public JSONArray getCardsInfo() {
        return cardsInfo;
    }

    public void setCardsInfo(JSONArray cardsInfo) {
        this.cardsInfo = cardsInfo;
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

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public BigDecimal getGetAmount() {
        return getAmount;
    }

    public void setGetAmount(BigDecimal getAmount) {
        this.getAmount = getAmount;
    }
}

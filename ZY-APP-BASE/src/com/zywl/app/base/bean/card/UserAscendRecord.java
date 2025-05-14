package com.zywl.app.base.bean.card;

import com.alibaba.fastjson2.JSONArray;

import java.math.BigDecimal;
import java.util.Date;

public class UserAscendRecord {

    private Long id;

    private Long userId;

    private Long playerCardId;

    private Long cardId;

    private BigDecimal costCoin;

    private JSONArray costCardIds;

    private int costItemNumber;

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

    public Long getPlayerCardId() {
        return playerCardId;
    }

    public void setPlayerCardId(Long playerCardId) {
        this.playerCardId = playerCardId;
    }

    public Long getCardId() {
        return cardId;
    }

    public void setCardId(Long cardId) {
        this.cardId = cardId;
    }

    public BigDecimal getCostCoin() {
        return costCoin;
    }

    public void setCostCoin(BigDecimal costCoin) {
        this.costCoin = costCoin;
    }

    public JSONArray getCostCardIds() {
        return costCardIds;
    }

    public void setCostCardIds(JSONArray costCardIds) {
        this.costCardIds = costCardIds;
    }

    public int getCostItemNumber() {
        return costItemNumber;
    }

    public void setCostItemNumber(int costItemNumber) {
        this.costItemNumber = costItemNumber;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}

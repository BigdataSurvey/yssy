package com.zywl.app.base.bean;

import com.zywl.app.base.BaseBean;

import java.math.BigDecimal;
import java.util.Date;

public class FoodGameRecord extends BaseBean {
    private Long id;
    private Integer roomId;
    private Long userId;
    private BigDecimal betAmount;
    private BigDecimal winAmount;
    private String cardInfo;
    private Integer winValue;
    private  Integer winLose;
    private String winInfo;
    private Date recordTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getRoomId() {
        return roomId;
    }

    public void setRoomId(Integer roomId) {
        this.roomId = roomId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public BigDecimal getBetAmount() {
        return betAmount;
    }

    public void setBetAmount(BigDecimal betAmount) {
        this.betAmount = betAmount;
    }

    public BigDecimal getWinAmount() {
        return winAmount;
    }

    public void setWinAmount(BigDecimal winAmount) {
        this.winAmount = winAmount;
    }

    public String getCardInfo() {
        return cardInfo;
    }

    public void setCardInfo(String cardInfo) {
        this.cardInfo = cardInfo;
    }

    public Integer getWinValue() {
        return winValue;
    }

    public void setWinValue(Integer winValue) {
        this.winValue = winValue;
    }

    public Integer getWinLose() {
        return winLose;
    }

    public void setWinLose(Integer winLose) {
        this.winLose = winLose;
    }

    public String getWinInfo() {
        return winInfo;
    }

    public void setWinInfo(String winInfo) {
        this.winInfo = winInfo;
    }

    public Date getRecordTime() {
        return recordTime;
    }

    public void setRecordTime(Date recordTime) {
        this.recordTime = recordTime;
    }
}

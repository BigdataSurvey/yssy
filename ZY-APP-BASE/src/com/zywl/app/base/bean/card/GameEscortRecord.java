package com.zywl.app.base.bean.card;

import com.zywl.app.base.BaseBean;

import java.math.BigDecimal;
import java.util.Date;

public class GameEscortRecord extends BaseBean {

    private Long id;

    private Long userId;

    private String gameOrder;

    private BigDecimal amount;

    private int beginNumber;

    private int event1;

    private int eventResult1;

    private int event2;

    private int eventResult2;

    private int event3;

    private int eventResult3;

    private int event4;

    private int eventResult4;

    private int event5;

    private int eventResult5;

    private int event6;

    private int eventResult6;

    private int nowNumber;

    private int nowCheckpoint;


    private BigDecimal getAmount;

    private int gameStatus;

    private Date createTime;

    private Date updateTime;

    public int getNowNumber() {
        return nowNumber;
    }

    public void setNowNumber(int nowNumber) {
        this.nowNumber = nowNumber;
    }

    public BigDecimal getGetAmount() {
        return getAmount;
    }

    public void setGetAmount(BigDecimal getAmount) {
        this.getAmount = getAmount;
    }

    public int getGameStatus() {
        return gameStatus;
    }

    public void setGameStatus(int gameStatus) {
        this.gameStatus = gameStatus;
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

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public int getBeginNumber() {
        return beginNumber;
    }

    public void setBeginNumber(int beginNumber) {
        this.beginNumber = beginNumber;
    }

    public int getEvent1() {
        return event1;
    }

    public void setEvent1(int event1) {
        this.event1 = event1;
    }

    public int getEventResult1() {
        return eventResult1;
    }

    public void setEventResult1(int eventResult1) {
        this.eventResult1 = eventResult1;
    }

    public int getEvent2() {
        return event2;
    }

    public void setEvent2(int event2) {
        this.event2 = event2;
    }

    public int getEventResult2() {
        return eventResult2;
    }

    public void setEventResult2(int eventResult2) {
        this.eventResult2 = eventResult2;
    }

    public int getEvent3() {
        return event3;
    }

    public void setEvent3(int event3) {
        this.event3 = event3;
    }

    public int getEventResult3() {
        return eventResult3;
    }

    public void setEventResult3(int eventResult3) {
        this.eventResult3 = eventResult3;
    }

    public int getEvent4() {
        return event4;
    }

    public void setEvent4(int event4) {
        this.event4 = event4;
    }

    public int getEventResult4() {
        return eventResult4;
    }

    public void setEventResult4(int eventResult4) {
        this.eventResult4 = eventResult4;
    }

    public int getEvent5() {
        return event5;
    }

    public void setEvent5(int event5) {
        this.event5 = event5;
    }

    public int getEventResult5() {
        return eventResult5;
    }

    public void setEventResult5(int eventResult5) {
        this.eventResult5 = eventResult5;
    }

    public int getEvent6() {
        return event6;
    }

    public void setEvent6(int event6) {
        this.event6 = event6;
    }

    public int getEventResult6() {
        return eventResult6;
    }

    public void setEventResult6(int eventResult6) {
        this.eventResult6 = eventResult6;
    }

    public String getGameOrder() {
        return gameOrder;
    }

    public void setGameOrder(String gameOrder) {
        this.gameOrder = gameOrder;
    }

    public int getNowCheckpoint() {
        return nowCheckpoint;
    }

    public void setNowCheckpoint(int nowCheckpoint) {
        this.nowCheckpoint = nowCheckpoint;
    }
}

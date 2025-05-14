package com.zywl.app.base.bean.vo.card;

import com.zywl.app.base.BaseBean;

import java.math.BigDecimal;
import java.util.Date;

public class GameEscortRecordVo extends BaseBean {

    private Long userId;

    private BigDecimal amount;

    private int beginNumber;

    private int nowNumber;


    private BigDecimal getAmount;


    private Date createTime;

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

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}

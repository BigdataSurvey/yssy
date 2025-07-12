package com.zywl.app.base.bean;

import com.zywl.app.base.BaseBean;

import java.math.BigDecimal;
import java.util.Date;

public class MzTrad extends BaseBean {

    private Long id;

    private Long mzItemId;

    private Long userItemId;

    private  Long sellUserId;

    private Long buyUserId;

    private BigDecimal sellPrice;

    private BigDecimal fee;

    private BigDecimal getAmount;

    private int status;

    private Date createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMzItemId() {
        return mzItemId;
    }

    public void setMzItemId(Long mzItemId) {
        this.mzItemId = mzItemId;
    }

    public Long getSellUserId() {
        return sellUserId;
    }

    public void setSellUserId(Long sellUserId) {
        this.sellUserId = sellUserId;
    }

    public Long getBuyUserId() {
        return buyUserId;
    }

    public void setBuyUserId(Long buyUserId) {
        this.buyUserId = buyUserId;
    }

    public BigDecimal getSellPrice() {
        return sellPrice;
    }

    public void setSellPrice(BigDecimal sellPrice) {
        this.sellPrice = sellPrice;
    }

    public BigDecimal getFee() {
        return fee;
    }

    public void setFee(BigDecimal fee) {
        this.fee = fee;
    }

    public BigDecimal getGetAmount() {
        return getAmount;
    }

    public void setGetAmount(BigDecimal getAmount) {
        this.getAmount = getAmount;
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

    public Long getUserItemId() {
        return userItemId;
    }

    public void setUserItemId(Long userItemId) {
        this.userItemId = userItemId;
    }
}

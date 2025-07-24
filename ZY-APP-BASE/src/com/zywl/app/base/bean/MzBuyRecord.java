package com.zywl.app.base.bean;

import com.zywl.app.base.BaseBean;

import java.math.BigDecimal;
import java.util.Date;

public class MzBuyRecord extends BaseBean {

    private Long id;

    private Long userId;

    private String orderNo;

    private BigDecimal amount;

    private BigDecimal fee;

    private int buyType;

    private Long sellUserId;

    private Long tradId;

    private Long mzItemId;

    private String name;

    private int icon;

    private int isWhite;

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

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getFee() {
        return fee;
    }

    public void setFee(BigDecimal fee) {
        this.fee = fee;
    }

    public int getBuyType() {
        return buyType;
    }

    public void setBuyType(int buyType) {
        this.buyType = buyType;
    }

    public Long getSellUserId() {
        return sellUserId;
    }

    public void setSellUserId(Long sellUserId) {
        this.sellUserId = sellUserId;
    }

    public Long getTradId() {
        return tradId;
    }

    public void setTradId(Long tradId) {
        this.tradId = tradId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Long getMzItemId() {
        return mzItemId;
    }

    public void setMzItemId(Long mzItemId) {
        this.mzItemId = mzItemId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public int getIsWhite() {
        return isWhite;
    }

    public void setIsWhite(int isWhite) {
        this.isWhite = isWhite;
    }
}

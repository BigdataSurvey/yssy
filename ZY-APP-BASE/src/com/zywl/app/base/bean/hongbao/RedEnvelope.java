package com.zywl.app.base.bean.hongbao;

import com.alibaba.fastjson2.JSONArray;
import com.zywl.app.base.BaseBean;
import java.math.BigDecimal;
import java.util.Date;

//红包表
public class RedEnvelope extends BaseBean {

    private Boolean isBomb; // 是否为炸弹红包

    private Long id;

    private Long userId;

    // 红包金额列表
    private JSONArray amount;

    //发放数量
    private int releasedQuantity;


    //发放红包奖励
    private BigDecimal redAward;

    //剩余金额
    private BigDecimal surplusAmount;

    private Date createTime;

    private Date updateTime;
    
    //抢红包金额
    private String allocationAmount;
    private int bombIndex;
    private Integer nowIndex;



    /*** 以下对象*/
    private  BigDecimal totalAmount; // 总金额（分）



    private int bombAmount; // 炸弹金额
    private int totalNumber;


    private int status;//状态

    private String remark;


    public Boolean getBomb() {
        return isBomb;
    }

    public void setBomb(Boolean bomb) {
        isBomb = bomb;
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

    public JSONArray getAmount() {
        return amount;
    }

    public void setAmount(JSONArray amount) {
        this.amount = amount;
    }

    public int getReleasedQuantity() {
        return releasedQuantity;
    }

    public void setReleasedQuantity(int releasedQuantity) {
        this.releasedQuantity = releasedQuantity;
    }

    public BigDecimal getRedAward() {
        return redAward;
    }

    public void setRedAward(BigDecimal redAward) {
        this.redAward = redAward;
    }

    public BigDecimal getSurplusAmount() {
        return surplusAmount;
    }

    public void setSurplusAmount(BigDecimal surplusAmount) {
        this.surplusAmount = surplusAmount;
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

    public String getAllocationAmount() {
        return allocationAmount;
    }

    public void setAllocationAmount(String allocationAmount) {
        this.allocationAmount = allocationAmount;
    }

    public int getBombIndex() {
        return bombIndex;
    }

    public void setBombIndex(int bombIndex) {
        this.bombIndex = bombIndex;
    }

    public Integer getNowIndex() {
        return nowIndex;
    }

    public void setNowIndex(Integer nowIndex) {
        this.nowIndex = nowIndex;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public int getBombAmount() {
        return bombAmount;
    }

    public void setBombAmount(int bombAmount) {
        this.bombAmount = bombAmount;
    }

    public int getTotalNumber() {
        return totalNumber;
    }

    public void setTotalNumber(int totalNumber) {
        this.totalNumber = totalNumber;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}

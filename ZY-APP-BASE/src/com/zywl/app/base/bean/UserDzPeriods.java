package com.zywl.app.base.bean;

import com.zywl.app.base.BaseBean;

import java.math.BigDecimal;
import java.util.Date;

public class UserDzPeriods extends BaseBean {


    private Long id;

    private Integer periods;

    private Integer userDZNum;

    private Integer userDkNum;

    private BigDecimal dzMoneyAll;

    private BigDecimal dzMoneyReturn;

    private Long userId;

    private Integer status;

    private BigDecimal cuMoney;

    private BigDecimal returnMoney;

    private Date createTime;

    private Date updateTime;

    //冗余字段

    private String userName;

    private String userImage;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getPeriods() {
        return periods;
    }

    public void setPeriods(Integer periods) {
        this.periods = periods;
    }

    public Integer getUserDZNum() {
        return userDZNum;
    }

    public void setUserDZNum(Integer userDZNum) {
        this.userDZNum = userDZNum;
    }

    public Integer getUserDkNum() {
        return userDkNum;
    }

    public void setUserDkNum(Integer userDkNum) {
        this.userDkNum = userDkNum;
    }

    public BigDecimal getDzMoneyAll() {
        return dzMoneyAll;
    }

    public void setDzMoneyAll(BigDecimal dzMoneyAll) {
        this.dzMoneyAll = dzMoneyAll;
    }

    public BigDecimal getDzMoneyReturn() {
        return dzMoneyReturn;
    }

    public void setDzMoneyReturn(BigDecimal dzMoneyReturn) {
        this.dzMoneyReturn = dzMoneyReturn;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public BigDecimal getCuMoney() {
        return cuMoney;
    }

    public void setCuMoney(BigDecimal cuMoney) {
        this.cuMoney = cuMoney;
    }

    public BigDecimal getReturnMoney() {
        return returnMoney;
    }

    public void setReturnMoney(BigDecimal returnMoney) {
        this.returnMoney = returnMoney;
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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }
}

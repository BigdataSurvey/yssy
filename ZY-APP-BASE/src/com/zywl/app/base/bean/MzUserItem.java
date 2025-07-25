package com.zywl.app.base.bean;

import com.zywl.app.base.BaseBean;

import java.math.BigDecimal;
import java.util.Date;

public class MzUserItem extends BaseBean {

    private Long id;

    private Long userId;

    private Long mzItemId;

    private String name;

    private String context;

    private int icon;

    private String lastUserNo;

    private String lastUserName;

    private Date createTime;

    private int status;

    private BigDecimal price;

    private Date upTime;

    private Date upEndTime;


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

    public String getLastUserNo() {
        return lastUserNo;
    }

    public void setLastUserNo(String lastUserNo) {
        this.lastUserNo = lastUserNo;
    }

    public String getLastUserName() {
        return lastUserName;
    }

    public void setLastUserName(String lastUserName) {
        this.lastUserName = lastUserName;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Date getUpTime() {
        return upTime;
    }

    public void setUpTime(Date upTime) {
        this.upTime = upTime;
    }

    public Date getUpEndTime() {
        return upEndTime;
    }

    public void setUpEndTime(Date upEndTime) {
        this.upEndTime = upEndTime;
    }

    public Long getMzItemId() {
        return mzItemId;
    }

    public void setMzItemId(Long mzItemId) {
        this.mzItemId = mzItemId;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}

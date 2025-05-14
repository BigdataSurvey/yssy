package com.zywl.app.base.bean;

import com.zywl.app.base.BaseBean;

import java.util.Date;

public class UserInviteInfo extends BaseBean {

    private Long id;

    private Long userId;

    private Integer issue;

    private Integer number;

    private Integer effectiveNumber;

    private Integer adNumber;

    private  Integer status;

    private Date createTime;

    private Date endTime;

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

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public Integer getEffectiveNumber() {
        return effectiveNumber;
    }

    public void setEffectiveNumber(Integer effectiveNumber) {
        this.effectiveNumber = effectiveNumber;
    }

    public Integer getAdNumber() {
        return adNumber;
    }

    public void setAdNumber(Integer adNumber) {
        this.adNumber = adNumber;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Integer getIssue() {
        return issue;
    }

    public void setIssue(Integer issue) {
        this.issue = issue;
    }
}

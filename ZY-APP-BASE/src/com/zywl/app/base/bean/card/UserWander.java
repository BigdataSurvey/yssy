package com.zywl.app.base.bean.card;

import com.alibaba.fastjson2.JSONArray;
import com.zywl.app.base.BaseBean;

import java.util.Date;

public class UserWander extends BaseBean {

    private Long id;

    private Long userId;

    private Long petId;

    private Integer pl;

    private int plCost;
    private int wanderType;

    private Long wanderId;



    private int maxPl;


    private Date nextPlTime;



    private Integer outputTime;

    private Integer status;


    private Date createTime;

    private Date dieTime;

    private Date updateTime;

    private Long nextTime;

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

    public Long getPetId() {
        return petId;
    }

    public void setPetId(Long petId) {
        this.petId = petId;
    }

    public Integer getPl() {
        return pl;
    }

    public void setPl(Integer pl) {
        this.pl = pl;
    }

    public Date getNextPlTime() {
        return nextPlTime;
    }

    public void setNextPlTime(Date nextPlTime) {
        this.nextPlTime = nextPlTime;
    }



    public Integer getOutputTime() {
        return outputTime;
    }

    public void setOutputTime(Integer outputTime) {
        this.outputTime = outputTime;
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

    public Date getDieTime() {
        return dieTime;
    }

    public void setDieTime(Date dieTime) {
        this.dieTime = dieTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Long getNextTime() {
        return nextTime;
    }

    public void setNextTime(Long nextTime) {
        this.nextTime = nextTime;
    }

    public int getWanderType() {
        return wanderType;
    }

    public void setWanderType(int wanderType) {
        this.wanderType = wanderType;
    }

    public Long getWanderId() {
        return wanderId;
    }

    public void setWanderId(Long wanderId) {
        this.wanderId = wanderId;
    }


    public int getMaxPl() {
        return maxPl;
    }

    public void setMaxPl(int maxPl) {
        this.maxPl = maxPl;
    }

    public int getPlCost() {
        return plCost;
    }

    public void setPlCost(int plCost) {
        this.plCost = plCost;
    }
}

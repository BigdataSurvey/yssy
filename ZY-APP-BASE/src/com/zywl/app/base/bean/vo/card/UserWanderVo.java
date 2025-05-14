package com.zywl.app.base.bean.vo.card;

import com.zywl.app.base.BaseBean;

import java.util.Date;

public class UserWanderVo extends BaseBean {

    private Long id;

    private Long userId;

    private Long petId;

    private Integer pl;

    private int plCost;

    private int maxPl;

    private int wanderType;

    private Long wanderId;



    private Long nextPlTime;




    private Integer outputTime;

    private Integer status;


    private Long createTime;

    private Long dieTime;


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

    public int getMaxPl() {
        return maxPl;
    }

    public void setMaxPl(int maxPl) {
        this.maxPl = maxPl;
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

    public Long getNextPlTime() {
        return nextPlTime;
    }

    public void setNextPlTime(Long nextPlTime) {
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

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Long getDieTime() {
        return dieTime;
    }

    public void setDieTime(Long dieTime) {
        this.dieTime = dieTime;
    }

    public int getPlCost() {
        return plCost;
    }

    public void setPlCost(int plCost) {
        this.plCost = plCost;
    }
}

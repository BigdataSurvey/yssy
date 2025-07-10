package com.zywl.app.base.bean;

import java.math.BigDecimal;
import java.util.Date;

public class Monster {

    private Integer id;


    private Long monsterNo;

    private Integer monsterType;

    private Integer currBlood;


    private long dieStatus;

    private Date createTime;

    private Date updateTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Long getMonsterNo() {
        return monsterNo;
    }

    public void setMonsterNo(Long monsterNo) {
        this.monsterNo = monsterNo;
    }

    public Integer getMonsterType() {
        return monsterType;
    }

    public void setMonsterType(Integer monsterType) {
        this.monsterType = monsterType;
    }

    public Integer getCurrBlood() {
        return currBlood;
    }

    public void setCurrBlood(Integer currBlood) {
        this.currBlood = currBlood;
    }

    public long getDieStatus() {
        return dieStatus;
    }

    public void setDieStatus(long dieStatus) {
        this.dieStatus = dieStatus;
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
}

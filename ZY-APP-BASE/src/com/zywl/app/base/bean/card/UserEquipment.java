package com.zywl.app.base.bean.card;

import com.zywl.app.base.BaseBean;

import java.util.Date;

public class UserEquipment extends BaseBean {

    private Long id;

    private Long userId;

    private Long equId;

    private int number;

    private int equPosition;

    private Date createTime;

    private Date updateTime;

    private int species=1;

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

    public Long getEquId() {
        return equId;
    }

    public void setEquId(Long equId) {
        this.equId = equId;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
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

    public int getEquPosition() {
        return equPosition;
    }

    public void setEquPosition(int equPosition) {
        this.equPosition = equPosition;
    }

    public int getSpecies() {
        return species;
    }

    public void setSpecies(int species) {
        this.species = species;
    }
}

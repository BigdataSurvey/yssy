package com.zywl.app.base.bean.vo;

import com.zywl.app.base.BaseBean;

public class FightingVo extends BaseBean {

    private Long immortalGateId;

    private Long toImmortalGateId;

    private Long power;

    private Long toPower;

    private Integer attribute;

    private Integer toAttribute;
    //1 克制对方 2 被克制 3 双方无事
    private Integer fightingStatus;

    private Long newPower;

    private Long newToPower;

    private String desc;
    //true 是抢占成功 、false 抢占失败
    private boolean flag;

    public Long getImmortalGateId() {
        return immortalGateId;
    }

    public void setImmortalGateId(Long immortalGateId) {
        this.immortalGateId = immortalGateId;
    }

    public Long getToImmortalGateId() {
        return toImmortalGateId;
    }

    public void setToImmortalGateId(Long toImmortalGateId) {
        this.toImmortalGateId = toImmortalGateId;
    }

    public Long getPower() {
        return power;
    }

    public void setPower(Long power) {
        this.power = power;
    }

    public Long getToPower() {
        return toPower;
    }

    public void setToPower(Long toPower) {
        this.toPower = toPower;
    }

    public Integer getAttribute() {
        return attribute;
    }

    public void setAttribute(Integer attribute) {
        this.attribute = attribute;
    }

    public Integer getToAttribute() {
        return toAttribute;
    }

    public void setToAttribute(Integer toAttribute) {
        this.toAttribute = toAttribute;
    }

    public Integer getFightingStatus() {
        return fightingStatus;
    }

    public void setFightingStatus(Integer fightingStatus) {
        this.fightingStatus = fightingStatus;
    }

    public Long getNewPower() {
        return newPower;
    }

    public void setNewPower(Long newPower) {
        this.newPower = newPower;
    }

    public Long getNewToPower() {
        return newToPower;
    }

    public void setNewToPower(Long newToPower) {
        this.newToPower = newToPower;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }
}

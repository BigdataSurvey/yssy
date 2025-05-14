package com.zywl.app.base.bean.card;

import com.alibaba.fastjson2.JSONArray;
import com.zywl.app.base.BaseBean;

public class DicPetWander extends BaseBean {
    private Long id;

    private Integer lv;

    private String name;

    private String desc;

    private Integer maxPl;

    private Integer startPl;

    private Integer plCost;

    private Integer type;

    private Integer nextId;

    private JSONArray cost;


    private JSONArray reward;

    private Integer outputTime;

    private Long survivalTime;

    private Long power;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getLv() {
        return lv;
    }

    public void setLv(Integer lv) {
        this.lv = lv;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Integer getMaxPl() {
        return maxPl;
    }

    public void setMaxPl(Integer maxPl) {
        this.maxPl = maxPl;
    }

    public Integer getPlCost() {
        return plCost;
    }

    public void setPlCost(Integer plCost) {
        this.plCost = plCost;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getNextId() {
        return nextId;
    }

    public void setNextId(Integer nextId) {
        this.nextId = nextId;
    }

    public JSONArray getCost() {
        return cost;
    }

    public void setCost(JSONArray cost) {
        this.cost = cost;
    }


    public JSONArray getReward() {
        return reward;
    }

    public void setReward(JSONArray reward) {
        this.reward = reward;
    }

    public Integer getOutputTime() {
        return outputTime;
    }

    public void setOutputTime(Integer outputTime) {
        this.outputTime = outputTime;
    }

    public Long getSurvivalTime() {
        return survivalTime;
    }

    public void setSurvivalTime(Long survivalTime) {
        this.survivalTime = survivalTime;
    }

    public Long getPower() {
        return power;
    }

    public void setPower(Long power) {
        this.power = power;
    }

    public Integer getStartPl() {
        return startPl;
    }

    public void setStartPl(Integer startPl) {
        this.startPl = startPl;
    }
}

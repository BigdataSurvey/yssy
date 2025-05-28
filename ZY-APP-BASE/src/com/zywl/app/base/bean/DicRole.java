package com.zywl.app.base.bean;

import com.alibaba.fastjson2.JSONArray;
import com.zywl.app.base.BaseBean;

public class DicRole extends BaseBean {

    private Long id;

    private String name;

    private int skill;

    private int hp;

    private int cost;

    private JSONArray reward;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSkill() {
        return skill;
    }

    public void setSkill(int skill) {
        this.skill = skill;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public JSONArray getReward() {
        return reward;
    }

    public void setReward(JSONArray reward) {
        this.reward = reward;
    }
}

package com.zywl.app.base.bean.card;

import com.alibaba.fastjson2.JSONArray;
import com.zywl.app.base.BaseBean;

public class DicTimeTask extends BaseBean {

    private Long id;

    private String context;

    private int condition;

    private JSONArray reward;

    private int group;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public int getCondition() {
        return condition;
    }

    public void setCondition(int condition) {
        this.condition = condition;
    }

    public JSONArray getReward() {
        return reward;
    }

    public void setReward(JSONArray reward) {
        this.reward = reward;
    }

    public int getGroup() {
        return group;
    }

    public void setGroup(int group) {
        this.group = group;
    }
}

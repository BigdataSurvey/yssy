package com.zywl.app.base.bean;

import com.alibaba.fastjson2.JSONArray;
import com.zywl.app.base.BaseBean;

public class DicVip extends BaseBean {

    private int lv;

    private int beginExp;

    private int endExp;

    private JSONArray reward;

    public int getLv() {
        return lv;
    }

    public void setLv(int lv) {
        this.lv = lv;
    }

    public int getBeginExp() {
        return beginExp;
    }

    public void setBeginExp(int beginExp) {
        this.beginExp = beginExp;
    }

    public int getEndExp() {
        return endExp;
    }

    public void setEndExp(int endExp) {
        this.endExp = endExp;
    }

    public JSONArray getReward() {
        return reward;
    }

    public void setReward(JSONArray reward) {
        this.reward = reward;
    }
}

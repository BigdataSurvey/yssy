package com.zywl.app.base.bean;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.BaseBean;

public class DicPirzeDraw extends BaseBean {

    private Long id;

    private JSONObject reward;

    private int rate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public JSONObject getReward() {
        return reward;
    }

    public void setReward(JSONObject reward) {
        this.reward = reward;
    }

    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }
}

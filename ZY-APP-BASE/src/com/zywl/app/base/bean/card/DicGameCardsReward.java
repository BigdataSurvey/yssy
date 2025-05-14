package com.zywl.app.base.bean.card;

import com.alibaba.fastjson2.JSONArray;
import com.zywl.app.base.BaseBean;

public class DicGameCardsReward extends BaseBean {

    private Long id;

    private int type;

    private int hostageNum;

    private JSONArray reward;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getHostageNum() {
        return hostageNum;
    }

    public void setHostageNum(int hostageNum) {
        this.hostageNum = hostageNum;
    }

    public JSONArray getReward() {
        return reward;
    }

    public void setReward(JSONArray reward) {
        this.reward = reward;
    }
}

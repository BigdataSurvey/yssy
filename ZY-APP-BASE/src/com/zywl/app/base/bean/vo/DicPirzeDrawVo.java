package com.zywl.app.base.bean.vo;

import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.BaseBean;

public class DicPirzeDrawVo extends BaseBean {

    private Long id;

    private JSONObject reward;


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

}

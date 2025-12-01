package com.zywl.app.base.bean;

import com.alibaba.fastjson2.JSONArray;
import com.zywl.app.base.BaseBean;
/**
 * 手册每日奖励信息
 * **/
public class DicHandBookReward extends BaseBean {


    private Long id;

    private Long handbookId;

    private int dayNum;

    private JSONArray reward;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getHandbookId() {
        return handbookId;
    }

    public void setHandbookId(Long handbookId) {
        this.handbookId = handbookId;
    }

    public int getDayNum() {
        return dayNum;
    }

    public void setDayNum(int dayNum) {
        this.dayNum = dayNum;
    }

    public JSONArray getReward() {
        return reward;
    }

    public void setReward(JSONArray reward) {
        this.reward = reward;
    }
}

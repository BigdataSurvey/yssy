package com.zywl.app.base.bean;

import com.alibaba.fastjson2.JSONArray;

public class DicZyt {

    private Integer highNum;
    private JSONArray progress;

    public Integer getHighNum() {
        return highNum;
    }

    public void setHighNum(Integer highNum) {
        this.highNum = highNum;
    }

    public JSONArray getProgress() {
        return progress;
    }

    public void setProgress(JSONArray progress) {
        this.progress = progress;
    }
}

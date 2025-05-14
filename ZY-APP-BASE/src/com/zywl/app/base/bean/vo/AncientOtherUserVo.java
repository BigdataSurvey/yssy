package com.zywl.app.base.bean.vo;

import com.alibaba.fastjson2.JSONObject;

import java.util.Date;

public class AncientOtherUserVo {

    private Long userId;

    private String userNo;

    private String name;

    private Long hakesId;


    private String headImgUrl;

    private Date jionTime;



    private int userLv;


    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserNo() {
        return userNo;
    }

    public void setUserNo(String userNo) {
        this.userNo = userNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHeadImgUrl() {
        return headImgUrl;
    }

    public void setHeadImgUrl(String headImgUrl) {
        this.headImgUrl = headImgUrl;
    }


    public int getUserLv() {
        return userLv;
    }

    public void setUserLv(int userLv) {
        this.userLv = userLv;
    }

    public Date getJionTime() {
        return jionTime;
    }

    public void setJionTime(Date jionTime) {
        this.jionTime = jionTime;
    }

    public Long getHakesId() {
        return hakesId;
    }

    public void setHakesId(Long hakesId) {
        this.hakesId = hakesId;
    }
}

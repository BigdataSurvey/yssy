package com.zywl.app.bean;

public class UserInfo {
    private String userId;
    private String userName;
    private String userNo;
    private String headUrl;
    private Integer chairId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserNo() {
        return userNo;
    }

    public void setUserNo(String userNo) {
        this.userNo = userNo;
    }

    public String getHeadUrl() {
        return headUrl;
    }

    public void setHeadUrl(String headUrl) {
        this.headUrl = headUrl;
    }

    public Integer getChairId() {
        return chairId;
    }

    public void setChairId(Integer chairId) {
        this.chairId = chairId;
    }
}

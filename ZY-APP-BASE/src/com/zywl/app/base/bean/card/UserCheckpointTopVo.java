package com.zywl.app.base.bean.card;

import com.zywl.app.base.BaseBean;

public class UserCheckpointTopVo extends BaseBean {

    private Long userId;

    private String userNo;

    private int roleId;

    private String userName;

    private String userHeadImg;

    private Long checkpointId;

    private int isPopular;

    public int getIsPopular() {
        return isPopular;
    }

    public void setIsPopular(int isPopular) {
        this.isPopular = isPopular;
    }

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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserHeadImg() {
        return userHeadImg;
    }

    public void setUserHeadImg(String userHeadImg) {
        this.userHeadImg = userHeadImg;
    }

    public Long getCheckpoint() {
        return checkpointId;
    }

    public void setCheckpoint(Long checkpointId) {
        this.checkpointId = checkpointId;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public Long getCheckpointId() {
        return checkpointId;
    }

    public void setCheckpointId(Long checkpointId) {
        this.checkpointId = checkpointId;
    }
}

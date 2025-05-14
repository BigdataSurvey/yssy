package com.zywl.app.base.bean.vo;

import com.zywl.app.base.BaseBean;

import java.math.BigDecimal;

public class AnimaTopVo extends BaseBean {

    private Long userId;

    private String userNo;

    private String userName;

    private String userHeadImg;

    private BigDecimal getAnima;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

    public BigDecimal getGetAnima() {
        return getAnima;
    }

    public void setGetAnima(BigDecimal getAnima) {
        this.getAnima = getAnima;
    }

    public String getUserNo() {
        return userNo;
    }

    public void setUserNo(String userNo) {
        this.userNo = userNo;
    }
}

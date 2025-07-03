package com.zywl.app.base.bean;

import com.zywl.app.base.BaseBean;

import java.util.Date;

public class UserRoleAd extends BaseBean {

    private Long id;

    private Long userId;

    private String   ymd;

    private int canLook;

    private int look;

    private Date lastTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getYmd() {
        return ymd;
    }

    public void setYmd(String ymd) {
        this.ymd = ymd;
    }

    public int getCanLook() {
        return canLook;
    }

    public void setCanLook(int canLook) {
        this.canLook = canLook;
        if (this.canLook>4){
            this.canLook=4;
        }
    }

    public int getLook() {
        return look;
    }

    public void setLook(int look) {
        this.look = look;
    }

    public Date getLastTime() {
        return lastTime;
    }

    public void setLastTime(Date lastTime) {
        this.lastTime = lastTime;
    }
}

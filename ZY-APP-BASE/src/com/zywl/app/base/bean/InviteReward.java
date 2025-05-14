package com.zywl.app.base.bean;

import com.zywl.app.base.BaseBean;

import java.math.BigDecimal;

public class InviteReward extends BaseBean {

    private Long id;

    private Integer friendNum;

    private Integer adNum;

    private BigDecimal reward;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getFriendNum() {
        return friendNum;
    }

    public void setFriendNum(Integer friendNum) {
        this.friendNum = friendNum;
    }

    public Integer getAdNum() {
        return adNum;
    }

    public void setAdNum(Integer adNum) {
        this.adNum = adNum;
    }

    public BigDecimal getReward() {
        return reward;
    }

    public void setReward(BigDecimal reward) {
        this.reward = reward;
    }
}

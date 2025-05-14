package com.zywl.app.base.bean.vo.card;

import com.zywl.app.base.BaseBean;

import java.util.Date;

public class UserPetVo extends BaseBean {


    private Long id;


    private Long petId;

    private int lv;

    private int star;

    private int power;

    private Long playerCardId;

    public Long getPlayerCardId() {
        return playerCardId;
    }

    public void setPlayerCardId(Long playerCardId) {
        this.playerCardId = playerCardId;
    }

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public Long getPetId() {
        return petId;
    }

    public void setPetId(Long petId) {
        this.petId = petId;
    }

    public int getLv() {
        return lv;
    }

    public void setLv(int lv) {
        this.lv = lv;
    }

    public int getStar() {
        return star;
    }

    public void setStar(int star) {
        this.star = star;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}

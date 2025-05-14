package com.zywl.app.base.bean.vo.card;

import com.zywl.app.base.BaseBean;

public class PlayerCardCodexVo extends BaseBean {


    private Long id;

    private int lv;


    private Long cardId;

    private int quality;

    private int star;
    private int power;

    private int isDeploy;

    public int getIsDeploy() {
        return isDeploy;
    }

    public void setIsDeploy(int isDeploy) {
        this.isDeploy = isDeploy;
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

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public int getQuality() {
        return quality;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }

    public int getLv() {
        return lv;
    }

    public void setLv(int lv) {
        this.lv = lv;
    }

    public Long getCardId() {
        return cardId;
    }

    public void setCardId(Long cardId) {
        this.cardId = cardId;
    }
}

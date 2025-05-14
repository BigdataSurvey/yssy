package com.zywl.app.base.bean.vo.card;

import com.zywl.app.base.BaseBean;

public class LineupCardVo extends BaseBean {

    private long id;
    private long cardId;

    private int lv;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCardId() {
        return cardId;
    }

    public void setCardId(long cardId) {
        this.cardId = cardId;
    }

    public int getLv() {
        return lv;
    }

    public void setLv(int lv) {
        this.lv = lv;
    }

}

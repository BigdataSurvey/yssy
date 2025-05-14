package com.zywl.app.base.bean.card;

import com.zywl.app.base.BaseBean;

public class DicDrawProbability extends BaseBean {

    private Long id;

    private int cardPool;

    private int quality;

    private int rate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getQuality() {
        return quality;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }

    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    public int getCardPool() {
        return cardPool;
    }

    public void setCardPool(int cardPool) {
        this.cardPool = cardPool;
    }
}

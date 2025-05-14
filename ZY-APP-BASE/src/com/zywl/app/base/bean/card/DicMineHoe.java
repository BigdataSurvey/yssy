package com.zywl.app.base.bean.card;

import com.zywl.app.base.BaseBean;

public class DicMineHoe extends BaseBean {

    private Long itemId;

    private int lv;

    private int hour;

    private int vipHour;

    private int useNumber;

    private int vipUseNumber;

    public int getUseNumber() {
        return useNumber;
    }

    public void setUseNumber(int useNumber) {
        this.useNumber = useNumber;
    }

    public int getVipUseNumber() {
        return vipUseNumber;
    }

    public void setVipUseNumber(int vipUseNumber) {
        this.vipUseNumber = vipUseNumber;
    }

    public int getVipHour() {
        return vipHour;
    }

    public void setVipHour(int vipHour) {
        this.vipHour = vipHour;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public int getLv() {
        return lv;
    }

    public void setLv(int lv) {
        this.lv = lv;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }
}

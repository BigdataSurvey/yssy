package com.zywl.app.base.bean.card;

import com.zywl.app.base.BaseBean;

public class DicShopType extends BaseBean {
    /**
     * 商店类型ID
     * **/
    private int shopType;

    /**
     * 商店名字
     * **/
    private String name;

    /**
     * 商店归类
     * **/
    private  int type;

    public int getShopType() {
        return shopType;
    }

    public void setShopType(int shopType) {
        this.shopType = shopType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}

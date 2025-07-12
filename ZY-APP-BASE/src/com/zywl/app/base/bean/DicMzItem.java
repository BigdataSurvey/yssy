package com.zywl.app.base.bean;

import com.zywl.app.base.BaseBean;

import java.math.BigDecimal;

public class DicMzItem extends BaseBean {

    private Long id;

    private String name;

    private String context;

    private String type;

    private int lv;

    private int isShop;

    private int shopNumber;

    private BigDecimal price;

    private int isTrad;

    private int icon;

    private BigDecimal tradPrice;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public int getIsShop() {
        return isShop;
    }

    public void setIsShop(int isShop) {
        this.isShop = isShop;
    }

    public int getShopNumber() {
        return shopNumber;
    }

    public void setShopNumber(int shopNumber) {
        this.shopNumber = shopNumber;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getIsTrad() {
        return isTrad;
    }

    public void setIsTrad(int isTrad) {
        this.isTrad = isTrad;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public BigDecimal getTradPrice() {
        return tradPrice;
    }

    public void setTradPrice(BigDecimal tradPrice) {
        this.tradPrice = tradPrice;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getLv() {
        return lv;
    }

    public void setLv(int lv) {
        this.lv = lv;
    }
}

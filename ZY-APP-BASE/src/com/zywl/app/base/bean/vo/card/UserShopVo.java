package com.zywl.app.base.bean.vo.card;

import com.zywl.app.base.BaseBean;

import java.math.BigDecimal;

public class UserShopVo extends BaseBean {


    private Long id;
    private Long userId;
    private Long itemEquId;

    private int type;

    private int shopType;

    private int number;

    private int price;




    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public Long getItemEquId() {
        return itemEquId;
    }

    public void setItemEquId(Long itemEquId) {
        this.itemEquId = itemEquId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getShopType() {
        return shopType;
    }

    public void setShopType(int shopType) {
        this.shopType = shopType;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}

package com.zywl.app.base.bean.card;

import com.zywl.app.base.BaseBean;

import java.math.BigDecimal;
/**
 * 商店配置表
 * **/
public class DicShop extends BaseBean {

    private Long id;

    /**
     * 实际卖出商品的道具Id
     * **/
    private Long itemId;

    /**
     * 支付时消耗的"货币"道具/资产  ID
     * **/
    private Long useItemId;


    /**
     * 商店类型
     * **/
    private int shopType;

    /**
     * 一次购买的数量
     * **/
    private int number;

    /**
     * 单价
     * **/
    private BigDecimal price;

    /**
     * 是否在前端展示 1展示 0隐藏
     * **/
    private Integer isShow;

    /**
     * 是否允许购买 1可购买 0不可购买
     * **/
    private Integer canBuy;

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
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

    public Long getUseItemId() {
        return useItemId;
    }

    public void setUseItemId(Long useItemId) {
        this.useItemId = useItemId;
    }

    public Integer getIsShow() {
        return isShow;
    }

    public void setIsShow(Integer isShow) {
        this.isShow = isShow;
    }

    public Integer getCanBuy() {
        return canBuy;
    }

    public void setCanBuy(Integer canBuy) {
        this.canBuy = canBuy;
    }
}

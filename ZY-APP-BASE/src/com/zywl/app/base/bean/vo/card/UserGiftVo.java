package com.zywl.app.base.bean.vo.card;

import com.alibaba.fastjson2.JSONArray;
import com.zywl.app.base.BaseBean;

import java.math.BigDecimal;
import java.util.Date;

public class UserGiftVo extends BaseBean {

    private Long id;

    private String name;

    private JSONArray itemInfo;

    private Long useItem;

    private BigDecimal price;

    private int type;

    private int maxNumber;

    private int buyNumber;

    private String updateTime;

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

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

    public JSONArray getItemInfo() {
        return itemInfo;
    }

    public void setItemInfo(JSONArray itemInfo) {
        this.itemInfo = itemInfo;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getMaxNumber() {
        return maxNumber;
    }

    public void setMaxNumber(int maxNumber) {
        this.maxNumber = maxNumber;
    }

    public int getBuyNumber() {
        return buyNumber;
    }

    public void setBuyNumber(int buyNumber) {
        this.buyNumber = buyNumber;
    }

    public Long getUseItem() {
        return useItem;
    }

    public void setUseItem(Long useItem) {
        this.useItem = useItem;
    }
}

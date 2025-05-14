package com.zywl.app.base.bean.card;

import com.alibaba.fastjson2.JSONArray;
import com.zywl.app.base.BaseBean;

public class DicPet extends BaseBean {

    private Long id;

    private String pic;

    private String name;

    private String context;

    private JSONArray needItem;

    private int maxLv;

    private int maxStar;

    private int quality;

    private double wanderBonus;

    private int wanderRestore;

    public int getWanderRestore() {
        return wanderRestore;
    }

    public void setWanderRestore(int wanderRestore) {
        this.wanderRestore = wanderRestore;
    }

    public double getWanderBonus() {
        return wanderBonus;
    }

    public void setWanderBonus(double wanderBonus) {
        this.wanderBonus = wanderBonus;
    }

    private JSONArray wanderFeed;

    public JSONArray getWanderFeed() {
        return wanderFeed;
    }

    public void setWanderFeed(JSONArray wanderFeed) {
        this.wanderFeed = wanderFeed;
    }

    public int getQuality() {
        return quality;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public JSONArray getNeedItem() {
        return needItem;
    }

    public void setNeedItem(JSONArray needItem) {
        this.needItem = needItem;
    }

    public int getMaxLv() {
        return maxLv;
    }

    public void setMaxLv(int maxLv) {
        this.maxLv = maxLv;
    }

    public int getMaxStar() {
        return maxStar;
    }

    public void setMaxStar(int maxStar) {
        this.maxStar = maxStar;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }
}

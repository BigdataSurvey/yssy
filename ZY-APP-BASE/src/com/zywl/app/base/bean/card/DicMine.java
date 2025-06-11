package com.zywl.app.base.bean.card;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.BaseBean;

import java.math.BigDecimal;

public class DicMine extends BaseBean {

    private Long id;

    private String name;

    private int lv;

    private Long nextId;

    private JSONArray reward;

    private BigDecimal costMoney;

    private JSONArray costItem;

    private int count;

    private int index;

    private String miningItem;

    private int miningItemCount;

    private BigDecimal rewardFriend;

    private BigDecimal seasonRewardFriend;

    public BigDecimal getRewardFriend() {
        return rewardFriend;
    }

    public void setRewardFriend(BigDecimal rewardFriend) {
        this.rewardFriend = rewardFriend;
    }

    public BigDecimal getSeasonRewardFriend() {
        return seasonRewardFriend;
    }

    public void setSeasonRewardFriend(BigDecimal seasonRewardFriend) {
        this.seasonRewardFriend = seasonRewardFriend;
    }

    public String getMiningItem() {
        return miningItem;
    }

    public void setMiningItem(String miningItem) {
        this.miningItem = miningItem;
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

    public int getLv() {
        return lv;
    }

    public void setLv(int lv) {
        this.lv = lv;
    }

    public Long getNextId() {
        return nextId;
    }

    public void setNextId(Long nextId) {
        this.nextId = nextId;
    }

    public JSONArray getReward() {
        return reward;
    }

    public void setReward(JSONArray reward) {
        this.reward = reward;
    }

    public BigDecimal getCostMoney() {
        return costMoney;
    }

    public void setCostMoney(BigDecimal costMoney) {
        this.costMoney = costMoney;
    }

    public JSONArray getCostItem() {
        return costItem;
    }

    public void setCostItem(JSONArray costItem) {
        this.costItem = costItem;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getMiningItemCount() {
        return miningItemCount;
    }

    public void setMiningItemCount(int miningItemCount) {
        this.miningItemCount = miningItemCount;
    }
}

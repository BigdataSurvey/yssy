package com.zywl.app.bean;

import java.util.List;

public class FoodInfo {
    private String userId;
    private Integer chairId;
    private List<Integer> foodInfo;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Integer getChairId() {
        return chairId;
    }

    public void setChairId(Integer chairId) {
        this.chairId = chairId;
    }

    public List<Integer> getFoodInfo() {
        return foodInfo;
    }

    public void setFoodInfo(List<Integer> foodInfo) {
        this.foodInfo = foodInfo;
    }
}

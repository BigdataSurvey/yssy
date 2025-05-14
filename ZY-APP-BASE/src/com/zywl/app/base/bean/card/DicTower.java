package com.zywl.app.base.bean.card;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.BaseBean;

import java.util.ArrayList;
import java.util.List;

public class DicTower extends BaseBean {

    private Long floor;

    private Long cardId;

    private Long card1;

    private Long card2;

    private Long card3;

    private Long card4;

    private Long card5;

    private int allPower;

    private JSONArray reward;

    private JSONArray stageReward;

    private JSONObject firstReward;

    public JSONObject getFirstReward() {
        return firstReward;
    }

    public void setFirstReward(JSONObject firstReward) {
        this.firstReward = firstReward;
    }

    public JSONArray getStageReward() {
        return stageReward;
    }

    public void setStageReward(JSONArray stageReward) {
        this.stageReward = stageReward;
    }

    public int getAllPower() {
        return allPower;
    }

    public void setAllPower(int allPower) {
        this.allPower = allPower;
    }

    public JSONArray getReward() {
        return reward;
    }

    public void setReward(JSONArray reward) {
        this.reward = reward;
    }

    public List<Long> getCardIds(){
        List<Long> list = new ArrayList<>();
        list.add(card1);
        list.add(card2);
        list.add(card3);
        list.add(card4);
        list.add(card5);
        return list;
    }

    public Long getFloor() {
        return floor;
    }

    public void setFloor(Long floor) {
        this.floor = floor;
    }

    public Long getCardId() {
        return cardId;
    }

    public void setCardId(Long cardId) {
        this.cardId = cardId;
    }

    public Long getCard1() {
        return card1;
    }

    public void setCard1(Long card1) {
        this.card1 = card1;
    }

    public Long getCard2() {
        return card2;
    }

    public void setCard2(Long card2) {
        this.card2 = card2;
    }

    public Long getCard3() {
        return card3;
    }

    public void setCard3(Long card3) {
        this.card3 = card3;
    }

    public Long getCard4() {
        return card4;
    }

    public void setCard4(Long card4) {
        this.card4 = card4;
    }

    public Long getCard5() {
        return card5;
    }

    public void setCard5(Long card5) {
        this.card5 = card5;
    }
}

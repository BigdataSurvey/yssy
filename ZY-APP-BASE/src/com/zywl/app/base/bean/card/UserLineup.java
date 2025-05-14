package com.zywl.app.base.bean.card;

import com.alibaba.fastjson2.JSONArray;
import com.zywl.app.base.BaseBean;

import java.util.ArrayList;
import java.util.List;

public class UserLineup extends BaseBean {

    private Long id;

    private Long userId;

    private Long card1;

    private Long card2;

    private Long card3;

    private Long card4;

    private Long card5;

    private JSONArray cardIds;

    public JSONArray getCardIds() {
        JSONArray array = new JSONArray();
        array.add(card1);
        array.add(card2);
        array.add(card3);
        array.add(card4);
        array.add(card5);
        return array;
    }

    public void setCardIds(JSONArray cardIds) {
        this.cardIds = cardIds;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getCard1() {
        return card1;
    }

    public List<Long> getCards() {
        List<Long> list = new ArrayList<>();
        list.add(card1);
        list.add(card2);
        list.add(card3);
        list.add(card4);
        list.add(card5);
        return list;
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

    public boolean isFight(String id) {
        if (id.equals(card1.toString()) || id.equals(card2.toString()) || id.equals(card3.toString()) || id.equals(card4.toString()) || id.equals(card5.toString())) {
            return true;
        }
        return false;
    }



}

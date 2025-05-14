package com.zywl.app.base.bean.card;

import com.zywl.app.base.BaseBean;

import java.util.HashMap;
import java.util.Map;

public class RedDotState extends BaseBean {

    boolean hasRedDot;
    Map<Integer, Boolean> cardRedDots = new HashMap<>(); // 每张卡牌的红点状态
    Map<Integer, Boolean> equipmentRedDots = new HashMap<>(); // 每个装备位置的红点状态

    public boolean isHasRedDot() {
        return hasRedDot;
    }

    public void setHasRedDot(boolean hasRedDot) {
        this.hasRedDot = hasRedDot;
    }

    public Map<Integer, Boolean> getCardRedDots() {
        return cardRedDots;
    }

    public void setCardRedDots(Map<Integer, Boolean> cardRedDots) {
        this.cardRedDots = cardRedDots;
    }

    public Map<Integer, Boolean> getEquipmentRedDots() {
        return equipmentRedDots;
    }

    public void setEquipmentRedDots(Map<Integer, Boolean> equipmentRedDots) {
        this.equipmentRedDots = equipmentRedDots;
    }
}

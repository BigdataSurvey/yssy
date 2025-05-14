package com.zywl.app.base.bean.card;

import com.zywl.app.base.BaseBean;
import com.zywl.app.base.bean.Lv;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class UserLv extends BaseBean {

    private Long userId;

    private int lv;

    private Long exp;


    private static final List<Long> LEVEL_EXP = new ArrayList<>();


    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getExp() {
        return exp;
    }

    public void setExp(Long exp) {
        this.exp = exp;
    }


    public int getLv() {
        return lv;
    }

    public void setLv(int lv) {
        this.lv = lv;
    }


    public UserLv addExperience(long exp, Map<String, Lv> lvInfo) {
        if (LEVEL_EXP.size()==0){
            lvInfo.values().forEach(e->LEVEL_EXP.add(e.getCharacterExp()));
            Collections.sort(LEVEL_EXP);
        }
        this.exp += exp;
        updateLevel();
        return this;
    }

    // 更新等级
    private void updateLevel() {
        // 找到经验对应的最高等级
        while (lv < LEVEL_EXP.size()  && this.exp >= LEVEL_EXP.get(lv)) {
            exp -= ( LEVEL_EXP.get(lv)); // 扣除升级所需的经验
            lv++; // 升一级
        }
    }
}

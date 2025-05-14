package com.zywl.app.base.bean.card;

import com.zywl.app.base.BaseBean;

public class DicSet extends BaseBean {

    private Long id;

    private String setName;

    private String setBonus2;

    private String setBonus4;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSetName() {
        return setName;
    }

    public void setSetName(String setName) {
        this.setName = setName;
    }

    public String getSetBonus2() {
        return setBonus2;
    }

    public void setSetBonus2(String setBonus2) {
        this.setBonus2 = setBonus2;
    }

    public String getSetBonus4() {
        return setBonus4;
    }

    public void setSetBonus4(String setBonus4) {
        this.setBonus4 = setBonus4;
    }
}

package com.zywl.app.base.bean.vo.card;

import com.zywl.app.base.BaseBean;

public class EquipmentVo extends BaseBean {

    private Long id;

    private String name;

    private String context;

    private int position;

    private int starLv;

    private String basicAttribute;

    private int hasExtraAttribute;

    private String extraAttribute;


    private String pic;

    private int hasSet;

    private Long setId;

    private String setName;

    private String setBonus2;

    private String setBonus4;

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

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getStarLv() {
        return starLv;
    }

    public void setStarLv(int starLv) {
        this.starLv = starLv;
    }

    public String getBasicAttribute() {
        return basicAttribute;
    }

    public void setBasicAttribute(String basicAttribute) {
        this.basicAttribute = basicAttribute;
    }

    public int getHasExtraAttribute() {
        return hasExtraAttribute;
    }

    public void setHasExtraAttribute(int hasExtraAttribute) {
        this.hasExtraAttribute = hasExtraAttribute;
    }

    public String getExtraAttribute() {
        return extraAttribute;
    }

    public void setExtraAttribute(String extraAttribute) {
        this.extraAttribute = extraAttribute;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public int getHasSet() {
        return hasSet;
    }

    public void setHasSet(int hasSet) {
        this.hasSet = hasSet;
    }

    public Long getSetId() {
        return setId;
    }

    public void setSetId(Long setId) {
        this.setId = setId;
    }
}

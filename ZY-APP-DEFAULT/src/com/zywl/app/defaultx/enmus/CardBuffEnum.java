package com.zywl.app.defaultx.enmus;

/**
 * 对战事件
 * **/
public enum CardBuffEnum {

    zhiLiao("治疗",0),
    zhongDu("中毒",1),
    liuXue("流血",2),
    xuanYun("眩晕",3),
    zhuoShao("灼烧",4),
    mianShang ("免伤",5),
    gongJi("攻击",6),
    qingchu("清除",7)
    ;

    private CardBuffEnum(String name, int value) {
        this.name = name;
        this.value = value;
    }

    private String name;

    private int value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}

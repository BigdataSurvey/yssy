package com.zywl.app.defaultx.enmus;

public enum LuckUserEnum {

    LUCK_USER_NO_STATUS("不是幸运儿",1),
    LUCK_USER_YES_STATUS("是幸运儿",2)
    ;
    private LuckUserEnum(String name, int value) {
        this.name = name;
        this.value = value;
    }

    private String name;

    private Integer value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }
}

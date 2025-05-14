package com.zywl.app.defaultx.enmus;

/**
 * 副本类型枚举
 * **/
public enum FBTypeEnum {

    TOWER("试炼之塔","TOWER"),
    DAILY("日常副本","DAILY")
    ;

    private FBTypeEnum(String name, String value) {
        this.name = name;
        this.value = value;
    }

    private String name;

    private String value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

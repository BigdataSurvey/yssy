package com.zywl.app.defaultx.enmus;

/**
 * 副本类型枚举
 * **/
public enum TaskIdEnum {

    LOGIN("每日登录","1"),
    SHOP_BUY_NUMBER("商城购买1次道具","2"),

    ADD_POWER("补充一次体力","3"),
    DTS("参与一次倩女幽魂","4"),
    LHD("参与一次2选1","5"),
    SYN("合成一次","6")

    ;

    private TaskIdEnum(String name, String value) {
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

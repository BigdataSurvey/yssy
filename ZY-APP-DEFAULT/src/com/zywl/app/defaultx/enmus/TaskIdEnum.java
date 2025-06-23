package com.zywl.app.defaultx.enmus;

/**
 * 副本类型枚举
 * **/
public enum TaskIdEnum {

    LOGIN("每日登录","0"),
    SHOP_BUY_NUMBER("商城购买1次道具","1"),

    ADD_POWER("补充一次体力","2"),
    DTS("参与一次倩女幽魂","3"),
    LHD("参与一次2选1","4"),
    SYN("合成一次","5")

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

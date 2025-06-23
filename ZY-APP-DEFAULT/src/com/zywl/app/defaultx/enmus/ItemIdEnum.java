package com.zywl.app.defaultx.enmus;

/**
 * 道具ID配置
 **/
public enum ItemIdEnum {

    PVP_ITEM("挑战令", "3"),
    MONEY_1("银币", "1"),
    FRIEND("友情点", "7"),
    GOLD("金币", "2"),
    YYQ("游园券", "3"),
    XLB("小喇叭","40"),
    DLB("大喇叭", "41")


    ;


    private ItemIdEnum(String name, String value) {
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

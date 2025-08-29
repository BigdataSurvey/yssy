package com.zywl.app.defaultx.enmus;

/**
 * 道具ID配置
 **/
public enum ItemIdEnum {
    MONEY_1("银币", "1"),
    GOLD("金币", "2"),
    YYQ("游园券", "3"),
    WFSB("文房四宝","5"),
    DUST("金刚铃","53"),
    FRIEND("友情点", "7"),
    XG("信鸽","31"),
    XXF("小信封","32"),
    DXF("大信封","33"),
    XLB("小喇叭","40"),
    DLB("大喇叭", "12"),
    BUY_WHITE("购买凭证","43"),
    MZ_LZ("蒙尘的聊斋残卷","44"),
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

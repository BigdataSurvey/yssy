package com.zywl.app.defaultx.enmus;

/**
 * 打坐打卡领取签到状态
 * **/
public enum XmAttribute {

    MU("木属性",1),
    SHUI("水属性",2),
    HUO("火属性",3)
    ;

    private XmAttribute(String name, int value) {
        this.name = name;
        this.value = value;
    }

    public static String getName(int value) {
        XmAttribute[] ems = XmAttribute.values();
        for (XmAttribute xmAttribute : ems) {
            if (xmAttribute.getValue()==value) {
                return xmAttribute.name.toString();
            }
        }
        return null;
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

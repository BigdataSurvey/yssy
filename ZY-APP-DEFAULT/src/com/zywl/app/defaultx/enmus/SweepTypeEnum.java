package com.zywl.app.defaultx.enmus;

/**
 * 扫荡类型
 * **/
public enum SweepTypeEnum {

    TOWER("试炼之塔",1),
    PVP("演武场",2),
    EVERY_DAY_FB_1("日常副本_1",3),
    EVERY_DAY_FB_2("日常副本_2",4),
    EVERY_DAY_FB_3("日常副本_3",5),
    EVERY_DAY_FB_4("日常副本_4",6);

    private SweepTypeEnum(String name, int value) {
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

    public static String getNameByType(int type){
        SweepTypeEnum[] values = SweepTypeEnum.values();
        for (SweepTypeEnum sweepTypeEnum : values) {
            if (sweepTypeEnum.getValue()==type){
                return sweepTypeEnum.getName();
            }
        }
        return null;
    }



    public static int getFbByType(int type){
        if (type==1){
            return EVERY_DAY_FB_1.getValue();
        } else if (type==2) {
            return EVERY_DAY_FB_2.getValue();
        } else if (type==3) {
            return EVERY_DAY_FB_3.getValue();
        } else if (type==4) {
            return EVERY_DAY_FB_4.getValue();
        }
        return 0;
    }
}

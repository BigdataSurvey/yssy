package com.zywl.app.defaultx.enmus;

/**
 * 打坐打卡领取签到状态
 * **/
public enum DzStatus {

    BM_STATUS("报名",1),
    DK_STATUS("打卡",2),
    LQ_STATUS("领取",3),

    BM_H_STATUS("报名置灰",5),

    DK_H_STATUS("打卡置灰",4)

    ;

    private DzStatus(String name, int value) {
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

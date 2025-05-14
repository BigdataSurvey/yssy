package com.zywl.app.defaultx.enmus;

/**
 * 奖励类型
 * **/
public enum RewardTypeEnum {

    MAIL("邮件领取",1),
    CHECKPOINT("主线奖励",2),
    DISPATCH("派遣任务",3),
    TOWER_STAGE("试炼之塔层数奖励",4),
    SWEEP("副本扫荡",5);

    private RewardTypeEnum(String name, int value) {
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

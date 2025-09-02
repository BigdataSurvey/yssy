package com.zywl.app.defaultx.enmus;

/**
 * 副本类型枚举
 * **/
public enum TaskIdEnum {

    LOGIN("每日登录","0"),
    SDMZ("参与一次四大名著","1"),
    SHSG("参与一次山海狩怪","2"),
    DTS("参与一次倩女幽魂","3"),
    LHD("参与一次2选1","4"),
    KF("翻卡","5"),
    INVITE1("邀请5名好友","6"),
    INVITE2("邀请10名好友","7"),
    INVITE3("邀请20名好友","8"),
    GREAT_NOVELS("参与四大名著","1"),
    SEA_HUNT("参与山海狩怪","10"),
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

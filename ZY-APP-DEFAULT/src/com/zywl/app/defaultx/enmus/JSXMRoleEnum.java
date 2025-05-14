package com.zywl.app.defaultx.enmus;

public enum JSXMRoleEnum {
    IMMORTAL_GATE_TOU("掌门",3),
    IMMORTAL_GATE_TWO("长老",2),
    IMMORTAL_GATE_TOU_SONG("亲传弟子",1),
    IMMORTAL_GATE_MEMBER("试炼弟子",0);

    private JSXMRoleEnum(String name, int value) {
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

package com.zywl.app.defaultx.enmus;

public enum JSXMConstantEnum {

    IMMORTAL_GATE_CONTRIBUTION("捐献仙门资金",0),
    IMMORTAL_GATE_EXP("兑换仙门经验值",1),
    IMMORTAL_GATE_SHOP("购买仙门道具",2);

    private JSXMConstantEnum(String name, int value) {
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

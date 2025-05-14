package com.zywl.app.defaultx.enmus;

public enum JSXMEnum {

    CREATE_IMMORTAL_GATE("【%s】创建仙门",1),
    UPDATE_FIGHTING("【%s】更改战力要求",2),
    ACCEPT_MEMBER("【%s】将【%s】招收为仙门成员",3),
    IMMORTAL_GATE_ELDER_ADD("【%s】职位更改为长老",5),
    LEFT_IMMORTAL_GATE("【%s】主动脱离仙门",4),
    REMOVE_MEMBER("【%s】被【%s】逐出仙门",6),
    IMMORTAL_GATE_TOU("掌门移交",7),
    IMMORTAL_GATE_TOU_NEW("【%s】升任掌门",8),
    IMMORTAL_GATE_DESC("【%s】修改仙门简介",9),
    IMMORTAL_GATE_NAME("【%s】修改仙门名称",10),
    IMMORTAL_GATE_LEVEL("仙门自动升级为【%s】级",11),
    IMMORTAL_GATE_ELDER_DEL("【%s】职位更改为亲传弟子",12),
    IMMORTAL_GATE_ELDER_DEL_TWO("【%s】职位更改为试炼弟子",13),
    IMMORTAL_GATE_REFUSE_MEMBER("【%s】拒绝【%s】加入仙门",14),
    IMMORTAL_GATE_IMAGE_QIE("【%s】切换仙门头像",15),
    IMMORTAL_GATE_MEMBER_CONTRIBUTION("【%s】捐献【%s】个魔晶，仙门资金增加【%s】,个人贡献增加【%s】",16),
    IMMORTAL_GATE_UPDATE_ATTRIBUTE("【%s】修改了仙门的属性【%s】",17),
    IMMORTAL_GATE_FIGHTING_WIN_RECORD("本仙门抢占成功【%s】仙门的仙脉，可喜可贺！",18),
    IMMORTAL_GATE_FIGHTING_LOSE_RECORD("本仙门抢占失败【%s】仙门的仙脉，再接再厉！",19),
    IMMORTAL_GATE_FIGHTING_SHOU_WIN_RECORD("本仙门成功阻挡了【%s】仙门的抢占仙脉！仙门资金得到补偿",20),
    IMMORTAL_GATE_FIGHTING_SHOU_LOSE_RECORD("本仙门仙脉已被【%s】仙门抢占！",21),
    IMMORTAL_GATE_FIGHTING_WU_ZHU_RECORD("本仙门抢占成功一条仙脉！可喜可贺！",22),
    IMMORTAL_GATE_BUY_ITEM("【%s】购买了仙门道具【%s】,数量【%s】,共消耗【%s】资金！",23),
    IMMORTAL_GATE_IMAGE_QIE_BUY("【%s】切换仙门头像,消耗【%s】%s",24),
    ;

    private JSXMEnum(String name, int value) {
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

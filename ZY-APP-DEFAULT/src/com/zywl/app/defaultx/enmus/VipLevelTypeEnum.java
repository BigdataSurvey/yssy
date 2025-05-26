package com.zywl.app.defaultx.enmus;

import java.math.BigDecimal;

public enum VipLevelTypeEnum {
    VIP1(1, BigDecimal.valueOf(10)),
    VIP2(2, BigDecimal.valueOf(100)),
    VIP3(3,BigDecimal.valueOf(500)),
    VIP4(4, BigDecimal.valueOf(2000)),
    VIP5(5, BigDecimal.valueOf(5000)),
    VIP6(6, BigDecimal.valueOf(10000)),
    VIP7(7, BigDecimal.valueOf(20000)),
    VIP8(8, BigDecimal.valueOf(50000)),
    VIP9(9, BigDecimal.valueOf(100000)),
    VIP10(10, BigDecimal.valueOf(180000));

    private long  level;

    private BigDecimal value;

    VipLevelTypeEnum(long level, BigDecimal value) {
        this.level = level;
        this.value = value;
    }

    VipLevelTypeEnum() {
    }



    public long getLevel() {
        return level;
    }

    public void setLevel(long level) {
        this.level = level;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public static String getName(int value) {
        UserCapitalTypeEnum[] ems = UserCapitalTypeEnum.values();
        for (UserCapitalTypeEnum userCapitalTypeEnum : ems) {
            if (userCapitalTypeEnum.getValue()==value) {
                return userCapitalTypeEnum.getName();
            }
        }
        return "0";
    }


}

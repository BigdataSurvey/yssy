package com.zywl.app.defaultx.enmus;

import java.math.BigDecimal;

public enum ZytHighIncomeEnum {
    ONE(1, BigDecimal.valueOf(10)),
    TWO(2, BigDecimal.valueOf(100)),
    THREE(3,BigDecimal.valueOf(500)),
    FOUR(4, BigDecimal.valueOf(2000)),
    FIVE(5, BigDecimal.valueOf(5000)),
    SIX(6, BigDecimal.valueOf(10000));

    private long  level;

    private BigDecimal value;

    ZytHighIncomeEnum(long level, BigDecimal value) {
        this.level = level;
        this.value = value;
    }

    ZytHighIncomeEnum() {
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

    public static BigDecimal getValue(long level) {
        ZytHighIncomeEnum[] ems = ZytHighIncomeEnum.values();
        for (ZytHighIncomeEnum zytHighIncomeEnum : ems) {
            if (zytHighIncomeEnum.getLevel()==level) {
                return zytHighIncomeEnum.getValue();
            }
        }
        return BigDecimal.ZERO;
    }
}

package com.zywl.app.defaultx.enmus;

/**
 * @Author: lzx
 * @Create: 2026/2/11
 * @Version: V1.0
 * @Description: 交易行Tab列表枚举
 * @Task:
 */

public enum TradingTabEnum {
    SELL_MALL(0, "售卖商城"),
    ASKBUY_MALL(1, "求购商城"),
    MY_SELL(2, "我的售卖"),
    MY_ASKBUY(3, "我的求购");

    private final int value;
    private final String desc;

    TradingTabEnum(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public int getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }

    public static TradingTabEnum of(Integer v) {
        if (v == null) return null;
        for (TradingTabEnum e : values()) {
            if (e.value == v) return e;
        }
        return null;
    }

    /** tab -> dbType（0/1） */
    public int toDbType() {
        return (this == SELL_MALL || this == MY_SELL)
                ? TradingTypeEnum.sell.getValue()
                : TradingTypeEnum.askbuy.getValue();
    }

    /** 是否我的列表 */
    public boolean isMine() {
        return this == MY_SELL || this == MY_ASKBUY;
    }
}
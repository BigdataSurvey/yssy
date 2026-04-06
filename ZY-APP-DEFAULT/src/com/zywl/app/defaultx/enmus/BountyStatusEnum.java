package com.zywl.app.defaultx.enmus;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @Author: lzx
 * @Create: 2026/1/8
 * @Version: V1.2
 * @Description: 悬赏订单状态枚举及页签映射逻辑 （订单状态）
 */
public enum BountyStatusEnum {

    // 基础状态
    DOING(0, "进行中"),
    SUBMIT(1, "已提交待审核"),
    DONE(2, "已完成"),
    REJECT(3, "已驳回"),
    APPEAL(4, "申诉中"),
    CANCEL(5, "已取消"),
    TIMEOUT(6, "已超时");

    private final int code;
    private final String desc;

    BountyStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * 页签映射
     * 根据前端 Tab 类型获取对应的数据库状态列表
     */
    public static List<Integer> getCodesByTab(Integer tabId) {
        if (tabId == null || tabId == 0) return null;

        switch (tabId) {
            case 1:
                return Arrays.asList(DOING.code, REJECT.code, APPEAL.code);
            case 2:
                return Collections.singletonList(SUBMIT.code);
            case 3:
                return Collections.singletonList(DONE.code);
            case 4:
                return Collections.singletonList(REJECT.code);
            case 5:
                return Collections.singletonList(APPEAL.code);
            case 6:
                return Collections.singletonList(CANCEL.code);
            case 7:
                return Collections.singletonList(TIMEOUT.code);
            default:
                return null;
        }
    }

    public static BountyStatusEnum of(int code) {
        for (BountyStatusEnum s : values()) {
            if (s.code == code) return s;
        }
        return null;
    }
}

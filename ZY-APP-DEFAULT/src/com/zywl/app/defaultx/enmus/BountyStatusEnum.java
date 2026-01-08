package com.zywl.app.defaultx.enmus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @Author: lzx
 * @Create: 2026/1/8
 * @Version: V1.2
 * @Description: 悬赏订单状态枚举及页签映射逻辑
 */
@Getter
@AllArgsConstructor
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

    /**
     * 页签映射
     * 根据前端 Tab 类型获取对应的数据库状态列表
     */
    public static List<Integer> getCodesByTab(Integer tabId) {
        if (tabId == null || tabId == 0) return null;

        switch (tabId) {
            // 进行中 (包含: 进行中、驳回、申诉)
            case 1:
                return Arrays.asList(DOING.code, REJECT.code, APPEAL.code);
            // 待审核
            case 2:
                return Collections.singletonList(SUBMIT.code);
            // 已完成
            case 3:
                return Collections.singletonList(DONE.code);
            // 已驳回
            case 4:
                return Collections.singletonList(REJECT.code);
            // 申诉中
            case 5:
                return Collections.singletonList(APPEAL.code);
            // 已取消
            case 6:
                return Collections.singletonList(CANCEL.code);
            // 已超时
            case 7:
                return Collections.singletonList(TIMEOUT.code);
            default:
                return null;
        }
    }

    /**
     * 根据 code 获取枚举对象 (方便日志打印或转换)
     */
    public static BountyStatusEnum of(int code) {
        for (BountyStatusEnum s : values()) {
            if (s.code == code) return s;
        }
        return null;
    }
}
package com.zywl.app.base.bean.jingang;


import com.zywl.app.base.BaseBean;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;


/**
 * TKongkimBell 实体类
 */
@Data
public class BellRecord extends BaseBean {

    private Long id;

    private Long userId;

    /**
     * 兑换数量
     */
    private BigDecimal converCount;

    /**
     * 消耗积分
     */
    private BigDecimal consumeTotal;

    private Date createTime;
}

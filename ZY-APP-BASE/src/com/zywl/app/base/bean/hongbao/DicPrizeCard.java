package com.zywl.app.base.bean.hongbao;


import com.zywl.app.base.BaseBean;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 奖品配置表
 * **/
@Data
public class DicPrizeCard extends BaseBean {

    private Long id;

    private String name;

    private BigDecimal type;

    private String icon;

    private int total;

    private int numTotal;
}

package com.zywl.app.base.bean.hongbao;


import com.zywl.app.base.BaseBean;
import lombok.Data;

import java.math.BigDecimal;


@Data
public class DicPrize extends BaseBean {

    private Long id;

    private String name;

    private BigDecimal type;

    private String icon;

    private int total;

    private int numTotal;
}

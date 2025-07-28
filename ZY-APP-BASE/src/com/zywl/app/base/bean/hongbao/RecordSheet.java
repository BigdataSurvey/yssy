package com.zywl.app.base.bean.hongbao;

import com.zywl.app.base.BaseBean;
import lombok.Data;


import java.math.BigDecimal;
import java.util.Date;
import java.util.Random;


//记录表


@Data
public class RecordSheet extends BaseBean {


    private Long id;

    private Long userId;

    private Long redId;

    //红包名称
    private String name;

    private String headImg;

    //抢红包金额
    private BigDecimal amount;

    private BigDecimal redAmount;

    private Date createTime;

    //订单号
    private String orderNo;


    private Random random;

    private int isBoom;

    private String remark;

    private Date updateTime;
    private String state;

}

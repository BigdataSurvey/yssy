package com.zywl.app.base.bean.hongbao;

import com.alibaba.fastjson2.JSONArray;
import com.zywl.app.base.BaseBean;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
//红包表

public class RedEnvelope extends BaseBean {

    private Boolean isBomb; // 是否为炸弹红包

    private Long id;

    private Long userId;

    // 红包金额列表
    private JSONArray amount;

    //发放数量
    private int releasedQuantity;


    //发放红包奖励
    private BigDecimal redAward;

    //剩余金额
    private BigDecimal surplusAmount;

    private Date createTime;

    private Date updateTime;
    
    //抢红包金额
    private String allocationAmount;
    private int bombIndex;
    private Integer nowIndex;



    /*** 以下对象*/
    private  BigDecimal totalAmount; // 总金额（分）



    private int bombAmount; // 炸弹金额
    private int totalNumber;


    private int status;//状态

    private String remark;


}

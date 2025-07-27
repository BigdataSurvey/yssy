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

    private Integer userId;

    // 红包金额列表
    private JSONArray amount;

    //发放数量
    private String releasedQuantity;


    //发放红包奖励
    private String redAward;

    //剩余金额
    private Double surplusAmount;

    private Date createTime;

    private Date updateTime;
    
    //抢红包金额
    private String allocationAmount;
    private int bombIndex;
    private Integer nowIndex;

//    private Integer bombAmount; // 炸弹金额（分）


    /*** 以下对象*/
    private  Long totalAmount; // 总金额（分）
    private  int totalPeople; // 总人数


    private  double serviceRate; // 平台服务费率
    private  int serviceFee; // 平台手续费（分）
    private  int actualAmount; // 实际可分配金额（分）
    private boolean isGameOver; // 游戏是否结束

    private int bombAmount; // 炸弹金额
    private int totalNumber;

    private BigDecimal totalAmouns;
    
    private int status;//状态


}

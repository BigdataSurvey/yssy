package com.zywl.app.base.bean.hongbao;

import com.zywl.app.base.BaseBean;
import lombok.Data;


import java.math.BigDecimal;
import java.util.Date;
import java.util.Random;


//记录表


@Data
public class RecordSheet extends BaseBean {

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getRedId() {
        return redId;
    }

    public void setRedId(Long redId) {
        this.redId = redId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public Random getRandom() {
        return random;
    }

    public void setRandom(Random random) {
        this.random = random;
    }

    private Long id;

    private Long userId;

    private Long redId;

    //红包名称
    private String name;

    //抢红包金额
    private BigDecimal amount;

    private Date createTime;

    //订单号
    private String orderNo;


    private Random random;

    private int isBoom;

}

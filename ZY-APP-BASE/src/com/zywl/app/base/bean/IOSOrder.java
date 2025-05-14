package com.zywl.app.base.bean;

import com.zywl.app.base.BaseBean;
import com.zywl.app.base.util.DateUtil;

import java.math.BigDecimal;
import java.util.Date;

public class IOSOrder extends BaseBean {

    private Long userId;

    private Long id;

    private Long productId;

    private  String orderNo;

    private  String receiptData;

    private String inApp;

    private BigDecimal price;

    private Date createTime;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getReceiptData() {
        return receiptData;
    }

    public void setReceiptData(String receiptData) {
        this.receiptData = receiptData;
    }

    public String getInApp() {
        return inApp;
    }

    public void setInApp(String inApp) {
        this.inApp = inApp;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}

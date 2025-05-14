package com.zywl.app.base.bean;

import com.zywl.app.base.BaseBean;

public class DuoYouOrder extends BaseBean {

    private Long id;

    private Long userId;

    private String orderId;

    private String advertName;

    private String advertId;

    private String created;

    private String mediaIncome;

    private String memberIncome;

    private String mediaId;

    private String deviceId;

    private String content;

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

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getAdvertName() {
        return advertName;
    }

    public void setAdvertName(String advertName) {
        this.advertName = advertName;
    }

    public String getAdvertId() {
        return advertId;
    }

    public void setAdvertId(String advertId) {
        this.advertId = advertId;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getMediaIncome() {
        return mediaIncome;
    }

    public void setMediaIncome(String mediaIncome) {
        this.mediaIncome = mediaIncome;
    }

    public String getMemberIncome() {
        return memberIncome;
    }

    public void setMemberIncome(String memberIncome) {
        this.memberIncome = memberIncome;
    }

    public String getMediaId() {
        return mediaId;
    }

    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}

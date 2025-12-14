package com.zywl.app.base.bean;

import com.zywl.app.base.BaseBean;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @Author: lzx
 * @Create: 2025/12/xx
 * @Version: V1.0
 * @Description: 欢乐值事件明细表 t_user_joy_event
 * @Task:
 */
public class UserJoyEvent extends BaseBean {

    private Long id;

    /** 来源事件唯一ID */
    private String eventId;

    /** 收到欢乐值的上级用户ID */
    private Long receiverUserId;

    /** 行为来源的下级用户ID */
    private Long fromUserId;

    /** 来源类型 */
    private String sourceType;

    /** 本次事件给该上级记账的欢乐值 */
    private BigDecimal joyAmount;

    private Date createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public Long getReceiverUserId() {
        return receiverUserId;
    }

    public void setReceiverUserId(Long receiverUserId) {
        this.receiverUserId = receiverUserId;
    }

    public Long getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(Long fromUserId) {
        this.fromUserId = fromUserId;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public BigDecimal getJoyAmount() {
        return joyAmount;
    }

    public void setJoyAmount(BigDecimal joyAmount) {
        this.joyAmount = joyAmount;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}

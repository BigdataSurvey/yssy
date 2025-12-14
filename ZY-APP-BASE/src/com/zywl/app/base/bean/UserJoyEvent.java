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

    /** 第几代上级(1~5) */
    private Integer level;

    /** 触发道具quality(用于取JOY_PER_LEVEL) */
    private Integer itemQuality;

    /** 基础欢乐值(JOY_PER_LEVEL[quality]) */
    private BigDecimal baseJoy;

    /** 分润百分比(JOY_LEVEL_PERCENT[level]) */
    private Integer percent;

    /** 计算公式文本(用于审计) */
    private String calcExpr;

    /** 可读描述: from->receiver, level, percent, joy */
    private String calcDesc;

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

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Integer getItemQuality() {
        return itemQuality;
    }

    public void setItemQuality(Integer itemQuality) {
        this.itemQuality = itemQuality;
    }

    public BigDecimal getBaseJoy() {
        return baseJoy;
    }

    public void setBaseJoy(BigDecimal baseJoy) {
        this.baseJoy = baseJoy;
    }

    public Integer getPercent() {
        return percent;
    }

    public void setPercent(Integer percent) {
        this.percent = percent;
    }

    public String getCalcExpr() {
        return calcExpr;
    }

    public void setCalcExpr(String calcExpr) {
        this.calcExpr = calcExpr;
    }

    public String getCalcDesc() {
        return calcDesc;
    }

    public void setCalcDesc(String calcDesc) {
        this.calcDesc = calcDesc;
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

package com.zywl.app.base.bean;

import com.zywl.app.base.BaseBean;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author:
 * @time: 2026-01-03 13:02
 * @desc: 悬赏任务-接单/提交明细实体
 */

public class BountyTaskOrder extends BaseBean {

    private Long id;

    /** 任务ID */
    private Long taskId;

    /** 发布者用户ID（冗余字段，便于查询） */
    private Long publisherUserId;

    /** 接单用户ID */
    private Long userId;

    /** 订单号（用于幂等与日志关联，建议唯一） */
    private String orderNo;

    /**
     * 状态：
     * 0=待完成(已接单未提交)
     * 1=待审核(已提交等待发布者审核)
     * 2=已完成(审核通过)
     * 3=未通过(驳回，可重新提交或申诉)
     * 4=申诉中(等待后台处理)
     * 5=已取消(接单用户取消参与)
     * 6=已超时(超过接单时限)
     */
    private Integer status;

    /** 接单时间 */
    private Date takeTime;

    /** 截止时间（takeTime + 限时） */
    private Date deadlineTime;

    /** 提交时间 */
    private Date submitTime;

    /** 审核时间 */
    private Date auditTime;

    /** 提交材料图片（建议存JSON字符串数组，1~10张） */
    private String submitImgs;

    /** 参与者在活动/软件中的个人ID */
    private String submitUserId;

    /** 驳回原因 */
    private String rejectReason;

    /** 申诉理由 */
    private String appealReason;

    /** 申诉时间 */
    private Date appealTime;

    /**
     * 申诉处理状态：
     * 0=无申诉
     * 1=申诉中
     * 2=申诉通过
     * 3=申诉驳回
     */
    private Integer appealStatus;

    /** 申诉处理原因（后台填写） */
    private String appealHandleReason;

    /** 申诉处理时间 */
    private Date appealHandleTime;

    /** 申诉处理人（后台用户ID，若无后台账号体系可先存0） */
    private Long appealHandleUserId;


    /** 单价快照（用于结算，核心资产1001） */
    private BigDecimal unitPrice;

    /** 币种类型（固定1001，留作字段快照） */
    private Integer capitalType;

    private Date createTime;

    private Date updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Long getPublisherUserId() {
        return publisherUserId;
    }

    public void setPublisherUserId(Long publisherUserId) {
        this.publisherUserId = publisherUserId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getTakeTime() {
        return takeTime;
    }

    public void setTakeTime(Date takeTime) {
        this.takeTime = takeTime;
    }

    public Date getDeadlineTime() {
        return deadlineTime;
    }

    public void setDeadlineTime(Date deadlineTime) {
        this.deadlineTime = deadlineTime;
    }

    public Date getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(Date submitTime) {
        this.submitTime = submitTime;
    }

    public Date getAuditTime() {
        return auditTime;
    }

    public void setAuditTime(Date auditTime) {
        this.auditTime = auditTime;
    }

    public String getSubmitImgs() {
        return submitImgs;
    }

    public void setSubmitImgs(String submitImgs) {
        this.submitImgs = submitImgs;
    }

    public String getSubmitUserId() {
        return submitUserId;
    }

    public void setSubmitUserId(String submitUserId) {
        this.submitUserId = submitUserId;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    public String getAppealReason() {
        return appealReason;
    }

    public void setAppealReason(String appealReason) {
        this.appealReason = appealReason;
    }

    public Date getAppealTime() {
        return appealTime;
    }

    public void setAppealTime(Date appealTime) {
        this.appealTime = appealTime;
    }

    public Integer getAppealStatus() {
        return appealStatus;
    }

    public void setAppealStatus(Integer appealStatus) {
        this.appealStatus = appealStatus;
    }

    public String getAppealHandleReason() {
        return appealHandleReason;
    }

    public void setAppealHandleReason(String appealHandleReason) {
        this.appealHandleReason = appealHandleReason;
    }

    public Date getAppealHandleTime() {
        return appealHandleTime;
    }

    public void setAppealHandleTime(Date appealHandleTime) {
        this.appealHandleTime = appealHandleTime;
    }

    public Long getAppealHandleUserId() {
        return appealHandleUserId;
    }

    public void setAppealHandleUserId(Long appealHandleUserId) {
        this.appealHandleUserId = appealHandleUserId;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Integer getCapitalType() {
        return capitalType;
    }

    public void setCapitalType(Integer capitalType) {
        this.capitalType = capitalType;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}

package com.zywl.app.base.bean;
import com.zywl.app.base.BaseBean;
import java.math.BigDecimal;
import java.util.Date;
/**
 * @Author: lzx
 * @Create: 2026-01-03
 * @Version: V1.0
 * @Description: 悬赏任务-任务配置表实体
 */

public class BountyTask extends BaseBean {

    private Long id;

    /** 发布者用户ID */
    private Long userId;

    /** 任务标题 */
    private String taskTitle;

    /** 任务名称 */
    private String taskName;

    /** 任务描述 */
    private String taskDesc;

    /** 任务步骤（建议存JSON字符串，前端自行解析展示） */
    private String taskSteps;

    /** 任务视频URL（可为空） */
    private String videoUrl;

    /** 下载/二维码图片（建议存JSON字符串数组） */
    private String downloadImgs;

    /** 活动ID字段提示文案（例如："请输入你的XX软件ID"） */
    private String idTip;

    /** 单价（核心资产1001） */
    private BigDecimal unitPrice;

    /** 总名额 */
    private Integer quotaTotal;

    /** 剩余名额 */
    private Integer quotaRemain;

    /** 接单时限（小时，1~72） */
    private Integer takeLimitHours;

    /** 状态：1=上架/进行中；2=已取消；3=已结束（可选） */
    private Integer status;

    /** 热度/参与人数（用于排序与展示） */
    private Integer joinCount;

    /** 完成数量（用于展示与排序） */
    private Integer finishCount;

    /** 币种类型（固定1001，留作字段快照） */
    private Integer capitalType;

    /** 托管金额（单价*总名额） */
    private BigDecimal escrowAmount;

    /** 平台手续费金额（单次发布收取，不退） */
    private BigDecimal feeAmount;

    /** 平台手续费率（快照，默认0.05，可由t_config调整） */
    private BigDecimal feeRate;

    private Date createTime;

    private Date updateTime;

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

    public String getTaskTitle() {
        return taskTitle;
    }

    public void setTaskTitle(String taskTitle) {
        this.taskTitle = taskTitle;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTaskDesc() {
        return taskDesc;
    }

    public void setTaskDesc(String taskDesc) {
        this.taskDesc = taskDesc;
    }

    public String getTaskSteps() {
        return taskSteps;
    }

    public void setTaskSteps(String taskSteps) {
        this.taskSteps = taskSteps;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getDownloadImgs() {
        return downloadImgs;
    }

    public void setDownloadImgs(String downloadImgs) {
        this.downloadImgs = downloadImgs;
    }

    public String getIdTip() {
        return idTip;
    }

    public void setIdTip(String idTip) {
        this.idTip = idTip;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Integer getQuotaTotal() {
        return quotaTotal;
    }

    public void setQuotaTotal(Integer quotaTotal) {
        this.quotaTotal = quotaTotal;
    }

    public Integer getQuotaRemain() {
        return quotaRemain;
    }

    public void setQuotaRemain(Integer quotaRemain) {
        this.quotaRemain = quotaRemain;
    }

    public Integer getTakeLimitHours() {
        return takeLimitHours;
    }

    public void setTakeLimitHours(Integer takeLimitHours) {
        this.takeLimitHours = takeLimitHours;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getJoinCount() {
        return joinCount;
    }

    public void setJoinCount(Integer joinCount) {
        this.joinCount = joinCount;
    }

    public Integer getFinishCount() {
        return finishCount;
    }

    public void setFinishCount(Integer finishCount) {
        this.finishCount = finishCount;
    }

    public Integer getCapitalType() {
        return capitalType;
    }

    public void setCapitalType(Integer capitalType) {
        this.capitalType = capitalType;
    }

    public BigDecimal getEscrowAmount() {
        return escrowAmount;
    }

    public void setEscrowAmount(BigDecimal escrowAmount) {
        this.escrowAmount = escrowAmount;
    }

    public BigDecimal getFeeAmount() {
        return feeAmount;
    }

    public void setFeeAmount(BigDecimal feeAmount) {
        this.feeAmount = feeAmount;
    }

    public BigDecimal getFeeRate() {
        return feeRate;
    }

    public void setFeeRate(BigDecimal feeRate) {
        this.feeRate = feeRate;
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

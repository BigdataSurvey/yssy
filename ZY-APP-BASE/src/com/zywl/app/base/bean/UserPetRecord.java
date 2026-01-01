package com.zywl.app.base.bean;
import com.zywl.app.base.BaseBean;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @Author: lzx
 * @Create: 2025/12/30
 * @Version: V1.0
 * @Description: 养宠用户流水(幂等/补偿/审计)
 */
public class UserPetRecord extends BaseBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 狮子ID
     */
    private Long petId;

    /**
     * 0=STATE快照 1=SETTLE 2=DIVIDEND 3=BUY 4=FEED 5=CLAIM 6=UNLOCK
     */
    private Integer recordType;

    /**
     * 幂等key
     */
    private String recordKey;

    /**
     * 来源用户ID
     */
    private Long fromUserId;

    /**
     * 代数
     */
    private Integer level;

    /**
     * 饱腹值(小时)结算前
     */
    private Integer hungerBefore;

    /**
     * 饱腹值(小时)结算后
     */
    private Integer hungerAfter;

    /**
     * 本次主要资产类型
     */
    private Integer capitalType;

    /**
     * 本次主要资产变动
     */
    private BigDecimal amount;

    /**
     * 扩展资金变动(JSON)
     */
    private String changesJson;

    /**
     * 扩展数据(JSON)
     */
    private String payloadJson;

    /**
     * 0处理中/待补偿 1成功 2失败
     */
    private Integer status;

    /**
     * 失败原因
     */
    private String failMsg;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
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

    public Long getPetId() {
        return petId;
    }

    public void setPetId(Long petId) {
        this.petId = petId;
    }

    public Integer getRecordType() {
        return recordType;
    }

    public void setRecordType(Integer recordType) {
        this.recordType = recordType;
    }

    public String getRecordKey() {
        return recordKey;
    }

    public void setRecordKey(String recordKey) {
        this.recordKey = recordKey;
    }

    public Long getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(Long fromUserId) {
        this.fromUserId = fromUserId;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Integer getHungerBefore() {
        return hungerBefore;
    }

    public void setHungerBefore(Integer hungerBefore) {
        this.hungerBefore = hungerBefore;
    }

    public Integer getHungerAfter() {
        return hungerAfter;
    }

    public void setHungerAfter(Integer hungerAfter) {
        this.hungerAfter = hungerAfter;
    }

    public Integer getCapitalType() {
        return capitalType;
    }

    public void setCapitalType(Integer capitalType) {
        this.capitalType = capitalType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getChangesJson() {
        return changesJson;
    }

    public void setChangesJson(String changesJson) {
        this.changesJson = changesJson;
    }

    public String getPayloadJson() {
        return payloadJson;
    }

    public void setPayloadJson(String payloadJson) {
        this.payloadJson = payloadJson;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getFailMsg() {
        return failMsg;
    }

    public void setFailMsg(String failMsg) {
        this.failMsg = failMsg;
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

    @Override
    public String toString() {
        return "UserPetRecord{" +
                "id=" + id +
                ", userId=" + userId +
                ", petId=" + petId +
                ", recordType=" + recordType +
                ", recordKey='" + recordKey + '\'' +
                ", fromUserId=" + fromUserId +
                ", level=" + level +
                ", hungerBefore=" + hungerBefore +
                ", hungerAfter=" + hungerAfter +
                ", capitalType=" + capitalType +
                ", amount=" + amount +
                ", changesJson='" + changesJson + '\'' +
                ", payloadJson='" + payloadJson + '\'' +
                ", status=" + status +
                ", failMsg='" + failMsg + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}
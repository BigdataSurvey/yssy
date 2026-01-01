package com.zywl.app.base.bean;
import com.zywl.app.base.BaseBean;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @Author: lzx
 * @Create: 2025/12/30
 * @Version: V1.0
 * @Description: 养宠用户状态(用户维度)
 */
public class UserPetUser extends BaseBean implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 当前饱腹值(小时，用户维度)
     */
    private Integer hungerHours;

    /**
     * 待领取产出
     */
    private BigDecimal pendingYieldAmount;

    /**
     * 累计产出
     */
    private BigDecimal totalYieldAmount;

    /**
     * 今日分润收入
     */
    private BigDecimal todayDividendAmount;

    /**
     * 累计分润收入
     */
    private BigDecimal totalDividendAmount;

    /**
     * 3代是否已解锁(0/1)
     */
    private Integer unlockLv3;

    /**
     * 4代是否已解锁(0/1)
     */
    private Integer unlockLv4;

    /**
     * 5代是否已解锁(0/1)
     */
    private Integer unlockLv5;

    /**
     * 最后一次结算到的整点时间(小时粒度)
     */
    private Date lastSettleTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getHungerHours() {
        return hungerHours;
    }

    public void setHungerHours(Integer hungerHours) {
        this.hungerHours = hungerHours;
    }

    public BigDecimal getPendingYieldAmount() {
        return pendingYieldAmount;
    }

    public void setPendingYieldAmount(BigDecimal pendingYieldAmount) {
        this.pendingYieldAmount = pendingYieldAmount;
    }

    public BigDecimal getTotalYieldAmount() {
        return totalYieldAmount;
    }

    public void setTotalYieldAmount(BigDecimal totalYieldAmount) {
        this.totalYieldAmount = totalYieldAmount;
    }

    public BigDecimal getTodayDividendAmount() {
        return todayDividendAmount;
    }

    public void setTodayDividendAmount(BigDecimal todayDividendAmount) {
        this.todayDividendAmount = todayDividendAmount;
    }

    public BigDecimal getTotalDividendAmount() {
        return totalDividendAmount;
    }

    public void setTotalDividendAmount(BigDecimal totalDividendAmount) {
        this.totalDividendAmount = totalDividendAmount;
    }

    public Integer getUnlockLv3() {
        return unlockLv3;
    }

    public void setUnlockLv3(Integer unlockLv3) {
        this.unlockLv3 = unlockLv3;
    }

    public Integer getUnlockLv4() {
        return unlockLv4;
    }

    public void setUnlockLv4(Integer unlockLv4) {
        this.unlockLv4 = unlockLv4;
    }

    public Integer getUnlockLv5() {
        return unlockLv5;
    }

    public void setUnlockLv5(Integer unlockLv5) {
        this.unlockLv5 = unlockLv5;
    }

    public Date getLastSettleTime() {
        return lastSettleTime;
    }

    public void setLastSettleTime(Date lastSettleTime) {
        this.lastSettleTime = lastSettleTime;
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
        return "UserPetUser{" +
                "userId=" + userId +
                ", hungerHours=" + hungerHours +
                ", pendingYieldAmount=" + pendingYieldAmount +
                ", totalYieldAmount=" + totalYieldAmount +
                ", todayDividendAmount=" + todayDividendAmount +
                ", totalDividendAmount=" + totalDividendAmount +
                ", unlockLv3=" + unlockLv3 +
                ", unlockLv4=" + unlockLv4 +
                ", unlockLv5=" + unlockLv5 +
                ", lastSettleTime=" + lastSettleTime +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}
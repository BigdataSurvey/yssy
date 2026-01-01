package com.zywl.app.base.bean;
import com.zywl.app.base.BaseBean;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
/**
 * @Author: lzx
 * @Create: 2025/12/29
 * @Version: V1.0
 * @Description: 养宠全局配置表
 */
public class DicPet  extends BaseBean implements Serializable{

    private static final long serialVersionUID = 1L;

    /**
     * 配置ID
     */
    private Integer id;

    /**
     * 1启用 0停用
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 结算周期(分钟)
     */
    private Integer settleIntervalMin;

    /**
     * 饱腹值上限(天，全服统一10/20等)
     */
    private Integer hungerMaxDays;

    /**
     * 喂养增加饱腹(小时)
     */
    private Integer feedAddHours;

    /**
     * 购买狮子消耗的资产类型(capitalType，对应dic_item.type=4的itemId)
     */
    private Integer buyCostCapitalType;

    /**
     * 购买单只狮子消耗金额
     */
    private BigDecimal buyCostAmount;

    /**
     * 喂养消耗资产类型(capitalType)
     */
    private Integer feedCostCapitalType;

    /**
     * 喂养消耗金额/次
     */
    private BigDecimal feedCostAmount;

    /**
     * 狮子产出资产类型(capitalType)
     */
    private Integer yieldCapitalType;

    /**
     * 上级分润资产类型(capitalType)
     */
    private Integer dividendCapitalType;

    /**
     * 解锁贡献币种
     */
    private Integer unlockContribCapitalType;

    /**
     * 3代解锁直推人数阈值
     */
    private Integer unlockDirectLv3;

    /**
     * 4代解锁直推人数阈值
     */
    private Integer unlockDirectLv4;

    /**
     * 5代解锁直推人数阈值
     */
    private Integer unlockDirectLv5;

    /**
     * 3代解锁贡献阈值(只看1+2代贡献)
     */
    private BigDecimal unlockContribLv3;

    /**
     * 4代解锁贡献阈值(只看1+2代贡献)
     */
    private BigDecimal unlockContribLv4;

    /**
     * 5代解锁贡献阈值(只看1+2代贡献)
     */
    private BigDecimal unlockContribLv5;

    /**
     * 五级固定分润(JSON): [{"level":1,"amountPerHour":"0.05"},...]
     */
    private String profitFixedJson;

    /**
     * 收益曲线(JSON): [{"dayStart":1,"dayEnd":45,"amountPerHour":"8"},...]
     */
    private String yieldCurveJson;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getSettleIntervalMin() {
        return settleIntervalMin;
    }

    public void setSettleIntervalMin(Integer settleIntervalMin) {
        this.settleIntervalMin = settleIntervalMin;
    }

    public Integer getHungerMaxDays() {
        return hungerMaxDays;
    }

    public void setHungerMaxDays(Integer hungerMaxDays) {
        this.hungerMaxDays = hungerMaxDays;
    }

    public Integer getFeedAddHours() {
        return feedAddHours;
    }

    public void setFeedAddHours(Integer feedAddHours) {
        this.feedAddHours = feedAddHours;
    }

    public Integer getBuyCostCapitalType() {
        return buyCostCapitalType;
    }

    public void setBuyCostCapitalType(Integer buyCostCapitalType) {
        this.buyCostCapitalType = buyCostCapitalType;
    }

    public BigDecimal getBuyCostAmount() {
        return buyCostAmount;
    }

    public void setBuyCostAmount(BigDecimal buyCostAmount) {
        this.buyCostAmount = buyCostAmount;
    }

    public Integer getFeedCostCapitalType() {
        return feedCostCapitalType;
    }

    public void setFeedCostCapitalType(Integer feedCostCapitalType) {
        this.feedCostCapitalType = feedCostCapitalType;
    }

    public BigDecimal getFeedCostAmount() {
        return feedCostAmount;
    }

    public void setFeedCostAmount(BigDecimal feedCostAmount) {
        this.feedCostAmount = feedCostAmount;
    }

    public Integer getYieldCapitalType() {
        return yieldCapitalType;
    }

    public void setYieldCapitalType(Integer yieldCapitalType) {
        this.yieldCapitalType = yieldCapitalType;
    }

    public Integer getDividendCapitalType() {
        return dividendCapitalType;
    }

    public void setDividendCapitalType(Integer dividendCapitalType) {
        this.dividendCapitalType = dividendCapitalType;
    }

    public Integer getUnlockContribCapitalType() {
        return unlockContribCapitalType;
    }

    public void setUnlockContribCapitalType(Integer unlockContribCapitalType) {
        this.unlockContribCapitalType = unlockContribCapitalType;
    }

    public Integer getUnlockDirectLv3() {
        return unlockDirectLv3;
    }

    public void setUnlockDirectLv3(Integer unlockDirectLv3) {
        this.unlockDirectLv3 = unlockDirectLv3;
    }

    public Integer getUnlockDirectLv4() {
        return unlockDirectLv4;
    }

    public void setUnlockDirectLv4(Integer unlockDirectLv4) {
        this.unlockDirectLv4 = unlockDirectLv4;
    }

    public Integer getUnlockDirectLv5() {
        return unlockDirectLv5;
    }

    public void setUnlockDirectLv5(Integer unlockDirectLv5) {
        this.unlockDirectLv5 = unlockDirectLv5;
    }

    public BigDecimal getUnlockContribLv3() {
        return unlockContribLv3;
    }

    public void setUnlockContribLv3(BigDecimal unlockContribLv3) {
        this.unlockContribLv3 = unlockContribLv3;
    }

    public BigDecimal getUnlockContribLv4() {
        return unlockContribLv4;
    }

    public void setUnlockContribLv4(BigDecimal unlockContribLv4) {
        this.unlockContribLv4 = unlockContribLv4;
    }

    public BigDecimal getUnlockContribLv5() {
        return unlockContribLv5;
    }

    public void setUnlockContribLv5(BigDecimal unlockContribLv5) {
        this.unlockContribLv5 = unlockContribLv5;
    }

    public String getProfitFixedJson() {
        return profitFixedJson;
    }

    public void setProfitFixedJson(String profitFixedJson) {
        this.profitFixedJson = profitFixedJson;
    }

    public String getYieldCurveJson() {
        return yieldCurveJson;
    }

    public void setYieldCurveJson(String yieldCurveJson) {
        this.yieldCurveJson = yieldCurveJson;
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
        return "DicPet{" +
                "id=" + id +
                ", status=" + status +
                ", remark='" + remark + '\'' +
                ", settleIntervalMin=" + settleIntervalMin +
                ", hungerMaxDays=" + hungerMaxDays +
                ", feedAddHours=" + feedAddHours +
                ", buyCostCapitalType=" + buyCostCapitalType +
                ", buyCostAmount=" + buyCostAmount +
                ", feedCostCapitalType=" + feedCostCapitalType +
                ", feedCostAmount=" + feedCostAmount +
                ", yieldCapitalType=" + yieldCapitalType +
                ", dividendCapitalType=" + dividendCapitalType +
                ", unlockContribCapitalType=" + unlockContribCapitalType +
                ", unlockDirectLv3=" + unlockDirectLv3 +
                ", unlockDirectLv4=" + unlockDirectLv4 +
                ", unlockDirectLv5=" + unlockDirectLv5 +
                ", unlockContribLv3=" + unlockContribLv3 +
                ", unlockContribLv4=" + unlockContribLv4 +
                ", unlockContribLv5=" + unlockContribLv5 +
                ", profitFixedJson='" + profitFixedJson + '\'' +
                ", yieldCurveJson='" + yieldCurveJson + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}

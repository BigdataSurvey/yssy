package com.zywl.app.base.bean;

import java.util.Date;
import com.zywl.app.base.BaseBean;
import java.math.BigDecimal;

/**
 * @Author: lzx
 * @Create: 2026-01-19
 * @Version: V1.0
 * @Description: VIP配置Bean (对应表 dic_vip)
 * @Task:
 */
public class DicVip extends BaseBean {

    /** VIP类型 1=VIP1 2=VIP2*/
    private Integer vipType;

    /** VIP名称 */
    private String name;

    /** 开通/续期时长(天) */
    private Integer durationDays;

    /** 购买价格 (对应 decimal) */
    private BigDecimal price;

    /** 扣费资产类型ID (UserCapitalTypeEnum.value) */
    private Integer capitalTypeId;

    /** 权益文案(前端展示) */
    private String benefitText;

    /** 每日领取奖励JSON addReward */
    private String dailyReward;

    /** VIP卡道具ItemId (dic_item.id) */
    private Integer cardItemId;

    /** 状态 0禁用 1启用 */
    private Integer status;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;

    public Integer getVipType() {
        return vipType;
    }

    public void setVipType(Integer vipType) {
        this.vipType = vipType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getDurationDays() {
        return durationDays;
    }

    public void setDurationDays(Integer durationDays) {
        this.durationDays = durationDays;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getCapitalTypeId() {
        return capitalTypeId;
    }

    public void setCapitalTypeId(Integer capitalTypeId) {
        this.capitalTypeId = capitalTypeId;
    }

    public String getBenefitText() {
        return benefitText;
    }

    public void setBenefitText(String benefitText) {
        this.benefitText = benefitText;
    }

    public String getDailyReward() {
        return dailyReward;
    }

    public void setDailyReward(String dailyReward) {
        this.dailyReward = dailyReward;
    }

    public Integer getCardItemId() {
        return cardItemId;
    }

    public void setCardItemId(Integer cardItemId) {
        this.cardItemId = cardItemId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }

    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }


}
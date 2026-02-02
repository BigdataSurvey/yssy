package com.zywl.app.base.bean;

import com.zywl.app.base.BaseBean;
import java.util.Date;

/**
 * @Author: lzx
 * @Create: 2026-01-19
 * @Version: V1.0
 * @Description: VIP卡转赠记录实体 (对应表: t_vip_gift_record)
 * @Task:
 */
public class TVipGiftRecord extends BaseBean {

    /** 主键ID */
    private Long id;

    /** 转赠流水号(全局唯一) */
    private String giftNo;

    /** 转赠发起人用户ID */
    private Long fromUserId;

    /** 转赠接收人用户ID */
    private Long toUserId;

    /** VIP类型 1=VIP1 2=VIP2 */
    private Integer vipType;

    /** 卡道具ItemId(dic_item.id) */
    private Integer cardItemId;

    /** 转赠卡数量(通常=1) */
    private Integer cardNumber;

    /** 备注(可空) */
    private String remark;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getGiftNo() { return giftNo; }
    public void setGiftNo(String giftNo) { this.giftNo = giftNo; }

    public Long getFromUserId() { return fromUserId; }
    public void setFromUserId(Long fromUserId) { this.fromUserId = fromUserId; }

    public Long getToUserId() { return toUserId; }
    public void setToUserId(Long toUserId) { this.toUserId = toUserId; }

    public Integer getVipType() { return vipType; }
    public void setVipType(Integer vipType) { this.vipType = vipType; }

    public Integer getCardItemId() { return cardItemId; }
    public void setCardItemId(Integer cardItemId) { this.cardItemId = cardItemId; }

    public Integer getCardNumber() { return cardNumber; }
    public void setCardNumber(Integer cardNumber) { this.cardNumber = cardNumber; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }

    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
}
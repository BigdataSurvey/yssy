package com.zywl.app.base.bean;
import com.zywl.app.base.BaseBean;
import java.util.Date;

/**
 * @Author: lzx
 * @Create: 2026-01-19
 * @Version: V1.0
 * @Description: VIP每日领取记录Bean (对应表: r_vip_receive_record)
 * @Task:
 */
public class VipReceiveRecord extends BaseBean {

    /** 主键ID */
    private Long id;

    /** 用户ID */
    private Long userId;

    /** VIP类型 1=VIP1*/
    private Integer vipType;

    /** 领取日期 */
    private Date claimDate;

    /** 奖励快照JSON */
    private String reward;

    /** 订单号*/
    private String orderNo;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Integer getVipType() { return vipType; }
    public void setVipType(Integer vipType) { this.vipType = vipType; }

    public Date getClaimDate() { return claimDate; }
    public void setClaimDate(Date claimDate) { this.claimDate = claimDate; }

    public String getReward() { return reward; }
    public void setReward(String reward) { this.reward = reward; }

    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }

    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }

    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }

}
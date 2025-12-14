package com.zywl.app.base.bean;

import com.zywl.app.base.BaseBean;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @Author: lzx
 * @Create: 2025/12/xx
 * @Version: V1.0
 * @Description: 好友贡献欢乐值汇总表 t_user_joy_contrib
 */

public class UserJoyContrib extends BaseBean {

    private Long id;

    /** 被贡献的人，即上级用户ID */
    private Long receiverUserId;

    /** 贡献来源，下级/好友用户ID */
    private Long fromUserId;

    /** 累计贡献欢乐值 */
    private BigDecimal totalJoy;

    /** 今日贡献欢乐值 */
    private BigDecimal todayJoy;

    /** 今日日期：yyyyMMdd，用于跨天重置 todayJoy */
    private Integer todayDate;

    private Date createTime;

    private Date updateTime;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public BigDecimal getTotalJoy() {
        return totalJoy;
    }

    public void setTotalJoy(BigDecimal totalJoy) {
        this.totalJoy = totalJoy;
    }

    public BigDecimal getTodayJoy() {
        return todayJoy;
    }

    public void setTodayJoy(BigDecimal todayJoy) {
        this.todayJoy = todayJoy;
    }

    public Integer getTodayDate() {
        return todayDate;
    }

    public void setTodayDate(Integer todayDate) {
        this.todayDate = todayDate;
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

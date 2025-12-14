package com.zywl.app.base.bean;
import com.zywl.app.base.BaseBean;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @Author: lzx
 * @Create: 2025/12/14
 * @Version: V1.0
 * @Description: 用户欢乐值汇总表 t_user_joy
 */

public class UserJoy extends BaseBean {

    private Long id;

    /** 用户ID */
    private Long userId;

    /** 累计欢乐值（只增） */
    private BigDecimal totalJoy;

    /** 可用欢乐值（可增减，用于兑换气球等） */
    private BigDecimal availableJoy;

    /** 今日欢乐值 */
    private BigDecimal todayJoy;

    /** 今日日期：yyyyMMdd，用于跨天重置 */
    private Integer todayDate;

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

    public BigDecimal getTotalJoy() {
        return totalJoy;
    }

    public void setTotalJoy(BigDecimal totalJoy) {
        this.totalJoy = totalJoy;
    }

    public BigDecimal getAvailableJoy() {
        return availableJoy;
    }

    public void setAvailableJoy(BigDecimal availableJoy) {
        this.availableJoy = availableJoy;
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
package com.zywl.app.base.bean;
import com.zywl.app.base.BaseBean;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @Author: lzx
 * @Create: 2025/12/30
 * @Version: V1.0
 * @Description: 用户宠物明细
 */
public class UserPet extends BaseBean implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 狮子ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 1正常 0删除/失效(预留)
     */
    private Integer status;

    /**
     * 购买时间(用于生命周期/收益曲线)
     */
    private Date buyTime;

    /**
     * 该狮子累计产出(1003)
     */
    private BigDecimal totalYieldAmount;

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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getBuyTime() {
        return buyTime;
    }

    public void setBuyTime(Date buyTime) {
        this.buyTime = buyTime;
    }

    public BigDecimal getTotalYieldAmount() {
        return totalYieldAmount;
    }

    public void setTotalYieldAmount(BigDecimal totalYieldAmount) {
        this.totalYieldAmount = totalYieldAmount;
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
        return "UserPet{" +
                "id=" + id +
                ", userId=" + userId +
                ", status=" + status +
                ", buyTime=" + buyTime +
                ", totalYieldAmount=" + totalYieldAmount +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}
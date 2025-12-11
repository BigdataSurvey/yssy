package com.zywl.app.base.bean.card;
import com.zywl.app.base.BaseBean;
import java.util.Date;

/**
 * @Author: lzx
 * @Create: 2025/12/8
 * @Version: V1.0
 * @Description: 农场种地配置表 dic_farm
 * @Task:
 */

public class DicFarm extends BaseBean {

    private Long id;

    /** 种子道具ID（如1101、1201） */
    private Integer seedItemId;

    /** 产出奖励JSON **/
    private String reward;

    /** 生长时间，单位：秒 */
    private Integer growSeconds;

    /** 状态：1=启用，0=禁用 */
    private Integer status;

    private Date createTime;

    private Date updateTime;

    // ========== getter / setter ==========

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getSeedItemId() {
        return seedItemId;
    }

    public void setSeedItemId(Integer seedItemId) {
        this.seedItemId = seedItemId;
    }

    public Integer getGrowSeconds() {
        return growSeconds;
    }

    public void setGrowSeconds(Integer growSeconds) {
        this.growSeconds = growSeconds;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
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

    public String getReward() {
        return reward;
    }

    public void setReward(String reward) {
        this.reward = reward;
    }
}

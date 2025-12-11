package com.zywl.app.base.bean;
import com.zywl.app.base.BaseBean;
import lombok.Data;

import java.util.Date;
/**
 * @Author: lzx
 * @Create: 2025/12/8
 * @Version: V1.0
 * @Description: 用户农场土地表 t_user_farm_land
 * @Task:
 */

public class UserFarmLand extends BaseBean {

    private Long id;

    /** 用户ID */
    private Long userId;

    /** 地块编号：1~9 */
    private Integer landIndex;

    /** 当前种子的道具ID，空为无作物 */
    private Integer seedItemId;

    /** 种植时间 */
    private Date startTime;

    /** 成熟时间 */
    private Date endTime;

    /** 状态：0=空地，1=成长中，2=成熟（可收割） */
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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getLandIndex() {
        return landIndex;
    }

    public void setLandIndex(Integer landIndex) {
        this.landIndex = landIndex;
    }

    public Integer getSeedItemId() {
        return seedItemId;
    }

    public void setSeedItemId(Integer seedItemId) {
        this.seedItemId = seedItemId;
    }


    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
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
}

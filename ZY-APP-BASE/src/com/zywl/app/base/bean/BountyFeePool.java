package com.zywl.app.base.bean;
import com.zywl.app.base.BaseBean;

import java.util.Date;

/**
 * @Author: lzx
 * @Create: 2026-01-03
 * @Version: V1.0
 * @Description: 悬赏任务-手续费奖池
 */
public class BountyFeePool extends BaseBean {

    private Long id;

    /** 奖池余额（单位：分，long） */
    private Long poolCents;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPoolCents() {
        return poolCents;
    }

    public void setPoolCents(Long poolCents) {
        this.poolCents = poolCents;
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

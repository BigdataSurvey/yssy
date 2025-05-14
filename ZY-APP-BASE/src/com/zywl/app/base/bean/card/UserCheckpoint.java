package com.zywl.app.base.bean.card;

import com.zywl.app.base.BaseBean;

public class UserCheckpoint extends BaseBean {

    private Long userId;

    private Long checkpointId;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getCheckpointId() {
        return checkpointId;
    }

    public void setCheckpointId(Long checkpointId) {
        this.checkpointId = checkpointId;
    }
}
